
import SwiftUI

struct SplashScreenView: View {
    @AppStorage("hasOnboarded") private var hasOnboarded: Bool = false
    @State private var isActive = false
    @State private var waveTraceProgress: CGFloat = 0.0
    @State private var waveOpacity: Double = 0.0
    @State private var waveScale: CGFloat = 0.8
    @State private var textOpacity: Double = 0.0
    @State private var showFillAnimation: Bool = false
    @State private var bubblesOpacity: Double = 0.0
    
    var body: some View {
        if isActive {
            if hasOnboarded {
                ContentView()
            } else {
                OnboardingView()
            }
        } else {
            ZStack {
                // Ocean gradient background
                LinearGradient(
                    gradient: Gradient(colors: [Color.deepOcean, Color.mediumBlue]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
                
                VStack(spacing: 40) {
                    // Main wave animation
                    ZStack {
                        // Traced wave outline (back wave)
                        WaveShape()
                            .trim(from: 0.0, to: waveTraceProgress)
                            .stroke(
                                Color.oceanBlue.opacity(0.6),
                                style: StrokeStyle(lineWidth: 3, lineCap: .round, lineJoin: .round)
                            )
                            .frame(width: 140, height: 100)
                            .scaleEffect(waveScale)
                            .opacity(waveOpacity)
                        
                        // Middle wave layer
                        WaveShape()
                            .trim(from: 0.0, to: waveTraceProgress)
                            .stroke(
                                Color.oceanBlue.opacity(0.8),
                                style: StrokeStyle(lineWidth: 3.5, lineCap: .round, lineJoin: .round)
                            )
                            .frame(width: 140, height: 100)
                            .scaleEffect(waveScale * 0.95)
                            .offset(y: 10)
                            .opacity(waveOpacity)
                        
                        // Front wave layer - filled after tracing
                        WaveShape()
                            .fill(
                                LinearGradient(
                                    gradient: Gradient(colors: [
                                        Color.oceanBlue.opacity(0.4),
                                        Color.oceanBlue.opacity(0.7)
                                    ]),
                                    startPoint: .top,
                                    endPoint: .bottom
                                )
                            )
                            .frame(width: 140, height: 100)
                            .scaleEffect(waveScale * 0.9)
                            .offset(y: 20)
                            .opacity(showFillAnimation ? waveOpacity : 0)
                        
                        // Wave details (foam and curls)
                        WaveDetails()
                            .trim(from: 0.0, to: waveTraceProgress)
                            .stroke(
                                Color.white.opacity(0.9),
                                style: StrokeStyle(lineWidth: 2, lineCap: .round, lineJoin: .round)
                            )
                            .frame(width: 140, height: 100)
                            .scaleEffect(waveScale)
                            .opacity(waveOpacity)
                        
                        // Bubbles/spray
                        ForEach(0..<5) { index in
                            Circle()
                                .fill(Color.white.opacity(0.4))
                                .frame(width: CGFloat(8 - index), height: CGFloat(8 - index))
                                .offset(
                                    x: CGFloat([-60, -30, 0, 30, 60][index]),
                                    y: -50 - CGFloat(index) * 5
                                )
                                .opacity(bubblesOpacity)
                        }
                    }
                    
                    // App title
                    VStack(spacing: 8) {
                        Text("Aquacult")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .opacity(textOpacity)
                        
                        Text("Smart Aquaculture Management")
                            .font(.subheadline)
                            .foregroundColor(.white.opacity(0.8))
                            .opacity(textOpacity)
                    }
                }
            }
            .onAppear {
                startAnimation()
            }
        }
    }
    
    private func startAnimation() {
        // Wave appears
        withAnimation(.easeOut(duration: 0.5)) {
            waveOpacity = 1.0
            waveScale = 1.0
        }
        
        // Wave tracing animation
        withAnimation(.easeInOut(duration: 2.0).delay(0.3)) {
            waveTraceProgress = 1.0
        }
        
        // Fill animation after tracing
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            withAnimation(.easeInOut(duration: 0.5)) {
                showFillAnimation = true
            }
        }
        
        // Bubbles/spray appear
        withAnimation(.easeOut(duration: 0.8).delay(2.0)) {
            bubblesOpacity = 1.0
        }
        
        // Text appears
        withAnimation(.easeOut(duration: 0.6).delay(1.5)) {
            textOpacity = 1.0
        }
        
        // Transition to main app
        DispatchQueue.main.asyncAfter(deadline: .now() + 3.5) {
            withAnimation(.easeInOut(duration: 0.5)) {
                isActive = true
            }
        }
    }
}

// MARK: - Custom Shapes

struct WaveShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let width = rect.width
        let height = rect.height
        
        // Start from left side
        path.move(to: CGPoint(x: 0, y: height * 0.65))
        
        // First wave crest
        path.addCurve(
            to: CGPoint(x: width * 0.25, y: height * 0.3),
            control1: CGPoint(x: width * 0.08, y: height * 0.55),
            control2: CGPoint(x: width * 0.17, y: height * 0.35)
        )
        
        // First wave curl/tip
        path.addCurve(
            to: CGPoint(x: width * 0.35, y: height * 0.4),
            control1: CGPoint(x: width * 0.28, y: height * 0.25),
            control2: CGPoint(x: width * 0.32, y: height * 0.32)
        )
        
        // Second wave rise
        path.addCurve(
            to: CGPoint(x: width * 0.55, y: height * 0.25),
            control1: CGPoint(x: width * 0.42, y: height * 0.35),
            control2: CGPoint(x: width * 0.48, y: height * 0.28)
        )
        
        // Second wave curl/tip
        path.addCurve(
            to: CGPoint(x: width * 0.65, y: height * 0.35),
            control1: CGPoint(x: width * 0.58, y: height * 0.22),
            control2: CGPoint(x: width * 0.62, y: height * 0.28)
        )
        
        // Third wave rise
        path.addCurve(
            to: CGPoint(x: width * 0.85, y: height * 0.2),
            control1: CGPoint(x: width * 0.72, y: height * 0.32),
            control2: CGPoint(x: width * 0.78, y: height * 0.24)
        )
        
        // Final wave tip
        path.addCurve(
            to: CGPoint(x: width, y: height * 0.28),
            control1: CGPoint(x: width * 0.90, y: height * 0.18),
            control2: CGPoint(x: width * 0.95, y: height * 0.22)
        )
        
        // Base line back
        path.addLine(to: CGPoint(x: width, y: height))
        path.addLine(to: CGPoint(x: 0, y: height))
        path.closeSubpath()
        
        return path
    }
}

struct WaveDetails: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let width = rect.width
        let height = rect.height
        
        // Foam/spray on first wave
        path.move(to: CGPoint(x: width * 0.25, y: height * 0.3))
        path.addCurve(
            to: CGPoint(x: width * 0.28, y: height * 0.35),
            control1: CGPoint(x: width * 0.26, y: height * 0.28),
            control2: CGPoint(x: width * 0.27, y: height * 0.32)
        )
        
        // Curl detail on first wave
        path.addArc(
            center: CGPoint(x: width * 0.30, y: height * 0.36),
            radius: width * 0.04,
            startAngle: .degrees(180),
            endAngle: .degrees(360),
            clockwise: false
        )
        
        // Foam/spray on second wave
        path.move(to: CGPoint(x: width * 0.55, y: height * 0.25))
        path.addCurve(
            to: CGPoint(x: width * 0.58, y: height * 0.30),
            control1: CGPoint(x: width * 0.56, y: height * 0.23),
            control2: CGPoint(x: width * 0.57, y: height * 0.27)
        )
        
        // Curl detail on second wave
        path.addArc(
            center: CGPoint(x: width * 0.60, y: height * 0.31),
            radius: width * 0.04,
            startAngle: .degrees(180),
            endAngle: .degrees(360),
            clockwise: false
        )
        
        // Foam/spray on third wave
        path.move(to: CGPoint(x: width * 0.85, y: height * 0.2))
        path.addCurve(
            to: CGPoint(x: width * 0.88, y: height * 0.24),
            control1: CGPoint(x: width * 0.86, y: height * 0.18),
            control2: CGPoint(x: width * 0.87, y: height * 0.21)
        )
        
        // Curl detail on third wave
        path.addArc(
            center: CGPoint(x: width * 0.90, y: height * 0.25),
            radius: width * 0.03,
            startAngle: .degrees(180),
            endAngle: .degrees(360),
            clockwise: false
        )
        
        return path
    }
}

#Preview {
    SplashScreenView()
}
