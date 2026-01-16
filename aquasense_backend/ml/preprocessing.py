"""
Image Preprocessing - Prepare images for ML model inference
"""
import numpy as np
from PIL import Image
from typing import Tuple


def preprocess_image(
    image: Image.Image,
    target_size: Tuple[int, int] = (224, 224),
    normalize: bool = True
) -> np.ndarray:
    """
    Preprocess image for model inference.

    Args:
        image: PIL Image object
        target_size: Target size for resizing (width, height)
        normalize: Whether to normalize pixel values to [0, 1]

    Returns:
        Preprocessed image as numpy array with shape (1, height, width, channels)
    """
    # Convert to RGB if needed
    if image.mode != 'RGB':
        image = image.convert('RGB')

    # Resize image
    image = image.resize(target_size, Image.Resampling.LANCZOS)

    # Convert to numpy array
    img_array = np.array(image, dtype=np.float32)

    # Normalize if requested
    if normalize:
        img_array = img_array / 255.0

    # Add batch dimension
    img_array = np.expand_dims(img_array, axis=0)

    return img_array


def augment_image(image: Image.Image) -> Image.Image:
    """
    Apply data augmentation to image (for training or robustness).

    Args:
        image: PIL Image object

    Returns:
        Augmented image
    """
    # Basic augmentation for inference robustness
    # Can be expanded for training data augmentation

    # Random horizontal flip (50% chance)
    import random
    if random.random() > 0.5:
        image = image.transpose(Image.FLIP_LEFT_RIGHT)

    return image


def crop_center(image: Image.Image, crop_size: Tuple[int, int]) -> Image.Image:
    """
    Crop the center of the image.

    Args:
        image: PIL Image object
        crop_size: Size of the crop (width, height)

    Returns:
        Cropped image
    """
    width, height = image.size
    crop_width, crop_height = crop_size

    left = (width - crop_width) // 2
    top = (height - crop_height) // 2
    right = left + crop_width
    bottom = top + crop_height

    return image.crop((left, top, right, bottom))


def resize_with_padding(
    image: Image.Image,
    target_size: Tuple[int, int],
    fill_color: Tuple[int, int, int] = (0, 0, 0)
) -> Image.Image:
    """
    Resize image while maintaining aspect ratio with padding.

    Args:
        image: PIL Image object
        target_size: Target size (width, height)
        fill_color: Color for padding (R, G, B)

    Returns:
        Resized image with padding
    """
    # Calculate aspect ratios
    img_ratio = image.size[0] / image.size[1]
    target_ratio = target_size[0] / target_size[1]

    if img_ratio > target_ratio:
        # Image is wider than target
        new_width = target_size[0]
        new_height = int(new_width / img_ratio)
    else:
        # Image is taller than target
        new_height = target_size[1]
        new_width = int(new_height * img_ratio)

    # Resize image
    resized = image.resize((new_width, new_height), Image.Resampling.LANCZOS)

    # Create new image with padding
    padded = Image.new('RGB', target_size, fill_color)

    # Paste resized image in center
    paste_x = (target_size[0] - new_width) // 2
    paste_y = (target_size[1] - new_height) // 2
    padded.paste(resized, (paste_x, paste_y))

    return padded


def validate_image(image: Image.Image, max_size_mb: float = 10.0) -> bool:
    """
    Validate image for processing.

    Args:
        image: PIL Image object
        max_size_mb: Maximum allowed image size in MB

    Returns:
        True if valid, False otherwise
    """
    # Check image mode
    if image.mode not in ['RGB', 'RGBA', 'L']:
        return False

    # Check image size (rough estimate)
    width, height = image.size
    num_pixels = width * height
    channels = len(image.getbands())
    size_bytes = num_pixels * channels

    max_size_bytes = max_size_mb * 1024 * 1024

    if size_bytes > max_size_bytes:
        return False

    return True
