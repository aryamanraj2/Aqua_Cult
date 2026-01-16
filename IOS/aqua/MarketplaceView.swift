//
//  MarketplaceView.swift
//  aqua
//
//  Main marketplace view
//

import SwiftUI

struct MarketplaceView: View {
    @EnvironmentObject private var cartManager: CartManager
    @EnvironmentObject private var profileManager: UserProfileManager
    @State private var selectedCategory: ProductCategory?
    @State private var searchText = ""
    @State private var showingCart = false
    
    var filteredProducts: [MarketplaceProduct] {
        var products = MarketplaceProduct.sampleProducts
        
        if let category = selectedCategory {
            products = products.filter { $0.category == category }
        }
        
        if !searchText.isEmpty {
            products = products.filter {
                $0.name.localizedCaseInsensitiveContains(searchText) ||
                $0.description.localizedCaseInsensitiveContains(searchText)
            }
        }
        
        return products
    }
    
    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(spacing: 20) {
                    // Header
                    headerSection
                    
                    // Search bar
                    searchBar
                    
                    // Category filter
                    categoryFilter
                    
                    // Products grid
                    productsGrid
                }
                .padding(.bottom, 100)
            }
            
            // Floating cart button
            if !cartManager.items.isEmpty {
                floatingCartButton
            }
        }
        .sheet(isPresented: $showingCart) {
            CartView(cartManager: cartManager, userProfile: profileManager.profile)
        }
        .background(
            LinearGradient(
                gradient: Gradient(colors: [Color.subtleBlueLight, Color.subtleBlueMid]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .navigationTitle("Marketplace")
        .navigationBarTitleDisplayMode(.large)
    }
    
    private var headerSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Quality supplies for your aquaculture")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal)
        .padding(.top, 8)
    }
    
    private var searchBar: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundStyle(.secondary)
            
            TextField("Search products...", text: $searchText)
                .textFieldStyle(.plain)
            
            if !searchText.isEmpty {
                Button {
                    searchText = ""
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundStyle(.secondary)
                }
            }
        }
        .padding(12)
        .background(.background, in: RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal)
    }
    
    private var categoryFilter: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                CategoryChip(
                    title: "All",
                    icon: "square.grid.2x2.fill",
                    isSelected: selectedCategory == nil
                ) {
                    withAnimation(.spring(response: 0.3)) {
                        selectedCategory = nil
                    }
                }
                
                ForEach(ProductCategory.allCases) { category in
                    CategoryChip(
                        title: category.rawValue,
                        icon: category.icon,
                        isSelected: selectedCategory == category
                    ) {
                        withAnimation(.spring(response: 0.3)) {
                            selectedCategory = category == selectedCategory ? nil : category
                        }
                    }
                }
            }
            .padding(.horizontal)
        }
    }
    
    private var productsGrid: some View {
        LazyVGrid(columns: [
            GridItem(.flexible(), spacing: 16),
            GridItem(.flexible(), spacing: 16)
        ], spacing: 16) {
            ForEach(filteredProducts) { product in
                MarketplaceProductCard(product: product, cartManager: cartManager)
            }
        }
        .padding(.horizontal)
    }
    
    private var floatingCartButton: some View {
        Button {
            showingCart = true
        } label: {
            HStack(spacing: 12) {
                Image(systemName: "cart.fill")
                    .font(.title3)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text("\(cartManager.totalItems) items")
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
            .padding(.horizontal)
            .padding(.bottom, 8)
        }
    }
}

struct CategoryChip: View {
    let title: String
    let icon: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.caption)
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(
                isSelected ? Color.oceanBlue : Color(.systemBackground),
                in: Capsule()
            )
            .foregroundStyle(isSelected ? .white : .primary)
            .overlay(
                Capsule()
                    .strokeBorder(isSelected ? Color.clear : Color(.separator), lineWidth: 1)
            )
        }
    }
}

struct MarketplaceProductCard: View {
    let product: MarketplaceProduct
    @ObservedObject var cartManager: CartManager
    @State private var showingDetail = false
    
    var body: some View {
        Button {
            showingDetail = true
        } label: {
            VStack(alignment: .leading, spacing: 8) {
                // Product icon
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(
                            LinearGradient(
                                colors: [.subtleBlueLight, .subtleBlueMid],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                    
                    Image(systemName: product.imageName)
                        .font(.system(size: 32))
                        .foregroundStyle(Color.oceanBlue)
                }
                .frame(height: 120)
                .overlay(alignment: .topTrailing) {
                    if !product.inStock {
                        Text("Out of Stock")
                            .font(.caption2)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(.red, in: Capsule())
                            .foregroundStyle(.white)
                            .padding(8)
                    }
                }
                
                // Product info
                VStack(alignment: .leading, spacing: 4) {
                    Text(product.name)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .lineLimit(2)
                        .foregroundStyle(.primary)
                    
                    HStack(spacing: 4) {
                        Image(systemName: "star.fill")
                            .font(.caption2)
                            .foregroundStyle(.yellow)
                        Text(String(format: "%.1f", product.rating))
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    
                    Spacer()
                    
                    Text(product.formattedPrice)
                        .font(.headline)
                        .foregroundStyle(Color.oceanBlue)
                }
                .padding(.horizontal, 4)
            }
            .padding(12)
            .background(.background, in: RoundedRectangle(cornerRadius: 16))
            .shadow(color: .black.opacity(0.05), radius: 8, y: 2)
        }
        .buttonStyle(.plain)
        .sheet(isPresented: $showingDetail) {
            ProductDetailView(product: product, cartManager: cartManager)
        }
    }
}

#Preview {
    NavigationStack {
        MarketplaceView()
            .environmentObject(CartManager())
            .environmentObject(UserProfileManager())
    }
}
