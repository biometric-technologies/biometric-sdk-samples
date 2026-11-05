# Samples for auto/face-jvm-tensor-sizes

Commit a430dd0635f773b4d6ad7be6b4e3c82b16f5c047 of biometric-sdk-samples, biometric-sdk auto/face-jvm-tensor-sizes, run https://github.com/biometric-technologies/biometric-sdk-samples/actions/runs/42001325129
Jobs: jvm failure, java/web/c success, android success, ios skipped

### android
| modality | encoded | comparisons as expected |
|---|---|---|
| iris | 12 / 12 | 66 / 66 |
 
Failures: none

### java
| modality | encoded | comparisons as expected |
|---|---|---|
| iris | 12 / 12 | 66 / 66 |

Failures: none

### jvm

## SDK samples on the JVM

### Image decoder: 0 of 0 PNG/BMP files decoded exactly as ImageIO does

### iris: 12 of 12 images encoded, 66 of 66 comparisons as expected

Same person: 12 of 12 match, scores 0.707 to 0.991. Different people: 54 of 54 do not match, scores 0.522 to 0.624. Average encoding time 194 ms.

### face: 0 of 8 images encoded, 0 of 0 comparisons as expected

Same person: 0 of 0 match. Different people: 0 of 0 do not match. Average encoding time 0 ms.

| Problem | Images | Score |
|---|---|---|
| not encoded: IllegalStateException: Face operations are unavailable: IOException: Socket timeout has expired [url=https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true, socket_timeout=unknown] ms | face-001-neutral-front | |
| not encoded: IllegalStateException: Face operations are unavailable: IOException: Socket timeout has expired [url=https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true, socket_timeout=unknown] ms | face-001-smiling-front | |
| not encoded: IllegalStateException: Face operations are unavailable: IOException: Socket timeout has expired [url=https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true, socket_timeout=unknown] ms | face-004-neutral-front | |
| not encoded: IllegalStateException: Face operations are unavailable: IOException: Socket timeout has expired [url=https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true, socket_timeout=unknown] ms | face-004-smiling-front | |
| not encoded: IllegalStateException: Face operations are unavailable: IOException: Socket timeout has expired [url=https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true, socket_timeout=unknown] ms | face-005-neutral-front | |
| not encoded: IllegalStateException: Face operations are unavailable: IOException: Socket timeout has expired [url=https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true, socket_timeout=unknown] ms | face-005-smiling-front | |
| not encoded: IllegalStateException: Face operations are unavailable: IOException: Socket timeout has expired [url=https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true, socket_timeout=unknown] ms | face-008-neutral-front | |
| not encoded: IllegalStateException: Face operations are unavailable: IOException: Socket timeout has expired [url=https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true, socket_timeout=unknown] ms | face-008-smiling-front | |

8 expectations failed.

### web
| modality | encoded | comparisons as expected |
|---|---|---|
| iris | 12 / 12 | 66 / 66 |
| face | 8 / 8 | 28 / 28 |

Failures: none

