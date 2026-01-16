"""
Voice Service - Business logic for voice agent interactions
"""
from sqlalchemy.orm import Session
from typing import Dict, Optional, Any
from datetime import datetime, timedelta
import base64

from ai.gemini_client import GeminiClient
from ai.session_memory import SessionMemory
from services.tank_service import TankService
from services.product_service import ProductService


class VoiceService:
    """Service class for voice agent operations"""

    def __init__(self, db: Session, session_id: str):
        self.db = db
        self.session_id = session_id
        self.gemini_client = GeminiClient()
        self.session_memory = SessionMemory(session_id)
        self.tank_service = TankService(db)
        self.product_service = ProductService(db)

    def _is_aquaculture_related(self, text: str) -> tuple[bool, str]:
        """
        Quick keyword-based filter to detect obviously off-topic questions.
        Returns: Tuple of (is_valid, reason)
        """
        text_lower = text.lower()

        # Off-topic keywords (blacklist - high confidence)
        offtopic_keywords = {
            'movie', 'music', 'sport', 'game', 'celebrity', 'politics', 'election',
            'news', 'weather', 'recipe', 'cooking', 'restaurant', 'president',
            'football', 'basketball', 'soccer', 'actor', 'song', 'album', 'concert',
            'tv show', 'netflix', 'youtube', 'instagram', 'twitter', 'facebook',
            'stock market', 'cryptocurrency', 'bitcoin', 'travel', 'vacation'
        }

        # Check for obvious off-topic keywords
        for keyword in offtopic_keywords:
            if keyword in text_lower:
                return False, f"detected off-topic keyword: {keyword}"

        # Allow by default (defer to Gemini prompt engineering)
        return True, "no clear off-topic indicators, defer to AI"

    def _enrich_tank_context(self, metadata: Optional[Dict] = None) -> Dict:
        """
        Enrich metadata with full tank data for all tanks.

        Args:
            metadata: Original metadata from client
                - May contain "all_tanks_data" (JSON string with full tank data from Android)
                - Or "all_tank_ids" (pipe-separated IDs to fetch from database)
                - May contain "primary_tank_id" (focused tank ID)

        Returns:
            Enriched metadata with full tank information for all user's tanks
        """
        if not metadata:
            return {}

        # Start with empty dict
        enriched = {}

        # OPTION 1: Use full tank data sent from Android client (preferred - more efficient)
        all_tanks_data_str = metadata.get("all_tanks_data")
        if all_tanks_data_str:
            try:
                import json
                all_tanks = json.loads(all_tanks_data_str)
                enriched["all_tanks"] = all_tanks
            except Exception as e:
                import logging
                logging.error(f"Error parsing all_tanks_data JSON: {str(e)}")
                # Fall through to OPTION 2

        # OPTION 2: Fetch full details from database if only IDs provided (fallback)
        if "all_tanks" not in enriched:
            all_tank_ids_str = metadata.get("all_tank_ids")
            if all_tank_ids_str:
                try:
                    tank_ids = all_tank_ids_str.split("|")
                    all_tanks = []

                    for tid in tank_ids:
                        tank = self.tank_service.get_tank_by_id(tid)
                        if tank:
                            tank_data = {
                                "id": tank.id,
                                "name": tank.name,
                                "species": tank.species,
                                "capacity": float(tank.capacity) if tank.capacity else 0.0,
                                "current_stock": int(tank.current_stock) if tank.current_stock else 0,
                                "location": tank.location or "",
                                "status": tank.status
                            }

                            # Add water quality for each tank
                            latest_wq = self.tank_service.get_latest_water_quality(tid)
                            if latest_wq:
                                tank_data["water_quality"] = {
                                    "ph": float(latest_wq.ph) if latest_wq.ph else None,
                                    "temperature": float(latest_wq.temperature) if latest_wq.temperature else None,
                                    "dissolved_oxygen": float(latest_wq.dissolved_oxygen) if latest_wq.dissolved_oxygen else None,
                                    "ammonia": float(latest_wq.ammonia) if latest_wq.ammonia else None,
                                    "nitrite": float(latest_wq.nitrite) if latest_wq.nitrite else None,
                                    "nitrate": float(latest_wq.nitrate) if latest_wq.nitrate else None,
                                    "salinity": float(latest_wq.salinity) if latest_wq.salinity else None,
                                    "turbidity": float(latest_wq.turbidity) if latest_wq.turbidity else None,
                                    "recorded_at": latest_wq.recorded_at.isoformat() if latest_wq.recorded_at else None
                                }

                            all_tanks.append(tank_data)

                    enriched["all_tanks"] = all_tanks
                except Exception as e:
                    import logging
                    logging.error(f"Error fetching all tanks: {str(e)}")

        # If primary_tank_id is provided, mark it for focused context
        primary_tank_id = metadata.get("primary_tank_id")
        if primary_tank_id:
            enriched["primary_tank_id"] = primary_tank_id

        return enriched

    async def process_text_input(self, text: str, metadata: Optional[Dict] = None) -> Dict[str, Any]:
        """
        Process text input from user and generate response.
        """
        # LAYER 2: Pre-processing filter (quick rejection of obvious off-topic)
        is_valid, reason = self._is_aquaculture_related(text)

        if not is_valid:
            # Return standardized off-topic response without calling Gemini
            off_topic_response = "I can only help with aquaculture and tank management questions. Please ask about your tanks, water quality, fish care, diseases, or products."

            # Still log to session memory for context
            self.session_memory.add_message("user", text)
            self.session_memory.add_message("assistant", off_topic_response,
                                           metadata={"rejected_reason": reason})

            return {
                "type": "text",
                "content": off_topic_response,
                "action": None,
                "data": None,
                "timestamp": datetime.utcnow()
            }

        # Enrich metadata with tank data if tank_id is provided
        enriched_metadata = self._enrich_tank_context(metadata)

        # Add to session memory
        self.session_memory.add_message("user", text)

        # Get conversation history
        history = self.session_memory.get_history()

        # LAYER 1: Process with enhanced Gemini prompt (primary defense)
        response = await self.gemini_client.process_voice_query(
            query=text,
            history=history,
            context=enriched_metadata
        )

        # Add response to session memory
        self.session_memory.add_message("assistant", response["text"])

        # Check if action is required
        action = None
        action_data = None

        if response.get("action"):
            action = response["action"]
            action_data = await self._execute_action(action, response.get("action_params"))

        return {
            "type": "text",
            "content": response["text"],
            "action": action,
            "data": action_data,
            "timestamp": datetime.utcnow()
        }

    async def process_audio_input(self, audio_base64: str) -> Dict[str, Any]:
        """
        Process audio input (to be implemented with STT on client side).
        This is a placeholder for server-side audio processing if needed.
        """
        # For now, return error as STT is handled on Android
        return {
            "type": "error",
            "content": "Audio processing should be handled on client side with Android SpeechRecognizer",
            "timestamp": datetime.utcnow()
        }

    async def _execute_action(self, action: str, params: Optional[Dict] = None) -> Optional[Dict]:
        """
        Execute actions requested by the AI agent.
        """
        if not params:
            params = {}

        if action == "get_tanks":
            tanks = self.tank_service.get_all_tanks()
            return {"tanks": [self._tank_to_dict(t) for t in tanks]}

        elif action == "get_tank_details":
            tank_id = params.get("tank_id")
            if tank_id:
                tank = self.tank_service.get_tank_by_id(tank_id)
                return {"tank": self._tank_to_dict(tank)} if tank else None

        elif action == "add_water_quality":
            tank_id = params.get("tank_id")
            # This would require proper reading data
            # Return placeholder for now
            return {"message": "Water quality reading would be added"}

        elif action == "search_products":
            category = params.get("category")
            products = self.product_service.get_all_products(category=category)
            return {"products": [self._product_to_dict(p) for p in products]}

        elif action == "navigate":
            destination = params.get("destination")
            return {"destination": destination}

        return None

    def _tank_to_dict(self, tank) -> Dict:
        """Convert tank model to dictionary."""
        if not tank:
            return {}
        return {
            "id": tank.id,
            "name": tank.name,
            "species": tank.species,
            "capacity": tank.capacity,
            "current_stock": tank.current_stock
        }

    def _product_to_dict(self, product) -> Dict:
        """Convert product model to dictionary."""
        if not product:
            return {}
        return {
            "id": product.id,
            "name": product.name,
            "category": product.category,
            "price": product.price,
            "unit": product.unit
        }

    def clear_session(self):
        """Clear session memory."""
        self.session_memory.clear()
