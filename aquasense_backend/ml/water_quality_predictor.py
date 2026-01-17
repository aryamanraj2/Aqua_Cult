import joblib
import numpy as np
from typing import Dict, Any, Optional
import logging

logger = logging.getLogger(__name__)

class WaterQualityPredictor:
    """ML service for water quality prediction using Random Forest model"""

    def __init__(self):
        self.model = None
        self.model_path = "ml_tank/aqua_sense_model.pkl"
        self.quality_labels = {0: "Excellent", 1: "Good", 2: "Poor"}
        self._load_model()

    def _load_model(self):
        """Load the pickled Random Forest model"""
        try:
            logger.info("=" * 80)
            logger.info("🔧 LOADING ML WATER QUALITY MODEL")
            logger.info(f"📁 Model Path: {self.model_path}")
            logger.info("=" * 80)

            self.model = joblib.load(self.model_path)

            # Log model details
            logger.info(f"✅ Model loaded successfully!")
            logger.info(f"📊 Model Type: {type(self.model).__name__}")
            if hasattr(self.model, 'n_features_in_'):
                logger.info(f"🔢 Number of Features: {self.model.n_features_in_}")
            if hasattr(self.model, 'classes_'):
                logger.info(f"🏷️  Classes: {self.model.classes_} (0=Excellent, 1=Good, 2=Poor)")
            if hasattr(self.model, 'n_estimators'):
                logger.info(f"🌲 Number of Trees: {self.model.n_estimators}")
            logger.info("=" * 80 + "\n")

        except Exception as e:
            logger.error("=" * 80)
            logger.error(f"❌ FAILED TO LOAD ML WATER QUALITY MODEL")
            logger.error(f"📁 Model Path: {self.model_path}")
            logger.error(f"Error: {e}")
            logger.error("=" * 80)
            import traceback
            logger.error(traceback.format_exc())
            self.model = None

    def _prepare_features(self, wq_reading) -> tuple:
        """
        Prepare feature vector from WaterQuality reading.
        Maps tank DB fields to model features and fills missing values with defaults.

        Model expects 14 features in this order:
        Temp, Turbidity__cm_, DO_mg_L_, BOD__mg_L_, CO2, pH,
        Alkalinity__mg_L_1__, Hardness__mg_L_1__, Calcium__mg_L_1__,
        Ammonia__mg_L_1__, Nitrite__mg_L_1__, Phosphorus__mg_L_1__,
        H2S__mg_L_1__, Plankton__No__L_1_

        Returns: (features_array, missing_features_list)
        """
        # Default values for missing parameters (ideal/safe values for aquaculture)
        defaults = {
            'BOD__mg_L_': 3.0,              # Biological Oxygen Demand
            'CO2': 5.0,                     # Carbon Dioxide
            'Alkalinity__mg_L_1__': 100.0,  # Alkalinity
            'Hardness__mg_L_1__': 150.0,    # Water hardness
            'Calcium__mg_L_1__': 60.0,      # Calcium
            'Phosphorus__mg_L_1__': 0.05,   # Phosphorus
            'H2S__mg_L_1__': 0.001,         # Hydrogen Sulfide
            'Plankton__No__L_1_': 5000      # Plankton count
        }

        # Track which features we're using defaults for
        missing = []

        # Build feature array in exact model order
        features = []

        # 1. Temp
        features.append(wq_reading.temperature if wq_reading.temperature is not None else 26.0)
        if wq_reading.temperature is None:
            missing.append('temperature')

        # 2. Turbidity__cm_
        features.append(wq_reading.turbidity if wq_reading.turbidity is not None else 5.0)
        if wq_reading.turbidity is None:
            missing.append('turbidity')

        # 3. DO_mg_L_
        features.append(wq_reading.dissolved_oxygen if wq_reading.dissolved_oxygen is not None else 7.0)
        if wq_reading.dissolved_oxygen is None:
            missing.append('dissolved_oxygen')

        # 4. BOD__mg_L_ (always default - not in tank data)
        features.append(defaults['BOD__mg_L_'])
        missing.append('BOD')

        # 5. CO2 (always default - not in tank data)
        features.append(defaults['CO2'])
        missing.append('CO2')

        # 6. pH
        features.append(wq_reading.ph if wq_reading.ph is not None else 7.5)
        if wq_reading.ph is None:
            missing.append('ph')

        # 7. Alkalinity__mg_L_1__ (always default - not in tank data)
        features.append(defaults['Alkalinity__mg_L_1__'])
        missing.append('Alkalinity')

        # 8. Hardness__mg_L_1__ (always default - not in tank data)
        features.append(defaults['Hardness__mg_L_1__'])
        missing.append('Hardness')

        # 9. Calcium__mg_L_1__ (always default - not in tank data)
        features.append(defaults['Calcium__mg_L_1__'])
        missing.append('Calcium')

        # 10. Ammonia__mg_L_1__
        features.append(wq_reading.ammonia if wq_reading.ammonia is not None else 0.01)
        if wq_reading.ammonia is None:
            missing.append('ammonia')

        # 11. Nitrite__mg_L_1__
        features.append(wq_reading.nitrite if wq_reading.nitrite is not None else 0.01)
        if wq_reading.nitrite is None:
            missing.append('nitrite')

        # 12. Phosphorus__mg_L_1__ (always default - not in tank data)
        features.append(defaults['Phosphorus__mg_L_1__'])
        missing.append('Phosphorus')

        # 13. H2S__mg_L_1__ (always default - not in tank data)
        features.append(defaults['H2S__mg_L_1__'])
        missing.append('H2S')

        # 14. Plankton__No__L_1_ (always default - not in tank data)
        features.append(defaults['Plankton__No__L_1_'])
        missing.append('Plankton')

        return np.array(features).reshape(1, -1), missing

    async def predict(self, wq_reading) -> Optional[Dict[str, Any]]:
        """
        Predict water quality from sensor readings.

        Returns:
            {
                'prediction': 'Excellent|Good|Poor',
                'prediction_class': 0|1|2,
                'confidence': 0.0-1.0,
                'probabilities': {'Excellent': 0.8, 'Good': 0.15, 'Poor': 0.05},
                'features_used': {...},
                'missing_features': [...]
            }
        """
        if not self.model:
            logger.error("❌ ML MODEL NOT LOADED - Cannot make prediction")
            return None

        try:
            logger.info("=" * 80)
            logger.info("🤖 ML WATER QUALITY MODEL CALLED")
            logger.info(f"📁 Model Path: {self.model_path}")
            logger.info("=" * 80)

            # Log input parameters
            logger.info("\n📊 INPUT PARAMETERS (from Tank Database):")
            logger.info(f"  • Temperature: {wq_reading.temperature}°C" if wq_reading.temperature is not None else "  • Temperature: NOT AVAILABLE (will use default)")
            logger.info(f"  • pH: {wq_reading.ph}" if wq_reading.ph is not None else "  • pH: NOT AVAILABLE (will use default)")
            logger.info(f"  • Dissolved Oxygen: {wq_reading.dissolved_oxygen} mg/L" if wq_reading.dissolved_oxygen is not None else "  • Dissolved Oxygen: NOT AVAILABLE (will use default)")
            logger.info(f"  • Turbidity: {wq_reading.turbidity} cm" if wq_reading.turbidity is not None else "  • Turbidity: NOT AVAILABLE (will use default)")
            logger.info(f"  • Ammonia: {wq_reading.ammonia} mg/L" if wq_reading.ammonia is not None else "  • Ammonia: NOT AVAILABLE (will use default)")
            logger.info(f"  • Nitrite: {wq_reading.nitrite} mg/L" if wq_reading.nitrite is not None else "  • Nitrite: NOT AVAILABLE (will use default)")

            # Prepare features
            logger.info("\n⚙️  PREPARING FEATURES (14 total)...")
            features, missing = self._prepare_features(wq_reading)

            # Log which features are measured vs defaults
            measured = [f for f in ['temperature', 'turbidity', 'dissolved_oxygen', 'ph', 'ammonia', 'nitrite'] if f not in missing]
            always_default = ['BOD', 'CO2', 'Alkalinity', 'Hardness', 'Calcium', 'Phosphorus', 'H2S', 'Plankton']

            logger.info(f"\n✅ MEASURED PARAMETERS ({len(measured)}/6):")
            for param in measured:
                logger.info(f"  • {param}")

            logger.info(f"\n⚠️  DEFAULT VALUES USED ({len(missing)}/14):")
            for param in missing:
                if param in always_default:
                    logger.info(f"  • {param} (always default - not tracked in tank DB)")
                else:
                    logger.info(f"  • {param} (missing from this reading)")

            # Predict
            logger.info("\n🔮 RUNNING ML MODEL PREDICTION...")
            prediction_class = self.model.predict(features)[0]
            probabilities = self.model.predict_proba(features)[0]

            # Format result
            result = {
                'prediction': self.quality_labels[prediction_class],
                'prediction_class': int(prediction_class),
                'confidence': float(probabilities[prediction_class]),
                'probabilities': {
                    'Excellent': float(probabilities[0]),
                    'Good': float(probabilities[1]),
                    'Poor': float(probabilities[2])
                },
                'features_used': {
                    'Temp': float(features[0, 0]),
                    'Turbidity__cm_': float(features[0, 1]),
                    'DO_mg_L_': float(features[0, 2]),
                    'BOD__mg_L_': float(features[0, 3]),
                    'CO2': float(features[0, 4]),
                    'pH': float(features[0, 5]),
                    'Alkalinity__mg_L_1__': float(features[0, 6]),
                    'Hardness__mg_L_1__': float(features[0, 7]),
                    'Calcium__mg_L_1__': float(features[0, 8]),
                    'Ammonia__mg_L_1__': float(features[0, 9]),
                    'Nitrite__mg_L_1__': float(features[0, 10]),
                    'Phosphorus__mg_L_1__': float(features[0, 11]),
                    'H2S__mg_L_1__': float(features[0, 12]),
                    'Plankton__No__L_1_': float(features[0, 13])
                },
                'missing_features': missing
            }

            # Log prediction results
            logger.info("\n" + "=" * 80)
            logger.info("✨ ML MODEL PREDICTION RESULTS")
            logger.info("=" * 80)
            logger.info(f"🎯 PREDICTION: {result['prediction']}")
            logger.info(f"📈 CONFIDENCE: {result['confidence']:.2%}")
            logger.info(f"\n📊 PROBABILITY BREAKDOWN:")
            logger.info(f"  • Excellent: {result['probabilities']['Excellent']:.2%}")
            logger.info(f"  • Good: {result['probabilities']['Good']:.2%}")
            logger.info(f"  • Poor: {result['probabilities']['Poor']:.2%}")

            logger.info(f"\n📝 FEATURE VECTOR (14 values):")
            logger.info(f"  [1] Temp: {features[0, 0]:.2f}°C")
            logger.info(f"  [2] Turbidity: {features[0, 1]:.2f} cm")
            logger.info(f"  [3] DO: {features[0, 2]:.2f} mg/L")
            logger.info(f"  [4] BOD: {features[0, 3]:.2f} mg/L (default)")
            logger.info(f"  [5] CO2: {features[0, 4]:.2f} mg/L (default)")
            logger.info(f"  [6] pH: {features[0, 5]:.2f}")
            logger.info(f"  [7] Alkalinity: {features[0, 6]:.2f} mg/L (default)")
            logger.info(f"  [8] Hardness: {features[0, 7]:.2f} mg/L (default)")
            logger.info(f"  [9] Calcium: {features[0, 8]:.2f} mg/L (default)")
            logger.info(f"  [10] Ammonia: {features[0, 9]:.4f} mg/L")
            logger.info(f"  [11] Nitrite: {features[0, 10]:.4f} mg/L")
            logger.info(f"  [12] Phosphorus: {features[0, 11]:.4f} mg/L (default)")
            logger.info(f"  [13] H2S: {features[0, 12]:.4f} mg/L (default)")
            logger.info(f"  [14] Plankton: {features[0, 13]:.0f} No/L (default)")

            logger.info("\n" + "=" * 80)
            logger.info(f"✅ ML PREDICTION COMPLETE - Passing to Gemini AI for validation")
            logger.info("=" * 80 + "\n")

            return result

        except Exception as e:
            logger.error("=" * 80)
            logger.error(f"❌ ERROR IN ML WATER QUALITY PREDICTION")
            logger.error(f"Error: {e}")
            logger.error("=" * 80)
            import traceback
            logger.error(traceback.format_exc())
            return None
