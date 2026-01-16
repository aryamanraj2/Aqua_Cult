//
//  TankAnalysis.swift
//  aqua
//
//  Created by AI Assistant on 01/11/25.
//

import Foundation
import SwiftUI

struct TankAnalysis: Codable {
    let overview: AnalysisOverview
    let alerts: AnalysisSection
    let monitor: AnalysisSection
    let good: AnalysisSection
    let recommendedProducts: [MarketplaceProduct]
    let spokenSummary: String // Concise summary for text-to-speech
    
    // Legacy fields for backward compatibility during transition
    let diseaseRisks: [DiseaseRisk]
    let harvestInsights: HarvestInsights
    let waterConcerns: [WaterConcern]
    let recommendations: [Recommendation]
    let productsNeeded: [MarketplaceProduct]
}

struct AnalysisSection: Codable {
    let title: String
    let items: [AnalysisItem]
    
    var hasContent: Bool {
        !items.isEmpty
    }
}

struct AnalysisItem: Codable, Identifiable {
    let id: String
    let type: ItemType
    let title: String
    let description: String
    let priority: Priority?
    let details: [String]
    let actionItems: [String]
    let metadata: [String: String]?
    
    enum ItemType: String, Codable, CaseIterable {
        case diseaseRisk = "disease_risk"
        case waterConcern = "water_concern"
        case harvestInsight = "harvest_insight"
        case recommendation = "recommendation"
        case achievement = "achievement"
        case status = "status"
    }
    
    enum Priority: String, Codable, CaseIterable {
        case low = "low"
        case medium = "medium"
        case high = "high"
        case critical = "critical"
        
        var color: Color {
            switch self {
            case .low: return .aquaGreen
            case .medium: return .aquaYellow
            case .high: return .orange
            case .critical: return .aquaRed
            }
        }
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.id = (try? container.decode(String.self, forKey: .id)) ?? UUID().uuidString
        self.type = try container.decode(ItemType.self, forKey: .type)
        self.title = try container.decode(String.self, forKey: .title)
        self.description = try container.decode(String.self, forKey: .description)
        self.priority = try? container.decode(Priority.self, forKey: .priority)
        self.details = (try? container.decode([String].self, forKey: .details)) ?? []
        self.actionItems = (try? container.decode([String].self, forKey: .actionItems)) ?? []
        self.metadata = try? container.decode([String: String].self, forKey: .metadata)
    }
}

struct AnalysisOverview: Codable {
    let healthScore: Int // 0-100
    let status: String // "Excellent", "Good", "Needs Attention", "Critical"
    let summary: String
    let keyMetrics: [String: String]
}

struct DiseaseRisk: Codable, Identifiable {
    var id: String { diseaseName }
    let diseaseName: String
    let riskLevel: String // "Low", "Moderate", "High", "Critical"
    let probability: Int // 0-100
    let symptoms: [String]
    let causes: [String]
    let prevention: [String]
    let treatment: [String]?
}

struct HarvestInsights: Codable {
    let estimatedHarvestDate: String?
    let currentGrowthStage: String
    let expectedYield: String?
    let optimalConditions: [String]
    let growthRecommendations: [String]
}

struct WaterConcern: Codable, Identifiable {
    var id: String { parameter }
    let parameter: String
    let currentValue: String
    let optimalRange: String
    let severity: String // "Normal", "Monitor", "Action Required", "Critical"
    let impact: String
    let actionItems: [String]
}

struct Recommendation: Codable, Identifiable {
    let id: String
    let category: String // "Water Quality", "Feeding", "Disease Prevention", etc.
    let priority: String // "High", "Medium", "Low"
    let title: String
    let description: String
    let actionItems: [String]
    
    enum CodingKeys: String, CodingKey {
        case category, priority, title, description, actionItems
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.id = UUID().uuidString
        self.category = try container.decode(String.self, forKey: .category)
        self.priority = try container.decode(String.self, forKey: .priority)
        self.title = try container.decode(String.self, forKey: .title)
        self.description = try container.decode(String.self, forKey: .description)
        self.actionItems = try container.decode([String].self, forKey: .actionItems)
    }
    
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(category, forKey: .category)
        try container.encode(priority, forKey: .priority)
        try container.encode(title, forKey: .title)
        try container.encode(description, forKey: .description)
        try container.encode(actionItems, forKey: .actionItems)
    }
}



// Extension for color mapping
extension String {
    var riskColor: Color {
        switch self.lowercased() {
        case "low", "normal":
            return .aquaGreen
        case "moderate", "monitor":
            return .aquaYellow
        case "high", "action required":
            return .orange
        case "critical":
            return .aquaRed
        default:
            return .mediumGray
        }
    }
    
    var statusColor: Color {
        switch self.lowercased() {
        case "excellent", "optimal":
            return .aquaGreen
        case "good":
            return .green
        case "needs attention", "caution":
            return .aquaYellow
        case "critical":
            return .aquaRed
        default:
            return .mediumGray
        }
    }
}
