# Voice Agent WebSocket - Testing Commands

Quick reference for testing the Voice Agent WebSocket backend.

---

## Prerequisites

### 1. Activate Virtual Environment
```bash
cd /Users/parthvats/AndroidStudioProjects/AquaSense/aquasense_backend
source venv/bin/activate
```

### 2. Install Testing Dependencies (if needed)
```bash
pip install websockets httpx
```

### 3. Verify Environment
```bash
python verify_implementation.py
```

---

## Starting the Server

### Option 1: Standard Mode
```bash
cd /Users/parthvats/AndroidStudioProjects/AquaSense/aquasense_backend
source venv/bin/activate
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Option 2: Debug Mode
```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000 --log-level debug
```

### Option 3: Using Python
```bash
python main.py
```

---

## Automated Testing

### Run All Tests
```bash
cd /Users/parthvats/AndroidStudioProjects/AquaSense/aquasense_backend
source venv/bin/activate
python test_websocket.py
```

This runs:
- Connection test
- Message exchange test
- Ping/Pong test
- Error handling test
- Session status test

---

## Manual Testing with curl

### 1. Health Check
```bash
curl http://localhost:8000/health
```

Expected response:
```json
{
  "status": "healthy",
  "service": "AquaSense Backend"
}
```

### 2. Check Session Status
```bash
# Replace with your session ID
SESSION_ID="550e8400-e29b-41d4-a716-446655440000"
curl http://localhost:8000/api/v1/voice/sessions/$SESSION_ID/status
```

Expected response:
```json
{
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "active": false
}
```

---

## Manual Testing with websocat

### Install websocat
```bash
# macOS
brew install websocat

# Or download from: https://github.com/vi/websocat/releases
```

### Connect to WebSocket
```bash
# Generate a session ID (or use any UUID)
SESSION_ID="550e8400-e29b-41d4-a716-446655440000"

# Connect
websocat ws://localhost:8000/api/v1/voice/ws/$SESSION_ID
```

### Send Test Messages

After connecting, you'll receive a welcome message. Then send messages one per line:

```json
{"type": "text", "content": "Hello, how can you help me?", "session_id": "550e8400-e29b-41d4-a716-446655440000"}
```

```json
{"type": "text", "content": "How many tanks do I have?", "session_id": "550e8400-e29b-41d4-a716-446655440000"}
```

```json
{"type": "text", "content": "Show me my tanks", "session_id": "550e8400-e29b-41d4-a716-446655440000"}
```

```json
{"type": "ping", "content": "ping", "session_id": "550e8400-e29b-41d4-a716-446655440000"}
```

To exit: Press Ctrl+C

---

## Manual Testing with Python

### Quick Interactive Test
```python
import asyncio
import websockets
import json
import uuid

async def test():
    session_id = str(uuid.uuid4())
    url = f"ws://localhost:8000/api/v1/voice/ws/{session_id}"

    async with websockets.connect(url) as ws:
        # Receive welcome
        welcome = await ws.recv()
        print("Welcome:", json.loads(welcome))

        # Send message
        msg = {
            "type": "text",
            "content": "How many tanks do I have?",
            "session_id": session_id
        }
        await ws.send(json.dumps(msg))

        # Receive response
        response = await ws.recv()
        print("Response:", json.loads(response))

asyncio.run(test())
```

Save as `quick_test.py` and run:
```bash
python quick_test.py
```

---

## Android Emulator Testing

### 1. Start Backend
```bash
cd /Users/parthvats/AndroidStudioProjects/AquaSense/aquasense_backend
source venv/bin/activate
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### 2. Get Your Local IP
```bash
# macOS/Linux
ifconfig | grep "inet " | grep -v 127.0.0.1
```

### 3. Test from Android Emulator

In Android code, use:
```kotlin
// For emulator
val wsUrl = "ws://10.0.2.2:8000/api/v1/voice/ws/$sessionId"
```

### 4. Test from Physical Device

In Android code, use your local IP:
```kotlin
// For physical device - replace with your IP from step 2
val wsUrl = "ws://192.168.1.100:8000/api/v1/voice/ws/$sessionId"
```

---

## Troubleshooting Commands

### Check if Server is Running
```bash
curl http://localhost:8000/
```

### Check Server Logs
Server logs appear in the terminal where you ran `uvicorn`. Look for:
```
INFO:     WebSocket connection accepted
INFO:     Voice agent handler initialized for session xxx
INFO:     WebSocket disconnected for session xxx
```

### Check Port is Open
```bash
lsof -i :8000
```

### Kill Process on Port 8000 (if needed)
```bash
lsof -ti:8000 | xargs kill -9
```

### Verify Dependencies
```bash
cd /Users/parthvats/AndroidStudioProjects/AquaSense/aquasense_backend
source venv/bin/activate
pip list | grep -E "fastapi|websockets|google-generativeai"
```

### Check Python Version
```bash
python --version
# Should be 3.8 or higher
```

### Verify GEMINI_API_KEY
```bash
cd /Users/parthvats/AndroidStudioProjects/AquaSense/aquasense_backend
cat .env | grep GEMINI_API_KEY
```

---

## Test Message Examples

### Basic Conversation
```json
{"type": "text", "content": "Hello!", "session_id": "xxx"}
{"type": "text", "content": "What can you help me with?", "session_id": "xxx"}
```

### Tank Queries
```json
{"type": "text", "content": "How many tanks do I have?", "session_id": "xxx"}
{"type": "text", "content": "Show me tank 1", "session_id": "xxx"}
{"type": "text", "content": "List all my tanks", "session_id": "xxx"}
```

### Water Quality
```json
{"type": "text", "content": "What's the water quality like?", "session_id": "xxx"}
{"type": "text", "content": "Is my water quality good?", "session_id": "xxx"}
```

### Product Search
```json
{"type": "text", "content": "What fish feed do you recommend?", "session_id": "xxx"}
{"type": "text", "content": "Find products for water treatment", "session_id": "xxx"}
```

### Navigation
```json
{"type": "text", "content": "Show me my tanks", "session_id": "xxx"}
{"type": "text", "content": "Go to marketplace", "session_id": "xxx"}
{"type": "text", "content": "Take me to the dashboard", "session_id": "xxx"}
```

### Ping/Pong
```json
{"type": "ping", "content": "ping", "session_id": "xxx"}
```

### Invalid Message (should return error)
```json
{"type": "invalid_type", "content": "test", "session_id": "xxx"}
```

---

## Expected Response Formats

### Welcome Message
```json
{
  "type": "connected",
  "content": "Voice agent connected. How can I help you today?",
  "session_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Text Response
```json
{
  "type": "text",
  "content": "You have 3 tanks. Tank 1 has Tilapia...",
  "action": null,
  "data": null,
  "timestamp": "2025-12-26T10:30:00.123456"
}
```

### Action Response
```json
{
  "type": "text",
  "content": "Here are your tanks.",
  "action": "navigate",
  "data": {
    "destination": "tanks"
  },
  "timestamp": "2025-12-26T10:30:00.123456"
}
```

### Pong Response
```json
{
  "type": "pong",
  "content": "pong",
  "timestamp": "2025-12-26T10:30:00.123456"
}
```

### Error Response
```json
{
  "type": "error",
  "content": "An error occurred. Please try again.",
  "error": "Invalid message type: xxx",
  "timestamp": "2025-12-26T10:30:00.123456"
}
```

---

## Performance Testing

### Measure Response Time
```python
import asyncio
import websockets
import json
import uuid
import time

async def measure_response_time():
    session_id = str(uuid.uuid4())
    url = f"ws://localhost:8000/api/v1/voice/ws/{session_id}"

    async with websockets.connect(url) as ws:
        # Skip welcome
        await ws.recv()

        # Send message and measure
        start = time.time()
        msg = {
            "type": "text",
            "content": "How many tanks do I have?",
            "session_id": session_id
        }
        await ws.send(json.dumps(msg))

        # Receive response
        response = await ws.recv()
        end = time.time()

        print(f"Response time: {(end - start) * 1000:.2f}ms")
        print(f"Response: {json.loads(response)['content'][:50]}...")

asyncio.run(measure_response_time())
```

---

## Continuous Monitoring

### Monitor Active Connections
```bash
# In one terminal, start server
uvicorn main:app --reload --host 0.0.0.0 --port 8000 --log-level debug

# In another terminal, watch connections
watch -n 1 'curl -s http://localhost:8000/api/v1/voice/sessions/xxx/status'
```

### Monitor Server Resources
```bash
# CPU and memory usage
top -pid $(lsof -ti:8000)
```

---

## Cleanup Commands

### Stop Server
```
Press Ctrl+C in the terminal running uvicorn
```

### Deactivate Virtual Environment
```bash
deactivate
```

### Clear Session Data
Sessions are stored in memory, so they're cleared when the server restarts.

To force restart:
```bash
# Stop server (Ctrl+C)
# Start again
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

---

## Quick Reference

### One-Liner: Start Server
```bash
cd /Users/parthvats/AndroidStudioProjects/AquaSense/aquasense_backend && source venv/bin/activate && uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### One-Liner: Run Tests
```bash
cd /Users/parthvats/AndroidStudioProjects/AquaSense/aquasense_backend && source venv/bin/activate && python test_websocket.py
```

### One-Liner: Verify Implementation
```bash
cd /Users/parthvats/AndroidStudioProjects/AquaSense/aquasense_backend && python verify_implementation.py
```

### One-Liner: Health Check
```bash
curl http://localhost:8000/health && echo
```

---

## Integration Test Checklist

Before starting Android implementation:

- [ ] Run `python verify_implementation.py` - All checks pass
- [ ] Start server - No errors
- [ ] Run `python test_websocket.py` - All tests pass
- [ ] Test with websocat - Can send/receive messages
- [ ] Check health endpoint - Returns "healthy"
- [ ] Verify .env has GEMINI_API_KEY - Set correctly
- [ ] Review documentation - Understand WebSocket contract

---

## Need Help?

1. **Server won't start**: Check if port 8000 is already in use
2. **Connection refused**: Verify server is running and URL is correct
3. **No AI responses**: Check GEMINI_API_KEY in .env file
4. **Tests fail**: Make sure server is running first
5. **Session errors**: Try with a fresh UUID session ID

For detailed troubleshooting, see: `VOICE_AGENT_IMPLEMENTATION.md`

---

**Everything ready for testing!** 🧪
