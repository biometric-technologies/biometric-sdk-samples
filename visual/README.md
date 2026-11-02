# Visual report

One HTML page that shows what the SDK does with every sample image, step by step, so a failure
can be found by looking rather than by reading scores:

- **Iris:** the photo with the segmentation drawn over it (pupil red, limbus blue, eyelids
  orange; from `IrisExtractor.segment`), the unwrapped iris with masked samples in yellow
  (`IrisExtractor.extract`), the ISO quality values and issues, and whether a template was made.
  When an image fails, the card turns red and says at which step: no iris found, iris found but
  too little of it visible, or no template.
- **Face:** the photo and the face the extractor cut out of it.
- **Every pair** of one modality in a matrix with its score: green when the SDK decided as
  expected (same person: match, different people: no match), red when not.

From the repository root, with the images and the SDK source in place (main README, "Getting
started"):

```sh
bash visual/run.sh            # writes results/report.html
open results/report.html      # macOS; xdg-open on Linux
```

To look at an SDK change, point `sdk/` at your working copy (`ln -s ~/work/biometric-sdk sdk`)
and run it again.

The report is for your own computer: the iris images may be used for research but not
published. CI runs it with `--no-images`, which keeps the numbers and the matrix and leaves out
every picture. It also writes `results/visual.md` (the shared results table) and exits with 1 if
something was not as expected.

Needs biometric-sdk with `IrisExtractor.segment` (biometric-sdk PR #21).
