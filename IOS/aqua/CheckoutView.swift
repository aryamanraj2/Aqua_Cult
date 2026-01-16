//
//  CheckoutView.swift
//  aqua
//
//  Checkout and order confirmation view
//

import SwiftUI

enum PaymentMethod: String, CaseIterable {
    case cod = "Cash on Delivery"
    case upi = "UPI"
    case card = "Credit/Debit Card"
        
    var icon: String {
        switch self {
        case .cod: return "indianrupeesign.circle.fill"
        case .upi: return "qrcode"
        case .card: return "creditcard.fill"
        }
    }
}

struct CheckoutView: View {
    let cartManager: CartManager
    let userProfile: UserProfile
    let onOrderComplete: () -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var orderPlaced = false
    @State private var showConfetti = false
    @State private var name = ""
    @State private var phone = ""
    @State private var address = ""
    @State private var pincode = ""
    @State private var paymentMethod = PaymentMethod.cod
    @State private var isEditingName = false
    @State private var isEditingPhone = false
    @State private var isEditingAddress = false
    @State private var isEditingPincode = false
    
    var isFormValid: Bool {
        !name.isEmpty && !phone.isEmpty && !address.isEmpty && pincode.count == 6
    }
    
    var body: some View {
        NavigationStack {
            ZStack {
                if orderPlaced {
                    orderConfirmationView
                } else {
                    checkoutForm
                }
            }
            .navigationTitle("Checkout")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                if !orderPlaced {
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
            }
            .interactiveDismissDisabled(orderPlaced)
            .onAppear {
                // Pre-fill user data from profile
                name = userProfile.fullName
                phone = userProfile.mobile
                address = userProfile.address
                pincode = userProfile.pincode
            }
        }
    }
    
    private var checkoutForm: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Order summary
                orderSummarySection
                
                // Delivery details
                deliveryDetailsSection
                
                // Payment method
                paymentMethodSection
                
                // Place order button
                placeOrderButton
            }
            .padding()
        }
    }
    
    private var orderSummarySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Order Summary")
                .font(.headline)
            
            VStack(spacing: 8) {
                ForEach(cartManager.items) { item in
                    HStack {
                        Text("\(item.quantity) × \(item.product.name)")
                            .font(.subheadline)
                            .lineLimit(1)
                        Spacer()
                        Text(item.formattedTotal)
                            .font(.subheadline)
                            .fontWeight(.medium)
                    }
                }
                
                Divider()
                
                HStack {
                    Text("Subtotal")
                        .foregroundStyle(.secondary)
                    Spacer()
                    Text(cartManager.formattedSubtotal)
                }
                .font(.subheadline)
                
                HStack {
                    Text("GST (18%)")
                        .foregroundStyle(.secondary)
                    Spacer()
                    Text(cartManager.formattedGST)
                }
                .font(.subheadline)
                
                HStack {
                    Text("Shipping")
                        .foregroundStyle(.secondary)
                    Spacer()
                    Text(cartManager.formattedShipping)
                        .foregroundStyle(cartManager.shippingFee == 0 ? .green : .primary)
                }
                .font(.subheadline)
                
                Divider()
                
                HStack {
                    Text("Total")
                        .font(.headline)
                    Spacer()
                    Text(cartManager.formattedTotal)
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundStyle(Color.oceanBlue)
                }
            }
            .padding()
            .background(.background, in: RoundedRectangle(cornerRadius: 16))
        }
    }
    
    private var deliveryDetailsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Delivery Details")
                .font(.headline)
            
            VStack(spacing: 12) {
                // Name Field
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text("Full Name")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        Spacer()
                        if !isEditingName {
                            Button("Edit") {
                                isEditingName = true
                            }
                            .font(.caption)
                            .foregroundStyle(Color.oceanBlue)
                        }
                    }
                    
                    if isEditingName {
                        TextField("Full Name", text: $name)
                            .textFieldStyle(CustomTextFieldStyle())
                            .onSubmit {
                                isEditingName = false
                            }
                    } else {
                        Text(name.isEmpty ? "Enter your full name" : name)
                            .font(.body)
                            .foregroundStyle(name.isEmpty ? .secondary : .primary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.vertical, 12)
                            .padding(.horizontal, 16)
                            .background(.background, in: RoundedRectangle(cornerRadius: 12))
                            .onTapGesture {
                                isEditingName = true
                            }
                    }
                }
                
                // Phone Field
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text("Phone Number")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        Spacer()
                        if !isEditingPhone {
                            Button("Edit") {
                                isEditingPhone = true
                            }
                            .font(.caption)
                            .foregroundStyle(Color.oceanBlue)
                        }
                    }
                    
                    if isEditingPhone {
                        TextField("Phone Number", text: $phone)
                            .textFieldStyle(CustomTextFieldStyle())
                            .keyboardType(.phonePad)
                            .onSubmit {
                                isEditingPhone = false
                            }
                    } else {
                        Text(phone.isEmpty ? "Enter your phone number" : phone)
                            .font(.body)
                            .foregroundStyle(phone.isEmpty ? .secondary : .primary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.vertical, 12)
                            .padding(.horizontal, 16)
                            .background(.background, in: RoundedRectangle(cornerRadius: 12))
                            .onTapGesture {
                                isEditingPhone = true
                            }
                    }
                }
                
                // Address Field
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text("Address")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        Spacer()
                        if !isEditingAddress {
                            Button("Edit") {
                                isEditingAddress = true
                            }
                            .font(.caption)
                            .foregroundStyle(Color.oceanBlue)
                        }
                    }
                    
                    if isEditingAddress {
                        TextField("Address", text: $address, axis: .vertical)
                            .textFieldStyle(CustomTextFieldStyle())
                            .lineLimit(3...5)
                            .onSubmit {
                                isEditingAddress = false
                            }
                    } else {
                        Text(address.isEmpty ? "Enter your address" : address)
                            .font(.body)
                            .foregroundStyle(address.isEmpty ? .secondary : .primary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.vertical, 12)
                            .padding(.horizontal, 16)
                            .background(.background, in: RoundedRectangle(cornerRadius: 12))
                            .onTapGesture {
                                isEditingAddress = true
                            }
                    }
                }
                
                // PIN Code Field
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text("PIN Code")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        Spacer()
                        if !isEditingPincode {
                            Button("Edit") {
                                isEditingPincode = true
                            }
                            .font(.caption)
                            .foregroundStyle(Color.oceanBlue)
                        }
                    }
                    
                    if isEditingPincode {
                        TextField("PIN Code", text: $pincode)
                            .textFieldStyle(CustomTextFieldStyle())
                            .keyboardType(.numberPad)
                            .onChange(of: pincode) { _, newValue in
                                if newValue.count > 6 {
                                    pincode = String(newValue.prefix(6))
                                }
                            }
                            .onSubmit {
                                isEditingPincode = false
                            }
                    } else {
                        Text(pincode.isEmpty ? "Enter PIN code" : pincode)
                            .font(.body)
                            .foregroundStyle(pincode.isEmpty ? .secondary : .primary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.vertical, 12)
                            .padding(.horizontal, 16)
                            .background(.background, in: RoundedRectangle(cornerRadius: 12))
                            .onTapGesture {
                                isEditingPincode = true
                            }
                    }
                }
            }
        }
    }
    
    private var paymentMethodSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Payment Method")
                .font(.headline)
            
            VStack(spacing: 8) {
                ForEach(PaymentMethod.allCases, id: \.self) { method in
                    PaymentMethodRow(
                        method: method,
                        isSelected: paymentMethod == method,
                        onTap: { paymentMethod = method }
                    )
                }
            }
        }
    }
    
    private var placeOrderButton: some View {
        Button {
            placeOrder()
        } label: {
            HStack {
                Text("Place Order")
                    .font(.headline)
                Image(systemName: "checkmark.circle.fill")
            }
            .frame(maxWidth: .infinity)
            .padding(16)
            .background(
                isFormValid ?
                LinearGradient(
                    colors: [Color.oceanBlue, Color.mediumBlue],
                    startPoint: .leading,
                    endPoint: .trailing
                ) : LinearGradient(colors: [.gray], startPoint: .leading, endPoint: .trailing),
                in: RoundedRectangle(cornerRadius: 16)
            )
            .foregroundStyle(.white)
            .shadow(color: isFormValid ? Color.oceanBlue.opacity(0.3) : .clear, radius: 12, y: 4)
        }
        .disabled(!isFormValid)
        .padding(.top)
    }
    
    private var orderConfirmationView: some View {
        ZStack {
            VStack(spacing: 24) {
                Spacer()
                
                // Animated checkmark
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [Color.aquaGreen.opacity(0.2), Color.aquaGreen.opacity(0.1)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 120, height: 120)
                        .scaleEffect(showConfetti ? 1 : 0.5)
                        .opacity(showConfetti ? 1 : 0)
                    
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 80))
                        .foregroundStyle(Color.aquaGreen)
                        .scaleEffect(showConfetti ? 1 : 0.3)
                        .rotationEffect(.degrees(showConfetti ? 0 : -180))
                }
                .padding(.bottom, 8)
                
                VStack(spacing: 12) {
                    Text("Order Confirmed!")
                        .font(.title)
                        .fontWeight(.bold)
                        .opacity(showConfetti ? 1 : 0)
                        .offset(y: showConfetti ? 0 : 20)
                    
                    Text("Your order has been placed successfully")
                        .font(.body)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                        .opacity(showConfetti ? 1 : 0)
                        .offset(y: showConfetti ? 0 : 20)
                }
                
                VStack(spacing: 16) {
                    InfoRow(
                        icon: "person.fill",
                        title: "Delivery to",
                        value: name
                    )
                    
                    InfoRow(
                        icon: "phone.fill",
                        title: "Contact",
                        value: phone
                    )
                    
                    InfoRow(
                        icon: "location.fill",
                        title: "Address",
                        value: address
                    )
                    
                    InfoRow(
                        icon: paymentMethod.icon,
                        title: "Payment",
                        value: paymentMethod.rawValue
                    )
                    
                    InfoRow(
                        icon: "indianrupeesign.circle.fill",
                        title: "Total Amount",
                        value: cartManager.formattedTotal,
                        valueColor: Color.oceanBlue
                    )
                }
                .padding()
                .background(.background, in: RoundedRectangle(cornerRadius: 20))
                .opacity(showConfetti ? 1 : 0)
                .offset(y: showConfetti ? 0 : 30)
                
                Spacer()
                
                Button {
                    cartManager.clearCart()
                    onOrderComplete()
                } label: {
                    Text("Continue Shopping")
                        .font(.headline)
                        .foregroundStyle(.white)
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
                        .shadow(color: Color.oceanBlue.opacity(0.3), radius: 12, y: 4)
                }
                .opacity(showConfetti ? 1 : 0)
                .offset(y: showConfetti ? 0 : 20)
            }
            .padding()
            
            // Confetti effect
            if showConfetti {
                ForEach(0..<30, id: \.self) { index in
                    ConfettiPiece(index: index)
                }
            }
        }
        .onAppear {
            withAnimation(.spring(response: 0.6, dampingFraction: 0.7)) {
                showConfetti = true
            }
            
            // Haptic feedback
            let generator = UINotificationFeedbackGenerator()
            generator.notificationOccurred(.success)
        }
    }
    
    private func placeOrder() {
        // Simulate order processing
        withAnimation(.spring(response: 0.5, dampingFraction: 0.8)) {
            orderPlaced = true
        }
    }
}

struct CustomTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(12)
            .background(.background, in: RoundedRectangle(cornerRadius: 10))
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .strokeBorder(Color(.separator), lineWidth: 1)
            )
    }
}

struct InfoRow: View {
    let icon: String
    let title: String
    let value: String
    var valueColor: Color = .primary
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundStyle(Color.oceanBlue)
                .frame(width: 32)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(value)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundStyle(valueColor)
            }
            
            Spacer()
        }
    }
}

struct PaymentMethodRow: View {
    let method: PaymentMethod
    let isSelected: Bool
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            HStack {
                Image(systemName: method.icon)
                    .font(.title3)
                    .foregroundStyle(Color.oceanBlue)
                    .frame(width: 32)
                
                Text(method.rawValue)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundStyle(.primary)
                
                Spacer()
                
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundStyle(Color.oceanBlue)
                } else {
                    Image(systemName: "circle")
                        .foregroundStyle(.secondary)
                }
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(isSelected ? Color.oceanBlue.opacity(0.1) : .clear)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? Color.oceanBlue : Color.clear, lineWidth: 2)
            )
        }
    }
}

struct ConfettiPiece: View {
    let index: Int
    @State private var isAnimating = false
    
    private var randomColor: Color {
        [Color.oceanBlue, Color.aquaGreen, Color.aquaYellow, Color.subtleBlueAccent].randomElement() ?? Color.oceanBlue
    }
    
    private var randomX: CGFloat {
        CGFloat.random(in: -150...150)
    }
    
    private var randomRotation: Double {
        Double.random(in: 0...720)
    }
    
    private var randomDelay: Double {
        Double(index) * 0.02
    }
    
    var body: some View {
        RoundedRectangle(cornerRadius: 2)
            .fill(randomColor)
            .frame(width: 8, height: 12)
            .offset(x: isAnimating ? randomX : 0, y: isAnimating ? 600 : -50)
            .rotationEffect(.degrees(isAnimating ? randomRotation : 0))
            .opacity(isAnimating ? 0 : 1)
            .onAppear {
                withAnimation(
                    .easeOut(duration: 1.5)
                    .delay(randomDelay)
                ) {
                    isAnimating = true
                }
            }
    }
}

#Preview {
    CheckoutView(
        cartManager: {
            let manager = CartManager()
            manager.addToCart(product: MarketplaceProduct.sampleProducts[0], quantity: 2)
            return manager
        }(),
        userProfile: UserProfile.sampleProfile,
        onOrderComplete: {}
    )
}
