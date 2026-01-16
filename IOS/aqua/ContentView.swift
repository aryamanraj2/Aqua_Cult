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
    @ObservedObject private var localizationManager = LocalizationManager.shared

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
                    Label(localizationManager.localizedString(for: "tab_dashboard"), systemImage: "fish.fill")
                }

                NavigationStack {
                    DiseaseDetectionView()
                        .environmentObject(profileManager)
                }
                .tabItem {
                    Label(localizationManager.localizedString(for: "tab_disease"), systemImage: "cross.case.fill")
                }

                NavigationStack {
                    MarketplaceView()
                        .environmentObject(profileManager)
                }
                .tabItem {
                    Label(localizationManager.localizedString(for: "tab_market"), systemImage: "cart.fill")
                }

                NavigationStack {
                    ProfileView()
                        .environmentObject(tankManager)
                        .environmentObject(profileManager)
                }
                .tabItem {
                    Label(localizationManager.localizedString(for: "tab_profile"), systemImage: "person.fill")
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
