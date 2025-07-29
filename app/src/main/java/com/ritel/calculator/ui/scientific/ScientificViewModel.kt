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

data class ScientificUiState(
    val sequence: List<String> = emptyList(),
    val prevSeq: List<String> = emptyList(),
    val altLayout: Boolean = false,
    val cursorIndex: Int = 0, // 0 at beginning, lastIndex+1 (=size) after first token
    val selectedIndex: Int? = null, // starts from 0, null if no selection
    val resultMode: Boolean = false,
    val errorTrigger: Int = 0
)

class ScientificViewModel : ViewModel() {
    var state by mutableStateOf(ScientificUiState())
        private set

    private val evaluator = ExpressionEvaluator()

    //region Public API

    fun setCursorIndex(index: Int) {
        state = state.copy(cursorIndex = index.coerceIn(0, state.sequence.size))
    }

    fun onPrevSeqClicked() {
        restorePrev()
    }

    fun getButtonEnabled(button: ScientificButton): Boolean {
        return when (button) {
            is Delete, is Clear -> return state.sequence.isNotEmpty() || state.prevSeq.isNotEmpty()
            is Equals -> return state.sequence.isNotEmpty() && !state.resultMode
            else -> true
        }
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
        get() = state.sequence.size + 1

    private fun reset() {
        state = ScientificUiState()
    }

    private fun restorePrev(toRight: Boolean = false) {
        if (state.prevSeq.isEmpty()) return

        state = state.copy(
            sequence = state.prevSeq,
            prevSeq = emptyList(),
            cursorIndex = if (toRight) state.prevSeq.size else 0,
            resultMode = false
        )
    }

    private fun handleAlternate() {
        state = state.copy(altLayout = !state.altLayout)
    }

    private fun handleLeftArrow() {
        if (state.resultMode) {
            restorePrev(true)
            return
        }
        state =
            state.copy(cursorIndex = ((state.cursorIndex - 1) + cursorIndexMod) % cursorIndexMod)
    }

    private fun handleRightArrow() {
        if (state.resultMode) {
            restorePrev()
            return
        }
        state = state.copy(cursorIndex = (state.cursorIndex + 1) % cursorIndexMod)
    }

    private fun handleNumericAndDot(symbol: String) {
        if (state.resultMode) reset()

        var leftIndex = state.cursorIndex - 1

        val newSequence = state.sequence.toMutableList().apply {
            val left = getOrNull(leftIndex)

            if (left?.toDoubleOrNull() != null) { // left is a number
                set(leftIndex, left + symbol)
            } else { // insert new number
                leftIndex++
                add(leftIndex, symbol)
            }
        }

        state = state.copy(sequence = newSequence, cursorIndex = leftIndex + 1)
    }

    private fun handleOperatorAndFunction(button: ScientificButton) {
        val newSequence = state.sequence.toMutableList().apply {
            add(state.cursorIndex, button.symbol)
        }
        state = state.copy(
            sequence = newSequence, cursorIndex = state.cursorIndex + 1, resultMode = false
        )
    }

    private fun handleDelete() {
        if (state.resultMode) {
            restorePrev(true)
            return
        }
        if (state.sequence.isEmpty()) return

        var index = state.cursorIndex - 1

        val newSequence = state.sequence.toMutableList().apply {
            if (state.cursorIndex == 0) return@apply // no deletion at the beginning

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

        state = state.copy(sequence = newSequence, cursorIndex = index)
    }

    private fun handleEquals() {
        if (state.resultMode) return

        val result = evaluator.evaluate(state.sequence)

        state = if (result.isSuccess) {
            state.copy(
                sequence = listOf(result.value?.toPlainString() ?: ""),
                prevSeq = result.sequence,
                cursorIndex = 1,
                resultMode = true
            )
        } else {
            state.copy(
                cursorIndex = 0, errorTrigger = state.errorTrigger + 1
            )
        }
    }
}