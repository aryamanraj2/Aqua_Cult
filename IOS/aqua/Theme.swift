

import SwiftUI

extension Color {
    // Ocean-themed color palette
    static let deepOcean = Color(hex: "#001F3F")
    static let oceanBlue = Color(hex: "#1B6F9A")
    static let mediumBlue = Color(hex: "#0A4D92")
    static let darkBlack = Color(hex: "#000000")
    
    // Subtle blue gradient colors
    static let subtleBlueLight = Color(hex: "#E8F4F8")
    static let subtleBlueMid = Color(hex: "#D4E9F2")
    static let subtleBlueAccent = Color(hex: "#B8DAEB")
    
    // Utility colors
    static let aquaGreen = Color(hex: "#2ECC71")
    static let aquaYellow = Color(hex: "#F39C12")
    static let aquaRed = Color(hex: "#E74C3C")
    static let lightGray = Color(hex: "#ECF0F1")
    static let mediumGray = Color(hex: "#95A5A6")
    
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
