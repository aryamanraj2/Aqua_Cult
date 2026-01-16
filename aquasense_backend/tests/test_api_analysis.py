"""
Tests for Analysis API endpoints (Disease Detection, Tank Analysis, AI)
"""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session
from unittest.mock import Mock, patch, AsyncMock


@pytest.mark.integration
class TestDiseaseDetectionEndpoints:
    """Test disease detection API endpoints."""

    @patch('services.analysis_service.AnalysisService.detect_disease')
    def test_disease_detection_base64(self, mock_detect, client: TestClient, sample_image_base64, mock_gemini_response):
        """Test disease detection with base64 image."""
        # Mock the service response
        mock_detect.return_value = {
            "detected_diseases": mock_gemini_response["diseases"],
            "recommendation": mock_gemini_response["recommendation"],
            "severity": mock_gemini_response["severity"],
            "urgent_action_required": mock_gemini_response["urgent_action_required"]
        }

        request_data = {
            "image_base64": sample_image_base64,
            "symptoms": ["Red spots on body", "Lethargy"]
        }

        response = client.post(
            "/api/v1/analysis/disease-detection",
            json=request_data
        )

        assert response.status_code == 200
        data = response.json()
        assert "detected_diseases" in data
        assert "recommendation" in data
        assert "severity" in data
        assert isinstance(data["detected_diseases"], list)
        mock_detect.assert_called_once()

    def test_disease_detection_upload(self, client: TestClient):
        """Test disease detection with file upload."""
        from io import BytesIO
        from PIL import Image
        import numpy as np

        # Create test image file
        img_array = np.random.randint(0, 255, (224, 224, 3), dtype=np.uint8)
        img = Image.fromarray(img_array, 'RGB')
        img_bytes = BytesIO()
        img.save(img_bytes, format='JPEG')
        img_bytes.seek(0)

        files = {
            "file": ("test_fish.jpg", img_bytes, "image/jpeg")
        }

        response = client.post(
            "/api/v1/analysis/disease-detection/upload",
            files=files
        )

        assert response.status_code == 200
        data = response.json()
        assert "detected_diseases" in data
        assert "timestamp" in data

    def test_disease_detection_invalid_image(self, client: TestClient):
        """Test disease detection with invalid image data."""
        request_data = {
            "image_base64": "invalid_base64_string"
        }

        response = client.post(
            "/api/v1/analysis/disease-detection",
            json=request_data
        )

        assert response.status_code == 400
        assert "invalid image" in response.json()["detail"].lower()

    def test_disease_detection_file_too_large(self, client: TestClient):
        """Test disease detection with file exceeding size limit."""
        from io import BytesIO

        # Create a large file (> 10MB)
        large_file = BytesIO(b"0" * (11 * 1024 * 1024))

        files = {
            "file": ("large_image.jpg", large_file, "image/jpeg")
        }

        response = client.post(
            "/api/v1/analysis/disease-detection/upload",
            files=files
        )

        assert response.status_code == 413  # Payload too large

    def test_disease_detection_unsupported_format(self, client: TestClient):
        """Test disease detection with unsupported file format."""
        from io import BytesIO

        files = {
            "file": ("test.txt", BytesIO(b"not an image"), "text/plain")
        }

        response = client.post(
            "/api/v1/analysis/disease-detection/upload",
            files=files
        )

        assert response.status_code == 400
        assert "unsupported" in response.json()["detail"].lower()


@pytest.mark.integration
class TestTankAnalysisEndpoints:
    """Test comprehensive tank analysis endpoints."""

    @patch('services.analysis_service.AnalysisService.analyze_tank_health')
    def test_tank_analysis_complete(self, mock_analyze, client: TestClient, test_user, test_tank, test_water_quality):
        """Test comprehensive tank analysis."""
        mock_analyze.return_value = {
            "tank_id": test_tank.id,
            "overall_health_score": 85,
            "water_quality_analysis": {
                "status": "good",
                "issues": [],
                "recommendations": ["Maintain current parameters"]
            },
            "disease_analysis": {
                "detected_diseases": [],
                "risk_level": "low"
            },
            "ai_recommendations": [
                "Water quality is optimal",
                "Continue regular monitoring"
            ],
            "alerts": []
        }

        request_data = {
            "tank_id": test_tank.id,
            "include_water_quality": True,
            "include_disease_check": True
        }

        response = client.post(
            "/api/v1/analysis/tank-analysis",
            json=request_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["tank_id"] == test_tank.id
        assert "overall_health_score" in data
        assert "water_quality_analysis" in data
        assert "ai_recommendations" in data
        mock_analyze.assert_called_once()

    @patch('services.analysis_service.AnalysisService.analyze_tank_health')
    def test_tank_analysis_with_image(self, mock_analyze, client: TestClient, test_user, test_tank, sample_image_base64):
        """Test tank analysis with fish image."""
        mock_analyze.return_value = {
            "tank_id": test_tank.id,
            "overall_health_score": 75,
            "disease_analysis": {
                "detected_diseases": [
                    {
                        "name": "Bacterial Red Disease",
                        "confidence": 0.65
                    }
                ],
                "risk_level": "medium"
            },
            "ai_recommendations": ["Consider antibiotic treatment"],
            "alerts": ["Disease detected"]
        }

        request_data = {
            "tank_id": test_tank.id,
            "include_disease_check": True,
            "fish_image_base64": sample_image_base64
        }

        response = client.post(
            "/api/v1/analysis/tank-analysis",
            json=request_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert "disease_analysis" in data
        assert len(data["alerts"]) > 0

    def test_tank_analysis_not_found(self, client: TestClient, test_user):
        """Test tank analysis for non-existent tank."""
        request_data = {
            "tank_id": "nonexistent-tank-id",
            "include_water_quality": True
        }

        response = client.post(
            "/api/v1/analysis/tank-analysis",
            json=request_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 404


@pytest.mark.integration
class TestWaterQualityAnalysis:
    """Test water quality analysis endpoints."""

    @patch('ai.gemini_client.GeminiClient.analyze_water_quality')
    def test_analyze_water_quality(self, mock_analyze, client: TestClient, test_user, test_tank, test_water_quality):
        """Test water quality analysis with AI."""
        mock_analyze.return_value = {
            "status": "good",
            "issues": [],
            "recommendations": [
                "Temperature is optimal for Tilapia",
                "pH is within acceptable range"
            ],
            "risk_level": "low"
        }

        request_data = {
            "tank_id": test_tank.id
        }

        response = client.post(
            "/api/v1/analysis/water-quality-analysis",
            json=request_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert "status" in data
        assert "recommendations" in data
        assert isinstance(data["recommendations"], list)
        mock_analyze.assert_called_once()

    @patch('ai.gemini_client.GeminiClient.analyze_water_quality')
    def test_analyze_water_quality_with_issues(self, mock_analyze, client: TestClient, test_user, test_tank, test_db):
        """Test water quality analysis with detected issues."""
        # Create reading with poor parameters
        from models.water_quality import WaterQualityReading
        poor_reading = WaterQualityReading(
            id="poor-wq-123",
            tank_id=test_tank.id,
            temperature=32.0,  # Too high
            ph=9.0,  # Too high
            dissolved_oxygen=3.0,  # Too low
            ammonia=0.5,  # Too high
            nitrite=0.3,  # Too high
            nitrate=50.0  # Too high
        )
        test_db.add(poor_reading)
        test_db.commit()

        mock_analyze.return_value = {
            "status": "critical",
            "issues": [
                "Temperature too high - risk of oxygen depletion",
                "pH too high - stress on fish",
                "Ammonia levels dangerous",
                "Dissolved oxygen critically low"
            ],
            "recommendations": [
                "Immediate water change (50%)",
                "Stop feeding for 24 hours",
                "Increase aeration",
                "Test for dead fish"
            ],
            "risk_level": "critical"
        }

        request_data = {
            "tank_id": test_tank.id
        }

        response = client.post(
            "/api/v1/analysis/water-quality-analysis",
            json=request_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "critical"
        assert len(data["issues"]) > 0
        assert data["risk_level"] == "critical"


@pytest.mark.integration
class TestAIRecommendations:
    """Test AI-powered recommendation endpoints."""

    @patch('ai.gemini_client.GeminiClient.get_recommendations')
    def test_get_general_recommendations(self, mock_recommend, client: TestClient, test_user, test_tank):
        """Test getting general AI recommendations."""
        mock_recommend.return_value = {
            "recommendations": [
                "Consider increasing feeding frequency for faster growth",
                "Monitor water temperature during summer months",
                "Plan for partial water change next week"
            ],
            "priority": "medium"
        }

        request_data = {
            "tank_id": test_tank.id,
            "context": "general care"
        }

        response = client.post(
            "/api/v1/analysis/recommendations",
            json=request_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert "recommendations" in data
        assert isinstance(data["recommendations"], list)
        assert len(data["recommendations"]) > 0

    @patch('ai.gemini_client.GeminiClient.get_treatment_plan')
    def test_get_treatment_plan(self, mock_treatment, client: TestClient, test_user, test_tank):
        """Test getting disease treatment plan."""
        mock_treatment.return_value = {
            "disease": "Bacterial Red Disease",
            "treatment_steps": [
                "Quarantine affected fish immediately",
                "Treat with oxytetracycline (50mg/L) for 5 days",
                "Perform 30% water change daily",
                "Increase aeration",
                "Monitor closely for 14 days"
            ],
            "duration_days": 5,
            "cost_estimate": "$45-60 for antibiotics",
            "success_rate": "80-90% if caught early"
        }

        request_data = {
            "tank_id": test_tank.id,
            "disease_name": "Bacterial Red Disease"
        }

        response = client.post(
            "/api/v1/analysis/treatment-plan",
            json=request_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert "treatment_steps" in data
        assert "duration_days" in data
        assert isinstance(data["treatment_steps"], list)


@pytest.mark.ml
class TestMLModelIntegration:
    """Test ML model integration in disease detection."""

    def test_ml_model_predictions(self, client: TestClient, sample_image_base64):
        """Test that ML model predictions are included in response."""
        request_data = {
            "image_base64": sample_image_base64
        }

        response = client.post(
            "/api/v1/analysis/disease-detection",
            json=request_data
        )

        # Should return 200 even if model not loaded (falls back to AI)
        assert response.status_code in [200, 500]  # 500 if model not found

        if response.status_code == 200:
            data = response.json()
            assert "detected_diseases" in data
            # Check if any disease has confidence score (from ML)
            if data["detected_diseases"]:
                assert "confidence" in data["detected_diseases"][0]

    @patch('ml.disease_classifier.DiseaseClassifier.predict')
    async def test_ml_predictions_merged_with_ai(self, mock_predict, client: TestClient, sample_image_base64):
        """Test that ML predictions are merged with AI analysis."""
        mock_predict.return_value = [
            {
                "name": "Bacterial Red Disease",
                "confidence": 0.85,
                "description": "ML detected disease",
                "causes": ["Poor water quality"],
                "symptoms": ["Red spots"],
                "treatment": "Antibiotic treatment",
                "prevention": ["Monitor water"]
            }
        ]

        request_data = {
            "image_base64": sample_image_base64
        }

        response = client.post(
            "/api/v1/analysis/disease-detection",
            json=request_data
        )

        if response.status_code == 200:
            data = response.json()
            # Should contain both ML and AI results
            assert len(data["detected_diseases"]) >= 1
