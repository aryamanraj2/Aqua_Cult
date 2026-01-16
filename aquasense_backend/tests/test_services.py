"""
Tests for Service Layer (Business Logic)
"""
import pytest
from unittest.mock import Mock, patch, AsyncMock
from sqlalchemy.orm import Session

from services.tank_service import TankService
from services.product_service import ProductService
from services.analysis_service import AnalysisService
from services.voice_service import VoiceService


@pytest.mark.unit
class TestTankService:
    """Test tank service business logic."""

    @pytest.fixture
    def tank_service(self, test_db):
        """Create tank service instance."""
        return TankService(test_db)

    def test_get_all_tanks(self, tank_service, test_tank):
        """Test retrieving all tanks for a user."""
        tanks = tank_service.get_all_tanks(test_tank.user_id)

        assert len(tanks) >= 1
        assert tanks[0].id == test_tank.id
        assert tanks[0].name == test_tank.name

    def test_get_tank_by_id(self, tank_service, test_tank):
        """Test retrieving tank by ID."""
        tank = tank_service.get_tank(test_tank.id)

        assert tank is not None
        assert tank.id == test_tank.id
        assert tank.name == test_tank.name

    def test_get_tank_not_found(self, tank_service):
        """Test retrieving non-existent tank."""
        tank = tank_service.get_tank("nonexistent-id")
        assert tank is None

    def test_create_tank(self, tank_service, test_user):
        """Test creating a new tank."""
        tank_data = {
            "name": "New Test Tank",
            "user_id": test_user.id,
            "species": ["Catfish"],
            "capacity_liters": 8000.0,
            "current_stock": 100
        }

        tank = tank_service.create_tank(tank_data)

        assert tank is not None
        assert tank.name == tank_data["name"]
        assert tank.user_id == test_user.id
        assert tank.species == tank_data["species"]

    def test_update_tank(self, tank_service, test_tank):
        """Test updating tank information."""
        update_data = {
            "name": "Updated Name",
            "current_stock": 200
        }

        updated_tank = tank_service.update_tank(test_tank.id, update_data)

        assert updated_tank.name == update_data["name"]
        assert updated_tank.current_stock == update_data["current_stock"]

    def test_delete_tank(self, tank_service, test_tank):
        """Test deleting a tank."""
        success = tank_service.delete_tank(test_tank.id)

        assert success is True
        # Verify deletion
        tank = tank_service.get_tank(test_tank.id)
        assert tank is None

    def test_calculate_health_score(self, tank_service, test_tank, test_water_quality):
        """Test calculating tank health score."""
        health_score = tank_service.calculate_health_score(test_tank.id)

        assert health_score is not None
        assert 0 <= health_score <= 100

    def test_get_tank_statistics(self, tank_service, test_tank, test_water_quality):
        """Test retrieving tank statistics."""
        stats = tank_service.get_tank_statistics(test_tank.id)

        assert stats is not None
        assert "total_readings" in stats
        assert "avg_temperature" in stats
        assert "avg_ph" in stats
        assert stats["tank_id"] == test_tank.id


@pytest.mark.unit
class TestProductService:
    """Test product service business logic."""

    @pytest.fixture
    def product_service(self, test_db):
        """Create product service instance."""
        return ProductService(test_db)

    def test_get_all_products(self, product_service, test_product):
        """Test retrieving all products."""
        products = product_service.get_all_products()

        assert len(products) >= 1
        assert test_product.id in [p.id for p in products]

    def test_get_products_by_category(self, product_service, test_product):
        """Test filtering products by category."""
        products = product_service.get_products_by_category(test_product.category)

        assert len(products) >= 1
        assert all(p.category == test_product.category for p in products)

    def test_search_products(self, product_service, test_product):
        """Test product search functionality."""
        results = product_service.search_products("feed")

        assert len(results) >= 1
        # Should find test product
        assert any(p.id == test_product.id for p in results)

    def test_create_order(self, product_service, test_user, test_product):
        """Test creating an order."""
        order_data = {
            "user_id": test_user.id,
            "items": [
                {
                    "product_id": test_product.id,
                    "quantity": 2
                }
            ],
            "shipping_address": "Test Address",
            "payment_method": "cash_on_delivery"
        }

        order = product_service.create_order(order_data)

        assert order is not None
        assert order.user_id == test_user.id
        assert order.status == "pending"
        assert len(order.items) == 1
        assert order.total_amount > 0

    def test_create_order_insufficient_stock(self, product_service, test_user, test_product):
        """Test creating order with insufficient stock."""
        order_data = {
            "user_id": test_user.id,
            "items": [
                {
                    "product_id": test_product.id,
                    "quantity": 99999
                }
            ],
            "shipping_address": "Test Address",
            "payment_method": "cash_on_delivery"
        }

        with pytest.raises(ValueError, match="Insufficient stock"):
            product_service.create_order(order_data)

    def test_update_order_status(self, product_service, test_order):
        """Test updating order status."""
        updated_order = product_service.update_order_status(
            test_order.id,
            "confirmed"
        )

        assert updated_order.status == "confirmed"

    def test_cancel_order(self, product_service, test_order):
        """Test canceling an order."""
        cancelled_order = product_service.cancel_order(test_order.id)

        assert cancelled_order.status == "cancelled"


@pytest.mark.unit
class TestAnalysisService:
    """Test analysis service business logic."""

    @pytest.fixture
    def analysis_service(self, test_db):
        """Create analysis service instance."""
        return AnalysisService(test_db)

    @patch('ml.disease_classifier.DiseaseClassifier.predict')
    @patch('ai.gemini_client.GeminiClient.analyze_disease')
    async def test_detect_disease(self, mock_gemini, mock_ml, analysis_service, sample_image_base64):
        """Test disease detection combining ML and AI."""
        # Mock ML predictions
        mock_ml.return_value = [
            {
                "name": "Bacterial Red Disease",
                "confidence": 0.85,
                "description": "Bacterial infection",
                "causes": ["Poor water"],
                "symptoms": ["Red spots"],
                "treatment": "Antibiotics",
                "prevention": ["Water quality"]
            }
        ]

        # Mock Gemini AI analysis
        mock_gemini.return_value = {
            "diseases": [
                {
                    "name": "Parasitic Disease",
                    "confidence": 0.65,
                    "description": "External parasites"
                }
            ],
            "recommendation": "Quarantine and treat",
            "severity": "medium"
        }

        result = await analysis_service.detect_disease(
            image_base64=sample_image_base64,
            symptoms=["Red spots"]
        )

        assert result is not None
        assert "detected_diseases" in result
        assert len(result["detected_diseases"]) >= 2  # ML + AI results
        assert "recommendation" in result

    @patch('ai.gemini_client.GeminiClient.analyze_water_quality')
    async def test_analyze_water_quality(self, mock_gemini, analysis_service, test_tank, test_water_quality):
        """Test water quality analysis."""
        mock_gemini.return_value = {
            "status": "good",
            "issues": [],
            "recommendations": ["Maintain parameters"],
            "risk_level": "low"
        }

        result = await analysis_service.analyze_water_quality(test_tank.id)

        assert result is not None
        assert "status" in result
        assert "recommendations" in result
        mock_gemini.assert_called_once()

    @patch('services.analysis_service.AnalysisService.detect_disease')
    @patch('services.analysis_service.AnalysisService.analyze_water_quality')
    async def test_analyze_tank_health(self, mock_wq, mock_disease, analysis_service, test_tank, sample_image_base64):
        """Test comprehensive tank health analysis."""
        mock_wq.return_value = {
            "status": "good",
            "issues": [],
            "recommendations": []
        }

        mock_disease.return_value = {
            "detected_diseases": [],
            "recommendation": "No issues detected",
            "severity": "low"
        }

        result = await analysis_service.analyze_tank_health(
            tank_id=test_tank.id,
            include_water_quality=True,
            include_disease_check=True,
            fish_image_base64=sample_image_base64
        )

        assert result is not None
        assert "overall_health_score" in result
        assert "water_quality_analysis" in result
        assert "disease_analysis" in result

    def test_calculate_health_score(self, analysis_service, test_water_quality):
        """Test health score calculation logic."""
        # Good parameters
        score_good = analysis_service._calculate_health_score(
            temperature=26.5,
            ph=7.2,
            dissolved_oxygen=6.8,
            ammonia=0.01,
            nitrite=0.01,
            nitrate=5.0
        )
        assert 80 <= score_good <= 100

        # Poor parameters
        score_poor = analysis_service._calculate_health_score(
            temperature=35.0,  # Too high
            ph=9.5,  # Too high
            dissolved_oxygen=2.0,  # Too low
            ammonia=0.8,  # Too high
            nitrite=0.5,  # Too high
            nitrate=80.0  # Too high
        )
        assert score_poor < 50


@pytest.mark.unit
class TestVoiceService:
    """Test voice agent service logic."""

    @pytest.fixture
    def voice_service(self, test_db):
        """Create voice service instance."""
        return VoiceService(test_db)

    @patch('ai.gemini_client.GeminiClient.chat')
    async def test_process_voice_message(self, mock_chat, voice_service):
        """Test processing voice/text message."""
        mock_chat.return_value = {
            "response": "Your tank parameters look good. Temperature and pH are optimal for Tilapia.",
            "action": None
        }

        result = await voice_service.process_message(
            session_id="test-session",
            message="How are my tanks doing?",
            user_id="test-user-123"
        )

        assert result is not None
        assert "response" in result
        assert result["response"] is not None
        mock_chat.assert_called_once()

    @patch('ai.gemini_client.GeminiClient.chat')
    async def test_process_action_message(self, mock_chat, voice_service, test_tank):
        """Test processing message with action."""
        mock_chat.return_value = {
            "response": "I've added a water quality reminder for tomorrow.",
            "action": {
                "type": "add_reminder",
                "parameters": {
                    "tank_id": test_tank.id,
                    "task": "Check water quality",
                    "due_date": "2025-12-23"
                }
            }
        }

        result = await voice_service.process_message(
            session_id="test-session",
            message="Remind me to check water quality tomorrow",
            user_id="test-user-123"
        )

        assert result is not None
        assert "action" in result
        assert result["action"] is not None
        assert result["action"]["type"] == "add_reminder"

    def test_session_memory(self, voice_service):
        """Test session memory management."""
        session_id = "test-session"

        # Add messages to session
        voice_service.add_to_memory(session_id, "user", "Hello")
        voice_service.add_to_memory(session_id, "assistant", "Hi! How can I help?")
        voice_service.add_to_memory(session_id, "user", "Show my tanks")

        # Retrieve session history
        history = voice_service.get_session_history(session_id)

        assert len(history) == 3
        assert history[0]["role"] == "user"
        assert history[1]["role"] == "assistant"
        assert history[2]["message"] == "Show my tanks"

    def test_clear_session(self, voice_service):
        """Test clearing session memory."""
        session_id = "test-session"

        # Add messages
        voice_service.add_to_memory(session_id, "user", "Hello")
        voice_service.add_to_memory(session_id, "assistant", "Hi")

        # Clear session
        voice_service.clear_session(session_id)

        # Verify cleared
        history = voice_service.get_session_history(session_id)
        assert len(history) == 0
