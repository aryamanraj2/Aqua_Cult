//
//  VoiceBotView.swift
//  aqua
//
//  Voice bot with liquid glass iOS 26 design
//

import SwiftUI

struct VoiceBotView: View {
    @StateObject private var voiceBotManager = VoiceBotManager()
    @StateObject private var languageManager = LanguageManager.shared
    @EnvironmentObject private var tankManager: TankManager
    @EnvironmentObject private var cartManager: CartManager
    @State private var backgroundOffset: CGFloat = 0
    @State private var showLanguagePackAlert = false
    
    var body: some View {
        NavigationStack {
            ZStack {
                // Liquid glass background
                LiquidGlassBackground(offset: backgroundOffset)
                    .ignoresSafeArea()

                VStack(spacing: 0) {
                    // Status bar (compact)
                    if voiceBotManager.isRecording || voiceBotManager.isProcessing || voiceBotManager.isSpeaking || voiceBotManager.isTranslating {
                        HStack(spacing: 12) {
                            // Animated indicator
                            ZStack {
                                Circle()
                                    .fill(.white.opacity(0.2))
                                    .frame(width: 32, height: 32)

                                if voiceBotManager.isRecording {
                                    Image(systemName: "waveform")
                                        .font(.system(size: 16, weight: .medium))
                                        .foregroundStyle(.white)
                                        .symbolEffect(.variableColor.iterative.reversing, options: .repeating)
                                } else if voiceBotManager.isTranslating {
                                    Image(systemName: "arrow.left.arrow.right")
                                        .font(.system(size: 16, weight: .medium))
                                        .foregroundStyle(.purple)
                                        .symbolEffect(.pulse, options: .repeating)
                                } else if voiceBotManager.isProcessing {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                        .scaleEffect(0.8)
                                } else if voiceBotManager.isSpeaking {
                                    Image(systemName: "speaker.wave.2.fill")
                                        .font(.system(size: 16, weight: .medium))
                                        .foregroundStyle(.cyan)
                                        .symbolEffect(.variableColor.iterative.reversing, options: .repeating)
                                }
                            }

                            // Status text
                            VStack(alignment: .leading, spacing: 2) {
                                if voiceBotManager.isRecording {
                                    Text("Listening...")
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundStyle(.white)

                                    if !voiceBotManager.currentTranscript.isEmpty {
                                        Text(voiceBotManager.currentTranscript)
                                            .font(.system(size: 12, weight: .regular))
                                            .foregroundStyle(.white.opacity(0.7))
                                            .lineLimit(1)
                                    }
                                } else if voiceBotManager.isTranslating {
                                    Text("Translating...")
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundStyle(.purple)
                                } else if voiceBotManager.isProcessing {
                                    Text("Thinking...")
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundStyle(.white)
                                } else if voiceBotManager.isSpeaking {
                                    Text("Speaking...")
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundStyle(.cyan)
                                }
                            }

                            Spacer()
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 12)
                        .background(.ultraThinMaterial)
                        .transition(.move(edge: .top).combined(with: .opacity))
                    }

                    // Chat conversation
                    if voiceBotManager.conversationHistory.isEmpty && !voiceBotManager.isRecording {
                        // Empty state
                        VStack(spacing: 20) {
                            Spacer()

                            Image(systemName: "waveform.circle.fill")
                                .font(.system(size: 80, weight: .thin))
                                .foregroundStyle(.white.opacity(0.7))
                                .symbolEffect(.pulse, options: .repeating)

                            VStack(spacing: 8) {
                                Text(languageManager.currentLanguage == .hindi ? "बोलने के लिए टैप करें" : "Tap to speak")
                                    .font(.system(size: 18, weight: .medium))
                                    .foregroundStyle(.white.opacity(0.6))

                                Text(languageManager.currentLanguage == .hindi 
                                     ? "पानी की गुणवत्ता, खिलाने के कार्यक्रम के बारे में पूछें,\nया उत्पाद सुझाव प्राप्त करें"
                                     : "Ask about water quality, feeding schedules,\nor get product recommendations")
                                    .font(.system(size: 14, weight: .regular))
                                    .foregroundStyle(.white.opacity(0.4))
                                    .multilineTextAlignment(.center)
                                    .padding(.horizontal, 40)
                            }

                            Spacer()
                        }
                        .transition(.opacity)
                    } else {
                        // Message list
                        ImprovedConversationView(
                            messages: voiceBotManager.conversationHistory,
                            isProcessing: voiceBotManager.isProcessing,
                            cartManager: cartManager,
                            voiceBotManager: voiceBotManager
                        )
                    }

                    // Error display
                    if let error = voiceBotManager.error {
                        HStack(spacing: 12) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundStyle(.red)
                            Text(error)
                                .font(.caption)
                                .foregroundStyle(.white)
                            Spacer()
                            Button("Dismiss") {
                                voiceBotManager.error = nil
                            }
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(.cyan)
                        }
                        .padding()
                        .background(.red.opacity(0.2))
                        .cornerRadius(12)
                        .padding(.horizontal)
                    }

                    // Voice control button
                    LiquidRecordButton(
                        isRecording: voiceBotManager.isRecording,
                        isProcessing: voiceBotManager.isProcessing,
                        action: { handleRecordingAction() }
                    )
                    .padding(.horizontal, 20)
                    .padding(.bottom, 24)
                }
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    HStack(spacing: 8) {
                        // Language toggle button
                        Button {
                            let newLanguage: AppLanguage = languageManager.currentLanguage == .english ? .hindi : .english

                            // Check if Hindi is available
                            if newLanguage == .hindi && !languageManager.speechRecognitionAvailable {
                                showLanguagePackAlert = true
                            } else {
                                withAnimation(.smooth(duration: 0.3)) {
                                    languageManager.switchLanguage(to: newLanguage)
                                }
                            }
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: "globe")
                                    .font(.system(size: 14, weight: .semibold))
                                Text(languageManager.currentLanguage == .english ? "EN" : "HI")
                                    .font(.system(size: 13, weight: .bold))
                            }
                            .foregroundStyle(.white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .background(
                                Capsule()
                                    .fill(.white.opacity(0.25))
                                    .overlay(
                                        Capsule()
                                            .stroke(.white.opacity(0.4), lineWidth: 1)
                                    )
                            )
                        }

                        // Clear conversation button
                        if !voiceBotManager.conversationHistory.isEmpty {
                            Button {
                                withAnimation(.smooth(duration: 0.4)) {
                                    voiceBotManager.clearConversation()
                                }
                            } label: {
                                Image(systemName: "arrow.counterclockwise")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundStyle(.white)
                                    .frame(width: 32, height: 32)
                                    .background(.ultraThinMaterial)
                                    .clipShape(Circle())
                            }
                        }
                    }
                }
            }
            .alert("Hindi Language Pack Required", isPresented: $showLanguagePackAlert) {
                Button("Go to Settings", role: .none) {
                    if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(settingsURL)
                    }
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text(languageManager.languagePackInstructions)
            }
        }
        .onAppear {
            voiceBotManager.checkPermissions()
            startBackgroundAnimation()
        }
    }
    
    private func startBackgroundAnimation() {
        withAnimation(.linear(duration: 20).repeatForever(autoreverses: false)) {
            backgroundOffset = 360
        }
    }
    
    private func handleRecordingAction() {
        if voiceBotManager.isRecording {
            let transcript = voiceBotManager.currentTranscript
            voiceBotManager.stopRecording()
            if !transcript.isEmpty {
                Task { await voiceBotManager.sendMessage(transcript, tanks: tankManager.tanks) }
            }
        } else if !voiceBotManager.isProcessing && !voiceBotManager.isSpeaking {
            voiceBotManager.startRecording()
        } else if voiceBotManager.isSpeaking {
            // Stop speaking if user taps while bot is speaking
            voiceBotManager.stopSpeaking()
        }
    }
}

// MARK: - Improved Conversation View
struct ImprovedConversationView: View {
    let messages: [VoiceMessage]
    let isProcessing: Bool
    let cartManager: CartManager
    @ObservedObject var voiceBotManager: VoiceBotManager
    
    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 20) {
                    ForEach(messages) { message in
                        ImprovedMessageBubble(
                            message: message,
                            cartManager: cartManager,
                            voiceBotManager: voiceBotManager
                        )
                        .id(message.id)
                    }
                    
                    if isProcessing {
                        TypingIndicator()
                            .id("typing")
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 20)
            }
            .scrollDismissesKeyboard(.interactively)
            .onChange(of: messages.count) { _, _ in
                if let lastMessage = messages.last {
                    withAnimation(.smooth(duration: 0.4)) {
                        proxy.scrollTo(lastMessage.id, anchor: .bottom)
                    }
                }
            }
            .onChange(of: isProcessing) { _, newValue in
                if newValue {
                    withAnimation(.smooth(duration: 0.4)) {
                        proxy.scrollTo("typing", anchor: .bottom)
                    }
                }
            }
        }
    }
}

// MARK: - Improved Message Bubble
struct ImprovedMessageBubble: View {
    let message: VoiceMessage
    let cartManager: CartManager
    @ObservedObject var voiceBotManager: VoiceBotManager
    @State private var isPressed = false
    
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            if message.role == .user {
                Spacer(minLength: 60)
            }
            
            VStack(alignment: message.role == .user ? .trailing : .leading, spacing: 12) {
                // Message bubble
                VStack(alignment: message.role == .user ? .trailing : .leading, spacing: 8) {
                    Text(message.content)
                        .font(.system(size: 16, weight: .regular))
                        .foregroundStyle(.white)
                        .multilineTextAlignment(message.role == .user ? .trailing : .leading)
                        .fixedSize(horizontal: false, vertical: true)
                    
                    if message.role == .assistant {
                        HStack(spacing: 4) {
                            Image(systemName: "speaker.wave.2.fill")
                                .font(.system(size: 10, weight: .semibold))
                            Text("Tap to listen")
                                .font(.system(size: 11, weight: .medium))
                        }
                        .foregroundStyle(.white.opacity(0.5))
                    }
                }
                .padding(.horizontal, 18)
                .padding(.vertical, 14)
                .background(
                    RoundedRectangle(cornerRadius: 22, style: .continuous)
                        .fill(
                            message.role == .user 
                            ? LinearGradient(
                                colors: [.white.opacity(0.25), .white.opacity(0.15)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                            : LinearGradient(
                                colors: [.white.opacity(0.15), .white.opacity(0.08)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 22, style: .continuous)
                                .stroke(
                                    LinearGradient(
                                        colors: [.white.opacity(0.3), .white.opacity(0.1)],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    ),
                                    lineWidth: 1
                                )
                        )
                        .shadow(color: .black.opacity(0.1), radius: 8, y: 4)
                )
                .scaleEffect(isPressed ? 0.98 : 1.0)
                .onTapGesture {
                    if message.role == .assistant {
                        // Haptic feedback
                        let generator = UIImpactFeedbackGenerator(style: .light)
                        generator.impactOccurred()
                        
                        // Speak the message
                        TTSSummaryService.shared.stop()
                       
                    }
                }
                .simultaneousGesture(
                    DragGesture(minimumDistance: 0)
                        .onChanged { _ in 
                            if message.role == .assistant {
                                isPressed = true
                            }
                        }
                        .onEnded { _ in 
                            isPressed = false
                        }
                )
                
                // Product carousel (if any)
                if let products = message.suggestedProducts, !products.isEmpty {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Suggested Products")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(.white.opacity(0.8))
                            .padding(.leading, 4)
                        
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(products) { product in
                                    ImprovedProductCard(
                                        product: product,
                                        cartManager: cartManager
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Timestamp
                Text(message.timestamp, style: .time)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundStyle(.white.opacity(0.4))
            }
            
            if message.role == .assistant {
                Spacer(minLength: 60)
            }
        }
        .transition(.scale.combined(with: .opacity))
    }
}

// MARK: - Improved Product Card
struct ImprovedProductCard: View {
    let product: MarketplaceProduct
    let cartManager: CartManager
    @State private var isAdded = false
    @State private var dragOffset: CGFloat = 0
    @State private var isDragging = false
    
    private let swipeThreshold: CGFloat = 80
    
    var body: some View {
        ZStack(alignment: .trailing) {
            // Background "Add to Cart" label revealed on swipe
            if dragOffset < -20 {
                HStack {
                    Spacer()
                    VStack(spacing: 4) {
                        Image(systemName: "cart.fill.badge.plus")
                            .font(.system(size: 20, weight: .semibold))
                        Text("Add")
                            .font(.system(size: 12, weight: .semibold))
                    }
                    .foregroundStyle(.white)
                    .opacity(min(1.0, Double(-dragOffset) / Double(swipeThreshold)))
                    .padding(.trailing, 20)
                }
                .frame(width: 170, height: 220)
            }
            
            // Product card
            VStack(alignment: .leading, spacing: 10) {
                // Product icon
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [.white.opacity(0.2), .white.opacity(0.05)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 50, height: 50)
                    
                    Image(systemName: product.imageName)
                        .font(.system(size: 22, weight: .medium))
                        .foregroundStyle(.white)
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(product.name)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(.white)
                        .lineLimit(2)
                        .frame(height: 36, alignment: .top)
                    
                    Text(product.formattedPrice)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(.cyan)
                }
                
                Spacer()
                
                // Status indicator
                if isAdded {
                    HStack {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 18, weight: .semibold))
                        
                        Text("Added")
                            .font(.system(size: 13, weight: .semibold))
                    }
                    .foregroundStyle(.green)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(
                        Capsule()
                            .fill(.green.opacity(0.2))
                            .overlay(
                                Capsule()
                                    .stroke(.green.opacity(0.5), lineWidth: 1)
                            )
                    )
                    .transition(.scale.combined(with: .opacity))
                } else {
                    HStack(spacing: 4) {
                        Image(systemName: "arrow.left")
                            .font(.system(size: 11, weight: .semibold))
                        Text("Swipe to add")
                            .font(.system(size: 11, weight: .medium))
                    }
                    .foregroundStyle(.white.opacity(0.5))
                }
            }
            .padding(14)
            .frame(width: 170, height: 220)
            .background(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: isAdded 
                                ? [.green.opacity(0.2), .green.opacity(0.1)]
                                : [.white.opacity(0.12), .white.opacity(0.05)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 20, style: .continuous)
                            .stroke(
                                LinearGradient(
                                    colors: isAdded
                                        ? [.green.opacity(0.4), .green.opacity(0.2)]
                                        : [.white.opacity(0.25), .white.opacity(0.1)],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 1
                            )
                    )
                    .shadow(color: .black.opacity(0.15), radius: 10, y: 5)
            )
            .offset(x: dragOffset)
            .gesture(
                DragGesture()
                    .onChanged { value in
                        // Only allow left swipe
                        if value.translation.width < 0 {
                            isDragging = true
                            dragOffset = value.translation.width
                        }
                    }
                    .onEnded { value in
                        isDragging = false
                        
                        // Check if swipe threshold was met
                        if value.translation.width < -swipeThreshold {
                            // Add to cart
                            addToCart()
                            
                            // Reset immediately
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                                dragOffset = 0
                            }
                        } else {
                            // Snap back
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                                dragOffset = 0
                            }
                        }
                    }
            )
        }
    }
    
    private func addToCart() {
        cartManager.addToCart(product: product)
        withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
            isAdded = true
        }
        
        // Haptic feedback
        let generator = UIImpactFeedbackGenerator(style: .medium)
        generator.impactOccurred()
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            withAnimation(.smooth(duration: 0.3)) {
                isAdded = false
            }
        }
    }
}

// MARK: - Typing Indicator
struct TypingIndicator: View {
    @State private var animate = false
    
    var body: some View {
        HStack(spacing: 0) {
            HStack(spacing: 8) {
                ForEach(0..<3) { index in
                    Circle()
                        .fill(.white.opacity(0.8))
                        .frame(width: 8, height: 8)
                        .offset(y: animate ? -8 : 0)
                        .animation(
                            .easeInOut(duration: 0.6)
                            .repeatForever()
                            .delay(Double(index) * 0.2),
                            value: animate
                        )
                }
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 14)
            .background(
                RoundedRectangle(cornerRadius: 22, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: [.white.opacity(0.15), .white.opacity(0.08)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 22, style: .continuous)
                            .stroke(
                                LinearGradient(
                                    colors: [.white.opacity(0.3), .white.opacity(0.1)],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 1
                            )
                    )
            )
            
            Spacer(minLength: 60)
        }
        .onAppear { animate = true }
        .transition(.scale.combined(with: .opacity))
    }
}

// MARK: - Liquid Glass Background
struct LiquidGlassBackground: View {
    let offset: CGFloat
    
    var body: some View {
        ZStack {
            // Base gradient
            LinearGradient(
                colors: [
                    .oceanBlue,
                    .mediumBlue,
                    .oceanBlue
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            
            // Animated liquid blobs
            ForEach(0..<3) { index in
                Circle()
                    .fill(
                        RadialGradient(
                            colors: [
                                .white.opacity(0.10),
                                .white.opacity(0.03),
                                .clear
                            ],
                            center: .center,
                            startRadius: 0,
                            endRadius: 150
                        )
                    )
                    .frame(width: 300, height: 300)
                    .blur(radius: 60)
                    .offset(
                        x: cos(offset * 0.01 + Double(index) * 2) * 100,
                        y: sin(offset * 0.015 + Double(index) * 1.5) * 120
                    )
            }
        }
    }
}




// MARK: - Liquid Record Button
struct LiquidRecordButton: View {
    let isRecording: Bool
    let isProcessing: Bool
    let action: () -> Void
    
    @State private var pulseScale: CGFloat = 1.0
    @State private var glowIntensity: Double = 0.3
    
    var body: some View {
        Button(action: action) {
            ZStack {
                // Glow effect
                if isRecording {
                    Circle()
                        .fill(
                            RadialGradient(
                                colors: [
                                    .white.opacity(glowIntensity),
                                    .clear
                                ],
                                center: .center,
                                startRadius: 0,
                                endRadius: 60
                            )
                        )
                        .frame(width: 100, height: 100)
                        .blur(radius: 20)
                        .scaleEffect(pulseScale)
                }
                
                // Main button
                Circle()
                    .fill(.ultraThinMaterial)
                    .overlay(
                        Circle()
                            .stroke(.white.opacity(0.4), lineWidth: 2)
                    )
                    .frame(width: 70, height: 70)
                    .shadow(color: .white.opacity(0.3), radius: isRecording ? 20 : 10)
                
                // Icon
                Group {
                    if isProcessing {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .scaleEffect(1.2)
                    } else if isRecording {
                        RoundedRectangle(cornerRadius: 6, style: .continuous)
                            .fill(.white)
                            .frame(width: 20, height: 20)
                    } else {
                        Image(systemName: "waveform")
                            .font(.system(size: 24, weight: .medium))
                            .foregroundStyle(.white)
                    }
                }
            }
        }
        .disabled(isProcessing)
        .sensoryFeedback(.impact(weight: .light), trigger: isRecording)
        .onChange(of: isRecording) { _, newValue in
            if newValue {
                withAnimation(.easeInOut(duration: 1.5).repeatForever(autoreverses: true)) {
                    pulseScale = 1.3
                    glowIntensity = 0.6
                }
            } else {
                withAnimation(.smooth(duration: 0.3)) {
                    pulseScale = 1.0
                    glowIntensity = 0.3
                }
            }
        }
    }
}

#Preview {
    VoiceBotView()
        .environmentObject(TankManager())
        .environmentObject(CartManager())
}
