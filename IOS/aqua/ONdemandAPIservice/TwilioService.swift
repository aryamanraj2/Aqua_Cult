//
//  TwilioService.swift
//  aqua
//
//  Twilio SMS Service for sending notifications
//  Handles Tank Analysis and Disease Detection SMS alerts
//

import Foundation

// MARK: - SMS Status Enum
enum SMSStatus: Equatable {
    case idle
    case sending
    case success(String)
    case failure(String)
    
    var message: String {
        switch self {
        case .idle:
            return ""
        case .sending:
            return "Sending SMS notification..."
        case .success(let msg):
            return msg
        case .failure(let msg):
            return msg
        }
    }
    
    var isSuccess: Bool {
        if case .success = self { return true }
        return false
    }
}

// MARK: - Twilio Service
class TwilioService {
    static let shared = TwilioService()
    
    private init() {}
    
    // MARK: - Tank Analysis SMS
    
    /// Sends SMS notification with tank analysis results
    /// - Parameters:
    ///   - analysis: The TankAnalysis result from Gemini AI
    ///   - tankName: Optional tank name for context
    /// - Returns: SMSStatus indicating success or failure
    func sendTankAnalysisSMS(analysis: TankAnalysis, tankName: String = "Tank") async -> SMSStatus {
        print("📱 SMS: Preparing tank analysis notification...")
        
        let message = formatTankAnalysisMessage(analysis: analysis, tankName: tankName)
        return await sendSMS(message: message, context: "Tank Analysis")
    }
    
    /// Formats the tank analysis into an SMS-friendly message
    private func formatTankAnalysisMessage(analysis: TankAnalysis, tankName: String) -> String {
        var message = "AQUA CULT - TANK ANALYSIS\n\n"
        
        // Health Score
        message += "Health Score: \(analysis.overview.healthScore)/100\n"
        message += "Status: \(analysis.overview.status)\n\n"
        
        // Key Alerts (if any)
        if !analysis.alerts.items.isEmpty {
            message += "ALERTS:\n"
            for (index, alert) in analysis.alerts.items.prefix(3).enumerated() {
                message += "- \(alert.title)\n"
                if index >= 2 { break }
            }
            message += "\n"
        }
        
        // Monitoring Points
        if !analysis.monitor.items.isEmpty {
            message += "MONITOR:\n"
            for (index, item) in analysis.monitor.items.prefix(2).enumerated() {
                message += "- \(item.title)\n"
                if index >= 1 { break }
            }
            message += "\n"
        }
        
        // Good Status (brief)
        if !analysis.good.items.isEmpty {
            message += "GOOD: \(analysis.good.items.count) positive indicators\n\n"
        }
        
        // Key Metrics
        if let waterQuality = analysis.overview.keyMetrics["waterQuality"] {
            message += "Water: \(waterQuality)\n"
        }
        if let diseaseRisk = analysis.overview.keyMetrics["diseaseRisk"] {
            message += "Disease Risk: \(diseaseRisk)\n"
        }
        
        // Timestamp
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM HH:mm"
        message += "\nSent: \(formatter.string(from: Date()))"
        
        return message
    }
    
    // MARK: - Disease Detection SMS
    
    /// Sends SMS notification with disease detection results
    /// - Parameter result: The DiseaseResult from classification
    /// - Returns: SMSStatus indicating success or failure
    func sendDiseaseDetectionSMS(result: DiseaseResult) async -> SMSStatus {
        print("📱 SMS: Preparing disease detection notification...")
        
        let message = formatDiseaseDetectionMessage(result: result)
        return await sendSMS(message: message, context: "Disease Detection")
    }
    
    /// Formats the disease detection result into an SMS-friendly message
    private func formatDiseaseDetectionMessage(result: DiseaseResult) -> String {
        var message = "AQUA CULT - DISEASE ALERT\n\n"
        
        // Disease Name and Confidence
        message += "Disease: \(result.diseaseName)\n"
        message += "Confidence: \(Int(result.confidence * 100))%\n\n"
        
        // Brief Description (truncated for SMS)
        if !result.description.isEmpty {
            let truncatedDesc = String(result.description.prefix(100))
            message += "\(truncatedDesc)...\n\n"
        }
        
        // Prevention/Recommendations
        if !result.recommendations.isEmpty {
            message += "PREVENTION:\n"
            for (index, rec) in result.recommendations.prefix(3).enumerated() {
                let truncatedRec = String(rec.prefix(50))
                message += "- \(truncatedRec)\n"
                if index >= 2 { break }
            }
        }
        
        // Timestamp
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM HH:mm"
        message += "\nSent: \(formatter.string(from: Date()))"
        
        return message
    }
    
    // MARK: - Core SMS Sending
    
    /// Core method to send SMS via Twilio API
    private func sendSMS(message: String, context: String) async -> SMSStatus {
        print("📱 SMS: Sending \(context) notification to \(TwilioConfig.recipientPhoneNumber)...")
        
        guard let url = URL(string: TwilioConfig.smsEndpoint) else {
            print("❌ SMS: Invalid Twilio endpoint URL")
            return .failure("Invalid configuration")
        }
        
        // Create request
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        // Basic Auth header
        let credentials = "\(TwilioConfig.accountSID):\(TwilioConfig.authToken)"
        guard let credentialsData = credentials.data(using: .utf8) else {
            print("❌ SMS: Failed to encode credentials")
            return .failure("Authentication error")
        }
        let base64Credentials = credentialsData.base64EncodedString()
        request.setValue("Basic \(base64Credentials)", forHTTPHeaderField: "Authorization")
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
        
        // Form body
        let bodyParams = [
            "From": TwilioConfig.twilioPhoneNumber,
            "To": TwilioConfig.recipientPhoneNumber,
            "Body": message
        ]
        
        let bodyString = bodyParams
            .map { key, value in
                let encodedKey = key.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? key
                let encodedValue = value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? value
                return "\(encodedKey)=\(encodedValue)"
            }
            .joined(separator: "&")
        
        request.httpBody = bodyString.data(using: .utf8)
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            
            guard let httpResponse = response as? HTTPURLResponse else {
                print("❌ SMS: Invalid response type")
                return .failure("Invalid response")
            }
            
            // Log response
            if let responseString = String(data: data, encoding: .utf8) {
                print("📱 SMS: Response (\(httpResponse.statusCode)): \(responseString.prefix(200))")
            }
            
            // Check status code
            switch httpResponse.statusCode {
            case 200...299:
                // Parse response to get message SID
                if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let sid = json["sid"] as? String {
                    print("✅ SMS: \(context) SMS sent successfully!")
                    print("   📨 Message SID: \(sid)")
                    print("   📞 To: \(TwilioConfig.recipientPhoneNumber)")
                    return .success("SMS sent to \(formatPhoneForDisplay(TwilioConfig.recipientPhoneNumber))")
                }
                print("✅ SMS: \(context) SMS sent successfully!")
                return .success("SMS sent successfully")
                
            case 400:
                print("❌ SMS: Bad request - check phone numbers and message format")
                return .failure("Invalid request format")
                
            case 401:
                print("❌ SMS: Authentication failed - check Account SID and Auth Token")
                return .failure("Authentication failed")
                
            case 403:
                print("❌ SMS: Forbidden - check Twilio account permissions")
                return .failure("Permission denied")
                
            case 404:
                print("❌ SMS: Endpoint not found - check Account SID")
                return .failure("Service not found")
                
            case 429:
                print("❌ SMS: Rate limited - too many requests")
                return .failure("Rate limited")
                
            default:
                print("❌ SMS: HTTP error \(httpResponse.statusCode)")
                return .failure("Server error (\(httpResponse.statusCode))")
            }
            
        } catch {
            print("❌ SMS: Network error - \(error.localizedDescription)")
            return .failure("Network error: \(error.localizedDescription)")
        }
    }
    
    // MARK: - Helper Methods
    
    private func getHealthEmoji(score: Int) -> String {
        switch score {
        case 80...100: return "🟢"
        case 60..<80: return "🟡"
        case 40..<60: return "🟠"
        default: return "🔴"
        }
    }
    
    private func getConfidenceEmoji(confidence: Double) -> String {
        switch confidence {
        case 0.8...1.0: return "🟢"
        case 0.6..<0.8: return "🟡"
        default: return "🟠"
        }
    }
    
    private func formatPhoneForDisplay(_ phone: String) -> String {
        // Format: +91 XXXXX XXXXX -> +91 XXX...XX
        if phone.count > 8 {
            let prefix = String(phone.prefix(6))
            let suffix = String(phone.suffix(2))
            return "\(prefix)...\(suffix)"
        }
        return phone
    }
}
