
import SwiftUI

struct ProductDetailView: View {
    let product: MarketplaceProduct
    @ObservedObject var cartManager: CartManager
    @Environment(\.dismiss) private var dismiss
    @State private var quantity = 1
    @State private var showingAddedAlert = false
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // Product image
                    productImage
                    
                    // Product info
                    VStack(alignment: .leading, spacing: 16) {
                        // Category badge
                        HStack {
                            Label(product.category.rawValue, systemImage: product.category.icon)
                                .font(.caption)
                                .fontWeight(.medium)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(Color.oceanBlue.opacity(0.1), in: Capsule())
                                .foregroundStyle(Color.oceanBlue)
                            
                            Spacer()
                            
                            // Rating
                            HStack(spacing: 4) {
                                Image(systemName: "star.fill")
                                    .foregroundStyle(.yellow)
                                Text(String(format: "%.1f", product.rating))
                                    .fontWeight(.semibold)
                            }
                            .font(.subheadline)
                        }
                        
                        // Name & price
                        VStack(alignment: .leading, spacing: 8) {
                            Text(product.name)
                                .font(.title2)
                                .fontWeight(.bold)
                            
                            HStack(alignment: .firstTextBaseline, spacing: 4) {
                                Text(product.formattedPrice)
                                    .font(.title)
                                    .fontWeight(.bold)
                                    .foregroundStyle(Color.oceanBlue)
                                
                                Text("/ \(product.unit)")
                                    .font(.subheadline)
                                    .foregroundStyle(.secondary)
                            }
                        }
                        
                        Divider()
                        
                        // Description
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Description")
                                .font(.headline)
                            
                            Text(product.description)
                                .font(.body)
                                .foregroundStyle(.secondary)
                        }
                        
                        Divider()
                        
                        // Stock status
                        HStack {
                            Image(systemName: product.inStock ? "checkmark.circle.fill" : "xmark.circle.fill")
                                .foregroundStyle(product.inStock ? .green : .red)
                            Text(product.inStock ? "In Stock" : "Out of Stock")
                                .font(.subheadline)
                                .fontWeight(.medium)
                        }
                        
                        // Quantity selector (only if in stock)
                        if product.inStock {
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Quantity")
                                    .font(.headline)
                                
                                HStack(spacing: 16) {
                                    Button {
                                        if quantity > 1 {
                                            quantity -= 1
                                        }
                                    } label: {
                                        Image(systemName: "minus")
                                            .font(.body.weight(.semibold))
                                            .frame(width: 40, height: 40)
                                            .background(Color.oceanBlue.opacity(0.1), in: Circle())
                                            .foregroundStyle(Color.oceanBlue)
                                    }
                                    .disabled(quantity <= 1)
                                    .opacity(quantity <= 1 ? 0.5 : 1)
                                    
                                    Text("\(quantity)")
                                        .font(.title3)
                                        .fontWeight(.semibold)
                                        .frame(minWidth: 40)
                                    
                                    Button {
                                        if quantity < 99 {
                                            quantity += 1
                                        }
                                    } label: {
                                        Image(systemName: "plus")
                                            .font(.body.weight(.semibold))
                                            .frame(width: 40, height: 40)
                                            .background(Color.oceanBlue, in: Circle())
                                            .foregroundStyle(.white)
                                    }
                                    .disabled(quantity >= 99)
                                    .opacity(quantity >= 99 ? 0.5 : 1)
                                    
                                    Spacer()
                                    
                                    VStack(alignment: .trailing, spacing: 2) {
                                        Text("Total")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                        Text(String(format: "₹%.2f", product.price * Double(quantity)))
                                            .font(.title3)
                                            .fontWeight(.bold)
                                            .foregroundStyle(Color.oceanBlue)
                                    }
                                }
                            }
                        }
                    }
                    .padding()
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title3)
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .safeAreaInset(edge: .bottom) {
                if product.inStock {
                    addToCartButton
                }
            }
            .alert("Added to Cart", isPresented: $showingAddedAlert) {
                Button("OK", role: .cancel) { }
            } message: {
                Text("\(quantity) × \(product.name) added to your cart")
            }
        }
    }
    
    private var productImage: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 20)
                .fill(
                    LinearGradient(
                        colors: [.subtleBlueLight, .subtleBlueMid],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
            
            Image(systemName: product.imageName)
                .font(.system(size: 80))
                .foregroundStyle(Color.oceanBlue)
        }
        .frame(height: 240)
        .padding()
    }
    
    private var addToCartButton: some View {
        Button {
            cartManager.addToCart(product: product, quantity: quantity)
            showingAddedAlert = true
            // Haptic feedback
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.impactOccurred()
        } label: {
            HStack {
                Image(systemName: "cart.fill.badge.plus")
                    .font(.title3)
                Text("Add to Cart")
                    .font(.headline)
            }
            .frame(maxWidth: .infinity)
            .padding(16)
            .background(
                LinearGradient(
                    colors: [Color.oceanBlue, Color.mediumBlue],
                    startPoint: .leading,
                    endPoint: .trailing
                ),
                in: RoundedRectangle(cornerRadius: 16)
            )
            .foregroundStyle(.white)
            .shadow(color: Color.oceanBlue.opacity(0.3), radius: 12, y: 4)
        }
        .padding()
        .background(.ultraThinMaterial)
    }
}

#Preview {
    ProductDetailView(
        product: MarketplaceProduct.sampleProducts[0],
        cartManager: CartManager()
    )
}
