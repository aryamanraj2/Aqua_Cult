"""
WebSocket Message Types - Message structure and parsing
"""
from enum import Enum
from typing import Dict, Any, Tuple, Optional
from datetime import datetime


class MessageType(str, Enum):
    """WebSocket message types"""
    TEXT = "text"
    AUDIO = "audio"
    ACTION = "action"
    ERROR = "error"
    PING = "ping"
    PONG = "pong"
    CONNECTED = "connected"
    DISCONNECTED = "disconnected"


def parse_message(message: Dict[str, Any]) -> Tuple[MessageType, str, Optional[Dict]]:
    """
    Parse incoming WebSocket message.

    Args:
        message: Raw message dictionary

    Returns:
        Tuple of (message_type, content, metadata)

    Raises:
        ValueError: If message format is invalid
    """
    if not isinstance(message, dict):
        raise ValueError("Message must be a dictionary")

    # Extract message type
    msg_type_str = message.get("type")
    if not msg_type_str:
        raise ValueError("Message must have a 'type' field")

    try:
        msg_type = MessageType(msg_type_str)
    except ValueError:
        raise ValueError(f"Invalid message type: {msg_type_str}")

    # Extract content
    content = message.get("content", "")

    # Extract metadata
    metadata = message.get("metadata")

    return msg_type, content, metadata


def create_response(
    msg_type: MessageType,
    content: str,
    action: Optional[str] = None,
    data: Optional[Dict] = None,
    error: Optional[str] = None
) -> Dict[str, Any]:
    """
    Create a WebSocket response message.

    Args:
        msg_type: Message type
        content: Response content
        action: Optional action to perform on client
        data: Optional data payload
        error: Optional error message

    Returns:
        Response dictionary
    """
    response = {
        "type": msg_type.value,
        "content": content,
        "timestamp": datetime.utcnow().isoformat()
    }

    if action:
        response["action"] = action

    if data:
        response["data"] = data

    if error:
        response["error"] = error

    return response


def create_error_response(error_message: str, details: Optional[Dict] = None) -> Dict[str, Any]:
    """
    Create an error response message.

    Args:
        error_message: Error message
        details: Optional error details

    Returns:
        Error response dictionary
    """
    response = create_response(
        MessageType.ERROR,
        error_message,
        error=error_message
    )

    if details:
        response["details"] = details

    return response


def validate_message(message: Dict[str, Any]) -> bool:
    """
    Validate message structure.

    Args:
        message: Message to validate

    Returns:
        True if valid, False otherwise
    """
    # Check required fields
    if not isinstance(message, dict):
        return False

    if "type" not in message:
        return False

    if "content" not in message:
        return False

    # Validate message type
    try:
        MessageType(message["type"])
    except ValueError:
        return False

    return True
