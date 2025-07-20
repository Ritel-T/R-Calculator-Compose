package com.ritel.calculator.ui.scientific

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ritel.calculator.data.model.Alternate
import com.ritel.calculator.data.model.Clear
import com.ritel.calculator.data.model.Delete
import com.ritel.calculator.data.model.Equals
import com.ritel.calculator.data.model.ExpressionEvaluator.evaluate
import com.ritel.calculator.data.model.LeftArrow
import com.ritel.calculator.data.model.RightArrow
import com.ritel.calculator.data.model.ScientificButton
import java.math.MathContext


data class ScientificUiState(
    val expression: String? = null, var input: String = "", var altLayout: Boolean = false
)

class ScientificViewModel : ViewModel() {
    var state by mutableStateOf(ScientificUiState())
        private set
    private val mathContext = MathContext.DECIMAL128

    fun getButtonEnabled(action: ScientificButton): Boolean = true

    fun onButtonClicked(button: ScientificButton) {
        when (button) {
            is Alternate -> {
                state = state.copy(altLayout = !state.altLayout)
            }

            is Clear -> {
                reset()
            }

            is Delete -> {
                if (state.input.isNotEmpty()) {
                    state = state.copy(input = state.input.dropLast(1))
                }
            }

            is Equals -> {
                val result = evaluate(state.input)
                if (result.isFailure) return
                state = state.copy(expression = state.input, input = result.getOrNull()
                    ?.toString() ?: "")
            }

            is LeftArrow -> {
                // Handle left arrow logic, e.g., moving cursor or selection
                // This is a placeholder as the actual behavior depends on the UI implementation
            }

            is RightArrow -> {
                // Handle right arrow logic, e.g., moving cursor or selection
                // This is a placeholder as the actual behavior depends on the UI implementation
            }

            else -> {
                state = state.copy(input = state.input + button.symbol)
            }
        }
    }

    fun reset() {
        state = ScientificUiState()
    }
}