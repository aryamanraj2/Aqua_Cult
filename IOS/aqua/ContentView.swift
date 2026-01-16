//
//  ContentView.swift
//  aqua
//
//  Created by aryaman jaiswal on 31/10/25.
//

import SwiftUI

struct ContentView: View {
    @StateObject private var tankManager = TankManager()
    @StateObject private var profileManager = UserProfileManager()
    @StateObject private var cartManager = CartManager()

    var body: some View {
        OneTimeOnBoarding(appStorageID: "AquaSense_Tutorial") {
            TabView {
                NavigationStack {
                    DashboardView()
                        .environmentObject(tankManager)
                        .environmentObject(profileManager)
                        .environmentObject(cartManager)
                }
                .tabItem {
                    Label("Dashboard", systemImage: "fish.fill")
                }
                
                NavigationStack {
                    DiseaseDetectionView()
                        .environmentObject(profileManager)
                }
                .tabItem {
                    Label("Disease", systemImage: "cross.case.fill")
                }
                
                NavigationStack {
                    MarketplaceView()
                        .environmentObject(profileManager)
                }
                .tabItem {
                    Label("Market", systemImage: "cart.fill")
                }
                
                NavigationStack {
                    ProfileView()
                        .environmentObject(tankManager)
                        .environmentObject(profileManager)
                }
                .tabItem {
                    Label("Profile", systemImage: "person.fill")
                }
            }
            .tint(.oceanBlue)
            .tabBarMinimizeBehavior(.onScrollDown)
        } beginOnboarding: {
            // Brief delay to allow UI to settle
            try? await Task.sleep(for: .seconds(0.3))
        } onBoardingFinished: {
            // Tutorial completed
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(CartManager())
}
