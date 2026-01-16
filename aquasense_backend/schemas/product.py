"""
Product and Order Schemas
"""
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime


class ProductCreate(BaseModel):
    """Schema for creating a product"""
    name: str = Field(..., min_length=1, max_length=255)
    category: str = Field(..., min_length=1, max_length=100)
    description: Optional[str] = None
    price: float = Field(..., gt=0)
    unit: str = Field(..., min_length=1, max_length=50)
    stock_quantity: int = Field(0, ge=0)
    image_url: Optional[str] = None
    manufacturer: Optional[str] = None


class ProductUpdate(BaseModel):
    """Schema for updating a product"""
    name: Optional[str] = Field(None, min_length=1, max_length=255)
    category: Optional[str] = None
    description: Optional[str] = None
    price: Optional[float] = Field(None, gt=0)
    unit: Optional[str] = None
    stock_quantity: Optional[int] = Field(None, ge=0)
    image_url: Optional[str] = None
    manufacturer: Optional[str] = None


class ProductResponse(BaseModel):
    """Schema for product response"""
    id: str
    name: str
    category: str
    description: Optional[str]
    price: float
    unit: str
    stock_quantity: int
    image_url: Optional[str]
    manufacturer: Optional[str]
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class OrderItemCreate(BaseModel):
    """Schema for order item in creation"""
    product_id: str
    quantity: int = Field(..., gt=0)


class OrderItem(BaseModel):
    """Schema for order item"""
    product_id: str
    quantity: int = Field(..., gt=0)
    price: float = Field(..., gt=0)


class OrderCreate(BaseModel):
    """Schema for creating an order"""
    items: List[OrderItemCreate] = Field(..., min_items=1)
    shipping_address: Optional[str] = None
    delivery_address: Optional[str] = None
    payment_method: Optional[str] = None
    notes: Optional[str] = None


class OrderUpdate(BaseModel):
    """Schema for updating an order"""
    status: Optional[str] = None
    payment_status: Optional[str] = None
    delivery_address: Optional[str] = None
    notes: Optional[str] = None


class OrderResponse(BaseModel):
    """Schema for order response"""
    id: str
    user_id: str
    items: List[dict]
    total_amount: float
    status: str
    delivery_address: Optional[str]
    payment_method: Optional[str]
    payment_status: str
    notes: Optional[str]
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
