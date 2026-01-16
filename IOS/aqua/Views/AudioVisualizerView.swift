//
//  AudioVisualizerView.swift
//  aqua
//
//  Apple-inspired audio visualizer with animated frequency bars
//

import SwiftUI
import AVFoundation

struct AudioVisualizerView: View {
    let isActive: Bool
    let isListening: Bool
    let isProcessing: Bool
    let isSpeaking: Bool
    
    @State private var barHeights: [CGFloat] = Array(repeating: 0.1, count: 40)
    @State private var glowIntensity: Double = 0.3
    @State private var rotationAngle: Double = 0
    
    private let barCount = 40
    private let minHeight: CGFloat = 0.1
    private let maxHeight: CGFloat = 1.0
    
    var body: some View {
        ZStack {
            // Outer glow ring
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            visualizerColor.opacity(glowIntensity),
                            visualizerColor.opacity(glowIntensity * 0.5),
                            .clear
                        ],
                        center: .center,
                        startRadius: 0,
                        endRadius: 200
                    )
                )
                .frame(width: 320, height: 320)
                .blur(radius: 40)
            
            // Frequency bars in circular arrangement
            ZStack {
                ForEach(0..<barCount, id: \.self) { index in
                    RoundedRectangle(cornerRadius: 4)
                        .fill(
                            LinearGradient(
                                colors: [
                                    visualizerColor.opacity(0.9),
                                    visualizerColor.opacity(0.6)
                                ],
                                startPoint: .bottom,
                                endPoint: .top
                            )
                        )
                        .frame(width: 4, height: barHeights[index] * 80 + 8)
                        .offset(y: -80)
                        .rotationEffect(.degrees(Double(index) * 9))
                }
            }
            .rotationEffect(.degrees(rotationAngle))
            
            // Center orb
            ZStack {
                Circle()
                    .fill(.ultraThinMaterial)
                    .frame(width: 100, height: 100)
                    .overlay(
                        Circle()
                            .stroke(visualizerColor.opacity(0.5), lineWidth: 2)
                    )
                
                // Icon based on state
                Group {
                    if isProcessing {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .scaleEffect(1.5)
                    } else if isSpeaking {
                        Image(systemName: "speaker.wave.3.fill")
                            .font(.system(size: 32, weight: .medium))
                            .foregroundStyle(visualizerColor)
                            .symbolEffect(.variableColor.iterative.reversing, options: .repeating)
                    } else if isListening {
                        Image(systemName: "waveform")
                            .font(.system(size: 32, weight: .medium))
                            .foregroundStyle(visualizerColor)
                            .symbolEffect(.variableColor.iterative.reversing, options: .repeating)
                    } else {
                        Image(systemName: "waveform.circle")
                            .font(.system(size: 32, weight: .thin))
                            .foregroundStyle(.white.opacity(0.7))
                    }
                }
            }
        }
        .onChange(of: isActive) { _, newValue in
            if newValue {
                startAnimation()
            } else {
                stopAnimation()
            }
        }
        .onChange(of: isListening) { _, _ in
            updateAnimationIntensity()
        }
        .onChange(of: isSpeaking) { _, _ in
            updateAnimationIntensity()
        }
        .onAppear {
            if isActive {
                startAnimation()
            }
        }
    }
    
    private var visualizerColor: Color {
        if isSpeaking {
            return .cyan
        } else if isListening {
            return .white
        } else if isProcessing {
            return .purple.opacity(0.8)
        } else {
            return .white.opacity(0.5)
        }
    }
    
    private func startAnimation() {
        // Animate bars
        withAnimation(.linear(duration: 0.8).repeatForever(autoreverses: false)) {
            updateBarHeights()
        }
        
        // Animate glow
        withAnimation(.easeInOut(duration: 1.5).repeatForever(autoreverses: true)) {
            glowIntensity = 0.7
        }
        
        // Slow rotation
        withAnimation(.linear(duration: 20).repeatForever(autoreverses: false)) {
            rotationAngle = 360
        }
    }
    
    private func stopAnimation() {
        withAnimation(.smooth(duration: 0.5)) {
            barHeights = Array(repeating: minHeight, count: barCount)
            glowIntensity = 0.3
            rotationAngle = 0
        }
    }
    
    private func updateAnimationIntensity() {
        updateBarHeights()
    }
    
    private func updateBarHeights() {
        for index in 0..<barCount {
            let delay = Double(index) * 0.02
            DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                withAnimation(.easeInOut(duration: 0.3)) {
                    if isActive {
                        // Create wave pattern with varying intensities
                        let phase = Double(index) / Double(barCount) * .pi * 2
                        let baseHeight = (sin(phase) + 1) / 2
                        let randomVariation = Double.random(in: 0.6...1.0)
                        let intensity = isListening || isSpeaking ? 1.0 : 0.5
                        barHeights[index] = minHeight + (maxHeight - minHeight) * baseHeight * randomVariation * intensity
                    } else {
                        barHeights[index] = minHeight
                    }
                }
            }
        }
        
        // Schedule next update
        if isActive {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                updateBarHeights()
            }
        }
    }
}

#Preview {
    ZStack {
        LinearGradient(
            colors: [.oceanBlue, .mediumBlue],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
        .ignoresSafeArea()
        
        AudioVisualizerView(
            isActive: true,
            isListening: true,
            isProcessing: false,
            isSpeaking: false
        )
    }
}
