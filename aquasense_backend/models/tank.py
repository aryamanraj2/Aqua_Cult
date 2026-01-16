"""
Tank Model
"""
from sqlalchemy import Column, String, Integer, Float, DateTime, ForeignKey, JSON
from sqlalchemy.orm import relationship
from datetime import datetime
from config.database import Base
import uuid


class Tank(Base):
    __tablename__ = "tanks"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), nullable=False)
    name = Column(String(255), nullable=False)
    species = Column(JSON, nullable=False)  # List of fish species
    capacity = Column(Float, nullable=False)  # in liters
    current_stock = Column(Integer, default=0)  # number of fish
    location = Column(String(255), nullable=True)
    status = Column(String(50), default="active")  # active, inactive, maintenance
    notes = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    # Relationships
    user = relationship("User", back_populates="tanks")
    water_quality_readings = relationship("WaterQuality", back_populates="tank", cascade="all, delete-orphan")
