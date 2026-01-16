
import SwiftUI
import PhotosUI

struct AddTankView: View {
    @Environment(\.dismiss) var dismiss
    @Binding var tanks: [Tank]
    
    @State private var tankName = ""
    @State private var selectedSpecies: Set<String> = []
    @State private var length = ""
    @State private var width = ""
    @State private var depth = ""
    @State private var sensorID = ""
    @State private var currentStage = "Preparation"
    @State private var showingImagePicker = false
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var selectedImage: UIImage?
    
    let availableSpecies = ["Salmon", "Cod", "Trout", "Tilapia", "Shrimp", "Catfish", "Bass"]
    let stages = ["Preparation", "Stocking", "Grow-out", "Feeding", "Monitoring", "Harvesting"]
    
    var body: some View {
        ZStack {
            backgroundGradient
            mainContent
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button("Cancel") {
                    dismiss()
                }
                .foregroundColor(Color.oceanBlue)
            }
        }
        .onChange(of: selectedPhotoItem) { _, newItem in
            Task {
                if let newItem = newItem {
                    if let data = try? await newItem.loadTransferable(type: Data.self) {
                        if let uiImage = UIImage(data: data) {
                            selectedImage = uiImage
                        }
                    }
                }
            }
        }
    }
    
    private var backgroundGradient: some View {
        LinearGradient(
            colors: [Color.deepOcean, Color.mediumBlue.opacity(0.8)],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
        .ignoresSafeArea()
    }
    
    private var mainContent: some View {
        ScrollView {
            VStack(spacing: 24) {
                headerSection
                formFields
                addButton
            }
        }
    }
    
    private var headerSection: some View {
        VStack(spacing: 8) {
            Image(systemName: "fish.circle.fill")
                .font(.system(size: 60))
                .foregroundColor(Color.oceanBlue)
            
            Text("Add New Tank")
                .font(.system(size: 32, weight: .bold))
                .foregroundColor(.white)
            
            Text("Start monitoring a new aquaculture tank")
                .font(.system(size: 16))
                .foregroundColor(.white.opacity(0.7))
        }
        .padding(.top, 20)
    }
    
    private var formFields: some View {
        VStack(spacing: 20) {
            tankNameField
            speciesSelectionField
            dimensionsField
            sensorIDField
            stageSelectionField
            photoField
        }
        .padding(.horizontal)
    }
    
    private var tankNameField: some View {
        FormSection(title: "Tank Name") {
            OceanTextField(placeholder: "Enter tank name", text: $tankName)
        }
    }
    
    private var speciesSelectionField: some View {
        FormSection(title: "Species Type") {
            VStack(spacing: 12) {
                Text("Select one or more species")
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.6))
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                    ForEach(availableSpecies, id: \.self) { species in
                        SpeciesToggle(species: species, isSelected: selectedSpecies.contains(species)) {
                            if selectedSpecies.contains(species) {
                                selectedSpecies.remove(species)
                            } else {
                                selectedSpecies.insert(species)
                            }
                        }
                    }
                }
            }
        }
    }
    
    private var dimensionsField: some View {
        FormSection(title: "Tank Dimensions") {
            VStack(spacing: 12) {
                HStack(spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Length (m)")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.6))
                        OceanTextField(placeholder: "0.0", text: $length, keyboardType: .decimalPad)
                    }
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Width (m)")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.6))
                        OceanTextField(placeholder: "0.0", text: $width, keyboardType: .decimalPad)
                    }
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text("Depth (m)")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.6))
                    OceanTextField(placeholder: "0.0", text: $depth, keyboardType: .decimalPad)
                }
                
                if let vol = calculatedVolume {
                    HStack {
                        Image(systemName: "cube.fill")
                            .foregroundColor(Color.oceanBlue)
                        Text("Volume: \(vol, specifier: "%.2f") m³")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.white)
                    }
                    .padding(12)
                    .frame(maxWidth: .infinity)
                    .background(Color.oceanBlue.opacity(0.3))
                    .cornerRadius(10)
                }
            }
        }
    }
    
    private var sensorIDField: some View {
        FormSection(title: "IoT Sensor ID") {
            OceanTextField(placeholder: "Enter sensor ID", text: $sensorID)
        }
    }
    
    private var stageSelectionField: some View {
        FormSection(title: "Current Stage") {
            Menu {
                ForEach(stages, id: \.self) { stage in
                    Button(stage) {
                        currentStage = stage
                    }
                }
            } label: {
                HStack {
                    Text(currentStage)
                        .foregroundColor(.white)
                    Spacer()
                    Image(systemName: "chevron.down")
                        .foregroundColor(.white.opacity(0.6))
                }
                .padding()
                .background(Color.white.opacity(0.1))
                .cornerRadius(12)
            }
        }
    }
    
    private var photoField: some View {
        FormSection(title: "Tank Photo (Optional)") {
            VStack(spacing: 12) {
                // Display selected image if available
                if let image = selectedImage {
                    Image(uiImage: image)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(height: 120)
                        .clipped()
                        .cornerRadius(12)
                        .overlay(
                            // Remove button
                            VStack {
                                HStack {
                                    Spacer()
                                    Button {
                                        selectedImage = nil
                                        selectedPhotoItem = nil
                                    } label: {
                                        Image(systemName: "xmark.circle.fill")
                                            .font(.system(size: 24))
                                            .foregroundColor(.white)
                                            .background(Circle().fill(Color.black.opacity(0.6)))
                                    }
                                    .padding(8)
                                }
                                Spacer()
                            }
                        )
                }
                
                // Photo picker button
                PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                    HStack {
                        Image(systemName: selectedImage == nil ? "camera.fill" : "photo.fill")
                            .font(.system(size: 24))
                            .foregroundColor(Color.oceanBlue)
                        VStack(alignment: .leading, spacing: 4) {
                            Text(selectedImage == nil ? "Add Tank Photo" : "Change Photo")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.white)
                            Text(selectedImage == nil ? "Select from photo library" : "Select a different photo")
                                .font(.system(size: 14))
                                .foregroundColor(.white.opacity(0.6))
                        }
                        Spacer()
                    }
                    .padding()
                    .background(Color.white.opacity(0.1))
                    .cornerRadius(12)
                }
            }
        }
    }
    
    private var addButton: some View {
        Button {
            addTank()
        } label: {
            HStack {
                Image(systemName: "plus.circle.fill")
                    .font(.system(size: 20))
                Text("Add Tank")
                    .font(.system(size: 18, weight: .semibold))
            }
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(
                LinearGradient(
                    colors: [Color.oceanBlue, Color.mediumBlue],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .cornerRadius(16)
        }
        .disabled(!isFormValid)
        .opacity(isFormValid ? 1.0 : 0.5)
        .padding(.horizontal)
        .padding(.bottom, 40)
    }
    
    var calculatedVolume: Double? {
        guard let l = Double(length), let w = Double(width), let d = Double(depth) else {
            return nil
        }
        return l * w * d
    }
    
    var isFormValid: Bool {
        !tankName.isEmpty &&
        !selectedSpecies.isEmpty &&
        calculatedVolume != nil &&
        calculatedVolume! > 0
    }
    
    func addTank() {
        guard let l = Double(length), let w = Double(width), let d = Double(depth) else {
            return
        }
        
        // Save image to documents directory if one is selected
        var savedImageName: String? = nil
        if let image = selectedImage {
            savedImageName = saveImageToDocuments(image: image)
        }
        
        let newTank = Tank(
            name: tankName,
            species: Array(selectedSpecies),
            dimensions: TankDimensions(length: l, width: w, depth: d),
            currentStage: currentStage,
            sensorID: sensorID.isEmpty ? nil : sensorID,
            waterQuality: WaterQuality(
                temperature: 15.0,
                pH: 7.5,
                dissolvedOxygen: 7.0,
                ammonia: 0.03,
                salinity: 32.0,
                turbidity: 2.5,
                status: .good
            ),
            imageName: savedImageName
        )
        
        tanks.append(newTank)
        dismiss()
    }
    
    private func saveImageToDocuments(image: UIImage) -> String? {
        guard let imageData = image.jpegData(compressionQuality: 0.8) else {
            return nil
        }
        
        let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        let fileName = "tank_image_\(UUID().uuidString).jpg"
        let fileURL = documentsDirectory.appendingPathComponent(fileName)
        
        do {
            try imageData.write(to: fileURL)
            return fileName
        } catch {
            print("Error saving image: \(error)")
            return nil
        }
    }
}

// MARK: - Form Section
struct FormSection<Content: View>: View {
    let title: String
    let content: Content
    
    init(title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.white)
            
            content
        }
    }
}

// MARK: - Ocean Text Field Style
struct OceanTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding()
            .background(Color.white.opacity(0.1))
            .cornerRadius(12)
            .foregroundColor(.white)
            .tint(.white)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.white.opacity(0.2), lineWidth: 1)
            )
    }
}

// MARK: - Custom Text Field with Grey Placeholder
struct OceanTextField: View {
    let placeholder: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default
    
    var body: some View {
        ZStack(alignment: .leading) {
            // Background and styling
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.white.opacity(0.1))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.white.opacity(0.2), lineWidth: 1)
                )
            
            // Placeholder text positioned inside the field
            if text.isEmpty {
                HStack {
                    Text(placeholder)
                        .foregroundColor(.gray)
                        .padding(.leading, 16)
                    Spacer()
                }
            }
            
            // Actual text field
            TextField("", text: $text)
                .keyboardType(keyboardType)
                .foregroundColor(.white)
                .tint(.white)
                .padding(16)
        }
    }
}

// MARK: - Species Toggle
struct SpeciesToggle: View {
    let species: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(isSelected ? Color.aquaGreen : .white.opacity(0.6))
                Text(species)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
                Spacer()
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(isSelected ? Color.oceanBlue.opacity(0.4) : Color.white.opacity(0.1))
            .cornerRadius(10)
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(isSelected ? Color.aquaGreen : Color.white.opacity(0.2), lineWidth: isSelected ? 2 : 1)
            )
        }
    }
}

struct AddTankView_Previews: PreviewProvider {
    static var previews: some View {
        AddTankView(tanks: .constant(Tank.sampleTanks))
    }
}
