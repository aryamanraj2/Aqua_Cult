//
//  CartView.swift
//  aqua
//
//  Shopping cart view
//

import SwiftUI

struct CartView: View {
    @ObservedObject var cartManager: CartManager
    let userProfile: UserProfile
    @Environment(\.dismiss) private var dismiss
    @State private var showingCheckout = false
    
    var body: some View {
        NavigationStack {
            ZStack {
                if cartManager.items.isEmpty {
                    emptyCartView
                } else {
                    cartContent
                }
            }
            .navigationTitle("Shopping Cart")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title3)
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .sheet(isPresented: $showingCheckout) {
                CheckoutView(cartManager: cartManager, userProfile: userProfile, onOrderComplete: {
                    dismiss()
                })
            }
        }
    }
    
    private var emptyCartView: some View {
        VStack(spacing: 16) {
            Image(systemName: "cart")
                .font(.system(size: 60))
                .foregroundStyle(.secondary)
            
            Text("Your cart is empty")
                .font(.title2)
                .fontWeight(.semibold)
            
            Text("Add some products to get started")
                .font(.body)
                .foregroundStyle(.secondary)
            
            Button {
                dismiss()
            } label: {
                Text("Continue Shopping")
                    .font(.headline)
                    .foregroundStyle(.white)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(Color.oceanBlue, in: Capsule())
            }
            .padding(.top, 8)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private var cartContent: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 16) {
                    ForEach(cartManager.items) { item in
                        CartItemRow(item: item, cartManager: cartManager)
                    }
                }
                .padding()
                .padding(.bottom, 280) // Space for summary
            }
            
            cartSummary
        }
    }
    
    private var cartSummary: some View {
        VStack(spacing: 0) {
            Divider()
            
            VStack(spacing: 12) {
                HStack {
                    Text("Subtotal")
                        .foregroundStyle(.secondary)
                    Spacer()
                    Text(cartManager.formattedSubtotal)
                        .fontWeight(.medium)
                }
                
                HStack {
                    Text("GST (18%)")
                        .foregroundStyle(.secondary)
                    Spacer()
                    Text(cartManager.formattedGST)
                        .fontWeight(.medium)
                }
                
                HStack {
                    HStack(spacing: 4) {
                        Text("Shipping")
                            .foregroundStyle(.secondary)
                        if cartManager.shippingFee == 0 {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.caption)
                                .foregroundStyle(.green)
                        }
                    }
                    Spacer()
                    Text(cartManager.formattedShipping)
                        .fontWeight(.medium)
                        .foregroundStyle(cartManager.shippingFee == 0 ? .green : .primary)
                }
                
                if cartManager.subtotal < 10000 {
                    Text("Add ₹\(String(format: "%.0f", 10000 - cartManager.subtotal)) more for free shipping")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
                
                Divider()
                    .padding(.vertical, 4)
                
                HStack {
                    Text("Total")
                        .font(.headline)
                    Spacer()
                    Text(cartManager.formattedTotal)
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundStyle(Color.oceanBlue)
                }
                
                Button {
                    showingCheckout = true
                } label: {
                    HStack {
                        Text("Proceed to Checkout")
                            .font(.headline)
                        Image(systemName: "arrow.right")
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
                .padding(.top, 4)
            }
            .padding()
            .background(.ultraThinMaterial)
        }
    }
}

struct CartItemRow: View {
    let item: CartItem
    @ObservedObject var cartManager: CartManager
    
    var body: some View {
        HStack(spacing: 12) {
            // Product image
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(
                        LinearGradient(
                            colors: [.subtleBlueLight, .subtleBlueMid],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                
                Image(systemName: item.product.imageName)
                    .font(.title2)
                    .foregroundStyle(Color.oceanBlue)
            }
            .frame(width: 80, height: 80)
            
            // Product info
            VStack(alignment: .leading, spacing: 4) {
                Text(item.product.name)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .lineLimit(2)
                
                Text(item.product.formattedPrice)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                
                Spacer()
                
                // Quantity controls
                HStack(spacing: 12) {
                    Button {
                        withAnimation(.easeInOut(duration: 0.2)) {
                            cartManager.updateQuantity(item: item, quantity: item.quantity - 1)
                        }
                    } label: {
                        Image(systemName: "minus.circle.fill")
                            .font(.title3)
                            .foregroundStyle(Color.oceanBlue)
                    }
                    
                    Text("\(item.quantity)")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .frame(minWidth: 24)
                        .animation(.easeInOut(duration: 0.2), value: item.quantity)
                    
                    Button {
                        withAnimation(.easeInOut(duration: 0.2)) {
                            cartManager.updateQuantity(item: item, quantity: item.quantity + 1)
                        }
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.title3)
                            .foregroundStyle(Color.oceanBlue)
                    }
                }
            }
            
            Spacer()
            
            // Price and remove
            VStack(alignment: .trailing, spacing: 8) {
                Button {
                    withAnimation {
                        cartManager.removeFromCart(item: item)
                    }
                } label: {
                    Image(systemName: "trash.fill")
                        .font(.callout)
                        .foregroundStyle(.red)
                }
                
                Spacer()
                
                Text(item.formattedTotal)
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .foregroundStyle(Color.oceanBlue)
            }
        }
        .padding(12)
        .background(.background, in: RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 8, y: 2)
    }
}

#Preview {
    CartView(cartManager: {
        let manager = CartManager()
        manager.addToCart(product: MarketplaceProduct.sampleProducts[0], quantity: 2)
        manager.addToCart(product: MarketplaceProduct.sampleProducts[6], quantity: 1)
        return manager
    }(), userProfile: UserProfile.sampleProfile)
}
