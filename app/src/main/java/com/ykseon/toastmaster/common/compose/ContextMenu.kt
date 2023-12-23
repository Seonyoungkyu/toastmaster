package com.ykseon.toastmaster.common.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ykseon.toastmaster.R

data class ContextMenuItem(val name: String, val action: (id: Long) -> Unit)
data class ContextMenuState(val show: Boolean, val x: Int, val y: Int, val id: Long, val items: List<ContextMenuItem>)

@Composable
fun ContextMenu(
    touchX : Int,
    touchY: Int,
    parentSize: Size,
    id: Long,
    items: List<ContextMenuItem>,
    onUpdateMenuBound: (rect: Rect)-> Unit = {},
    onDismiss: ()->Unit ) {

    Box( modifier = Modifier
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {

                val adjustX = (touchX - placeable.width / 2).let {
                    if (it < 0 ) 0
                    else if (it > parentSize.width -placeable.width) parentSize.width -placeable.width
                    else it
                }
                val adjustY =(touchY - placeable.height - 50).let {
                    if (it < 0 ) 0
                    else if (it > parentSize.height -placeable.height) parentSize.height -placeable.height
                    else it
                }
                placeable.placeRelative(adjustX.toInt(), adjustY.toInt())
            }
        }
    )
    {
        Box( modifier = Modifier
            .background(
                color = colorResource(id = R.color.translucent_gray),
                shape = RoundedCornerShape(8.dp)
            )
            .onGloballyPositioned { layoutCoordinates ->
                // Update the menu bounds when the layout is globally positioned
                onUpdateMenuBound(layoutCoordinates.boundsInRoot())
            }
            .clickable {  }
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
            ) {
                items.forEach { item ->
                    Box (modifier = Modifier.widthIn(min = 100.dp).clickable {
                        item.action(id)
                        onDismiss()
                    }
                    ) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = item.name,
                            style = TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = MaterialTheme.typography.button.fontSize,
                            ),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
