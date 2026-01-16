//
//  CameraPreviewView.swift
//  aqua
//
//  Created for full-screen camera disease detection
//

import SwiftUI
import AVFoundation
import UIKit
import Combine

struct CameraPreviewView: UIViewRepresentable {
    @Binding var capturedImage: UIImage?
    let onCapture: () -> Void
    let cameraController: CameraController?
    
    func makeUIView(context: Context) -> CameraPreview {
        let preview = CameraPreview()
        preview.onImageCaptured = { image in
            DispatchQueue.main.async {
                capturedImage = image
                onCapture()
            }
        }
        
        // Connect with camera controller
        cameraController?.setCameraPreview(preview)
        
        return preview
    }
    
    func updateUIView(_ uiView: CameraPreview, context: Context) {}
}

class CameraPreview: UIView {
    var captureSession: AVCaptureSession!
    var previewLayer: AVCaptureVideoPreviewLayer!
    var photoOutput: AVCapturePhotoOutput!
    var onImageCaptured: ((UIImage) -> Void)?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupCamera()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupCamera()
    }
    
    func setupCamera() {
        captureSession = AVCaptureSession()
        captureSession.sessionPreset = .photo
        
        guard let backCamera = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) else {
            print("Unable to access back camera!")
            return
        }
        
        do {
            let input = try AVCaptureDeviceInput(device: backCamera)
            
            if captureSession.canAddInput(input) {
                captureSession.addInput(input)
            }
            
            photoOutput = AVCapturePhotoOutput()
            
            if captureSession.canAddOutput(photoOutput) {
                captureSession.addOutput(photoOutput)
            }
            
            previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
            previewLayer.videoGravity = .resizeAspectFill
            layer.addSublayer(previewLayer)
            
            DispatchQueue.global(qos: .userInitiated).async { [weak self] in
                self?.captureSession.startRunning()
            }
            
        } catch {
            print("Error setting up camera: \(error)")
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        previewLayer?.frame = bounds
    }
    
    func capturePhoto() {
        let settings = AVCapturePhotoSettings()
        settings.flashMode = .auto
        
        photoOutput.capturePhoto(with: settings, delegate: self)
    }
    
    func stopSession() {
        if captureSession?.isRunning == true {
            DispatchQueue.global(qos: .userInitiated).async { [weak self] in
                self?.captureSession.stopRunning()
            }
        }
    }
    
    func startSession() {
        if captureSession?.isRunning == false {
            DispatchQueue.global(qos: .userInitiated).async { [weak self] in
                self?.captureSession.startRunning()
            }
        }
    }
}

extension CameraPreview: AVCapturePhotoCaptureDelegate {
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        guard let imageData = photo.fileDataRepresentation(),
              let image = UIImage(data: imageData) else {
            print("Error capturing photo: \(error?.localizedDescription ?? "Unknown error")")
            return
        }
        
        onImageCaptured?(image)
    }
}

// MARK: - Camera Controller for external capture
@MainActor
class CameraController: ObservableObject {
    private var cameraPreview: CameraPreview?
    
    func setCameraPreview(_ preview: CameraPreview) {
        self.cameraPreview = preview
    }
    
    func capturePhoto() {
        cameraPreview?.capturePhoto()
    }
    
    func startSession() {
        cameraPreview?.startSession()
    }
    
    func stopSession() {
        cameraPreview?.stopSession()
    }
}
