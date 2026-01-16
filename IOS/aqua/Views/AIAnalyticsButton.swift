
import SwiftUI

struct AIAnalyticsButton: View {
    let tank: Tank
    @EnvironmentObject private var cartManager: CartManager
    @EnvironmentObject private var profileManager: UserProfileManager
    @State private var isPressed = false
    @State private var isHovering = false
    @State private var showingAnalytics = false
    
    var body: some View {
        Button {
            showingAnalytics = true
        } label: {
            ZStack {
                // Liquid Glass Background Layer
                RoundedRectangle(cornerRadius: 16)
                    .fill(.ultraThinMaterial)
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(
                                LinearGradient(
                                    colors: [
                                        Color.white.opacity(0.25),
                                        Color.white.opacity(0.1)
                                    ],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(
                                LinearGradient(
                                    colors: [
                                        Color.white.opacity(0.5),
                                        Color.white.opacity(0.2)
                                    ],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 1.5
                            )
                    )
                    .frame(width: 52, height: 52)
                
                // Inner glow effect
                RoundedRectangle(cornerRadius: 16)
                    .fill(
                        RadialGradient(
                            colors: [
                                Color.blue.opacity(0.12),
                                Color.clear
                            ],
                            center: .center,
                            startRadius: 0,
                            endRadius: 30
                        )
                    )
                    .frame(width: 52, height: 52)
                    .opacity(isHovering ? 1 : 0.7)
                
                // AI Icon - simple and clean
                Image(systemName: "brain.head.profile")
                    .font(.system(size: 22, weight: .medium))
                    .foregroundColor(Color(red: 0.2, green: 0.4, blue: 0.8))
                    .shadow(color: Color.blue.opacity(0.2), radius: 3, x: 0, y: 1)
            }
            .scaleEffect(isPressed ? 0.92 : 1.0)
            .shadow(
                color: Color.blue.opacity(0.2),
                radius: isHovering ? 12 : 8,
                x: 0,
                y: isHovering ? 6 : 4
            )
        }
        .buttonStyle(PlainButtonStyle())
        .onLongPressGesture(minimumDuration: 0.1, pressing: { pressing in
            withAnimation(.easeInOut(duration: 0.15)) {
                isPressed = pressing
            }
        }, perform: {})
        .onHover { hovering in
            withAnimation(.easeInOut(duration: 0.2)) {
                isHovering = hovering
            }
        }
        .sheet(isPresented: $showingAnalytics) {
            AIAnalyticsView(tank: tank)
                .environmentObject(cartManager)
                .environmentObject(profileManager)
        }
    }
}

// MARK: - Preview
#Preview {
    VStack(spacing: 40) {
        AIAnalyticsButton(tank: Tank.sampleTanks[0])
        
        AIAnalyticsButton(tank: Tank.sampleTanks[0])
            .preferredColorScheme(.dark)
    }
    .padding(40)
    .background(
        LinearGradient(
            colors: [Color.blue.opacity(0.1), Color.purple.opacity(0.1)],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    )
    .environmentObject(CartManager())
    .environmentObject(UserProfileManager())
}
