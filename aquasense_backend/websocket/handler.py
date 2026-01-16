"""
WebSocket Handler - Manages voice agent WebSocket connections
"""
from sqlalchemy.orm import Session
from typing import Dict, Any
import logging

from services.voice_service import VoiceService
from websocket.message_types import MessageType, parse_message, create_response

logger = logging.getLogger(__name__)


class VoiceAgentHandler:
    """
    Handles WebSocket messages for the voice agent.
    """

    def __init__(self, db: Session, session_id: str):
        """
        Initialize the voice agent handler.

        Args:
            db: Database session
            session_id: Unique session identifier
        """
        self.db = db
        self.session_id = session_id
        self.voice_service = VoiceService(db, session_id)
        logger.info(f"Voice agent handler initialized for session {session_id}")

    async def handle_message(self, message: Dict[str, Any]) -> Dict[str, Any]:
        """
        Handle incoming WebSocket message.

        Args:
            message: Message dictionary from client

        Returns:
            Response dictionary to send back to client
        """
        try:
            # Parse message
            msg_type, content, metadata = parse_message(message)

            # Route based on message type
            if msg_type == MessageType.TEXT:
                return await self._handle_text_message(content, metadata)

            elif msg_type == MessageType.AUDIO:
                return await self._handle_audio_message(content)

            elif msg_type == MessageType.ACTION:
                return await self._handle_action(content, metadata)

            elif msg_type == MessageType.PING:
                return create_response(MessageType.PONG, "pong")

            else:
                return create_response(
                    MessageType.ERROR,
                    f"Unknown message type: {msg_type}"
                )

        except Exception as e:
            logger.error(f"Error handling message: {str(e)}")
            return create_response(
                MessageType.ERROR,
                f"Error processing message: {str(e)}"
            )

    async def _handle_text_message(
        self,
        text: str,
        metadata: Dict = None
    ) -> Dict[str, Any]:
        """
        Handle text message from user.

        Args:
            text: Text content
            metadata: Optional metadata

        Returns:
            Response dictionary
        """
        try:
            # Process text through voice service
            result = await self.voice_service.process_text_input(text, metadata)

            # Create response
            response = create_response(
                MessageType.TEXT,
                result["content"],
                action=result.get("action"),
                data=result.get("data")
            )

            return response

        except Exception as e:
            logger.error(f"Error handling text message: {str(e)}")
            return create_response(
                MessageType.ERROR,
                "Sorry, I couldn't process that. Please try again."
            )

    async def _handle_audio_message(self, audio_base64: str) -> Dict[str, Any]:
        """
        Handle audio message from user.

        Args:
            audio_base64: Base64 encoded audio

        Returns:
            Response dictionary
        """
        try:
            # Process audio through voice service
            result = await self.voice_service.process_audio_input(audio_base64)

            return create_response(
                MessageType.TEXT,
                result["content"]
            )

        except Exception as e:
            logger.error(f"Error handling audio message: {str(e)}")
            return create_response(
                MessageType.ERROR,
                "Audio processing is handled on the client side with Android SpeechRecognizer."
            )

    async def _handle_action(
        self,
        action: str,
        metadata: Dict = None
    ) -> Dict[str, Any]:
        """
        Handle action request from client.

        Args:
            action: Action to perform
            metadata: Optional metadata

        Returns:
            Response dictionary
        """
        try:
            # Actions like "clear_session", "get_history", etc.
            if action == "clear_session":
                self.voice_service.clear_session()
                return create_response(
                    MessageType.ACTION,
                    "Session cleared successfully",
                    action="session_cleared"
                )

            elif action == "get_history":
                history = self.voice_service.session_memory.get_history()
                return create_response(
                    MessageType.ACTION,
                    "History retrieved",
                    data={"history": history}
                )

            else:
                return create_response(
                    MessageType.ERROR,
                    f"Unknown action: {action}"
                )

        except Exception as e:
            logger.error(f"Error handling action: {str(e)}")
            return create_response(
                MessageType.ERROR,
                f"Error performing action: {str(e)}"
            )

    def cleanup(self):
        """
        Clean up handler resources.
        """
        logger.info(f"Cleaning up voice agent handler for session {self.session_id}")
        # Perform any necessary cleanup
