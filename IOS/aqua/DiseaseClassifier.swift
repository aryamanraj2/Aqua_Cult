//
//  DiseaseClassifier.swift
//  aqua
//
//  Created by aryaman jaiswal on 31/10/25.
//

import Foundation
import UIKit
import Combine

class DiseaseClassifier {
    static let shared = DiseaseClassifier()

    // Backend API configuration
    private let baseURL = "http://192.168.1.21:8000/api/v1"  // Change to your backend URL

    // Disease information database - mapped to the 7 outputs from fish_disease.keras model
    private let diseaseInfo: [String: (description: String, recommendations: [String])] = [
        "Bacterial Red disease": (
            description: "Bacterial hemorrhagic septicemia causing red lesions and hemorrhaging. Common bacterial infection in fish.",
            recommendations: [
                "Quarantine infected fish immediately",
                "Use antibiotics (oxytetracycline or florfenicol) as prescribed",
                "Improve water quality and increase aeration",
                "Consider salt bath treatment (1-3%)",
                "Consult with aquaculture veterinarian"
            ]
        ),
        "Bacterial diseases - Aeromoniasis": (
            description: "Systemic bacterial infection caused by Aeromonas species, leading to septicemia and organ damage.",
            recommendations: [
                "Antibiotic treatment (oxytetracycline, sulfonamides)",
                "Improve water quality immediately",
                "Increase dissolved oxygen levels",
                "Reduce stocking density if overcrowded",
                "Implement salt treatment protocol"
            ]
        ),
        "Bacterial gill disease": (
            description: "Bacterial infection affecting gill tissue, impairing respiratory function and leading to oxygen deprivation.",
            recommendations: [
                "Improve water quality immediately",
                "Increase aeration to boost dissolved oxygen",
                "Reduce feeding temporarily",
                "Apply antibiotics (chloramine-T, hydrogen peroxide bath)",
                "Monitor ammonia levels closely"
            ]
        ),
        "Fungal diseases Saprolegniasis": (
            description: "Fungal infection caused by Saprolegnia species, appearing as cotton-like growth on fish body, fins, or eggs.",
            recommendations: [
                "Salt bath treatment (0.5-1% for 10-15 minutes)",
                "Use antifungal medication (malachite green, methylene blue)",
                "Improve water quality and circulation",
                "Remove dead tissue if severe",
                "Handle fish carefully to avoid injuries"
            ]
        ),
        "Healthy Fish": (
            description: "Fish appears healthy with no visible signs of disease or distress. Continue regular monitoring and maintenance.",
            recommendations: [
                "Continue regular water quality monitoring",
                "Maintain optimal water parameters",
                "Follow regular feeding schedule",
                "Monitor weekly for any changes",
                "Keep up preventive care practices"
            ]
        ),
        "Parasitic diseases": (
            description: "External or internal parasites affecting fish health, including protozoa, worms, and crustaceans.",
            recommendations: [
                "Identify specific parasite type",
                "Treat with appropriate antiparasitic medication",
                "For Ich: raise temperature to 30°C with salt treatment",
                "Quarantine new fish for 2-3 weeks",
                "Disinfect equipment between uses"
            ]
        ),
        "Viral diseases White tail disease": (
            description: "Viral infection causing white discoloration of the tail and systemic illness. Highly contagious and often fatal.",
            recommendations: [
                "Quarantine infected fish immediately",
                "No specific antiviral treatment - supportive care only",
                "Cull severely affected fish to prevent spread",
                "Disinfect all equipment and tanks thoroughly",
                "Source fish from certified disease-free suppliers"
            ]
        )
    ]

    private init() {
        // No model loading needed - using backend API
    }
    
    func classifyImage(_ image: UIImage, completion: @escaping (Result<DiseaseResult, Error>) -> Void) {
        // Convert image to JPEG data
        guard let imageData = image.jpegData(compressionQuality: 0.8) else {
            completion(.failure(ClassificationError.invalidImage))
            return
        }

        // Create multipart form data request
        let url = URL(string: "\(baseURL)/analysis/disease-detection/ml-only")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"

        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        var body = Data()

        // Add image file
        body.append("--\(boundary)\r\n".data(using: .utf8)!)
        body.append("Content-Disposition: form-data; name=\"file\"; filename=\"fish.jpg\"\r\n".data(using: .utf8)!)
        body.append("Content-Type: image/jpeg\r\n\r\n".data(using: .utf8)!)
        body.append(imageData)
        body.append("\r\n".data(using: .utf8)!)
        body.append("--\(boundary)--\r\n".data(using: .utf8)!)

        request.httpBody = body

        // Make the request
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            if let error = error {
                DispatchQueue.main.async {
                    completion(.failure(error))
                }
                return
            }

            guard let data = data else {
                DispatchQueue.main.async {
                    completion(.failure(ClassificationError.noResults))
                }
                return
            }

            do {
                // Parse JSON response
                let decoder = JSONDecoder()
                let apiResponse = try decoder.decode(DiseaseDetectionResponse.self, from: data)

                // Get the top prediction
                if let topPrediction = apiResponse.predictions.first {
                    // Debug logging
                    print("📱 iOS: Received prediction from backend:")
                    print("   Name: \(topPrediction.name)")
                    print("   Confidence: \(topPrediction.confidence)")
                    print("   Description from backend: \(topPrediction.description ?? "nil")")
                    print("   Prevention count: \(topPrediction.prevention?.count ?? 0)")

                    let diseaseResult = self?.createDiseaseResult(from: topPrediction) ?? DiseaseResult(
                        diseaseName: "Unknown",
                        confidence: 0.0,
                        description: "Unable to classify the image",
                        recommendations: ["Please try with a clearer image", "Consult with a fish health expert"],
                        recommendedProducts: []
                    )

                    DispatchQueue.main.async {
                        completion(.success(diseaseResult))
                    }
                } else {
                    DispatchQueue.main.async {
                        completion(.failure(ClassificationError.noResults))
                    }
                }
            } catch {
                DispatchQueue.main.async {
                    completion(.failure(error))
                }
            }
        }.resume()
    }
    
    private func createDiseaseResult(from prediction: DiseasePrediction) -> DiseaseResult {
        // Use backend data if available, otherwise fall back to local data
        let localInfo = diseaseInfo[prediction.name]

        let description: String
        let recommendations: [String]

        // Prefer backend description if available
        if let backendDescription = prediction.description, !backendDescription.isEmpty {
            description = backendDescription
            print("   ✓ Using description from backend")
        } else if let localDesc = localInfo?.description {
            description = localDesc
            print("   ⚠️ Using LOCAL description (backend sent none)")
        } else {
            description = "Disease detected but specific information not available."
            print("   ⚠️ Using FALLBACK description")
        }

        // Prefer backend prevention/recommendations if available
        if let backendPrevention = prediction.prevention, !backendPrevention.isEmpty {
            recommendations = backendPrevention
            print("   ✓ Using \(backendPrevention.count) recommendations from backend")
        } else if let localRecs = localInfo?.recommendations {
            recommendations = localRecs
            print("   ⚠️ Using LOCAL recommendations (backend sent none)")
        } else {
            recommendations = ["Consult with a fish health specialist", "Monitor fish closely", "Improve water quality"]
            print("   ⚠️ Using FALLBACK recommendations")
        }

        return DiseaseResult(
            diseaseName: prediction.name,
            confidence: prediction.confidence,
            description: description,
            recommendations: recommendations,
            recommendedProducts: getRecommendedProducts(for: prediction.name)
        )
    }
    
    private func getRecommendedProducts(for diseaseName: String) -> [MarketplaceProduct] {
        // Get products from marketplace based on disease type
        let allProducts = MarketplaceProduct.sampleProducts

        switch diseaseName {
        case "Healthy Fish":
            return allProducts.filter { product in
                product.category == .feed ||
                (product.category == .maintenance && product.name.lowercased().contains("water"))
            }.prefix(3).map { $0 }

        case "Bacterial Red disease", "Bacterial diseases - Aeromoniasis", "Bacterial gill disease":
            return allProducts.filter { product in
                product.category == .ointments &&
                (product.name.lowercased().contains("antibacterial") ||
                 product.name.lowercased().contains("antibiotic") ||
                 product.description.lowercased().contains("bacterial"))
            }.prefix(4).map { $0 }

        case "Fungal diseases Saprolegniasis":
            return allProducts.filter { product in
                product.category == .ointments &&
                (product.name.lowercased().contains("antifungal") ||
                 product.name.lowercased().contains("fungus") ||
                 product.description.lowercased().contains("fungal"))
            }.prefix(4).map { $0 }

        case "Parasitic diseases":
            return allProducts.filter { product in
                product.category == .ointments &&
                (product.name.lowercased().contains("antiparasitic") ||
                 product.name.lowercased().contains("parasite") ||
                 product.description.lowercased().contains("parasitic"))
            }.prefix(4).map { $0 }

        case "Viral diseases White tail disease":
            return allProducts.filter { product in
                (product.category == .feed && product.description.lowercased().contains("immunity")) ||
                (product.category == .ointments && product.description.lowercased().contains("viral"))
            }.prefix(3).map { $0 }

        default:
            // For unknown diseases, return general health products
            return allProducts.filter { product in
                product.category == .ointments || product.category == .feed
            }.prefix(3).map { $0 }
        }
    }
}

// MARK: - API Response Models

struct DiseaseDetectionResponse: Codable, Sendable {
    let predictions: [DiseasePrediction]
    let message: String?
}

struct DiseasePrediction: Codable, Sendable {
    let name: String
    let confidence: Double
    let description: String?
    let causes: [String]?
    let symptoms: [String]?
    let treatment: String?
    let prevention: [String]?
}

// MARK: - Error Types

enum ClassificationError: LocalizedError {
    case modelNotLoaded
    case invalidImage
    case noResults

    var errorDescription: String? {
        switch self {
        case .modelNotLoaded:
            return "Unable to connect to the disease detection service. Please check your internet connection."
        case .invalidImage:
            return "The selected image could not be processed. Please try selecting a different image."
        case .noResults:
            return "No classification results were returned. Please try with a clearer image of the fish."
        }
    }
}
