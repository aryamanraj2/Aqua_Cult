//
//  CartManager.swift
//  aqua
//
//  Cart management system
//

import Foundation
import SwiftUI
import Combine

@MainActor
class CartManager: ObservableObject {
    @Published var items: [CartItem] = []
    
    var totalItems: Int {
        items.reduce(0) { $0 + $1.quantity }
    }
    
    var subtotal: Double {
        items.reduce(0) { $0 + $1.totalPrice }
    }
    
    var shippingFee: Double {
        subtotal > 10000 ? 0 : 150 // Free shipping above ₹10,000
    }
    
    var gst: Double {
        subtotal * 0.18 // 18% GST
    }
    
    var total: Double {
        subtotal + shippingFee + gst
    }
    
    var formattedSubtotal: String {
        String(format: "₹%.2f", subtotal)
    }
    
    var formattedShipping: String {
        shippingFee == 0 ? "FREE" : String(format: "₹%.2f", shippingFee)
    }
    
    var formattedGST: String {
        String(format: "₹%.2f", gst)
    }
    
    var formattedTotal: String {
        String(format: "₹%.2f", total)
    }
    
    func addToCart(product: MarketplaceProduct, quantity: Int = 1) {
        if let index = items.firstIndex(where: { $0.product.id == product.id }) {
            items[index].quantity += quantity
        } else {
            items.append(CartItem(product: product, quantity: quantity))
        }
    }
    
    func removeFromCart(item: CartItem) {
        items.removeAll { $0.id == item.id }
    }
    
    func updateQuantity(item: CartItem, quantity: Int) {
        if let index = items.firstIndex(where: { $0.id == item.id }) {
            if quantity > 0 {
                items[index].quantity = quantity
            } else {
                items.remove(at: index)
            }
        }
    }
    
    func clearCart() {
        items.removeAll()
    }
    
    func isInCart(product: MarketplaceProduct) -> Bool {
        items.contains { $0.product.id == product.id }
    }
    
    func getQuantity(for product: MarketplaceProduct) -> Int {
        items.first { $0.product.id == product.id }?.quantity ?? 0
    }
    
    func removeFromCart(product: MarketplaceProduct) {
        if let index = items.firstIndex(where: { $0.product.id == product.id }) {
            if items[index].quantity > 1 {
                items[index].quantity -= 1
            } else {
                items.remove(at: index)
            }
        }
    }
}
