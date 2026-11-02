# Samples for auto/face-match-threshold

Commit 0493f4e208e1d61246a9fe77d9a82c26a69afa69 of biometric-sdk-samples, biometric-sdk auto/face-match-threshold, run https://github.com/biometric-technologies/biometric-sdk-samples/actions/runs/41429641040
Jobs: jvm success, java/web/c success, android skipped, ios skipped

### java
| modality | encoded | comparisons as expected |
|---|---|---|
| iris | 12 / 12 | 66 / 66 |

Failures: none

### jvm

## SDK samples on the JVM

### Image decoder: 0 of 0 PNG/BMP files decoded exactly as ImageIO does

### iris: 12 of 12 images encoded, 66 of 66 comparisons as expected

Same person: 12 of 12 match, scores 0.671 to 0.997. Different people: 54 of 54 do not match, scores 0.518 to 0.597. Average encoding time 266 ms.

### face: 8 of 8 images encoded, 28 of 28 comparisons as expected

Same person: 4 of 4 match, scores 0.247 to 0.421. Different people: 24 of 24 do not match, scores 1.280 to 1.565. Average encoding time 298 ms.

All expectations met.

### web
| modality | encoded | comparisons as expected |
|---|---|---|
| iris | 12 / 12 | 66 / 66 |
| face | 8 / 8 | 28 / 28 |

Failures: none

