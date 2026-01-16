#!/bin/bash

# AquaSense Test Runner Script
# Quick commands to run tests

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

PYTHON="../.venv/bin/python"

echo -e "${GREEN}=== AquaSense Test Runner ===${NC}\n"

# Function to run tests
run_test() {
    echo -e "${YELLOW}Running: $1${NC}"
    $PYTHON -m pytest $2 $3 $4
}

# Parse command line arguments
case "$1" in
    "all")
        echo "Running all tests..."
        run_test "All Tests" "tests/" "-v"
        ;;
    "database")
        echo "Running database model tests..."
        run_test "Database Tests" "tests/test_database.py" "-v"
        ;;
    "api")
        echo "Running API endpoint tests..."
        run_test "API Tests" "tests/test_api_*.py" "-v"
        ;;
    "tanks")
        echo "Running tank API tests..."
        run_test "Tank API Tests" "tests/test_api_tanks.py" "-v"
        ;;
    "products")
        echo "Running product/order API tests..."
        run_test "Product API Tests" "tests/test_api_products.py" "-v"
        ;;
    "analysis")
        echo "Running analysis API tests..."
        run_test "Analysis API Tests" "tests/test_api_analysis.py" "-v"
        ;;
    "services")
        echo "Running service layer tests..."
        run_test "Service Tests" "tests/test_services.py" "-v"
        ;;
    "websocket")
        echo "Running WebSocket tests..."
        run_test "WebSocket Tests" "tests/test_websocket.py" "-v"
        ;;
    "ml")
        echo "Running ML model tests..."
        run_test "ML Tests" "tests/test_ml_model.py" "-v"
        ;;
    "unit")
        echo "Running unit tests only..."
        run_test "Unit Tests" "tests/" "-v -m unit"
        ;;
    "integration")
        echo "Running integration tests only..."
        run_test "Integration Tests" "tests/" "-v -m integration"
        ;;
    "passing")
        echo "Running only passing tests..."
        run_test "Passing Tests" "tests/test_database.py tests/test_ml_model.py" "-v"
        ;;
    "coverage")
        echo "Running tests with coverage report..."
        $PYTHON -m pytest tests/ --cov=. --cov-report=html --cov-report=term
        echo -e "\n${GREEN}Coverage report generated at: htmlcov/index.html${NC}"
        ;;
    "quick")
        echo "Running quick smoke test (database + ML)..."
        run_test "Quick Tests" "tests/test_database.py tests/test_ml_model.py" "-v"
        ;;
    "failed")
        echo "Re-running previously failed tests..."
        run_test "Failed Tests" "tests/" "--lf -v"
        ;;
    "single")
        if [ -z "$2" ]; then
            echo -e "${RED}Error: Please specify test path${NC}"
            echo "Usage: ./RUN_TESTS.sh single tests/test_database.py::TestUserModel::test_create_user"
            exit 1
        fi
        echo "Running single test: $2"
        run_test "Single Test" "$2" "-v"
        ;;
    "help"|"")
        echo "Usage: ./RUN_TESTS.sh [command]"
        echo ""
        echo "Commands:"
        echo "  all          - Run all tests"
        echo "  database     - Run database model tests"
        echo "  api          - Run all API endpoint tests"
        echo "  tanks        - Run tank API tests"
        echo "  products     - Run product/order API tests"
        echo "  analysis     - Run analysis/disease detection tests"
        echo "  services     - Run service layer tests"
        echo "  websocket    - Run WebSocket tests"
        echo "  ml           - Run ML model tests"
        echo "  unit         - Run unit tests only"
        echo "  integration  - Run integration tests only"
        echo "  passing      - Run only currently passing tests"
        echo "  coverage     - Run tests with coverage report"
        echo "  quick        - Quick smoke test (database + ML)"
        echo "  failed       - Re-run previously failed tests"
        echo "  single <path> - Run a single test"
        echo "  help         - Show this help message"
        echo ""
        echo "Examples:"
        echo "  ./RUN_TESTS.sh all"
        echo "  ./RUN_TESTS.sh database"
        echo "  ./RUN_TESTS.sh coverage"
        echo "  ./RUN_TESTS.sh single tests/test_database.py::TestUserModel::test_create_user"
        ;;
    *)
        echo -e "${RED}Unknown command: $1${NC}"
        echo "Run './RUN_TESTS.sh help' for usage"
        exit 1
        ;;
esac
