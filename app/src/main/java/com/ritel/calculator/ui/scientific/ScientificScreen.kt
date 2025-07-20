package com.ritel.calculator.ui.scientific

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ritel.calculator.data.layouts.ButtonGrid
import com.ritel.calculator.data.layouts.ScientificLayoutAlternative
import com.ritel.calculator.data.layouts.ScientificLayoutDefault

@Composable
fun ScientificScreen(modifier: Modifier = Modifier, viewModel: ScientificViewModel = viewModel()) {
    val uiState = viewModel.state
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        ScientificExpressionDisplay(
            expression = uiState.expression,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
        ScientificInputField(
            sequence = uiState.sequence,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
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
fun ScientificExpressionDisplay(
    expression: List<String>,
    modifier: Modifier = Modifier,
) {
    val fontSize = 16.sp

    val color = MaterialTheme.colorScheme.surfaceVariant
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant

    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .wrapContentHeight()
                .horizontalScroll(scrollState, reverseScrolling = true),
            horizontalArrangement = Arrangement.End
        ) {
            expression.forEach { token ->
                Text(
                    modifier = Modifier.wrapContentSize(),
                    text = token,
                    fontSize = fontSize,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ScientificInputField(
    sequence: List<String>,
    modifier: Modifier = Modifier,
) {
    val fontSize = 32.sp

    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .wrapContentHeight()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.End
        ) {
            sequence.forEach { token ->
                Text(
                    modifier = Modifier.wrapContentSize(),
                    text = token,
                    fontSize = fontSize,
                    maxLines = 1
                )
            }
        }
    }
}