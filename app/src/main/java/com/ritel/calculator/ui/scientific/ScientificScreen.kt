package com.ritel.calculator.ui.scientific

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.ritel.calculator.data.model.Clear
import com.ritel.calculator.data.model.Delete
import com.ritel.calculator.data.model.Equals
import com.ritel.calculator.ui.theme.JetBrainsMono

@Composable
fun ScientificScreen(modifier: Modifier = Modifier, viewModel: ScientificViewModel = viewModel()) {
    val uiState by viewModel.state.collectAsState()
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        ScientificPreviousExpression(
            prevSeq = uiState.prevSeq,
            onClick = viewModel::onPrevSeqClicked,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .height(32.dp)
        )
        ScientificInputField(
            sequence = uiState.sequence,
            cursorIndex = uiState.cursorIndex,
            setCursorIndex = viewModel::setCursorIndex,
            resultMode = uiState.resultMode,
            errorTrigger = uiState.errorTrigger,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 16.dp)
                .height(48.dp)
        )
        ButtonGrid(
            buttonLayout = if (uiState.altLayout) {
                ScientificLayoutAlternative
            } else {
                ScientificLayoutDefault
            }, getButtonEnabled = { button ->
                when (button) {
                    Delete, Clear -> uiState.sequence.isNotEmpty() || uiState.prevSeq.isNotEmpty()
                    Equals -> uiState.sequence.isNotEmpty() && !uiState.resultMode
                    else -> true
                }
            }, modifier = Modifier.fillMaxWidth(), onClick = viewModel::onButtonClicked
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontSize = 16.sp

    if (prevSeq.isNotEmpty()) {
        Surface(
            modifier = modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onClick),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            SequenceDisplay(
                sequence = prevSeq,
                fontSize = fontSize,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    } else Spacer(modifier)
}

@Preview
@Composable
fun ScientificPreviousExpressionPreview() {
    val sequence = listOf("sin", "45", "°", "+", "12345")
    ScientificPreviousExpression(
        prevSeq = sequence,
        onClick = {},
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
    resultMode: Boolean,
    errorTrigger: Int,
    modifier: Modifier = Modifier,
) {
    val fontSize = 32.sp

    val offsetX = remember { Animatable(0f) }

    val color = if (resultMode) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (resultMode) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val colorTweenSpec = tween<Color>(durationMillis = 400, easing = FastOutSlowInEasing)

    val animatedColor by animateColorAsState(
        targetValue = color, animationSpec = colorTweenSpec
    )
    val animatedContentColor by animateColorAsState(
        targetValue = contentColor, animationSpec = colorTweenSpec
    )


    LaunchedEffect(errorTrigger) {
        offsetX.animateTo(
            targetValue = 10f,
            animationSpec = tween(durationMillis = 50, easing = FastOutLinearInEasing)
        )
        offsetX.animateTo(
            targetValue = 0f, animationSpec = spring(
                dampingRatio = 0.1f, stiffness = Spring.StiffnessHigh
            )
        )
    }

    Surface(
        modifier = modifier
            .offset(x = offsetX.value.dp)
            .clip(MaterialTheme.shapes.medium),
        color = animatedColor,
        contentColor = animatedContentColor
    ) {
        SequenceWithCursorDisplay(
            sequence = sequence,
            cursorIndex = cursorIndex,
            setCursorIndex = setCursorIndex,
            resultMode = resultMode,
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
        resultMode = false,
        errorTrigger = 0,
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
            .padding(horizontal = 16.dp),
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
    resultMode: Boolean,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutLinearInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "cursorAlpha"
    )

    val cursorSize = DpSize(1.5.dp, fontSize.value.dp)

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            (-1).dp, if (resultMode) Alignment.End else Alignment.Start
        )
    ) {
        sequence.forEachIndexed { index, text ->

            if (cursorIndex == index) Cursor(cursorAlpha, cursorSize)
            else Spacer(Modifier.size(cursorSize))

            Text(
                text = text,
                modifier = Modifier
                    .clickable {
                        setCursorIndex(index + 1)
                    }
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        color = if (cursorIndex - 1 == index && !resultMode) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        } else Color.Transparent
                    )
                    .padding(horizontal = 4.dp)
                    .padding(bottom = 4.dp)
                    .wrapContentSize(),
                fontSize = fontSize,
                fontFamily = JetBrainsMono,
                letterSpacing = (-2).sp,
            )
        }

        if (cursorIndex == sequence.size && !resultMode) Cursor(cursorAlpha, cursorSize)
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