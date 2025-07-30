package com.ritel.calculator.ui.simple

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _state = MutableStateFlow(SimpleUiState())
    val state: StateFlow<SimpleUiState> = _state.asStateFlow()
    private val errorText = "Error"
    private val mathContext = MathContext.DECIMAL128

    fun onButtonClicked(button: SimpleButton) {
        when (button) {
            is Numeric -> enterDigit(button.symbol)
            is Dot -> enterDot()
            is Operator -> handleOperator(button)
            is SimpleFunction -> handleFunction(button)
            is Delete -> handleDelete()
            is Clear -> reset()
            is Equals -> handleEquals()
        }
    }

    private fun reset() {
        _state.value = SimpleUiState()
    }

    private fun enterDigit(symbol: String) {
        if (_state.value.readOnly) reset()
        _state.value = _state.value.copy(
            currentNumber = _state.value.currentNumber?.takeIf { it != "0" }?.plus(symbol) ?: symbol
        )
    }

    private fun enterDot() {
        if (_state.value.readOnly) reset()
        _state.value = _state.value.copy(currentNumber = _state.value.currentNumber.takeIf { it?.contains('.') == true }
            ?: ((_state.value.currentNumber ?: "0") + "."))
    }

    private fun handleOperator(button: Operator) {
        if (_state.value.isError) return

        if (_state.value.readOnly) _state.value = _state.value.copy(readOnly = false)

        val (newLeft, newCurrent, newOperator) = when {
            _state.value.leftNumber == null && _state.value.currentNumber == null -> Triple(
                "0", null, button
            )

            _state.value.leftNumber == null -> Triple(
                _state.value.currentNumber, null, button
            )

            _state.value.currentNumber == null -> Triple(
                _state.value.leftNumber, null, button
            )

            else -> {
                val result = calculate(
                    _state.value.leftNumber, _state.value.currentNumber, _state.value.operator
                )
                if (result == errorText) {
                    _state.value = _state.value.copy(isError = true, readOnly = true)
                    Triple(
                        null, errorText, null
                    )
                } else Triple(
                    result, null, button
                )
            }
        }

        _state.value = _state.value.copy(
            leftNumber = newLeft,
            currentNumber = newCurrent,
            operator = newOperator,
            operatorTrigger = _state.value.operatorTrigger + 1
        )
    }

    private fun handleFunction(button: SimpleFunction) {
        when (button) {
            is PlusMinus -> {
                _state.value.currentNumber?.let {
                    if (it != "0") {
                        _state.value = _state.value.copy(
                            currentNumber = if (it.startsWith("-")) {
                            it.removePrefix("-").ifEmpty { null }
                        } else {
                            "-$it"
                        })
                    }
                }
            }

            is Percent -> {
                _state.value = _state.value.copy(
                    currentNumber = _state.value.currentNumber?.toBigDecimalOrNull()?.divide(
                        BigDecimal.valueOf(100), mathContext
                    )?.stripTrailingZeros()?.toString()
                )
            }
        }
    }

    private fun handleDelete() {
        when {
            _state.value.readOnly || _state.value.isError -> reset()

            _state.value.currentNumber == null -> {
                _state.value = _state.value.copy(
                    operator = null, currentNumber = _state.value.leftNumber, leftNumber = null
                )
            }

            else -> _state.value = _state.value.copy(
                currentNumber = _state.value.currentNumber!!.dropLast(1).ifEmpty { null })
        }
    }

    private fun handleEquals() {
        val result = calculate(_state.value.leftNumber, _state.value.currentNumber, _state.value.operator)
        if (result == errorText) _state.value = _state.value.copy(isError = true)

        _state.value = _state.value.copy(
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