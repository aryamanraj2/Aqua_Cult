
import Foundation
import SwiftUI

struct Tank: Identifiable {
    let id = UUID()
    var name: String
    var species: [String] // e.g., ["Salmon"], ["Cod"], or ["Salmon", "Cod"] for mixed
    var dimensions: TankDimensions
    var currentStage: String
    var sensorID: String?
    var waterQuality: WaterQuality
    var imageName: String?
}

struct TankDimensions {
    var length: Double // meters
    var width: Double // meters
    var depth: Double // meters
    
    var volume: Double {
        length * width * depth // cubic meters
    }
}

struct WaterQuality {
    var temperature: Double // Celsius
    var pH: Double
    var dissolvedOxygen: Double // mg/L
    var ammonia: Double // mg/L
    var salinity: Double // ppt (parts per thousand)
    var turbidity: Double // NTU
    var status: WaterQualityStatus
}

enum WaterQualityStatus: String {
    case optimal = "Optimal"
    case good = "Good"
    case caution = "Caution"
    case critical = "Critical"
    
    var color: Color {
        switch self {
        case .optimal: return .aquaGreen
        case .good: return .green
        case .caution: return .aquaYellow
        case .critical: return .aquaRed
        }
    }
}

struct EnvironmentalData {
    var airTemperature: Double // Celsius
    var condition: String // e.g., "Partly Cloudy"
    var humidity: Double // percentage
    var windSpeed: Double // km/h
    var precipitation: Double // percentage
    var icon: String // SF Symbol name
}

// Sample data for preview
extension Tank {
    static let sampleTanks = [
        Tank(
            name: "Tank A",
            species: ["Salmon"],
            dimensions: TankDimensions(length: 10, width: 8, depth: 3),
            currentStage: "Grow-out",
            sensorID: "SEN-001",
            waterQuality: WaterQuality(
                temperature: 16.5,
                pH: 7.2,
                dissolvedOxygen: 7.5,
                ammonia: 0.02,
                salinity: 32.0,
                turbidity: 2.1,
                status: .optimal
            ),
            imageName: "tank_salmon"
        ),
        Tank(
            name: "Tank B",
            species: ["Cod"],
            dimensions: TankDimensions(length: 12, width: 10, depth: 3.5),
            currentStage: "Nursery",
            sensorID: "SEN-002",
            waterQuality: WaterQuality(
                temperature: 14.2,
                pH: 7.8,
                dissolvedOxygen: 6.8,
                ammonia: 0.05,
                salinity: 33.5,
                turbidity: 3.2,
                status: .good
            ),
            imageName: "tank_cod"
        ),
        Tank(
            name: "Tank C",
            species: ["Salmon", "Cod"],
            dimensions: TankDimensions(length: 15, width: 12, depth: 4),
            currentStage: "Hatchery",
            sensorID: "SEN-003",
            waterQuality: WaterQuality(
                temperature: 18.5,
                pH: 8.2,
                dissolvedOxygen: 5.5,
                ammonia: 0.12,
                salinity: 31.0,
                turbidity: 4.8,
                status: .caution
            ),
            imageName: "tank_mixed"
        )
    ]
}

extension EnvironmentalData {
    static let sample = EnvironmentalData(
        airTemperature: 22,
        condition: "Partly Cloudy",
        humidity: 68,
        windSpeed: 12,
        precipitation: 25,
        icon: "cloud.sun.fill"
    )
}

// MARK: - User Profile

struct UserProfile {
    let id: UUID
    var fullName: String
    var mobile: String
    var address: String
    var pincode: String
    let location: String
    let totalTanks: Int
    let totalVolume: Double
    let experienceYears: Int
    let appVersion: String
    let joinDate: Date
    
    static let sampleProfile = UserProfile(
        id: UUID(),
        fullName: "Mukesh Kumar",
        mobile: "+91 98765 43210",
        address: "152, Rashbehari Avenue, Near Lake Market, Ballygunge",
        pincode: "700029",
        location: "West Bengal, India",
        totalTanks: 5,
        totalVolume: 12.5,
        experienceYears: 3,
        appVersion: "1.0.0",
        joinDate: Date().addingTimeInterval(-365 * 24 * 60 * 60 * 2) // 2 years ago
    )
}
