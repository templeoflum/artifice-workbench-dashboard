# Adaptive segmentation reference contract — first expansion

Status: 720 sample-tape reference comparisons pass (3,802 leaves). Split bits, ordered leaves, random-draw consumption and single pixel coverage match. Seeded-generator qualification and reconstructed-image parity remain pending; no adaptive image control is shipped.

## Observed retained behavior

- codec.pde builds all three channel segment trees from the original planes before prediction or reconstruction. Channels are segmented independently in order 0, 1, 2.
- Root size is the greater padded power-of-two dimension. Nodes whose origin is outside the image return without sampling or emitting a tree bit.
- Every visited node computes sampled standard deviation, even if maximum size forces a split or minimum size prevents one. Skipping this computation changes later random draws.
- Number of samples is integer truncation of max(0.1 × size², 4). Every sample draws x then y within the node; repeats are allowed. Samples beyond the image use the plane reference value (RGB 128 in the present subset).
- Float accumulators start A=0, Q=0. For k=1..limit: oldA=A; A+=(sample−A)/k; Q+=(sample−oldA)×(sample−A). Deviation is sqrt(Q/(limit−1)). Float32 operation boundaries matter.
- Split when size > max_size OR (size > min_size AND deviation > threshold). Equality does not split. Children visit top-left, top-right, bottom-left, bottom-right.
- The reference writes a split/leaf bit stream, preserving the chosen tree for decoding. Recomputing an unseeded tree is not its replay contract.
- No randomSeed call was found in the retained GLIC folder. Other random options consume the shared stream before segmentation. A seed alone cannot claim parity with an arbitrary historical session.

## Proposed bounded implementation

Keep fixed-block recipes unchanged. Add a separately versioned adaptive RGB mode with explicit minimum size, maximum size, threshold and sampling seed. Start with power-of-two sizes 8–64, current four predictors, no transforms or color-space conversion. Preserve original channel order and original-plane segmentation.

Use two validation layers: (1) feed the same explicit coordinate tape into retained Java segmentation and the new implementation, comparing every split bit and ordered leaf; (2) independently qualify the seeded generator and Float32 sampling adapter before naming it Processing-compatible. A deterministic alternative generator must be named as an Artifice choice, never silently described as the original.

Saved adaptive projects must include engine version, normalized source hash, settings, seed and ordered tree hashes. Retain the actual trees if replay depends on sampled decisions. Reject invalid trees, overlapping or uncovered pixels, illegal sizes, and incompatible engine versions. Existing fixed-block projects remain readable.

## Acceptance gate before the adaptive control ships

1. Exact Java/JavaScript split-bit and leaf-order comparison using identical sample tapes: flat fields, ramp, checkerboard, sparse edge and impulse, odd dimensions and partial edge blocks; three channels, forced splits, threshold equality and minimum-size leaves.
2. Assert full single coverage of every image pixel, bounded recursion, and repeatability. Varying the seed may change the tree; unchanged seed/settings/source must not.
3. Integrate closed-loop prediction using the selected channel tree. Compare reconstructed output and encoded residuals against the retained reference on those same trees.
4. Expose a segmentation overlay and per-channel leaf count, allowing the user to inspect why detail splits. Export/reopen a project and reproduce exact RGBA pixels.
5. Measure runtime and memory on the diagnostic and at the existing image limit before widening limits. Motion remains fixed-block until adaptive transition semantics are separately specified.

## Next work

Build the sample-tape oracle first, then adaptive tree generation and reconstruction. Color spaces, one transform, editable chains, broader motion and measured capacity follow as separate milestones. Incidental UI polish is parked.

## Retained file SHA-256

- segmentation.pde: `8c3c494da014a533e4f7be7f0b0be9b5d84651e827b82cc2481daaab3cf02324`
- codec.pde: `6741eafad343bc6cc1abe1c8426206f75a529d394cbcfa70826f3d279762a7de`
- planes.pde: `784529983936f29bdb2b6ca65640f613d6863c4440f65cf11e475021bc49a177`

## Executed oracle method

AdaptiveOracle.java embeds the retained Segment, makeSegmentation, encode-side segment and calcStdDev bodies. The only syntax adaptation is Processing’s decimal literal 0.1 → Java 0.1f. Minimal planes and bit-output adapters isolate segmentation. The sample tape returns ((drawIndex × 73 + 19) & 255) × size / 256, shared across channels and reset per configuration. It is intentionally not a substitute claim about Processing random(). Test dimensions are 16×16, 23×13 and 64×33; five synthetic patterns, two minima, two maxima, four thresholds and three channels produce 720 cases. tests/adaptive-reference.mjs checks the independent Float32 implementation against the compressed Java fixture. Original GLIC MIT attribution remains in GLIC-LICENSE.txt.
