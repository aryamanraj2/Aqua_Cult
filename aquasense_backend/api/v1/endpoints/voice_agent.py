"""
Voice Agent Endpoints - WebSocket for voice interaction
"""
from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Depends
from sqlalchemy.orm import Session
from typing import Dict
import json
import logging

from config.database import get_db
from websocket.handler import VoiceAgentHandler

router = APIRouter()
logger = logging.getLogger(__name__)

# Store active connections
active_connections: Dict[str, WebSocket] = {}


@router.websocket("/ws/{session_id}")
async def voice_agent_websocket(
    websocket: WebSocket,
    session_id: str,
    db: Session = Depends(get_db)
):
    """
    WebSocket endpoint for voice agent communication.
    """
    await websocket.accept()
    active_connections[session_id] = websocket

    handler = VoiceAgentHandler(db, session_id)

    try:
        # Send welcome message
        await websocket.send_json({
            "type": "connected",
            "content": "Voice agent connected. How can I help you today?",
            "session_id": session_id
        })

        while True:
            # Receive message from client
            data = await websocket.receive_text()
            message = json.loads(data)

            # Process message through handler
            response = await handler.handle_message(message)

            # Send response back to client
            await websocket.send_json(response)

    except WebSocketDisconnect:
        logger.info(f"WebSocket disconnected for session {session_id}")
        if session_id in active_connections:
            del active_connections[session_id]

    except Exception as e:
        logger.error(f"Error in WebSocket connection: {str(e)}")
        await websocket.send_json({
            "type": "error",
            "content": "An error occurred. Please try again.",
            "error": str(e)
        })
        if session_id in active_connections:
            del active_connections[session_id]


@router.get("/sessions/{session_id}/status")
async def get_session_status(session_id: str):
    """
    Check if a voice agent session is active.
    """
    return {
        "session_id": session_id,
        "active": session_id in active_connections
    }
