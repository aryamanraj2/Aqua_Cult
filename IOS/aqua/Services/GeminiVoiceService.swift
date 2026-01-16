
import Foundation

class GeminiVoiceService {
    static let shared = GeminiVoiceService()
    
    private var apiKey: String {
        // Forcing usage of APIConfig to debug "expired key" issue
        // This ensures strictly the key in APIConfig.swift is used
        return APIConfig.geminiAPIKey
    }
    
    private let availableProducts = MarketplaceProduct.sampleProducts
    private let endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    
    private init() {}
    
    func processVoiceQuery(
        query: String,
        conversationContext: [String],
        tanks: [Tank]
    ) async throws -> VoiceQueryResponse {
        let prompt = buildVoicePrompt(
            query: query,
            context: conversationContext,
            tanks: tanks
        )
        
        let requestBody: [String: Any] = [
            "contents": [
                [
                    "parts": [
                        ["text": prompt]
                    ]
                ]
            ],
            "generationConfig": [
                "temperature": 0.8,
                "topK": 40,
                "topP": 0.95,
                "maxOutputTokens": 4096,
                "responseMimeType": "application/json"
            ]
        ]
        
        guard !apiKey.isEmpty else {
            throw GeminiError.missingAPIKey
        }
        
        guard let url = URL(string: "\(endpoint)?key=\(apiKey)") else {
            throw GeminiError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw GeminiError.invalidResponse()
        }
        
        guard httpResponse.statusCode == 200 else {
            let bodyText = String(data: data, encoding: .utf8) ?? "<unreadable body>"
            throw GeminiError.apiError(statusCode: httpResponse.statusCode, body: bodyText)
        }
        
        return try parseVoiceResponse(data)
    }
    
    private func buildVoicePrompt(
        query: String,
        context: [String],
        tanks: [Tank]
    ) -> String {
        // Build tank information
        let tanksInfo = tanks.map { tank in
            let wq = tank.waterQuality
            return """
            Tank: \(tank.name)
            - Species: \(tank.species.joined(separator: ", "))
            - Stage: \(tank.currentStage)
            - Volume: \(tank.dimensions.volume) m³
            - Temperature: \(wq.temperature)°C
            - pH: \(wq.pH)
            - Dissolved Oxygen: \(wq.dissolvedOxygen) mg/L
            - Ammonia: \(wq.ammonia) mg/L
            - Salinity: \(wq.salinity) ppt
            - Turbidity: \(wq.turbidity) NTU
            - Status: \(wq.status.rawValue)
            """
        }.joined(separator: "\n\n")
        
        // Build product information
        let productsInfo = availableProducts.map { product in
            "- \"\(product.name)\" (Category: \(product.category.rawValue), Price: \(product.formattedPrice), Description: \(product.description))"
        }.joined(separator: "\n")
        
        // Build conversation context
        let contextString = context.isEmpty ? "No previous context" : context.suffix(6).joined(separator: "\n")
        
        return """
        You are AquaBot, an expert aquaculture AI assistant with deep knowledge in fish farming, disease management, and water quality optimization.
        
        You are having a conversation with a user about their aquaculture tanks. Maintain conversational context and provide helpful, accurate responses.
        
        AVAILABLE TANKS:
        \(tanksInfo)
        
        AVAILABLE PRODUCTS IN MARKETPLACE:
        \(productsInfo)
        
        CONVERSATION CONTEXT (recent messages):
        \(contextString)
        
        USER QUERY:
        \(query)
        
        INSTRUCTIONS:
        1. Provide a conversational, natural response to the user's query
        2. Use the tank data to give specific, accurate information
        3. If discussing predictions, forecasts, or disease risks, base them on the actual water quality parameters
        4. If the user asks about precautions or cures, recommend relevant products from the marketplace
        5. Maintain context from previous messages in the conversation
        6. Be concise but informative - aim for 2-4 sentences unless more detail is specifically requested
        7. If recommending products, choose maximum 2-3 most relevant ones
        8. Format your response naturally, as if speaking to the user
        
        RESPONSE FORMAT (JSON):
        {
          "textResponse": "<natural conversational response to the user>",
          "recommendedProducts": ["<exact product name 1>", "<exact product name 2>"]
        }
        
        EXAMPLES:
        - If user asks about Tank A's prediction: Analyze Tank A's parameters and predict potential issues based on actual data
        - If user asks about precautions: Provide actionable advice and suggest relevant products
        - If user asks follow-up questions: Use conversation context to provide coherent responses
        - If discussing disease: Recommend treatment products like "Pro-Bio Aqua Solution" or "pH Stabilizer Pro"
        
        Return ONLY valid JSON, no additional text.
        """
    }
    
    private struct TempVoiceResponse: Codable {
        let textResponse: String
        let recommendedProducts: [String]
    }
    
    private func parseVoiceResponse(_ data: Data) throws -> VoiceQueryResponse {
        struct GeminiResponse: Codable {
            let candidates: [Candidate]?
            let error: APIError?
            
            struct Candidate: Codable {
                let content: Content
                let finishReason: String?
                
                struct Content: Codable {
                    let parts: [Part]?
                    
                    struct Part: Codable {
                        let text: String
                    }
                }
            }
            
            struct APIError: Codable {
                let code: Int
                let message: String
                let status: String
            }
        }
        
        let geminiResponse = try JSONDecoder().decode(GeminiResponse.self, from: data)
        
        if let apiError = geminiResponse.error {
            throw GeminiError.apiError(statusCode: apiError.code, body: "\(apiError.status): \(apiError.message)")
        }
        
        guard let firstCandidate = geminiResponse.candidates?.first,
              let parts = firstCandidate.content.parts,
              let firstPart = parts.first else {
            throw GeminiError.noContent
        }
        
        var jsonText = firstPart.text.trimmingCharacters(in: .whitespacesAndNewlines)
        
        // Remove markdown code blocks
        if jsonText.hasPrefix("```json") {
            jsonText = jsonText.replacingOccurrences(of: "```json", with: "")
            jsonText = jsonText.replacingOccurrences(of: "```", with: "")
            jsonText = jsonText.trimmingCharacters(in: .whitespacesAndNewlines)
        } else if jsonText.hasPrefix("```") {
            jsonText = jsonText.replacingOccurrences(of: "```", with: "")
            jsonText = jsonText.trimmingCharacters(in: .whitespacesAndNewlines)
        }
        
        guard let jsonData = jsonText.data(using: .utf8) else {
            throw GeminiError.invalidJSON(details: "Could not convert text to data")
        }
        
        let tempResponse = try JSONDecoder().decode(TempVoiceResponse.self, from: jsonData)
        
        // Convert product names to MarketplaceProduct objects
        let products = tempResponse.recommendedProducts.compactMap { productName in
            availableProducts.first { $0.name.lowercased() == productName.lowercased() }
        }
        
        return VoiceQueryResponse(
            textResponse: tempResponse.textResponse,
            recommendedProducts: products
        )
    }
}
