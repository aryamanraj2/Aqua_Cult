"""
Voice Agent Schemas - WebSocket communication
"""
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
from datetime import datetime


class VoiceMessageRequest(BaseModel):
    """Schema for voice agent message from client"""
    type: str = Field(..., description="Message type: text, audio, action")
    content: str = Field(..., description="Text content or base64 audio")
    session_id: str
    metadata: Optional[Dict[str, Any]] = None


class VoiceMessageResponse(BaseModel):
    """Schema for voice agent message to client"""
    type: str = Field(..., description="Message type: text, audio, action, error")
    content: str = Field(..., description="Response text or base64 audio")
    action: Optional[str] = None  # navigate, show_tank, etc.
    data: Optional[Dict[str, Any]] = None
    timestamp: datetime


class VoiceSessionCreate(BaseModel):
    """Schema for creating voice session"""
    user_id: str
    context: Optional[Dict[str, Any]] = None


class VoiceSessionResponse(BaseModel):
    """Schema for voice session response"""
    session_id: str
    user_id: str
    created_at: datetime
    expires_at: datetime
