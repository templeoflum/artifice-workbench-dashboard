# One-level Haar finishing transform

A finishing step after adaptive prediction/quantization, reconstruction and color conversion back to RGB. This follows the retained Artifice Engine wavelet-node concept; it does not claim GLIC/JWave codec compatibility or reproduce its block-scaled transform packing.

One level, orthonormal Haar, symmetric extension at odd dimensions, three RGB channels independently. Keep approximation coefficients; zero detail coefficients only when absolute magnitude is strictly less than the chosen threshold (0–255, in byte-domain coefficient units). Inverse transform, crop to original dimensions, round half upward and clamp RGB to bytes. Alpha is copied. Zero threshold is an exact identity fast path; bypass leaves the preceding output unchanged. This standalone finishing composition and byte rounding are explicit workbench choices.

For integer input, 2D one-level Haar coefficients are exact half-integers; inverse values after coefficient removal are quarter-integers. The JS implementation uses dyadic arithmetic to avoid irrational cancellation. The independent oracle executes retained _wavelet_transform_2d, _compress_dwt and _inverse_wavelet_2d with PyWavelets1.8.0. PyWavelets coefficients are canonicalized to the mathematically exact half-integer grid before thresholding, and inverse results to the quarter-integer grid before byte rounding. Largest observed coefficient adjustment: 1.1368683772161603e-13. Signed zero is normalized for numerical comparison. This qualification covers125cases across five sizes, five patterns and five thresholds. Raw irrational floating-point bit identity is not claimed.

Tests cover coefficients, inverse, thresholded pixels, edge extension, alpha, exact zero/bypass and RGB/YCbCr composition. Saved transform projects use artifice-adaptive-haar1-finish-v1 and retain color space, threshold and preceding processing settings. Existing projects default to bypass.

Reference: https://pywavelets.readthedocs.io/en/latest/ref/2d-dwt-and-idwt.html
Retained wavelet.py SHA256: f87ffb114cba319b1c1c786a59ec7e8d5ffb07d60e1394f3e622525b1045b0e4
