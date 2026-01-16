"""
Seed Database with Sample Data
"""
import uuid
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from config.database import SessionLocal, engine, Base
from models.user import User
from models.tank import Tank
from models.water_quality import WaterQuality
from models.product import Product

def seed_database():
    """Add sample data to the database"""

    # Create tables if they don't exist
    Base.metadata.create_all(bind=engine)

    # Create a session
    db: Session = SessionLocal()

    try:
        # Check if user already exists
        existing_user = db.query(User).filter(User.id == "default_user_001").first()

        if not existing_user:
            # Create default user
            user = User(
                id="default_user_001",
                name="John Doe",
                email="john.doe@aquasense.com",
                phone="+1234567890",
                created_at=datetime.utcnow(),
                updated_at=datetime.utcnow()
            )
            db.add(user)
            print("✅ Created default user")
        else:
            print("ℹ️  Default user already exists")

        # Check if tanks already exist
        existing_tanks = db.query(Tank).filter(Tank.user_id == "default_user_001").count()

        if existing_tanks == 0:
            # Tank 1: Koi Pond
            tank1 = Tank(
                id=str(uuid.uuid4()),
                user_id="default_user_001",
                name="Koi Pond - Main",
                species=["Koi", "Goldfish"],
                capacity=5000.0,
                current_stock=45,
                location="North Garden",
                status="active",
                notes="Primary koi breeding pond with filtration system",
                created_at=datetime.utcnow(),
                updated_at=datetime.utcnow()
            )
            db.add(tank1)
            print(f"✅ Created Tank 1: {tank1.name}")

            # Add water quality readings for Tank 1 (last 3 days)
            for days_ago in range(3, 0, -1):
                reading = WaterQuality(
                    id=str(uuid.uuid4()),
                    tank_id=tank1.id,
                    ph=7.2 + (days_ago * 0.1),
                    temperature=24.0 + (days_ago * 0.5),
                    dissolved_oxygen=7.5 - (days_ago * 0.2),
                    ammonia=0.02 + (days_ago * 0.005),
                    nitrite=0.01,
                    nitrate=10.0 + (days_ago * 1.0),
                    salinity=0.0,
                    turbidity=2.5 + (days_ago * 0.3),
                    created_at=datetime.utcnow() - timedelta(days=days_ago)
                )
                db.add(reading)
            print(f"  → Added 3 water quality readings")

            # Tank 2: Tilapia Farm
            tank2 = Tank(
                id=str(uuid.uuid4()),
                user_id="default_user_001",
                name="Tilapia Farm Tank A",
                species=["Nile Tilapia"],
                capacity=10000.0,
                current_stock=120,
                location="Greenhouse Section B",
                status="active",
                notes="Commercial tilapia production tank with biofilter",
                created_at=datetime.utcnow(),
                updated_at=datetime.utcnow()
            )
            db.add(tank2)
            print(f"✅ Created Tank 2: {tank2.name}")

            # Add water quality readings for Tank 2 (last 3 days)
            for days_ago in range(3, 0, -1):
                reading = WaterQuality(
                    id=str(uuid.uuid4()),
                    tank_id=tank2.id,
                    ph=7.8 - (days_ago * 0.05),
                    temperature=27.0 + (days_ago * 0.3),
                    dissolved_oxygen=6.2 + (days_ago * 0.15),
                    ammonia=0.08 - (days_ago * 0.01),
                    nitrite=0.05 - (days_ago * 0.005),
                    nitrate=15.0 + (days_ago * 2.0),
                    salinity=0.0,
                    turbidity=4.0 - (days_ago * 0.2),
                    created_at=datetime.utcnow() - timedelta(days=days_ago)
                )
                db.add(reading)
            print(f"  → Added 3 water quality readings")

            print(f"\n✅ Created 2 tanks with 6 total water quality readings")
        else:
            print(f"ℹ️  {existing_tanks} tanks already exist")

        # Check if products already exist
        existing_products = db.query(Product).count()

        if existing_products == 0:
            # Feed Products
            feed_products = [
                {
                    "name": "Tilapia Premium Pellets",
                    "category": "feed",
                    "description": "High-quality pellets specially formulated for optimal tilapia growth and health",
                    "price": 450.0,
                    "unit": "kg",
                    "stock_quantity": 500,
                    "manufacturer": "AquaNutrition Ltd"
                },
                {
                    "name": "Catfish Growth Formula",
                    "category": "feed",
                    "description": "Protein-rich formula designed for catfish growth stages",
                    "price": 520.0,
                    "unit": "kg",
                    "stock_quantity": 300,
                    "manufacturer": "FishFeed Pro"
                },
                {
                    "name": "Pond Fish Starter Feed",
                    "category": "feed",
                    "description": "Balanced nutrition for young fish in pond environments",
                    "price": 380.0,
                    "unit": "kg",
                    "stock_quantity": 400,
                    "manufacturer": "AquaNutrition Ltd"
                },
                {
                    "name": "High Protein Fish Feed",
                    "category": "feed",
                    "description": "45% protein content for accelerated growth in commercial aquaculture",
                    "price": 600.0,
                    "unit": "kg",
                    "stock_quantity": 250,
                    "manufacturer": "Premium Aqua Feeds"
                }
            ]

            # Medicine Products
            medicine_products = [
                {
                    "name": "Antibacterial Treatment",
                    "category": "medicine",
                    "description": "Broad-spectrum antibacterial for treating common fish infections",
                    "price": 850.0,
                    "unit": "100ml",
                    "stock_quantity": 80,
                    "manufacturer": "AquaHealth Solutions"
                },
                {
                    "name": "Anti-Fungal Solution",
                    "category": "medicine",
                    "description": "Effective treatment for fungal infections in freshwater fish",
                    "price": 1200.0,
                    "unit": "250ml",
                    "stock_quantity": 60,
                    "manufacturer": "Fish Care Plus"
                },
                {
                    "name": "Vitamin Supplement",
                    "category": "medicine",
                    "description": "Essential vitamins and minerals for fish immune system support",
                    "price": 650.0,
                    "unit": "500ml",
                    "stock_quantity": 100,
                    "manufacturer": "AquaHealth Solutions"
                },
                {
                    "name": "Stress Relief Tonic",
                    "category": "medicine",
                    "description": "Reduces stress during transport and handling of fish",
                    "price": 950.0,
                    "unit": "1L",
                    "stock_quantity": 70,
                    "manufacturer": "Fish Care Plus"
                },
                {
                    "name": "Anti-Parasitic Treatment",
                    "category": "medicine",
                    "description": "Eliminates external parasites like ich, anchor worms, and flukes",
                    "price": 1450.0,
                    "unit": "500ml",
                    "stock_quantity": 50,
                    "manufacturer": "AquaHealth Solutions"
                },
                {
                    "name": "Methylene Blue Solution",
                    "category": "medicine",
                    "description": "Treats fungal and bacterial infections, also used for egg disinfection",
                    "price": 580.0,
                    "unit": "250ml",
                    "stock_quantity": 90,
                    "manufacturer": "Fish Care Plus"
                },
                {
                    "name": "Malachite Green",
                    "category": "medicine",
                    "description": "Effective against protozoan parasites and fungal infections",
                    "price": 720.0,
                    "unit": "100ml",
                    "stock_quantity": 65,
                    "manufacturer": "AquaHealth Solutions"
                },
                {
                    "name": "Formalin 37%",
                    "category": "medicine",
                    "description": "Controls external parasites and improves gill function",
                    "price": 890.0,
                    "unit": "1L",
                    "stock_quantity": 45,
                    "manufacturer": "AquaMed Pro"
                },
                {
                    "name": "Potassium Permanganate",
                    "category": "medicine",
                    "description": "Oxidizing agent for treating bacterial and parasitic infections",
                    "price": 450.0,
                    "unit": "500g",
                    "stock_quantity": 120,
                    "manufacturer": "ChemAqua"
                },
                {
                    "name": "Oxytetracycline",
                    "category": "medicine",
                    "description": "Antibiotic for treating bacterial diseases in fish",
                    "price": 2100.0,
                    "unit": "100g",
                    "stock_quantity": 35,
                    "manufacturer": "VetAqua"
                },
                {
                    "name": "Erythromycin",
                    "category": "medicine",
                    "description": "Treats bacterial gill disease and other gram-positive infections",
                    "price": 1850.0,
                    "unit": "50g",
                    "stock_quantity": 40,
                    "manufacturer": "VetAqua"
                },
                {
                    "name": "Acriflavine Solution",
                    "category": "medicine",
                    "description": "Antiseptic for treating wounds and external infections",
                    "price": 680.0,
                    "unit": "250ml",
                    "stock_quantity": 75,
                    "manufacturer": "Fish Care Plus"
                },
                {
                    "name": "Salt (Aquaculture Grade)",
                    "category": "medicine",
                    "description": "Non-iodized salt for osmotic stress relief and parasite control",
                    "price": 180.0,
                    "unit": "5kg",
                    "stock_quantity": 200,
                    "manufacturer": "AquaSalt Co"
                },
                {
                    "name": "Probiotics for Fish",
                    "category": "medicine",
                    "description": "Beneficial bacteria to improve gut health and immunity",
                    "price": 1350.0,
                    "unit": "1kg",
                    "stock_quantity": 55,
                    "manufacturer": "BioCare Aqua"
                },
                {
                    "name": "Vitamin C Supplement",
                    "category": "medicine",
                    "description": "Boosts immune system and aids in disease recovery",
                    "price": 890.0,
                    "unit": "500g",
                    "stock_quantity": 85,
                    "manufacturer": "AquaHealth Solutions"
                },
                {
                    "name": "Mineral Supplement",
                    "category": "medicine",
                    "description": "Essential minerals for bone development and overall health",
                    "price": 750.0,
                    "unit": "1kg",
                    "stock_quantity": 95,
                    "manufacturer": "AquaHealth Solutions"
                },
                {
                    "name": "Wound Healing Gel",
                    "category": "medicine",
                    "description": "Topical gel for treating injuries and preventing secondary infections",
                    "price": 1150.0,
                    "unit": "100g",
                    "stock_quantity": 60,
                    "manufacturer": "Fish Care Plus"
                },
                {
                    "name": "Ammonia Detoxifier",
                    "category": "medicine",
                    "description": "Emergency treatment to neutralize toxic ammonia levels",
                    "price": 980.0,
                    "unit": "500ml",
                    "stock_quantity": 70,
                    "manufacturer": "WaterSafe"
                },
                {
                    "name": "Chloramine Neutralizer",
                    "category": "medicine",
                    "description": "Removes chlorine and chloramines from tap water",
                    "price": 520.0,
                    "unit": "1L",
                    "stock_quantity": 110,
                    "manufacturer": "WaterSafe"
                },
                {
                    "name": "Gill Treatment Solution",
                    "category": "medicine",
                    "description": "Specialized treatment for gill flukes and respiratory issues",
                    "price": 1620.0,
                    "unit": "500ml",
                    "stock_quantity": 48,
                    "manufacturer": "AquaMed Pro"
                },
                {
                    "name": "Ich Treatment Kit",
                    "category": "medicine",
                    "description": "Complete treatment kit for white spot disease (Ichthyophthirius)",
                    "price": 1280.0,
                    "unit": "kit",
                    "stock_quantity": 52,
                    "manufacturer": "Fish Care Plus"
                },
                {
                    "name": "Fin Rot Treatment",
                    "category": "medicine",
                    "description": "Treats fin and tail rot caused by bacterial infections",
                    "price": 780.0,
                    "unit": "250ml",
                    "stock_quantity": 88,
                    "manufacturer": "AquaHealth Solutions"
                },
                {
                    "name": "Dropsy Treatment",
                    "category": "medicine",
                    "description": "Specialized treatment for dropsy and bloating conditions",
                    "price": 1580.0,
                    "unit": "200ml",
                    "stock_quantity": 42,
                    "manufacturer": "VetAqua"
                },
                {
                    "name": "Nitrite/Nitrate Reducer",
                    "category": "medicine",
                    "description": "Bio-chemical solution to reduce toxic nitrogen compounds",
                    "price": 1100.0,
                    "unit": "1L",
                    "stock_quantity": 65,
                    "manufacturer": "WaterSafe"
                },
                {
                    "name": "Immune Booster Powder",
                    "category": "medicine",
                    "description": "Enhances natural immunity during disease outbreaks",
                    "price": 1450.0,
                    "unit": "500g",
                    "stock_quantity": 58,
                    "manufacturer": "BioCare Aqua"
                },
                {
                    "name": "Quarantine Treatment Kit",
                    "category": "medicine",
                    "description": "Complete kit for quarantining new fish arrivals",
                    "price": 2250.0,
                    "unit": "kit",
                    "stock_quantity": 30,
                    "manufacturer": "Fish Care Plus"
                }
            ]

            # Equipment Products
            equipment_products = [
                {
                    "name": "Oxygen Aerator 2000W",
                    "category": "equipment",
                    "description": "High-capacity aerator for maintaining optimal dissolved oxygen levels",
                    "price": 15000.0,
                    "unit": "piece",
                    "stock_quantity": 20,
                    "manufacturer": "AquaTech Industries"
                },
                {
                    "name": "Water Quality Test Kit",
                    "category": "equipment",
                    "description": "Complete testing kit for pH, ammonia, nitrite, nitrate, and dissolved oxygen",
                    "price": 2500.0,
                    "unit": "piece",
                    "stock_quantity": 50,
                    "manufacturer": "AquaTest Pro"
                },
                {
                    "name": "Digital pH Meter",
                    "category": "equipment",
                    "description": "Precision digital pH meter with automatic temperature compensation",
                    "price": 3500.0,
                    "unit": "piece",
                    "stock_quantity": 30,
                    "manufacturer": "AquaTest Pro"
                },
                {
                    "name": "Fish Net (Large)",
                    "category": "equipment",
                    "description": "Durable nylon net for safe fish handling and transfer",
                    "price": 450.0,
                    "unit": "piece",
                    "stock_quantity": 100,
                    "manufacturer": "AquaGear"
                },
                {
                    "name": "Automatic Feeder",
                    "category": "equipment",
                    "description": "Programmable automatic fish feeder with timer and portion control",
                    "price": 8500.0,
                    "unit": "piece",
                    "stock_quantity": 25,
                    "manufacturer": "AquaTech Industries"
                }
            ]

            # Combine all products
            all_products = feed_products + medicine_products + equipment_products

            # Add products to database
            for product_data in all_products:
                product = Product(
                    id=str(uuid.uuid4()),
                    **product_data,
                    created_at=datetime.utcnow(),
                    updated_at=datetime.utcnow()
                )
                db.add(product)

            print(f"✅ Created {len(all_products)} products:")
            print(f"   - {len(feed_products)} feed products")
            print(f"   - {len(medicine_products)} medicine products")
            print(f"   - {len(equipment_products)} equipment products")
        else:
            print(f"ℹ️  {existing_products} products already exist")

        # Commit the transaction
        db.commit()
        print("\n✅ Database seeding completed successfully!")

    except Exception as e:
        db.rollback()
        print(f"\n❌ Error seeding database: {e}")
        raise
    finally:
        db.close()

if __name__ == "__main__":
    print("🌱 Seeding database with sample data...\n")
    seed_database()
