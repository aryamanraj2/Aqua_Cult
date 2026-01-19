"""
Analysis Service - Disease detection and tank analysis using AI/ML
"""
from sqlalchemy.orm import Session
from typing import List, Optional, Dict
from datetime import datetime

from services.tank_service import TankService
from ai.gemini_client import GeminiClient
from ml.disease_classifier import DiseaseClassifier
from ml.water_quality_predictor import WaterQualityPredictor
from schemas.analysis import (
    DiseaseInfo,
    DiseaseDetectionResponse,
    WaterQualityAnalysis,
    TankAnalysisResponse,
    AIRecommendationResponse
)


class AnalysisService:
    """Service class for analysis operations"""

    def __init__(self, db: Session):
        self.db = db
        self.tank_service = TankService(db)
        self.gemini_client = GeminiClient()
        self.disease_classifier = DiseaseClassifier()
        self.water_quality_predictor = WaterQualityPredictor()

    async def detect_disease(
        self,
        image_base64: Optional[str] = None,
        symptoms: Optional[List[str]] = None,
        tank_id: Optional[str] = None
    ) -> DiseaseDetectionResponse:
        """
        Detect fish diseases from image and/or symptoms.
        """
        import logging
        logger = logging.getLogger(__name__)

        logger.info("\n" + "🔬" * 40)
        logger.info("🦠 DISEASE DETECTION SERVICE STARTED")
        logger.info(f"📸 Has Image: {image_base64 is not None}")
        logger.info(f"📝 Has Symptoms: {symptoms is not None}")
        logger.info(f"🏊 Tank ID: {tank_id}")
        logger.info("🔬" * 40)

        detected_diseases = []

        # Use ML model if image is provided
        if image_base64:
            logger.info("\n📍 STEP 1: Running ML Disease Model...")
            logger.info(f"Image size: {len(image_base64)} characters (base64)")

            try:
                ml_result = await self.disease_classifier.predict(image_base64)
                logger.info(f"✅ ML MODEL RETURNED {len(ml_result)} PREDICTIONS:")
                for disease in ml_result:
                    logger.info(f"  • {disease.name}: {disease.confidence:.2%}")
                detected_diseases.extend(ml_result)
            except Exception as e:
                logger.error(f"❌ ML model prediction failed: {e}")
                import traceback
                logger.error(traceback.format_exc())
        else:
            logger.info("\n📍 STEP 1: Skipping ML model (no image provided)")

        # Use Gemini AI for comprehensive analysis
        context = {}
        if tank_id:
            tank = self.tank_service.get_tank_by_id(tank_id)
            if tank:
                context["tank_name"] = tank.name
                context["species"] = tank.species
                logger.info(f"\n🏊 Tank Context: {tank.name} (Species: {tank.species})")

        logger.info("\n📍 STEP 2: Calling Gemini AI for analysis...")
        try:
            ai_analysis = await self.gemini_client.analyze_disease(
                image_base64=image_base64,
                symptoms=symptoms,
                context=context
            )
            logger.info(f"✅ GEMINI AI ANALYSIS COMPLETE")
            logger.info(f"Recommendation: {ai_analysis.get('recommendation', 'None')[:100]}...")

            # Merge ML and AI results
            if ai_analysis.get("diseases"):
                logger.info(f"Adding {len(ai_analysis.get('diseases'))} diseases from Gemini")
                detected_diseases.extend(ai_analysis["diseases"])
        except Exception as e:
            logger.error(f"❌ Gemini AI analysis failed: {e}")
            import traceback
            logger.error(traceback.format_exc())

        # Determine severity
        severity = self._calculate_severity(detected_diseases)

        logger.info("\n" + "=" * 80)
        logger.info("✨ DISEASE DETECTION COMPLETE")
        logger.info(f"📊 Total Diseases Detected: {len(detected_diseases)}")
        logger.info(f"⚠️  Severity: {severity.upper()}")
        logger.info("=" * 80 + "\n")

        return DiseaseDetectionResponse(
            detected_diseases=detected_diseases,
            recommendation=ai_analysis.get("recommendation", "No specific recommendations at this time."),
            severity=severity,
            urgent_action_required=severity in ["high", "critical"],
            timestamp=datetime.utcnow()
        )

    async def analyze_tank(
        self,
        tank_id: str,
        include_water_quality: bool = True,
        include_disease_check: bool = False,
        fish_image_base64: Optional[str] = None
    ) -> Optional[TankAnalysisResponse]:
        """
        Perform comprehensive tank analysis.
        """
        import logging
        logger = logging.getLogger(__name__)

        logger.info("\n" + "🐟" * 40)
        logger.info("🏊 TANK ANALYSIS SERVICE STARTED")
        logger.info(f"🆔 Tank ID: {tank_id}")
        logger.info(f"💧 Include Water Quality: {include_water_quality}")
        logger.info(f"🦠 Include Disease Check: {include_disease_check}")
        logger.info("🐟" * 40)

        tank = self.tank_service.get_tank_by_id(tank_id)
        if not tank:
            logger.error(f"❌ Tank not found: {tank_id}")
            return None

        logger.info(f"✅ Tank found: {tank.name}")
        logger.info(f"🐠 Species: {tank.species}")
        logger.info(f"📏 Capacity: {tank.capacity}L, Current Stock: {tank.current_stock}")

        water_quality_analysis = None
        disease_detection = None
        alerts = []
        general_recommendations = []

        # Analyze water quality
        if include_water_quality:
            logger.info("\n📍 Checking for water quality data...")
            latest_wq = self.tank_service.get_latest_water_quality(tank_id)

            if latest_wq:
                logger.info("✅ Water quality reading found!")
                logger.info(f"   • Temperature: {latest_wq.temperature}°C")
                logger.info(f"   • pH: {latest_wq.ph}")
                logger.info(f"   • DO: {latest_wq.dissolved_oxygen} mg/L")
                logger.info(f"   • Turbidity: {latest_wq.turbidity} cm")
                logger.info(f"   • Ammonia: {latest_wq.ammonia} mg/L")
                logger.info(f"   • Nitrite: {latest_wq.nitrite} mg/L")

                water_quality_analysis = await self._analyze_water_quality(latest_wq, tank.species)
                if water_quality_analysis.status in ["poor", "critical"]:
                    alerts.extend(water_quality_analysis.issues)
            else:
                logger.warning("⚠️  NO WATER QUALITY DATA FOUND IN DATABASE!")
                logger.warning("   The tank has no water quality readings.")
                logger.warning("   ML water quality model will NOT be called.")
                logger.warning("   Please add water quality readings to the tank.")
                alerts.append("No water quality data available. Please add readings.")

        # Analyze for diseases
        if include_disease_check and fish_image_base64:
            disease_detection = await self.detect_disease(
                image_base64=fish_image_base64,
                tank_id=tank_id
            )
            if disease_detection.urgent_action_required:
                alerts.append("Urgent: Possible disease detected. Check recommendations.")

        # Calculate overall health score
        health_score = self._calculate_health_score(water_quality_analysis, disease_detection)

        # Get AI recommendations
        ai_recs = await self.gemini_client.get_tank_recommendations(
            tank_info={
                "name": tank.name,
                "species": tank.species,
                "capacity": tank.capacity,
                "current_stock": tank.current_stock
            },
            water_quality=water_quality_analysis,
            disease_info=disease_detection
        )
        general_recommendations = ai_recs.get("recommendations", [])

        return TankAnalysisResponse(
            tank_id=tank_id,
            tank_name=tank.name,
            overall_health_score=health_score,
            water_quality_analysis=water_quality_analysis,
            disease_detection=disease_detection,
            general_recommendations=general_recommendations,
            alerts=alerts,
            timestamp=datetime.utcnow()
        )

    async def get_ai_recommendation(
        self,
        query: str,
        context: Optional[Dict] = None
    ) -> AIRecommendationResponse:
        """
        Get AI-powered recommendations for aquaculture questions.
        """
        recommendation = await self.gemini_client.get_recommendation(query, context)

        return AIRecommendationResponse(
            recommendation=recommendation.get("answer", ""),
            sources=recommendation.get("sources"),
            confidence=recommendation.get("confidence"),
            timestamp=datetime.utcnow()
        )

    async def _analyze_water_quality(self, wq_reading, species: List[str]) -> WaterQualityAnalysis:
        """
        Analyze water quality parameters using ML model + Gemini AI.
        """
        import logging
        logger = logging.getLogger(__name__)

        logger.info("\n" + "🌊" * 40)
        logger.info("🔬 STARTING WATER QUALITY ANALYSIS")
        logger.info(f"🐟 Species: {', '.join(species)}")
        logger.info("🌊" * 40)

        # Step 1: Get ML prediction
        logger.info("\n📍 STEP 1: Calling ML Water Quality Model...")
        ml_prediction = await self.water_quality_predictor.predict(wq_reading)

        # Step 2: Use ML-enhanced analysis or fallback
        if ml_prediction:
            # Format missing parameters note
            missing = ml_prediction['missing_features']
            # Separate always-default vs. optionally-measured
            always_default = ['BOD', 'CO2', 'Alkalinity', 'Hardness', 'Calcium', 'Phosphorus', 'H2S', 'Plankton']
            not_measured = [m for m in missing if m not in always_default]

            missing_note = "\nIMPORTANT: This ML prediction used default values for parameters not tracked in your tank system:"
            missing_note += f"\n- Always defaults: {', '.join(always_default)}"
            if not_measured:
                missing_note += f"\n- Not measured this time: {', '.join(not_measured)}"
            missing_note += "\n\nFor more accurate ML predictions, consider adding sensors for BOD, CO2, Alkalinity, Hardness, Calcium, Phosphorus, H2S, and Plankton count."

            # Send ML prediction + raw parameters to Gemini
            logger.info("\n📍 STEP 2: Sending ML prediction to Gemini AI for validation...")
            logger.info(f"🤖 ML Prediction: {ml_prediction['prediction']} ({ml_prediction['confidence']:.2%} confidence)")
            logger.info("🧠 Gemini AI will validate this prediction against measured parameters...")

            analysis = await self.gemini_client.analyze_water_quality_with_ml(
                wq_reading=wq_reading,
                species=species,
                ml_prediction=ml_prediction,
                missing_note=missing_note
            )

            logger.info("\n✅ ML-Enhanced Analysis Complete!")
            logger.info(f"📊 Final Status: {analysis.get('status', 'unknown')}")

        else:
            # Fallback to Gemini-only analysis if ML fails
            logger.warning("\n⚠️  ML prediction failed, using Gemini-only analysis")
            logger.warning("📍 STEP 2: Using standard Gemini AI analysis (no ML prediction)")

            analysis = await self.gemini_client.analyze_water_quality(
                wq_reading=wq_reading,
                species=species
            )

            logger.info("\n✅ Gemini-Only Analysis Complete!")
            logger.info(f"📊 Final Status: {analysis.get('status', 'unknown')}")

        logger.info("\n" + "🌊" * 40)
        logger.info("✨ WATER QUALITY ANALYSIS FINISHED")
        logger.info("🌊" * 40 + "\n")

        return WaterQualityAnalysis(
            status=analysis.get("status", "unknown"),
            issues=analysis.get("issues", []),
            recommendations=analysis.get("recommendations", []),
            parameters=analysis.get("parameters", {}),
            ml_prediction=analysis.get("ml_prediction")
        )

    def _calculate_severity(self, diseases: List[DiseaseInfo]) -> str:
        """
        Calculate overall severity from detected diseases.
        """
        if not diseases:
            return "low"

        max_confidence = max((d.confidence for d in diseases), default=0)

        if max_confidence >= 0.8:
            return "critical"
        elif max_confidence >= 0.6:
            return "high"
        elif max_confidence >= 0.4:
            return "medium"
        else:
            return "low"

    def _calculate_health_score(
        self,
        water_quality: Optional[WaterQualityAnalysis],
        disease_detection: Optional[DiseaseDetectionResponse]
    ) -> float:
        """
        Calculate overall tank health score (0-100).
        """
        score = 100.0

        # Deduct points for water quality issues
        if water_quality:
            status_penalties = {
                "excellent": 0,
                "good": 5,
                "fair": 15,
                "poor": 30,
                "critical": 50
            }
            score -= status_penalties.get(water_quality.status, 20)

        # Deduct points for diseases
        if disease_detection:
            severity_penalties = {
                "low": 5,
                "medium": 15,
                "high": 30,
                "critical": 50
            }
            score -= severity_penalties.get(disease_detection.severity, 10)

        return max(0, min(100, score))
