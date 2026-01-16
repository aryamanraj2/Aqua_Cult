
import Foundation
import AVFoundation
import SwiftUI
import Combine

@MainActor
class TTSSummaryService: NSObject, ObservableObject {
    static let shared = TTSSummaryService()
    
    @Published var isSpeaking = false
    @Published var isPaused = false
    
    private var synthesizer: AVSpeechSynthesizer
    
    // Supported language (US English only)
    enum Language: String {
        case english = "en-US"
    }
    
    // Different speaking styles or “modes”
    enum SpeechMode {
        case normal
        case informative
    }
    
    private var currentLanguage: Language = .english
    private var currentMode: SpeechMode = .normal
    
    override init() {
        synthesizer = AVSpeechSynthesizer()
        super.init()
        synthesizer.delegate = self
    }
    
    /// Set the language for speech synthesis
    func setLanguage(_ language: Language) {
        currentLanguage = language
    }
    
    /// Set the desired speaking mode
    func setMode(_ mode: SpeechMode) {
        currentMode = mode
    }
    
    /// Speak the given text with style and fluency
    func speak(_ text: String, rate: Float = 0.47) {
        if synthesizer.isSpeaking {
            stop()
        }
        
        // Configure audio session
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .spokenAudio, options: [])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Audio session error: \(error)")
        }
        
        // Process text for better prosody
        let processedText = preprocessTextForSpeech(text)
        
        let utterance = AVSpeechUtterance(string: processedText)
        utterance.voice = selectVoice()
        
        // Apply mode-specific tuning
        switch currentMode {
        case .informative:
            utterance.rate = 0.45                   // slightly slower for clarity, adjusted for natural flow
            utterance.pitchMultiplier = 1.07        // adds authority and brightness
            utterance.volume = 1.0
            utterance.preUtteranceDelay = 0.07
            utterance.postUtteranceDelay = 0.12
        case .normal:
            utterance.rate = rate * 1.05            // slightly faster for more expressiveness
            utterance.pitchMultiplier = 1.03        // slightly higher for a more engaging tone
            utterance.volume = 1.0
            utterance.preUtteranceDelay = 0.05
            utterance.postUtteranceDelay = 0.1
        }
        
        synthesizer.speak(utterance)
        isSpeaking = true
        isPaused = false
    }
    
    /// Choose the best US English voice available, prioritizing natural-sounding options.
    private func selectVoice() -> AVSpeechSynthesisVoice {
        let preferredVoices = ["Vicki", "Daniel", "Samantha", "Allison", "Ava"] // Prioritize more natural-sounding voices

        // Attempt to find a preferred voice
        if let selectedVoice = AVSpeechSynthesisVoice.speechVoices().first(where: { voice in
            voice.language == "en-US" && preferredVoices.contains(where: { voice.name.contains($0) })
        }) {
            return selectedVoice
        }

        // Fallback to any available en-US voice if preferred voices are not found
        if let defaultUSVoice = AVSpeechSynthesisVoice.speechVoices().first(where: { $0.language == "en-US" }) {
            return defaultUSVoice
        }

        // Fallback to the system's default voice for the current language if no specific en-US voice is found
        if let languageVoice = AVSpeechSynthesisVoice(language: currentLanguage.rawValue) {
            return languageVoice
        }

        // Ultimate fallback: return the first available voice
        return AVSpeechSynthesisVoice.speechVoices().first ?? AVSpeechSynthesisVoice(language: "en-US")!
    }
    
    /// Refine text to create natural pauses and rhythm
    private func preprocessTextForSpeech(_ text: String) -> String {
        var t = text.trimmingCharacters(in: .whitespacesAndNewlines)
        
        // Ensure proper spacing after punctuation
        // Ensure proper spacing after punctuation: replace punctuation followed by zero or more spaces with punctuation and a single space
        t = t.replacingOccurrences(of: "([.,:;])\\s*", with: "$1 ", options: .regularExpression)

        // Add pauses after key analytical terms for clarity, ensuring word boundaries
        let keywords = ["temperature", "oxygen", "ph", "ammonia", "tank", "level", "fish", "sensor", "reading"]
        for word in keywords {
            // Use word boundaries to avoid matching parts of other words
            t = t.replacingOccurrences(of: "\\b\(word)\\b", with: "\(word),", options: .regularExpression)
        }
        
        // Add a leading phrase for informative tone (optional)
        if currentMode == .informative && !t.lowercased().hasPrefix("here") {
            t = "Here’s what I found. " + t
        }
        
        // Clean up multiple spaces into single spaces
        t = t.replacingOccurrences(of: "\\s+", with: " ", options: .regularExpression)
        
        return t
    }
    
    /// Pause ongoing speech
    func pause() {
        if synthesizer.isSpeaking && !isPaused {
            synthesizer.pauseSpeaking(at: .word)
            isPaused = true
        }
    }
    
    /// Resume paused speech
    func resume() {
        if isPaused {
            synthesizer.continueSpeaking()
            isPaused = false
        }
    }
    
    /// Stop all speech immediately
    func stop() {
        if synthesizer.isSpeaking {
            synthesizer.stopSpeaking(at: .immediate)
            isSpeaking = false
            isPaused = false
        }
    }
}

// MARK: - AVSpeechSynthesizerDelegate
extension TTSSummaryService: AVSpeechSynthesizerDelegate {
    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didStart utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = true
        }
    }
    
    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
            isPaused = false
        }
    }
    
    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didPause utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isPaused = true
        }
    }
    
    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didContinue utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isPaused = false
        }
    }
    
    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
            isPaused = false
        }
    }
}
