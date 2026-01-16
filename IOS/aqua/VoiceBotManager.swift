//
//  VoiceBotManager.swift
//  aqua
//
//  Voice bot state management and coordination
//

import Foundation
import AVFoundation
import Speech
import SwiftUI
import Combine

@MainActor
class VoiceBotManager: NSObject, ObservableObject {
    @Published var isRecording = false
    @Published var isProcessing = false
    @Published var conversationHistory: [VoiceMessage] = []
    @Published var currentTranscript = ""
    @Published var error: String?
    @Published var recordingPermissionGranted = false
    @Published var isSpeaking = false
    @Published var isTranslating = false

    // TTS Service
    private let ttsService = TTSSummaryService.shared

    // Translation Service
    private let translationService = TranslationService.shared

    // Language Manager
    private let languageManager = LanguageManager.shared

    private var audioEngine: AVAudioEngine?
    private var speechRecognizers: [String: SFSpeechRecognizer] = [:]
    private var currentSpeechRecognizer: SFSpeechRecognizer?
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?

    // Conversation context
    private var conversationContext: [String] = []

    override init() {
        super.init()
        setupSpeechRecognizers()
        checkPermissions()
    }

    private func setupSpeechRecognizers() {
        // Initialize recognizers for both languages
        speechRecognizers["en-US"] = SFSpeechRecognizer(locale: Locale(identifier: "en-US"))
        speechRecognizers["hi-IN"] = SFSpeechRecognizer(locale: Locale(identifier: "hi-IN"))

        // Set current recognizer based on language manager
        updateCurrentRecognizer()
    }

    private func updateCurrentRecognizer() {
        currentSpeechRecognizer = speechRecognizers[languageManager.currentLanguage.localeIdentifier]
        print("🎤 [VoiceBotManager] Using speech recognizer for: \(languageManager.currentLanguage.displayName)")
    }
    
    func checkPermissions() {
        SFSpeechRecognizer.requestAuthorization { [weak self] authStatus in
            Task { @MainActor in
                self?.recordingPermissionGranted = (authStatus == .authorized)
            }
        }
        
        // Use the new iOS 17.0+ API if available, otherwise fall back to the deprecated method
        if #available(iOS 17.0, *) {
            AVAudioApplication.requestRecordPermission { [weak self] allowed in
                Task { @MainActor in
                    if !allowed {
                        self?.recordingPermissionGranted = false
                    }
                }
            }
        } else {
            AVAudioSession.sharedInstance().requestRecordPermission { [weak self] allowed in
                Task { @MainActor in
                    if !allowed {
                        self?.recordingPermissionGranted = false
                    }
                }
            }
        }
    }
    
    func startRecording() {
        // Update recognizer based on current language
        updateCurrentRecognizer()

        // Configure the audio session first, before accessing any IO nodes
        do {
            try AVAudioSession.sharedInstance().setCategory(.record, mode: .measurement, options: .duckOthers)
            try AVAudioSession.sharedInstance().setActive(true, options: .notifyOthersOnDeactivation)
        } catch {
            self.error = "Failed to configure audio session: \(error.localizedDescription)"
            return
        }

        guard recordingPermissionGranted else {
            error = "Microphone permission not granted"
            return
        }

        // Check if speech recognizer is available for selected language
        guard let recognizer = currentSpeechRecognizer else {
            error = "Speech recognition not available for \(languageManager.currentLanguage.displayName)"
            return
        }

        // Reset
        currentTranscript = ""
        error = nil

        audioEngine = AVAudioEngine()
        guard let audioEngine = audioEngine,
              let inputNode = audioEngine.inputNode as AVAudioInputNode? else {
            error = "Audio engine initialization failed"
            return
        }

        recognitionRequest = SFSpeechAudioBufferRecognitionRequest()
        guard let recognitionRequest = recognitionRequest else {
            error = "Unable to create recognition request"
            return
        }

        recognitionRequest.shouldReportPartialResults = true

        recognitionTask = recognizer.recognitionTask(with: recognitionRequest) { [weak self] result, error in
            Task { @MainActor in
                if let result = result {
                    self?.currentTranscript = result.bestTranscription.formattedString
                }

                if error != nil || result?.isFinal == true {
                    self?.stopRecording()
                }
            }
        }
        
        // Use the input node's native format - this is the most reliable approach
        let recordingFormat = inputNode.outputFormat(forBus: 0)
        
        // Validate that the format has reasonable parameters
        guard recordingFormat.sampleRate > 0 && recordingFormat.channelCount > 0 else {
            print("❌ Invalid audio format: \(recordingFormat)")
            error = "Invalid audio format from input node"
            return
        }
        
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { buffer, _ in
            recognitionRequest.append(buffer)
        }
        
        do {
            audioEngine.prepare()
            try audioEngine.start()
            
            isRecording = true
        } catch {
            self.error = "Failed to start recording: \(error.localizedDescription)"
        }
    }
    
    func stopRecording() {
        audioEngine?.stop()
        audioEngine?.inputNode.removeTap(onBus: 0)
        recognitionRequest?.endAudio()
        recognitionTask?.cancel()
        
        audioEngine = nil
        recognitionRequest = nil
        recognitionTask = nil
        
        isRecording = false
        
        // Process the transcript
        if !currentTranscript.isEmpty {
            processUserInput(currentTranscript)
        }
    }
    
    private func processUserInput(_ text: String) {
        Task {
            var processedText = text
            var displayText = text

            // If user is speaking Hindi, translate to English for Gemini
            if languageManager.currentLanguage == .hindi {
                isTranslating = true
                do {
                    print("🔄 [VoiceBotManager] Translating Hindi input to English...")
                    processedText = try await translationService.translateHindiToEnglish(text)
                    displayText = text // Keep original Hindi for display
                    print("✅ [VoiceBotManager] Translation complete: \(processedText.prefix(50))...")
                } catch {
                    print("❌ [VoiceBotManager] Translation failed: \(error)")
                    self.error = "Translation failed: \(error.localizedDescription)"
                    isTranslating = false
                    return
                }
                isTranslating = false
            }

            // Add user message to history (show original language)
            let userMessage = VoiceMessage(
                id: UUID().uuidString,
                role: .user,
                content: displayText,
                timestamp: Date()
            )
            conversationHistory.append(userMessage)

            // Add English version to context (for Gemini)
            conversationContext.append(processedText)

            // Reset transcript
            currentTranscript = ""
        }
    }
    
    func sendMessage(_ message: String, tanks: [Tank]) async {
        print("🎤 [VoiceBotManager] Sending message: \(message)")
        print("🎤 [VoiceBotManager] Tanks count: \(tanks.count)")
        isProcessing = true
        error = nil

        do {
            print("🎤 [VoiceBotManager] Calling Gemini API...")
            let response = try await GeminiVoiceService.shared.processVoiceQuery(
                query: message,
                conversationContext: conversationContext,
                tanks: tanks
            )
            print("🎤 [VoiceBotManager] Received response: \(response.textResponse.prefix(50))...")

            var displayText = response.textResponse
            var spokenText = response.textResponse

            // If user's language is Hindi, translate response to Hindi (preserving product names)
            if languageManager.currentLanguage == .hindi {
                isTranslating = true
                do {
                    print("🔄 [VoiceBotManager] Translating response to Hindi, preserving product names...")

                    // Extract product names from recommended products
                    let productNames = response.recommendedProducts.map { $0.name }

                    // Translate to Hindi while preserving English product names
                    spokenText = try await translationService.translateToHindiPreservingProducts(
                        response.textResponse,
                        products: productNames
                    )
                    displayText = spokenText
                    print("✅ [VoiceBotManager] Translation complete: \(spokenText.prefix(50))...")

                } catch {
                    print("⚠️ [VoiceBotManager] Translation failed, using English: \(error)")
                    // Fall back to English if translation fails
                    displayText = response.textResponse
                    spokenText = response.textResponse
                }
                isTranslating = false
            }

            // Add assistant message (in user's language)
            let assistantMessage = VoiceMessage(
                id: UUID().uuidString,
                role: .assistant,
                content: displayText,
                timestamp: Date(),
                suggestedProducts: response.recommendedProducts
            )
            conversationHistory.append(assistantMessage)

            // Update context with English response (for consistency with Gemini)
            conversationContext.append(response.textResponse)

            // Speak the response
            isProcessing = false

            // Set TTS language based on current language
            ttsService.setLanguage(languageManager.currentLanguage == .hindi ? .hindi : .english)
            ttsService.setMode(.informative)
            ttsService.speak(spokenText)
            isSpeaking = true

            // Monitor TTS state
            observeTTSState()

        } catch {
            print("❌ [VoiceBotManager] Error: \(error)")
            print("❌ [VoiceBotManager] Error description: \(error.localizedDescription)")
            self.error = error.localizedDescription
            isProcessing = false
        }
    }
    
    func clearConversation() {
        conversationHistory.removeAll()
        conversationContext.removeAll()
        currentTranscript = ""
        error = nil
        stopSpeaking()
    }
    
    func stopSpeaking() {
        ttsService.stop()
        isSpeaking = false
    }
    
    private func observeTTSState() {
        // Poll TTS state and update isSpeaking
        Task {
            while ttsService.isSpeaking {
                try? await Task.sleep(nanoseconds: 100_000_000) // 0.1 seconds
            }
            await MainActor.run {
                isSpeaking = false
            }
        }
    }
}

// MARK: - Models
struct VoiceMessage: Identifiable {
    let id: String
    let role: MessageRole
    let content: String
    let timestamp: Date
    var suggestedProducts: [MarketplaceProduct]?
    
    enum MessageRole {
        case user
        case assistant
    }
}

struct VoiceQueryResponse {
    let textResponse: String
    let recommendedProducts: [MarketplaceProduct]
}
