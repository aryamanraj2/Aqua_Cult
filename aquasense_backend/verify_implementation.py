"""
Implementation Verification Script

Verifies that all Voice Agent components are properly implemented.
Run this BEFORE starting the server to check implementation completeness.
"""
import os
import sys
from pathlib import Path


def check_file_exists(filepath, description):
    """Check if a file exists"""
    exists = Path(filepath).exists()
    status = "✓" if exists else "✗"
    print(f"{status} {description}: {filepath}")
    return exists


def check_file_has_content(filepath, min_lines, description):
    """Check if a file has minimum number of lines"""
    if not Path(filepath).exists():
        print(f"✗ {description}: {filepath} (not found)")
        return False

    with open(filepath, 'r') as f:
        lines = len(f.readlines())

    has_content = lines >= min_lines
    status = "✓" if has_content else "✗"
    print(f"{status} {description}: {filepath} ({lines} lines)")
    return has_content


def check_env_file():
    """Check if .env file exists and has GEMINI_API_KEY"""
    env_path = Path(".env")

    if not env_path.exists():
        print("✗ .env file not found")
        print("  Copy .env.example to .env and add your GEMINI_API_KEY")
        return False

    with open(env_path, 'r') as f:
        content = f.read()

    if "GEMINI_API_KEY" not in content:
        print("✗ GEMINI_API_KEY not found in .env")
        return False

    if "your-key-here" in content or "AIza" not in content:
        print("⚠ GEMINI_API_KEY may not be set correctly in .env")
        return False

    print("✓ .env file configured correctly")
    return True


def main():
    print("\n" + "="*70)
    print(" "*15 + "Voice Agent Implementation Verification")
    print("="*70 + "\n")

    all_checks = []

    # Core endpoint files
    print("Core Endpoint Files:")
    all_checks.append(check_file_has_content(
        "api/v1/endpoints/voice_agent.py",
        70,
        "Voice Agent Endpoint"
    ))

    # Service layer
    print("\nService Layer:")
    all_checks.append(check_file_has_content(
        "services/voice_service.py",
        130,
        "Voice Service"
    ))

    # WebSocket components
    print("\nWebSocket Components:")
    all_checks.append(check_file_has_content(
        "websocket/handler.py",
        180,
        "WebSocket Handler"
    ))
    all_checks.append(check_file_has_content(
        "websocket/message_types.py",
        140,
        "Message Types"
    ))

    # AI integration
    print("\nAI Integration:")
    all_checks.append(check_file_has_content(
        "ai/gemini_client.py",
        270,
        "Gemini Client"
    ))
    all_checks.append(check_file_has_content(
        "ai/prompts.py",
        110,
        "AI Prompts"
    ))
    all_checks.append(check_file_has_content(
        "ai/session_memory.py",
        150,
        "Session Memory"
    ))

    # Configuration
    print("\nConfiguration:")
    all_checks.append(check_file_has_content(
        "config/settings.py",
        40,
        "Settings"
    ))
    all_checks.append(check_env_file())

    # Router
    print("\nRouter Configuration:")
    all_checks.append(check_file_exists(
        "api/v1/router.py",
        "API Router"
    ))

    # Main application
    print("\nMain Application:")
    all_checks.append(check_file_exists(
        "main.py",
        "FastAPI Main"
    ))

    # Dependencies
    print("\nDependencies:")
    all_checks.append(check_file_exists(
        "requirements.txt",
        "Requirements File"
    ))

    # Testing
    print("\nTesting:")
    all_checks.append(check_file_exists(
        "test_websocket.py",
        "WebSocket Test Script"
    ))

    # Documentation
    print("\nDocumentation:")
    all_checks.append(check_file_exists(
        "VOICE_AGENT_IMPLEMENTATION.md",
        "Implementation Documentation"
    ))

    # Summary
    print("\n" + "="*70)
    passed = sum(all_checks)
    total = len(all_checks)
    percentage = (passed / total) * 100

    print(f"\nResults: {passed}/{total} checks passed ({percentage:.1f}%)")

    if passed == total:
        print("\n✓ ALL CHECKS PASSED - Implementation is complete!")
        print("\nNext steps:")
        print("1. Activate virtual environment: source venv/bin/activate")
        print("2. Install dependencies: pip install -r requirements.txt")
        print("3. Start server: uvicorn main:app --reload --host 0.0.0.0 --port 8000")
        print("4. Run tests: python test_websocket.py")
        return 0
    else:
        print("\n✗ Some checks failed - Review missing components above")
        return 1


if __name__ == "__main__":
    sys.exit(main())
