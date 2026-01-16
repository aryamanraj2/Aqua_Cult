"""
Gemini AI Client - Direct integration with Google Gemini API
"""
import google.generativeai as genai
from typing import Dict, List, Optional, Any
import json
import logging

from config.settings import settings
from ai.prompts import (
    DISEASE_ANALYSIS_PROMPT,
    WATER_QUALITY_PROMPT,
    TANK_RECOMMENDATION_PROMPT,
    VOICE_AGENT_PROMPT,
    GENERAL_RECOMMENDATION_PROMPT
)

logger = logging.getLogger(__name__)


class GeminiClient:
    """Client for interacting with Google Gemini API"""

    def __init__(self):
        """Initialize Gemini client with API key"""
        genai.configure(api_key=settings.GEMINI_API_KEY)
        self.model = genai.GenerativeModel(settings.GEMINI_MODEL)
        self.generation_config = {
            "temperature": settings.GEMINI_TEMPERATURE,
            "max_output_tokens": settings.GEMINI_MAX_TOKENS,
        }

    async def analyze_disease(
        self,
        image_base64: Optional[str] = None,
        symptoms: Optional[List[str]] = None,
        context: Optional[Dict] = None
    ) -> Dict[str, Any]:
        """
        Analyze fish disease using Gemini AI.
        """
        try:
            prompt = DISEASE_ANALYSIS_PROMPT.format(
                symptoms=symptoms if symptoms else "No symptoms provided",
                context=json.dumps(context) if context else "{}"
            )

            # Generate response
            if image_base64:
                # TODO: Handle image input when needed
                response = self.model.generate_content(
                    prompt,
                    generation_config=self.generation_config
                )
            else:
                response = self.model.generate_content(
                    prompt,
                    generation_config=self.generation_config
                )

            # Parse response
            result = self._parse_disease_response(response.text)
            return result

        except Exception as e:
            logger.error(f"Error in disease analysis: {str(e)}")
            return {
                "diseases": [],
                "recommendation": "Unable to analyze at this time. Please consult a aquaculture expert."
            }

    async def analyze_water_quality(
        self,
        wq_reading: Any,
        species: List[str]
    ) -> Dict[str, Any]:
        """
        Analyze water quality parameters using Gemini AI.
        """
        try:
            prompt = WATER_QUALITY_PROMPT.format(
                ph=wq_reading.ph,
                temperature=wq_reading.temperature,
                dissolved_oxygen=wq_reading.dissolved_oxygen,
                ammonia=wq_reading.ammonia or "N/A",
                nitrite=wq_reading.nitrite or "N/A",
                nitrate=wq_reading.nitrate or "N/A",
                salinity=wq_reading.salinity or "N/A",
                species=", ".join(species)
            )

            response = self.model.generate_content(
                prompt,
                generation_config=self.generation_config
            )

            result = self._parse_water_quality_response(response.text, wq_reading)
            return result

        except Exception as e:
            logger.error(f"Error in water quality analysis: {str(e)}")
            return {
                "status": "unknown",
                "issues": ["Unable to analyze water quality"],
                "recommendations": ["Please check parameters manually"],
                "parameters": {}
            }

    async def get_tank_recommendations(
        self,
        tank_info: Dict,
        water_quality: Optional[Any] = None,
        disease_info: Optional[Any] = None
    ) -> Dict[str, Any]:
        """
        Get comprehensive tank recommendations.
        """
        try:
            prompt = TANK_RECOMMENDATION_PROMPT.format(
                tank_info=json.dumps(tank_info),
                water_quality=json.dumps(water_quality.dict() if water_quality else {}),
                disease_info=json.dumps(disease_info.dict() if disease_info else {})
            )

            response = self.model.generate_content(
                prompt,
                generation_config=self.generation_config
            )

            return {
                "recommendations": self._extract_recommendations(response.text)
            }

        except Exception as e:
            logger.error(f"Error getting tank recommendations: {str(e)}")
            return {"recommendations": ["Unable to generate recommendations at this time."]}

    async def process_voice_query(
        self,
        query: str,
        history: List[Dict],
        context: Optional[Dict] = None
    ) -> Dict[str, Any]:
        """
        Process voice agent query with conversation history.
        """
        try:
            # Build conversation context
            conversation = "\n".join([
                f"{msg['role']}: {msg['content']}"
                for msg in history[-5:]  # Last 5 messages for context
            ])

            prompt = VOICE_AGENT_PROMPT.format(
                conversation=conversation,
                query=query,
                context=json.dumps(context) if context else "{}"
            )

            response = self.model.generate_content(
                prompt,
                generation_config=self.generation_config
            )

            return self._parse_voice_response(response.text)

        except Exception as e:
            logger.error(f"Error processing voice query: {str(e)}")
            return {
                "text": "I'm sorry, I couldn't process that. Could you please try again?",
                "action": None
            }

    async def get_recommendation(
        self,
        query: str,
        context: Optional[Dict] = None
    ) -> Dict[str, Any]:
        """
        Get general AI recommendation for aquaculture question.
        """
        try:
            prompt = GENERAL_RECOMMENDATION_PROMPT.format(
                query=query,
                context=json.dumps(context) if context else "{}"
            )

            response = self.model.generate_content(
                prompt,
                generation_config=self.generation_config
            )

            return {
                "answer": response.text,
                "sources": None,
                "confidence": 0.8
            }

        except Exception as e:
            logger.error(f"Error getting recommendation: {str(e)}")
            return {
                "answer": "Unable to provide recommendation at this time.",
                "sources": None,
                "confidence": 0.0
            }

    def _parse_disease_response(self, text: str) -> Dict[str, Any]:
        """Parse disease analysis response from Gemini."""
        # Basic parsing - can be enhanced with structured output
        return {
            "diseases": [],
            "recommendation": text
        }

    def _parse_water_quality_response(self, text: str, wq_reading: Any) -> Dict[str, Any]:
        """Parse water quality analysis response."""
        # Determine status based on response content
        status = "good"
        if "critical" in text.lower() or "severe" in text.lower():
            status = "critical"
        elif "poor" in text.lower() or "bad" in text.lower():
            status = "poor"
        elif "fair" in text.lower() or "moderate" in text.lower():
            status = "fair"
        elif "excellent" in text.lower():
            status = "excellent"

        # Extract issues and recommendations
        lines = text.split('\n')
        issues = [line.strip('- ') for line in lines if 'issue' in line.lower() or 'problem' in line.lower()]
        recommendations = [line.strip('- ') for line in lines if 'recommend' in line.lower() or 'should' in line.lower()]

        return {
            "status": status,
            "issues": issues if issues else [],
            "recommendations": recommendations if recommendations else [text],
            "parameters": {
                "ph": {"value": wq_reading.ph, "status": "good"},
                "temperature": {"value": wq_reading.temperature, "status": "good"},
                "dissolved_oxygen": {"value": wq_reading.dissolved_oxygen, "status": "good"}
            }
        }

    def _extract_recommendations(self, text: str) -> List[str]:
        """Extract recommendations from text."""
        lines = text.split('\n')
        recommendations = [
            line.strip('- ').strip()
            for line in lines
            if line.strip() and len(line.strip()) > 10
        ]
        return recommendations if recommendations else [text]

    def _parse_voice_response(self, text: str) -> Dict[str, Any]:
        """Parse voice agent response to detect actions."""
        # Check for common action patterns
        action = None
        action_params = {}

        text_lower = text.lower()

        if "show tank" in text_lower or "display tank" in text_lower:
            action = "navigate"
            action_params = {"destination": "tank_details"}
        elif "search product" in text_lower or "find product" in text_lower:
            action = "search_products"
        elif "list tanks" in text_lower or "show all tanks" in text_lower:
            action = "get_tanks"

        return {
            "text": text,
            "action": action,
            "action_params": action_params
        }
