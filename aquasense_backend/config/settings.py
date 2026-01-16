"""
Application Settings - Environment configuration using Pydantic
"""
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """
    Application settings loaded from environment variables.
    """
    # API Keys
    GEMINI_API_KEY: str

    # Database
    DATABASE_URL: str = "sqlite:///./aquasense.db"

    # Server
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    DEBUG: bool = True

    # File Upload
    MAX_UPLOAD_SIZE: int = 10 * 1024 * 1024  # 10MB
    ALLOWED_IMAGE_TYPES: set = {"image/jpeg", "image/png", "image/jpg"}

    # ML Model
    DISEASE_MODEL_PATH: str = "models/fish_disease.keras"

    # Gemini API
    GEMINI_MODEL: str = "gemini-flash-latest"
    GEMINI_MAX_TOKENS: int = 8192
    GEMINI_TEMPERATURE: float = 0.7

    # Session Management
    SESSION_TIMEOUT: int = 3600  # 1 hour in seconds

    class Config:
        env_file = ".env"
        case_sensitive = True


# Global settings instance
settings = Settings()
