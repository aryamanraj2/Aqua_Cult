//
//  MarketplaceModels.swift
//  aqua
//
//  Marketplace data models
//

import Foundation
import SwiftUI

enum ProductCategory: String, CaseIterable, Identifiable, Codable {
    case ointments = "Ointments & Treatments"
    case sensors = "IoT Sensors"
    case equipment = "Equipment"
    case feed = "Feed & Nutrition"
    case maintenance = "Maintenance"
    
    var id: String { rawValue }
    
    var icon: String {
        switch self {
        case .ointments: return "cross.vial.fill"
        case .sensors: return "sensor.fill"
        case .equipment: return "wrench.and.screwdriver.fill"
        case .feed: return "fish.fill"
        case .maintenance: return "gearshape.fill"
        }
    }
}

struct MarketplaceProduct: Identifiable, Hashable, Codable {
    let id: UUID
    var name: String
    var description: String
    var price: Double // INR
    var category: ProductCategory
    var imageName: String
    var rating: Double
    var inStock: Bool
    var unit: String // e.g., "bottle", "sensor", "kg"
    
    var formattedPrice: String {
        String(format: "₹%.2f", price)
    }
    
    init(name: String, description: String, price: Double, category: ProductCategory, imageName: String, rating: Double, inStock: Bool, unit: String) {
        self.id = UUID()
        self.name = name
        self.description = description
        self.price = price
        self.category = category
        self.imageName = imageName
        self.rating = rating
        self.inStock = inStock
        self.unit = unit
    }
}

struct CartItem: Identifiable, Hashable {
    let id = UUID()
    var product: MarketplaceProduct
    var quantity: Int
    
    var totalPrice: Double {
        product.price * Double(quantity)
    }
    
    var formattedTotal: String {
        String(format: "₹%.2f", totalPrice)
    }
}

// Sample marketplace data
extension MarketplaceProduct {
    static let sampleProducts: [MarketplaceProduct] = [
        // Ointments & Treatments
        MarketplaceProduct(
            name: "Anti-Bacterial Ointment",
            description: "Broad-spectrum treatment for bacterial infections in fish. Effective against fin rot and ulcers.",
            price: 899.00,
            category: .ointments,
            imageName: "pills.fill",
            rating: 4.5,
            inStock: true,
            unit: "bottle"
        ),
        MarketplaceProduct(
            name: "Fungal Treatment Solution",
            description: "Advanced formula to treat fungal infections. Safe for all freshwater and saltwater species.",
            price: 1299.00,
            category: .ointments,
            imageName: "drop.fill",
            rating: 4.8,
            inStock: true,
            unit: "bottle"
        ),
        MarketplaceProduct(
            name: "Wound Healing Gel",
            description: "Promotes rapid healing of wounds and injuries. Prevents secondary infections.",
            price: 749.00,
            category: .ointments,
            imageName: "bandage.fill",
            rating: 4.3,
            inStock: true,
            unit: "tube"
        ),
        MarketplaceProduct(
            name: "Parasite Control Treatment",
            description: "Eliminates external and internal parasites. Gentle on fish, tough on parasites.",
            price: 1499.00,
            category: .ointments,
            imageName: "allergens.fill",
            rating: 4.6,
            inStock: true,
            unit: "bottle"
        ),
        
        // IoT Sensors
        MarketplaceProduct(
            name: "Dissolved Oxygen Sensor",
            description: "High-precision DO sensor with real-time monitoring. Bluetooth connectivity, 0-20 mg/L range.",
            price: 8999.00,
            category: .sensors,
            imageName: "sensors.fill",
            rating: 4.7,
            inStock: true,
            unit: "unit"
        ),
        MarketplaceProduct(
            name: "pH & Temperature Probe",
            description: "Dual-function sensor for pH (0-14) and temperature (-5 to 50°C). WiFi enabled.",
            price: 6499.00,
            category: .sensors,
            imageName: "thermometer.medium",
            rating: 4.4,
            inStock: true,
            unit: "unit"
        ),
        MarketplaceProduct(
            name: "Ammonia Sensor (NH3/NH4+)",
            description: "Detects ammonia levels from 0-10 mg/L. Automatic alerts for dangerous levels.",
            price: 12999.00,
            category: .sensors,
            imageName: "aqi.medium",
            rating: 4.9,
            inStock: true,
            unit: "unit"
        ),
        MarketplaceProduct(
            name: "Turbidity Monitor",
            description: "Measures water clarity (0-1000 NTU). Essential for maintaining optimal conditions.",
            price: 7499.00,
            category: .sensors,
            imageName: "eye.trianglebadge.exclamationmark.fill",
            rating: 4.5,
            inStock: true,
            unit: "unit"
        ),
        MarketplaceProduct(
            name: "Salinity Sensor",
            description: "Measures salinity (0-70 ppt) with high accuracy. Ideal for marine aquaculture.",
            price: 5999.00,
            category: .sensors,
            imageName: "drop.triangle.fill",
            rating: 4.6,
            inStock: false,
            unit: "unit"
        ),
        MarketplaceProduct(
            name: "Multi-Parameter Sensor Hub",
            description: "All-in-one sensor: DO, pH, temperature, ammonia, and salinity. Premium solution.",
            price: 34999.00,
            category: .sensors,
            imageName: "sensor.tag.radiowaves.forward.fill",
            rating: 5.0,
            inStock: true,
            unit: "unit"
        ),
        
        // Equipment
        MarketplaceProduct(
            name: "Industrial Aerator",
            description: "High-capacity aerator for large tanks. 2HP motor, covers up to 5000L.",
            price: 18999.00,
            category: .equipment,
            imageName: "fan.fill",
            rating: 4.7,
            inStock: true,
            unit: "unit"
        ),
        MarketplaceProduct(
            name: "UV Sterilizer System",
            description: "Eliminates pathogens and algae. 55W UV-C lamp, suitable for 2000L tanks.",
            price: 15499.00,
            category: .equipment,
            imageName: "light.beacon.max.fill",
            rating: 4.8,
            inStock: true,
            unit: "unit"
        ),
        MarketplaceProduct(
            name: "Protein Skimmer",
            description: "Removes organic waste efficiently. Essential for marine setups.",
            price: 11999.00,
            category: .equipment,
            imageName: "humidity.fill",
            rating: 4.4,
            inStock: true,
            unit: "unit"
        ),
        MarketplaceProduct(
            name: "Automatic Feeder Pro",
            description: "Programmable feeding schedule. Holds 5kg feed, WiFi controlled.",
            price: 9499.00,
            category: .equipment,
            imageName: "clock.fill",
            rating: 4.6,
            inStock: true,
            unit: "unit"
        ),
        MarketplaceProduct(
            name: "Water Circulation Pump",
            description: "Maintains water flow and prevents stagnation. 5000L/hour capacity.",
            price: 7999.00,
            category: .equipment,
            imageName: "wind",
            rating: 4.5,
            inStock: true,
            unit: "unit"
        ),
        
        // Feed & Nutrition
        MarketplaceProduct(
            name: "Premium Protein Pellets",
            description: "High-protein feed (45%) for optimal growth. Suitable for salmon and cod.",
            price: 2499.00,
            category: .feed,
            imageName: "circle.grid.2x2.fill",
            rating: 4.7,
            inStock: true,
            unit: "5kg bag"
        ),
        MarketplaceProduct(
            name: "Vitamin-Enriched Feed",
            description: "Fortified with essential vitamins and minerals. Boosts immunity.",
            price: 2899.00,
            category: .feed,
            imageName: "pills.circle.fill",
            rating: 4.6,
            inStock: true,
            unit: "5kg bag"
        ),
        MarketplaceProduct(
            name: "Probiotic Fish Feed",
            description: "Contains beneficial bacteria for gut health. Reduces water pollution.",
            price: 3299.00,
            category: .feed,
            imageName: "leaf.fill",
            rating: 4.8,
            inStock: true,
            unit: "5kg bag"
        ),
        
        // Maintenance
        MarketplaceProduct(
            name: "Water Testing Kit Pro",
            description: "Complete kit for testing pH, ammonia, nitrite, nitrate, and hardness.",
            price: 1999.00,
            category: .maintenance,
            imageName: "testtube.2",
            rating: 4.5,
            inStock: true,
            unit: "kit"
        ),
        MarketplaceProduct(
            name: "Tank Cleaning Brush Set",
            description: "Professional-grade brushes for thorough tank cleaning. Set of 5.",
            price: 899.00,
            category: .maintenance,
            imageName: "paintbrush.fill",
            rating: 4.3,
            inStock: true,
            unit: "set"
        ),
        MarketplaceProduct(
            name: "Net Set (3 Sizes)",
            description: "Durable nets for fish handling. Small, medium, and large sizes included.",
            price: 649.00,
            category: .maintenance,
            imageName: "app.connected.to.app.below.fill",
            rating: 4.4,
            inStock: true,
            unit: "set"
        ),
        MarketplaceProduct(
            name: "Water Conditioner (5L)",
            description: "Removes chlorine and heavy metals. Safe for all aquaculture systems.",
            price: 1299.00,
            category: .maintenance,
            imageName: "drop.degreesign.fill",
            rating: 4.6,
            inStock: true,
            unit: "bottle"
        ),
    ]
}
