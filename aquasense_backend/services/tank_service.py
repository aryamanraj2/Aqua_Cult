"""
Tank Service - Business logic for tank management
"""
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime

from models.tank import Tank
from models.water_quality import WaterQuality
from schemas.tank import TankCreate, TankUpdate, WaterQualityCreate


class TankService:
    """Service class for tank operations"""

    def __init__(self, db: Session, user_id: str = "default_user_001"):
        self.db = db
        self.user_id = user_id

    def get_all_tanks(self, skip: int = 0, limit: int = 100) -> List[Tank]:
        """
        Get all tanks for the current user.
        """
        return self.db.query(Tank)\
            .filter(Tank.user_id == self.user_id)\
            .offset(skip)\
            .limit(limit)\
            .all()

    def get_tank_by_id(self, tank_id: str) -> Optional[Tank]:
        """
        Get a specific tank by ID.
        """
        return self.db.query(Tank)\
            .filter(Tank.id == tank_id, Tank.user_id == self.user_id)\
            .first()

    def create_tank(self, tank_data: TankCreate) -> Tank:
        """
        Create a new tank.
        """
        tank = Tank(
            user_id=self.user_id,
            name=tank_data.name,
            species=tank_data.species,
            capacity=tank_data.capacity,
            current_stock=tank_data.current_stock,
            location=tank_data.location,
            notes=tank_data.notes
        )
        self.db.add(tank)
        self.db.commit()
        self.db.refresh(tank)
        return tank

    def update_tank(self, tank_id: str, tank_data: TankUpdate) -> Optional[Tank]:
        """
        Update an existing tank.
        """
        tank = self.get_tank_by_id(tank_id)
        if not tank:
            return None

        # Update fields if provided
        update_data = tank_data.dict(exclude_unset=True)
        for field, value in update_data.items():
            setattr(tank, field, value)

        tank.updated_at = datetime.utcnow()
        self.db.commit()
        self.db.refresh(tank)
        return tank

    def delete_tank(self, tank_id: str) -> bool:
        """
        Delete a tank.
        """
        tank = self.get_tank_by_id(tank_id)
        if not tank:
            return False

        self.db.delete(tank)
        self.db.commit()
        return True

    def add_water_quality_reading(
        self,
        tank_id: str,
        reading: WaterQualityCreate
    ) -> Optional[WaterQuality]:
        """
        Add a water quality reading for a tank.
        """
        # Verify tank exists
        tank = self.get_tank_by_id(tank_id)
        if not tank:
            return None

        wq_reading = WaterQuality(
            tank_id=tank_id,
            ph=reading.ph,
            temperature=reading.temperature,
            dissolved_oxygen=reading.dissolved_oxygen,
            ammonia=reading.ammonia,
            nitrite=reading.nitrite,
            nitrate=reading.nitrate,
            salinity=reading.salinity,
            notes=reading.notes
        )
        self.db.add(wq_reading)
        self.db.commit()
        self.db.refresh(wq_reading)
        return wq_reading

    def get_water_quality_readings(
        self,
        tank_id: str,
        skip: int = 0,
        limit: int = 100
    ) -> List[WaterQuality]:
        """
        Get water quality readings for a tank.
        """
        return self.db.query(WaterQuality)\
            .filter(WaterQuality.tank_id == tank_id)\
            .order_by(WaterQuality.created_at.desc())\
            .offset(skip)\
            .limit(limit)\
            .all()

    def get_latest_water_quality(self, tank_id: str) -> Optional[WaterQuality]:
        """
        Get the most recent water quality reading for a tank.
        """
        return self.db.query(WaterQuality)\
            .filter(WaterQuality.tank_id == tank_id)\
            .order_by(WaterQuality.created_at.desc())\
            .first()
