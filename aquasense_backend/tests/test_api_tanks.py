"""
Tests for Tank API endpoints
"""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session


@pytest.mark.integration
class TestTankEndpoints:
    """Test tank CRUD operations via API."""

    def test_create_tank(self, client: TestClient, test_user):
        """Test creating a new tank."""
        tank_data = {
            "name": "Main Tilapia Tank",
            "species": ["Tilapia"],
            "capacity": 10000.0,
            "current_stock": 200
        }

        response = client.post(
            "/api/v1/tanks",
            json=tank_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 201
        data = response.json()
        assert data["name"] == tank_data["name"]
        assert data["species"] == tank_data["species"]
        assert data["capacity"] == tank_data["capacity"]
        assert data["current_stock"] == tank_data["current_stock"]
        assert "id" in data
        assert "created_at" in data

    def test_create_tank_validation_error(self, client: TestClient, test_user):
        """Test tank creation with invalid data."""
        invalid_data = {
            "name": "",  # Empty name should fail
            "species": [],
            "capacity": -100  # Negative capacity should fail
        }

        response = client.post(
            "/api/v1/tanks",
            json=invalid_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 422  # Validation error

    def test_get_all_tanks(self, client: TestClient, test_user, test_tank):
        """Test retrieving all tanks for a user."""
        response = client.get(
            "/api/v1/tanks",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        assert len(data) >= 1
        assert data[0]["id"] == test_tank.id
        assert data[0]["name"] == test_tank.name

    def test_get_tank_by_id(self, client: TestClient, test_user, test_tank):
        """Test retrieving a specific tank."""
        response = client.get(
            f"/api/v1/tanks/{test_tank.id}",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["id"] == test_tank.id
        assert data["name"] == test_tank.name
        assert data["species"] == test_tank.species

    def test_get_tank_not_found(self, client: TestClient, test_user):
        """Test retrieving non-existent tank."""
        response = client.get(
            "/api/v1/tanks/nonexistent-tank-id",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 404

    def test_update_tank(self, client: TestClient, test_user, test_tank):
        """Test updating tank information."""
        update_data = {
            "name": "Updated Tank Name",
            "current_stock": 180
        }

        response = client.put(
            f"/api/v1/tanks/{test_tank.id}",
            json=update_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["name"] == update_data["name"]
        assert data["current_stock"] == update_data["current_stock"]
        # Original values should remain
        assert data["species"] == test_tank.species

    def test_delete_tank(self, client: TestClient, test_user, test_tank):
        """Test deleting a tank."""
        response = client.delete(
            f"/api/v1/tanks/{test_tank.id}",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 204

        # Verify tank is deleted
        get_response = client.get(
            f"/api/v1/tanks/{test_tank.id}",
            headers={"X-User-ID": test_user.id}
        )
        assert get_response.status_code == 404


@pytest.mark.integration
class TestWaterQualityEndpoints:
    """Test water quality reading endpoints."""

    def test_add_water_quality_reading(self, client: TestClient, test_user, test_tank):
        """Test adding a water quality reading."""
        reading_data = {
            "temperature": 27.0,
            "ph": 7.5,
            "dissolved_oxygen": 7.2,
            "ammonia": 0.01,
            "nitrite": 0.005,
            "nitrate": 3.0,
            "turbidity": 2.8
        }

        response = client.post(
            f"/api/v1/tanks/{test_tank.id}/water-quality",
            json=reading_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 201
        data = response.json()
        assert data["tank_id"] == test_tank.id
        assert data["temperature"] == reading_data["temperature"]
        assert data["ph"] == reading_data["ph"]
        assert "id" in data
        assert "created_at" in data

    def test_get_water_quality_history(self, client: TestClient, test_user, test_tank, test_water_quality):
        """Test retrieving water quality history."""
        response = client.get(
            f"/api/v1/tanks/{test_tank.id}/water-quality",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        assert len(data) >= 1
        assert data[0]["id"] == test_water_quality.id
        assert data[0]["tank_id"] == test_tank.id

    def test_get_latest_water_quality(self, client: TestClient, test_user, test_tank, test_water_quality):
        """Test retrieving latest water quality reading."""
        response = client.get(
            f"/api/v1/tanks/{test_tank.id}/water-quality/latest",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["id"] == test_water_quality.id
        assert data["tank_id"] == test_tank.id
        assert data["temperature"] == test_water_quality.temperature

    def test_water_quality_validation(self, client: TestClient, test_user, test_tank):
        """Test water quality reading with invalid values."""
        invalid_data = {
            "temperature": -10,  # Temperature too low
            "ph": 15.0,  # pH out of range
            "dissolved_oxygen": -1  # Negative DO
        }

        response = client.post(
            f"/api/v1/tanks/{test_tank.id}/water-quality",
            json=invalid_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 422  # Validation error


@pytest.mark.integration
class TestTankStatistics:
    """Test tank statistics and analytics endpoints."""

    def test_get_tank_stats(self, client: TestClient, test_user, test_tank, test_water_quality):
        """Test retrieving tank statistics."""
        response = client.get(
            f"/api/v1/tanks/{test_tank.id}/stats",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert "tank_id" in data
        assert "total_readings" in data
        assert "avg_temperature" in data
        assert "avg_ph" in data
        assert "health_score" in data

    def test_get_dashboard_summary(self, client: TestClient, test_user, test_tank):
        """Test retrieving dashboard summary."""
        response = client.get(
            "/api/v1/tanks/dashboard",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert "total_tanks" in data
        assert "total_fish" in data
        assert "tanks_needing_attention" in data
        assert isinstance(data["recent_alerts"], list)
