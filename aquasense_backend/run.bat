@echo off
REM AquaSense Backend - Quick Start Script for Windows

echo Starting AquaSense Backend...
echo.

REM Check if virtual environment exists
if not exist "venv\" (
    echo Virtual environment not found. Creating...
    python -m venv venv
    echo Virtual environment created.
)

REM Activate virtual environment
echo Activating virtual environment...
call venv\Scripts\activate.bat

REM Check if .env exists
if not exist ".env" (
    echo Warning: .env file not found!
    echo Creating .env from .env.example...
    copy .env.example .env
    echo.
    echo IMPORTANT: Please edit .env and add your GEMINI_API_KEY before proceeding.
    echo Get your API key from: https://makersuite.google.com/app/apikey
    echo.
    pause
)

REM Install dependencies
echo Installing/updating dependencies...
pip install -r requirements.txt

REM Start the server
echo.
echo Starting FastAPI server...
echo API Docs will be available at: http://localhost:8000/docs
echo.
uvicorn main:app --reload --host 0.0.0.0 --port 8000
