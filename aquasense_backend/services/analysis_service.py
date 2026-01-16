"""
Analysis Service - Disease detection and tank analysis using AI/ML
"""
from sqlalchemy.orm import Session
from typing import List, Optional, Dict
from datetime import datetime

from services.tank_service import TankService
from ai.gemini_client import GeminiClient
from ml.disease_classifier import DiseaseClassifier
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

        detected_diseases = []

        # Use ML model if image is provided
        if image_base64:
            print("=" * 80)
            print("STARTING ML DISEASE PREDICTION...")
            ml_result = await self.disease_classifier.predict(image_base64)
            print(f"ML MODEL RETURNED {len(ml_result)} PREDICTIONS:")
            for disease in ml_result:
                print(f"  - {disease.name}: {disease.confidence:.2%}")
            print("=" * 80)
            detected_diseases.extend(ml_result)

        # Use Gemini AI for comprehensive analysis
        context = {}
        if tank_id:
            tank = self.tank_service.get_tank_by_id(tank_id)
            if tank:
                context["tank_name"] = tank.name
                context["species"] = tank.species

        print("STARTING GEMINI AI ANALYSIS...")
        ai_analysis = await self.gemini_client.analyze_disease(
            image_base64=image_base64,
            symptoms=symptoms,
            context=context
        )
        print(f"GEMINI AI RETURNED: {ai_analysis}")
        print("=" * 80)

        # Merge ML and AI results
        if ai_analysis.get("diseases"):
            print(f"Adding {len(ai_analysis.get('diseases'))} diseases from Gemini")
            detected_diseases.extend(ai_analysis["diseases"])

        # Determine severity
        severity = self._calculate_severity(detected_diseases)

        print(f"FINAL RESULT: {len(detected_diseases)} diseases, severity: {severity}")
        print("=" * 80)

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
        tank = self.tank_service.get_tank_by_id(tank_id)
        if not tank:
            return None

        water_quality_analysis = None
        disease_detection = None
        alerts = []
        general_recommendations = []

        # Analyze water quality
        if include_water_quality:
            latest_wq = self.tank_service.get_latest_water_quality(tank_id)
            if latest_wq:
                water_quality_analysis = await self._analyze_water_quality(latest_wq, tank.species)
                if water_quality_analysis.status in ["poor", "critical"]:
                    alerts.extend(water_quality_analysis.issues)
            else:
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
        Analyze water quality parameters against optimal ranges.
        """
        # Use Gemini to analyze water quality
        analysis = await self.gemini_client.analyze_water_quality(
            wq_reading=wq_reading,
            species=species
        )

        return WaterQualityAnalysis(
            status=analysis.get("status", "unknown"),
            issues=analysis.get("issues", []),
            recommendations=analysis.get("recommendations", []),
            parameters=analysis.get("parameters", {})
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
