"""
Add sample tank and water quality data for testing
"""
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from models.tank import Tank
from models.water_quality import WaterQuality
from datetime import datetime
import uuid

# Connect to database
engine = create_engine('sqlite:///aquasense.db')
Session = sessionmaker(bind=engine)
session = Session()

print("=" * 80)
print("ADDING SAMPLE DATA FOR TESTING")
print("=" * 80)

# Create a test tank
tank_id = str(uuid.uuid4())
test_tank = Tank(
    id=tank_id,
    user_id="test-user",  # Placeholder user ID
    name="Test Tank - Tilapia",
    species=["Tilapia"],
    capacity=1000,
    current_stock=50,
    location="Test Farm",
    status="active",
    created_at=datetime.utcnow()
)

session.add(test_tank)
print(f"\n✅ Created test tank:")
print(f"   ID: {tank_id}")
print(f"   Name: {test_tank.name}")
print(f"   Species: {test_tank.species}")

# Add water quality reading
wq_reading = WaterQuality(
    id=str(uuid.uuid4()),
    tank_id=tank_id,
    temperature=28.0,
    ph=7.5,
    dissolved_oxygen=6.5,
    turbidity=3.8,
    ammonia=0.015,
    nitrite=0.008,
    nitrate=10.0,
    salinity=0.5
)

session.add(wq_reading)
print(f"\n✅ Added water quality reading:")
print(f"   Temperature: {wq_reading.temperature}°C")
print(f"   pH: {wq_reading.ph}")
print(f"   DO: {wq_reading.dissolved_oxygen} mg/L")
print(f"   Turbidity: {wq_reading.turbidity} cm")
print(f"   Ammonia: {wq_reading.ammonia} mg/L")
print(f"   Nitrite: {wq_reading.nitrite} mg/L")

session.commit()

print(f"\n✅ SAMPLE DATA ADDED SUCCESSFULLY!")
print(f"\n📝 To test the ML model:")
print(f"   1. Copy this tank ID: {tank_id}")
print(f"   2. Use this curl command:")
print(f"")
print(f"   curl http://localhost:8000/api/v1/analysis/tank-analysis/{tank_id}")
print(f"")
print(f"   OR test in browser:")
print(f"   http://localhost:8000/api/v1/analysis/tank-analysis/{tank_id}")
print(f"")
print(f"   You should see ML model logs in the backend terminal!")
print("=" * 80)

session.close()
