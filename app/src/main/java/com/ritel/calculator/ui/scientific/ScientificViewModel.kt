package com.ritel.calculator.ui.scientific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScientificUiState(
    val sequence: List<String> = emptyList(),
    val prevSeq: List<String> = emptyList(),
    val altLayout: Boolean = false,
    val cursorIndex: Int = 0, // 0 at beginning, lastIndex+1 (=size) after first token
    val selectedIndex: Int? = null, // starts from 0, null if no selection
    val resultMode: Boolean = false
)

class ScientificViewModel : ViewModel() {
    private val _state = MutableStateFlow(ScientificUiState())
    val state: StateFlow<ScientificUiState> = _state.asStateFlow()

    private val _errorTrigger = MutableSharedFlow<Long>()
    val errorTrigger = _errorTrigger.asSharedFlow()

    private val evaluator = ExpressionEvaluator()

    //region Public API

    fun setCursorIndex(index: Int) {
        _state.value =
            _state.value.copy(cursorIndex = index.coerceIn(0, _state.value.sequence.size))
    }

    fun onPrevSeqClicked() {
        restorePrev()
    }

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
    //endregion

    private val cursorIndexMod: Int
        get() = _state.value.sequence.size + 1

    private fun reset() {
        _state.value = ScientificUiState()
    }

    private fun restorePrev(toRight: Boolean = false) {
        if (_state.value.prevSeq.isEmpty()) return

        _state.value = _state.value.copy(
            sequence = _state.value.prevSeq,
            prevSeq = emptyList(),
            resultMode = false
        )
        setCursorIndex(if (toRight) _state.value.sequence.size else 0)
    }

    private fun handleAlternate() {
        _state.value = _state.value.copy(altLayout = !_state.value.altLayout)
    }

    private fun handleLeftArrow() {
        if (_state.value.resultMode) {
            restorePrev(true)
            return
        }
        setCursorIndex(((_state.value.cursorIndex - 1) + cursorIndexMod) % cursorIndexMod)
    }

    private fun handleRightArrow() {
        if (_state.value.resultMode) {
            restorePrev()
            return
        }
        setCursorIndex((_state.value.cursorIndex + 1) % cursorIndexMod)
    }

    private fun handleNumericAndDot(symbol: String) {
        if (_state.value.resultMode) reset()

        var leftIndex = _state.value.cursorIndex - 1

        val newSequence = _state.value.sequence.toMutableList().apply {
            val left = getOrNull(leftIndex)

            if (left?.toDoubleOrNull() != null) { // left is a number
                set(leftIndex, left + symbol)
            } else { // insert new number
                leftIndex++
                add(leftIndex, symbol)
            }
        }

        _state.value = _state.value.copy(sequence = newSequence)
        setCursorIndex(leftIndex + 1)
    }

    private fun handleOperatorAndFunction(button: ScientificButton) {
        val index = _state.value.cursorIndex
        val newSequence = _state.value.sequence.toMutableList().apply {
            add(index, button.symbol)
        }
        _state.value = _state.value.copy(
            sequence = newSequence, resultMode = false
        )
        setCursorIndex(index + 1)
    }

    private fun handleDelete() {
        if (_state.value.resultMode) {
            restorePrev(true)
            return
        }
        if (_state.value.sequence.isEmpty()) return

        var index = _state.value.cursorIndex - 1

        val newSequence = _state.value.sequence.toMutableList().apply {
            if (_state.value.cursorIndex == 0) return@apply // no deletion at the beginning

            val it = getOrNull(index)

            if (it?.toDoubleOrNull() != null) { // it is a number
                if (it.length > 1) {
                    set(index, it.dropLast(1))
                    index++
                } else {
                    removeAt(index)
                }
            } else { // it is not a number
                removeAt(index)
            }
        }

        _state.value = _state.value.copy(sequence = newSequence)
        setCursorIndex(index)
    }

    private fun handleEquals() {
        if (_state.value.resultMode) return

        val result = evaluator.evaluate(_state.value.sequence)

        if (result.isSuccess) {
            _state.value = _state.value.copy(
                sequence = listOf(result.value?.toPlainString() ?: ""),
                prevSeq = result.sequence,
                resultMode = true
            )
            setCursorIndex(1)
        } else {
            triggerError()
            setCursorIndex(0)
        }
    }

    private fun triggerError() {
        viewModelScope.launch {
            _errorTrigger.emit(System.currentTimeMillis())
        }
    }
}