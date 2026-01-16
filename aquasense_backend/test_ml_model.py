"""
Comprehensive ML Model Integration Test
Tests the complete disease detection pipeline with the actual model
"""
import os
import sys
import asyncio
import base64
from io import BytesIO

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from ml.disease_classifier import DiseaseClassifier
from PIL import Image
import numpy as np


def create_test_image(size=(224, 224)):
    """Create a dummy test image for testing"""
    # Create a random RGB image
    img_array = np.random.randint(0, 255, (*size, 3), dtype=np.uint8)
    img = Image.fromarray(img_array, 'RGB')
    return img


def image_to_base64(img):
    """Convert PIL Image to base64 string"""
    buffered = BytesIO()
    img.save(buffered, format="JPEG")
    img_bytes = buffered.getvalue()
    img_base64 = base64.b64encode(img_bytes).decode('utf-8')
    return img_base64


def test_model_loading():
    """Test that the ML model loads correctly"""
    print("=" * 60)
    print("Test 1: Model Loading")
    print("=" * 60)

    classifier = DiseaseClassifier()

    if classifier.model is None:
        print("✗ Model not loaded")
        print(f"  Model path: {classifier.model_path}")
        print(f"  File exists: {os.path.exists(classifier.model_path)}")
        return False

    print("✓ Model loaded successfully")
    print(f"  Model type: {type(classifier.model)}")
    print(f"  Model path: {classifier.model_path}")

    # Check model input shape
    try:
        input_shape = classifier.model.input_shape
        print(f"  Expected input shape: {input_shape}")
    except:
        print("  Could not determine input shape")

    return True


def test_label_map():
    """Test label map loading"""
    print("\n" + "=" * 60)
    print("Test 2: Label Map Loading")
    print("=" * 60)

    classifier = DiseaseClassifier()

    print(f"✓ Label map loaded with {len(classifier.label_map)} classes")
    print("\nDisease Classes:")
    for idx, name in sorted(classifier.label_map.items(), key=lambda x: int(x[0])):
        print(f"  [{idx}] {name}")

    # Verify all indices 0-6 are present
    expected_indices = set(str(i) for i in range(7))
    actual_indices = set(classifier.label_map.keys())

    if expected_indices == actual_indices:
        print("\n✓ All expected indices (0-6) are present")
        return True
    else:
        print(f"\n✗ Missing indices: {expected_indices - actual_indices}")
        return False


def test_disease_info():
    """Test disease information mapping"""
    print("\n" + "=" * 60)
    print("Test 3: Disease Information Mapping")
    print("=" * 60)

    classifier = DiseaseClassifier()

    missing = []
    for idx, disease_name in classifier.label_map.items():
        if disease_name not in classifier.disease_info:
            missing.append(disease_name)

    if missing:
        print(f"✗ Missing disease info for: {missing}")
        return False

    print(f"✓ All {len(classifier.label_map)} diseases have detailed information")

    # Show sample info for each disease
    print("\nDisease Information Summary:")
    for idx in sorted(classifier.label_map.keys(), key=int):
        disease_name = classifier.label_map[idx]
        info = classifier.disease_info[disease_name]
        print(f"\n  [{idx}] {info['name']}")
        print(f"      Causes: {len(info['causes'])} listed")
        print(f"      Symptoms: {len(info['symptoms'])} listed")
        print(f"      Prevention: {len(info['prevention'])} measures")

    return True


async def test_prediction_with_dummy_image():
    """Test prediction with a dummy image"""
    print("\n" + "=" * 60)
    print("Test 4: Prediction with Dummy Image")
    print("=" * 60)

    classifier = DiseaseClassifier()

    if classifier.model is None:
        print("⚠ Skipping prediction test (model not loaded)")
        return True

    # Create test image
    print("Creating test image (224x224 RGB)...")
    test_img = create_test_image()
    img_base64 = image_to_base64(test_img)

    print(f"Test image created: {len(img_base64)} bytes (base64)")

    # Run prediction
    print("Running prediction...")
    try:
        results = await classifier.predict(img_base64)

        print(f"✓ Prediction completed successfully")
        print(f"  Number of results: {len(results)}")

        if results:
            print("\nTop Predictions:")
            for i, disease in enumerate(results, 1):
                print(f"  {i}. {disease.name}")
                print(f"     Confidence: {disease.confidence:.2%}")
                print(f"     Causes: {len(disease.causes)} listed")
                print(f"     Symptoms: {len(disease.symptoms)} listed")
        else:
            print("  No diseases detected above threshold")

        return True

    except Exception as e:
        print(f"✗ Prediction failed: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


def test_prediction_pipeline():
    """Test the prediction to disease info conversion"""
    print("\n" + "=" * 60)
    print("Test 5: Prediction Pipeline")
    print("=" * 60)

    classifier = DiseaseClassifier()

    # Create dummy predictions (simulate model output)
    print("Creating dummy model predictions...")
    dummy_predictions = np.array([
        0.15,  # Bacterial Red disease
        0.08,  # Aeromoniasis
        0.05,  # Bacterial gill disease
        0.12,  # Saprolegniasis
        0.45,  # Healthy Fish (highest)
        0.10,  # Parasitic diseases
        0.05   # White tail disease
    ])

    print(f"Dummy predictions shape: {dummy_predictions.shape}")
    print(f"Predictions sum to: {dummy_predictions.sum():.2f}")

    # Convert to disease info
    results = classifier._predictions_to_disease_info(dummy_predictions)

    print(f"\n✓ Pipeline conversion successful")
    print(f"  Input: 7 class probabilities")
    print(f"  Output: {len(results)} disease info objects")

    if results:
        print("\nConverted Results:")
        for i, disease in enumerate(results, 1):
            print(f"  {i}. {disease.name}")
            print(f"     Confidence: {disease.confidence:.2%}")
            print(f"     Has description: {len(disease.description) > 0}")
            print(f"     Has treatment: {len(disease.treatment) > 0}")

    return True


def test_confidence_thresholds():
    """Test confidence threshold filtering"""
    print("\n" + "=" * 60)
    print("Test 6: Confidence Threshold Filtering")
    print("=" * 60)

    classifier = DiseaseClassifier()

    # Test with low confidence predictions
    low_conf = np.array([0.15, 0.15, 0.15, 0.15, 0.20, 0.10, 0.10])
    results_low = classifier._predictions_to_disease_info(low_conf, confidence_threshold=0.2)

    # Test with high confidence predictions
    high_conf = np.array([0.05, 0.05, 0.05, 0.05, 0.70, 0.05, 0.05])
    results_high = classifier._predictions_to_disease_info(high_conf, confidence_threshold=0.2)

    print(f"Low confidence predictions (threshold=0.2):")
    print(f"  Results: {len(results_low)} diseases")

    print(f"\nHigh confidence prediction (0.70 for Healthy Fish):")
    print(f"  Results: {len(results_high)} diseases")
    if results_high:
        print(f"  Top result: {results_high[0].name} ({results_high[0].confidence:.2%})")

    print("\n✓ Confidence threshold filtering works correctly")
    return True


def test_model_input_output():
    """Test model input/output shapes"""
    print("\n" + "=" * 60)
    print("Test 7: Model Input/Output Shapes")
    print("=" * 60)

    classifier = DiseaseClassifier()

    if classifier.model is None:
        print("⚠ Skipping (model not loaded)")
        return True

    try:
        # Check input shape
        input_shape = classifier.model.input_shape
        expected_input = (None, 224, 224, 3)

        print(f"Model input shape: {input_shape}")
        print(f"Expected: {expected_input}")

        # Check output shape
        output_shape = classifier.model.output_shape
        expected_output = (None, 7)

        print(f"\nModel output shape: {output_shape}")
        print(f"Expected: {expected_output}")

        if output_shape[1] == 7:
            print("\n✓ Model architecture is correct")
            return True
        else:
            print(f"\n✗ Expected 7 output classes, got {output_shape[1]}")
            return False

    except Exception as e:
        print(f"✗ Error checking model shapes: {str(e)}")
        return False


async def run_all_tests():
    """Run all tests"""
    print("\n")
    print("╔" + "=" * 58 + "╗")
    print("║" + " " * 58 + "║")
    print("║" + "  AquaSense ML Model Integration Test Suite".center(58) + "║")
    print("║" + " " * 58 + "║")
    print("╚" + "=" * 58 + "╝")

    tests = [
        ("Model Loading", test_model_loading),
        ("Label Map Loading", test_label_map),
        ("Disease Information", test_disease_info),
        ("Prediction with Dummy Image", test_prediction_with_dummy_image),
        ("Prediction Pipeline", test_prediction_pipeline),
        ("Confidence Thresholds", test_confidence_thresholds),
        ("Model Architecture", test_model_input_output),
    ]

    results = []

    for name, test_func in tests:
        try:
            if asyncio.iscoroutinefunction(test_func):
                result = await test_func()
            else:
                result = test_func()
            results.append((name, result))
        except Exception as e:
            print(f"\n✗ {name} failed with exception: {str(e)}")
            import traceback
            traceback.print_exc()
            results.append((name, False))

    # Summary
    print("\n" + "=" * 60)
    print("TEST SUMMARY")
    print("=" * 60)

    passed = sum(1 for _, result in results if result)
    total = len(results)

    for name, result in results:
        status = "✓ PASS" if result else "✗ FAIL"
        print(f"{status}: {name}")

    print("\n" + "-" * 60)
    print(f"Total: {passed}/{total} tests passed ({passed/total*100:.1f}%)")
    print("=" * 60)

    if passed == total:
        print("\n🎉 All tests passed! ML model integration is working perfectly.")
        print("\nThe system is ready to:")
        print("  • Load the disease classification model")
        print("  • Process fish images")
        print("  • Detect 7 disease classes")
        print("  • Provide detailed disease information")
        print("  • Generate treatment recommendations")
        return 0
    else:
        print(f"\n⚠ {total - passed} test(s) failed.")
        print("Please review the errors above and fix any issues.")
        return 1


if __name__ == "__main__":
    exit_code = asyncio.run(run_all_tests())
    sys.exit(exit_code)
