"""
Check database structure and contents
"""
from sqlalchemy import create_engine, inspect
from sqlalchemy.orm import sessionmaker
from models.tank import Tank
from models.water_quality import WaterQuality
from models.user import User

# Connect to database
engine = create_engine('sqlite:///aquasense.db')
Session = sessionmaker(bind=engine)
session = Session()

print("=" * 80)
print("DATABASE INSPECTION")
print("=" * 80)

# Check tables
inspector = inspect(engine)
tables = inspector.get_table_names()

print(f"\n📊 Tables in database: {len(tables)}")
for table in tables:
    print(f"   • {table}")

# Check record counts
print(f"\n📈 Record counts:")
try:
    user_count = session.query(User).count()
    print(f"   • Users: {user_count}")
except Exception as e:
    print(f"   • Users: Error - {e}")

try:
    tank_count = session.query(Tank).count()
    print(f"   • Tanks: {tank_count}")
except Exception as e:
    print(f"   • Tanks: Error - {e}")

try:
    wq_count = session.query(WaterQuality).count()
    print(f"   • Water Quality Readings: {wq_count}")
except Exception as e:
    print(f"   • Water Quality Readings: Error - {e}")

if tank_count == 0:
    print(f"\n⚠️  DATABASE IS EMPTY!")
    print(f"   This means:")
    print(f"   1. No tanks have been created in iOS app yet, OR")
    print(f"   2. iOS app is connecting to a different backend URL, OR")
    print(f"   3. There's an error when creating tanks from iOS")
    print(f"\n💡 Next steps:")
    print(f"   1. Check if iOS app shows any tanks")
    print(f"   2. Check the backend URL in iOS app (should be http://localhost:8000)")
    print(f"   3. Try creating a new tank in iOS app and watch backend logs")

print("\n" + "=" * 80)
session.close()
