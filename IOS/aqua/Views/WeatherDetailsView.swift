
import SwiftUI

struct WeatherDetailsView: View {
    let environmentalData: EnvironmentalData
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Main Weather Card
                    DetailedEnvironmentalCard(environmentalData: environmentalData)
                    
                    // Aquaculture Weather Metrics Grid
                    AquacultureWeatherMetricsGrid(environmentalData: environmentalData)
                    
                    // Water Surface Conditions
                    WaterSurfaceConditionsCard(environmentalData: environmentalData)
                    
                    // Daily Impact on Aquaculture
                    AquacultureImpactSection(environmentalData: environmentalData)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }
            .background(Color(hex: "#f7f7f7").ignoresSafeArea())
            .navigationTitle("Weather Details")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                    .foregroundColor(.oceanBlue)
                }
            }
        }
    }
}

struct DetailedEnvironmentalCard: View {
    let environmentalData: EnvironmentalData
    
    var body: some View {
        VStack(spacing: 16) {
            HStack {
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 12) {
                        Image(systemName: environmentalData.icon)
                            .font(.largeTitle)
                            .foregroundColor(.oceanBlue)
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text("\(Int(environmentalData.airTemperature.rounded()))°C")
                                .font(.largeTitle)
                                .fontWeight(.bold)
                                .foregroundColor(.deepOcean)
                            
                            Text(environmentalData.condition)
                                .font(.subheadline)
                                .foregroundColor(.mediumGray)
                        }
                    }
                    
                    Text("Optimal for aquaculture: \(getTemperatureStatus())")
                        .font(.subheadline)
                        .foregroundColor(.mediumGray)
                }
                
                Spacer()
                
                VStack(alignment: .trailing, spacing: 8) {
                    Text("Today")
                        .font(.caption)
                        .foregroundColor(.mediumGray)
                    
                    VStack(alignment: .trailing, spacing: 4) {
                        Text("Wind: \(Int(environmentalData.windSpeed))km/h")
                            .font(.caption)
                            .fontWeight(.medium)
                            .foregroundColor(.deepOcean)
                        
                        Text("Humidity: \(Int(environmentalData.humidity))%")
                            .font(.caption)
                            .fontWeight(.medium)
                            .foregroundColor(.deepOcean)
                    }
                }
            }
        }
        .padding(20)
        .background(
            RoundedRectangle(cornerRadius: 22)
                .fill(Color.white)
                .shadow(color: Color.black.opacity(0.05), radius: 6)
        )
    }
    
    private func getTemperatureStatus() -> String {
        let temp = environmentalData.airTemperature
        switch temp {
        case 15...25: return "Excellent"
        case 10...30: return "Good"
        case 5...35: return "Fair"
        default: return "Challenging"
        }
    }
}

struct AquacultureWeatherMetricsGrid: View {
    let environmentalData: EnvironmentalData
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Aquaculture Conditions")
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(.deepOcean)
            
            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 2), spacing: 16) {
                AquacultureMetricCard(
                    icon: "humidity.fill",
                    title: "Humidity",
                    value: "\(Int(environmentalData.humidity.rounded()))%",
                    subtitle: getHumidityImpact(),
                    color: getHumidityColor()
                )
                
                AquacultureMetricCard(
                    icon: "wind",
                    title: "Wind Speed",
                    value: "\(Int(environmentalData.windSpeed.rounded())) km/h",
                    subtitle: getWindImpact(),
                    color: getWindColor()
                )
                
                AquacultureMetricCard(
                    icon: "drop.fill",
                    title: "Precipitation",
                    value: "\(Int(environmentalData.precipitation.rounded()))%",
                    subtitle: getRainImpact(),
                    color: getRainColor()
                )
                
                AquacultureMetricCard(
                    icon: "thermometer.medium",
                    title: "Air Temperature",
                    value: "\(Int(environmentalData.airTemperature.rounded()))°C",
                    subtitle: getTemperatureImpact(),
                    color: getTemperatureColor()
                )
                
                AquacultureMetricCard(
                    icon: "water.waves",
                    title: "Water Exchange",
                    value: getWaterExchangeRate(),
                    subtitle: "Based on conditions",
                    color: .oceanBlue
                )
                
                AquacultureMetricCard(
                    icon: "fish.fill",
                    title: "Fish Stress",
                    value: getFishStressLevel(),
                    subtitle: "Weather impact",
                    color: getFishStressColor()
                )
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 22)
                .fill(Color.white)
                .shadow(color: Color.black.opacity(0.05), radius: 6)
        )
    }
    
    private func getHumidityImpact() -> String {
        let humidity = environmentalData.humidity
        switch humidity {
        case 60...80: return "Optimal"
        case 50...90: return "Good"
        default: return "Monitor"
        }
    }
    
    private func getWindImpact() -> String {
        let wind = environmentalData.windSpeed
        switch wind {
        case 0...15: return "Calm conditions"
        case 15...30: return "Moderate mixing"
        default: return "High turbulence"
        }
    }
    
    private func getRainImpact() -> String {
        let rain = environmentalData.precipitation
        switch rain {
        case 0...30: return "Low dilution"
        case 30...70: return "Moderate impact"
        default: return "High dilution"
        }
    }
    
    private func getTemperatureImpact() -> String {
        let temp = environmentalData.airTemperature
        switch temp {
        case 15...25: return "Ideal range"
        case 10...30: return "Acceptable"
        default: return "Stressful"
        }
    }
    
    private func getWaterExchangeRate() -> String {
        let wind = environmentalData.windSpeed
        let rain = environmentalData.precipitation
        let combined = (wind + rain) / 2
        
        switch combined {
        case 0...25: return "Low"
        case 25...50: return "Moderate"
        default: return "High"
        }
    }
    
    private func getFishStressLevel() -> String {
        let temp = environmentalData.airTemperature
        let wind = environmentalData.windSpeed
        
        let stressScore = abs(temp - 20) + (wind > 25 ? 10 : 0)
        
        switch stressScore {
        case 0...5: return "Low"
        case 5...15: return "Moderate"
        default: return "High"
        }
    }
    
    private func getHumidityColor() -> Color {
        let humidity = environmentalData.humidity
        return (60...80).contains(humidity) ? .aquaGreen : .aquaYellow
    }
    
    private func getWindColor() -> Color {
        return environmentalData.windSpeed <= 25 ? .aquaGreen : .aquaYellow
    }
    
    private func getRainColor() -> Color {
        return environmentalData.precipitation <= 50 ? .aquaGreen : .aquaYellow
    }
    
    private func getTemperatureColor() -> Color {
        let temp = environmentalData.airTemperature
        return (15...25).contains(temp) ? .aquaGreen : .aquaYellow
    }
    
    private func getFishStressColor() -> Color {
        let temp = environmentalData.airTemperature
        let wind = environmentalData.windSpeed
        let stressScore = abs(temp - 20) + (wind > 25 ? 10 : 0)
        
        switch stressScore {
        case 0...5: return .aquaGreen
        case 5...15: return .aquaYellow
        default: return .aquaRed
        }
    }
}

struct AquacultureMetricCard: View {
    let icon: String
    let title: String
    let value: String
    let subtitle: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(color)
                
                Spacer()
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.caption)
                    .foregroundColor(.mediumGray)
                
                Text(value)
                    .font(.title3)
                    .fontWeight(.semibold)
                    .foregroundColor(.deepOcean)
                
                Text(subtitle)
                    .font(.caption2)
                    .foregroundColor(.mediumGray)
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color(hex: "#f7f7f7"))
        )
    }
}

struct WaterSurfaceConditionsCard: View {
    let environmentalData: EnvironmentalData
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Water Surface Impact")
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(.deepOcean)
            
            HStack(spacing: 20) {
                VStack(spacing: 12) {
                    SurfaceConditionItem(
                        icon: "water.waves",
                        label: "Surface Mixing",
                        value: getSurfaceMixing(),
                        color: .oceanBlue
                    )
                    
                    SurfaceConditionItem(
                        icon: "thermometer.sun.fill",
                        label: "Heat Exchange",
                        value: getHeatExchange(),
                        color: .aquaYellow
                    )
                }
                
                Spacer()
                
                VStack(alignment: .trailing, spacing: 12) {
                    VStack(alignment: .trailing, spacing: 4) {
                        Text("Oxygen Transfer")
                            .font(.caption)
                            .foregroundColor(.mediumGray)
                        
                        Text(getOxygenTransfer())
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(.deepOcean)
                    }
                    
                    VStack(alignment: .trailing, spacing: 4) {
                        Text("Water Quality")
                            .font(.caption)
                            .foregroundColor(.mediumGray)
                        
                        Text(getWaterQualityImpact())
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(.deepOcean)
                    }
                }
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 22)
                .fill(Color.white)
                .shadow(color: Color.black.opacity(0.05), radius: 6)
        )
    }
    
    private func getSurfaceMixing() -> String {
        return environmentalData.windSpeed > 15 ? "Active" : "Gentle"
    }
    
    private func getHeatExchange() -> String {
        let temp = environmentalData.airTemperature
        return temp > 25 ? "Warming" : temp < 15 ? "Cooling" : "Stable"
    }
    
    private func getOxygenTransfer() -> String {
        return environmentalData.windSpeed > 10 ? "Enhanced" : "Normal"
    }
    
    private func getWaterQualityImpact() -> String {
        let rain = environmentalData.precipitation
        return rain > 50 ? "Dilution" : "Stable"
    }
}

struct SurfaceConditionItem: View {
    let icon: String
    let label: String
    let value: String
    let color: Color
    
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(color)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(.mediumGray)
                
                Text(value)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(.deepOcean)
            }
        }
    }
}

struct AquacultureImpactSection: View {
    let environmentalData: EnvironmentalData
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Daily Aquaculture Impact")
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(.deepOcean)
            
            VStack(spacing: 12) {
                AquacultureImpactItem(
                    time: "Morning",
                    condition: "Feeding optimal",
                    description: "Stable conditions for feeding",
                    icon: "sunrise.fill",
                    color: .aquaGreen
                )
                
                AquacultureImpactItem(
                    time: "Midday",
                    condition: getMiddayCondition(),
                    description: getMiddayDescription(),
                    icon: "sun.max.fill",
                    color: getMiddayColor()
                )
                
                AquacultureImpactItem(
                    time: "Evening",
                    condition: "Monitor closely",
                    description: "Check oxygen levels",
                    icon: "sunset.fill",
                    color: .aquaYellow
                )
                
                AquacultureImpactItem(
                    time: "Night",
                    condition: "Stable period",
                    description: "Low activity expected",
                    icon: "moon.fill",
                    color: .oceanBlue
                )
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 22)
                .fill(Color.white)
                .shadow(color: Color.black.opacity(0.05), radius: 6)
        )
    }
    
    private func getMiddayCondition() -> String {
        let temp = environmentalData.airTemperature
        return temp > 30 ? "Heat stress risk" : "Good conditions"
    }
    
    private func getMiddayDescription() -> String {
        let temp = environmentalData.airTemperature
        return temp > 30 ? "Increase aeration" : "Normal operations"
    }
    
    private func getMiddayColor() -> Color {
        return environmentalData.airTemperature > 30 ? .aquaRed : .aquaGreen
    }
}

struct AquacultureImpactItem: View {
    let time: String
    let condition: String
    let description: String
    let icon: String
    let color: Color
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)
                .frame(width: 30)
            
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(time)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.deepOcean)
                    
                    Spacer()
                    
                    Text(condition)
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(color)
                }
                
                Text(description)
                    .font(.caption)
                    .foregroundColor(.mediumGray)
            }
        }
        .padding(.vertical, 8)
    }
}

#Preview {
    WeatherDetailsView(environmentalData: EnvironmentalData.sample)
}