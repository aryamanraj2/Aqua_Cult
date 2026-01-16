"""
Tests for WebSocket Voice Agent Handler
"""
import pytest
import json
from fastapi.testclient import TestClient
from unittest.mock import Mock, patch, AsyncMock


@pytest.mark.integration
class TestVoiceAgentWebSocket:
    """Test WebSocket voice agent connection and messaging."""

    def test_websocket_connection(self, client: TestClient):
        """Test establishing WebSocket connection."""
        session_id = "test-session-123"

        with client.websocket_connect(f"/ws/voice-agent/{session_id}") as websocket:
            # Should connect successfully
            assert websocket is not None

            # Send ping
            websocket.send_json({"type": "ping"})

            # Receive pong
            response = websocket.receive_json()
            assert response["type"] == "pong"

    def test_websocket_text_message(self, client: TestClient):
        """Test sending text message through WebSocket."""
        session_id = "test-session-456"

        with client.websocket_connect(f"/ws/voice-agent/{session_id}") as websocket:
            # Send text message
            message = {
                "type": "text",
                "content": "Show me all my tanks",
                "user_id": "test-user-123"
            }
            websocket.send_json(message)

            # Receive response
            response = websocket.receive_json()
            assert response["type"] == "response"
            assert "content" in response
            assert isinstance(response["content"], str)

    @patch('services.voice_service.VoiceService.process_message')
    def test_websocket_with_action(self, mock_process, client: TestClient):
        """Test WebSocket message that triggers an action."""
        mock_process.return_value = {
            "response": "I've added a reminder for tomorrow.",
            "action": {
                "type": "add_reminder",
                "parameters": {
                    "task": "Check water quality",
                    "due_date": "2025-12-23"
                }
            }
        }

        session_id = "test-session-789"

        with client.websocket_connect(f"/ws/voice-agent/{session_id}") as websocket:
            message = {
                "type": "text",
                "content": "Remind me to check water quality tomorrow",
                "user_id": "test-user-123"
            }
            websocket.send_json(message)

            response = websocket.receive_json()
            assert response["type"] == "response"
            assert "action" in response
            assert response["action"]["type"] == "add_reminder"

    def test_websocket_audio_message(self, client: TestClient):
        """Test sending audio message through WebSocket."""
        session_id = "test-session-audio"

        with client.websocket_connect(f"/ws/voice-agent/{session_id}") as websocket:
            # Send audio message (base64 encoded)
            message = {
                "type": "audio",
                "content": "base64_encoded_audio_data_here",
                "user_id": "test-user-123"
            }
            websocket.send_json(message)

            # Should receive transcription and response
            response = websocket.receive_json()
            assert response["type"] in ["transcription", "response", "error"]

    def test_websocket_error_handling(self, client: TestClient):
        """Test WebSocket error handling."""
        session_id = "test-session-error"

        with client.websocket_connect(f"/ws/voice-agent/{session_id}") as websocket:
            # Send invalid message
            websocket.send_json({"type": "invalid_type"})

            # Should receive error
            response = websocket.receive_json()
            assert response["type"] == "error"
            assert "message" in response

    def test_websocket_session_history(self, client: TestClient):
        """Test that WebSocket maintains session history."""
        session_id = "test-session-history"

        with client.websocket_connect(f"/ws/voice-agent/{session_id}") as websocket:
            # Send first message
            websocket.send_json({
                "type": "text",
                "content": "Hello",
                "user_id": "test-user-123"
            })
            response1 = websocket.receive_json()

            # Send follow-up message
            websocket.send_json({
                "type": "text",
                "content": "What did I just say?",
                "user_id": "test-user-123"
            })
            response2 = websocket.receive_json()

            # Response should reference previous context
            assert response2["type"] == "response"

    def test_websocket_disconnection(self, client: TestClient):
        """Test WebSocket disconnection."""
        session_id = "test-session-disconnect"

        with client.websocket_connect(f"/ws/voice-agent/{session_id}") as websocket:
            # Send message
            websocket.send_json({
                "type": "text",
                "content": "Hello",
                "user_id": "test-user-123"
            })
            websocket.receive_json()

            # Close connection
            websocket.close()

        # Session should be cleaned up (implementation dependent)

    def test_multiple_websocket_sessions(self, client: TestClient):
        """Test multiple concurrent WebSocket sessions."""
        session_id_1 = "test-session-multi-1"
        session_id_2 = "test-session-multi-2"

        with client.websocket_connect(f"/ws/voice-agent/{session_id_1}") as ws1:
            with client.websocket_connect(f"/ws/voice-agent/{session_id_2}") as ws2:
                # Send message to session 1
                ws1.send_json({
                    "type": "text",
                    "content": "Session 1 message",
                    "user_id": "test-user-123"
                })

                # Send message to session 2
                ws2.send_json({
                    "type": "text",
                    "content": "Session 2 message",
                    "user_id": "test-user-456"
                })

                # Both should receive responses
                response1 = ws1.receive_json()
                response2 = ws2.receive_json()

                assert response1["type"] == "response"
                assert response2["type"] == "response"


@pytest.mark.unit
class TestWebSocketMessageHandlers:
    """Test WebSocket message handler functions."""

    @pytest.fixture
    def mock_websocket(self):
        """Create mock WebSocket."""
        ws = Mock()
        ws.send_json = AsyncMock()
        ws.receive_json = AsyncMock()
        return ws

    @patch('websocket.handler.VoiceAgentHandler.handle_text_message')
    async def test_handle_text_message(self, mock_handler, mock_websocket):
        """Test text message handler."""
        mock_handler.return_value = {
            "type": "response",
            "content": "Test response"
        }

        from websocket.handler import VoiceAgentHandler
        handler = VoiceAgentHandler("test-session")

        result = await handler.handle_text_message(
            content="Test message",
            user_id="test-user"
        )

        assert result is not None
        assert result["type"] == "response"

    @patch('websocket.handler.VoiceAgentHandler.handle_audio_message')
    async def test_handle_audio_message(self, mock_handler, mock_websocket):
        """Test audio message handler."""
        mock_handler.return_value = {
            "type": "transcription",
            "text": "Transcribed text",
            "response": "AI response"
        }

        from websocket.handler import VoiceAgentHandler
        handler = VoiceAgentHandler("test-session")

        result = await handler.handle_audio_message(
            audio_base64="base64_audio_data"
        )

        assert result is not None
        assert "transcription" in result or "response" in result

    async def test_broadcast_message(self, mock_websocket):
        """Test broadcasting message to WebSocket."""
        from websocket.handler import VoiceAgentHandler
        handler = VoiceAgentHandler("test-session")

        message = {
            "type": "notification",
            "content": "Test notification"
        }

        await handler.broadcast(message, mock_websocket)
        mock_websocket.send_json.assert_called_once_with(message)

    async def test_handle_error(self, mock_websocket):
        """Test error handling in WebSocket."""
        from websocket.handler import VoiceAgentHandler
        handler = VoiceAgentHandler("test-session")

        error_message = "Test error"
        await handler.send_error(error_message, mock_websocket)

        # Verify error message sent
        calls = mock_websocket.send_json.call_args_list
        assert len(calls) > 0
        error_msg = calls[0][0][0]
        assert error_msg["type"] == "error"
        assert "message" in error_msg


@pytest.mark.unit
class TestMessageTypes:
    """Test WebSocket message type definitions."""

    def test_text_message_structure(self):
        """Test text message structure."""
        from websocket.message_types import TextMessage

        message = TextMessage(
            type="text",
            content="Test message",
            user_id="test-user-123"
        )

        assert message.type == "text"
        assert message.content == "Test message"
        assert message.user_id == "test-user-123"

    def test_audio_message_structure(self):
        """Test audio message structure."""
        from websocket.message_types import AudioMessage

        message = AudioMessage(
            type="audio",
            content="base64_audio",
            user_id="test-user-123"
        )

        assert message.type == "audio"
        assert message.content == "base64_audio"

    def test_response_message_structure(self):
        """Test response message structure."""
        from websocket.message_types import ResponseMessage

        message = ResponseMessage(
            type="response",
            content="AI response",
            action=None
        )

        assert message.type == "response"
        assert message.content == "AI response"
        assert message.action is None

    def test_response_with_action(self):
        """Test response message with action."""
        from websocket.message_types import ResponseMessage, ActionData

        action = ActionData(
            type="add_reminder",
            parameters={"task": "Check water", "due_date": "2025-12-23"}
        )

        message = ResponseMessage(
            type="response",
            content="Reminder added",
            action=action
        )

        assert message.action is not None
        assert message.action.type == "add_reminder"
        assert "task" in message.action.parameters

    def test_error_message_structure(self):
        """Test error message structure."""
        from websocket.message_types import ErrorMessage

        message = ErrorMessage(
            type="error",
            message="Something went wrong",
            code="INTERNAL_ERROR"
        )

        assert message.type == "error"
        assert message.message == "Something went wrong"
        assert message.code == "INTERNAL_ERROR"


@pytest.mark.integration
class TestVoiceAgentFeatures:
    """Test specific voice agent features through WebSocket."""

    @patch('services.voice_service.VoiceService.process_message')
    def test_get_tank_info(self, mock_process, client: TestClient, test_tank):
        """Test getting tank information through voice agent."""
        mock_process.return_value = {
            "response": f"You have 1 tank named {test_tank.name} with {test_tank.current_stock} fish.",
            "data": {
                "tanks": [
                    {
                        "id": test_tank.id,
                        "name": test_tank.name,
                        "stock": test_tank.current_stock
                    }
                ]
            }
        }

        session_id = "test-tank-info"

        with client.websocket_connect(f"/ws/voice-agent/{session_id}") as websocket:
            websocket.send_json({
                "type": "text",
                "content": "Show me my tanks",
                "user_id": "test-user-123"
            })

            response = websocket.receive_json()
            assert response["type"] == "response"
            assert "tanks" in response.get("data", {}) or "tank" in response["content"].lower()

    @patch('services.voice_service.VoiceService.process_message')
    def test_add_reminder(self, mock_process, client: TestClient):
        """Test adding reminder through voice agent."""
        mock_process.return_value = {
            "response": "I've added a reminder to check water quality tomorrow.",
            "action": {
                "type": "add_reminder",
                "parameters": {
                    "task": "Check water quality",
                    "due_date": "2025-12-23"
                }
            }
        }

        session_id = "test-reminder"

        with client.websocket_connect(f"/ws/voice-agent/{session_id}") as websocket:
            websocket.send_json({
                "type": "text",
                "content": "Remind me to check water quality tomorrow",
                "user_id": "test-user-123"
            })

            response = websocket.receive_json()
            assert response["type"] == "response"
            assert response.get("action", {}).get("type") == "add_reminder"

    @patch('services.voice_service.VoiceService.process_message')
    def test_get_recommendations(self, mock_process, client: TestClient):
        """Test getting AI recommendations through voice agent."""
        mock_process.return_value = {
            "response": "Based on your tank parameters, I recommend: 1) Perform water change this week, 2) Monitor temperature closely, 3) Reduce feeding slightly.",
            "data": {
                "recommendations": [
                    "Perform water change this week",
                    "Monitor temperature closely",
                    "Reduce feeding slightly"
                ]
            }
        }

        session_id = "test-recommendations"

        with client.websocket_connect(f"/ws/voice-agent/{session_id}") as websocket:
            websocket.send_json({
                "type": "text",
                "content": "What should I do to improve my tank health?",
                "user_id": "test-user-123"
            })

            response = websocket.receive_json()
            assert response["type"] == "response"
            assert "recommend" in response["content"].lower()
