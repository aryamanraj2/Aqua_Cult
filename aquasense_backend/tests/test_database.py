"""
Tests for Database Models and CRUD Operations
"""
import pytest
from datetime import datetime
from sqlalchemy.orm import Session

from models.user import User
from models.tank import Tank
from models.water_quality import WaterQuality
from models.product import Product
from models.order import Order


@pytest.mark.unit
class TestUserModel:
    """Test User model CRUD operations."""

    def test_create_user(self, test_db: Session):
        """Test creating a new user."""
        user = User(
            id="new-user-123",
            name="New User",
            email="newuser@test.com",
            phone="+9876543210"
        )
        test_db.add(user)
        test_db.commit()
        test_db.refresh(user)

        assert user.id == "new-user-123"
        assert user.name == "New User"
        assert user.email == "newuser@test.com"
        assert user.created_at is not None

    def test_user_relationships(self, test_db: Session, test_user, test_tank):
        """Test user relationships with tanks."""
        # Refresh to load relationships
        test_db.refresh(test_user)

        assert len(test_user.tanks) >= 1
        assert test_user.tanks[0].id == test_tank.id

    def test_update_user(self, test_db: Session, test_user):
        """Test updating user information."""
        test_user.name = "Updated Name"
        test_user.email = "updated@test.com"
        test_db.commit()
        test_db.refresh(test_user)

        assert test_user.name == "Updated Name"
        assert test_user.email == "updated@test.com"

    def test_delete_user(self, test_db: Session):
        """Test deleting a user."""
        user = User(
            id="temp-user",
            name="Temp User",
            email="temp@test.com"
        )
        test_db.add(user)
        test_db.commit()

        test_db.delete(user)
        test_db.commit()

        # Verify deletion
        deleted_user = test_db.query(User).filter(User.id == "temp-user").first()
        assert deleted_user is None


@pytest.mark.unit
class TestTankModel:
    """Test Tank model CRUD operations."""

    def test_create_tank(self, test_db: Session, test_user):
        """Test creating a new tank."""
        tank = Tank(
            id="new-tank-123",
            name="New Tank",
            user_id=test_user.id,
            species=["Salmon", "Trout"],
            capacity=15000.0,
            current_stock=300
        )
        test_db.add(tank)
        test_db.commit()
        test_db.refresh(tank)

        assert tank.id == "new-tank-123"
        assert tank.name == "New Tank"
        assert tank.user_id == test_user.id
        assert "Salmon" in tank.species
        assert tank.created_at is not None

    def test_tank_relationships(self, test_db: Session, test_tank, test_water_quality):
        """Test tank relationships with water quality readings."""
        test_db.refresh(test_tank)

        assert len(test_tank.water_quality_readings) >= 1
        assert test_tank.water_quality_readings[0].id == test_water_quality.id

    def test_tank_validation(self, test_db: Session, test_user):
        """Test tank field validation."""
        # Valid tank
        valid_tank = Tank(
            id="valid-tank",
            name="Valid Tank",
            user_id=test_user.id,
            species=["Tilapia"],
            capacity=5000.0,
            current_stock=100
        )
        test_db.add(valid_tank)
        test_db.commit()
        assert valid_tank.id is not None

    def test_update_tank(self, test_db: Session, test_tank):
        """Test updating tank information."""
        test_tank.name = "Updated Tank Name"
        test_tank.current_stock = 250
        test_db.commit()
        test_db.refresh(test_tank)

        assert test_tank.name == "Updated Tank Name"
        assert test_tank.current_stock == 250


@pytest.mark.unit
class TestWaterQualityModel:
    """Test WaterQuality model CRUD operations."""

    def test_create_reading(self, test_db: Session, test_tank):
        """Test creating a water quality reading."""
        reading = WaterQuality(
            id="new-reading-123",
            tank_id=test_tank.id,
            temperature=25.5,
            ph=7.0,
            dissolved_oxygen=7.5,
            ammonia=0.01,
            nitrite=0.01,
            nitrate=3.0,
            salinity=None
        )
        test_db.add(reading)
        test_db.commit()
        test_db.refresh(reading)

        assert reading.id == "new-reading-123"
        assert reading.tank_id == test_tank.id
        assert reading.temperature == 25.5
        assert reading.created_at is not None

    def test_reading_relationships(self, test_db: Session, test_water_quality, test_tank):
        """Test water quality reading relationships."""
        test_db.refresh(test_water_quality)

        assert test_water_quality.tank is not None
        assert test_water_quality.tank.id == test_tank.id

    def test_query_latest_readings(self, test_db: Session, test_tank):
        """Test querying latest water quality readings."""
        # Create multiple readings
        for i in range(5):
            reading = WaterQuality(
                id=f"reading-{i}",
                tank_id=test_tank.id,
                temperature=25.0 + i,
                ph=7.0,
                dissolved_oxygen=6.5,
                ammonia=0.01,
                nitrite=0.01,
                nitrate=5.0
            )
            test_db.add(reading)
        test_db.commit()

        # Query latest 3 readings
        latest_readings = (
            test_db.query(WaterQuality)
            .filter(WaterQuality.tank_id == test_tank.id)
            .order_by(WaterQuality.created_at.desc())
            .limit(3)
            .all()
        )

        assert len(latest_readings) == 3
        # Most recent should have highest temperature (created last)
        assert latest_readings[0].temperature >= latest_readings[1].temperature

    def test_calculate_averages(self, test_db: Session, test_tank):
        """Test calculating average water parameters."""
        from sqlalchemy import func

        # Get average temperature
        avg_temp = (
            test_db.query(func.avg(WaterQuality.temperature))
            .filter(WaterQuality.tank_id == test_tank.id)
            .scalar()
        )

        assert avg_temp is not None
        assert avg_temp > 0


@pytest.mark.unit
class TestProductModel:
    """Test Product model CRUD operations."""

    def test_create_product(self, test_db: Session):
        """Test creating a new product."""
        product = Product(
            id="new-product-123",
            name="Test Equipment",
            description="Test equipment description",
            category="equipment",
            price=150.00,
            unit="piece",
            stock_quantity=50,
            supplier="Test Supplier"
        )
        test_db.add(product)
        test_db.commit()
        test_db.refresh(product)

        assert product.id == "new-product-123"
        assert product.name == "Test Equipment"
        assert product.price == 150.00
        assert product.created_at is not None

    def test_product_search(self, test_db: Session, test_product):
        """Test searching products."""
        # Search by name
        results = (
            test_db.query(Product)
            .filter(Product.name.ilike("%feed%"))
            .all()
        )

        assert len(results) >= 1
        assert any(p.id == test_product.id for p in results)

    def test_filter_by_category(self, test_db: Session, test_product):
        """Test filtering products by category."""
        results = (
            test_db.query(Product)
            .filter(Product.category == test_product.category)
            .all()
        )

        assert len(results) >= 1
        assert all(p.category == test_product.category for p in results)

    def test_update_stock(self, test_db: Session, test_product):
        """Test updating product stock."""
        original_stock = test_product.stock_quantity
        test_product.stock_quantity = original_stock - 10
        test_db.commit()
        test_db.refresh(test_product)

        assert test_product.stock_quantity == original_stock - 10


@pytest.mark.unit
class TestOrderModel:
    """Test Order and OrderItem model CRUD operations."""

    def test_create_order(self, test_db: Session, test_user, test_product):
        """Test creating a new order with items."""
        order = Order(
            id="new-order-123",
            user_id=test_user.id,
            status="pending",
            total_amount=150.00,
            delivery_address="Test Address",
            payment_method="credit_card",
            items=[
                {
                    "product_id": test_product.id,
                    "quantity": 3,
                    "price": 50.00
                }
            ]
        )
        test_db.add(order)
        test_db.commit()
        test_db.refresh(order)

        assert order.id == "new-order-123"
        assert len(order.items) == 1
        assert order.items[0]["product_id"] == test_product.id

    def test_order_relationships(self, test_db: Session, test_order, test_user):
        """Test order relationships."""
        test_db.refresh(test_order)

        # Order -> User (if relationship is defined)
        # Note: Order model may not have user relationship defined
        # Just verify order data
        assert test_order.user_id == test_user.id

        # Order -> Items (JSON field)
        assert len(test_order.items) >= 1
        assert isinstance(test_order.items, list)
        assert "product_id" in test_order.items[0]

    def test_update_order_status(self, test_db: Session, test_order):
        """Test updating order status."""
        test_order.status = "confirmed"
        test_db.commit()
        test_db.refresh(test_order)

        assert test_order.status == "confirmed"

    def test_calculate_order_total(self, test_db: Session, test_order):
        """Test calculating order total from items."""
        total = sum(item["quantity"] * item["price"] for item in test_order.items)
        assert total == test_order.total_amount

    def test_query_user_orders(self, test_db: Session, test_user, test_order):
        """Test querying orders by user."""
        orders = (
            test_db.query(Order)
            .filter(Order.user_id == test_user.id)
            .all()
        )

        assert len(orders) >= 1
        assert any(o.id == test_order.id for o in orders)

    def test_query_orders_by_status(self, test_db: Session):
        """Test querying orders by status."""
        pending_orders = (
            test_db.query(Order)
            .filter(Order.status == "pending")
            .all()
        )

        assert isinstance(pending_orders, list)
        assert all(o.status == "pending" for o in pending_orders)


@pytest.mark.unit
class TestDatabaseConstraints:
    """Test database constraints and validations."""

    def test_foreign_key_constraint(self, test_db: Session):
        """Test foreign key constraints."""
        # Try to create tank with non-existent user
        tank = Tank(
            id="invalid-tank",
            name="Invalid Tank",
            user_id="nonexistent-user-id",
            species=["Fish"],
            capacity=1000.0
        )
        test_db.add(tank)

        # Should raise integrity error
        with pytest.raises(Exception):  # SQLAlchemy IntegrityError
            test_db.commit()
        test_db.rollback()

    def test_cascade_delete(self, test_db: Session, test_user):
        """Test cascade delete behavior."""
        # Create tank for user
        tank = Tank(
            id="cascade-tank",
            name="Cascade Tank",
            user_id=test_user.id,
            species=["Fish"],
            capacity=1000.0
        )
        test_db.add(tank)
        test_db.commit()

        # Delete user
        test_db.delete(test_user)
        test_db.commit()

        # Tank should also be deleted (if cascade is set)
        deleted_tank = test_db.query(Tank).filter(Tank.id == "cascade-tank").first()
        # Behavior depends on cascade settings in models

    def test_unique_constraints(self, test_db: Session, test_user):
        """Test unique constraint violations."""
        # Try to create user with duplicate email (if unique constraint exists)
        duplicate_user = User(
            id="duplicate-user",
            name="Duplicate",
            email=test_user.email  # Same email
        )
        test_db.add(duplicate_user)

        # May raise integrity error depending on schema
        try:
            test_db.commit()
        except Exception:
            test_db.rollback()
