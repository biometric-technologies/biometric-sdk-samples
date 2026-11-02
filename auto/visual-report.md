# Samples for auto/visual-report

Commit 70227fc097291da6491a6387a4e30902db44e72a of biometric-sdk-samples, biometric-sdk iris-v2, run https://github.com/biometric-technologies/biometric-sdk-samples/actions/runs/41431435594
Jobs: jvm skipped, java/web/c failure, android skipped, ios skipped

### visual: no results

```
> Task :compileKotlin FAILED
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:11:29 Unresolved reference 'IrisBoundaries'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:65:21 Unresolved reference 'IrisBoundaries'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:148:36 Unresolved reference 'segment'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:269:23 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:269:23 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:269:23 Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:269:27 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:288:19 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:288:19 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:288:19 Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:288:25 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:289:35 Unresolved reference 'pupil'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:289:54 Unresolved reference 'pupil'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:289:75 Unresolved reference 'pupil'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:290:36 Unresolved reference 'limbus'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:290:56 Unresolved reference 'limbus'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:290:78 Unresolved reference 'limbus'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:41 Unresolved reference 'pupil'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:58 Unresolved reference 'limbus'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:86 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:86 Inapplicable candidate(s): fun <T : Any> listOfNotNull(vararg elements: T?): List<T>
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:102 Unresolved reference 'upperEyelid'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:115 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:115 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:119 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:134 Unresolved reference 'lowerEyelid'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:147 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:147 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:151 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:164 Cannot infer type for type parameter 'C'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:291:164 Cannot infer type for type parameter 'R'. Specify it explicitly.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:302:24 Unresolved reference 'IrisBoundaries'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:305:36 Unresolved reference 'pupil'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:305:59 Unresolved reference 'pupil'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:305:81 Unresolved reference 'pupil'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:306:36 Unresolved reference 'limbus'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:306:60 Unresolved reference 'limbus'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:306:83 Unresolved reference 'limbus'.
e: file:///home/runner/work/biometric-sdk-samples/biometric-sdk-samples/visual/src/main/kotlin/net/iriscan/samples/visual/Report.kt:308:17 Cannot infer type for type parameter 'T'. Specify it explicitly.
```

