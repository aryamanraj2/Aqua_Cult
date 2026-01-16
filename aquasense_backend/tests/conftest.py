"""
Pytest configuration and shared fixtures for AquaSense backend tests
"""
import os
import sys
import pytest
from typing import Generator
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy.pool import StaticPool

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Set test environment variables
os.environ['GEMINI_API_KEY'] = 'test_gemini_api_key_12345'
os.environ['DATABASE_URL'] = 'sqlite:///:memory:'
os.environ['DEBUG'] = 'True'

from main import app
from config.database import Base, get_db
from models.user import User
from models.tank import Tank
from models.water_quality import WaterQuality
from models.product import Product
from models.order import Order


# Test database setup
TEST_DATABASE_URL = "sqlite:///:memory:"

@pytest.fixture(scope="function")
def test_db() -> Generator[Session, None, None]:
    """
    Create a fresh test database for each test function.
    Uses in-memory SQLite with StaticPool to share connection across threads.
    """
    engine = create_engine(
        TEST_DATABASE_URL,
        connect_args={"check_same_thread": False},
        poolclass=StaticPool
    )

    # Create all tables
    Base.metadata.create_all(bind=engine)

    # Create session
    TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    session = TestingSessionLocal()

    try:
        yield session
    finally:
        session.close()
        Base.metadata.drop_all(bind=engine)


@pytest.fixture(scope="function")
def client(test_db: Session) -> Generator[TestClient, None, None]:
    """
    Create a FastAPI test client with test database dependency override.
    """
    def override_get_db():
        try:
            yield test_db
        finally:
            pass

    app.dependency_overrides[get_db] = override_get_db

    with TestClient(app) as test_client:
        yield test_client

    app.dependency_overrides.clear()


@pytest.fixture
def test_user(test_db: Session) -> User:
    """Create a test user in the database."""
    user = User(
        id="test-user-123",
        name="Test User",
        email="test@aquasense.com",
        phone="+1234567890"
    )
    test_db.add(user)
    test_db.commit()
    test_db.refresh(user)
    return user


@pytest.fixture
def test_tank(test_db: Session, test_user: User) -> Tank:
    """Create a test tank in the database."""
    tank = Tank(
        id="test-tank-123",
        name="Test Tank 1",
        user_id=test_user.id,
        species=["Tilapia", "Catfish"],
        capacity=5000.0,
        current_stock=150
    )
    test_db.add(tank)
    test_db.commit()
    test_db.refresh(tank)
    return tank


@pytest.fixture
def test_water_quality(test_db: Session, test_tank: Tank) -> WaterQuality:
    """Create a test water quality reading."""
    reading = WaterQuality(
        id="test-wq-123",
        tank_id=test_tank.id,
        temperature=26.5,
        ph=7.2,
        dissolved_oxygen=6.8,
        ammonia=0.02,
        nitrite=0.01,
        nitrate=5.0,
        salinity=None
    )
    test_db.add(reading)
    test_db.commit()
    test_db.refresh(reading)
    return reading


@pytest.fixture
def test_product(test_db: Session) -> Product:
    """Create a test product."""
    product = Product(
        id="test-product-123",
        name="Premium Fish Feed",
        description="High-protein feed for fast growth",
        category="feed",
        price=45.99,
        unit="kg",
        stock_quantity=500,
        manufacturer="AquaSupplies Inc.",
        image_url="https://example.com/feed.jpg"
    )
    test_db.add(product)
    test_db.commit()
    test_db.refresh(product)
    return product


@pytest.fixture
def test_order(test_db: Session, test_user: User, test_product: Product) -> Order:
    """Create a test order with items."""
    order = Order(
        id="test-order-123",
        user_id=test_user.id,
        status="pending",
        total_amount=91.98,
        delivery_address="123 Test St, Test City",
        payment_method="cash_on_delivery",
        items=[
            {
                "product_id": test_product.id,
                "quantity": 2,
                "price": 45.99
            }
        ]
    )
    test_db.add(order)
    test_db.commit()
    test_db.refresh(order)
    return order


@pytest.fixture
def sample_image_base64() -> str:
    """
    Generate a small test image as base64 string.
    Creates a 100x100 RGB image.
    """
    from PIL import Image
    from io import BytesIO
    import base64
    import numpy as np

    # Create random RGB image
    img_array = np.random.randint(0, 255, (100, 100, 3), dtype=np.uint8)
    img = Image.fromarray(img_array, 'RGB')

    # Convert to base64
    buffered = BytesIO()
    img.save(buffered, format="JPEG")
    img_bytes = buffered.getvalue()
    img_base64 = base64.b64encode(img_bytes).decode('utf-8')

    return img_base64


@pytest.fixture
def mock_gemini_response():
    """Mock response from Gemini API."""
    return {
        "diseases": [
            {
                "name": "Bacterial Red Disease",
                "confidence": 0.85,
                "description": "Bacterial infection causing redness",
                "causes": ["Poor water quality", "Stress"],
                "symptoms": ["Red lesions", "Lethargy"],
                "treatment": "Antibiotic treatment recommended",
                "prevention": ["Maintain water quality", "Quarantine new fish"]
            }
        ],
        "recommendation": "Immediate treatment required",
        "severity": "high",
        "urgent_action_required": True
    }


# Pytest configuration
def pytest_configure(config):
    """Configure pytest with custom markers."""
    config.addinivalue_line(
        "markers", "integration: mark test as integration test"
    )
    config.addinivalue_line(
        "markers", "unit: mark test as unit test"
    )
    config.addinivalue_line(
        "markers", "slow: mark test as slow running"
    )
    config.addinivalue_line(
        "markers", "ml: mark test as ML model test"
    )
