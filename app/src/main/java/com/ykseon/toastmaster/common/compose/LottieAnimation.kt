package com.ykseon.toastmaster.common.compose

import android.widget.FrameLayout
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

@Composable
fun LottieAnimation(
    modifier: Modifier = Modifier,
    assetName: String,
) {
    val density = LocalDensity.current.density

    BoxWithConstraints(modifier = modifier) {
        val constraints = maxWidth.value * density to maxHeight.value * density

        AndroidView(
            factory = { context ->
                LottieAnimationView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(constraints.first.toInt(), constraints.second.toInt())
                    setAnimation(assetName)
                    repeatCount = LottieDrawable.INFINITE
                    playAnimation()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
