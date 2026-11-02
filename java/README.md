# Java sample

A plain Java console application that runs the samples check with the SDK: it encodes every iris
image of [samples/manifest.json](../samples/manifest.json), compares every pair and checks that two
images of the same person match and images of different people do not. Face is not in the check yet.

Requirements: JDK 17 or newer, the images in `samples/data/` and a biometric-sdk checkout in `sdk/`
(see the main [README](../README.md)). Gradle comes with the SDK checkout.

From the repository root:

```sh
scripts/fetch-samples.sh
git clone -b iris-v2 https://github.com/biometric-technologies/biometric-sdk sdk
sdk/gradlew -p java run
```

It prints the score of every comparison and the results table, writes the table to
`results/java.md` and exits with 1 when an image is not encoded or a comparison is not as expected.
CI runs it through [ci/run.sh](ci/run.sh).
