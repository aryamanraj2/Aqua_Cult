

import SwiftUI
import UIKit
import PhotosUI

struct DiseaseDetectionView: View {
    @StateObject private var cameraController = CameraController()
    @ObservedObject private var localizationManager = LocalizationManager.shared
    @State private var capturedImage: UIImage?
    @State private var predictionResult: DiseaseResult?
    @State private var isAnalyzing = false
    @State private var showingImagePicker = false
    @State private var errorMessage: String?
    @State private var showResults = false
    @State private var showResultsSheet = false

    var body: some View {
        ZStack {
            // Full-screen camera preview
            CameraPreviewView(
                capturedImage: $capturedImage,
                onCapture: {
                    analyzeImage()
                },
                cameraController: cameraController
            )
            .ignoresSafeArea()

            // Overlay UI
            VStack {
                // Top section with title
                VStack(spacing: 8) {
                    Text(localizationManager.localizedString(for: "disease_detection"))
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.white)
                        .shadow(color: .black.opacity(0.3), radius: 2, x: 0, y: 1)

                    Text(localizationManager.localizedString(for: "point_camera"))
                        .font(.system(size: 16))
                        .foregroundColor(.white.opacity(0.9))
                        .shadow(color: .black.opacity(0.3), radius: 2, x: 0, y: 1)

                    Text(localizationManager.localizedString(for: "ai_analyze"))
                        .font(.system(size: 16))
                        .foregroundColor(.white.opacity(0.9))
                        .shadow(color: .black.opacity(0.3), radius: 2, x: 0, y: 1)
                }
                .padding(.top, 60)
                
                Spacer()
                
                // Bottom controls
                HStack(spacing: 60) {
                    // Photo library button
                    Button(action: {
                        showingImagePicker = true
                    }) {
                        Image(systemName: "photo.fill")
                            .font(.system(size: 28))
                            .foregroundColor(.white)
                            .frame(width: 60, height: 60)
                            .background(
                                Circle()
                                    .fill(Color.oceanBlue.opacity(0.6))
                                    .overlay(
                                        Circle()
                                            .stroke(Color.subtleBlueAccent.opacity(0.5), lineWidth: 2)
                                    )
                            )
                    }
                    
                    // Capture button
                    Button(action: {
                        cameraController.capturePhoto()
                    }) {
                        ZStack {
                            Circle()
                                .fill(Color.white)
                                .frame(width: 70, height: 70)
                            
                            Circle()
                                .stroke(Color.white, lineWidth: 4)
                                .frame(width: 82, height: 82)
                        }
                    }
                    .disabled(isAnalyzing)
                    
                    // Flash/Settings button placeholder
                    Button(action: {
                        // Toggle flash or settings
                    }) {
                        Image(systemName: "bolt.slash.fill")
                            .font(.system(size: 28))
                            .foregroundColor(.white)
                            .frame(width: 60, height: 60)
                            .background(
                                Circle()
                                    .fill(Color.oceanBlue.opacity(0.6))
                                    .overlay(
                                        Circle()
                                            .stroke(Color.subtleBlueAccent.opacity(0.5), lineWidth: 2)
                                    )
                            )
                    }
                }
                .padding(.bottom, 100)
            }
            
            // Analysis overlay
            if isAnalyzing {
                AnalyzingOverlay()
            }
            
            // Error overlay
            if let error = errorMessage {
                ErrorOverlay(message: error, onDismiss: {
                    errorMessage = nil
                    capturedImage = nil
                })
                .transition(.opacity)
            }
        }
        .sheet(isPresented: $showingImagePicker) {
            ImagePicker(selectedImage: $capturedImage, sourceType: .photoLibrary)
                .onDisappear {
                    if capturedImage != nil {
                        analyzeImage()
                    }
                }
        }
        .sheet(isPresented: $showResultsSheet) {
            if let result = predictionResult {
                ResultsSheetView(result: result, onDismiss: {
                    showResultsSheet = false
                    predictionResult = nil
                    capturedImage = nil
                })
            }
        }
    }
    
    private func analyzeImage() {
        guard let image = capturedImage else { return }
        
        isAnalyzing = true
        errorMessage = nil
        
        // Perform ML prediction
        DiseaseClassifier.shared.classifyImage(image) { result in
            DispatchQueue.main.async {
                isAnalyzing = false
                
                switch result {
                case .success(let diseaseResult):
                    predictionResult = diseaseResult
                    showResultsSheet = true
                case .failure(let error):
                    errorMessage = error.localizedDescription
                }
            }
        }
    }
}

// MARK: - Overlay Views

struct AnalyzingOverlay: View {
    @ObservedObject private var localizationManager = LocalizationManager.shared

    var body: some View {
        ZStack {
            Color.black.opacity(0.7)
                .ignoresSafeArea()

            VStack(spacing: 20) {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .subtleBlueAccent))
                    .scaleEffect(1.5)

                Text(localizationManager.localizedString(for: "analyzing_fish"))
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
            }
            .padding(40)
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color.oceanBlue.opacity(0.7),
                                Color.mediumBlue.opacity(0.8)
                            ]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
            )
            .cornerRadius(20)
        }
    }
}

struct ResultsSheetView: View {
    let result: DiseaseResult
    let onDismiss: () -> Void
    @EnvironmentObject private var cartManager: CartManager
    @ObservedObject private var localizationManager = LocalizationManager.shared
    @State private var showCart = false

    var body: some View {
        NavigationView {
            // Results view - no loading state needed, the parent view handles loading
            ScrollView {
                        VStack(alignment: .leading, spacing: 20) {
                            // Header
                            VStack(alignment: .leading, spacing: 4) {
                                Text(localizationManager.localizedString(for: "analysis_complete"))
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.mediumGray)

                                Text(result.diseaseName)
                                    .font(.system(size: 26, weight: .bold))
                                    .foregroundColor(.deepOcean)
                            }

                            // Confidence badge
                            HStack {
                                Text("\(Int(result.confidence * 100))% \(localizationManager.localizedString(for: "confidence"))")
                                    .font(.system(size: 14, weight: .semibold))
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(
                                        LinearGradient(
                                            gradient: Gradient(colors: [
                                                confidenceColor.opacity(0.15),
                                                confidenceColor.opacity(0.25)
                                            ]),
                                            startPoint: .leading,
                                            endPoint: .trailing
                                        )
                                    )
                                    .foregroundColor(confidenceColor)
                                    .cornerRadius(8)

                                Spacer()
                            }

                            // Description
                            if !result.description.isEmpty {
                                VStack(alignment: .leading, spacing: 8) {
                                    Text(localizationManager.localizedString(for: "about"))
                                        .font(.system(size: 20, weight: .semibold))
                                        .foregroundColor(.deepOcean)

                                    Text(result.description)
                                        .font(.system(size: 14))
                                        .foregroundColor(.mediumGray)
                                        .lineSpacing(4)
                                }
                            }

                            // Recommendations
                            if !result.recommendations.isEmpty {
                                VStack(alignment: .leading, spacing: 12) {
                                    Text(localizationManager.localizedString(for: "recommendations"))
                                        .font(.system(size: 20, weight: .semibold))
                                        .foregroundColor(.deepOcean)

                                    ForEach(result.recommendations, id: \.self) { recommendation in
                                        HStack(alignment: .top, spacing: 12) {
                                            Image(systemName: "checkmark.circle.fill")
                                                .foregroundColor(.oceanBlue)
                                                .font(.system(size: 16))

                                            Text(recommendation)
                                                .font(.system(size: 14))
                                                .foregroundColor(.mediumGray)
                                                .lineSpacing(4)
                                        }
                                    }
                                }
                            }

                            // Recommended Products
                            if !result.recommendedProducts.isEmpty {
                                VStack(alignment: .leading, spacing: 12) {
                                    Text(localizationManager.localizedString(for: "recommended_products"))
                                        .font(.system(size: 20, weight: .semibold))
                                        .foregroundColor(.deepOcean)

                                    ForEach(result.recommendedProducts) { product in
                                        DiseaseProductCard(product: product)
                                    }
                                }
                            }
                        }
                        .padding(24)
                    }
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [Color.white, Color.subtleBlueLight.opacity(0.3)]),
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
            .navigationTitle(localizationManager.localizedString(for: "disease_analysis"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(localizationManager.localizedString(for: "done")) {
                        onDismiss()
                    }
                }
            }
        }
        .overlay(
            // Floating cart button
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    if !cartManager.items.isEmpty {
                        floatingCartButton
                            .padding(.horizontal)
                            .padding(.bottom, 8)
                    }
                }
            }
        )
        .sheet(isPresented: $showCart) {
            CartView(cartManager: cartManager, userProfile: UserProfileManager().profile)
        }
    }
    
    private var confidenceColor: Color {
        if result.confidence >= 0.8 {
            return .aquaGreen
        } else if result.confidence >= 0.6 {
            return .aquaYellow
        } else {
            return .aquaRed
        }
    }
    
    private var floatingCartButton: some View {
        Button {
            showCart = true
        } label: {
            HStack(spacing: 12) {
                Image(systemName: "cart.fill")
                    .font(.title3)

                VStack(alignment: .leading, spacing: 2) {
                    Text("\(cartManager.totalItems) \(localizationManager.localizedString(for: "items"))")
                        .font(.caption)
                        .fontWeight(.medium)
                    Text(cartManager.formattedTotal)
                        .font(.headline)
                }
                
                Spacer()
                
                Image(systemName: "chevron.right")
                    .font(.body.weight(.semibold))
            }
            .foregroundStyle(.white)
            .padding(16)
            .background(
                LinearGradient(
                    colors: [.oceanBlue, .mediumBlue],
                    startPoint: .leading,
                    endPoint: .trailing
                ),
                in: RoundedRectangle(cornerRadius: 16)
            )
            .shadow(color: .oceanBlue.opacity(0.3), radius: 12, y: 4)
        }
    }
}

struct ErrorOverlay: View {
    let message: String
    let onDismiss: () -> Void
    @ObservedObject private var localizationManager = LocalizationManager.shared

    var body: some View {
        ZStack {
            Color.black.opacity(0.7)
                .ignoresSafeArea()

            VStack(spacing: 20) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 50))
                    .foregroundColor(.aquaRed)

                Text(localizationManager.localizedString(for: "analysis_failed"))
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)

                Text(message)
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.9))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)

                Button(action: onDismiss) {
                    Text(localizationManager.localizedString(for: "try_again"))
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(width: 200, height: 50)
                        .background(
                            LinearGradient(
                                gradient: Gradient(colors: [Color.oceanBlue, Color.mediumBlue]),
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .cornerRadius(12)
                }
                .padding(.top, 8)
            }
            .padding(40)
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color.oceanBlue.opacity(0.7),
                                Color.mediumBlue.opacity(0.8)
                            ]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
            )
            .cornerRadius(20)
        }
    }
}

// MARK: - Supporting Models

struct DiseaseResult {
    let diseaseName: String
    let confidence: Double
    let description: String
    let recommendations: [String]
    let recommendedProducts: [MarketplaceProduct]
}

// MARK: - Image Picker

struct ImagePicker: UIViewControllerRepresentable {
    @Binding var selectedImage: UIImage?
    @Environment(\.dismiss) var dismiss
    let sourceType: UIImagePickerController.SourceType
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = sourceType
        picker.delegate = context.coordinator
        picker.allowsEditing = true
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: ImagePicker
        
        init(_ parent: ImagePicker) {
            self.parent = parent
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            DispatchQueue.main.async {
                if let editedImage = info[.editedImage] as? UIImage {
                    self.parent.selectedImage = editedImage
                } else if let originalImage = info[.originalImage] as? UIImage {
                    self.parent.selectedImage = originalImage
                }
                
                self.parent.dismiss()
            }
        }
        
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            DispatchQueue.main.async {
                self.parent.dismiss()
            }
        }
    }
}

// MARK: - Disease Product Card
struct DiseaseProductCard: View {
    let product: MarketplaceProduct
    @EnvironmentObject private var cartManager: CartManager
    @ObservedObject private var localizationManager = LocalizationManager.shared
    @State private var dragAmount = CGSize.zero

    private var currentQuantity: Int {
        cartManager.getQuantity(for: product)
    }

    var body: some View {
        VStack(spacing: 12) {
            HStack(spacing: 14) {
                // Icon
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    Color.oceanBlue.opacity(0.1),
                                    Color.mediumBlue.opacity(0.2)
                                ]),
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 50, height: 50)

                    Image(systemName: product.imageName)
                        .font(.system(size: 24))
                        .foregroundColor(.oceanBlue)
                }

                // Content
                VStack(alignment: .leading, spacing: 4) {
                    Text(product.name)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(.deepOcean)

                    Text(product.description)
                        .font(.system(size: 12))
                        .foregroundColor(.mediumGray)
                        .lineLimit(2)

                    HStack {
                        Text(product.formattedPrice)
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.oceanBlue)

                        Text("per \(product.unit)")
                            .font(.system(size: 11))
                            .foregroundColor(.mediumGray)

                        Spacer()

                        if !product.inStock {
                            Text(localizationManager.localizedString(for: "out_of_stock"))
                                .font(.system(size: 10, weight: .medium))
                                .foregroundColor(.aquaRed)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(
                                    Capsule()
                                        .fill(Color.aquaRed.opacity(0.1))
                                )
                        }
                    }

                    // Rating
                    HStack(spacing: 2) {
                        ForEach(1...5, id: \.self) { star in
                            Image(systemName: star <= Int(product.rating) ? "star.fill" : "star")
                                .font(.system(size: 10))
                                .foregroundColor(star <= Int(product.rating) ? .aquaYellow : .lightGray)
                        }
                        Text(String(format: "%.1f", product.rating))
                            .font(.system(size: 10))
                            .foregroundColor(.mediumGray)
                    }
                }

                Spacer()
            }

            // Swipe Quantity Control
            HStack {
                // Decrease indicator (swipe right to decrease)
                HStack(spacing: 4) {
                    Image(systemName: "minus.circle.fill")
                        .foregroundColor(.aquaRed)
                        .opacity(dragAmount.width > 30 ? 1.0 : 0.3)
                    Text("Swipe →")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(.aquaRed)
                        .opacity(dragAmount.width > 30 ? 1.0 : 0.5)
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                // Current quantity display
                VStack(spacing: 2) {
                    if currentQuantity > 0 {
                        Text("\(currentQuantity)")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.oceanBlue)
                        Text(localizationManager.localizedString(for: "in_cart"))
                            .font(.system(size: 10))
                            .foregroundColor(.mediumGray)
                    } else {
                        Text("0")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.lightGray)
                        Text(localizationManager.localizedString(for: "add_to_cart"))
                            .font(.system(size: 10))
                            .foregroundColor(.mediumGray)
                    }
                }
                .frame(minWidth: 80)
                
                // Increase indicator (swipe left to increase)
                HStack(spacing: 4) {
                    Text("← Swipe")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(.aquaGreen)
                        .opacity(dragAmount.width < -30 ? 1.0 : 0.5)
                    Image(systemName: "plus.circle.fill")
                        .foregroundColor(.aquaGreen)
                        .opacity(dragAmount.width < -30 ? 1.0 : 0.3)
                }
                .frame(maxWidth: .infinity, alignment: .trailing)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(product.inStock ? Color.subtleBlueLight.opacity(0.5) : Color.lightGray.opacity(0.3))
            )
            .offset(x: dragAmount.width * 0.1) // Subtle visual feedback
            .gesture(
                DragGesture()
                    .onChanged { value in
                        if product.inStock {
                            dragAmount = value.translation
                        }
                    }
                    .onEnded { value in
                        if product.inStock {
                            let swipeThreshold: CGFloat = 50
                            
                            if value.translation.width > swipeThreshold && currentQuantity > 0 {
                                // Swipe right - decrease quantity
                                cartManager.removeFromCart(product: product)
                            } else if value.translation.width < -swipeThreshold {
                                // Swipe left - increase quantity
                                cartManager.addToCart(product: product)
                            }
                        }
                        
                        withAnimation(.spring()) {
                            dragAmount = .zero
                        }
                    }
            )
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.white)
                .shadow(color: Color.oceanBlue.opacity(0.08), radius: 10, x: 0, y: 5)
        )
    }
}

#Preview {
    DiseaseDetectionView()
}
