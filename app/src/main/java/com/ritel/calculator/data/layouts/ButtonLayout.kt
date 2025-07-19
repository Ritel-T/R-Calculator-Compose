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
import com.ritel.calculator.ui.components.MorphButton
import com.ritel.calculator.data.model.Add
import com.ritel.calculator.data.model.ButtonAction
import com.ritel.calculator.data.model.Clear
import com.ritel.calculator.data.model.Delete
import com.ritel.calculator.data.model.Divide
import com.ritel.calculator.data.model.Dot
import com.ritel.calculator.data.model.Equals
import com.ritel.calculator.data.model.Multiply
import com.ritel.calculator.data.model.Numeric
import com.ritel.calculator.data.model.Percent
import com.ritel.calculator.data.model.PlusMinus
import com.ritel.calculator.data.model.Subtract
import com.ritel.calculator.ui.components.getButtonStyle

val buttonActions = listOf(
    listOf(Clear, PlusMinus, Percent, Divide),
    listOf(Numeric(7), Numeric(8), Numeric(9), Multiply),
    listOf(Numeric(4), Numeric(5), Numeric(6), Subtract),
    listOf(Numeric(1), Numeric(2), Numeric(3), Add),
    listOf(Numeric(0), Dot, Delete, Equals)
)

@Composable
@SuppressLint("UnusedBoxWithConstraintsScope")
fun ButtonGrid(
    getButtonEnabled: (ButtonAction) -> Boolean,
    modifier: Modifier = Modifier,
    onClick: (ButtonAction) -> Unit
) {
    val space = (-16).dp
    val colNumber = 4
    val fontSizeFactor = 0.45f

    BoxWithConstraints(modifier = modifier) {
        val buttonSize = (maxWidth + (colNumber - 1) * space) / colNumber
        val fontSize = (buttonSize.value * fontSizeFactor).sp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space)
        ) {
            buttonActions.forEach { row ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space),
                ) {
                    row.forEach { action ->
                        CalculatorButton(
                            action = action,
                            enabled = getButtonEnabled(action),
                            fontSize = fontSize,
                            modifier = Modifier.weight(1f)
                        ) { onClick(action) }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    action: ButtonAction,
    enabled: Boolean,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val view = LocalView.current

    val style = getButtonStyle(action)
    MorphButton(
        morphShape = style.morphShape,
        modifier = modifier.aspectRatio(1f),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.color, contentColor = style.onColor
        ),
        contentPadding = PaddingValues(16.dp),
        onClick = {
            view.performHapticFeedback(
                if (action is Clear || action is Equals) {
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
            text = action.symbol,
            fontSize = fontSize,
            fontWeight = FontWeight.Light
        )
    }
}