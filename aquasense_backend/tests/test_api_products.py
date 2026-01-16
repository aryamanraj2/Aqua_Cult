"""
Tests for Product and Order API endpoints
"""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session


@pytest.mark.integration
class TestProductEndpoints:
    """Test product marketplace endpoints."""

    def test_get_all_products(self, client: TestClient, test_product):
        """Test retrieving all products."""
        response = client.get("/api/v1/products")

        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        assert len(data) >= 1
        assert data[0]["id"] == test_product.id
        assert data[0]["name"] == test_product.name
        assert data[0]["price"] == test_product.price

    def test_get_products_by_category(self, client: TestClient, test_product):
        """Test filtering products by category."""
        response = client.get(f"/api/v1/products?category={test_product.category}")

        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        assert all(p["category"] == test_product.category for p in data)

    def test_get_product_by_id(self, client: TestClient, test_product):
        """Test retrieving a specific product."""
        response = client.get(f"/api/v1/products/{test_product.id}")

        assert response.status_code == 200
        data = response.json()
        assert data["id"] == test_product.id
        assert data["name"] == test_product.name
        assert data["description"] == test_product.description
        assert data["price"] == test_product.price
        assert data["stock_quantity"] == test_product.stock_quantity

    def test_get_product_not_found(self, client: TestClient):
        """Test retrieving non-existent product."""
        response = client.get("/api/v1/products/nonexistent-product-id")

        assert response.status_code == 404

    def test_search_products(self, client: TestClient, test_product):
        """Test product search functionality."""
        response = client.get(f"/api/v1/products/search?q=feed")

        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        # Should find the test product
        assert any(p["id"] == test_product.id for p in data)

    def test_create_product(self, client: TestClient):
        """Test creating a new product (admin endpoint)."""
        product_data = {
            "name": "Oxygen Pump Pro",
            "description": "High-efficiency oxygen pump for large tanks",
            "category": "equipment",
            "price": 199.99,
            "unit": "piece",
            "stock_quantity": 25,
            "supplier": "AquaTech Solutions",
            "image_url": "https://example.com/pump.jpg"
        }

        response = client.post(
            "/api/v1/products",
            json=product_data
        )

        assert response.status_code == 201
        data = response.json()
        assert data["name"] == product_data["name"]
        assert data["price"] == product_data["price"]
        assert data["category"] == product_data["category"]
        assert "id" in data

    def test_update_product(self, client: TestClient, test_product):
        """Test updating product information."""
        update_data = {
            "price": 49.99,
            "stock_quantity": 450
        }

        response = client.put(
            f"/api/v1/products/{test_product.id}",
            json=update_data
        )

        assert response.status_code == 200
        data = response.json()
        assert data["price"] == update_data["price"]
        assert data["stock_quantity"] == update_data["stock_quantity"]
        # Original values should remain
        assert data["name"] == test_product.name


@pytest.mark.integration
class TestOrderEndpoints:
    """Test order management endpoints."""

    def test_create_order(self, client: TestClient, test_user, test_product):
        """Test creating a new order."""
        order_data = {
            "items": [
                {
                    "product_id": test_product.id,
                    "quantity": 3
                }
            ],
            "shipping_address": "456 Farm Road, Aqua City",
            "payment_method": "cash_on_delivery"
        }

        response = client.post(
            "/api/v1/orders",
            json=order_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 201
        data = response.json()
        assert data["user_id"] == test_user.id
        assert data["status"] == "pending"
        # Note: API might return 'delivery_address' or 'shipping_address'
        assert "address" in data.get("shipping_address", data.get("delivery_address", "address"))
        assert len(data["items"]) == 1
        assert data["items"][0]["product_id"] == test_product.id
        assert data["items"][0]["quantity"] == 3
        assert data["total_amount"] > 0

    def test_create_order_insufficient_stock(self, client: TestClient, test_user, test_product):
        """Test creating order with quantity exceeding stock."""
        order_data = {
            "items": [
                {
                    "product_id": test_product.id,
                    "quantity": 99999  # More than available
                }
            ],
            "shipping_address": "456 Farm Road, Aqua City",
            "payment_method": "cash_on_delivery"
        }

        response = client.post(
            "/api/v1/orders",
            json=order_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 400
        assert "insufficient stock" in response.json()["detail"].lower()

    def test_get_user_orders(self, client: TestClient, test_user, test_order):
        """Test retrieving all orders for a user."""
        response = client.get(
            "/api/v1/orders",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        assert len(data) >= 1
        assert data[0]["id"] == test_order.id
        assert data[0]["user_id"] == test_user.id

    def test_get_order_by_id(self, client: TestClient, test_user, test_order):
        """Test retrieving a specific order."""
        response = client.get(
            f"/api/v1/orders/{test_order.id}",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["id"] == test_order.id
        assert data["status"] == test_order.status
        assert data["total_amount"] == test_order.total_amount
        assert len(data["items"]) >= 1

    def test_get_order_not_found(self, client: TestClient, test_user):
        """Test retrieving non-existent order."""
        response = client.get(
            "/api/v1/orders/nonexistent-order-id",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 404

    def test_update_order_status(self, client: TestClient, test_user, test_order):
        """Test updating order status."""
        update_data = {
            "status": "confirmed"
        }

        response = client.patch(
            f"/api/v1/orders/{test_order.id}",
            json=update_data,
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "confirmed"

    def test_cancel_order(self, client: TestClient, test_user, test_order):
        """Test canceling an order."""
        response = client.post(
            f"/api/v1/orders/{test_order.id}/cancel",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "cancelled"

    def test_cancel_already_shipped_order(self, client: TestClient, test_user, test_order, test_db):
        """Test that shipped orders cannot be cancelled."""
        # Update order status to shipped
        test_order.status = "shipped"
        test_db.commit()

        response = client.post(
            f"/api/v1/orders/{test_order.id}/cancel",
            headers={"X-User-ID": test_user.id}
        )

        assert response.status_code == 400
        assert "cannot cancel" in response.json()["detail"].lower()


@pytest.mark.integration
class TestProductCategories:
    """Test product category endpoints."""

    def test_get_categories(self, client: TestClient, test_product):
        """Test retrieving all product categories."""
        response = client.get("/api/v1/products/categories")

        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        assert test_product.category in data

    def test_get_category_stats(self, client: TestClient, test_product):
        """Test retrieving statistics by category."""
        response = client.get(
            f"/api/v1/products/categories/{test_product.category}/stats"
        )

        assert response.status_code == 200
        data = response.json()
        assert "category" in data
        assert "total_products" in data
        assert "avg_price" in data
        assert data["category"] == test_product.category
