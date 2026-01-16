# AquaSense Backend Structure

Complete overview of the backend implementation.

## 📁 Directory Structure

```
aquasense_backend/
├── 📄 main.py                          # FastAPI application entry point
├── 📄 requirements.txt                 # Python dependencies
├── 📄 README.md                        # Documentation
├── 📄 STRUCTURE.md                     # This file
├── 📄 .env.example                     # Environment variables template
├── 📄 .gitignore                       # Git ignore rules
├── 📄 run.sh                          # Quick start script (Unix/Mac)
├── 📄 run.bat                         # Quick start script (Windows)
│
├── 📁 config/                          # Configuration
│   ├── __init__.py
│   ├── settings.py                     # Pydantic settings (env vars)
│   └── database.py                     # SQLAlchemy database setup
│
├── 📁 models/                          # SQLAlchemy ORM Models
│   ├── __init__.py
│   ├── user.py                         # User model
│   ├── tank.py                         # Tank model
│   ├── water_quality.py                # Water quality readings model
│   ├── product.py                      # Product model (marketplace)
│   └── order.py                        # Order model (marketplace)
│
├── 📁 schemas/                         # Pydantic Schemas (Request/Response)
│   ├── __init__.py
│   ├── tank.py                         # Tank & water quality schemas
│   ├── analysis.py                     # Disease detection & analysis schemas
│   ├── voice.py                        # Voice agent message schemas
│   └── product.py                      # Product & order schemas
│
├── 📁 api/v1/                         # API Version 1
│   ├── __init__.py
│   ├── router.py                       # Main API router (aggregates all endpoints)
│   └── endpoints/                      # Endpoint modules
│       ├── __init__.py
│       ├── tanks.py                    # Tank CRUD & water quality
│       ├── products.py                 # Products & orders
│       ├── analysis.py                 # Disease detection & tank analysis
│       └── voice_agent.py              # WebSocket voice agent
│
├── 📁 services/                       # Business Logic Layer
│   ├── __init__.py
│   ├── tank_service.py                # Tank operations
│   ├── product_service.py             # Product & order operations
│   ├── analysis_service.py            # Analysis & AI operations
│   └── voice_service.py               # Voice agent operations
│
├── 📁 ai/                             # AI/Gemini Integration
│   ├── __init__.py
│   ├── gemini_client.py               # Gemini API client
│   ├── prompts.py                     # AI prompts for different tasks
│   └── session_memory.py              # Voice session memory management
│
├── 📁 ml/                             # Machine Learning
│   ├── __init__.py
│   ├── disease_classifier.py          # TensorFlow/Keras disease classifier
│   ├── preprocessing.py               # Image preprocessing utilities
│   └── models/                        # ML model files
│       ├── README.md                  # Model documentation
│       └── fish_disease.keras         # Disease detection model (from ML team)
│
├── 📁 knowledge/                      # Knowledge Base (JSON)
│   ├── diseases.json                  # Disease information database
│   ├── treatments.json                # Treatment protocols
│   └── species_info.json              # Fish species information
│
├── 📁 websocket/                      # WebSocket Handlers
│   ├── __init__.py
│   ├── handler.py                     # Voice agent WebSocket handler
│   └── message_types.py               # WebSocket message types & utilities
│
└── 📁 tests/                          # Test Suite
    ├── __init__.py
    └── test_basic.py                  # Basic API tests
```

## 🔧 Core Components

### 1. Application Entry (`main.py`)
- FastAPI application initialization
- CORS middleware configuration
- Database table creation on startup
- API router registration
- Health check endpoints

### 2. Configuration (`config/`)
- **settings.py**: Environment-based configuration using Pydantic
- **database.py**: SQLAlchemy engine and session management

### 3. Database Models (`models/`)
All models use:
- UUID primary keys
- Timestamps (created_at, updated_at)
- Proper foreign key relationships

**Models:**
- User: Basic user information
- Tank: Fish tank with species, capacity, stock
- WaterQuality: pH, temperature, DO, ammonia, etc.
- Product: Marketplace items (feed, medicine, equipment)
- Order: User orders with items and status

### 4. API Schemas (`schemas/`)
Pydantic models for:
- Request validation
- Response serialization
- Type safety
- Automatic API documentation

**Schema Files:**
- tank.py: Tank CRUD and water quality
- analysis.py: Disease detection, tank analysis, AI recommendations
- voice.py: Voice agent messages
- product.py: Products and orders

### 5. API Endpoints (`api/v1/endpoints/`)

#### Tanks (`tanks.py`)
- GET /tanks - List all tanks
- GET /tanks/{id} - Get tank details
- POST /tanks - Create tank
- PUT /tanks/{id} - Update tank
- DELETE /tanks/{id} - Delete tank
- POST /tanks/{id}/water-quality - Add reading
- GET /tanks/{id}/water-quality - Get readings

#### Products (`products.py`)
- GET /products - List products
- POST /products - Create product
- GET /products/{id} - Get product
- PUT /products/{id} - Update product
- DELETE /products/{id} - Delete product
- POST /products/orders - Create order
- GET /products/orders - List orders
- GET /products/orders/{id} - Get order

#### Analysis (`analysis.py`)
- POST /analysis/disease-detection - Detect from image/symptoms
- POST /analysis/disease-detection/upload - Upload image
- POST /analysis/tank-analysis - Comprehensive analysis
- GET /analysis/tank-analysis/{id} - Quick analysis
- POST /analysis/recommendation - AI Q&A

#### Voice Agent (`voice_agent.py`)
- WS /voice/ws/{session_id} - WebSocket connection
- GET /voice/sessions/{id}/status - Check session status

### 6. Business Logic (`services/`)

#### TankService
- CRUD operations for tanks
- Water quality reading management
- Hardcoded user ID (for local dev without auth)

#### ProductService
- Product catalog management
- Order creation and tracking
- Total amount calculation

#### AnalysisService
- Disease detection (ML + Gemini AI)
- Water quality analysis
- Comprehensive tank health scoring
- AI-powered recommendations

#### VoiceService
- Text input processing
- Session memory management
- Action execution (get tanks, search products, etc.)
- Integration with Gemini for conversational AI

### 7. AI Integration (`ai/`)

#### GeminiClient
Direct Gemini API integration for:
- Disease analysis
- Water quality assessment
- Tank recommendations
- Voice agent conversations
- General aquaculture Q&A

**No LangChain/LangGraph** - direct API calls for simplicity

#### Prompts
Domain-specific prompts for:
- Disease analysis
- Water quality evaluation
- Tank recommendations
- Voice agent conversations
- Species information

#### SessionMemory
In-memory conversation history:
- Per-session message storage
- Configurable history length
- Session expiration handling
- Cleanup utilities

### 8. Machine Learning (`ml/`)

#### DiseaseClassifier
- Loads TensorFlow/Keras model
- Image preprocessing
- Prediction with confidence scores
- Fallback if model not available
- Maps predictions to disease info

#### Preprocessing
- Image resizing (224x224)
- RGB conversion
- Normalization
- Batch dimension handling

### 9. Knowledge Base (`knowledge/`)

#### diseases.json
Database of fish diseases with:
- Symptoms
- Causes
- Treatment protocols
- Prevention measures
- Optimal conditions

#### treatments.json
Treatment methods:
- Salt baths
- Antibiotics
- Temperature therapy
- Quarantine protocols
- Water changes

#### species_info.json
Fish species profiles:
- Optimal water parameters
- Tank requirements
- Feeding guidelines
- Growth rates
- Common diseases

### 10. WebSocket (`websocket/`)

#### Handler
- Message routing
- Text/audio/action processing
- Error handling
- Session cleanup

#### MessageTypes
- Message type enumeration
- Message parsing utilities
- Response creation helpers
- Validation functions

## 🔄 Data Flow

### Tank Analysis Flow
```
Client Request
    ↓
Analysis Endpoint
    ↓
AnalysisService
    ├─→ TankService (get tank & water quality)
    ├─→ DiseaseClassifier (ML prediction)
    ├─→ GeminiClient (AI analysis)
    └─→ Calculate health score
    ↓
Response with analysis & recommendations
```

### Disease Detection Flow
```
Image Upload
    ↓
Analysis Endpoint
    ↓
AnalysisService
    ├─→ DiseaseClassifier (ML model)
    │       ↓
    │   Image Preprocessing
    │       ↓
    │   Model Inference
    │       ↓
    │   Disease Info Mapping
    │
    └─→ GeminiClient (AI analysis)
            ↓
        Comprehensive Analysis
    ↓
DiseaseDetectionResponse
```

### Voice Agent Flow
```
WebSocket Connection
    ↓
VoiceAgentHandler
    ↓
VoiceService
    ├─→ SessionMemory (get history)
    ├─→ GeminiClient (process query)
    └─→ Execute actions (if needed)
            ↓
        TankService / ProductService
    ↓
Response via WebSocket
```

## 🔐 Authentication

**Current**: Single hardcoded user (`default_user_001`)
**Reason**: Simplified local development
**Production**: Add JWT authentication, user registration, role-based access

## 🗄️ Database

**Development**: SQLite (file-based)
**Production**: PostgreSQL (recommended)

**Tables**:
- users
- tanks
- water_quality_readings
- products
- orders

## 🚀 Getting Started

1. Install dependencies: `pip install -r requirements.txt`
2. Copy `.env.example` to `.env`
3. Add Gemini API key to `.env`
4. Run: `uvicorn main:app --reload` or `./run.sh`

## 📊 API Documentation

Auto-generated at:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## 🧪 Testing

Run tests: `pytest`
Run with coverage: `pytest --cov=. tests/`

## 🔧 Development Workflow

1. Add Pydantic schema in `schemas/`
2. Create endpoint in `api/v1/endpoints/`
3. Implement business logic in `services/`
4. Add to router in `api/v1/router.py`
5. Write tests in `tests/`

## 📝 Key Design Decisions

1. **No LangChain**: Direct Gemini API for simplicity
2. **Server-side ML**: Centralized model, easier updates
3. **On-device STT**: Android handles speech-to-text
4. **SQLite**: Simple setup for local dev
5. **No Auth**: Single user for MVP/demo
6. **FastAPI**: Modern, async, auto-docs
7. **Pydantic**: Type safety, validation
8. **WebSocket**: Real-time voice agent

## 🎯 Next Steps

- [ ] Add JWT authentication
- [ ] Implement user registration
- [ ] Add rate limiting
- [ ] Set up logging
- [ ] Docker containerization
- [ ] CI/CD pipeline
- [ ] Production deployment guide
- [ ] API versioning strategy
- [ ] Caching layer (Redis)
- [ ] Advanced analytics

## 📚 Documentation Files

- `README.md` - Getting started guide
- `STRUCTURE.md` - This file (architecture overview)
- `ml/models/README.md` - ML model documentation
- See main project docs: `FEATURES.md`, `process_flow.md`, `claude.md`
