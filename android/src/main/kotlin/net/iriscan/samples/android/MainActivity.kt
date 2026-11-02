package net.iriscan.samples.android

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

/**
 * The app's only screen. For now it sets up the SDK and says so: the samples check runs as the
 * instrumented test (src/androidTest), and the sample photos come to this screen in a later step.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpSdk(this)
        setContentView(TextView(this).apply {
            text = "Biometric SDK is set up for iris."
            setPadding(48, 48, 48, 48)
        })
    }
}
