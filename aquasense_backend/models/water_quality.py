"""
Water Quality Model
"""
from sqlalchemy import Column, String, Float, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from datetime import datetime
from config.database import Base
import uuid


class WaterQuality(Base):
    __tablename__ = "water_quality_readings"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    tank_id = Column(String, ForeignKey("tanks.id"), nullable=False)
    ph = Column(Float, nullable=False)
    temperature = Column(Float, nullable=False)  # in Celsius
    dissolved_oxygen = Column(Float, nullable=False)  # in mg/L
    ammonia = Column(Float, nullable=True)  # in mg/L
    nitrite = Column(Float, nullable=True)  # in mg/L
    nitrate = Column(Float, nullable=True)  # in mg/L
    salinity = Column(Float, nullable=True)  # in ppt
    turbidity = Column(Float, nullable=True)  # in NTU
    notes = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    # Relationships
    tank = relationship("Tank", back_populates="water_quality_readings")
