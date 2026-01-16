
import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var tankManager: TankManager
    @EnvironmentObject var profileManager: UserProfileManager
    @ObservedObject private var localizationManager = LocalizationManager.shared
    @State private var showingSettings = false
    @State private var showingEditProfile = false
    @State private var showingLanguagePicker = false

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
        .navigationTitle(localizationManager.localizedString(for: "profile"))
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    showingEditProfile = true
                } label: {
                    Text(localizationManager.localizedString(for: "edit"))
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
        .sheet(isPresented: $showingLanguagePicker) {
            LanguagePickerView()
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

                Text("\(localizationManager.localizedString(for: "pincode")): \(profileManager.profile.pincode)")
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
            Text(localizationManager.localizedString(for: "my_aquaculture"))
                .font(.headline)
                .fontWeight(.semibold)
                .padding(.horizontal)

            HStack(spacing: 16) {
                StatCard(
                    icon: "fish.fill",
                    value: "\(tankManager.tankCount)",
                    label: localizationManager.localizedString(for: "active_tanks"),
                    color: .oceanBlue
                )

                StatCard(
                    icon: "drop.fill",
                    value: String(format: "%.1f", tankManager.totalVolume) + "m³",
                    label: localizationManager.localizedString(for: "total_volume"),
                    color: .blue
                )

                StatCard(
                    icon: "calendar",
                    value: "\(profileManager.profile.experienceYears)",
                    label: localizationManager.localizedString(for: "years_experience"),
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
            Text(localizationManager.localizedString(for: "settings_info"))
                .font(.headline)
                .fontWeight(.semibold)
                .padding(.horizontal)

            VStack(spacing: 0) {
                // Language Selection Row
                LanguageSettingsRow(
                    icon: "globe",
                    title: localizationManager.localizedString(for: "language"),
                    currentLanguage: localizationManager.currentLanguage
                ) {
                    showingLanguagePicker = true
                }

                Divider()
                    .padding(.leading, 60)

                SettingsRow(
                    icon: "gear.circle.fill",
                    title: localizationManager.localizedString(for: "app_settings"),
                    subtitle: localizationManager.localizedString(for: "preferences_config")
                ) {
                    showingSettings = true
                }

                Divider()
                    .padding(.leading, 60)

                SettingsRow(
                    icon: "questionmark.circle.fill",
                    title: localizationManager.localizedString(for: "help_support"),
                    subtitle: localizationManager.localizedString(for: "faqs_contact")
                ) {
                    // Handle help action
                }

                Divider()
                    .padding(.leading, 60)

                SettingsRow(
                    icon: "info.circle.fill",
                    title: localizationManager.localizedString(for: "about_aqua"),
                    subtitle: "Version \(profileManager.profile.appVersion)"
                ) {
                    // Handle about action
                }

                Divider()
                    .padding(.leading, 60)

                SettingsRow(
                    icon: "rectangle.portrait.and.arrow.right",
                    title: localizationManager.localizedString(for: "sign_out"),
                    subtitle: localizationManager.localizedString(for: "log_out_account"),
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
    @ObservedObject private var localizationManager = LocalizationManager.shared

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

                        Button(localizationManager.localizedString(for: "change_photo")) {
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
                            Text(localizationManager.localizedString(for: "full_name"))
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.primary)

                            TextField(localizationManager.localizedString(for: "enter_name"), text: $editedName)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .font(.body)
                        }

                        // Mobile Field
                        VStack(alignment: .leading, spacing: 8) {
                            Text(localizationManager.localizedString(for: "mobile_number"))
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.primary)

                            TextField(localizationManager.localizedString(for: "enter_mobile"), text: $editedMobile)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .keyboardType(.phonePad)
                                .font(.body)
                        }

                        // Address Field
                        VStack(alignment: .leading, spacing: 8) {
                            Text(localizationManager.localizedString(for: "address"))
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.primary)

                            TextField(localizationManager.localizedString(for: "enter_address"), text: $editedAddress, axis: .vertical)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .lineLimit(3...5)
                                .font(.body)
                        }

                        // Pincode Field
                        VStack(alignment: .leading, spacing: 8) {
                            Text(localizationManager.localizedString(for: "pincode"))
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.primary)

                            TextField(localizationManager.localizedString(for: "enter_pincode"), text: $editedPincode)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .keyboardType(.numberPad)
                                .font(.body)
                        }
                    }
                    .padding(.horizontal, 20)
                }
                .padding(.bottom, 40)
            }
            .navigationTitle(localizationManager.localizedString(for: "edit_profile"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(localizationManager.localizedString(for: "cancel")) {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(localizationManager.localizedString(for: "save")) {
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
    @ObservedObject private var localizationManager = LocalizationManager.shared

    var body: some View {
        NavigationView {
            VStack {
                Text(localizationManager.localizedString(for: "settings"))
                    .font(.title)
                Spacer()
                Text(localizationManager.localizedString(for: "settings_coming_soon"))
                    .foregroundColor(.secondary)
                Spacer()
            }
            .navigationTitle(localizationManager.localizedString(for: "settings"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(localizationManager.localizedString(for: "done")) {
                        dismiss()
                    }
                }
            }
        }
    }
}

// MARK: - Language Settings Row
struct LanguageSettingsRow: View {
    let icon: String
    let title: String
    let currentLanguage: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(.oceanBlue)
                    .frame(width: 24)

                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(.primary)

                    Text(currentLanguage)
                        .font(.caption)
                        .foregroundColor(.oceanBlue)
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

// MARK: - Language Picker View
struct LanguagePickerView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var localizationManager = LocalizationManager.shared

    let languages = [
        ("English", "English", "🇺🇸"),
        ("हिन्दी", "Hindi", "🇮🇳"),
        ("বাংলা", "Bengali", "🇧🇩")
    ]

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Header
                VStack(spacing: 8) {
                    Image(systemName: "globe")
                        .font(.system(size: 50))
                        .foregroundColor(.oceanBlue)
                        .padding(.top, 30)

                    Text(localizationManager.localizedString(for: "choose_language"))
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.deepOcean)

                    Text(localizationManager.localizedString(for: "select_preferred_language"))
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .padding(.bottom, 20)
                }

                // Language Options
                VStack(spacing: 12) {
                    ForEach(languages, id: \.0) { language in
                        LanguageOptionButton(
                            nativeName: language.0,
                            englishName: language.1,
                            flag: language.2,
                            isSelected: localizationManager.currentLanguage == language.0
                        ) {
                            withAnimation(.spring(response: 0.3)) {
                                localizationManager.setLanguage(language.0)
                            }
                            // Dismiss after a short delay to show the selection
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                                dismiss()
                            }
                        }
                    }
                }
                .padding(.horizontal, 24)

                Spacer()
            }
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [Color.subtleBlueLight, Color.white]),
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(localizationManager.localizedString(for: "done")) {
                        dismiss()
                    }
                }
            }
        }
    }
}

// MARK: - Language Option Button
struct LanguageOptionButton: View {
    let nativeName: String
    let englishName: String
    let flag: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Text(flag)
                    .font(.system(size: 32))

                VStack(alignment: .leading, spacing: 2) {
                    Text(nativeName)
                        .font(.headline)
                        .foregroundColor(isSelected ? .oceanBlue : .primary)

                    Text(englishName)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                Spacer()

                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.title2)
                        .foregroundColor(.oceanBlue)
                } else {
                    Circle()
                        .stroke(Color.gray.opacity(0.3), lineWidth: 2)
                        .frame(width: 24, height: 24)
                }
            }
            .padding(16)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(isSelected ? Color.oceanBlue.opacity(0.1) : Color.white)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(isSelected ? Color.oceanBlue : Color.gray.opacity(0.2), lineWidth: isSelected ? 2 : 1)
            )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Profile Sheet View (Apple-style bottom sheet drawer)
struct ProfileSheetView: View {
    @EnvironmentObject var tankManager: TankManager
    @EnvironmentObject var profileManager: UserProfileManager
    @ObservedObject private var localizationManager = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss
    @State private var showingSettings = false
    @State private var showingEditProfile = false
    @State private var showingLanguagePicker = false
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Compact Profile Header
                    compactProfileHeader
                    
                    // Quick Stats
                    quickStatsSection
                    
                    // Settings List
                    settingsListSection
                }
                .padding(.bottom, 40)
            }
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [Color.subtleBlueLight, Color.subtleBlueMid]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .navigationTitle(localizationManager.localizedString(for: "profile"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                            .foregroundStyle(.secondary)
                    }
                }
                
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showingEditProfile = true
                    } label: {
                        Text(localizationManager.localizedString(for: "edit"))
                            .fontWeight(.medium)
                            .foregroundColor(.oceanBlue)
                    }
                }
            }
        }
        .sheet(isPresented: $showingEditProfile) {
            EditProfileView(profile: $profileManager.profile)
        }
        .sheet(isPresented: $showingSettings) {
            SettingsView()
        }
        .sheet(isPresented: $showingLanguagePicker) {
            LanguagePickerView()
        }
    }
    
    // MARK: - Compact Profile Header
    private var compactProfileHeader: some View {
        HStack(spacing: 16) {
            // Profile Image
            ZStack {
                Circle()
                    .fill(Color.oceanBlue.opacity(0.1))
                    .frame(width: 70, height: 70)
                
                Image(systemName: "person.fill")
                    .font(.system(size: 28))
                    .foregroundColor(.oceanBlue)
            }
            .overlay(
                Circle()
                    .stroke(Color.oceanBlue, lineWidth: 2)
            )
            
            VStack(alignment: .leading, spacing: 4) {
                Text(profileManager.profile.fullName)
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(.primary)
                
                Text(profileManager.profile.mobile)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                
                Text(profileManager.profile.location)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.top, 16)
    }
    
    // MARK: - Quick Stats Section
    private var quickStatsSection: some View {
        HStack(spacing: 12) {
            SheetStatCard(
                icon: "fish.fill",
                value: "\(tankManager.tankCount)",
                label: localizationManager.localizedString(for: "active_tanks"),
                color: .oceanBlue
            )
            
            SheetStatCard(
                icon: "drop.fill",
                value: String(format: "%.1f", tankManager.totalVolume) + "m³",
                label: localizationManager.localizedString(for: "total_volume"),
                color: .blue
            )
            
            SheetStatCard(
                icon: "calendar",
                value: "\(profileManager.profile.experienceYears)",
                label: localizationManager.localizedString(for: "years_experience"),
                color: .green
            )
        }
        .padding(.horizontal, 20)
    }
    
    // MARK: - Settings List Section
    private var settingsListSection: some View {
        VStack(spacing: 0) {
            // Language Selection Row
            LanguageSettingsRow(
                icon: "globe",
                title: localizationManager.localizedString(for: "language"),
                currentLanguage: localizationManager.currentLanguage
            ) {
                showingLanguagePicker = true
            }
            
            Divider()
                .padding(.leading, 60)
            
            SettingsRow(
                icon: "gear.circle.fill",
                title: localizationManager.localizedString(for: "app_settings"),
                subtitle: localizationManager.localizedString(for: "preferences_config")
            ) {
                showingSettings = true
            }
            
            Divider()
                .padding(.leading, 60)
            
            SettingsRow(
                icon: "questionmark.circle.fill",
                title: localizationManager.localizedString(for: "help_support"),
                subtitle: localizationManager.localizedString(for: "faqs_contact")
            ) {
                // Handle help action
            }
            
            Divider()
                .padding(.leading, 60)
            
            SettingsRow(
                icon: "info.circle.fill",
                title: localizationManager.localizedString(for: "about_aqua"),
                subtitle: "Version \(profileManager.profile.appVersion)"
            ) {
                // Handle about action
            }
            
            Divider()
                .padding(.leading, 60)
            
            SettingsRow(
                icon: "rectangle.portrait.and.arrow.right",
                title: localizationManager.localizedString(for: "sign_out"),
                subtitle: localizationManager.localizedString(for: "log_out_account"),
                isDestructive: true
            ) {
                // Handle sign out
            }
        }
        .background(Color.white.opacity(0.8))
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 8, y: 2)
        .padding(.horizontal, 20)
    }
}

// MARK: - Sheet Stat Card (Compact for bottom sheet)
struct SheetStatCard: View {
    let icon: String
    let value: String
    let label: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(color)
            
            Text(value)
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.primary)
            
            Text(label)
                .font(.system(size: 10))
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .lineLimit(2)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(color.opacity(0.1))
        .cornerRadius(12)
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

#Preview("ProfileSheetView") {
    ProfileSheetView()
        .environmentObject(TankManager())
        .environmentObject(UserProfileManager())
}