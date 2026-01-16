"""
Product Service - Business logic for products and orders
"""
from sqlalchemy.orm import Session
from typing import List, Optional

from models.product import Product
from models.order import Order
from schemas.product import (
    ProductCreate,
    ProductUpdate,
    OrderCreate,
    OrderUpdate
)


class ProductService:
    """Service class for product and order operations"""

    def __init__(self, db: Session, user_id: str = "default_user_001"):
        self.db = db
        self.user_id = user_id

    # Product methods
    def get_all_products(
        self,
        category: Optional[str] = None,
        skip: int = 0,
        limit: int = 100
    ) -> List[Product]:
        """
        Get all products, optionally filtered by category.
        """
        query = self.db.query(Product)

        if category:
            query = query.filter(Product.category == category)

        return query.offset(skip).limit(limit).all()

    def get_product_by_id(self, product_id: str) -> Optional[Product]:
        """
        Get a specific product by ID.
        """
        return self.db.query(Product).filter(Product.id == product_id).first()

    def create_product(self, product_data: ProductCreate) -> Product:
        """
        Create a new product.
        """
        product = Product(**product_data.dict())
        self.db.add(product)
        self.db.commit()
        self.db.refresh(product)
        return product

    def update_product(
        self,
        product_id: str,
        product_data: ProductUpdate
    ) -> Optional[Product]:
        """
        Update an existing product.
        """
        product = self.get_product_by_id(product_id)
        if not product:
            return None

        update_data = product_data.dict(exclude_unset=True)
        for field, value in update_data.items():
            setattr(product, field, value)

        self.db.commit()
        self.db.refresh(product)
        return product

    def delete_product(self, product_id: str) -> bool:
        """
        Delete a product.
        """
        product = self.get_product_by_id(product_id)
        if not product:
            return False

        self.db.delete(product)
        self.db.commit()
        return True

    def search_products(self, query: str) -> List[Product]:
        """
        Search products by name or description.
        """
        search_term = f"%{query.lower()}%"
        return self.db.query(Product).filter(
            (Product.name.ilike(search_term)) |
            (Product.description.ilike(search_term))
        ).all()

    def get_all_categories(self) -> List[str]:
        """
        Get list of all unique product categories.
        """
        categories = self.db.query(Product.category).distinct().all()
        return [cat[0] for cat in categories]

    def get_category_stats(self, category: str) -> dict:
        """
        Get statistics for a specific category.
        """
        products = self.db.query(Product).filter(Product.category == category).all()

        if not products:
            return {
                "category": category,
                "total_products": 0,
                "avg_price": 0.0
            }

        return {
            "category": category,
            "total_products": len(products),
            "avg_price": sum(p.price for p in products) / len(products)
        }

    # Order methods
    def create_order(self, order_data: OrderCreate) -> Order:
        """
        Create a new order with stock validation.
        """
        # Validate stock and calculate total
        total_amount = 0
        items_dict = []

        for item in order_data.items:
            product = self.get_product_by_id(item.product_id)
            if not product:
                raise ValueError(f"Product {item.product_id} not found")

            if product.stock_quantity < item.quantity:
                raise ValueError(
                    f"Insufficient stock for {product.name}. "
                    f"Available: {product.stock_quantity}, Requested: {item.quantity}"
                )

            # Use product's current price
            item_dict = {
                "product_id": item.product_id,
                "quantity": item.quantity,
                "price": product.price
            }
            items_dict.append(item_dict)
            total_amount += product.price * item.quantity

        # Use shipping_address if provided, otherwise delivery_address
        address = order_data.shipping_address or order_data.delivery_address

        order = Order(
            user_id=self.user_id,
            items=items_dict,
            total_amount=total_amount,
            delivery_address=address,
            payment_method=order_data.payment_method,
            notes=order_data.notes
        )
        self.db.add(order)

        # Deduct stock for each item
        for item in order_data.items:
            product = self.get_product_by_id(item.product_id)
            if product:
                product.stock_quantity -= item.quantity

        self.db.commit()
        self.db.refresh(order)
        return order

    def get_all_orders(self, skip: int = 0, limit: int = 100) -> List[Order]:
        """
        Get all orders for the current user.
        """
        return self.db.query(Order)\
            .filter(Order.user_id == self.user_id)\
            .order_by(Order.created_at.desc())\
            .offset(skip)\
            .limit(limit)\
            .all()

    def get_order_by_id(self, order_id: str) -> Optional[Order]:
        """
        Get a specific order by ID.
        """
        return self.db.query(Order)\
            .filter(Order.id == order_id, Order.user_id == self.user_id)\
            .first()

    def update_order(self, order_id: str, order_data: OrderUpdate) -> Optional[Order]:
        """
        Update an order (status, payment, etc.).
        """
        order = self.get_order_by_id(order_id)
        if not order:
            return None

        update_data = order_data.dict(exclude_unset=True)
        for field, value in update_data.items():
            setattr(order, field, value)

        self.db.commit()
        self.db.refresh(order)
        return order

    def cancel_order(self, order_id: str) -> Optional[Order]:
        """
        Cancel an order. Only pending/confirmed orders can be cancelled.
        Restores stock for cancelled orders.
        """
        order = self.get_order_by_id(order_id)
        if not order:
            return None

        # Check if order can be cancelled
        if order.status in ["shipped", "delivered", "cancelled"]:
            raise ValueError(f"Cannot cancel order with status '{order.status}'")

        order.status = "cancelled"

        # Restore stock for each item
        for item in order.items:
            product = self.get_product_by_id(item['product_id'])
            if product:
                product.stock_quantity += item['quantity']

        self.db.commit()
        self.db.refresh(order)
        return order
