# AquaSense Backend

FastAPI backend for the AquaSense aquaculture management system.

## Features

- **Tank Management**: CRUD operations for fish tanks and water quality monitoring
- **Disease Detection**: ML-based fish disease detection using TensorFlow/Keras
- **AI Analysis**: Gemini AI integration for recommendations and insights
- **Voice Agent**: WebSocket-based voice assistant for farmers
- **Marketplace**: Product catalog and order management
- **Real-time Communication**: WebSocket support for live interactions

## Technology Stack

- **FastAPI**: Modern, fast web framework
- **SQLAlchemy**: SQL toolkit and ORM
- **Gemini API**: Google's generative AI
- **TensorFlow**: Machine learning model inference
- **SQLite**: Local database (switch to PostgreSQL for production)
- **WebSocket**: Real-time bidirectional communication

## Prerequisites

- Python 3.10 or higher
- pip (Python package manager)
- Gemini API key from [Google AI Studio](https://makersuite.google.com/app/apikey)

## Installation

1. **Navigate to backend directory**
   ```bash
   cd aquasense_backend
   ```

2. **Create virtual environment**
   ```bash
   python -m venv venv
   ```

3. **Activate virtual environment**
   - Windows:
     ```bash
     venv\Scripts\activate
     ```
   - macOS/Linux:
     ```bash
     source venv/bin/activate
     ```

4. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

5. **Configure environment variables**
   ```bash
   cp .env.example .env
   ```

   Edit `.env` and add your Gemini API key:
   ```
   GEMINI_API_KEY=your_actual_api_key_here
   ```

## Running the Server

### Development Mode (with auto-reload)

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Production Mode

```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --workers 4
```

### Using Python directly

```bash
python main.py
```

The server will start at `http://localhost:8000`

## API Documentation

Once the server is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc
- **OpenAPI JSON**: http://localhost:8000/openapi.json

## Project Structure

```
aquasense_backend/
├── main.py                 # Application entry point
├── requirements.txt        # Python dependencies
├── .env.example           # Environment variables template
├── config/                # Configuration
│   ├── settings.py        # App settings
│   └── database.py        # Database setup
├── models/                # SQLAlchemy ORM models
│   ├── user.py
│   ├── tank.py
│   ├── water_quality.py
│   ├── product.py
│   └── order.py
├── schemas/               # Pydantic schemas
│   ├── tank.py
│   ├── analysis.py
│   ├── voice.py
│   └── product.py
├── api/v1/               # API endpoints
│   ├── router.py
│   └── endpoints/
│       ├── tanks.py
│       ├── products.py
│       ├── analysis.py
│       └── voice_agent.py
├── services/             # Business logic
│   ├── tank_service.py
│   ├── product_service.py
│   ├── analysis_service.py
│   └── voice_service.py
├── ai/                   # AI integration
│   ├── gemini_client.py
│   ├── prompts.py
│   └── session_memory.py
├── ml/                   # ML models
│   ├── disease_classifier.py
│   ├── preprocessing.py
│   └── models/
│       └── fish_disease.keras
├── knowledge/            # Knowledge base
│   ├── diseases.json
│   ├── treatments.json
│   └── species_info.json
└── websocket/           # WebSocket handlers
    ├── handler.py
    └── message_types.py
```

## API Endpoints

### Tanks
- `GET /api/v1/tanks` - List all tanks
- `GET /api/v1/tanks/{id}` - Get tank details
- `POST /api/v1/tanks` - Create new tank
- `PUT /api/v1/tanks/{id}` - Update tank
- `DELETE /api/v1/tanks/{id}` - Delete tank
- `POST /api/v1/tanks/{id}/water-quality` - Add water quality reading
- `GET /api/v1/tanks/{id}/water-quality` - Get water quality history

### Products & Orders
- `GET /api/v1/products` - List products
- `GET /api/v1/products/{id}` - Get product details
- `POST /api/v1/products` - Create product
- `POST /api/v1/products/orders` - Create order
- `GET /api/v1/products/orders` - List orders

### Analysis
- `POST /api/v1/analysis/disease-detection` - Detect diseases from image
- `POST /api/v1/analysis/tank-analysis` - Comprehensive tank analysis
- `POST /api/v1/analysis/recommendation` - Get AI recommendations

### Voice Agent
- `WS /api/v1/voice/ws/{session_id}` - WebSocket connection for voice agent

## Testing

Run tests with pytest:

```bash
pytest
```

With coverage:

```bash
pytest --cov=. tests/
```

## Database

### Initialize Database

The database is automatically created on first run. Tables are created via SQLAlchemy's `create_all()`.

### Reset Database

To reset the database:

```bash
rm aquasense.db
```

The database will be recreated on next server start.

### Migrations (Optional)

For production, use Alembic for migrations:

```bash
# Initialize Alembic
alembic init alembic

# Create migration
alembic revision --autogenerate -m "Description"

# Apply migration
alembic upgrade head
```

## Configuration

Key settings in `.env`:

| Variable | Description | Default |
|----------|-------------|---------|
| `GEMINI_API_KEY` | Google Gemini API key | Required |
| `DATABASE_URL` | Database connection string | `sqlite:///./aquasense.db` |
| `HOST` | Server host | `0.0.0.0` |
| `PORT` | Server port | `8000` |
| `DEBUG` | Debug mode | `True` |
| `GEMINI_MODEL` | Gemini model name | `gemini-1.5-flash` |

## ML Model

Place the disease detection model at `ml/models/fish_disease.keras`. The model should:
- Accept 224x224 RGB images
- Output probabilities for disease classes
- Use TensorFlow/Keras format (.keras)

If the model is not available, the system will skip ML inference and rely only on Gemini AI.

## Development

### Code Style

Use Black for formatting:

```bash
black .
```

Use Flake8 for linting:

```bash
flake8 .
```

### Adding New Endpoints

1. Create Pydantic schema in `schemas/`
2. Add endpoint in `api/v1/endpoints/`
3. Implement business logic in `services/`
4. Register router in `api/v1/router.py`

## Troubleshooting

### Server won't start

- Check if port 8000 is already in use
- Verify all dependencies are installed
- Check `.env` file exists with valid `GEMINI_API_KEY`

### Database errors

- Delete `aquasense.db` and restart server
- Check file permissions

### Gemini API errors

- Verify API key is correct
- Check API rate limits (free tier: 15 requests/minute)
- Ensure internet connection is active

### Import errors

- Activate virtual environment
- Reinstall dependencies: `pip install -r requirements.txt`

## Production Deployment

For production deployment:

1. Use PostgreSQL instead of SQLite
2. Set `DEBUG=False`
3. Use environment-specific `.env` files
4. Enable HTTPS
5. Add authentication/authorization
6. Use a reverse proxy (Nginx)
7. Run with multiple workers
8. Set up monitoring and logging
9. Use Docker for containerization
10. Implement rate limiting

## License

See LICENSE file in project root.

## Support

For issues and questions, refer to the main project documentation or create an issue in the repository.
