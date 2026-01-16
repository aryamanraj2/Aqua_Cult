"""
Product Model - Marketplace items
"""
from sqlalchemy import Column, String, Float, Integer, DateTime, Text
from datetime import datetime
from config.database import Base
import uuid


class Product(Base):
    __tablename__ = "products"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String(255), nullable=False)
    category = Column(String(100), nullable=False)  # feed, medicine, equipment, etc.
    description = Column(Text, nullable=True)
    price = Column(Float, nullable=False)
    unit = Column(String(50), nullable=False)  # kg, liters, pieces, etc.
    stock_quantity = Column(Integer, default=0)
    image_url = Column(String(500), nullable=True)
    manufacturer = Column(String(255), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
