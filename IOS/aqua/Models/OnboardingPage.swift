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
    // Define cases for each onboarding screen (simplified to welcome + language only)
    case welcome = "water.waves"
    case language = "globe"
    
    var id: String { rawValue }
    
    // Computed property for the title of each page
    var title: String {
        let localizationManager = LocalizationManager.shared
        switch self {
        case .welcome: return localizationManager.localizedString(for: "welcome_title")
        case .language: return localizationManager.localizedString(for: "choose_language")
        }
    }
    
    // Computed property for the subtitle of each page
    var subtitle: String {
        let localizationManager = LocalizationManager.shared
        switch self {
        case .welcome: return localizationManager.localizedString(for: "welcome_subtitle")
        case .language: return localizationManager.localizedString(for: "select_preferred_language")
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
