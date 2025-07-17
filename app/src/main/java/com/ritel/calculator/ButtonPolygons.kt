package com.ritel.calculator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import kotlin.math.abs

private const val biggerRatio = 1.15f
private fun biggerFactor(bigger: Boolean, ratioAdjust: Float = 1f): Float {
    return if (bigger) ratioAdjust * biggerRatio else 1f
}

val circlePolygon: (Boolean) -> RoundedPolygon = { bigger ->
    RoundedPolygon(
        numVertices = 100,
        radius = biggerFactor(bigger) * 0.77f,
        rounding = CornerRounding(0.5f),
    )
}

val quadPolygon: (Boolean) -> RoundedPolygon = { bigger ->
    RoundedPolygon(
        numVertices = 4,
        radius = biggerFactor(bigger) * 1f,
        rounding = CornerRounding(0.35f),
    )
}

val hexPolygon: (Boolean) -> RoundedPolygon = { bigger ->
    RoundedPolygon.star(
        numVerticesPerRadius = 6,
        radius = biggerFactor(bigger) * 1f,
        innerRadius = biggerFactor(bigger) * 0.65f,
        rounding = CornerRounding(0.5f)
    )
}

val octPolygon: (Boolean) -> RoundedPolygon = { bigger ->
    RoundedPolygon.star(
        numVerticesPerRadius = 8,
        radius = biggerFactor(bigger) * 0.96f,
        innerRadius = biggerFactor(bigger) * 0.64f,
        rounding = CornerRounding(0.25f),
    )
}

fun polygonDegrees(polygon: (Boolean) -> RoundedPolygon): Float {
    return when (polygon) {
        quadPolygon -> 45f
        octPolygon -> 22.5f
        else -> 0f
    }
}

fun polygonSymmetricDegrees(polygon: (Boolean) -> RoundedPolygon): Float {
    return when (polygon) {
        circlePolygon -> 0f
        quadPolygon -> 90f
        hexPolygon -> 60f
        octPolygon -> 45f
        else -> 0f
    }
}

@Preview
@Composable
fun PolygonsPreview() {
    val polygons = listOf(
        circlePolygon, quadPolygon, hexPolygon, octPolygon
    )
    Row {
        Column {
            polygons.forEach { polygon ->
                Canvas(modifier = Modifier.size(64.dp)) {
                    val path = polygon(false).toPath().asComposePath()
                    val scale = size.minDimension / 2f
                    path.transform(Matrix().apply {
                        translate(size.width / 2f, size.height / 2f)
                        scale(scale, scale)
                        rotateZ(polygonDegrees(polygon))
                    })
                    drawPath(path, color = Color.White)
                }
            }
        }
        Column {
            polygons.forEach { polygon ->
                Canvas(modifier = Modifier.size(64.dp)) {
                    val path = polygon(true).toPath().asComposePath()
                    val scale = size.minDimension / 2f
                    path.transform(Matrix().apply {
                        translate(size.width / 2f, size.height / 2f)
                        scale(scale, scale)
                        rotateZ(polygonDegrees(polygon))
                    })
                    drawPath(path, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun createMorphShape(
    startPolygon: (Boolean) -> RoundedPolygon,
    startDegrees: Float = 0f,
    endPolygon: (Boolean) -> RoundedPolygon,
    endDegrees: Float = 0f,
    bigger: Boolean = true,
    percentage: Float,
): MorphPolygonShape {
    val morph = remember { Morph(startPolygon(!bigger), endPolygon(bigger)) }
    return MorphPolygonShape(
        morph, startDegrees, endDegrees, percentage
    )
}

@Composable
fun simpleCreateMorphShape(
    startPolygon: (Boolean) -> RoundedPolygon,
    endPolygon: (Boolean) -> RoundedPolygon,
    bigger: Boolean = true,
    percentage: Float
): MorphPolygonShape {
    // calculate and apply the best degrees

    val startBase = polygonDegrees(startPolygon)
    val endBase = polygonDegrees(endPolygon)
    val startSym = polygonSymmetricDegrees(startPolygon)
    val endSym = polygonSymmetricDegrees(endPolygon)

    if (abs(startSym) < 0.01f) {
        return createMorphShape(
            startPolygon, endBase, endPolygon, endBase, bigger, percentage
        )
    }
    if (abs(endSym) < 0.01f) {
        return createMorphShape(
            startPolygon, startBase, endPolygon, startBase, bigger, percentage
        )
    }

    var startDegrees = startBase
    var endDegrees = endBase

    val baseDiff = endBase - startBase
    val symDiff = endSym - startSym

    // If either shape is a circle (infinite symmetry), no complex rotation calculation is needed.
    // We can just align the start shape to the end shape's orientation.
    if (abs(baseDiff) < 0.01f || abs(symDiff) < 0.01f) {
        return createMorphShape(
            startPolygon, startDegrees, endPolygon, endDegrees, bigger, percentage
        )
    }

    var symDiffCount: Int = (baseDiff / symDiff).toInt()

    if (abs(baseDiff - symDiffCount * symDiff) - abs(baseDiff - (symDiffCount + 1) * symDiff) > 0.01f) {
        symDiffCount++
    }

    startDegrees -= symDiffCount * startSym
    endDegrees -= symDiffCount * endSym

    return createMorphShape(
        startPolygon, startDegrees, endPolygon, endDegrees, bigger, percentage
    )
}

@Composable
fun circleToQuadMorphShape(
    percentage: Float
): MorphPolygonShape {
    return simpleCreateMorphShape(
        startPolygon = circlePolygon, endPolygon = quadPolygon, percentage = percentage
    )
}

@Composable
fun quadToHexMorphShape(
    percentage: Float
): MorphPolygonShape {
    return simpleCreateMorphShape(
        startPolygon = quadPolygon, endPolygon = hexPolygon, percentage = percentage
    )
}

@Composable
fun hexToOctMorphShape(
    percentage: Float
): MorphPolygonShape {
    return simpleCreateMorphShape(
        startPolygon = hexPolygon, endPolygon = octPolygon, percentage = percentage
    )
}

@Composable
fun octToHexMorphShape(
    percentage: Float
): MorphPolygonShape {
    return simpleCreateMorphShape(
        startPolygon = octPolygon, endPolygon = hexPolygon, percentage = percentage
    )
}