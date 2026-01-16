# ML Models Directory

This directory should contain the TensorFlow/Keras model for fish disease detection.

## Expected Model

- **Filename**: `fish_disease.keras`
- **Format**: TensorFlow/Keras (.keras format)
- **Input**: 224x224 RGB images
- **Output**: Probabilities for disease classes

## Model Classes

The model should classify fish images into the following categories:

1. healthy
2. bacterial_infection
3. fungal_infection
4. parasitic_infection
5. viral_infection
6. nutritional_deficiency
7. environmental_stress

## Getting the Model

The model is provided by the ML team. If you don't have the model yet:

1. Contact the ML team to obtain the trained model
2. Place the `.keras` file in this directory
3. Update the `DISEASE_MODEL_PATH` in `.env` if using a different name

## Model Training

If you need to train a new model, refer to the ML team's training scripts and dataset.

## Fallback Behavior

If the model file is not present, the system will:
- Log a warning on startup
- Skip ML-based disease detection
- Rely solely on Gemini AI for disease analysis

This allows the system to function without the ML model during development.
