# GLIC YCbCr subset

YCbCr is available in Adaptive blocks. RGB remains the default; fixed-block variations and motion retain their existing format.

Input RGB is converted with the retained toYCbCr function before segmentation. The outside RGB128 reference is converted too. Processing operates on Y/Cb/Cr byte planes; reconstructed planes are converted by fromYCbCr before display or export. Alpha is preserved. Float32 operation boundaries, integer truncation, clamping, 127.5 offsets and the retained +1 red/blue corrections are preserved. This is the retained GLIC conversion, not a claim of generic standards compliance or lossless round trip.

ColorAdaptiveOracle.java retains the earlier seeded oracle methods and adds the original conversion bodies, adapting Processing color to Java int and decimal literals to float literals. 432 cases cover three dimensions, four seeds, four predictors, three thresholds and three quantization values. Tests compare split bits, draw counts, encoded values and final RGB output; alpha and pixel coverage are checked separately. Existing 432 adaptive RGB and 512 fixed RGB cases remain passing.

Projects in YCbCr use artifice-adaptive-ycbcr-xorshift32-v1. Legacy RGB projects remain readable. The saved engine must agree with the selected color space; source hash, regenerated tree bits and final output hash are checked before replacing the current image. The worker cache includes color space to prevent reuse of RGB trees in YCbCr.

Retained colorspaces.pde SHA-256: e546ba6bb7e27bb76b75dabd083b5076dac553f2f06bea8941f8962ab2245d0a
