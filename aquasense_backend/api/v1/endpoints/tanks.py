"""
Tank Endpoints - CRUD operations for tanks and water quality
"""
from fastapi import APIRouter, Depends, HTTPException, status, Header
from sqlalchemy.orm import Session
from typing import List, Optional

from config.database import get_db
from schemas.tank import (
    TankCreate,
    TankUpdate,
    TankResponse,
    TankDetailResponse,
    WaterQualityCreate,
    WaterQualityResponse
)
from services.tank_service import TankService

router = APIRouter()


@router.get("/", response_model=List[TankResponse])
async def get_tanks(
    skip: int = 0,
    limit: int = 100,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Get all tanks for the current user.
    """
    user_id = x_user_id or "default_user_001"
    service = TankService(db, user_id=user_id)
    tanks = service.get_all_tanks(skip=skip, limit=limit)
    return tanks


@router.get("/dashboard")
async def get_dashboard_summary(
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Get dashboard summary with all tanks and their latest water quality.
    """
    user_id = x_user_id or "default_user_001"
    service = TankService(db, user_id=user_id)

    tanks = service.get_all_tanks()

    # Calculate totals
    total_fish = sum(tank.current_stock for tank in tanks)
    tanks_needing_attention = 0
    recent_alerts = []

    for tank in tanks:
        latest_reading = service.get_latest_water_quality(tank.id)
        if latest_reading:
            # Check if tank needs attention based on water quality
            needs_attention = False
            if latest_reading.ph and (latest_reading.ph < 6.5 or latest_reading.ph > 8.5):
                needs_attention = True
                recent_alerts.append({
                    "tank_id": tank.id,
                    "tank_name": tank.name,
                    "type": "ph_warning",
                    "message": f"pH level out of range: {latest_reading.ph}"
                })
            if latest_reading.ammonia and latest_reading.ammonia > 0.05:
                needs_attention = True
                recent_alerts.append({
                    "tank_id": tank.id,
                    "tank_name": tank.name,
                    "type": "ammonia_warning",
                    "message": f"High ammonia level: {latest_reading.ammonia}"
                })
            if latest_reading.nitrite and latest_reading.nitrite > 0.05:
                needs_attention = True
                recent_alerts.append({
                    "tank_id": tank.id,
                    "tank_name": tank.name,
                    "type": "nitrite_warning",
                    "message": f"High nitrite level: {latest_reading.nitrite}"
                })

            if needs_attention:
                tanks_needing_attention += 1

    summary = {
        "total_tanks": len(tanks),
        "total_fish": total_fish,
        "tanks_needing_attention": tanks_needing_attention,
        "recent_alerts": recent_alerts
    }

    return summary


@router.get("/{tank_id}", response_model=TankDetailResponse)
async def get_tank(
    tank_id: str,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Get a specific tank by ID with water quality readings.
    """
    user_id = x_user_id or "default_user_001"
    service = TankService(db, user_id=user_id)
    tank = service.get_tank_by_id(tank_id)
    if not tank:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tank with id {tank_id} not found"
        )
    return tank


@router.post("/", response_model=TankResponse, status_code=status.HTTP_201_CREATED)
async def create_tank(
    tank_data: TankCreate,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Create a new tank.
    """
    user_id = x_user_id or "default_user_001"
    service = TankService(db, user_id=user_id)
    tank = service.create_tank(tank_data)
    return tank


@router.put("/{tank_id}", response_model=TankResponse)
async def update_tank(
    tank_id: str,
    tank_data: TankUpdate,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Update an existing tank.
    """
    user_id = x_user_id or "default_user_001"
    service = TankService(db, user_id=user_id)
    tank = service.update_tank(tank_id, tank_data)
    if not tank:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tank with id {tank_id} not found"
        )
    return tank


@router.delete("/{tank_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_tank(
    tank_id: str,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Delete a tank.
    """
    user_id = x_user_id or "default_user_001"
    service = TankService(db, user_id=user_id)
    success = service.delete_tank(tank_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tank with id {tank_id} not found"
        )
    return None


@router.post("/{tank_id}/water-quality", response_model=WaterQualityResponse, status_code=status.HTTP_201_CREATED)
async def add_water_quality_reading(
    tank_id: str,
    reading: WaterQualityCreate,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Add a water quality reading for a tank.
    """
    user_id = x_user_id or "default_user_001"
    service = TankService(db, user_id=user_id)
    wq_reading = service.add_water_quality_reading(tank_id, reading)
    if not wq_reading:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tank with id {tank_id} not found"
        )
    return wq_reading


@router.get("/{tank_id}/water-quality", response_model=List[WaterQualityResponse])
async def get_water_quality_readings(
    tank_id: str,
    skip: int = 0,
    limit: int = 100,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Get water quality readings for a tank.
    """
    user_id = x_user_id or "default_user_001"
    service = TankService(db, user_id=user_id)
    readings = service.get_water_quality_readings(tank_id, skip=skip, limit=limit)
    return readings


@router.get("/{tank_id}/water-quality/latest", response_model=WaterQualityResponse)
async def get_latest_water_quality(
    tank_id: str,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Get the latest water quality reading for a tank.
    """
    user_id = x_user_id or "default_user_001"
    service = TankService(db, user_id=user_id)
    reading = service.get_latest_water_quality(tank_id)
    if not reading:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"No water quality readings found for tank {tank_id}"
        )
    return reading


@router.get("/{tank_id}/stats")
async def get_tank_stats(
    tank_id: str,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Get statistics for a tank including water quality averages.
    """
    user_id = x_user_id or "default_user_001"
    service = TankService(db, user_id=user_id)

    # Get tank
    tank = service.get_tank_by_id(tank_id)
    if not tank:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tank with id {tank_id} not found"
        )

    # Get water quality readings
    readings = service.get_water_quality_readings(tank_id, skip=0, limit=100)

    # Calculate averages
    avg_ph = None
    avg_temperature = None
    avg_dissolved_oxygen = None
    avg_ammonia = None
    avg_nitrite = None
    avg_nitrate = None

    if readings:
        ph_values = [r.ph for r in readings if r.ph is not None]
        temp_values = [r.temperature for r in readings if r.temperature is not None]
        do_values = [r.dissolved_oxygen for r in readings if r.dissolved_oxygen is not None]
        ammonia_values = [r.ammonia for r in readings if r.ammonia is not None]
        nitrite_values = [r.nitrite for r in readings if r.nitrite is not None]
        nitrate_values = [r.nitrate for r in readings if r.nitrate is not None]

        avg_ph = sum(ph_values) / len(ph_values) if ph_values else None
        avg_temperature = sum(temp_values) / len(temp_values) if temp_values else None
        avg_dissolved_oxygen = sum(do_values) / len(do_values) if do_values else None
        avg_ammonia = sum(ammonia_values) / len(ammonia_values) if ammonia_values else None
        avg_nitrite = sum(nitrite_values) / len(nitrite_values) if nitrite_values else None
        avg_nitrate = sum(nitrate_values) / len(nitrate_values) if nitrate_values else None

    # Calculate simple health score based on ideal ranges
    health_score = 100.0
    if avg_ph is not None:
        # Ideal pH: 6.5-8.5
        if avg_ph < 6.5 or avg_ph > 8.5:
            health_score -= 20
    if avg_temperature is not None:
        # Ideal temp: 20-28°C
        if avg_temperature < 20 or avg_temperature > 28:
            health_score -= 20
    if avg_ammonia is not None and avg_ammonia > 0.05:
        health_score -= 30
    if avg_nitrite is not None and avg_nitrite > 0.05:
        health_score -= 30

    stats = {
        "tank_id": tank_id,
        "tank_name": tank.name,
        "total_readings": len(readings),
        "avg_ph": avg_ph,
        "avg_temperature": avg_temperature,
        "avg_dissolved_oxygen": avg_dissolved_oxygen,
        "avg_ammonia": avg_ammonia,
        "avg_nitrite": avg_nitrite,
        "avg_nitrate": avg_nitrate,
        "health_score": health_score
    }

    return stats
