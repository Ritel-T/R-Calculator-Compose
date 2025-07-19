package com.ritel.calculator.ui.scientific

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ritel.calculator.data.model.ScientificButton
import java.math.MathContext


data class ScientificUiState(
    val expression: String? = null,
    var input: String = ""
)
class ScientificViewModel : ViewModel() {
    var state by mutableStateOf(ScientificUiState())
        private set
    private val mathContext: MathContext = MathContext.DECIMAL128

    fun getButtonEnabled(action: ScientificButton): Boolean = true

    fun onButtonClicked(button: ScientificButton){
        state = state.copy(input = state.input + button.symbol)
    }

    fun reset() {
        state = ScientificUiState()
    }
}