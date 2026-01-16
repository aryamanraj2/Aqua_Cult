"""
Tank Schemas - Request/Response models
"""
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime


class WaterQualityCreate(BaseModel):
    """Schema for creating water quality reading"""
    ph: float = Field(..., ge=0, le=14, description="pH level (0-14)")
    temperature: float = Field(..., ge=-10, le=50, description="Temperature in Celsius")
    dissolved_oxygen: float = Field(..., ge=0, description="Dissolved oxygen in mg/L")
    ammonia: Optional[float] = Field(None, ge=0, description="Ammonia in mg/L")
    nitrite: Optional[float] = Field(None, ge=0, description="Nitrite in mg/L")
    nitrate: Optional[float] = Field(None, ge=0, description="Nitrate in mg/L")
    salinity: Optional[float] = Field(None, ge=0, description="Salinity in ppt")
    turbidity: Optional[float] = Field(None, ge=0, description="Turbidity in NTU")
    notes: Optional[str] = None


class WaterQualityResponse(BaseModel):
    """Schema for water quality reading response"""
    id: str
    tank_id: str
    ph: float
    temperature: float
    dissolved_oxygen: float
    ammonia: Optional[float]
    nitrite: Optional[float]
    nitrate: Optional[float]
    salinity: Optional[float]
    turbidity: Optional[float]
    notes: Optional[str]
    created_at: datetime

    class Config:
        from_attributes = True


class TankCreate(BaseModel):
    """Schema for creating a tank"""
    name: str = Field(..., min_length=1, max_length=255)
    species: List[str] = Field(..., min_items=1)
    capacity: float = Field(..., gt=0, description="Tank capacity in liters")
    current_stock: int = Field(0, ge=0, description="Number of fish")
    location: Optional[str] = None
    status: str = Field("active", description="Tank status: active, inactive, maintenance")
    notes: Optional[str] = None


class TankUpdate(BaseModel):
    """Schema for updating a tank"""
    name: Optional[str] = Field(None, min_length=1, max_length=255)
    species: Optional[List[str]] = None
    capacity: Optional[float] = Field(None, gt=0)
    current_stock: Optional[int] = Field(None, ge=0)
    location: Optional[str] = None
    status: Optional[str] = None
    notes: Optional[str] = None


class TankResponse(BaseModel):
    """Schema for tank response"""
    id: str
    user_id: str
    name: str
    species: List[str]
    capacity: float
    current_stock: int
    location: Optional[str]
    status: str
    notes: Optional[str]
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class TankDetailResponse(TankResponse):
    """Schema for detailed tank response with water quality readings"""
    water_quality_readings: List[WaterQualityResponse] = []

    class Config:
        from_attributes = True
