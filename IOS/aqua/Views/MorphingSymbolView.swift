//
//  MorphingSymbolView.swift
//  aqua
//
//  SF Symbol Morphing Animation View
//

import SwiftUI

struct MorphingSymbolView: View {
    // Configuration struct for customization
    struct Config {
        var font: Font = .system(size: 140, weight: .bold)
        var frame: CGSize = CGSize(width: 240, height: 240)
        var radius: CGFloat = 28
        var foregroundColor: Color = .white
        var keyFrameDuration: CGFloat = 0.35
        var symbolAnimation: Animation = .smooth(duration: 0.5, extraBounce: 0.1)
    }
    
    // View properties
    let symbol: String
    let config: Config
    
    // Internal state for animation
    @State private var trigger: Bool = false
    @State private var currentSymbol: String
    @State private var nextSymbol: String
    
    init(symbol: String, config: Config) {
        self.symbol = symbol
        self.config = config
        _currentSymbol = State(initialValue: symbol)
        _nextSymbol = State(initialValue: symbol)
    }
    
    var body: some View {
        Canvas { ctx, size in
            // Apply blur and alpha threshold filters for metaball morphing effect
            ctx.addFilter(.alphaThreshold(min: 0.4, color: config.foregroundColor))
            ctx.addFilter(.blur(radius: trigger ? config.radius : 0))
            
            // Resolve and draw the symbol
            if let resolvedSymbol = ctx.resolveSymbol(id: "morphedSymbol") {
                ctx.draw(resolvedSymbol, at: CGPoint(x: size.width / 2, y: size.height / 2))
            }
        }
        .frame(width: config.frame.width, height: config.frame.height)
        .background(Color.clear)
        .overlay {
            // Overlay the actual Image that Canvas will resolve
            Image(systemName: currentSymbol)
                .font(config.font)
                .foregroundColor(config.foregroundColor)
                .tag("morphedSymbol")
                .opacity(0) // Hidden - Canvas draws the visible one
        }
        .onChange(of: symbol) { oldValue, newValue in
            // Store the new symbol
            self.nextSymbol = newValue
            // Start the blur animation
            self.trigger.toggle()
            
            // Replace symbol at peak blur (mid-point of animation)
            DispatchQueue.main.asyncAfter(deadline: .now() + config.keyFrameDuration) {
                self.currentSymbol = self.nextSymbol
            }
        }
        // KeyframeAnimator controls the blur radius animation
        .keyframeAnimator(initialValue: CGFloat.zero, trigger: trigger) { content, radiusValue in
            content
        } keyframes: { radius in
            // Blur in: 0 to config.radius
            CubicKeyframe(config.radius, duration: config.keyFrameDuration)
            // Blur out: config.radius to 0
            CubicKeyframe(.zero, duration: config.keyFrameDuration)
        }
    }
}

#Preview {
    ZStack {
        LinearGradient(
            gradient: Gradient(colors: [Color.deepOcean, Color.mediumBlue]),
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
        .ignoresSafeArea()
        
        VStack(spacing: 30) {
            MorphingSymbolView(
                symbol: "fish.fill",
                config: MorphingSymbolView.Config()
            )
            
            Text("Aqua Sense")
                .font(.system(size: 34, weight: .bold))
                .foregroundColor(.white)
        }
    }
}
