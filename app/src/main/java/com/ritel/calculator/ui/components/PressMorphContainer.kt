package com.ritel.calculator.ui.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

class MorphPolygonShape(
    private val morph: Morph,
    private val startDegrees: Float = 0f,
    private val endDegrees: Float = 0f,
    private val percentage: Float
) : Shape {
    private val matrix = Matrix()

    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        matrix.reset()

        var morphProgress = percentage
        var scaleFactor = 1f

        if (percentage < 0f) {
            morphProgress = 0f
            scaleFactor = 1f + percentage * 0.3f
        } else if (percentage > 1f) {
            morphProgress = 1f
            scaleFactor = 1f + (percentage - 1f) * 0.2f
        }
        scaleFactor = scaleFactor.coerceIn(0.5f, 1.3f)

        val degrees = lerp(startDegrees, endDegrees, morphProgress)
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        matrix.scale(scaleFactor, scaleFactor)
        matrix.rotateZ(degrees)

        val path = morph.toPath(morphProgress).asComposePath()
        path.transform(matrix)

        return Outline.Generic(path)
    }
}

private val pressAnimSpec = spring<Float>(dampingRatio = 0.25f, stiffness = 1000f)
private val releaseAnimSpec = spring<Float>(dampingRatio = 0.4f, stiffness = 150f)

@Suppress("unused")
@Composable
fun PressMorphBox( // Don't use this for anything clickable, use MorphButton instead
    morphShape: @Composable (Float) -> MorphPolygonShape,
    modifier: Modifier = Modifier,
    indication: Indication? = LocalIndication.current,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable (BoxScope.() -> Unit)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedProgress = animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        label = "pressButton",
        animationSpec = if (isPressed) pressAnimSpec else releaseAnimSpec
    )

    Box(
        modifier = modifier
            .clip(morphShape(animatedProgress.value))
            .clickable(
                interactionSource = interactionSource, indication = indication
            ) { Log.d("ClickTest", "Box Clicked") },
        contentAlignment = contentAlignment,
        content = content
    )
}

@Composable
fun MorphButton(
    morphShape: @Composable (Float) -> MorphPolygonShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable (RowScope.() -> Unit)
) {
    val interactionSourceLocal = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by interactionSourceLocal.collectIsPressedAsState()
    val animatedProgress = animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        label = "pressButton",
        animationSpec = if (isPressed) pressAnimSpec else releaseAnimSpec
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = morphShape(animatedProgress.value),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSourceLocal,
        content = content
    )
}