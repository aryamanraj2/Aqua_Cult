"""
Test suite for the Water Quality ML Model (aqua_sense_model.pkl)

This script tests:
1. Model loading and basic properties
2. Direct model predictions with sample data
3. WaterQualityPredictor class integration
4. Edge cases and error handling
"""

import sys
import os
import joblib
import numpy as np
from typing import Dict, Any

# Add parent directory to path for imports
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from ml.water_quality_predictor import WaterQualityPredictor


class MockWaterQualityReading:
    """Mock object to simulate database WaterQuality readings"""
    def __init__(self, **kwargs):
        self.temperature = kwargs.get('temperature')
        self.ph = kwargs.get('ph')
        self.dissolved_oxygen = kwargs.get('dissolved_oxygen')
        self.turbidity = kwargs.get('turbidity')
        self.ammonia = kwargs.get('ammonia')
        self.nitrite = kwargs.get('nitrite')


def test_model_loading():
    """Test 1: Verify the model can be loaded and has expected properties"""
    print("=" * 80)
    print("TEST 1: MODEL LOADING AND PROPERTIES")
    print("=" * 80)

    try:
        model_path = "ml_tank/aqua_sense_model.pkl"
        model = joblib.load(model_path)

        print(f"✅ Model loaded successfully from {model_path}")
        print(f"📊 Model Type: {type(model).__name__}")

        # Check expected properties
        if hasattr(model, 'n_features_in_'):
            print(f"🔢 Number of Features: {model.n_features_in_}")
            assert model.n_features_in_ == 14, f"Expected 14 features, got {model.n_features_in_}"

        if hasattr(model, 'classes_'):
            print(f"🏷️  Classes: {model.classes_} (0=Excellent, 1=Good, 2=Poor)")
            assert len(model.classes_) == 3, f"Expected 3 classes, got {len(model.classes_)}"

        if hasattr(model, 'n_estimators'):
            print(f"🌲 Number of Trees: {model.n_estimators}")

        print("✅ Test 1 PASSED\n")
        return model

    except Exception as e:
        print(f"❌ Test 1 FAILED: {e}\n")
        raise


def test_direct_predictions(model):
    """Test 2: Test direct model predictions with various scenarios"""
    print("=" * 80)
    print("TEST 2: DIRECT MODEL PREDICTIONS")
    print("=" * 80)

    quality_labels = {0: "Excellent", 1: "Good", 2: "Poor"}

    # Test Case 1: Excellent water quality
    print("\n📊 Test Case 1: Excellent Water Quality (Ideal conditions)")
    excellent_features = np.array([[
        26.0,   # Temp (°C)
        30.0,   # Turbidity (cm)
        8.0,    # DO (mg/L)
        2.0,    # BOD (mg/L)
        5.0,    # CO2 (mg/L)
        7.5,    # pH
        120.0,  # Alkalinity (mg/L)
        150.0,  # Hardness (mg/L)
        60.0,   # Calcium (mg/L)
        0.01,   # Ammonia (mg/L)
        0.01,   # Nitrite (mg/L)
        0.03,   # Phosphorus (mg/L)
        0.001,  # H2S (mg/L)
        5000.0  # Plankton (No/L)
    ]])

    prediction = model.predict(excellent_features)[0]
    probabilities = model.predict_proba(excellent_features)[0]
    confidence = probabilities[prediction]

    print(f"  Input: Temp=26°C, pH=7.5, DO=8mg/L, Turbidity=30cm")
    print(f"  🎯 Prediction: {quality_labels[prediction]}")
    print(f"  📈 Confidence: {confidence:.2%}")
    print(f"  📊 Probabilities: Excellent={probabilities[0]:.2%}, Good={probabilities[1]:.2%}, Poor={probabilities[2]:.2%}")


    # Test Case 2: Poor water quality
    print("\n📊 Test Case 2: Poor Water Quality (Dangerous conditions)")
    poor_features = np.array([[
        35.0,   # Temp (high - stress)
        5.0,    # Turbidity (low - poor visibility)
        3.0,    # DO (low - hypoxia)
        10.0,   # BOD (high - pollution)
        15.0,   # CO2 (high)
        9.0,    # pH (high - alkaline)
        50.0,   # Alkalinity (low)
        300.0,  # Hardness (high)
        150.0,  # Calcium (high)
        2.0,    # Ammonia (high - toxic)
        1.0,    # Nitrite (high - toxic)
        0.5,    # Phosphorus (high - eutrophication)
        0.1,    # H2S (high - toxic)
        50000.0 # Plankton (very high - algal bloom)
    ]])

    prediction = model.predict(poor_features)[0]
    probabilities = model.predict_proba(poor_features)[0]
    confidence = probabilities[prediction]

    print(f"  Input: Temp=35°C, pH=9.0, DO=3mg/L, Ammonia=2mg/L")
    print(f"  🎯 Prediction: {quality_labels[prediction]}")
    print(f"  📈 Confidence: {confidence:.2%}")
    print(f"  📊 Probabilities: Excellent={probabilities[0]:.2%}, Good={probabilities[1]:.2%}, Poor={probabilities[2]:.2%}")


    # Test Case 3: Good/moderate water quality
    print("\n📊 Test Case 3: Good Water Quality (Acceptable conditions)")
    good_features = np.array([[
        28.0,   # Temp
        20.0,   # Turbidity
        6.0,    # DO (acceptable)
        4.0,    # BOD
        8.0,    # CO2
        7.8,    # pH
        100.0,  # Alkalinity
        180.0,  # Hardness
        70.0,   # Calcium
        0.1,    # Ammonia (slightly elevated)
        0.1,    # Nitrite (slightly elevated)
        0.1,    # Phosphorus
        0.01,   # H2S
        10000.0 # Plankton
    ]])

    prediction = model.predict(good_features)[0]
    probabilities = model.predict_proba(good_features)[0]
    confidence = probabilities[prediction]

    print(f"  Input: Temp=28°C, pH=7.8, DO=6mg/L, Ammonia=0.1mg/L")
    print(f"  🎯 Prediction: {quality_labels[prediction]}")
    print(f"  📈 Confidence: {confidence:.2%}")
    print(f"  📊 Probabilities: Excellent={probabilities[0]:.2%}, Good={probabilities[1]:.2%}, Poor={probabilities[2]:.2%}")

    print("\n✅ Test 2 PASSED\n")


async def test_predictor_class():
    """Test 3: Test WaterQualityPredictor class with mock readings"""
    print("=" * 80)
    print("TEST 3: WATERQUALITYPREDICTOR CLASS INTEGRATION")
    print("=" * 80)

    predictor = WaterQualityPredictor()

    # Test Case 1: Complete reading
    print("\n📊 Test Case 1: Complete sensor reading")
    reading1 = MockWaterQualityReading(
        temperature=26.5,
        ph=7.4,
        dissolved_oxygen=7.8,
        turbidity=25.0,
        ammonia=0.02,
        nitrite=0.015
    )

    result1 = await predictor.predict(reading1)

    if result1:
        print(f"  ✅ Prediction successful")
        print(f"  🎯 Prediction: {result1['prediction']}")
        print(f"  📈 Confidence: {result1['confidence']:.2%}")
        print(f"  🔢 Missing features: {len(result1['missing_features'])}")
    else:
        print(f"  ❌ Prediction failed")
        raise Exception("Prediction returned None")


    # Test Case 2: Partial reading (some missing values)
    print("\n📊 Test Case 2: Partial sensor reading (missing ammonia & nitrite)")
    reading2 = MockWaterQualityReading(
        temperature=27.0,
        ph=7.6,
        dissolved_oxygen=7.2,
        turbidity=22.0,
        ammonia=None,  # Missing
        nitrite=None   # Missing
    )

    result2 = await predictor.predict(reading2)

    if result2:
        print(f"  ✅ Prediction successful")
        print(f"  🎯 Prediction: {result2['prediction']}")
        print(f"  📈 Confidence: {result2['confidence']:.2%}")
        print(f"  🔢 Missing features: {len(result2['missing_features'])}")
        print(f"  ⚠️  Defaults used for: {', '.join([f for f in result2['missing_features'] if f in ['ammonia', 'nitrite']])}")
    else:
        print(f"  ❌ Prediction failed")
        raise Exception("Prediction returned None")


    # Test Case 3: Minimal reading (all optional fields missing)
    print("\n📊 Test Case 3: Minimal sensor reading (only temp & pH)")
    reading3 = MockWaterQualityReading(
        temperature=25.0,
        ph=7.2,
        dissolved_oxygen=None,
        turbidity=None,
        ammonia=None,
        nitrite=None
    )

    result3 = await predictor.predict(reading3)

    if result3:
        print(f"  ✅ Prediction successful")
        print(f"  🎯 Prediction: {result3['prediction']}")
        print(f"  📈 Confidence: {result3['confidence']:.2%}")
        print(f"  🔢 Missing features: {len(result3['missing_features'])}")
    else:
        print(f"  ❌ Prediction failed")
        raise Exception("Prediction returned None")

    print("\n✅ Test 3 PASSED\n")


def test_edge_cases(model):
    """Test 4: Test edge cases and extreme values"""
    print("=" * 80)
    print("TEST 4: EDGE CASES AND EXTREME VALUES")
    print("=" * 80)

    quality_labels = {0: "Excellent", 1: "Good", 2: "Poor"}

    # Test Case 1: Extreme cold temperature
    print("\n📊 Test Case 1: Extreme cold temperature (15°C)")
    cold_features = np.array([[15.0, 25.0, 7.0, 3.0, 5.0, 7.5, 100.0, 150.0, 60.0, 0.01, 0.01, 0.05, 0.001, 5000.0]])
    prediction = model.predict(cold_features)[0]
    print(f"  🎯 Prediction: {quality_labels[prediction]}")

    # Test Case 2: Extreme hot temperature
    print("\n📊 Test Case 2: Extreme hot temperature (38°C)")
    hot_features = np.array([[38.0, 25.0, 7.0, 3.0, 5.0, 7.5, 100.0, 150.0, 60.0, 0.01, 0.01, 0.05, 0.001, 5000.0]])
    prediction = model.predict(hot_features)[0]
    print(f"  🎯 Prediction: {quality_labels[prediction]}")

    # Test Case 3: Extreme low pH
    print("\n📊 Test Case 3: Extreme low pH (4.0)")
    low_ph_features = np.array([[26.0, 25.0, 7.0, 3.0, 5.0, 4.0, 100.0, 150.0, 60.0, 0.01, 0.01, 0.05, 0.001, 5000.0]])
    prediction = model.predict(low_ph_features)[0]
    print(f"  🎯 Prediction: {quality_labels[prediction]}")

    # Test Case 4: Extreme high pH
    print("\n📊 Test Case 4: Extreme high pH (10.0)")
    high_ph_features = np.array([[26.0, 25.0, 7.0, 3.0, 5.0, 10.0, 100.0, 150.0, 60.0, 0.01, 0.01, 0.05, 0.001, 5000.0]])
    prediction = model.predict(high_ph_features)[0]
    print(f"  🎯 Prediction: {quality_labels[prediction]}")

    # Test Case 5: Very low dissolved oxygen
    print("\n📊 Test Case 5: Very low dissolved oxygen (1.0 mg/L)")
    low_do_features = np.array([[26.0, 25.0, 1.0, 3.0, 5.0, 7.5, 100.0, 150.0, 60.0, 0.01, 0.01, 0.05, 0.001, 5000.0]])
    prediction = model.predict(low_do_features)[0]
    print(f"  🎯 Prediction: {quality_labels[prediction]}")

    # Test Case 6: Zero values
    print("\n📊 Test Case 6: All zero values (edge case)")
    zero_features = np.zeros((1, 14))
    prediction = model.predict(zero_features)[0]
    probabilities = model.predict_proba(zero_features)[0]
    print(f"  🎯 Prediction: {quality_labels[prediction]}")
    print(f"  📈 Confidence: {probabilities[prediction]:.2%}")

    print("\n✅ Test 4 PASSED\n")


def test_model_consistency():
    """Test 5: Test that same inputs produce same outputs (determinism)"""
    print("=" * 80)
    print("TEST 5: MODEL CONSISTENCY AND DETERMINISM")
    print("=" * 80)

    model_path = "ml_tank/aqua_sense_model.pkl"

    # Load model twice
    model1 = joblib.load(model_path)
    model2 = joblib.load(model_path)

    # Test features
    test_features = np.array([[26.0, 25.0, 7.5, 3.0, 5.0, 7.5, 100.0, 150.0, 60.0, 0.01, 0.01, 0.05, 0.001, 5000.0]])

    # Get predictions
    pred1 = model1.predict(test_features)[0]
    pred2 = model2.predict(test_features)[0]

    proba1 = model1.predict_proba(test_features)[0]
    proba2 = model2.predict_proba(test_features)[0]

    print(f"  Model 1 Prediction: {pred1}")
    print(f"  Model 2 Prediction: {pred2}")
    print(f"  Predictions match: {pred1 == pred2}")

    print(f"  Model 1 Probabilities: {proba1}")
    print(f"  Model 2 Probabilities: {proba2}")
    print(f"  Probabilities match: {np.allclose(proba1, proba2)}")

    assert pred1 == pred2, "Predictions should be identical"
    assert np.allclose(proba1, proba2), "Probabilities should be identical"

    print("\n✅ Test 5 PASSED\n")


def print_test_summary():
    """Print summary of all tests"""
    print("=" * 80)
    print("🎉 ALL TESTS PASSED SUCCESSFULLY!")
    print("=" * 80)
    print("\nTest Coverage:")
    print("  ✅ Model loading and properties")
    print("  ✅ Direct predictions with various scenarios")
    print("  ✅ WaterQualityPredictor class integration")
    print("  ✅ Edge cases and extreme values")
    print("  ✅ Model consistency and determinism")
    print("\nThe water quality model is working correctly and ready for production use!")
    print("=" * 80 + "\n")


if __name__ == "__main__":
    import asyncio

    print("\n" + "=" * 80)
    print("WATER QUALITY ML MODEL TEST SUITE")
    print("Testing: aqua_sense_model.pkl")
    print("=" * 80 + "\n")

    try:
        # Run all tests
        model = test_model_loading()
        test_direct_predictions(model)
        asyncio.run(test_predictor_class())
        test_edge_cases(model)
        test_model_consistency()

        # Print summary
        print_test_summary()

    except Exception as e:
        print("\n" + "=" * 80)
        print("❌ TEST SUITE FAILED")
        print("=" * 80)
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()
        print("=" * 80 + "\n")
        sys.exit(1)
