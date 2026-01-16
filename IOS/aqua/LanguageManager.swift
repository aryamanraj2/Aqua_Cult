//
//  LanguageManager.swift
//  aqua
//
//  Language preference management for voice bot
//

import Foundation
import Speech
import Combine

enum AppLanguage: String, CaseIterable {
    case english = "en-US"
    case hindi = "hi-IN"

    var displayName: String {
        switch self {
        case .english: return "English"
        case .hindi: return "हिन्दी"
        }
    }

    var localeIdentifier: String {
        return self.rawValue
    }

    var ttsLanguageCode: String {
        switch self {
        case .english: return "en-US"
        case .hindi: return "hi-IN"
        }
    }
}

@MainActor
class LanguageManager: ObservableObject {
    static let shared = LanguageManager()

    @Published var currentLanguage: AppLanguage {
        didSet {
            saveLanguagePreference()
            checkSpeechRecognitionSupport()
        }
    }

    @Published var speechRecognitionAvailable = false
    @Published var showLanguagePackAlert = false

    private let userDefaultsKey = "app_selected_language"

    private init() {
        // Load saved language preference
        if let savedLanguage = UserDefaults.standard.string(forKey: userDefaultsKey),
           let language = AppLanguage(rawValue: savedLanguage) {
            self.currentLanguage = language
        } else {
            self.currentLanguage = .english
        }

        checkSpeechRecognitionSupport()
    }

    /// Save language preference to UserDefaults
    private func saveLanguagePreference() {
        UserDefaults.standard.set(currentLanguage.rawValue, forKey: userDefaultsKey)
        print("💾 [LanguageManager] Saved language preference: \(currentLanguage.displayName)")
    }

    /// Check if speech recognition is available for the current language
    func checkSpeechRecognitionSupport() {
        let supportedLocales = SFSpeechRecognizer.supportedLocales()
        let locale = Locale(identifier: currentLanguage.localeIdentifier)

        speechRecognitionAvailable = supportedLocales.contains(locale)

        if speechRecognitionAvailable {
            print("✅ [LanguageManager] Speech recognition supported for \(currentLanguage.displayName)")
        } else {
            print("⚠️ [LanguageManager] Speech recognition NOT supported for \(currentLanguage.displayName)")
            showLanguagePackAlert = true
        }
    }

    /// Check if Hindi language pack is available for translation
    func checkHindiTranslationAvailability() async -> Bool {
        return await TranslationService.shared.hindiLanguageAvailable
    }

    /// Switch to a different language
    func switchLanguage(to language: AppLanguage) {
        print("🔄 [LanguageManager] Switching language from \(currentLanguage.displayName) to \(language.displayName)")
        currentLanguage = language
    }

    /// Get language pack installation instructions
    var languagePackInstructions: String {
        return """
        To use Hindi voice recognition:

        1. Open Settings app
        2. Go to General → Keyboard
        3. Tap "Keyboards" → "Add New Keyboard"
        4. Select "Hindi" from the list
        5. Enable "Dictation" for Hindi

        After installation, restart the app.
        """
    }
}
