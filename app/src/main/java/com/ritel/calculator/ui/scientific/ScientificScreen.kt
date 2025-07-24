package com.ritel.calculator.ui.scientific

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ritel.calculator.data.layouts.ButtonGrid
import com.ritel.calculator.data.layouts.ScientificLayoutAlternative
import com.ritel.calculator.data.layouts.ScientificLayoutDefault
import com.ritel.calculator.ui.theme.JetBrainsMono

@Composable
fun ScientificScreen(modifier: Modifier = Modifier, viewModel: ScientificViewModel = viewModel()) {
    val uiState = viewModel.state
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        ScientificPreviousExpression(
            prevSeq = uiState.prevSeq,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .height(32.dp)
        )
        ScientificInputField(
            sequence = uiState.sequence,
            cursorIndex = uiState.cursorIndex,
            setCursorIndex = viewModel::setCursorIndex,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 16.dp)
                .height(48.dp)
        )
        ButtonGrid(
            buttonLayout = if (uiState.altLayout) ScientificLayoutAlternative
            else ScientificLayoutDefault,
            getButtonEnabled = viewModel::getButtonEnabled,
            modifier = Modifier.fillMaxWidth(),
            onClick = viewModel::onButtonClicked
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScientificScreenPreview() {
    ScientificScreen()
}

@Composable
fun ScientificPreviousExpression(
    prevSeq: List<String>,
    modifier: Modifier = Modifier,
) {
    val fontSize = 16.sp

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        SequenceDisplay(
            sequence = prevSeq,
            fontSize = fontSize,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .fillMaxSize()
        )
    }
}

@Preview
@Composable
fun ScientificPreviousExpressionPreview() {
    val sequence = listOf("sin", "45", "°", "+", "12345")
    ScientificPreviousExpression(
        prevSeq = sequence,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(32.dp)
    )
}

@Composable
fun ScientificInputField(
    sequence: List<String>,
    cursorIndex: Int,
    setCursorIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontSize = 32.sp

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        SequenceWithCursorDisplay(
            sequence = sequence,
            cursorIndex = cursorIndex,
            setCursorIndex = setCursorIndex,
            fontSize = fontSize,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxSize()
        )
    }
}

@Preview
@Composable
fun ScientificInputFieldPreview() {
    val sequence = listOf("sin", "45", "°", "+", "12345")
    ScientificInputField(
        sequence = sequence,
        cursorIndex = 1,
        setCursorIndex = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(48.dp)
    )
}

@Composable
fun SequenceDisplay(
    sequence: List<String>,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
    ) {
        sequence.forEach { token ->
            Text(
                text = token,
                modifier = Modifier.wrapContentSize(),
                fontSize = fontSize,
                fontFamily = JetBrainsMono,
                letterSpacing = (-0.5).sp,
            )
        }
    }
}

@Composable
fun SequenceWithCursorDisplay(
    sequence: List<String>,
    cursorIndex: Int,
    setCursorIndex: (Int) -> Unit,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, // 从 1f 开始，让光标一开始就可见
        targetValue = 0f,  // 动画到 0f
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutLinearInEasing),
            // 让动画可以反向播放，实现 1 -> 0 -> 1 的闪烁效果
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "cursorAlpha"
    )

    val cursorSize = DpSize(1.5.dp, fontSize.value.dp)

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((-1).dp)
    ) {
        sequence.forEachIndexed { index, text ->

            if (cursorIndex == index) Cursor(cursorAlpha, cursorSize)
            else Spacer(Modifier.size(cursorSize))

            Text(
                text = text,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        color = if (cursorIndex - 1 == index) MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.3f
                        )
                        else Color.Transparent
                    )
                    .clickable {
                        setCursorIndex(index)
                    }
                    .padding(horizontal = 4.dp)
                    .padding(bottom = 4.dp)
                    .wrapContentSize(),
                fontSize = fontSize,
                fontFamily = JetBrainsMono,
                letterSpacing = (-2).sp,
            )
        }

        if (cursorIndex == sequence.size) Cursor(cursorAlpha, cursorSize)
        else Spacer(Modifier.size(cursorSize))
    }
}

@Composable
fun Cursor(alpha: Float, size: DpSize) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(MaterialTheme.shapes.large)
            .background(LocalContentColor.current.copy(alpha = alpha))
    )
}