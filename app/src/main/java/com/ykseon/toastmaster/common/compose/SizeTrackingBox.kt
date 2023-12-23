package com.ykseon.toastmaster.common.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

@Composable
fun SizeTrackingBox(
    modifier: Modifier,
    onSizeChanged: (width: Float, height: Float) -> Unit,
    content: @Composable BoxScope.() -> Unit
)
{
    Box(
        modifier = Modifier
            .then(modifier)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val width = placeable.width
                val height = placeable.height
                onSizeChanged(width.toFloat(), height.toFloat())
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            },
        content = content
    )
}
