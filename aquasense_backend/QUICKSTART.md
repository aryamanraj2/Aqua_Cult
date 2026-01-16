# AquaSense Backend - Quick Start Guide

## 🚀 Get Running in 5 Minutes

### Prerequisites
- Python 3.10 or higher
- pip installed
- Gemini API key ([Get one here](https://makersuite.google.com/app/apikey))

### Option 1: Automated Setup (Recommended)

**Mac/Linux:**
```bash
./run.sh
```

**Windows:**
```bash
run.bat
```

The script will:
1. Create virtual environment
2. Install dependencies
3. Create .env from template
4. Start the server

### Option 2: Manual Setup

```bash
# 1. Create virtual environment
python3 -m venv venv

# 2. Activate it
source venv/bin/activate  # Mac/Linux
# OR
venv\Scripts\activate     # Windows

# 3. Install dependencies
pip install -r requirements.txt

# 4. Set up environment
cp .env.example .env
# Edit .env and add your GEMINI_API_KEY

# 5. Run the server
uvicorn main:app --reload
```

### Verify Installation

Once running, open: http://localhost:8000/docs

You should see the interactive API documentation.

## 📝 Environment Setup

Edit `.env` file:

```bash
# REQUIRED: Get from https://makersuite.google.com/app/apikey
GEMINI_API_KEY=your_actual_key_here

# Optional (defaults are fine for local dev)
DATABASE_URL=sqlite:///./aquasense.db
HOST=0.0.0.0
PORT=8000
DEBUG=True
```

## 🧪 Test the API

### Using the Interactive Docs (Swagger UI)

1. Go to http://localhost:8000/docs
2. Expand any endpoint
3. Click "Try it out"
4. Fill in parameters
5. Click "Execute"

### Using curl

**Health Check:**
```bash
curl http://localhost:8000/health
```

**Create a Tank:**
```bash
curl -X POST http://localhost:8000/api/v1/tanks \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Main Tank",
    "species": ["Tilapia"],
    "capacity": 1000,
    "current_stock": 50,
    "location": "Farm A"
  }'
```

**Get All Tanks:**
```bash
curl http://localhost:8000/api/v1/tanks
```

**Add Water Quality Reading:**
```bash
curl -X POST http://localhost:8000/api/v1/tanks/{tank_id}/water-quality \
  -H "Content-Type: application/json" \
  -d '{
    "ph": 7.2,
    "temperature": 28.5,
    "dissolved_oxygen": 6.5,
    "ammonia": 0.02
  }'
```

## 🔌 Connect Android App

Update Android app's `NetworkModule.kt`:

**For Emulator:**
```kotlin
private const val BASE_URL = "http://10.0.2.2:8000/api/v1/"
```

**For Physical Device:**
```kotlin
private const val BASE_URL = "http://10.50.40.170:8000/api/v1/"
// Replace 10.50.40.170 with your computer's local IP
```

**Find your local IP:**
- Mac: `ifconfig | grep "inet "`
- Windows: `ipconfig`
- Linux: `ip addr show`

## 📦 Project Structure (Key Files)

```
aquasense_backend/
├── main.py              ← Start here (entry point)
├── requirements.txt     ← Dependencies
├── .env.example        ← Copy to .env
├── api/v1/endpoints/   ← API endpoints
├── services/           ← Business logic
├── models/             ← Database models
├── schemas/            ← Request/response schemas
└── knowledge/          ← Disease/treatment data
```

## 🛠️ Common Commands

**Install dependencies:**
```bash
pip install -r requirements.txt
```

**Run server:**
```bash
uvicorn main:app --reload
```

**Run on different port:**
```bash
uvicorn main:app --reload --port 8080
```

**Run tests:**
```bash
pytest
```

**Format code:**
```bash
black .
```

## 🔍 Troubleshooting

### Port 8000 already in use
```bash
# Use different port
uvicorn main:app --reload --port 8080
```

### Import errors
```bash
# Make sure virtual environment is activated
source venv/bin/activate  # Mac/Linux
venv\Scripts\activate     # Windows

# Reinstall dependencies
pip install -r requirements.txt
```

### Gemini API errors
- Check API key in `.env` is correct
- Verify internet connection
- Check rate limits (free tier: 15 req/min)

### Database errors
```bash
# Delete and recreate database
rm aquasense.db
# Restart server - it will recreate tables
```

## 📚 Next Steps

1. **Read the docs:**
   - `README.md` - Full documentation
   - `STRUCTURE.md` - Architecture overview
   - `FEATURES.md` - Feature specifications (in parent directory)

2. **Explore the API:**
   - Swagger UI: http://localhost:8000/docs
   - ReDoc: http://localhost:8000/redoc

3. **Test endpoints:**
   - Create tanks
   - Add water quality readings
   - Try disease detection (when you have images)
   - Test the voice agent WebSocket

4. **Connect Android app:**
   - Update BASE_URL in NetworkModule.kt
   - Run Android app
   - Test integration

## 🎯 Key Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check |
| `/api/v1/tanks` | GET | List tanks |
| `/api/v1/tanks` | POST | Create tank |
| `/api/v1/tanks/{id}` | GET | Get tank details |
| `/api/v1/tanks/{id}/water-quality` | POST | Add water quality |
| `/api/v1/products` | GET | List products |
| `/api/v1/analysis/tank-analysis` | POST | Analyze tank |
| `/api/v1/voice/ws/{session_id}` | WS | Voice agent |

## 💡 Tips

- **Auto-reload:** Server restarts on code changes when using `--reload`
- **API Docs:** Always available at `/docs` while server is running
- **Database:** SQLite file created in project directory
- **Logs:** Check console for errors and info
- **Testing:** Use Swagger UI for quick API testing

## 🆘 Get Help

- Check `README.md` for detailed documentation
- Review `STRUCTURE.md` for architecture
- See main project `FEATURES.md` for specifications
- Check logs in terminal for error messages

## ✅ Verification Checklist

- [ ] Python 3.10+ installed
- [ ] Virtual environment created and activated
- [ ] Dependencies installed successfully
- [ ] `.env` file created with valid GEMINI_API_KEY
- [ ] Server starts without errors
- [ ] Can access http://localhost:8000/docs
- [ ] Health check endpoint returns success
- [ ] Can create a tank via API

If all checks pass, you're ready to go! 🎉
