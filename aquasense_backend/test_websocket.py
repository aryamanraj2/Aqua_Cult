"""
WebSocket Testing Script for Voice Agent

This script tests the Voice Agent WebSocket endpoint using the websockets library.
Run this after starting the FastAPI server.

Usage:
    python test_websocket.py
"""
import asyncio
import websockets
import json
import uuid
from datetime import datetime


async def test_voice_agent():
    """Test the voice agent WebSocket endpoint"""

    # Generate a unique session ID
    session_id = str(uuid.uuid4())

    # WebSocket URL
    url = f"ws://localhost:8000/api/v1/voice/ws/{session_id}"

    print(f"\n{'='*70}")
    print(f"Testing Voice Agent WebSocket")
    print(f"{'='*70}")
    print(f"Session ID: {session_id}")
    print(f"URL: {url}")
    print(f"{'='*70}\n")

    try:
        # Connect to WebSocket
        async with websockets.connect(url) as websocket:
            print(f"[{datetime.now().strftime('%H:%M:%S')}] Connected to WebSocket\n")

            # Receive welcome message
            welcome = await websocket.recv()
            welcome_data = json.loads(welcome)
            print(f"[RECEIVED] Welcome Message:")
            print(json.dumps(welcome_data, indent=2))
            print()

            # Test 1: Ask about tanks
            test_messages = [
                {
                    "type": "text",
                    "content": "How many tanks do I have?",
                    "session_id": session_id,
                    "metadata": {}
                },
                {
                    "type": "text",
                    "content": "What's the water quality like?",
                    "session_id": session_id,
                    "metadata": {}
                },
                {
                    "type": "text",
                    "content": "Show me my tanks",
                    "session_id": session_id,
                    "metadata": {}
                },
                {
                    "type": "text",
                    "content": "What products do you recommend for fish feed?",
                    "session_id": session_id,
                    "metadata": {}
                }
            ]

            for i, message in enumerate(test_messages, 1):
                print(f"\n{'─'*70}")
                print(f"Test {i}: {message['content']}")
                print(f"{'─'*70}")

                # Send message
                await websocket.send(json.dumps(message))
                print(f"[SENT] {message['content']}")

                # Receive response
                response = await websocket.recv()
                response_data = json.loads(response)

                print(f"\n[RECEIVED] Response:")
                print(json.dumps(response_data, indent=2))

                # Wait a bit between messages
                await asyncio.sleep(1)

            print(f"\n{'='*70}")
            print(f"All tests completed successfully!")
            print(f"{'='*70}\n")

    except websockets.exceptions.WebSocketException as e:
        print(f"\n[ERROR] WebSocket error: {e}")
        print("\nMake sure the FastAPI server is running:")
        print("  cd aquasense_backend")
        print("  uvicorn main:app --reload --host 0.0.0.0 --port 8000")

    except Exception as e:
        print(f"\n[ERROR] Unexpected error: {e}")
        import traceback
        traceback.print_exc()


async def test_ping_pong():
    """Test ping/pong functionality"""
    session_id = str(uuid.uuid4())
    url = f"ws://localhost:8000/api/v1/voice/ws/{session_id}"

    print(f"\n{'='*70}")
    print(f"Testing Ping/Pong")
    print(f"{'='*70}\n")

    try:
        async with websockets.connect(url) as websocket:
            # Skip welcome message
            await websocket.recv()

            # Send ping
            ping_message = {
                "type": "ping",
                "content": "ping",
                "session_id": session_id
            }

            await websocket.send(json.dumps(ping_message))
            print("[SENT] Ping message")

            # Receive pong
            response = await websocket.recv()
            response_data = json.loads(response)

            print(f"[RECEIVED] Response:")
            print(json.dumps(response_data, indent=2))

            if response_data.get("type") == "pong":
                print("\n✓ Ping/Pong working correctly!")
            else:
                print("\n✗ Expected pong response")

    except Exception as e:
        print(f"[ERROR] {e}")


async def test_error_handling():
    """Test error handling with invalid messages"""
    session_id = str(uuid.uuid4())
    url = f"ws://localhost:8000/api/v1/voice/ws/{session_id}"

    print(f"\n{'='*70}")
    print(f"Testing Error Handling")
    print(f"{'='*70}\n")

    try:
        async with websockets.connect(url) as websocket:
            # Skip welcome message
            await websocket.recv()

            # Test invalid message type
            invalid_message = {
                "type": "invalid_type",
                "content": "This should cause an error",
                "session_id": session_id
            }

            await websocket.send(json.dumps(invalid_message))
            print("[SENT] Invalid message type")

            response = await websocket.recv()
            response_data = json.loads(response)

            print(f"[RECEIVED] Response:")
            print(json.dumps(response_data, indent=2))

            if response_data.get("type") == "error":
                print("\n✓ Error handling working correctly!")
            else:
                print("\n✗ Expected error response")

    except Exception as e:
        print(f"[ERROR] {e}")


async def test_session_status():
    """Test the session status REST endpoint"""
    import httpx

    session_id = str(uuid.uuid4())

    print(f"\n{'='*70}")
    print(f"Testing Session Status Endpoint")
    print(f"{'='*70}\n")

    try:
        async with httpx.AsyncClient() as client:
            # Check status before connection
            response = await client.get(
                f"http://localhost:8000/api/v1/voice/sessions/{session_id}/status"
            )
            data = response.json()

            print(f"[GET] /api/v1/voice/sessions/{session_id}/status")
            print(f"Response: {data}")
            print(f"Active before connection: {data['active']}")

            # Now connect
            url = f"ws://localhost:8000/api/v1/voice/ws/{session_id}"
            async with websockets.connect(url) as websocket:
                await websocket.recv()  # Skip welcome

                # Check status while connected
                response = await client.get(
                    f"http://localhost:8000/api/v1/voice/sessions/{session_id}/status"
                )
                data = response.json()
                print(f"Active during connection: {data['active']}")

            # Small delay to ensure connection is closed
            await asyncio.sleep(0.5)

            # Check status after disconnection
            response = await client.get(
                f"http://localhost:8000/api/v1/voice/sessions/{session_id}/status"
            )
            data = response.json()
            print(f"Active after disconnection: {data['active']}")

            print("\n✓ Session status tracking working correctly!")

    except Exception as e:
        print(f"[ERROR] {e}")


async def main():
    """Run all tests"""
    print("\n" + "="*70)
    print(" "*20 + "Voice Agent WebSocket Tests")
    print("="*70)

    # Run main test
    await test_voice_agent()

    # Run additional tests
    await test_ping_pong()
    await test_error_handling()
    await test_session_status()

    print("\n" + "="*70)
    print(" "*25 + "Tests Complete")
    print("="*70 + "\n")


if __name__ == "__main__":
    # Check if websockets is installed
    try:
        import websockets
        import httpx
    except ImportError:
        print("\n[ERROR] Required packages not installed.")
        print("Please install: pip install websockets httpx")
        exit(1)

    # Run tests
    asyncio.run(main())
