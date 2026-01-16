"""
Session Memory - Manages conversation history for voice agent sessions
"""
from typing import Dict, List
from datetime import datetime, timedelta
from collections import defaultdict


class SessionMemory:
    """
    Manages conversation history for voice agent sessions.
    Simple in-memory storage for local development.
    In production, use Redis or database.
    """

    # Class-level storage for all sessions
    _sessions: Dict[str, List[Dict]] = defaultdict(list)
    _session_timestamps: Dict[str, datetime] = {}

    def __init__(self, session_id: str, max_history: int = 50):
        """
        Initialize session memory.

        Args:
            session_id: Unique session identifier
            max_history: Maximum number of messages to keep
        """
        self.session_id = session_id
        self.max_history = max_history

        # Initialize session if not exists
        if session_id not in self._sessions:
            self._sessions[session_id] = []
            self._session_timestamps[session_id] = datetime.utcnow()

    def add_message(self, role: str, content: str, metadata: Dict = None):
        """
        Add a message to the session history.

        Args:
            role: Message role (user, assistant, system)
            content: Message content
            metadata: Optional metadata dictionary
        """
        message = {
            "role": role,
            "content": content,
            "timestamp": datetime.utcnow().isoformat(),
            "metadata": metadata or {}
        }

        self._sessions[self.session_id].append(message)

        # Trim history if exceeds max
        if len(self._sessions[self.session_id]) > self.max_history:
            self._sessions[self.session_id] = self._sessions[self.session_id][-self.max_history:]

        # Update session timestamp
        self._session_timestamps[self.session_id] = datetime.utcnow()

    def get_history(self, limit: int = None) -> List[Dict]:
        """
        Get conversation history for the session.

        Args:
            limit: Optional limit on number of messages to return

        Returns:
            List of message dictionaries
        """
        history = self._sessions.get(self.session_id, [])

        if limit:
            return history[-limit:]

        return history

    def get_recent_messages(self, count: int = 5) -> List[Dict]:
        """
        Get the most recent messages.

        Args:
            count: Number of recent messages to return

        Returns:
            List of recent message dictionaries
        """
        return self.get_history(limit=count)

    def clear(self):
        """
        Clear the session history.
        """
        if self.session_id in self._sessions:
            self._sessions[self.session_id] = []

    def session_exists(self) -> bool:
        """
        Check if session exists.

        Returns:
            True if session exists, False otherwise
        """
        return self.session_id in self._sessions

    def is_expired(self, timeout_minutes: int = 60) -> bool:
        """
        Check if session has expired.

        Args:
            timeout_minutes: Session timeout in minutes

        Returns:
            True if session is expired, False otherwise
        """
        if self.session_id not in self._session_timestamps:
            return True

        last_activity = self._session_timestamps[self.session_id]
        expiry_time = last_activity + timedelta(minutes=timeout_minutes)

        return datetime.utcnow() > expiry_time

    @classmethod
    def cleanup_expired_sessions(cls, timeout_minutes: int = 60):
        """
        Clean up expired sessions.

        Args:
            timeout_minutes: Session timeout in minutes
        """
        current_time = datetime.utcnow()
        expired_sessions = []

        for session_id, last_activity in cls._session_timestamps.items():
            expiry_time = last_activity + timedelta(minutes=timeout_minutes)
            if current_time > expiry_time:
                expired_sessions.append(session_id)

        # Remove expired sessions
        for session_id in expired_sessions:
            if session_id in cls._sessions:
                del cls._sessions[session_id]
            if session_id in cls._session_timestamps:
                del cls._session_timestamps[session_id]

        return len(expired_sessions)

    @classmethod
    def get_active_session_count(cls) -> int:
        """
        Get count of active sessions.

        Returns:
            Number of active sessions
        """
        return len(cls._sessions)
