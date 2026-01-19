
import Foundation
import Translation
import Combine

enum TranslationError: LocalizedError {
    case languageNotSupported
    case translationFailed(String)
    case languagePackNotAvailable

    var errorDescription: String? {
        switch self {
        case .languageNotSupported:
            return "Hindi-English translation is not supported on this device"
        case .translationFailed(let message):
            return "Translation failed: \(message)"
        case .languagePackNotAvailable:
            return "Hindi language pack is not downloaded. Please download it from Settings > General > Keyboard > Keyboards"
        }
    }
}

@MainActor
class TranslationService: ObservableObject {
    static let shared = TranslationService()

    @Published var isTranslating = false
    @Published var hindiLanguageAvailable = false

    private init() {
        Task {
            await checkLanguageAvailability()
        }
    }

    /// Check if Hindi-English translation is available
    func checkLanguageAvailability() async {
        if #available(iOS 17.4, *) {
            let availability = LanguageAvailability()
            let hindiSupported = await availability.status(
                from: Locale.Language(identifier: "hi"),
                to: Locale.Language(identifier: "en")
            )

            switch hindiSupported {
            case .installed:
                hindiLanguageAvailable = true
                print("✅ Hindi translation language pack is installed")
            case .supported:
                hindiLanguageAvailable = false
                print("⚠️ Hindi translation is supported but language pack not downloaded")
            case .unsupported:
                hindiLanguageAvailable = false
                print("❌ Hindi translation is not supported on this device")
            @unknown default:
                hindiLanguageAvailable = false
                print("❓ Unknown language availability status")
            }
        } else {
            // For older iOS versions, assume not available
            hindiLanguageAvailable = false
            print("⚠️ Translation framework requires iOS 17.4+")
        }
    }

    /// Translate Hindi text to English
    func translateHindiToEnglish(_ text: String) async throws -> String {
        print("🔄 [TranslationService] Translating Hindi to English: \(text.prefix(50))...")

        guard !text.isEmpty else {
            return text
        }

        guard #available(iOS 17.4, *) else {
            throw TranslationError.languageNotSupported
        }

        // Check language availability
        await checkLanguageAvailability()
        guard hindiLanguageAvailable else {
            throw TranslationError.languagePackNotAvailable
        }

        isTranslating = true
        defer { isTranslating = false }

        do {
            let sourceLanguage = Locale.Language(identifier: "hi")
            let targetLanguage = Locale.Language(identifier: "en")

            let session = TranslationSession(installedSource: sourceLanguage, target: targetLanguage)
            let response = try await session.translate(text)

            let translatedText = response.targetText

            print("✅ [TranslationService] Translated to English: \(translatedText.prefix(50))...")
            return translatedText

        } catch {
            print("❌ [TranslationService] Translation error: \(error)")
            throw TranslationError.translationFailed(error.localizedDescription)
        }
    }

    /// Translate English text to Hindi while preserving product names
    func translateToHindiPreservingProducts(_ text: String, products: [String]) async throws -> String {
        print("🔄 [TranslationService] Translating to Hindi, preserving products: \(products)")

        guard !text.isEmpty else {
            return text
        }

        guard #available(iOS 17.4, *) else {
            throw TranslationError.languageNotSupported
        }

        // Check language availability
        await checkLanguageAvailability()
        guard hindiLanguageAvailable else {
            throw TranslationError.languagePackNotAvailable
        }

        isTranslating = true
        defer { isTranslating = false }

        // Step 1: Replace product names with placeholders
        var modifiedText = text
        var productMapping: [String: String] = [:]

        for (index, product) in products.enumerated() {
            let placeholder = "PRODUCT_PLACEHOLDER_\(index)"
            productMapping[placeholder] = product

            // Replace product name with placeholder (case-insensitive)
            let pattern = NSRegularExpression.escapedPattern(for: product)
            if let regex = try? NSRegularExpression(pattern: pattern, options: .caseInsensitive) {
                let range = NSRange(modifiedText.startIndex..., in: modifiedText)
                modifiedText = regex.stringByReplacingMatches(
                    in: modifiedText,
                    range: range,
                    withTemplate: placeholder
                )
            }
        }

        print("🔄 [TranslationService] Text with placeholders: \(modifiedText.prefix(100))...")

        // Step 2: Translate text with placeholders
        do {
            let sourceLanguage = Locale.Language(identifier: "en")
            let targetLanguage = Locale.Language(identifier: "hi")

            let session = TranslationSession(installedSource: sourceLanguage, target: targetLanguage)
            let response = try await session.translate(modifiedText)

            var translatedText = response.targetText

            // Step 3: Replace placeholders back with original English product names
            for (placeholder, product) in productMapping {
                translatedText = translatedText.replacingOccurrences(of: placeholder, with: product)
            }

            print("✅ [TranslationService] Translated to Hindi: \(translatedText.prefix(100))...")
            return translatedText

        } catch {
            print("❌ [TranslationService] Translation error: \(error)")
            throw TranslationError.translationFailed(error.localizedDescription)
        }
    }

    /// Simple English to Hindi translation (no product preservation)
    func translateEnglishToHindi(_ text: String) async throws -> String {
        return try await translateToHindiPreservingProducts(text, products: [])
    }
}
