package com.ritel.calculator.data.layouts

import android.annotation.SuppressLint
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.ritel.calculator.data.model.AbsoluteValue
import com.ritel.calculator.data.model.Add
import com.ritel.calculator.data.model.Alternate
import com.ritel.calculator.data.model.ArHyperCosine
import com.ritel.calculator.data.model.ArHyperSine
import com.ritel.calculator.data.model.ArHyperTangent
import com.ritel.calculator.data.model.ArcCosine
import com.ritel.calculator.data.model.ArcSine
import com.ritel.calculator.data.model.ArcTangent
import com.ritel.calculator.data.model.CalculatorButton
import com.ritel.calculator.data.model.Clear
import com.ritel.calculator.data.model.CommonLog
import com.ritel.calculator.data.model.Cosine
import com.ritel.calculator.data.model.CubeRoot
import com.ritel.calculator.data.model.Degrees
import com.ritel.calculator.data.model.Delete
import com.ritel.calculator.data.model.Divide
import com.ritel.calculator.data.model.Dot
import com.ritel.calculator.data.model.Equals
import com.ritel.calculator.data.model.EulersNumber
import com.ritel.calculator.data.model.Factorial
import com.ritel.calculator.data.model.HyperCosine
import com.ritel.calculator.data.model.HyperSine
import com.ritel.calculator.data.model.HyperTangent
import com.ritel.calculator.data.model.LeftArrow
import com.ritel.calculator.data.model.LeftParen
import com.ritel.calculator.data.model.Multiply
import com.ritel.calculator.data.model.NaturalLog
import com.ritel.calculator.data.model.Numeric
import com.ritel.calculator.data.model.Percent
import com.ritel.calculator.data.model.Pi
import com.ritel.calculator.data.model.PlusMinus
import com.ritel.calculator.data.model.Power
import com.ritel.calculator.data.model.RadToDeg
import com.ritel.calculator.data.model.RightArrow
import com.ritel.calculator.data.model.RightParen
import com.ritel.calculator.data.model.ScientificButton
import com.ritel.calculator.data.model.SimpleButton
import com.ritel.calculator.data.model.Sine
import com.ritel.calculator.data.model.SquareRoot
import com.ritel.calculator.data.model.Subtract
import com.ritel.calculator.data.model.Tangent
import com.ritel.calculator.ui.components.MorphButton
import com.ritel.calculator.ui.components.getButtonStyle

sealed class ButtonLayout<T : CalculatorButton>(
    val rows: Int, val cols: Int, val layout: List<List<T>>
)

object SimpleLayout : ButtonLayout<SimpleButton>(
    5, 4, listOf(
        listOf(Clear, PlusMinus, Percent, Divide),
        listOf(Numeric(7), Numeric(8), Numeric(9), Multiply),
        listOf(Numeric(4), Numeric(5), Numeric(6), Subtract),
        listOf(Numeric(1), Numeric(2), Numeric(3), Add),
        listOf(Dot, Numeric(0), Delete, Equals)
    )
)

sealed class ScientificLayout(rows: Int, cols: Int, layout: List<List<ScientificButton>>) :
    ButtonLayout<ScientificButton>(rows, cols, layout)

object ScientificLayoutDefault : ScientificLayout(
    7, 5, listOf(
        listOf(Clear,        LeftArrow,         LeftParen,        RightParen,      RightArrow),
        listOf(Alternate, HyperSine, HyperCosine, HyperTangent,      Degrees),
        listOf(AbsoluteValue,Sine,              Cosine,           Tangent,          Divide),
        listOf(Pi,           Numeric(7), Numeric(8), Numeric(9), Multiply),
        listOf(CommonLog,    Numeric(4), Numeric(5), Numeric(6), Subtract),
        listOf(Power,        Numeric(1), Numeric(2), Numeric(3),Add),
        listOf(SquareRoot,   Dot,               Numeric(0), Delete,           Equals)
    )
)
object ScientificLayoutAlternative : ScientificLayout(
    7, 5, listOf(
        listOf(Clear,        LeftArrow,         LeftParen,        RightParen,       RightArrow),
        listOf(Alternate, ArHyperSine, ArHyperCosine, ArHyperTangent,   RadToDeg),
        listOf(AbsoluteValue,ArcSine,          ArcCosine,         ArcTangent,        Divide),
        listOf(EulersNumber, Numeric(7), Numeric(8), Numeric(9), Multiply),
        listOf(NaturalLog,   Numeric(4), Numeric(5), Numeric(6), Subtract),
        listOf(Factorial,    Numeric(1), Numeric(2), Numeric(3), Add),
        listOf(CubeRoot,     Dot,              Numeric(0),  Delete,           Equals)
    )
)


@Composable
@SuppressLint("UnusedBoxWithConstraintsScope")
fun <T : CalculatorButton> ButtonGrid(
    buttonLayout: ButtonLayout<T>,
    getButtonEnabled: (T) -> Boolean,
    modifier: Modifier = Modifier,
    onClick: (T) -> Unit
) {
    val cols = buttonLayout.cols
    val (aspectRatio, fontSizeFactor) = when (buttonLayout) {
        is SimpleLayout -> 1f to 0.45f
        is ScientificLayout -> 1f to 0.28f
    }
    val (rowSpace, colSpace) = when (buttonLayout) {
        is SimpleLayout -> (-16).dp to (-16).dp
        is ScientificLayout -> (-14).dp to (-14).dp
    }

    BoxWithConstraints(modifier = modifier) {
        val buttonSize = (maxWidth + (cols - 1) * rowSpace) / cols
        val fontSize = (buttonSize.value * fontSizeFactor).sp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(rowSpace)
        ) {
            buttonLayout.layout.forEach { row ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(colSpace),
                ) {
                    row.forEach { button ->
                        CalculatorButton(
                            button = button,
                            enabled = getButtonEnabled(button),
                            modifier = Modifier.weight(1f),
                            aspectRatio = aspectRatio,
                            fontSize = fontSize
                        ) { onClick(button) }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    button: CalculatorButton,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1f,
    fontSize: TextUnit = 32.sp,
    onClick: () -> Unit
) {
    val view = LocalView.current

    val style = getButtonStyle(button)
    MorphButton(
        morphShape = style.morphShape,
        modifier = modifier.aspectRatio(aspectRatio),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.color, contentColor = style.onColor
        ),
        contentPadding = PaddingValues(16.dp),
        onClick = {
            view.performHapticFeedback(
                if (button is Clear || button is Equals) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        HapticFeedbackConstants.REJECT
                    } else {
                        HapticFeedbackConstants.CONTEXT_CLICK
                    }
                } else {
                    HapticFeedbackConstants.KEYBOARD_TAP
                }
            )
            onClick()
        }) {
        Text(
            modifier = Modifier.wrapContentSize(),
            text = button.symbol,
            fontSize = fontSize,
            fontWeight = FontWeight.Light
        )
    }
}