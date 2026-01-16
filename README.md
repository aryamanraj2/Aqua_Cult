# AquaSense - Process Flow Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Diagram](#architecture-diagram)
3. [Voice Agent Flow](#voice-agent-flow)
4. [API Request/Response Flows](#api-requestresponse-flows)
5. [Data Flow Between Android and Backend](#data-flow-between-android-and-backend)
6. [ML Inference Pipeline](#ml-inference-pipeline)
7. [WebSocket Message Sequences](#websocket-message-sequences)
8. [Database Flow](#database-flow)

---

## System Overview

AquaSense is an aquaculture management system consisting of:
- **Android App** (Kotlin/Jetpack Compose) - User interface
- **Python Backend** (FastAPI) - API server, AI, ML inference
- **SQLite Database** - Data persistence
- **Gemini API** - AI-powered analysis and conversation

```
┌─────────────────┐     HTTP/REST      ┌─────────────────┐
│                 │ ◄────────────────► │                 │
│   Android App   │                    │  Python Backend │
│   (Kotlin)      │     WebSocket      │   (FastAPI)     │
│                 │ ◄────────────────► │                 │
└─────────────────┘                    └────────┬────────┘
                                                │
                              ┌─────────────────┼─────────────────┐
                              ▼                 ▼                 ▼
                       ┌──────────┐      ┌──────────┐      ┌──────────┐
                       │  SQLite  │      │  Gemini  │      │  .keras  │
                       │    DB    │      │   API    │      │  Model   │
                       └──────────┘      └──────────┘      └──────────┘
```

---

## Architecture Diagram

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           ANDROID CLIENT                                     │
│                      (Kotlin + Jetpack Compose)                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│  │  Dashboard  │  │   Disease   │  │ Marketplace │  │   Profile   │       │
│  │   Screen    │  │  Detection  │  │   Screen    │  │   Screen    │       │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘       │
│         │                │                │                │               │
│  ┌──────┴────────────────┴────────────────┴────────────────┴──────┐       │
│  │                      ViewModel Layer                            │       │
│  └──────┬────────────────┬────────────────┬────────────────┬──────┘       │
│         │                │                │                │               │
│  ┌──────┴────────────────┴────────────────┴────────────────┴──────┐       │
│  │                     Repository Layer                            │       │
│  └──────┬────────────────┬────────────────────────────────────────┘       │
│         │                │                                                 │
│  ┌──────┴──────┐  ┌──────┴──────┐                                         │
│  │  Retrofit   │  │  WebSocket  │                                         │
│  │   Client    │  │   Client    │                                         │
│  └──────┬──────┘  └──────┬──────┘                                         │
│         │                │                                                 │
└─────────┼────────────────┼─────────────────────────────────────────────────┘
          │ REST API       │ WS Connection
          ▼                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PYTHON BACKEND                                     │
│                         (FastAPI + Uvicorn)                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         API Layer                                    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐  │   │
│  │  │ REST Router  │  │  WebSocket   │  │   Middleware (CORS)      │  │   │
│  │  │ /api/v1/*    │  │  /ws/*       │  │                          │  │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────────────────────────┘  │   │
│  └─────────┼─────────────────┼──────────────────────────────────────────┘   │
│            │                 │                                              │
│  ┌─────────┴─────────────────┴──────────────────────────────────────────┐   │
│  │                       Service Layer                                   │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐     │   │
│  │  │   Tank     │  │  Product   │  │  Analysis  │  │   Voice    │     │   │
│  │  │  Service   │  │  Service   │  │  Service   │  │  Service   │     │   │
│  │  └──────┬─────┘  └──────┬─────┘  └──────┬─────┘  └──────┬─────┘     │   │
│  └─────────┼───────────────┼───────────────┼───────────────┼────────────┘   │
│            │               │               │               │                │
│  ┌─────────┴───────────────┴───────────────┴───────────────┴────────────┐   │
│  │                       Data Layer                                      │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐     │   │
│  │  │  SQLite    │  │   Gemini   │  │   Keras    │  │   gTTS     │     │   │
│  │  │  Database  │  │    API     │  │   Model    │  │   Audio    │     │   │
│  │  └────────────┘  └────────────┘  └────────────┘  └────────────┘     │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Voice Agent Flow

### Complete Voice Conversation Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              USER                                            │
│                         (Speaks into phone)                                  │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     ANDROID: SpeechRecognizer                                │
│                     (On-device, FREE)                                        │
│                                                                             │
│  1. User presses mic button                                                 │
│  2. SpeechRecognizer.startListening()                                       │
│  3. Audio captured and transcribed on-device                                │
│  4. Returns: "How is my salmon tank doing?"                                 │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │ Transcribed text
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     ANDROID: WebSocket Client                                │
│                                                                             │
│  Send JSON message:                                                         │
│  {                                                                          │
│    "type": "user_message",                                                  │
│    "session_id": "abc123",                                                  │
│    "text": "How is my salmon tank doing?",                                  │
│    "timestamp": "2025-12-21T10:30:00Z"                                      │
│  }                                                                          │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │ WebSocket
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     BACKEND: WebSocket Handler                               │
│                     /ws/voice-agent/{session_id}                             │
│                                                                             │
│  1. Receive message                                                         │
│  2. Parse JSON                                                              │
│  3. Get/create session from memory                                          │
│  4. Pass to Voice Service                                                   │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     BACKEND: Voice Service                                   │
│                                                                             │
│  1. Load session history (last 10 messages)                                 │
│  2. Detect intent from user message                                         │
│  3. Fetch relevant data:                                                    │
│     - get_tank_data(tank_id) → Tank info                                   │
│     - get_sensor_readings(tank_id) → Water quality                         │
│  4. Build context for Gemini                                                │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     BACKEND: Gemini API Call                                 │
│                                                                             │
│  Prompt:                                                                    │
│  """                                                                        │
│  You are AquaSense AI, an expert aquaculture assistant...                   │
│                                                                             │
│  Current tank data:                                                         │
│  - Tank: Salmon Tank 1                                                      │
│  - Species: Atlantic Salmon                                                 │
│  - pH: 7.2, Temp: 24.5°C, DO: 8.5mg/L, Ammonia: 0.3ppm                    │
│                                                                             │
│  Conversation history:                                                      │
│  [previous messages...]                                                     │
│                                                                             │
│  User: How is my salmon tank doing?                                         │
│  """                                                                        │
│                                                                             │
│  Response: "Your salmon tank is doing well overall..."                      │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     BACKEND: Text-to-Speech                                  │
│                     (gTTS - Google Text-to-Speech, FREE)                     │
│                                                                             │
│  1. Take response text                                                      │
│  2. Convert to MP3 audio bytes                                              │
│  3. Encode as base64 for transmission                                       │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     BACKEND: WebSocket Response                              │
│                                                                             │
│  Send JSON messages:                                                        │
│                                                                             │
│  1. Text response (for display):                                            │
│  {                                                                          │
│    "type": "text_response",                                                 │
│    "text": "Your salmon tank is doing well...",                             │
│    "timestamp": "2025-12-21T10:30:01Z"                                      │
│  }                                                                          │
│                                                                             │
│  2. Audio response (for playback):                                          │
│  {                                                                          │
│    "type": "audio_response",                                                │
│    "audio_base64": "//uQxAAAAAANIAAAAAE...",                               │
│    "format": "mp3"                                                          │
│  }                                                                          │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │ WebSocket
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     ANDROID: Response Handler                                │
│                                                                             │
│  1. Receive text_response → Display in chat UI                              │
│  2. Receive audio_response → Decode base64                                  │
│  3. Play audio with ExoPlayer                                               │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              USER                                            │
│                      (Hears AI response)                                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Session Memory Flow

```
Session Storage (In-Memory Dict)
────────────────────────────────

sessions = {
    "session_abc123": {
        "history": [
            {"role": "user", "content": "How is my salmon tank?"},
            {"role": "assistant", "content": "Your tank is doing well..."},
            {"role": "user", "content": "What about ammonia levels?"},
            {"role": "assistant", "content": "Ammonia is at 0.3ppm..."}
        ],
        "current_tank_id": "tank_001",
        "context": {
            "last_topic": "water_quality",
            "mentioned_products": []
        }
    }
}

Memory Management:
- Keep last 10 conversation turns
- Store current tank context
- Track mentioned products for recommendations
- Session expires after 30 minutes of inactivity
```

---

## API Request/Response Flows

### Tank CRUD Operations

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         GET /api/v1/tanks                                 │
│                         List all tanks                                    │
├──────────────────────────────────────────────────────────────────────────┤
│  Request:                                                                │
│  GET http://localhost:8000/api/v1/tanks                                  │
│  Headers: Content-Type: application/json                                 │
│                                                                          │
│  Response (200 OK):                                                      │
│  {                                                                       │
│    "tanks": [                                                            │
│      {                                                                   │
│        "id": "tank_001",                                                 │
│        "name": "Salmon Tank 1",                                          │
│        "species": ["Atlantic Salmon"],                                   │
│        "volume_m3": 150.0,                                               │
│        "current_stage": "grow_out",                                      │
│        "health_status": "healthy",                                       │
│        "last_reading": {                                                 │
│          "ph": 7.2,                                                      │
│          "temperature": 24.5,                                            │
│          "dissolved_oxygen": 8.5                                         │
│        }                                                                 │
│      }                                                                   │
│    ],                                                                    │
│    "total": 1                                                            │
│  }                                                                       │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                         POST /api/v1/tanks                                │
│                         Create new tank                                   │
├──────────────────────────────────────────────────────────────────────────┤
│  Request:                                                                │
│  POST http://localhost:8000/api/v1/tanks                                 │
│  Body:                                                                   │
│  {                                                                       │
│    "name": "Tilapia Tank 2",                                             │
│    "species": ["Nile Tilapia"],                                          │
│    "length_m": 10.0,                                                     │
│    "width_m": 5.0,                                                       │
│    "depth_m": 2.0,                                                       │
│    "current_stage": "nursery"                                            │
│  }                                                                       │
│                                                                          │
│  Response (201 Created):                                                 │
│  {                                                                       │
│    "id": "tank_002",                                                     │
│    "name": "Tilapia Tank 2",                                             │
│    "volume_m3": 100.0,                                                   │
│    "created_at": "2025-12-21T10:30:00Z"                                  │
│  }                                                                       │
└──────────────────────────────────────────────────────────────────────────┘
```

### AI Analysis Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    POST /api/v1/analysis/tank/{id}                        │
│                    Trigger AI tank analysis                               │
├──────────────────────────────────────────────────────────────────────────┤
│  Request:                                                                │
│  POST http://localhost:8000/api/v1/analysis/tank/tank_001                │
│                                                                          │
│  Backend Process:                                                        │
│  1. Fetch tank data from database                                        │
│  2. Fetch latest water quality readings                                  │
│  3. Build prompt with tank context                                       │
│  4. Call Gemini API                                                      │
│  5. Parse JSON response                                                  │
│  6. Store analysis in database                                           │
│  7. Return to client                                                     │
│                                                                          │
│  Response (200 OK):                                                      │
│  {                                                                       │
│    "analysis_id": "analysis_001",                                        │
│    "tank_id": "tank_001",                                                │
│    "health_score": 85,                                                   │
│    "status": "healthy",                                                  │
│    "summary": "Tank is in good condition with minor concerns...",        │
│    "disease_risks": [                                                    │
│      {                                                                   │
│        "disease": "Ammonia Stress",                                      │
│        "risk_level": "low",                                              │
│        "probability": 0.15                                               │
│      }                                                                   │
│    ],                                                                    │
│    "recommendations": [                                                  │
│      {                                                                   │
│        "priority": "medium",                                             │
│        "action": "Perform 20% water change",                             │
│        "reason": "Slightly elevated ammonia levels"                      │
│      }                                                                   │
│    ],                                                                    │
│    "analyzed_at": "2025-12-21T10:30:00Z"                                 │
│  }                                                                       │
└──────────────────────────────────────────────────────────────────────────┘
```

### Disease Detection Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    POST /api/v1/analysis/disease                          │
│                    Upload image for disease detection                     │
├──────────────────────────────────────────────────────────────────────────┤
│  Request:                                                                │
│  POST http://localhost:8000/api/v1/analysis/disease                      │
│  Content-Type: multipart/form-data                                       │
│  Body:                                                                   │
│    - image: fish_photo.jpg (file upload)                                 │
│    - tank_id: "tank_001" (optional)                                      │
│                                                                          │
│  Backend Process:                                                        │
│  1. Receive uploaded image                                               │
│  2. Preprocess: resize to 224x224, normalize                             │
│  3. Load .keras model                                                    │
│  4. Run inference                                                        │
│  5. Get disease classification + confidence                              │
│  6. Query treatment recommendations                                      │
│  7. Return results                                                       │
│                                                                          │
│  Response (200 OK):                                                      │
│  {                                                                       │
│    "diagnosis_id": "diag_001",                                           │
│    "disease": "Bacterial Infection",                                     │
│    "confidence": 0.87,                                                   │
│    "all_predictions": [                                                  │
│      {"class": "Bacterial", "confidence": 0.87},                         │
│      {"class": "Healthy", "confidence": 0.08},                           │
│      {"class": "Fungal", "confidence": 0.03}                             │
│    ],                                                                    │
│    "treatment": {                                                        │
│      "immediate_actions": [                                              │
│        "Isolate affected fish",                                          │
│        "Test water quality"                                              │
│      ],                                                                   │
│      "medications": [                                                    │
│        {                                                                 │
│          "name": "Oxytetracycline",                                      │
│          "dosage": "50-75 mg/kg body weight",                            │
│          "duration": "10 days"                                           │
│        }                                                                 │
│      ],                                                                   │
│      "recommended_products": [                                           │
│        {"id": "prod_001", "name": "Antibacterial Treatment"}             │
│      ]                                                                   │
│    },                                                                    │
│    "analyzed_at": "2025-12-21T10:30:00Z"                                 │
│  }                                                                       │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Between Android and Backend

### App Startup Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           APP STARTUP                                        │
└─────────────────────────────────────────────────────────────────────────────┘

    ANDROID                                              BACKEND
    ───────                                              ───────

    App Launch
        │
        ▼
    Splash Screen (2s)
        │
        ├────────────────────────────────────────────►  Health Check
        │                GET /api/v1/health              │
        │◄────────────────────────────────────────────   {"status": "ok"}
        │
        ▼
    Check Local Cache
        │
        ├─── Cache Valid? ───► Use cached data
        │         │
        │         No
        │         ▼
        ├────────────────────────────────────────────►  Fetch Tanks
        │                GET /api/v1/tanks               │
        │◄────────────────────────────────────────────   [tanks...]
        │
        ├────────────────────────────────────────────►  Fetch Products
        │              GET /api/v1/products              │
        │◄────────────────────────────────────────────   [products...]
        │
        ▼
    Store in Room DB
        │
        ▼
    Navigate to Dashboard
```

### Real-time Sync Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     PERIODIC DATA SYNC                                       │
└─────────────────────────────────────────────────────────────────────────────┘

Every 5 minutes (when app is active):

    ANDROID                                              BACKEND
    ───────                                              ───────

    WorkManager Trigger
        │
        ├────────────────────────────────────────────►  Get Latest Readings
        │        GET /api/v1/tanks/{id}/water-quality   │
        │◄────────────────────────────────────────────   [readings...]
        │
        ▼
    Compare with cached
        │
        ├─── Changed? ───► Update Room DB
        │         │                │
        │         │                ▼
        │         │        Emit StateFlow update
        │         │                │
        │         │                ▼
        │         │        UI recomposes
        │
        └─── No change ───► Do nothing
```

---

## ML Inference Pipeline

### Disease Detection Pipeline

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     IMAGE CAPTURE (Android)                                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  CameraX captures image                                                      │
│  - Resolution: 1080x1080 (square crop)                                      │
│  - Format: JPEG                                                             │
│  - Quality: 85%                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     IMAGE UPLOAD (HTTP)                                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     BACKEND: Image Preprocessing                             │
│                                                                             │
│  def preprocess_image(image_bytes):                                         │
│      # 1. Decode image                                                      │
│      img = tf.image.decode_jpeg(image_bytes, channels=3)                    │
│                                                                             │
│      # 2. Resize to model input size                                        │
│      img = tf.image.resize(img, [224, 224])                                 │
│                                                                             │
│      # 3. Normalize pixel values (0-1)                                      │
│      img = img / 255.0                                                      │
│                                                                             │
│      # 4. Add batch dimension                                               │
│      img = tf.expand_dims(img, 0)  # Shape: (1, 224, 224, 3)               │
│                                                                             │
│      return img                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     BACKEND: Model Inference                                 │
│                                                                             │
│  # Load model (done once at startup)                                        │
│  model = tf.keras.models.load_model('models/fish_disease.keras')            │
│                                                                             │
│  # Run inference                                                            │
│  predictions = model.predict(preprocessed_image)                            │
│                                                                             │
│  # Output shape: (1, num_classes)                                           │
│  # Example: [[0.02, 0.87, 0.03, 0.05, 0.02, 0.01, 0.00]]                    │
│                                                                             │
│  CLASS_NAMES = [                                                            │
│      'Healthy',                                                             │
│      'Bacterial',                                                           │
│      'Fungal',                                                              │
│      'Parasitic',                                                           │
│      'Viral',                                                               │
│      'Nutritional',                                                         │
│      'Environmental'                                                        │
│  ]                                                                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     BACKEND: Post-processing                                 │
│                                                                             │
│  def get_diagnosis(predictions):                                            │
│      # Get top prediction                                                   │
│      predicted_idx = predictions.argmax()                                   │
│      confidence = float(predictions[0][predicted_idx])                      │
│      disease = CLASS_NAMES[predicted_idx]                                   │
│                                                                             │
│      # Get all predictions sorted by confidence                             │
│      all_preds = [                                                          │
│          {"class": CLASS_NAMES[i], "confidence": float(predictions[0][i])}  │
│          for i in range(len(CLASS_NAMES))                                   │
│      ]                                                                      │
│      all_preds.sort(key=lambda x: x["confidence"], reverse=True)            │
│                                                                             │
│      return {                                                               │
│          "disease": disease,                                                │
│          "confidence": confidence,                                          │
│          "all_predictions": all_preds                                       │
│      }                                                                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     BACKEND: Treatment Lookup                                │
│                                                                             │
│  def get_treatment(disease: str):                                           │
│      # Load treatment data from knowledge base                              │
│      treatments = load_json("knowledge/treatments.json")                    │
│      return treatments.get(disease, default_treatment)                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     RESPONSE TO ANDROID                                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## WebSocket Message Sequences

### Connection Establishment

```
    ANDROID                                              BACKEND
    ───────                                              ───────

    Connect to WS
        │
        ├────────────────────────────────────────────►
        │     WebSocket UPGRADE                         Accept
        │     ws://10.0.2.2:8000/ws/voice-agent/abc     │
        │◄────────────────────────────────────────────   │
        │     101 Switching Protocols                    │
        │                                                │
        ├────────────────────────────────────────────►   │
        │     {"type": "init", "session_id": "abc"}     Create Session
        │◄────────────────────────────────────────────   │
        │     {"type": "ready", "session_id": "abc"}    │
        │                                                │
    Connected!                                      Session Active
```

### Message Exchange Sequence

```
    ANDROID                                              BACKEND
    ───────                                              ───────

    User speaks
        │
        ▼
    SpeechRecognizer
        │
        ▼
    "Check tank 1 temperature"
        │
        ├────────────────────────────────────────────►
        │     {                                         Receive
        │       "type": "user_message",                 │
        │       "text": "Check tank 1 temperature",     Parse
        │       "session_id": "abc"                     │
        │     }                                         Fetch Data
        │                                               │
        │                                               Call Gemini
        │                                               │
        │◄────────────────────────────────────────────  Generate TTS
        │     {                                         │
        │       "type": "text_response",                │
        │       "text": "Tank 1 temperature is..."     │
        │     }                                         │
        │                                               │
    Display text                                        │
        │                                               │
        │◄────────────────────────────────────────────   │
        │     {                                         │
        │       "type": "audio_response",               │
        │       "audio_base64": "//uQxAAA...",         │
        │       "format": "mp3"                         │
        │     }                                         │
        │                                               │
    Play audio                                          │
```

### Error Handling

```
    ANDROID                                              BACKEND
    ───────                                              ───────

        ├────────────────────────────────────────────►
        │     {"type": "user_message", "text": "..."}   │
        │                                               Error occurs
        │◄────────────────────────────────────────────   │
        │     {                                         │
        │       "type": "error",                        │
        │       "code": "GEMINI_ERROR",                 │
        │       "message": "AI service unavailable"    │
        │     }                                         │
        │                                               │
    Show error toast                                    │
    Offer retry                                         │
```

### Connection Keep-Alive

```
    ANDROID                                              BACKEND
    ───────                                              ───────

    Every 30 seconds:
        │
        ├────────────────────────────────────────────►
        │     {"type": "ping"}                          │
        │◄────────────────────────────────────────────   │
        │     {"type": "pong"}                          │
        │                                               │

    If no pong in 10s:
        │
    Reconnect                                           │
```

---

## Database Flow

### Entity Relationships

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          DATABASE SCHEMA                                     │
└─────────────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │    users     │
    ├──────────────┤
    │ id (PK)      │
    │ name         │
    │ phone        │
    │ address      │
    │ created_at   │
    └───────┬──────┘
            │
            │ 1:N
            ▼
    ┌──────────────┐         ┌────────────────────┐
    │    tanks     │         │ water_quality_     │
    ├──────────────┤         │    readings        │
    │ id (PK)      │ 1:N     ├────────────────────┤
    │ user_id (FK) │────────►│ id (PK)            │
    │ name         │         │ tank_id (FK)       │
    │ species[]    │         │ ph                 │
    │ volume_m3    │         │ temperature        │
    │ current_stage│         │ dissolved_oxygen   │
    │ created_at   │         │ ammonia            │
    └───────┬──────┘         │ recorded_at        │
            │                └────────────────────┘
            │ 1:N
            ▼
    ┌──────────────┐
    │tank_analyses │
    ├──────────────┤
    │ id (PK)      │
    │ tank_id (FK) │
    │ health_score │
    │ status       │
    │ summary      │
    │ analyzed_at  │
    └──────────────┘


    ┌──────────────┐         ┌──────────────┐
    │   orders     │         │ order_items  │
    ├──────────────┤         ├──────────────┤
    │ id (PK)      │ 1:N     │ id (PK)      │
    │ user_id (FK) │────────►│ order_id(FK) │
    │ total        │         │ product_id   │
    │ status       │         │ quantity     │
    │ created_at   │         │ price        │
    └──────────────┘         └──────┬───────┘
                                    │
                                    │ N:1
                                    ▼
                             ┌──────────────┐
                             │  products    │
                             ├──────────────┤
                             │ id (PK)      │
                             │ name         │
                             │ category     │
                             │ price_inr    │
                             │ in_stock     │
                             └──────────────┘
```

### Data Access Patterns

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     COMMON QUERIES                                           │
└─────────────────────────────────────────────────────────────────────────────┘

1. Dashboard Load:
   SELECT t.*,
          (SELECT * FROM water_quality_readings
           WHERE tank_id = t.id
           ORDER BY recorded_at DESC LIMIT 1) as latest_reading
   FROM tanks t
   WHERE t.user_id = :user_id

2. Tank Analysis History:
   SELECT * FROM tank_analyses
   WHERE tank_id = :tank_id
   ORDER BY analyzed_at DESC
   LIMIT 10

3. Water Quality Trend (7 days):
   SELECT * FROM water_quality_readings
   WHERE tank_id = :tank_id
   AND recorded_at > datetime('now', '-7 days')
   ORDER BY recorded_at

4. Product Search:
   SELECT * FROM products
   WHERE category = :category
   AND in_stock = true
   ORDER BY rating DESC
```

---

## Error Handling Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     ERROR HANDLING STRATEGY                                  │
└─────────────────────────────────────────────────────────────────────────────┘

    ANDROID                                              BACKEND
    ───────                                              ───────

    API Call
        │
        ├────────────────────────────────────────────►
        │                                               │
        │                                          Error occurs
        │                                               │
        │◄────────────────────────────────────────────   │
        │     HTTP 4xx/5xx                              │
        │     {                                         │
        │       "error": {                              │
        │         "code": "TANK_NOT_FOUND",             │
        │         "message": "Tank does not exist",    │
        │         "details": {...}                      │
        │       }                                       │
        │     }                                         │
        │                                               │
    RepositoryResult.Error
        │
        ▼
    ViewModel handles
        │
        ├─── Retryable? ───► Show retry button
        │         │
        │    Not retryable
        │         ▼
        └─── Show error message in UI


Error Codes:
- VALIDATION_ERROR (400) → Show field errors
- NOT_FOUND (404) → Navigate back
- RATE_LIMITED (429) → Wait and retry
- SERVER_ERROR (500) → Show generic error
- NETWORK_ERROR → Check connection
```

---

## Security Flow (Local Development)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     SIMPLIFIED AUTH (Local Only)                             │
└─────────────────────────────────────────────────────────────────────────────┘

Since this is local development only:

1. Single hardcoded user in database
2. No login required
3. All requests assume user_id = "user_001"
4. API key for Gemini stored in environment variable

Backend startup:
    │
    ├── Load GEMINI_API_KEY from .env
    │
    ├── Initialize database
    │
    ├── Seed default user if not exists:
    │   INSERT INTO users (id, name)
    │   VALUES ('user_001', 'Demo User')
    │
    └── Ready to accept connections

Android:
    │
    └── All API calls include hardcoded user context
        (No auth headers needed for local dev)
```

---

## Summary

This document covers:
- Complete system architecture
- Voice agent conversation flow
- All API endpoints and their request/response formats
- Data synchronization between Android and Backend
- ML inference pipeline for disease detection
- WebSocket message protocols
- Database schema and access patterns
- Error handling strategies

For implementation details, see:
- `claude.md` - Project context and conventions
- `FEATURES.md` - Feature specifications
- Backend code in `aquasense_backend/`
- Android code in `app/src/main/java/com/parth/aquasense/`
