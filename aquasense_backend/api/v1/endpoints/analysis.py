"""
Analysis Endpoints - Disease detection and tank analysis using AI/ML
"""
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File
from sqlalchemy.orm import Session
from typing import Optional
import base64

from config.database import get_db
from schemas.analysis import (
    DiseaseDetectionRequest,
    DiseaseDetectionResponse,
    TankAnalysisRequest,
    TankAnalysisResponse,
    AIRecommendationRequest,
    AIRecommendationResponse
)
from services.analysis_service import AnalysisService

router = APIRouter()


@router.post("/disease-detection", response_model=DiseaseDetectionResponse)
async def detect_disease(
    request: DiseaseDetectionRequest,
    db: Session = Depends(get_db)
):
    """
    Detect fish diseases from image and/or symptoms.
    """
    service = AnalysisService(db)
    result = await service.detect_disease(
        image_base64=request.image_base64,
        symptoms=request.symptoms,
        tank_id=request.tank_id
    )
    return result


@router.post("/disease-detection/upload", response_model=DiseaseDetectionResponse)
async def detect_disease_from_upload(
    file: UploadFile = File(...),
    tank_id: Optional[str] = None,
    db: Session = Depends(get_db)
):
    """
    Detect fish diseases from uploaded image file.
    """
    # Read and encode image
    image_bytes = await file.read()
    image_base64 = base64.b64encode(image_bytes).decode('utf-8')

    service = AnalysisService(db)
    result = await service.detect_disease(
        image_base64=image_base64,
        tank_id=tank_id
    )
    return result


@router.post("/disease-detection/ml-only")
async def detect_disease_ml_only(
    file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    """
    Detect fish diseases using only ML model (no Gemini AI).
    Simplified endpoint for mobile apps.
    """
    from ml.disease_classifier import DiseaseClassifier

    print("=" * 80)
    print("ML-ONLY DISEASE DETECTION ENDPOINT CALLED (iOS App)")
    print(f"Received file: {file.filename} ({file.content_type})")

    # Read and encode image
    image_bytes = await file.read()
    image_size_kb = len(image_bytes) / 1024
    print(f"Image size: {image_size_kb:.2f} KB")

    image_base64 = base64.b64encode(image_bytes).decode('utf-8')

    # Get ML predictions only
    print("STARTING ML MODEL PREDICTION...")
    classifier = DiseaseClassifier()
    predictions = await classifier.predict(image_base64)

    # Log predictions
    print(f"ML MODEL RETURNED {len(predictions)} PREDICTIONS:")
    for disease in predictions:
        print(f"  - {disease.name}: {disease.confidence:.2%}")

    if predictions:
        top_prediction = predictions[0]
        print(f"TOP PREDICTION: {top_prediction.name} ({top_prediction.confidence:.2%})")
    else:
        print("WARNING: NO PREDICTIONS RETURNED BY ML MODEL")

    print("=" * 80)

    return {
        "predictions": predictions,
        "message": "ML model predictions (no AI analysis)"
    }


@router.post("/tank-analysis", response_model=TankAnalysisResponse)
async def analyze_tank(
    request: TankAnalysisRequest,
    db: Session = Depends(get_db)
):
    """
    Perform comprehensive tank analysis including water quality and optional disease check.
    """
    service = AnalysisService(db)
    result = await service.analyze_tank(
        tank_id=request.tank_id,
        include_water_quality=request.include_water_quality,
        include_disease_check=request.include_disease_check,
        fish_image_base64=request.fish_image_base64
    )

    if not result:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tank with id {request.tank_id} not found"
        )

    return result


@router.get("/tank-analysis/{tank_id}", response_model=TankAnalysisResponse)
async def get_tank_analysis(
    tank_id: str,
    db: Session = Depends(get_db)
):
    """
    Get quick tank analysis (water quality only).
    """
    service = AnalysisService(db)
    result = await service.analyze_tank(
        tank_id=tank_id,
        include_water_quality=True,
        include_disease_check=False
    )

    if not result:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tank with id {tank_id} not found"
        )

    return result


@router.post("/recommendation", response_model=AIRecommendationResponse)
async def get_ai_recommendation(
    request: AIRecommendationRequest,
    db: Session = Depends(get_db)
):
    """
    Get AI-powered recommendations for aquaculture questions.
    """
    service = AnalysisService(db)
    result = await service.get_ai_recommendation(
        query=request.query,
        context=request.context
    )
    return result
