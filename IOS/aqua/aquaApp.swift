import SwiftUI

@main
struct aquaApp: App {
    @StateObject private var cartManager = CartManager()
    
    var body: some Scene {
        WindowGroup {
            SplashScreenView()
                .environmentObject(cartManager)
        }
    }
}
