package com.ritel.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier, viewModel: CalculatorViewModel = viewModel()) {
    val uiState = viewModel.state
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        FormulaDisplay(
            leftNumber = uiState.leftNumber,
            operator = uiState.operator,
            operatorTrigger = uiState.operatorTrigger,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
        CurrentNumberDisplay(
            currentNumber = uiState.currentNumber,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
            color = when {
                uiState.isError -> MaterialTheme.colorScheme.errorContainer
                uiState.readOnly -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = when {
                uiState.isError -> MaterialTheme.colorScheme.onErrorContainer
                uiState.readOnly -> MaterialTheme.colorScheme.onSecondary
                else -> MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
        ButtonGrid(
            getButtonEnabled = viewModel::getButtonEnabled,
            modifier = Modifier.fillMaxWidth(),
            onClick = viewModel::onAction
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    CalculatorScreen()
}

@Composable
fun FormulaDisplay(
    leftNumber: String?, operator: Operator?, operatorTrigger: Int, modifier: Modifier = Modifier
) {
    val height = 32.dp
    val numSize = 20.sp
    val opSize = 26.sp
    val animationSpec = spring<Float>(dampingRatio = 0.5f, stiffness = 200f)

    val leftNumColor = MaterialTheme.colorScheme.surfaceVariant
    val leftNumContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val opColor = MaterialTheme.colorScheme.tertiary
    val opContentColor = MaterialTheme.colorScheme.onTertiary

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = leftNumber,
            modifier = Modifier
                .height(height)
                .fillMaxWidth(0.8f),
            transitionSpec = {
                ContentTransform(
                    scaleIn(animationSpec, transformOrigin = TransformOrigin(0f, 0.5f)) + fadeIn(),
                    scaleOut(transformOrigin = TransformOrigin(0f, 0.5f)) + fadeOut(),
                    sizeTransform = SizeTransform(clip = false)
                )
            },
            label = "leftNumberAnimation"
        ) { targetNum ->
            if (targetNum != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentWidth(Alignment.Start)
                        .widthIn(min = height * 2),
                    color = leftNumColor,
                    contentColor = leftNumContentColor,
                    shape = CircleShape
                ) {
                    Text(
                        text = targetNum,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .wrapContentSize(),
                        fontSize = numSize,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            } else {
                Spacer(modifier = Modifier.fillMaxSize())
            }
        }

        val combinedState by remember(operator?.symbol, operatorTrigger) {
            derivedStateOf { Pair(operator?.symbol, operatorTrigger) }
        }

        AnimatedContent(
            targetState = combinedState, modifier = Modifier.size(height), transitionSpec = {
                (scaleIn(animationSpec) + fadeIn() togetherWith scaleOut() + fadeOut()).using(
                    SizeTransform(clip = false)
                )
            }, label = "operatorAnimation"
        ) { targetState ->
            if (targetState.first != null) {
                Surface(
                    modifier = Modifier.fillMaxSize(), // fill AnimatedContent size
                    color = opColor, contentColor = opContentColor, shape = CircleShape
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = targetState.first!!,
                        fontSize = opSize
                    )
                }
            } else {
                Spacer(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Preview
@Composable
fun AnimatedFormulaDisplayPreview() {
    var show by remember { mutableStateOf(true) }
    var long by remember { mutableStateOf(false) }

    val shortNumber = "4"
    val longNumber = "1234567123456712345671234567"
    val shortOperator = Subtract
    val longOperator = Add

    var leftNumber by remember { mutableStateOf<String?>(shortNumber) }
    var operator by remember { mutableStateOf<Operator?>(shortOperator) }
    var operatorTrigger by remember { mutableIntStateOf(0) }

    fun updateValues() {
        leftNumber = if (show) if (long) longNumber else shortNumber else null
        operator = if (show) if (long) longOperator else shortOperator else null
        operatorTrigger++
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { show = !show; updateValues() }) {
                Text(if (show) "Hide" else "Show")
            }
            Button(onClick = { long = !long; updateValues() }) {
                Text(if (long) "Short" else "Long")
            }
        }
        FormulaDisplay(leftNumber, operator, operatorTrigger, Modifier.fillMaxWidth())
    }
}

@Composable
fun CurrentNumberDisplay(
    currentNumber: String?,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val maxHeight = 180.dp
    val fontSize = 66.sp
    val lineHeight = 64.sp

    val scrollState = rememberScrollState()

    val scrollTweenSpec = tween<Float>(durationMillis = 200, easing = LinearOutSlowInEasing)
    val colorTweenSpec = tween<Color>(durationMillis = 400, easing = LinearOutSlowInEasing)
    val animationSpec = spring<IntSize>(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)

    val animatedColor by animateColorAsState(
        targetValue = color, animationSpec = colorTweenSpec
    )
    val animatedContentColor by animateColorAsState(
        targetValue = contentColor, animationSpec = colorTweenSpec
    )

    LaunchedEffect(currentNumber) {
        scrollState.animateScrollTo(0, scrollTweenSpec)
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = animatedColor,
        contentColor = animatedContentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .wrapContentHeight()
                .heightIn(max = maxHeight)
                .verticalScroll(scrollState, reverseScrolling = true),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .animateContentSize(animationSpec)
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.Top),
                    text = currentNumber ?: " ",
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrentNumberDisplayPreview() {
    Column {
        CurrentNumberDisplay("3.1415926535898", Modifier.fillMaxWidth())
        CurrentNumberDisplay(
            "3.141592653589793238462643383279502884197169",
            Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
        CurrentNumberDisplay(
            "Error",
            Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
