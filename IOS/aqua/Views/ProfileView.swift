//
//  ProfileView.swift
//  aqua
//
//  Created on 02/11/25.
//

import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var tankManager: TankManager
    @EnvironmentObject var profileManager: UserProfileManager
    @State private var showingSettings = false
    @State private var showingEditProfile = false
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Profile Header
                profileHeader
                
                // Stats Section
                statsSection
                
                // Settings & Information
                settingsSection
            }
            .padding(.bottom, 100)
        }
        .background(
            LinearGradient(
                gradient: Gradient(colors: [Color.subtleBlueLight, Color.subtleBlueMid]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .navigationTitle("Profile")
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    showingEditProfile = true
                } label: {
                    Text("Edit")
                        .foregroundColor(.oceanBlue)
                }
            }
        }
        .sheet(isPresented: $showingEditProfile) {
            EditProfileView(profile: $profileManager.profile)
        }
        .sheet(isPresented: $showingSettings) {
            SettingsView()
        }
    }
    
    private var profileHeader: some View {
        VStack(spacing: 16) {
            // Profile Image
            ZStack {
                Circle()
                    .fill(Color.oceanBlue.opacity(0.1))
                    .frame(width: 120, height: 120)
                
                Image(systemName: "person.fill")
                    .font(.system(size: 50))
                    .foregroundColor(.oceanBlue)
            }
            .overlay(
                Circle()
                    .stroke(Color.oceanBlue, lineWidth: 3)
            )
            
            VStack(spacing: 4) {
                Text(profileManager.profile.fullName)
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(.primary)
                
                Text(profileManager.profile.mobile)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                
                Text(profileManager.profile.address)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                
                Text("Pincode: \(profileManager.profile.pincode)")
                    .font(.caption2)
                    .foregroundColor(.secondary)
                
                Text(profileManager.profile.location)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.horizontal)
        .padding(.top, 20)
    }
    
    private var statsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("My Aquaculture")
                .font(.headline)
                .fontWeight(.semibold)
                .padding(.horizontal)
            
            HStack(spacing: 16) {
                StatCard(
                    icon: "fish.fill",
                    value: "\(tankManager.tankCount)",
                    label: "Active Tanks",
                    color: .oceanBlue
                )
                
                StatCard(
                    icon: "drop.fill",
                    value: String(format: "%.1f", tankManager.totalVolume) + "m³",
                    label: "Total Volume",
                    color: .blue
                )
                
                StatCard(
                    icon: "calendar",
                    value: "\(profileManager.profile.experienceYears)",
                    label: "Years Experience",
                    color: .green
                )
            }
            .padding(.horizontal)
        }
        .padding(.vertical)
        .background(Color.white.opacity(0.8))
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 8, y: 2)
        .padding(.horizontal)
    }
    
    private var settingsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Settings & Info")
                .font(.headline)
                .fontWeight(.semibold)
                .padding(.horizontal)
            
            VStack(spacing: 0) {
                SettingsRow(
                    icon: "gear.circle.fill",
                    title: "App Settings",
                    subtitle: "Preferences & configuration"
                ) {
                    showingSettings = true
                }
                
                Divider()
                    .padding(.leading, 60)
                
                SettingsRow(
                    icon: "questionmark.circle.fill",
                    title: "Help & Support",
                    subtitle: "FAQs and contact support"
                ) {
                    // Handle help action
                }
                
                Divider()
                    .padding(.leading, 60)
                
                SettingsRow(
                    icon: "info.circle.fill",
                    title: "About Aqua",
                    subtitle: "Version \(profileManager.profile.appVersion)"
                ) {
                    // Handle about action
                }
                
                Divider()
                    .padding(.leading, 60)
                
                SettingsRow(
                    icon: "rectangle.portrait.and.arrow.right",
                    title: "Sign Out",
                    subtitle: "Log out of your account",
                    isDestructive: true
                ) {
                    // Handle sign out
                }
            }
            .background(Color.white.opacity(0.8))
            .cornerRadius(16)
            .shadow(color: .black.opacity(0.05), radius: 8, y: 2)
            .padding(.horizontal)
        }
    }
}

// MARK: - Supporting Views

struct StatCard: View {
    let icon: String
    let value: String
    let label: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)
            
            Text(value)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(.primary)
            
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(color.opacity(0.1))
        .cornerRadius(12)
    }
}

struct SettingsRow: View {
    let icon: String
    let title: String
    let subtitle: String
    var isDestructive: Bool = false
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(isDestructive ? .red : .oceanBlue)
                    .frame(width: 24)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(isDestructive ? .red : .primary)
                    
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal)
            .padding(.vertical, 12)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Placeholder Views

struct EditProfileView: View {
    @Binding var profile: UserProfile
    @Environment(\.dismiss) private var dismiss
    
    @State private var editedName: String = ""
    @State private var editedMobile: String = ""
    @State private var editedAddress: String = ""
    @State private var editedPincode: String = ""
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Profile Image Section
                    VStack(spacing: 12) {
                        ZStack {
                            Circle()
                                .fill(Color.oceanBlue.opacity(0.1))
                                .frame(width: 100, height: 100)
                            
                            Image(systemName: "person.fill")
                                .font(.system(size: 40))
                                .foregroundColor(.oceanBlue)
                        }
                        .overlay(
                            Circle()
                                .stroke(Color.oceanBlue, lineWidth: 2)
                        )
                        
                        Button("Change Photo") {
                            // Handle photo change
                        }
                        .font(.caption)
                        .foregroundColor(.oceanBlue)
                    }
                    .padding(.top, 20)
                    
                    // Edit Form
                    VStack(spacing: 20) {
                        // Name Field
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Full Name")
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.primary)
                            
                            TextField("Enter your name", text: $editedName)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .font(.body)
                        }
                        
                        // Mobile Field
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Mobile Number")
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.primary)
                            
                            TextField("Enter your mobile number", text: $editedMobile)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .keyboardType(.phonePad)
                                .font(.body)
                        }
                        
                        // Address Field
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Address")
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.primary)
                            
                            TextField("Enter your address", text: $editedAddress, axis: .vertical)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .lineLimit(3...5)
                                .font(.body)
                        }
                        
                        // Pincode Field
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Pincode")
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.primary)
                            
                            TextField("Enter pincode", text: $editedPincode)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .keyboardType(.numberPad)
                                .font(.body)
                        }
                    }
                    .padding(.horizontal, 20)
                }
                .padding(.bottom, 40)
            }
            .navigationTitle("Edit Profile")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        saveProfile()
                        dismiss()
                    }
                    .fontWeight(.semibold)
                }
            }
            .onAppear {
                loadCurrentProfile()
            }
        }
    }
    
    private func loadCurrentProfile() {
        editedName = profile.fullName
        editedMobile = profile.mobile
        editedAddress = profile.address
        editedPincode = profile.pincode
    }
    
    private func saveProfile() {
        profile.fullName = editedName
        profile.mobile = editedMobile
        profile.address = editedAddress
        profile.pincode = editedPincode
    }
}

struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationView {
            VStack {
                Text("Settings")
                    .font(.title)
                Spacer()
                Text("Settings functionality coming soon...")
                    .foregroundColor(.secondary)
                Spacer()
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}

// MARK: - Data Model

#Preview {
    NavigationStack {
        ProfileView()
            .environmentObject(TankManager())
            .environmentObject(UserProfileManager())
    }
}