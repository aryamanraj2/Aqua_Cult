"""
Quick script to check if tanks have water quality data
"""
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from models.tank import Tank
from models.water_quality import WaterQuality

# Connect to database
engine = create_engine('sqlite:///aquasense.db')
Session = sessionmaker(bind=engine)
session = Session()

print("=" * 80)
print("CHECKING WATER QUALITY DATA IN DATABASE")
print("=" * 80)

# Get all tanks
tanks = session.query(Tank).all()
print(f"\n📊 Found {len(tanks)} tank(s) in database\n")

if not tanks:
    print("❌ No tanks found in database!")
    print("   Please create a tank first in the iOS app.")
else:
    for tank in tanks:
        print(f"🏊 Tank: {tank.name} (ID: {tank.id})")
        print(f"   Species: {tank.species}")
        print(f"   Capacity: {tank.capacity}L")

        # Check for water quality readings
        wq_count = session.query(WaterQuality).filter_by(tank_id=tank.id).count()

        if wq_count == 0:
            print(f"   ⚠️  NO WATER QUALITY READINGS FOUND")
            print(f"   → ML model will NOT run for this tank")
            print(f"   → Add water quality readings in the iOS app")
        else:
            print(f"   ✅ {wq_count} water quality reading(s) found")

            # Get latest reading
            latest = session.query(WaterQuality).filter_by(tank_id=tank.id).order_by(WaterQuality.recorded_at.desc()).first()
            if latest:
                print(f"   Latest reading:")
                print(f"     • Temperature: {latest.temperature}°C")
                print(f"     • pH: {latest.ph}")
                print(f"     • DO: {latest.dissolved_oxygen} mg/L")
                print(f"     • Turbidity: {latest.turbidity} cm")
                print(f"     • Ammonia: {latest.ammonia} mg/L")
                print(f"     • Nitrite: {latest.nitrite} mg/L")
                print(f"   → ML model WILL run for this tank ✓")

        print()

print("=" * 80)
print("TIP: To test ML model, make sure your tank has water quality readings!")
print("=" * 80)

session.close()
