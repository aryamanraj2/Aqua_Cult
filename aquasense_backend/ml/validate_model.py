#!/usr/bin/env python3
"""
Disease Detection Model Validation Script

Run this script to validate that the disease detection model loads correctly.
Usage: python ml/validate_model.py
"""
import os
import sys
import json


def validate():
    """Validate disease detection model and dependencies"""
    print("=" * 60)
    print("Disease Detection Model Validation")
    print("=" * 60)

    # Check TensorFlow
    try:
        import tensorflow as tf
        print(f"\n✓ TensorFlow version: {tf.__version__}")
        print(f"  Keras version: {tf.keras.__version__}")
    except ImportError as e:
        print(f"\n✗ TensorFlow not installed: {e}")
        return False

    # Check model files
    model_path = "models/fish_disease.keras"
    label_map_path = "models/label_map.json"

    print(f"\nChecking model files...")
    if not os.path.exists(model_path):
        print(f"✗ Model not found: {model_path}")
        return False
    else:
        model_size_mb = os.path.getsize(model_path) / (1024 * 1024)
        print(f"✓ Model file exists: {model_path} ({model_size_mb:.2f} MB)")

    if not os.path.exists(label_map_path):
        print(f"✗ Label map not found: {label_map_path}")
        return False
    else:
        print(f"✓ Label map exists: {label_map_path}")

    # Load label map
    try:
        with open(label_map_path, 'r') as f:
            label_map = json.load(f)
        print(f"\n✓ Label map loaded successfully")
        print(f"  Number of classes: {len(label_map)}")
        print(f"  Classes:")
        for idx, label in label_map.items():
            print(f"    {idx}: {label}")
    except Exception as e:
        print(f"\n✗ Error loading label map: {e}")
        return False

    # Load model
    print(f"\nLoading model (this may take a few seconds)...")
    try:
        model = tf.keras.models.load_model(model_path)
        print(f"✓ Model loaded successfully!")
        print(f"  Input shape: {model.input_shape}")
        print(f"  Output shape: {model.output_shape}")
        print(f"  Number of layers: {len(model.layers)}")

        # Validate output matches label map
        output_classes = model.output_shape[-1]
        if output_classes == len(label_map):
            print(f"✓ Model output ({output_classes} classes) matches label map ({len(label_map)} classes)")
        else:
            print(f"⚠ Warning: Model output ({output_classes}) != label map size ({len(label_map)})")
    except Exception as e:
        print(f"\n✗ Error loading model: {e}")
        print(f"\nTroubleshooting:")
        print(f"  1. Ensure TensorFlow version is 2.16+ (current: {tf.__version__})")
        print(f"  2. If on macOS, use tensorflow-macos instead of tensorflow")
        print(f"  3. Check that the model file is not corrupted")
        return False

    # Test prediction (dry run)
    print(f"\nTesting inference...")
    try:
        import numpy as np
        # Create dummy input (224x224x3 RGB image)
        dummy_input = np.random.rand(1, 224, 224, 3).astype(np.float32)
        predictions = model.predict(dummy_input, verbose=0)
        print(f"✓ Inference test successful")
        print(f"  Output shape: {predictions.shape}")
        print(f"  Sample prediction: {predictions[0][:3]} ...")  # First 3 values
    except Exception as e:
        print(f"✗ Inference test failed: {e}")
        return False

    print("\n" + "=" * 60)
    print("✓ All validations passed!")
    print("=" * 60)
    print("\nThe disease detection model is ready to use.")
    print("You can now start the backend with: uvicorn main:app --reload")
    return True


if __name__ == "__main__":
    try:
        success = validate()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\nValidation interrupted by user.")
        sys.exit(1)
    except Exception as e:
        print(f"\n\nUnexpected error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
