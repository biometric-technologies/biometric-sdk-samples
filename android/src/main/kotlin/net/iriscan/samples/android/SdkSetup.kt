package net.iriscan.samples.android

import android.content.Context
import net.iriscan.sdk.BiometricSdkFactory
import net.iriscan.sdk.BiometricSdkOperations
import net.iriscan.sdk.iris.IrisEncodeProperties
import net.iriscan.sdk.iris.IrisExtractProperties
import net.iriscan.sdk.iris.IrisMatchProperties

/**
 * Sets up the SDK for iris and returns it; the app and the instrumented test both start here.
 * The SDK keeps the first setup of the process, so calling this again costs nothing.
 */
fun setUpSdk(context: Context): BiometricSdkOperations {
    BiometricSdkFactory.initialize(
        BiometricSdkFactory.configBuilder()
            // On Android the SDK reads its files (models, cache) through a Context. It keeps it for
            // the life of the process, so it gets the application context, not an activity.
            .withContext(context.applicationContext)
            // the SDK's default iris settings; the match threshold is in IrisMatchProperties
            .withIris(IrisExtractProperties(), IrisEncodeProperties(), IrisMatchProperties())
            .build()
    )
    return BiometricSdkFactory.getInstance() ?: error("The SDK did not initialize")
}
