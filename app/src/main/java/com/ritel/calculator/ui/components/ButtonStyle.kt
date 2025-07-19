package com.ritel.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritel.calculator.data.model.Add
import com.ritel.calculator.data.model.CalculatorButton
import com.ritel.calculator.data.model.Clear
import com.ritel.calculator.data.model.Delete
import com.ritel.calculator.data.model.Dot
import com.ritel.calculator.data.model.Equals
import com.ritel.calculator.data.model.Numeric
import com.ritel.calculator.data.model.Operator
import com.ritel.calculator.data.model.Percent
import com.ritel.calculator.data.model.ScientificFunction
import com.ritel.calculator.data.model.SimpleButton
import com.ritel.calculator.data.model.SimpleFunction
import com.ritel.calculator.data.model.circleToQuadMorphShape
import com.ritel.calculator.data.model.hexToOctMorphShape
import com.ritel.calculator.data.model.octToHexMorphShape
import com.ritel.calculator.data.model.quadToHexMorphShape

data class StyleConfig(
    val color: Color,
    val onColor: Color,
    val staticShape: Shape = CircleShape,
    val morphShape: (@Composable (Float) -> MorphPolygonShape) = { percentage ->
        circleToQuadMorphShape(percentage)
    },
)

@Composable
fun getButtonStyle(button: CalculatorButton): StyleConfig {
    return when (button) {
        is Numeric -> StyleConfig(
            color = MaterialTheme.colorScheme.secondaryContainer,
            onColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        is Dot -> StyleConfig(
            color = MaterialTheme.colorScheme.secondaryContainer,
            onColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        is Delete -> StyleConfig(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        is Operator -> StyleConfig(
            color = MaterialTheme.colorScheme.primary,
            onColor = MaterialTheme.colorScheme.onPrimary,
            morphShape = { percentage ->
                quadToHexMorphShape(percentage)
            })

        is SimpleFunction -> StyleConfig(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        is ScientificFunction -> StyleConfig(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        is Clear -> StyleConfig(
            color = MaterialTheme.colorScheme.error,
            onColor = MaterialTheme.colorScheme.onError,
            morphShape = { percentage ->
                hexToOctMorphShape(percentage)
            })

        is Equals -> StyleConfig(
            color = MaterialTheme.colorScheme.tertiary,
            onColor = MaterialTheme.colorScheme.onTertiary,
            morphShape = { percentage ->
                octToHexMorphShape(percentage)
            })
    }
}

@Preview
@Composable
fun ButtonConfigPreview() {
    Column {
        Row {
            PreviewButton(Numeric(1))
            PreviewButton(Add)
        }
        Row {
            PreviewButton(Percent)
            PreviewButton(Clear)
        }
        Row {
            PreviewButton(Equals)
            PreviewButton(Dot)
        }
    }
}

@Composable
fun PreviewButton(action: SimpleButton) {
    val style = getButtonStyle(action)
    MorphButton(
        morphShape = style.morphShape,
        onClick = { },
        modifier = Modifier.size(128.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = style.color, contentColor = style.onColor
        ),
        contentPadding = PaddingValues(12.dp),
    ) {
        Text(
            modifier = Modifier.wrapContentSize(),
            text = action.symbol,
            fontSize = 40.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center
        )
    }
}