//
//  aquaApp.swift
//  aqua
//
//  Created by aryaman jaiswal on 31/10/25.
//

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
