//
//  AIAnalyticsView.swift
//  aqua
//
//  Created by AI Assistant on 01/11/25.
//

import SwiftUI

struct AIAnalyticsView: View {
    let tank: Tank
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var cartManager: CartManager
    @EnvironmentObject private var profileManager: UserProfileManager
    @StateObject private var ttsService = TTSSummaryService.shared
    @State private var analysis: TankAnalysis?
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showCart = false
    
    var body: some View {
        NavigationStack {
            ZStack {
                // Background gradient
                LinearGradient(
                    gradient: Gradient(colors: [Color.subtleBlueLight, Color.subtleBlueMid]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()
                
                if isLoading {
                    LoadingView()
                } else if let error = errorMessage {
                    ErrorView(message: error, retry: loadAnalysis)
                } else if let analysis = analysis {
                    AnalysisContentView(tank: tank, analysis: analysis)
                } else {
                    EmptyView()
                }
                
                // Floating cart button (replica of MarketplaceView)
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        if !cartManager.items.isEmpty {
                            floatingCartButton
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.bottom, 8)
            }
            .navigationTitle("AI Tank Analysis")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    if let analysis = analysis {
                        Button {
                            handleSpeakerButtonTap(analysis: analysis)
                        } label: {
                            Image(systemName: ttsService.isSpeaking ? (ttsService.isPaused ? "speaker.wave.1.fill" : "speaker.wave.3.fill") : "speaker.wave.2.fill")
                                .foregroundColor(ttsService.isSpeaking ? .oceanBlue : .mediumGray)
                                .font(.title3)
                                .symbolEffect(.bounce, value: ttsService.isSpeaking)
                        }
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        ttsService.stop()
                        dismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.mediumGray)
                    }
                }
            }
        }
        .sheet(isPresented: $showCart) {
            CartView(cartManager: cartManager, userProfile: profileManager.profile)
        }
        .onAppear {
            loadAnalysis()
        }
        .onDisappear {
            ttsService.stop()
        }
    }
    
    private var floatingCartButton: some View {
        Button {
            showCart = true
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
        }
    }
    
    private func loadAnalysis() {
        isLoading = true
        errorMessage = nil
        
        Task {
            do {
                let result = try await GeminiService.shared.analyzeTank(tank)
                await MainActor.run {
                    analysis = result
                    isLoading = false
                }
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                    isLoading = false
                }
            }
        }
    }
    
    private func handleSpeakerButtonTap(analysis: TankAnalysis) {
        if ttsService.isSpeaking {
            if ttsService.isPaused {
                ttsService.resume()
            } else {
                ttsService.pause()
            }
        } else {
            // Start speaking the summary
            ttsService.speak(analysis.spokenSummary)
        }
    }
}

// MARK: - Loading View
struct LoadingView: View {
    @State private var isAnimating = false
    
    var body: some View {
        VStack(spacing: 20) {
            ZStack {
                Circle()
                    .stroke(Color.oceanBlue.opacity(0.2), lineWidth: 8)
                    .frame(width: 80, height: 80)
                
                Circle()
                    .trim(from: 0, to: 0.7)
                    .stroke(Color.oceanBlue, lineWidth: 8)
                    .frame(width: 80, height: 80)
                    .rotationEffect(Angle(degrees: isAnimating ? 360 : 0))
                    .animation(.linear(duration: 1.5).repeatForever(autoreverses: false), value: isAnimating)
                
                Image(systemName: "brain.head.profile")
                    .font(.system(size: 30))
                    .foregroundColor(.oceanBlue)
            }
            
            Text("Analyzing Tank Conditions...")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.deepOcean)
            
            Text("AI is examining water quality, disease risks,\nand harvest potential")
                .font(.system(size: 14))
                .foregroundColor(.mediumGray)
                .multilineTextAlignment(.center)
        }
        .padding(40)
        .background(
            RoundedRectangle(cornerRadius: 24)
                .fill(Color.white)
                .shadow(color: Color.oceanBlue.opacity(0.1), radius: 20, x: 0, y: 10)
        )
        .padding()
        .onAppear {
            isAnimating = true
        }
    }
}

// MARK: - Error View
struct ErrorView: View {
    let message: String
    let retry: () -> Void
    
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 50))
                .foregroundColor(.aquaYellow)
            
            Text("Analysis Failed")
                .font(.system(size: 22, weight: .bold))
                .foregroundColor(.deepOcean)
            
            Text(message)
                .font(.system(size: 15))
                .foregroundColor(.mediumGray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            Button(action: retry) {
                HStack {
                    Image(systemName: "arrow.clockwise")
                    Text("Try Again")
                }
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(.white)
                .padding(.horizontal, 32)
                .padding(.vertical, 14)
                .background(
                    LinearGradient(
                        colors: [Color.oceanBlue, Color.mediumBlue],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .cornerRadius(12)
            }
        }
        .padding(40)
        .background(
            RoundedRectangle(cornerRadius: 24)
                .fill(Color.white)
                .shadow(color: Color.oceanBlue.opacity(0.1), radius: 20, x: 0, y: 10)
        )
        .padding()
    }
}

// MARK: - Main Analysis Content
struct AnalysisContentView: View {
    let tank: Tank
    let analysis: TankAnalysis
    @State private var expandedSections: Set<String> = []
    
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Hero Overview Card
                OverviewCard(overview: analysis.overview)
                
                // New Collapsible Sections
                CollapsibleAnalysisSection(
                    section: analysis.alerts,
                    icon: "exclamationmark.triangle.fill",
                    iconColor: .aquaRed,
                    expandedSections: $expandedSections
                )
                
                CollapsibleAnalysisSection(
                    section: analysis.monitor,
                    icon: "eye.fill",
                    iconColor: .aquaYellow,
                    expandedSections: $expandedSections
                )
                
                CollapsibleAnalysisSection(
                    section: analysis.good,
                    icon: "checkmark.circle.fill",
                    iconColor: .aquaGreen,
                    expandedSections: $expandedSections
                )
                
                // Recommended Products Section
                if !analysis.recommendedProducts.isEmpty {
                    CollapsibleProductSection(
                        products: analysis.recommendedProducts,
                        expandedSections: $expandedSections
                    )
                }
                
                // Legacy sections (hidden by default, for fallback)
                if expandedSections.contains("legacy") {
                    LegacySectionsView(analysis: analysis)
                }
            }
            .padding()
        }
    }
}

// MARK: - Collapsible Analysis Section
struct CollapsibleAnalysisSection: View {
    let section: AnalysisSection
    let icon: String
    let iconColor: Color
    @Binding var expandedSections: Set<String>
    
    private var isExpanded: Bool {
        expandedSections.contains(section.title)
    }
    
    var body: some View {
        if section.hasContent {
            VStack(spacing: 12) {
                Button {
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                        if isExpanded {
                            expandedSections.remove(section.title)
                        } else {
                            expandedSections.insert(section.title)
                        }
                    }
                } label: {
                    HStack {
                        Image(systemName: icon)
                            .font(.system(size: 18))
                            .foregroundColor(iconColor)
                        
                        Text(section.title)
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.deepOcean)
                        
                        Text("(\(section.items.count))")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.mediumGray)
                        
                        Spacer()
                        
                        Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(.oceanBlue)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 14)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color.white)
                            .shadow(color: iconColor.opacity(0.15), radius: 8, x: 0, y: 4)
                    )
                }
                .buttonStyle(PlainButtonStyle())
                
                if isExpanded {
                    VStack(spacing: 12) {
                        ForEach(section.items) { item in
                            AnalysisItemCard(item: item)
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Collapsible Product Section
struct CollapsibleProductSection: View {
    let products: [MarketplaceProduct]
    @Binding var expandedSections: Set<String>
    
    private let sectionTitle = "Recommended Products"
    private var isExpanded: Bool {
        expandedSections.contains(sectionTitle)
    }
    
    var body: some View {
        VStack(spacing: 12) {
            Button {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                    if isExpanded {
                        expandedSections.remove(sectionTitle)
                    } else {
                        expandedSections.insert(sectionTitle)
                    }
                }
            } label: {
                HStack {
                    Image(systemName: "cart.fill")
                        .font(.system(size: 18))
                        .foregroundColor(.oceanBlue)
                    
                    Text(sectionTitle)
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.deepOcean)
                    
                    Text("(\(products.count))")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.mediumGray)
                    
                    Spacer()
                    
                    Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.oceanBlue)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 14)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color.white)
                        .shadow(color: Color.oceanBlue.opacity(0.15), radius: 8, x: 0, y: 4)
                )
            }
            .buttonStyle(PlainButtonStyle())
            
            if isExpanded {
                VStack(spacing: 12) {
                    ForEach(products) { product in
                        ProductCard(product: product)
                    }
                }
            }
        }
    }
}

// MARK: - Analysis Item Card
struct AnalysisItemCard: View {
    let item: AnalysisItem
    @State private var isExpanded = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Button {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                    isExpanded.toggle()
                }
            } label: {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text(item.title)
                                .font(.system(size: 17, weight: .semibold))
                                .foregroundColor(.deepOcean)
                            
                            if let priority = item.priority {
                                Text(priority.rawValue.uppercased())
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(priority.color)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(
                                        Capsule()
                                            .fill(priority.color.opacity(0.15))
                                    )
                            }
                        }
                        
                        Text(item.description)
                            .font(.system(size: 14))
                            .foregroundColor(.deepOcean.opacity(0.8))
                            .lineLimit(isExpanded ? nil : 2)
                    }
                    
                    Spacer()
                    
                    Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.oceanBlue)
                }
            }
            .buttonStyle(PlainButtonStyle())
            
            if isExpanded {
                VStack(alignment: .leading, spacing: 12) {
                    // Metadata display
                    if let metadata = item.metadata, !metadata.isEmpty {
                        VStack(alignment: .leading, spacing: 6) {
                            ForEach(Array(metadata.keys.sorted()), id: \.self) { key in
                                if let value = metadata[key] {
                                    HStack {
                                        Text("\(key.capitalized):")
                                            .font(.system(size: 12, weight: .semibold))
                                            .foregroundColor(.mediumGray)
                                        Text(value)
                                            .font(.system(size: 12))
                                            .foregroundColor(.deepOcean)
                                        Spacer()
                                    }
                                }
                            }
                        }
                        .padding(10)
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.subtleBlueLight.opacity(0.3))
                        )
                    }
                    
                    // Details section
                    if !item.details.isEmpty {
                        DetailSection(title: "Details", items: item.details, color: .oceanBlue)
                    }
                    
                    // Action items section
                    if !item.actionItems.isEmpty {
                        DetailSection(title: "Actions", items: item.actionItems, color: .aquaGreen)
                    }
                }
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.white)
                .shadow(color: Color.oceanBlue.opacity(0.08), radius: 10, x: 0, y: 5)
        )
    }
}

// MARK: - Legacy Sections View
struct LegacySectionsView: View {
    let analysis: TankAnalysis
    
    var body: some View {
        VStack(spacing: 20) {
            // Disease Risks Section
            if !analysis.diseaseRisks.isEmpty {
                SectionHeader(title: "Disease Risk Assessment", icon: "cross.case.fill")
                ForEach(analysis.diseaseRisks) { risk in
                    DiseaseRiskCard(risk: risk)
                }
            }
            
            // Harvest Insights
            SectionHeader(title: "Harvest Insights", icon: "chart.line.uptrend.xyaxis")
            HarvestInsightsCard(insights: analysis.harvestInsights)
            
            // Water Concerns
            if !analysis.waterConcerns.isEmpty {
                SectionHeader(title: "Water Quality Concerns", icon: "drop.fill")
                ForEach(analysis.waterConcerns) { concern in
                    WaterConcernCard(concern: concern)
                }
            }
            
            // Recommendations
            if !analysis.recommendations.isEmpty {
                SectionHeader(title: "Expert Recommendations", icon: "lightbulb.fill")
                ForEach(analysis.recommendations) { recommendation in
                    RecommendationCard(recommendation: recommendation)
                }
            }
            
            // Legacy Products
            if !analysis.productsNeeded.isEmpty {
                SectionHeader(title: "Legacy Products", icon: "cart.fill")
                ForEach(analysis.productsNeeded) { product in
                    ProductCard(product: product)
                }
            }
        }
    }
}

// MARK: - Section Header
struct SectionHeader: View {
    let title: String
    let icon: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundColor(.oceanBlue)
            
            Text(title)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.deepOcean)
            
            Spacer()
        }
        .padding(.top, 8)
    }
}

// MARK: - Overview Card
struct OverviewCard: View {
    let overview: AnalysisOverview
    
    var body: some View {
        VStack(spacing: 16) {
            // Health Score Circle
            ZStack {
                Circle()
                    .stroke(Color.oceanBlue.opacity(0.2), lineWidth: 12)
                    .frame(width: 120, height: 120)
                
                Circle()
                    .trim(from: 0, to: CGFloat(overview.healthScore) / 100)
                    .stroke(
                        LinearGradient(
                            colors: [overview.status.statusColor, overview.status.statusColor.opacity(0.6)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        style: StrokeStyle(lineWidth: 12, lineCap: .round)
                    )
                    .frame(width: 120, height: 120)
                    .rotationEffect(Angle(degrees: -90))
                
                VStack(spacing: 4) {
                    Text("\(overview.healthScore)")
                        .font(.system(size: 36, weight: .bold))
                        .foregroundColor(.deepOcean)
                    Text("Health Score")
                        .font(.system(size: 12))
                        .foregroundColor(.mediumGray)
                }
            }
            
            Text(overview.status)
                .font(.system(size: 22, weight: .bold))
                .foregroundColor(overview.status.statusColor)
            
            Text(overview.summary)
                .font(.system(size: 15))
                .foregroundColor(.deepOcean.opacity(0.8))
                .multilineTextAlignment(.center)
                .lineSpacing(4)
            
            // Key Metrics
            HStack(spacing: 12) {
                ForEach(Array(overview.keyMetrics.keys.sorted()), id: \.self) { key in
                    if let value = overview.keyMetrics[key] {
                        VStack(spacing: 4) {
                            Text(key.capitalized)
                                .font(.system(size: 11))
                                .foregroundColor(.mediumGray)
                            Text(value)
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(.deepOcean)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(
                            RoundedRectangle(cornerRadius: 10)
                                .fill(Color.subtleBlueLight.opacity(0.5))
                        )
                    }
                }
            }
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.white)
                .shadow(color: Color.oceanBlue.opacity(0.1), radius: 15, x: 0, y: 8)
        )
    }
}

// MARK: - Disease Risk Card
struct DiseaseRiskCard: View {
    let risk: DiseaseRisk
    @State private var isExpanded = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Button {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                    isExpanded.toggle()
                }
            } label: {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(risk.diseaseName)
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(.deepOcean)
                        
                        HStack {
                            Text(risk.riskLevel)
                                .font(.system(size: 13, weight: .medium))
                                .foregroundColor(risk.riskLevel.riskColor)
                            
                            Text("•")
                                .foregroundColor(.mediumGray)
                            
                            Text("\(risk.probability)% probability")
                                .font(.system(size: 13))
                                .foregroundColor(.mediumGray)
                        }
                    }
                    
                    Spacer()
                    
                    Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.oceanBlue)
                }
            }
            .buttonStyle(PlainButtonStyle())
            
            if isExpanded {
                VStack(alignment: .leading, spacing: 12) {
                    if !risk.symptoms.isEmpty {
                        DetailSection(title: "Symptoms", items: risk.symptoms, color: .aquaRed)
                    }
                    
                    if !risk.causes.isEmpty {
                        DetailSection(title: "Causes", items: risk.causes, color: .aquaYellow)
                    }
                    
                    if !risk.prevention.isEmpty {
                        DetailSection(title: "Prevention", items: risk.prevention, color: .aquaGreen)
                    }
                    
                    if let treatment = risk.treatment, !treatment.isEmpty {
                        DetailSection(title: "Treatment", items: treatment, color: .oceanBlue)
                    }
                }
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.white)
                .shadow(color: Color.oceanBlue.opacity(0.08), radius: 10, x: 0, y: 5)
        )
    }
}

// MARK: - Detail Section
struct DetailSection: View {
    let title: String
    let items: [String]
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(color)
            
            ForEach(items, id: \.self) { item in
                HStack(alignment: .top, spacing: 8) {
                    Circle()
                        .fill(color.opacity(0.6))
                        .frame(width: 5, height: 5)
                        .padding(.top, 6)
                    
                    Text(item)
                        .font(.system(size: 13))
                        .foregroundColor(.deepOcean.opacity(0.8))
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(color.opacity(0.08))
        )
    }
}

// MARK: - Harvest Insights Card
struct HarvestInsightsCard: View {
    let insights: HarvestInsights
    
    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Growth Stage")
                        .font(.system(size: 12))
                        .foregroundColor(.mediumGray)
                    Text(insights.currentGrowthStage)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.deepOcean)
                }
                
                Spacer()
                
                if let harvestDate = insights.estimatedHarvestDate {
                    VStack(alignment: .trailing, spacing: 4) {
                        Text("Est. Harvest")
                            .font(.system(size: 12))
                            .foregroundColor(.mediumGray)
                        Text(harvestDate)
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(.oceanBlue)
                    }
                }
            }
            
            if let yield = insights.expectedYield {
                HStack {
                    Image(systemName: "chart.bar.fill")
                        .foregroundColor(.aquaGreen)
                    Text("Expected Yield: \(yield)")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.deepOcean)
                }
                .padding(10)
                .background(
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color.aquaGreen.opacity(0.1))
                )
            }
            
            if !insights.optimalConditions.isEmpty {
                DetailSection(
                    title: "Optimal Conditions",
                    items: insights.optimalConditions,
                    color: .oceanBlue
                )
            }
            
            if !insights.growthRecommendations.isEmpty {
                DetailSection(
                    title: "Growth Recommendations",
                    items: insights.growthRecommendations,
                    color: .aquaGreen
                )
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.white)
                .shadow(color: Color.oceanBlue.opacity(0.08), radius: 10, x: 0, y: 5)
        )
    }
}

// MARK: - Water Concern Card
struct WaterConcernCard: View {
    let concern: WaterConcern
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(concern.parameter)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.deepOcean)
                    
                    HStack {
                        Text(concern.currentValue)
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(concern.severity.riskColor)
                        
                        Text("(Optimal: \(concern.optimalRange))")
                            .font(.system(size: 12))
                            .foregroundColor(.mediumGray)
                    }
                }
                
                Spacer()
                
                Text(concern.severity)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(concern.severity.riskColor)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(
                        Capsule()
                            .fill(concern.severity.riskColor.opacity(0.15))
                    )
            }
            
            Text(concern.impact)
                .font(.system(size: 13))
                .foregroundColor(.deepOcean.opacity(0.7))
            
            if !concern.actionItems.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Actions")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.oceanBlue)
                    
                    ForEach(concern.actionItems, id: \.self) { action in
                        HStack(alignment: .top, spacing: 8) {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.system(size: 12))
                                .foregroundColor(.oceanBlue)
                            
                            Text(action)
                                .font(.system(size: 12))
                                .foregroundColor(.deepOcean)
                        }
                    }
                }
                .padding(10)
                .background(
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color.oceanBlue.opacity(0.08))
                )
            }
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.white)
                .shadow(color: Color.oceanBlue.opacity(0.08), radius: 10, x: 0, y: 5)
        )
    }
}

// MARK: - Recommendation Card
struct RecommendationCard: View {
    let recommendation: Recommendation
    
    var priorityColor: Color {
        switch recommendation.priority.lowercased() {
        case "high": return .aquaRed
        case "medium": return .aquaYellow
        default: return .aquaGreen
        }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(recommendation.title)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.deepOcean)
                    
                    Text(recommendation.category)
                        .font(.system(size: 12))
                        .foregroundColor(.mediumGray)
                }
                
                Spacer()
                
                Text(recommendation.priority)
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(priorityColor)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(
                        Capsule()
                            .fill(priorityColor.opacity(0.15))
                    )
            }
            
            Text(recommendation.description)
                .font(.system(size: 13))
                .foregroundColor(.deepOcean.opacity(0.7))
                .lineSpacing(3)
            
            if !recommendation.actionItems.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    ForEach(recommendation.actionItems, id: \.self) { action in
                        HStack(alignment: .top, spacing: 8) {
                            Image(systemName: "arrow.right.circle.fill")
                                .font(.system(size: 12))
                                .foregroundColor(.oceanBlue)
                            
                            Text(action)
                                .font(.system(size: 12))
                                .foregroundColor(.deepOcean)
                        }
                    }
                }
            }
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.white)
                .shadow(color: Color.oceanBlue.opacity(0.08), radius: 10, x: 0, y: 5)
        )
    }
}

// MARK: - Product Card
struct ProductCard: View {
    let product: MarketplaceProduct
    @EnvironmentObject private var cartManager: CartManager
    @State private var swipeOffset: CGFloat = 0
    @State private var dragAmount = CGSize.zero
    
    private var currentQuantity: Int {
        cartManager.getQuantity(for: product)
    }
    
    var body: some View {
        VStack(spacing: 12) {
            HStack(spacing: 14) {
                // Icon
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(
                            LinearGradient(
                                colors: [Color.oceanBlue.opacity(0.15), Color.oceanBlue.opacity(0.25)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 50, height: 50)
                    
                    Image(systemName: product.imageName)
                        .font(.system(size: 22))
                        .foregroundColor(.oceanBlue)
                }
                
                // Content
                VStack(alignment: .leading, spacing: 4) {
                    Text(product.name)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(.deepOcean)
                    
                    Text(product.description)
                        .font(.system(size: 12))
                        .foregroundColor(.mediumGray)
                        .lineLimit(2)
                    
                    HStack {
                        Text(product.formattedPrice)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(.aquaGreen)
                        
                        Text("per \(product.unit)")
                            .font(.system(size: 10))
                            .foregroundColor(.mediumGray)
                        
                        Spacer()
                        
                        // Stock status
                        HStack(spacing: 4) {
                            Circle()
                                .fill(product.inStock ? Color.aquaGreen : Color.aquaRed)
                                .frame(width: 6, height: 6)
                            Text(product.inStock ? "In Stock" : "Out of Stock")
                                .font(.system(size: 10, weight: .medium))
                                .foregroundColor(product.inStock ? Color.aquaGreen : Color.aquaRed)
                        }
                    }
                    
                    // Rating
                    HStack(spacing: 2) {
                        ForEach(1...5, id: \.self) { star in
                            Image(systemName: star <= Int(product.rating) ? "star.fill" : "star")
                                .font(.system(size: 8))
                                .foregroundColor(Color.aquaYellow)
                        }
                        Text(String(format: "%.1f", product.rating))
                            .font(.system(size: 9))
                            .foregroundColor(.mediumGray)
                    }
                }
                
                Spacer()
            }
            
            // Swipe Quantity Control
            HStack {
                // Decrease indicator (swipe right to decrease)
                HStack(spacing: 4) {
                    Image(systemName: "minus.circle.fill")
                        .foregroundColor(.aquaRed)
                        .opacity(dragAmount.width > 30 ? 1.0 : 0.3)
                    Text("Swipe →")
                        .font(.system(size: 10))
                        .foregroundColor(.mediumGray)
                        .opacity(dragAmount.width > 30 ? 1.0 : 0.5)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                
                // Current quantity display
                VStack(spacing: 2) {
                    if currentQuantity > 0 {
                        Text("In Cart")
                            .font(.system(size: 10))
                            .foregroundColor(.mediumGray)
                        Text("\(currentQuantity)")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.oceanBlue)
                    } else {
                        Text("Swipe to Add")
                            .font(.system(size: 12))
                            .foregroundColor(.mediumGray)
                    }
                }
                .frame(minWidth: 80)
                
                // Increase indicator (swipe left to increase)
                HStack(spacing: 4) {
                    Text("← Swipe")
                        .font(.system(size: 10))
                        .foregroundColor(.mediumGray)
                        .opacity(dragAmount.width < -30 ? 1.0 : 0.5)
                    Image(systemName: "plus.circle.fill")
                        .foregroundColor(.aquaGreen)
                        .opacity(dragAmount.width < -30 ? 1.0 : 0.3)
                }
                .frame(maxWidth: .infinity, alignment: .trailing)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(product.inStock ? Color.subtleBlueLight.opacity(0.5) : Color.lightGray.opacity(0.3))
            )
            .offset(x: dragAmount.width * 0.1) // Subtle visual feedback
            .gesture(
                DragGesture()
                    .onChanged { value in
                        if product.inStock {
                            dragAmount = value.translation
                        }
                    }
                    .onEnded { value in
                        if product.inStock {
                            // Swipe left (negative width) to increase (+1)
                            if value.translation.width < -50 {
                                withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
                                    cartManager.addToCart(product: product)
                                }
                            }
                            // Swipe right (positive width) to decrease (-1)
                            else if value.translation.width > 50 && currentQuantity > 0 {
                                withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
                                    cartManager.removeFromCart(product: product)
                                }
                            }
                        }
                        
                        // Reset drag
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                            dragAmount = .zero
                        }
                    }
            )
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.white)
                .shadow(color: Color.oceanBlue.opacity(0.08), radius: 10, x: 0, y: 5)
        )
    }
}

// MARK: - Preview
#Preview {
    AIAnalyticsView(tank: Tank.sampleTanks[0])
        .environmentObject(CartManager())
        .environmentObject(UserProfileManager())
}
