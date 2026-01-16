"""
Product and Order Endpoints - Marketplace functionality
"""
from fastapi import APIRouter, Depends, HTTPException, status, Query, Header
from sqlalchemy.orm import Session
from typing import List, Optional

from config.database import get_db
from schemas.product import (
    ProductCreate,
    ProductUpdate,
    ProductResponse,
    OrderCreate,
    OrderUpdate,
    OrderResponse
)
from services.product_service import ProductService

router = APIRouter()
orders_router = APIRouter()


# Product endpoints (order matters - specific routes before dynamic ones)
@router.get("/categories")
async def get_categories(db: Session = Depends(get_db)):
    """
    Get all product categories.
    """
    service = ProductService(db)
    categories = service.get_all_categories()
    return categories


@router.get("/categories/{category}/stats")
async def get_category_stats(
    category: str,
    db: Session = Depends(get_db)
):
    """
    Get statistics for a specific category.
    """
    service = ProductService(db)
    stats = service.get_category_stats(category)
    return stats


@router.get("/search", response_model=List[ProductResponse])
async def search_products(
    q: str = Query(..., min_length=1),
    db: Session = Depends(get_db)
):
    """
    Search products by name or description.
    """
    service = ProductService(db)
    products = service.search_products(q)
    return products


@router.get("/", response_model=List[ProductResponse])
async def get_products(
    category: Optional[str] = None,
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """
    Get all products, optionally filtered by category.
    """
    service = ProductService(db)
    products = service.get_all_products(category=category, skip=skip, limit=limit)
    return products


@router.get("/{product_id}", response_model=ProductResponse)
async def get_product(
    product_id: str,
    db: Session = Depends(get_db)
):
    """
    Get a specific product by ID.
    """
    service = ProductService(db)
    product = service.get_product_by_id(product_id)
    if not product:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Product with id {product_id} not found"
        )
    return product


@router.post("/", response_model=ProductResponse, status_code=status.HTTP_201_CREATED)
async def create_product(
    product_data: ProductCreate,
    db: Session = Depends(get_db)
):
    """
    Create a new product (admin only in production).
    """
    service = ProductService(db)
    product = service.create_product(product_data)
    return product


@router.put("/{product_id}", response_model=ProductResponse)
async def update_product(
    product_id: str,
    product_data: ProductUpdate,
    db: Session = Depends(get_db)
):
    """
    Update an existing product.
    """
    service = ProductService(db)
    product = service.update_product(product_id, product_data)
    if not product:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Product with id {product_id} not found"
        )
    return product


@router.delete("/{product_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_product(
    product_id: str,
    db: Session = Depends(get_db)
):
    """
    Delete a product.
    """
    service = ProductService(db)
    success = service.delete_product(product_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Product with id {product_id} not found"
        )
    return None


# Order endpoints
@orders_router.post("/", response_model=OrderResponse, status_code=status.HTTP_201_CREATED)
async def create_order(
    order_data: OrderCreate,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Create a new order.
    """
    user_id = x_user_id or "default_user_001"
    service = ProductService(db, user_id=user_id)
    try:
        order = service.create_order(order_data)
        return order
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )


@orders_router.get("/", response_model=List[OrderResponse])
async def get_orders(
    skip: int = 0,
    limit: int = 100,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Get all orders for the current user.
    """
    user_id = x_user_id or "default_user_001"
    service = ProductService(db, user_id=user_id)
    orders = service.get_all_orders(skip=skip, limit=limit)
    return orders


@orders_router.get("/{order_id}", response_model=OrderResponse)
async def get_order(
    order_id: str,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Get a specific order by ID.
    """
    user_id = x_user_id or "default_user_001"
    service = ProductService(db, user_id=user_id)
    order = service.get_order_by_id(order_id)
    if not order:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Order with id {order_id} not found"
        )
    return order


@orders_router.patch("/{order_id}", response_model=OrderResponse)
async def update_order(
    order_id: str,
    order_data: OrderUpdate,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Update an order (status, payment, etc.).
    """
    user_id = x_user_id or "default_user_001"
    service = ProductService(db, user_id=user_id)
    order = service.update_order(order_id, order_data)
    if not order:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Order with id {order_id} not found"
        )
    return order


@orders_router.post("/{order_id}/cancel", response_model=OrderResponse)
async def cancel_order(
    order_id: str,
    x_user_id: Optional[str] = Header(None, alias="X-User-ID"),
    db: Session = Depends(get_db)
):
    """
    Cancel an order.
    """
    user_id = x_user_id or "default_user_001"
    service = ProductService(db, user_id=user_id)
    try:
        order = service.cancel_order(order_id)
        if not order:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Order with id {order_id} not found"
            )
        return order
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
