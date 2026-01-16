//
//  OnboardingPage.swift
//  aqua
//
//  Created for Aqua Sense Onboarding
//

import Foundation
import SwiftUI
import Combine

enum OnboardingPage: String, CaseIterable, Identifiable {
    // Define cases for each onboarding screen
    case welcome = "water.waves"
    case language = "globe"
    case monitoring = "fish.fill"
    case disease = "cross.case.fill"
    case marketplace = "cart.fill"
    case voiceSupport = "waveform"
    
    var id: String { rawValue }
    
    // Computed property for the title of each page
    var title: String {
        let localizationManager = LocalizationManager.shared
        switch self {
        case .welcome: return localizationManager.localizedString(for: "welcome_title")
        case .language: return localizationManager.localizedString(for: "choose_language")
        case .monitoring: return localizationManager.localizedString(for: "monitoring_title")
        case .disease: return localizationManager.localizedString(for: "disease_title")
        case .marketplace: return localizationManager.localizedString(for: "marketplace_title")
        case .voiceSupport: return localizationManager.localizedString(for: "voice_support_title")
        }
    }
    
    // Computed property for the subtitle of each page
    var subtitle: String {
        let localizationManager = LocalizationManager.shared
        switch self {
        case .welcome: return localizationManager.localizedString(for: "welcome_subtitle")
        case .language: return localizationManager.localizedString(for: "select_preferred_language")
        case .monitoring: return localizationManager.localizedString(for: "monitoring_subtitle")
        case .disease: return localizationManager.localizedString(for: "disease_subtitle")
        case .marketplace: return localizationManager.localizedString(for: "marketplace_subtitle")
        case .voiceSupport: return localizationManager.localizedString(for: "voice_support_subtitle")
        }
    }
    
    // Computed property for the index of each page
    var index: Int {
        Self.allCases.firstIndex(of: self) ?? 0
    }
    
    // Computed property to get the next page
    var nextPage: OnboardingPage {
        if index < Self.allCases.count - 1 {
            return Self.allCases[index + 1]
        }
        return self
    }
    
    // Computed property to get the previous page
    var previousPage: OnboardingPage {
        if index > 0 {
            return Self.allCases[index - 1]
        }
        return self
    }
    
    // Check if it's the last page
    var isLastPage: Bool {
        index == Self.allCases.count - 1
    }
    
    // Check if it's the first page
    var isFirstPage: Bool {
        index == 0
    }
    
    // Check if it's the language page
    var isLanguagePage: Bool {
        self == .language
    }
}
