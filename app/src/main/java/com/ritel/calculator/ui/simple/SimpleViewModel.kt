package com.ritel.calculator.ui.simple

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ritel.calculator.data.model.Add
import com.ritel.calculator.data.model.Clear
import com.ritel.calculator.data.model.Delete
import com.ritel.calculator.data.model.Divide
import com.ritel.calculator.data.model.Dot
import com.ritel.calculator.data.model.Equals
import com.ritel.calculator.data.model.Multiply
import com.ritel.calculator.data.model.Numeric
import com.ritel.calculator.data.model.Operator
import com.ritel.calculator.data.model.Percent
import com.ritel.calculator.data.model.PlusMinus
import com.ritel.calculator.data.model.SimpleButton
import com.ritel.calculator.data.model.SimpleFunction
import com.ritel.calculator.data.model.Subtract
import java.math.BigDecimal
import java.math.MathContext

data class SimpleUiState(
    val leftNumber: String? = null,
    val currentNumber: String? = null,
    val operator: Operator? = null,
    val readOnly: Boolean = false,
    val isError: Boolean = false,
    val operatorTrigger: Int = 0
)

class SimpleViewModel : ViewModel() {
    var state by mutableStateOf(SimpleUiState())
        private set
    private val errorText = "Error"
    private val mathContext = MathContext.DECIMAL128

    fun getButtonEnabled(action: SimpleButton): Boolean {
        return when (action) {
            is Dot -> state.currentNumber?.contains('.') != true
            is Operator -> !state.isError
            is Delete -> state.currentNumber != null || state.leftNumber != null
            is SimpleFunction -> state.currentNumber != null && !state.isError // exclude Delete
            is Equals -> !state.isError && !state.readOnly && state.leftNumber != null
            else -> true
        }
    }

    fun onButtonClicked(button: SimpleButton) {
        when (button) {
            is Numeric -> enterDigit(button.symbol)
            is Dot -> enterDot()
            is Delete -> handleDelete()
            is Operator -> handleOperatorAction(button)
            is SimpleFunction -> handleFunctionAction(button)
            is Clear -> reset()
            is Equals -> handleEquals()
        }
    }

    private fun reset() {
        state = SimpleUiState()
    }

    private fun enterDigit(symbol: String) {
        if (state.readOnly) reset()
        state = state.copy(
            currentNumber = state.currentNumber?.takeIf { it != "0" }?.plus(symbol) ?: symbol
        )
    }

    private fun enterDot() {
        if (state.readOnly) reset()
        state = state.copy(currentNumber = state.currentNumber.takeIf { it?.contains('.') == true }
            ?: ((state.currentNumber ?: "0") + "."))
    }

    private fun handleDelete() {
        when {
            state.readOnly || state.isError -> reset()

            state.currentNumber == null -> {
                state = state.copy(
                    operator = null, currentNumber = state.leftNumber, leftNumber = null
                )
            }

            else -> state = state.copy(
                currentNumber = state.currentNumber!!.dropLast(1).ifEmpty { null })
        }
    }

    private fun handleOperatorAction(action: Operator) {
        if (state.isError) return

        if (state.readOnly) state = state.copy(readOnly = false)

        val (newLeft, newCurrent, newOperator) = when {
            state.leftNumber == null && state.currentNumber == null -> Triple(
                "0", null, action
            )

            state.leftNumber == null -> Triple(
                state.currentNumber, null, action
            )

            state.currentNumber == null -> Triple(
                state.leftNumber, null, action
            )

            else -> {
                val result = calculate(
                    state.leftNumber, state.currentNumber, state.operator
                )
                if (result == errorText) {
                    state = state.copy(isError = true, readOnly = true)
                    Triple(
                        null, errorText, null
                    )
                } else Triple(
                    result, null, action
                )
            }
        }

        state = state.copy(
            leftNumber = newLeft,
            currentNumber = newCurrent,
            operator = newOperator,
            operatorTrigger = state.operatorTrigger + 1
        )
    }

    private fun handleFunctionAction(action: SimpleFunction) {
        when (action) {
            is PlusMinus -> {
                state.currentNumber?.let {
                    if (it != "0") {
                        state = state.copy(
                            currentNumber = if (it.startsWith("-")) {
                            it.removePrefix("-").ifEmpty { null }
                        } else {
                            "-$it"
                        })
                    }
                }
            }

            is Percent -> {
                state = state.copy(
                    currentNumber = state.currentNumber?.toBigDecimalOrNull()?.divide(
                        BigDecimal.valueOf(100), mathContext
                    )?.stripTrailingZeros()?.toString()
                )
            }
        }
    }

    private fun handleEquals() {
        val result = calculate(state.leftNumber, state.currentNumber, state.operator)
        if (result == errorText) state = state.copy(isError = true)

        state = state.copy(
            currentNumber = result, leftNumber = null, operator = null, readOnly = true
        )
    }

    private fun calculate(
        leftNumber: String?, rightNumber: String?, operator: Operator?
    ): String? {
        val numL = leftNumber?.toBigDecimalOrNull() ?: return rightNumber
        val numR = rightNumber?.toBigDecimalOrNull() ?: return leftNumber
        return operator?.let {
            when (it) {
                Add -> numL.add(numR, mathContext)
                Subtract -> numL.subtract(numR, mathContext)
                Multiply -> numL.multiply(numR, mathContext)
                Divide -> {
                    if (numR == BigDecimal.ZERO) {
                        return errorText
                    }
                    numL.divide(numR, mathContext)
                }
            }?.stripTrailingZeros()?.toPlainString()
        }
    }
}