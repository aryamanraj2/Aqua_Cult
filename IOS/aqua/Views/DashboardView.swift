//
//  DashboardView.swift
//  aqua
//
//  Created by aryaman jaiswal on 31/10/25.
//

import SwiftUI

struct DashboardView: View {
    @EnvironmentObject var tankManager: TankManager
    @EnvironmentObject var cartManager: CartManager
    @State private var environmentalData = EnvironmentalData.sample
    @State private var showingAddTank = false
    @State private var showingWeatherDetails = false
    @State private var showingVoiceBot = false
    @Namespace private var namespace
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Quick Stats Section
                    HStack(spacing: 16) {
                        QuickStatCard(
                            icon: "fish.fill",
                            value: "\(tankManager.tankCount)",
                            label: "Active Tanks",
                            color: .oceanBlue
                        )
                        
                        QuickStatCard(
                            icon: "drop.fill",
                            value: String(format: "%.1f", tankManager.totalVolume) + "m³",
                            label: "Total Volume",
                            color: .oceanBlue
                        )
                        
                        QuickStatCard(
                            icon: "thermometer.medium",
                            value: "\(Int(environmentalData.airTemperature))°C",
                            label: "Avg Temp",
                            color: .oceanBlue
                        )
                    }
                    .padding(.horizontal)
                    
                    // Environmental Conditions Card (tappable)
                    Button {
                        showingWeatherDetails = true
                    } label: {
                        EnvironmentalCard(data: environmentalData)
                    }
                    .buttonStyle(PlainButtonStyle())
                    .padding(.horizontal)
                    
                    // Water Quality Metrics Grid
                    WaterQualityMetricsView()
                        .padding(.horizontal)
                    
                    // My Tanks Section
                    VStack(spacing: 16) {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("My Tanks")
                                    .font(.system(size: 28, weight: .bold))
                                    .foregroundColor(.deepOcean)
                                Text("Monitor your aquaculture systems")
                                    .font(.system(size: 16))
                                    .foregroundColor(.mediumGray)
                            }
                            Spacer()
                        }
                        .padding(.horizontal)
                        
                        // Tank Cards
                        ForEach(tankManager.tanks) { tank in
                            NavigationLink(destination: TankView(tank: tank)) {
                                TankCard(tank: tank)
                                    .padding(.horizontal)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                }
                .padding(.vertical, 8)
            }
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [Color.subtleBlueLight, Color.subtleBlueMid]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            // MARK: - Code responsible for the "Aqua-Sense" header behavior
            .navigationTitle("Aqua-Sense") // Sets the title text
            .navigationBarTitleDisplayMode(.large) // Enables the large title that collapses on scroll
            .toolbar {
                ToolbarItemGroup(placement: .topBarTrailing) {
                    HStack(spacing: 8) {
                        Button {
                            showingVoiceBot = true
                        } label: {
                            Image(systemName: "mic.circle.fill")
                                .font(.title3)
                                .foregroundColor(.oceanBlue)
                        }
                        .matchedTransitionSource(id: "voiceBotTransition", in: namespace)
                        
                        Button {
                            showingAddTank = true
                        } label: {
                            Image(systemName: "plus.circle.fill")
                                .font(.title3)
                                .foregroundColor(.oceanBlue)
                        }
                        .matchedTransitionSource(id: "newTankTransition", in: namespace)
                    }
                }
            }
            .sheet(isPresented: $showingAddTank) {
                NavigationStack {
                    AddTankView(tanks: $tankManager.tanks)
                }
                .presentationDetents([.large])
                .presentationDragIndicator(.visible)
                .navigationTransition(.zoom(sourceID: "newTankTransition", in: namespace))
            }
            .sheet(isPresented: $showingVoiceBot) {
                VoiceBotView()
                    .environmentObject(tankManager)
                    .environmentObject(cartManager)
                    .presentationDetents([.large])
                    .presentationDragIndicator(.visible)
                    .navigationTransition(.zoom(sourceID: "voiceBotTransition", in: namespace))
            }
            .sheet(isPresented: $showingWeatherDetails) {
                WeatherDetailsView(environmentalData: environmentalData)
            }
        }
    }
}

// MARK: - Quick Stat Card
struct QuickStatCard: View {
    let icon: String
    let value: String
    let label: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(color)
            
            Text(value)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.deepOcean)
            
            Text(label)
                .font(.system(size: 12))
                .foregroundColor(.mediumGray)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .padding(.horizontal, 12)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(
                    LinearGradient(
                        gradient: Gradient(colors: [Color.white, Color.subtleBlueLight.opacity(0.3)]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .shadow(color: Color.oceanBlue.opacity(0.08), radius: 10, x: 0, y: 5)
        )
    }
}

// MARK: - Environmental Card
struct EnvironmentalCard: View {
    let data: EnvironmentalData
    
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 20)
                .fill(
                    LinearGradient(
                        gradient: Gradient(colors: [Color.white, Color.subtleBlueLight.opacity(0.4)]),
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .shadow(color: Color.oceanBlue.opacity(0.08), radius: 10, x: 0, y: 5)

            HStack(spacing: 16) {
                // Temperature and condition
                HStack(spacing: 10) {
                    Image(systemName: data.icon)
                        .font(.system(size: 36))
                        .foregroundColor(.oceanBlue)
                    
                    VStack(alignment: .leading, spacing: 0) {
                        Text("\(Int(data.airTemperature))°")
                            .font(.system(size: 48, weight: .bold))
                            .foregroundColor(.deepOcean)
                        Text(data.condition)
                            .font(.system(size: 13))
                            .foregroundColor(.mediumGray)
                    }
                }
                
                Spacer()
                
                // Environmental metrics - compact
                HStack(spacing: 20) {
                    MetricColumn(icon: "humidity.fill", value: "\(Int(data.humidity))%", label: "Humidity")
                    MetricColumn(icon: "drop.fill", value: "\(Int(data.precipitation))%", label: "Rain")
                    
                    Image(systemName: "chevron.right")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.mediumGray)
                }
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 16)
        }
        .frame(height: 100)
    }
}

struct MetricColumn: View {
    let icon: String
    let value: String
    let label: String
    
    var body: some View {
        VStack(spacing: 2) {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundColor(.oceanBlue)
            Text(value)
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(.deepOcean)
            Text(label)
                .font(.system(size: 11))
                .foregroundColor(.mediumGray)
        }
    }
}

// MARK: - Water Quality Metrics View
struct WaterQualityMetricsView: View {
    let metrics = [
        ("drop.fill", "pH Level", "7.5", "Optimal", Color.aquaGreen),
        ("thermometer.medium", "Water Temp", "16.2°C", "Optimal", Color.aquaGreen),
        ("bubbles.and.sparkles.fill", "Dissolved O₂", "7.2 mg/L", "On Track", Color.aquaYellow),
        ("leaf.fill", "Ammonia", "0.06 mg/L", "Normal", Color.oceanBlue),
        ("water.waves", "Salinity", "32.5 ppt", "Consider", Color.aquaYellow),
        ("eye.fill", "Turbidity", "3.1 NTU", "Caution", Color.aquaYellow)
    ]
    
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 20)
                .fill(
                    LinearGradient(
                        gradient: Gradient(colors: [Color.white, Color.subtleBlueLight.opacity(0.5)]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .shadow(color: Color.oceanBlue.opacity(0.08), radius: 10, x: 0, y: 5)

            VStack(alignment: .leading, spacing: 16) {
                Text("Tank Conditions")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.deepOcean)
                
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    ForEach(metrics, id: \.1) { metric in
                        WaterMetricCard(
                            icon: metric.0,
                            title: metric.1,
                            value: metric.2,
                            status: metric.3,
                            statusColor: metric.4
                        )
                    }
                }
            }
            .padding(20)
        }
    }
}

struct WaterMetricCard: View {
    let icon: String
    let title: String
    let value: String
    let status: String
    let statusColor: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: icon)
                    .font(.system(size: 16))
                    .foregroundColor(statusColor)
                Spacer()
                Text(status)
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(statusColor)
            }
            
            Text(title)
                .font(.system(size: 13))
                .foregroundColor(.deepOcean.opacity(0.8))
            
            Text(value)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.deepOcean)
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(height: 100)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(
                    LinearGradient(
                        gradient: Gradient(colors: [
                            statusColor.opacity(0.12),
                            statusColor.opacity(0.18).opacity(0.8)
                        ]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
        )
    }
}

// MARK: - Tank Card
struct TankCard: View {
    let tank: Tank
    
    var body: some View {
        ZStack {
            Group {
                if let imageName = tank.imageName {
                    Image(imageName)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } else {
                    // Fallback color if no image is provided
                    Color.deepOcean
                }
            }
            .frame(height: 240)
            .clipped()
            
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.clear,
                    Color.black.opacity(0.3),
                    Color.black.opacity(0.7)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            
            VStack {
                // AI Analytics Button - Top Right
                HStack {
                    Spacer()
                    AIAnalyticsButton(tank: tank)
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
                
                Spacer()
                
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(tank.species.joined(separator: ", "))
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                            
                            Text("\(tank.dimensions.volume, specifier: "%.1f")m³")
                                .font(.subheadline)
                                .foregroundColor(.white.opacity(0.9))
                        }
                        
                        Spacer()
                        
                        VStack(alignment: .trailing, spacing: 4) {
                            Text("Current Stage")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.8))
                            
                            Text(tank.currentStage)
                                .font(.caption)
                                .fontWeight(.medium)
                                .foregroundColor(.white)
                                .multilineTextAlignment(.trailing)
                        }
                    }
                }
                .padding(20)
            }
        }
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.15), radius: 8, x: 0, y: 4)
    }
}

// Helper for corner radius
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners
    
    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}

#Preview {
    DashboardView()
        .environmentObject(CartManager())
}
