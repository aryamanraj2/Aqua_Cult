
import Foundation

class GeminiService {
    static let shared = GeminiService()
    
  // API key is read from (in priority):
  // 1) Environment variable `GEMINI_API_KEY` (useful for local testing / CI)
  // 2) Info.plist entry `GEMINI_API_KEY` (can be used for simulator builds)
  // 3) APIConfig.swift hardcoded value (fallback for device builds)
  // This avoids committing the key into source code. See GEMINI_SETUP_GUIDE.md for setup.
  private var apiKey: String {
    
    // Fallback to hardcoded API key for device builds
    return APIConfig.geminiAPIKey
  }
  
  // Available marketplace products for AI recommendations
  private let availableProducts = MarketplaceProduct.sampleProducts
  // Use the v1beta generateContent endpoint with gemini-2.5-flash model
  // Note: gemini-1.5-flash is no longer available; use gemini-2.5-flash or gemini-flash-latest
  private let endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    
    private init() {}
    
    func analyzeTank(_ tank: Tank) async throws -> TankAnalysis {
        let prompt = buildAnalysisPrompt(for: tank)
        
        let requestBody: [String: Any] = [
            "contents": [
                [
                    "parts": [
                        ["text": prompt]
                    ]
                ]
            ],
            "generationConfig": [
                "temperature": 0.7,
                "topK": 40,
                "topP": 0.95,
                "maxOutputTokens": 16384,  // Increased from 2048 to 16384 for longer responses
                "responseMimeType": "application/json"
            ]
        ]
        
    // Ensure API key is provided
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
      // Include response body in the error to aid debugging (e.g. 404 details)
      let bodyText = String(data: data, encoding: .utf8) ?? "<unreadable body>"
      throw GeminiError.apiError(statusCode: httpResponse.statusCode, body: bodyText)
    }
        
        return try parseGeminiResponse(data)
    }
    
    private func buildAnalysisPrompt(for tank: Tank) -> String {
        let wq = tank.waterQuality
        let species = tank.species.joined(separator: ", ")
        
        // Create a list of available products for AI to choose from
        let productsInfo = availableProducts.map { product in
            """
            - "\(product.name)" (Category: \(product.category.rawValue), Price: \(product.formattedPrice), Description: \(product.description))
            """
        }.joined(separator: "\n")
        
        return """
        You are an expert aquaculture consultant with deep knowledge in fish farming, disease management, and water quality optimization.
        
        Analyze the following tank data and provide comprehensive insights categorized into specific sections in JSON format.
        
        Tank Information:
        - Species: \(species)
        - Current Stage: \(tank.currentStage)
        - Volume: \(tank.dimensions.volume) cubic meters
        
        Water Quality Parameters:
        - Temperature: \(wq.temperature)°C
        - pH: \(wq.pH)
        - Dissolved Oxygen: \(wq.dissolvedOxygen) mg/L
        - Ammonia: \(wq.ammonia) mg/L
        - Salinity: \(wq.salinity) ppt
        - Turbidity: \(wq.turbidity) NTU
        - Status: \(wq.status.rawValue)
        
        Available Products in Our Marketplace:
        \(productsInfo)
        
        CATEGORIZATION GUIDELINES:
        - ALERTS: Critical issues, high disease risks, immediate action required items
        - MONITOR: Moderate concerns, things to watch, preventive measures
        - GOOD: Positive findings, optimal conditions, achievements, healthy indicators
        - RECOMMENDED PRODUCTS: Specific products from marketplace to address issues or optimize performance
        
        Provide a detailed analysis in the following JSON structure:
        {
          "overview": {
            "healthScore": <0-100>,
            "status": "<Excellent|Good|Needs Attention|Critical>",
            "summary": "<2-3 sentence overall assessment>",
            "keyMetrics": {
              "waterQuality": "<status>",
              "diseaseRisk": "<level>",
              "growthRate": "<assessment>"
            }
          },
          "spokenSummary": "<A concise 3-4 sentence audio-friendly summary highlighting overall health score, critical alerts (if any), key concerns, and positive points. This will be spoken aloud to the user, so make it natural and conversational. Focus on actionable insights and risks.>",
          "alerts": {
            "title": "Alerts",
            "items": [
              {
                "id": "<unique_id>",
                "type": "<disease_risk|water_concern|recommendation>",
                "title": "<short descriptive title>",
                "description": "<detailed description>",
                "priority": "<critical|high>",
                "details": ["<detail1>", "<detail2>"],
                "actionItems": ["<urgent_action1>", "<urgent_action2>"],
                "metadata": {
                  "currentValue": "<if water concern>",
                  "optimalRange": "<if water concern>",
                  "probability": "<if disease risk>"
                }
              }
            ]
          },
          "monitor": {
            "title": "Monitor",
            "items": [
              {
                "id": "<unique_id>",
                "type": "<disease_risk|water_concern|recommendation|harvest_insight>",
                "title": "<short descriptive title>",
                "description": "<detailed description>",
                "priority": "<medium|low>",
                "details": ["<detail1>", "<detail2>"],
                "actionItems": ["<monitoring_action1>", "<monitoring_action2>"],
                "metadata": {
                  "parameter": "<if applicable>",
                  "frequency": "<if monitoring needed>"
                }
              }
            ]
          },
          "good": {
            "title": "Good",
            "items": [
              {
                "id": "<unique_id>",
                "type": "<achievement|status|harvest_insight>",
                "title": "<positive finding title>",
                "description": "<positive assessment>",
                "priority": null,
                "details": ["<positive_detail1>", "<positive_detail2>"],
                "actionItems": ["<maintenance_action1>", "<maintenance_action2>"],
                "metadata": {
                  "metric": "<if applicable>",
                  "value": "<if applicable>"
                }
              }
            ]
          },
          "recommendedProducts": [
            "<exact product name from available products list>"
          ],
          "diseaseRisks": [
            {
              "diseaseName": "<disease name>",
              "riskLevel": "<Low|Moderate|High|Critical>",
              "probability": <0-100>,
              "symptoms": ["<symptom1>", "<symptom2>"],
              "causes": ["<cause1>", "<cause2>"],
              "prevention": ["<prevention1>", "<prevention2>"],
              "treatment": ["<treatment1>", "<treatment2>"]
            }
          ],
          "harvestInsights": {
            "estimatedHarvestDate": "<date or null>",
            "currentGrowthStage": "<assessment of stage>",
            "expectedYield": "<estimate or null>",
            "optimalConditions": ["<condition1>", "<condition2>"],
            "growthRecommendations": ["<recommendation1>", "<recommendation2>"]
          },
          "waterConcerns": [
            {
              "parameter": "<parameter name>",
              "currentValue": "<value with unit>",
              "optimalRange": "<range>",
              "severity": "<Normal|Monitor|Action Required|Critical>",
              "impact": "<description of impact>",
              "actionItems": ["<action1>", "<action2>"]
            }
          ],
          "recommendations": [
            {
              "category": "<category>",
              "priority": "<High|Medium|Low>",
              "title": "<short title>",
              "description": "<detailed description>",
              "actionItems": ["<action1>", "<action2>"]
            }
          ],
          "productsNeeded": [
            "<exact product name from available products list>"
          ]
        }
        
        Focus on:
        1. Analyze tank conditions comprehensively for \(species) at \(tank.currentStage) stage
        2. Categorize ALL findings into the appropriate sections:
           - ALERTS: Critical disease risks, dangerous parameter levels, urgent actions needed
           - MONITOR: Moderate concerns, preventive measures, parameters to watch
           - GOOD: Optimal conditions, healthy indicators, positive achievements
        3. Provide specific, actionable recommendations
        4. Suggest relevant products from marketplace for identified issues
        5. Include detailed metadata for each item (values, ranges, probabilities, etc.)
        
        CATEGORIZATION RULES:
        - ALERTS: Items with "critical" or "high" priority, immediate risks, values far outside optimal ranges
        - MONITOR: Items with "medium" or "low" priority, preventive measures, values approaching limits  
        - GOOD: Positive findings, optimal conditions, achievements, things working well
        - If a section would be empty, still include it with an empty "items" array
        - POPULATE THE NEW SECTION STRUCTURE (alerts, monitor, good) as the PRIMARY response format
        - The legacy fields (diseaseRisks, harvestInsights, etc.) are for backward compatibility only
        
        ITEM TYPE MAPPING:
        - disease_risk: Disease-related concerns and risks
        - water_concern: Water quality parameter issues
        - harvest_insight: Growth stage and harvest-related information
        - recommendation: General recommendations and suggestions
        - achievement: Positive accomplishments and optimal conditions
        - status: Current status assessments
        
        IMPORTANT: 
        1. For recommendedProducts and productsNeeded, use EXACT product names from the marketplace list above
        2. Focus on populating the NEW categorized sections (alerts, monitor, good) with detailed items
        3. Each item should have appropriate metadata relevant to its type
        4. Generate unique IDs for each item (use descriptive strings like "high_ammonia_alert")
        
        Return ONLY valid JSON, no additional text.
        """
    }
    
    // Temporary structure for parsing JSON response
    private struct TempTankAnalysis: Codable {
        let overview: AnalysisOverview
        let alerts: AnalysisSection
        let monitor: AnalysisSection
        let good: AnalysisSection
        let recommendedProducts: [String] // Product names as strings
        let spokenSummary: String
        
        // Legacy fields for backward compatibility
        let diseaseRisks: [DiseaseRisk]
        let harvestInsights: HarvestInsights
        let waterConcerns: [WaterConcern]
        let recommendations: [Recommendation]
        let productsNeeded: [String] // Product names as strings
    }
    
    private func parseGeminiResponse(_ data: Data) throws -> TankAnalysis {
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
        
        // First, try to decode the Gemini response
        let geminiResponse: GeminiResponse
        do {
            geminiResponse = try JSONDecoder().decode(GeminiResponse.self, from: data)
        } catch {
            // If decoding fails, show the raw response for debugging
            let rawResponse = String(data: data, encoding: .utf8) ?? "Unable to read response"
            throw GeminiError.invalidResponse(details: "Failed to decode Gemini response: \(error.localizedDescription). Raw response: \(rawResponse)")
        }
        
        // Check for API errors
        if let apiError = geminiResponse.error {
            throw GeminiError.apiError(statusCode: apiError.code, body: "\(apiError.status): \(apiError.message)")
        }
        
        // Extract the content
        guard let firstCandidate = geminiResponse.candidates?.first else {
            throw GeminiError.noContent
        }
        
        // Check finish reason
        if let finishReason = firstCandidate.finishReason, finishReason == "MAX_TOKENS" {
            throw GeminiError.maxTokensReached
        }
        
        guard let parts = firstCandidate.content.parts,
              let firstPart = parts.first else {
            throw GeminiError.noContent
        }
        
        var jsonText = firstPart.text.trimmingCharacters(in: .whitespacesAndNewlines)
        
        // Remove markdown code blocks if present
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
        
        // Try to decode the analysis
        do {
            // First decode into a temporary structure
            let tempAnalysis = try JSONDecoder().decode(TempTankAnalysis.self, from: jsonData)
            
            // Convert product names to MarketplaceProduct objects
            let recommendedProducts = tempAnalysis.recommendedProducts.compactMap { productName in
                availableProducts.first { $0.name.lowercased() == productName.lowercased() }
            }
            
            // Legacy products for backward compatibility
            let legacyProducts = tempAnalysis.productsNeeded.compactMap { productName in
                availableProducts.first { $0.name.lowercased() == productName.lowercased() }
            }
            
            // Create the final analysis with actual MarketplaceProduct objects
            let analysis = TankAnalysis(
                overview: tempAnalysis.overview,
                alerts: tempAnalysis.alerts,
                monitor: tempAnalysis.monitor,
                good: tempAnalysis.good,
                recommendedProducts: recommendedProducts,
                spokenSummary: tempAnalysis.spokenSummary,
                diseaseRisks: tempAnalysis.diseaseRisks,
                harvestInsights: tempAnalysis.harvestInsights,
                waterConcerns: tempAnalysis.waterConcerns,
                recommendations: tempAnalysis.recommendations,
                productsNeeded: legacyProducts
            )
            
            return analysis
        } catch {
            throw GeminiError.invalidJSON(details: "Failed to parse TankAnalysis: \(error.localizedDescription). JSON text: \(String(jsonText.prefix(500)))")
        }
    }
}

enum GeminiError: LocalizedError {
  case invalidURL
  case invalidResponse(details: String? = nil)
  case apiError(statusCode: Int, body: String?)
  case noContent
  case invalidJSON(details: String? = nil)
  case missingAPIKey
  case maxTokensReached

  var errorDescription: String? {
    switch self {
    case .invalidURL:
      return "Invalid API URL"
    case .invalidResponse(let details):
      if let details = details {
        return "Invalid response from server: \(details)"
      }
      return "Invalid response from server"
    case .apiError(let statusCode, let body):
      if let body = body {
        return "API error (status \(statusCode)): \(body)"
      } else {
        return "API error with status code: \(statusCode)"
      }
    case .missingAPIKey:
      return "Missing Gemini API key. Set GEMINI_API_KEY in environment or add GEMINI_API_KEY in Info.plist."
    case .noContent:
      return "No content in response"
    case .invalidJSON(let details):
      if let details = details {
        return "Could not parse AI response: \(details)"
      }
      return "Could not parse AI response"
    case .maxTokensReached:
      return "Response was too long and exceeded token limit. The analysis has been configured to use more tokens. Please try again."
    }
  }
}
