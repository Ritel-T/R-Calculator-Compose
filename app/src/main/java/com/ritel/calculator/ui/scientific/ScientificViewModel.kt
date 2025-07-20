package com.ritel.calculator.ui.scientific

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ritel.calculator.data.model.Alternate
import com.ritel.calculator.data.model.Clear
import com.ritel.calculator.data.model.Delete
import com.ritel.calculator.data.model.Dot
import com.ritel.calculator.data.model.Equals
import com.ritel.calculator.data.model.ExpressionEvaluator
import com.ritel.calculator.data.model.LeftArrow
import com.ritel.calculator.data.model.Numeric
import com.ritel.calculator.data.model.Operator
import com.ritel.calculator.data.model.RightArrow
import com.ritel.calculator.data.model.ScientificButton
import com.ritel.calculator.data.model.ScientificFunction

data class ErrorState(
    val trigger: Boolean = false, val pos: Int = 0
)

data class ScientificUiState(
    val sequence: List<String> = emptyList(),
    val expression: List<String> = emptyList(),
    val altLayout: Boolean = false,
    val cursorPos: Int = 0,
    val resultMode: Boolean = false,
    val errorState: ErrorState = ErrorState()
)

class ScientificViewModel : ViewModel() {
    var state by mutableStateOf(ScientificUiState())
        private set

    private val evaluator = ExpressionEvaluator()

    fun getButtonEnabled(@Suppress("unused") button: ScientificButton): Boolean = true

    fun onButtonClicked(button: ScientificButton) {
        when (button) {
            is Numeric, is Dot -> handleNumericAndDot(button.symbol)
            is Operator, is ScientificFunction -> handleOperatorAndFunction(button)
            is Delete -> handleDelete()
            is Clear -> reset()
            is Equals -> handleEquals()
            is Alternate -> handleAlternate()
            is LeftArrow -> handleLeftArrow()
            is RightArrow -> handleRightArrow()
        }
    }

    private fun reset() {
        state = ScientificUiState()
    }

    private fun handleAlternate() {
        state = state.copy(altLayout = !state.altLayout)
    }

    private fun handleLeftArrow() {
        state = state.copy(cursorPos = maxOf(0, state.cursorPos - 1))
    }

    private fun handleRightArrow() {
        state = state.copy(cursorPos = minOf(state.sequence.size, state.cursorPos + 1))
    }

    private fun handleNumericAndDot(symbol: String) {
        val newSequence = state.sequence.toMutableList().apply {
            if (lastOrNull()?.toDoubleOrNull() != null) {
                add(last() + symbol)
                removeAt(lastIndex - 1)
            } else {
                add(symbol)
            }
        }
        state = state.copy(sequence = newSequence)
    }

    private fun handleOperatorAndFunction(button: ScientificButton) {
        state = state.copy(
            sequence = state.sequence + button.symbol
        )
    }

    private fun handleDelete() {
        val newSequence = state.sequence.toMutableList().apply {
            if (isNotEmpty()) {
                var lastItem = last()
                if (lastItem.toDoubleOrNull() != null) { // is a number
                    removeAt(lastIndex)
                    lastItem = lastItem.dropLast(1)
                    if (lastItem.isNotEmpty()) add(lastItem)
                } else { // is an operator or function
                    removeAt(lastIndex)
                }
            } else return
        }
        state = state.copy(sequence = newSequence)
    }

    private fun handleEquals() {
        val result = evaluator.evaluate(state.sequence)
        if (result.isSuccess) {
            state = state.copy(
                expression = state.sequence,
                sequence = listOf(result.getOrNull()?.toPlainString() ?: ""),
                resultMode = true
            )
        } else {
            val error = result.exceptionOrNull()
            state = state.copy(
                errorState = ErrorState(trigger = true, pos = 0), // TODO: locate error position
                resultMode = false
            )
        }
    }
}