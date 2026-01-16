
import Foundation
import SwiftUI
import Combine

class TankManager: ObservableObject {
    @Published var tanks: [Tank] = Tank.sampleTanks
    
    var totalVolume: Double {
        tanks.reduce(0) { $0 + $1.dimensions.volume }
    }
    
    var tankCount: Int {
        tanks.count
    }
}