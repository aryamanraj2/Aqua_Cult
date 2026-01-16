//
//  OnboardingView.swift
//  aqua
//
//  Onboarding Experience for Aqua Sense
//

import SwiftUI

struct OnboardingView: View {
    @AppStorage("hasOnboarded") private var hasOnboarded: Bool = false
    @AppStorage("selectedLanguage") private var selectedLanguage: String = "English"
    @StateObject private var localizationManager = LocalizationManager.shared
    
    @State private var activePage: OnboardingPage = .welcome
    @State private var showContent: Bool = false
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Ocean gradient background
                LinearGradient(
                    gradient: Gradient(colors: [Color.deepOcean, Color.mediumBlue]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // Header View (Skip/Back buttons)
                    HeaderView(activePage: $activePage, hasOnboarded: $hasOnboarded)
                        .padding(.horizontal, 24)
                        .padding(.top, 20)
                        .padding(.bottom, 20)
                    
                    // Morphing Symbol View - Adaptive size based on page
                    MorphingSymbolView(
                        symbol: activePage.rawValue,
                        config: MorphingSymbolView.Config(
                            font: .system(size: activePage.isLanguagePage ? 100 : 140, weight: .bold),
                            frame: CGSize(
                                width: activePage.isLanguagePage ? 180 : 240,
                                height: activePage.isLanguagePage ? 180 : 240
                            ),
                            radius: 28,
                            foregroundColor: .white,
                            keyFrameDuration: 0.35
                        )
                    )
                    .padding(.vertical, activePage.isLanguagePage ? 20 : 30)
                    .animation(.smooth(duration: 0.7, extraBounce: 0.15), value: activePage)
                    
                    // Text Content View (Title and Subtitle)
                    Group {
                        if activePage.isLanguagePage {
                            LanguageSelectionView(selectedLanguage: $selectedLanguage)
                                .transition(.opacity.combined(with: .scale))
                        } else {
                            TextContentsView(activePage: activePage)
                                .transition(.opacity.combined(with: .scale))
                        }
                    }
                    .animation(.smooth(duration: 0.5, extraBounce: 0.1), value: activePage)
                    
                    Spacer(minLength: 20)
                    
                    // Page Indicators
                    IndicatorView(activePage: $activePage)
                        .padding(.bottom, 20)
                    
                    // Continue Button
                    ContinueButton(
                        activePage: $activePage,
                        hasOnboarded: $hasOnboarded
                    )
                    .padding(.horizontal, 32)
                    .padding(.bottom, geometry.safeAreaInsets.bottom + 30)
                }
                .opacity(showContent ? 1 : 0)
                .offset(y: showContent ? 0 : 30)
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.8).delay(0.2)) {
                showContent = true
            }
            // Set initial language in localization manager
            localizationManager.setLanguage(selectedLanguage)
        }
        .onChange(of: selectedLanguage) { _, newLanguage in
            // Update localization manager when language changes
            localizationManager.setLanguage(newLanguage)
        }
    }
}

// MARK: - Header View
struct HeaderView: View {
    @Binding var activePage: OnboardingPage
    @Binding var hasOnboarded: Bool
    @ObservedObject private var localizationManager = LocalizationManager.shared
    
    var body: some View {
        HStack {
            // Back Button
            Button(action: {
                activePage = activePage.previousPage
            }) {
                Image(systemName: "chevron.left")
                    .font(.title2.weight(.bold))
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .opacity(activePage.isFirstPage ? 0 : 1)
            .animation(.smooth(duration: 0.3), value: activePage)
            
            Spacer()
            
            // Skip Button
            Button(action: {
                hasOnboarded = true
            }) {
                Text(localizationManager.localizedString(for: "skip"))
                    .font(.body.weight(.semibold))
                    .foregroundColor(.white.opacity(0.9))
                    .contentShape(Rectangle())
            }
            .opacity(activePage.isLastPage ? 0 : 1)
            .animation(.smooth(duration: 0.3), value: activePage)
        }
    }
}

// MARK: - Text Contents View
struct TextContentsView: View {
    let activePage: OnboardingPage
    
    var body: some View {
        VStack(spacing: 16) {
            Text(activePage.title)
                .font(.system(size: 34, weight: .bold))
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
                .lineLimit(2)
                .minimumScaleFactor(0.8)
                .padding(.horizontal, 32)
            
            Text(activePage.subtitle)
                .font(.system(size: 18))
                .foregroundColor(.white.opacity(0.8))
                .multilineTextAlignment(.center)
                .lineLimit(4)
                .lineSpacing(4)
                .padding(.horizontal, 40)
                .fixedSize(horizontal: false, vertical: true)
        }
        .id(activePage.id)
        .transition(.asymmetric(
            insertion: .move(edge: .trailing).combined(with: .opacity),
            removal: .move(edge: .leading).combined(with: .opacity)
        ))
    }
}

// MARK: - Language Selection View
struct LanguageSelectionView: View {
    @Binding var selectedLanguage: String
    
    let languages = [
        "English",
        "हिन्दी",
        "বাংলা"
    ]
    
    var body: some View {
        VStack(spacing: 20) {
            VStack(spacing: 12) {
                Text("Choose Your Language")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                
                Text("Select your preferred language")
                    .font(.system(size: 16))
                    .foregroundColor(.white.opacity(0.8))
            }
            .padding(.horizontal, 32)
            .padding(.bottom, 8)
            
            VStack(spacing: 12) {
                ForEach(languages, id: \.self) { language in
                    LanguageButton(
                        language: language,
                        isSelected: selectedLanguage == language,
                        action: {
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                                selectedLanguage = language
                            }
                        }
                    )
                }
            }
            .padding(.horizontal, 40)
        }
    }
}

// MARK: - Language Button
struct LanguageButton: View {
    let language: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Text(language)
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(isSelected ? .deepOcean : .white)
                
                Spacer()
                
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 24))
                        .foregroundColor(.oceanBlue)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(isSelected ? Color.white : Color.white.opacity(0.12))
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(
                                isSelected ? Color.clear : Color.white.opacity(0.25),
                                lineWidth: 2
                            )
                    )
                    .shadow(
                        color: isSelected ? Color.white.opacity(0.3) : Color.clear,
                        radius: 12,
                        x: 0,
                        y: 6
                    )
            )
        }
        .buttonStyle(PlainButtonStyle())
        .scaleEffect(isSelected ? 1.02 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.7), value: isSelected)
    }
}

// MARK: - Page Indicators
struct IndicatorView: View {
    @Binding var activePage: OnboardingPage
    
    var body: some View {
        HStack(spacing: 10) {
            ForEach(OnboardingPage.allCases) { page in
                Capsule()
                    .fill(activePage == page ? Color.white : Color.white.opacity(0.35))
                    .frame(width: activePage == page ? 32 : 8, height: 8)
                    .shadow(
                        color: activePage == page ? Color.white.opacity(0.5) : Color.clear,
                        radius: 6,
                        x: 0,
                        y: 2
                    )
                    .animation(.smooth(duration: 0.4), value: activePage)
            }
        }
    }
}

// MARK: - Continue Button
struct ContinueButton: View {
    @Binding var activePage: OnboardingPage
    @Binding var hasOnboarded: Bool
    @ObservedObject private var localizationManager = LocalizationManager.shared
    
    var buttonText: String {
        activePage.isLastPage ? localizationManager.localizedString(for: "get_started") : localizationManager.localizedString(for: "continue")
    }
    
    var body: some View {
        Button(action: {
            withAnimation(.smooth(duration: 0.5, extraBounce: 0.15)) {
                if activePage.isLastPage {
                    hasOnboarded = true
                } else {
                    activePage = activePage.nextPage
                }
            }
        }) {
            HStack(spacing: 12) {
                Text(buttonText)
                    .font(.system(size: 19, weight: .bold))
                    .foregroundColor(.deepOcean)
                
                if activePage.isLastPage {
                    Image(systemName: "arrow.right.circle.fill")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.deepOcean)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 20)
            .background(
                Capsule()
                    .fill(Color.white)
                    .shadow(color: Color.white.opacity(0.4), radius: 15, x: 0, y: 8)
            )
        }
        .animation(.smooth(duration: 0.4, extraBounce: 0.1), value: activePage)
    }
}

#Preview {
    OnboardingView()
}
