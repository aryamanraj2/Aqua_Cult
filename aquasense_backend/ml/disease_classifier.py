"""
Disease Classifier - ML model for fish disease detection
"""
import os
import base64
import io
import json
import logging
from typing import List, Optional
from PIL import Image
import numpy as np

from config.settings import settings
from ml.preprocessing import preprocess_image
from schemas.analysis import DiseaseInfo

logger = logging.getLogger(__name__)


class DiseaseClassifier:
    """
    Fish disease classifier using TensorFlow/Keras model.
    Model is provided by the ML team.
    """

    def __init__(self):
        """Initialize the disease classifier"""
        self.model = None
        self.model_path = settings.DISEASE_MODEL_PATH
        self.label_map_path = os.path.join(os.path.dirname(self.model_path), "label_map.json")
        self.label_map = self._load_label_map()
        self.disease_info = self._load_disease_info()

        # Load model if it exists
        if os.path.exists(self.model_path):
            self._load_model()
        else:
            logger.warning(f"Disease model not found at {self.model_path}. Model inference will be skipped.")

    def _load_model(self):
        """Load the Keras model"""
        try:
            import tensorflow as tf
            self.model = tf.keras.models.load_model(self.model_path)
            logger.info("Disease classification model loaded successfully")
        except Exception as e:
            logger.error(f"Failed to load model: {str(e)}")
            self.model = None

    def _load_label_map(self) -> dict:
        """
        Load label map from JSON file.
        Maps class indices to disease names.
        """
        if os.path.exists(self.label_map_path):
            try:
                with open(self.label_map_path, 'r') as f:
                    label_map = json.load(f)
                logger.info(f"Label map loaded successfully with {len(label_map)} classes")
                return label_map
            except Exception as e:
                logger.error(f"Error loading label map: {str(e)}")
                return self._get_default_label_map()
        else:
            logger.warning(f"Label map not found at {self.label_map_path}. Using default mapping.")
            return self._get_default_label_map()

    def _get_default_label_map(self) -> dict:
        """
        Get default label map if file not found.
        """
        return {
            "0": "Bacterial Red disease",
            "1": "Bacterial diseases - Aeromoniasis",
            "2": "Bacterial gill disease",
            "3": "Fungal diseases Saprolegniasis",
            "4": "Healthy Fish",
            "5": "Parasitic diseases",
            "6": "Viral diseases White tail disease"
        }

    def _load_disease_info(self) -> dict:
        """
        Load disease information mapping.
        Maps disease names from label_map to detailed disease information.
        """
        return {
            "Bacterial Red disease": {
                "name": "Bacterial Red Disease",
                "description": "Bacterial hemorrhagic septicemia causing red lesions and hemorrhaging. Common bacterial infection in fish.",
                "causes": ["Aeromonas bacteria", "Poor water quality", "Stress", "Physical injuries", "Weakened immune system"],
                "symptoms": ["Red lesions on body", "Hemorrhages on skin and fins", "Ulcers", "Lethargy", "Loss of appetite", "Abnormal swimming"],
                "treatment": "Quarantine infected fish immediately. Use antibiotics (oxytetracycline or florfenicol) as prescribed. Improve water quality. Increase aeration. Salt bath (1-3%).",
                "prevention": ["Maintain excellent water quality", "Regular water changes", "Avoid overcrowding", "Quarantine new fish", "Reduce stress factors", "Proper nutrition"]
            },
            "Bacterial diseases - Aeromoniasis": {
                "name": "Aeromoniasis (Motile Aeromonas Septicemia)",
                "description": "Systemic bacterial infection caused by Aeromonas species, leading to septicemia and organ damage.",
                "causes": ["Aeromonas hydrophila bacteria", "Poor water quality", "Stress", "High organic load", "Temperature fluctuations"],
                "symptoms": ["Hemorrhages on body", "Fin rot", "Ulcers", "Swollen abdomen", "Exophthalmia (bulging eyes)", "Lethargy", "Loss of appetite"],
                "treatment": "Antibiotic treatment (oxytetracycline, sulfonamides). Improve water quality immediately. Increase dissolved oxygen. Reduce stocking density. Salt treatment.",
                "prevention": ["Maintain optimal water parameters", "Regular monitoring", "Avoid stress", "Proper feeding", "Quarantine protocols", "Disinfect equipment"]
            },
            "Bacterial gill disease": {
                "name": "Bacterial Gill Disease",
                "description": "Bacterial infection affecting gill tissue, impairing respiratory function and leading to oxygen deprivation.",
                "causes": ["Flavobacterium branchiophilum", "Poor water quality", "High ammonia levels", "Overcrowding", "Low dissolved oxygen"],
                "symptoms": ["Rapid gill movement", "Gasping at surface", "Pale or swollen gills", "Excess mucus on gills", "Lethargy", "Reduced feeding", "Mortality"],
                "treatment": "Improve water quality immediately. Increase aeration. Reduce feeding temporarily. Antibiotics (chloramine-T, hydrogen peroxide bath). Copper sulfate treatment (carefully dosed).",
                "prevention": ["Maintain high dissolved oxygen (>5 mg/L)", "Keep ammonia at 0 ppm", "Regular water changes", "Avoid overcrowding", "Proper filtration", "Monitor water parameters"]
            },
            "Fungal diseases Saprolegniasis": {
                "name": "Saprolegniasis (Fungal Infection)",
                "description": "Fungal infection caused by Saprolegnia species, appearing as cotton-like growth on fish body, fins, or eggs.",
                "causes": ["Saprolegnia fungus (opportunistic)", "Physical injury", "Poor water quality", "Low temperature", "Weakened immune system", "Stress"],
                "symptoms": ["White or gray cotton-like growth", "Fluffy patches on body/fins", "Fin deterioration", "Often follows injury or stress", "Secondary to bacterial infections"],
                "treatment": "Salt bath (0.5-1% for 10-15 minutes). Antifungal medication (malachite green, methylene blue, potassium permanganate). Improve water quality. Remove dead tissue if severe.",
                "prevention": ["Avoid physical injuries", "Maintain good water quality", "Proper nutrition", "Handle fish carefully", "Quarantine injured fish", "Maintain optimal temperature"]
            },
            "Healthy Fish": {
                "name": "Healthy Fish - No Disease Detected",
                "description": "Fish appears healthy with no visible signs of disease or distress. Continue regular monitoring and maintenance.",
                "causes": [],
                "symptoms": ["Active swimming", "Normal appetite", "Bright coloration", "Clear eyes", "Intact fins", "Normal gill movement"],
                "treatment": "No treatment needed. Continue regular monitoring and maintenance practices.",
                "prevention": ["Maintain optimal water quality", "Regular feeding schedule", "Avoid overcrowding", "Monitor water parameters weekly", "Quarantine new fish", "Reduce stress factors"]
            },
            "Parasitic diseases": {
                "name": "Parasitic Infections",
                "description": "External or internal parasites affecting fish health, including protozoa, worms, and crustaceans.",
                "causes": ["Ichthyophthirius (Ich)", "Trichodina", "Gyrodactylus", "Dactylogyrus", "Anchor worms", "Fish lice", "Introduction of infected fish", "Poor quarantine"],
                "symptoms": ["White spots on body (Ich)", "Scratching/flashing behavior", "Excess mucus production", "Rapid gill movement", "Weight loss", "Lethargy", "Clamped fins"],
                "treatment": "Identify specific parasite. For Ich: raise temperature to 30°C, salt treatment (1-2%), copper sulfate. For external parasites: formalin bath, potassium permanganate. For internal parasites: medicated feed.",
                "prevention": ["Quarantine new fish for 2-3 weeks", "Disinfect equipment", "Regular inspection", "Maintain water quality", "Avoid stress", "Clean equipment between uses"]
            },
            "Viral diseases White tail disease": {
                "name": "Viral White Tail Disease",
                "description": "Viral infection causing white discoloration of the tail and systemic illness. Highly contagious and often fatal.",
                "causes": ["Viral pathogen (species-specific virus)", "Infected fish introduction", "Contaminated water", "Stress", "Poor biosecurity"],
                "symptoms": ["White discoloration of tail", "Tail necrosis", "Lethargy", "Loss of appetite", "Abnormal swimming", "High mortality rate", "Systemic infection"],
                "treatment": "No specific antiviral treatment. Supportive care only. Quarantine infected fish immediately. Cull severely affected fish to prevent spread. Disinfect all equipment and tanks.",
                "prevention": ["Source fish from certified disease-free suppliers", "Strict quarantine protocols (3-4 weeks)", "Excellent biosecurity", "Do not share equipment between tanks", "Maintain optimal conditions to boost immunity", "Vaccination if available"]
            }
        }

    async def predict(self, image_base64: str) -> List[DiseaseInfo]:
        """
        Predict disease from fish image.

        Args:
            image_base64: Base64 encoded image

        Returns:
            List of DiseaseInfo objects with predictions
        """
        if not self.model:
            logger.warning("Model not loaded. Returning empty predictions.")
            return []

        try:
            # Decode and preprocess image
            image_bytes = base64.b64decode(image_base64)
            image = Image.open(io.BytesIO(image_bytes))
            processed_image = preprocess_image(image)

            # Make prediction
            predictions = self.model.predict(processed_image)

            # Convert predictions to DiseaseInfo objects
            results = self._predictions_to_disease_info(predictions[0])

            return results

        except Exception as e:
            logger.error(f"Error in disease prediction: {str(e)}")
            return []

    def _predictions_to_disease_info(
        self,
        predictions: np.ndarray,
        confidence_threshold: float = 0.2
    ) -> List[DiseaseInfo]:
        """
        Convert model predictions to DiseaseInfo objects.

        Args:
            predictions: Model output probabilities
            confidence_threshold: Minimum confidence to include in results

        Returns:
            List of DiseaseInfo objects
        """
        results = []

        # Get top predictions
        sorted_indices = np.argsort(predictions)[::-1]

        # Log raw ML predictions before filtering
        print("\nRAW ML MODEL OUTPUT (sorted by confidence):")
        for i, idx in enumerate(sorted_indices[:5], 1):  # Show top 5
            disease_name = self.label_map.get(str(idx), "Unknown")
            confidence = float(predictions[idx])
            print(f"  {i}. [{idx}] {disease_name}: {confidence:.2%}")
        print()

        for idx in sorted_indices:
            confidence = float(predictions[idx])

            # Skip if below threshold
            if confidence < confidence_threshold:
                print(f"  ⊘ Skipped (below threshold {confidence_threshold}): confidence {confidence:.2%}")
                continue

            # Get disease name from label map
            disease_name = self.label_map.get(str(idx))

            if not disease_name:
                logger.warning(f"No label found for index {idx}")
                continue

            # Skip "Healthy Fish" unless it has very high confidence
            if disease_name == "Healthy Fish" and confidence < 0.7:
                print(f"  ⊘ Skipped '{disease_name}': {confidence:.2%} (< 70% threshold for healthy)")
                continue

            # Get detailed disease information
            disease_data = self.disease_info.get(disease_name, {})

            if not disease_data:
                # Fallback if no detailed info available
                disease_data = {
                    "name": disease_name,
                    "description": f"Detected {disease_name}",
                    "causes": [],
                    "symptoms": [],
                    "treatment": "Consult aquaculture veterinarian for specific treatment.",
                    "prevention": ["Maintain good water quality", "Regular monitoring"]
                }

            disease_info = DiseaseInfo(
                name=disease_data.get("name", disease_name),
                confidence=confidence,
                description=disease_data.get("description", ""),
                causes=disease_data.get("causes", []),
                symptoms=disease_data.get("symptoms", []),
                treatment=disease_data.get("treatment", ""),
                prevention=disease_data.get("prevention", [])
            )

            results.append(disease_info)
            print(f"  ✓ Added to results: {disease_name} ({confidence:.2%})")

            # Limit to top 3 results
            if len(results) >= 3:
                break

        print(f"\nFINAL FILTERED RESULTS: {len(results)} diseases returned")
        if results:
            print(f"FIRST RESULT (sent to iOS app): {results[0].name} ({results[0].confidence:.2%})")
        print()

        return results

    def get_disease_info(self, disease_name: str) -> Optional[dict]:
        """
        Get detailed information about a specific disease.

        Args:
            disease_name: Name of the disease

        Returns:
            Disease information dictionary or None
        """
        return self.disease_info.get(disease_name)
