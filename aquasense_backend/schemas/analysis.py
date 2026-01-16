"""
Analysis Schemas - Disease detection and tank analysis
"""
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime


class DiseaseDetectionRequest(BaseModel):
    """Schema for disease detection request"""
    image_base64: Optional[str] = None
    symptoms: Optional[List[str]] = None
    tank_id: Optional[str] = None


class DiseaseInfo(BaseModel):
    """Schema for disease information"""
    name: str
    confidence: float = Field(..., ge=0, le=1)
    description: str
    causes: List[str]
    symptoms: List[str]
    treatment: str
    prevention: List[str]


class DiseaseDetectionResponse(BaseModel):
    """Schema for disease detection response"""
    detected_diseases: List[DiseaseInfo]
    recommendation: str
    severity: str  # low, medium, high, critical
    urgent_action_required: bool
    timestamp: datetime


class TankAnalysisRequest(BaseModel):
    """Schema for tank analysis request"""
    tank_id: str
    include_water_quality: bool = True
    include_disease_check: bool = False
    fish_image_base64: Optional[str] = None


class WaterQualityAnalysis(BaseModel):
    """Schema for water quality analysis"""
    status: str  # excellent, good, fair, poor, critical
    issues: List[str]
    recommendations: List[str]
    parameters: Dict[str, Dict[str, Any]]  # parameter_name: {value, status, optimal_range}


class TankAnalysisResponse(BaseModel):
    """Schema for comprehensive tank analysis"""
    tank_id: str
    tank_name: str
    overall_health_score: float = Field(..., ge=0, le=100)
    water_quality_analysis: Optional[WaterQualityAnalysis]
    disease_detection: Optional[DiseaseDetectionResponse]
    general_recommendations: List[str]
    alerts: List[str]
    timestamp: datetime


class AIRecommendationRequest(BaseModel):
    """Schema for AI recommendation request"""
    query: str = Field(..., min_length=1)
    context: Optional[Dict] = None  # Additional context like tank_id, species, etc.


class AIRecommendationResponse(BaseModel):
    """Schema for AI recommendation response"""
    recommendation: str
    sources: Optional[List[str]] = None
    confidence: Optional[float] = None
    timestamp: datetime
