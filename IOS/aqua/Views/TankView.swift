
import SwiftUI

struct TankView: View {
    let tank: Tank
    @State private var environmentalData = EnvironmentalData.sample
    @State private var scrollOffset: CGFloat = 0.0
    @State private var showingWeatherDetails = false
    
    let stages = ["Broodstock", "Hatchery", "Nursery", "Grow-out", "Harvesting"]
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading) {
                // Sticky Header with Tank Image
                ZStack {
                    Group {
                        if let imageName = tank.imageName {
                            Image(imageName)
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        } else {
                            // Fallback gradient if no image is provided
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    Color.oceanBlue,
                                    Color.deepOcean
                                ]),
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        }
                    }
                    .frame(height: 350 + (scrollOffset < 0 ? abs(scrollOffset / 2) : 0))
                    .blur(radius: scrollOffset < 0 ? abs(scrollOffset) / 50 : 0)
                    .clipped()
                    
                    // Dark overlay gradient
                    LinearGradient(
                        colors: [.black.opacity(0.1), .black.opacity(0.6)],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                    .frame(height: 350 + (scrollOffset < 0 ? abs(scrollOffset / 2) : 0))
                    
                    // Header content
                    VStack(spacing: 16) {
                        Spacer()
                        
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(tank.species.joined(separator: ", "))
                                    .font(.system(size: 32, weight: .bold))
                                    .foregroundColor(.white)
                                Text("\(tank.dimensions.volume, specifier: "%.1f")m³ • \(tank.name)")
                                    .font(.system(size: 16))
                                    .foregroundColor(.white.opacity(0.9))
                            }
                            Spacer()
                            
                            // AI Analytics Button
                            AIAnalyticsButton(tank: tank)
                        }
                        .padding(.horizontal)
                        
                        // Progress Bar with Stages in header
                        StageProgressView(stages: stages, currentStage: tank.currentStage)
                            .padding(.horizontal)
                        
                        Spacer().frame(height: 20)
                    }
                }
                
                // Main content
                TankDetailsView
            }
            .offset(y: scrollOffset > 0 ? 0 : scrollOffset)
        }
        .onScrollGeometryChange(for: CGFloat.self, of: { geometry in
            geometry.contentOffset.y
        }, action: { _, newValue in
            scrollOffset = newValue
        })
        .ignoresSafeArea()
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showingWeatherDetails) {
            WeatherDetailsView(environmentalData: environmentalData)
        }
    }
    
    var TankDetailsView: some View {
        VStack(spacing: 20) {
            // Environmental Conditions Card
            Button {
                showingWeatherDetails = true
            } label: {
                EnvironmentalCard(data: environmentalData)
            }
            .buttonStyle(PlainButtonStyle())
            .padding(.horizontal)
            
            // Tank Conditions Section
            TankConditionsView(tank: tank)
                .padding(.horizontal)
            
            Spacer(minLength: 100)
        }
        .padding(.top, 20)
        .background(Color(hex: "#f7f7f7"))
    }
}

// MARK: - Stage Progress View
struct StageProgressView: View {
    let stages: [String]
    let currentStage: String
    
    private var currentStageIndex: Int {
        stages.firstIndex(where: { $0.lowercased() == currentStage.lowercased() }) ?? 0
    }
    
    var body: some View {
        VStack(spacing: 12) {
            // Progress bar with dots and lines
            HStack(spacing: 0) {
                ForEach(Array(stages.enumerated()), id: \.offset) { index, stage in
                    HStack(spacing: 0) {
                        Circle()
                            .fill(stageColor(for: index))
                            .frame(width: 20, height: 20)
                            .overlay(
                                Circle()
                                    .stroke(Color.white, lineWidth: 2)
                            )
                        
                        if index < stages.count - 1 {
                            Rectangle()
                                .fill(stageLineColor(for: index))
                                .frame(height: 2)
                                .frame(maxWidth: .infinity)
                        }
                    }
                }
            }
            
            // Stage labels
            HStack {
                ForEach(Array(stages.enumerated()), id: \.offset) { index, stage in
                    Text(stage)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(index <= currentStageIndex ? .deepOcean : .mediumGray)
                        .multilineTextAlignment(.center)
                    
                    if index < stages.count - 1 {
                        Spacer()
                    }
                }
            }
            .padding(.top, 8)
        }
        .padding(.vertical, 16)
        .padding(.horizontal, 20)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.white)
                .shadow(color: Color.black.opacity(0.05), radius: 8, x: 0, y: 4)
        )
    }
    
    private func stageColor(for index: Int) -> Color {
        return index <= currentStageIndex ? Color.oceanBlue : Color.gray.opacity(0.3)
    }
    
    private func stageLineColor(for index: Int) -> Color {
        return index < currentStageIndex ? Color.oceanBlue : Color.gray.opacity(0.3)
    }
    
    private var progressWidth: CGFloat {
        let totalStages = stages.count - 1
        if totalStages == 0 { return 0 }
        return CGFloat(currentStageIndex) / CGFloat(totalStages)
    }
}

// MARK: - Tank Conditions View
struct TankConditionsView: View {
    let tank: Tank
    
    var tankMetrics: [(String, String, String, String, Color)] {
        [
            ("drop.fill", "pH Level", String(format: "%.1f", tank.waterQuality.pH), tank.waterQuality.status.rawValue, tank.waterQuality.status.color),
            ("thermometer.medium", "Water Temp", String(format: "%.1f°C", tank.waterQuality.temperature), tank.waterQuality.status.rawValue, tank.waterQuality.status.color),
            ("bubbles.and.sparkles.fill", "Dissolved O₂", String(format: "%.1f mg/L", tank.waterQuality.dissolvedOxygen), tank.waterQuality.status.rawValue, tank.waterQuality.status.color),
            ("leaf.fill", "Ammonia", String(format: "%.2f mg/L", tank.waterQuality.ammonia), tank.waterQuality.status.rawValue, tank.waterQuality.status.color),
            ("water.waves", "Salinity", String(format: "%.1f ppt", tank.waterQuality.salinity), tank.waterQuality.status.rawValue, tank.waterQuality.status.color),
            ("eye.fill", "Turbidity", String(format: "%.1f NTU", tank.waterQuality.turbidity), tank.waterQuality.status.rawValue, tank.waterQuality.status.color)
        ]
    }
    
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.white)
                .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 5)

            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Text("Tank Conditions")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.deepOcean)
                    Spacer()
                    Text(tank.name)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.mediumGray)
                }
                
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    ForEach(tankMetrics, id: \.1) { metric in
                        TankMetricCard(
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

struct TankMetricCard: View {
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
                .fill(statusColor.opacity(0.15))
        )
    }
}

#Preview {
    TankView(tank: Tank.sampleTanks[0])
}
