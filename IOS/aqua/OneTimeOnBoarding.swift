import SwiftUI

/// OnBoarding Item
struct OnBoardingItem: Identifiable {
    var id: Int
    var view: AnyView
    var maskLocation: CGRect
}

/// OnBoarding Coordinator
@Observable
fileprivate class OnBoardingCoordinator {
    var items: [OnBoardingItem] = []
    var overlayWindow: UIWindow?
    var isOnboardingFinished: Bool = false

    /// Ordered Items
    ///
    var orderedItems: [OnBoardingItem] {
        items.sorted { $0.id < $1.id }
        
    }
}

struct OneTimeOnBoarding<Content: View>: View {
    @AppStorage var isOnBoarded: Bool
    var content: Content

    /// Allows you to do job before animating the onboarding effect!
    var beginOnboarding: () async -> Void
    var onBoardingFinished: () -> Void

    init(
        appStorageID: String,
        @ViewBuilder content: @escaping () -> Content,
        beginOnboarding: @escaping () async -> Void,
        onBoardingFinished: @escaping () -> Void
    ) {
        /// Initializing User-Defaults!!
        self._isOnBoarded = .init(wrappedValue: false, appStorageID)
        self.content = content()
        self.beginOnboarding = beginOnboarding
        self.onBoardingFinished = onBoardingFinished
    }
    fileprivate var coordinator = OnBoardingCoordinator()

    var body: some View {
        content
            .environment(coordinator)
            .task {
                await beginOnboarding()
                await createWindow()
            }
            .onChange(of: coordinator.isOnboardingFinished){ oldValue, newValue in
                if newValue{
                    onBoardingFinished()
                    hideWindow()
                    
                }
                
            }
    }

    private func createWindow() async {
        if let scene = (UIApplication.shared.connectedScenes.first as? UIWindowScene),
           !isOnBoarded,
           coordinator.overlayWindow == nil {

            let window = UIWindow(windowScene: scene)
            window.backgroundColor = .clear
            window.isHidden = false
            window.isUserInteractionEnabled = true

            coordinator.overlayWindow = window

            
            try? await Task.sleep(for: .seconds(0.1))
            if coordinator.items.isEmpty{
                hideWindow()
            }else{
                guard let snapshot = snapshotScreen() else {
                     hideWindow()
                     return
                }
                let hostController = UIHostingController(
                    rootView: OverlayWindowView(snapshot: snapshot)
                        .environment(coordinator)
                )

                hostController.view.backgroundColor = .clear
                coordinator.overlayWindow?.rootViewController = hostController

            }
        }
    }

    private func hideWindow() {
        coordinator.overlayWindow?.rootViewController = nil
        coordinator.overlayWindow?.isHidden = true
        coordinator.overlayWindow?.isUserInteractionEnabled = false
    }

    

}
extension View {
    /// You can pass custom shape for each onboarding item as well!
    @ViewBuilder
    func onBoarding<Content: View>(
        _ position: Int,
        @ViewBuilder content: @escaping () -> Content
    ) -> some View {
        self
            .modifier(
                OnBoardingItemSetter(
                    position: position,
                    onBoardingContent: content
                )
            ) 
    }
}

/// OnBoarding Item-Setter
fileprivate
struct OnBoardingItemSetter<OnBoardingContent: View>: ViewModifier {
    var position: Int
    @ViewBuilder var onBoardingContent: OnBoardingContent

    @Environment(OnBoardingCoordinator.self) var coordinator

    func body(content: Content) -> some View {
        content
            /// Adding/Removing item to the coordinator object!
            .onGeometryChange(for: CGRect.self) {
                $0.frame(in: .global)
            } action: { newValue in
                coordinator.items.removeAll(where: { $0.id == position })

                let newItem = OnBoardingItem(
                    id: position,
                    view: .init(onBoardingContent),
                    maskLocation: newValue
                )

                coordinator.items.append(newItem)
            }
            .onDisappear {
                coordinator.items.removeAll(where: { $0.id == position })
            }
    }
}

/// Overlay Window View (Animation View)
fileprivate struct OverlayWindowView: View {
    var snapshot: UIImage

    @Environment(OnBoardingCoordinator.self) var coordinator
    @State private var animate: Bool = false
    @State private var currentIndex: Int = 0
    @State private var dismissOpacity: Double = 1.0

    var body: some View {
        GeometryReader {
            let safeArea = $0.safeAreaInsets
            let isHomeButtoniPhone = safeArea.bottom == 0
            let cornerRadius: CGFloat = isHomeButtoniPhone ? 15 : 35

            ZStack {
                LinearGradient(
                    gradient: Gradient(colors: [Color.deepOcean, Color.mediumBlue]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )

                Image(uiImage: snapshot)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .clipShape(
                        .rect(
                            cornerRadius: animate ? cornerRadius : 0,
                            style: .circular
                        )
                    )
                    .overlay{
                        Rectangle()
                            .fill(.black.opacity(0.3 ))
                            .reverseMask(alignment: .topLeading) {
                                if !coordinator.orderedItems.isEmpty {
                                    let maskLocation = coordinator.orderedItems[currentIndex].maskLocation

                                    RoundedRectangle(cornerRadius: 35, style: .continuous)
                                        .frame(
                                             width: maskLocation.width,
                                            height: maskLocation.height
                                        )
                                        .offset(
                                            x: maskLocation.minX,
                                            y: maskLocation.minY
                                        )
                                }
                                
                            }

                    }
                    .overlay {
                        
                        iPhoneShape(safeArea, animate: animate)
                    }
                    .scaleEffect(animate ? 0.68 : 1, anchor: .top)
                    .offset(x: 0, y: animate ? (safeArea.top + 25) : 0)
                    .frame (maxWidth: .infinity, maxHeight: .infinity)
                    .background(alignment: .bottom) {
                        BottomView(safeArea, orderedItems: coordinator.orderedItems, currentIndex: $currentIndex, onFinish: closeWindow)
                    }
                    .opacity(animate ? 1 : 0)
                
                
            }
            .opacity(dismissOpacity)
            .ignoresSafeArea()
        }
        .onAppear {
            guard !animate else { return }
            withAnimation(.smooth(duration: 0.35, extraBounce: 0)) {
                animate = true
            }
            
        }
    }
    
    private func closeWindow() {
        // First animate the phone back to full size
        withAnimation(.smooth(duration: 0.3, extraBounce: 0)) {
            animate = false
        }
        
        // Then fade out the entire overlay
        withAnimation(.easeOut(duration: 0.25).delay(0.15)) {
            dismissOpacity = 0
        }
        
        // Finally mark as finished after animations complete
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
            coordinator.isOnboardingFinished = true
        }
    }
}

@ViewBuilder
private func iPhoneShape(_ safeArea: EdgeInsets, animate: Bool) -> some View {
    let isHomeButtoniPhone = safeArea.bottom == 0
    let cornerRadius: CGFloat = isHomeButtoniPhone ? 20 : 45

    ZStack(alignment: .top) {
        RoundedRectangle(
            cornerRadius: animate ? cornerRadius : 0,
            style: .continuous
        )
        .stroke(.white, lineWidth: animate ? 15 : 0)
        .padding(-6)
    }
}

@ViewBuilder
private func BottomView(_ safeArea: EdgeInsets, orderedItems: [OnBoardingItem], currentIndex: Binding<Int>, onFinish: @escaping () -> Void) -> some View {
    VStack(spacing: 16) {
        // Pin icon
        Image(systemName: "pin.fill")
            .font(.system(size: 16))
            .foregroundStyle(.gray)
            .rotationEffect(.degrees(45))
        
        /// Switching between the onboarding items view!
        ZStack {
            ForEach(orderedItems) { info in
                let idx = orderedItems.firstIndex(where: { $0.id == info.id })
                if currentIndex.wrappedValue == idx {
                    info.view
                        .transition(.blurReplace)
                        .environment(\.colorScheme, .dark)
                }
            }
        }
        .frame(maxWidth: 280)
        
        /// Continue, Back, Skip Button - Liquid Glass Style
        VStack(spacing: 12) {
            GlassEffectContainer(spacing: 8) {
                HStack(spacing: 8) {
                    // Back Button - only visible when not on first item
                    if currentIndex.wrappedValue > 0 {
                        Button {
                            withAnimation(.smooth(duration: 0.35, extraBounce: 0)) {
                                currentIndex.wrappedValue = max((currentIndex.wrappedValue - 1), 0)
                            }
                        } label: {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 18, weight: .semibold))
                                .foregroundStyle(.white)
                                .frame(width: 50, height: 50)
                        }
                        .glassEffect(.regular.interactive(), in: .capsule)
                        .transition(.asymmetric(
                            insertion: .scale(scale: 0.5).combined(with: .opacity),
                            removal: .scale(scale: 0.5).combined(with: .opacity)
                        ))
                    }
                    
                    // Next/Finish Button
                    Button {
                        if currentIndex.wrappedValue == orderedItems.count - 1 {
                            onFinish()
                        } else {
                            withAnimation(.smooth(duration: 0.35, extraBounce: 0)) {
                                currentIndex.wrappedValue += 1
                            }
                        }
                    } label: {
                        Text(currentIndex.wrappedValue == orderedItems.count - 1 ? "Finish" : "Next")
                            .fontWeight(.semibold)
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .contentTransition(.numericText())
                    }
                    .glassEffect(.regular.interactive().tint(.blue), in: .capsule)
                    .frame(maxWidth: currentIndex.wrappedValue > 0 ? 200 : 250)
                }
            }
            .frame(maxWidth: 280)
            .animation(.smooth(duration: 0.35, extraBounce: 0), value: currentIndex.wrappedValue)

            Button(action: onFinish) {
                Text("Skip Tutorial")
                    .font(.callout)
                    .underline()
            }
            .foregroundStyle(.gray)
        }
    }
    .padding(.horizontal, 15)
    .padding(.bottom, safeArea.bottom + 10)
}

extension View {
    /// Snapshotting the screen
    fileprivate func snapshotScreen() -> UIImage? {
        if let snapshotView = (UIApplication.shared.connectedScenes.first as? UIWindowScene)?.keyWindow {
            let renderer: UIGraphicsImageRenderer = UIGraphicsImageRenderer(
                size: snapshotView.bounds.size
            )

            let image: UIImage = renderer.image { context in
                snapshotView.drawHierarchy(
                    in: snapshotView.bounds,
                    afterScreenUpdates: true
                )
            }

            return image
        }

        return nil
    }
    /// Reverse Mask
    @ViewBuilder
    func reverseMask<Content: View>(
        alignment: Alignment = .center,
        @ViewBuilder content: @escaping () -> Content
    ) -> some View {
        self
            .mask {
                Rectangle()
                    .overlay(alignment: alignment) {
                        content()
                            .blendMode(.destinationOut)
                    }
            }
    }
}

// MARK: - Tutorial Content View
/// Reusable view for displaying tutorial step content
struct TutorialContentView: View {
    let title: String
    let description: String
    let icon: String
    
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 32))
                .foregroundStyle(.tint)
            
            Text(title)
                .font(.headline)
                .foregroundStyle(.white)
            
            Text(description)
                .font(.caption)
                .foregroundStyle(.gray)
                .multilineTextAlignment(.center)
        }
    }
}

#Preview {
    TutorialContentView(
        title: "Voice & Add Tank",
        description: "Use the microphone for voice commands or tap the plus to add a new tank.",
        icon: "mic.and.plus"
    )
}

