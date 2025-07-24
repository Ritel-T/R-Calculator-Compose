package com.ritel.calculator.data.model

import ch.obermuhlner.math.big.BigDecimalMath
import java.math.BigDecimal
import java.math.MathContext
import java.util.Stack

class ExpressionEvaluator {
    private val mathContext = MathContext.DECIMAL128

    data class EvaluationResult(
        val isSuccess: Boolean,
        val value: BigDecimal? = null,
        val sequence: List<String> = emptyList(),
    )

    //region Public API
    fun evaluate(sequence: List<String>): EvaluationResult {
        if (sequence.isEmpty()) {
            return EvaluationResult(
                isSuccess = true,
                sequence = emptyList(),
                value = BigDecimal.ZERO // Return Zero for empty input
            )
        }

        try {
            // The order of preprocessing is crucial for correctness.
            val parenthesizedSequence = autoParenthesizeFunctionArgs(sequence)
            val correctedSequence = autoCorrectParentheses(parenthesizedSequence)
            val preprocessed = preprocess(correctedSequence)
            val postfix = infixToPostfix(preprocessed)
            val result = evaluatePostfix(postfix)

            return EvaluationResult(
                isSuccess = true,
                sequence = preprocessed, // Return the fully processed sequence for display
                value = result.stripTrailingZeros()
            )
        } catch (_: Exception) {
            // In case of any error, return a failure result. The message can be logged or displayed.
            // For simplicity, we don't pass the message, but the calling ViewModel could log e.message.
            return EvaluationResult(
                isSuccess = false, sequence = sequence // Return original sequence on failure
            )
        }
    }
    //endregion

    //region Preprocessing, Correction, and Auto-Parenthesizing

    private fun autoParenthesizeFunctionArgs(sequence: List<String>): List<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i < sequence.size) {
            val token = sequence[i]
            if (token in unaryPrefixOperators && (i + 1 >= sequence.size || sequence[i + 1] != "(")) {
                result.add(token)
                result.add("(")
                i++
                val argStartIndex = i
                while (i < sequence.size) {
                    val argToken = sequence[i]
                    val isHighPrecedenceComponent =
                        argToken.isNumeric() || argToken in constants || argToken == "^"
                    if (!isHighPrecedenceComponent) {
                        val isNestedFunc = argToken in unaryPrefixOperators && (i == argStartIndex)
                        if (!isNestedFunc) break
                    }
                    i++
                }
                val argEndIndex = i
                result.addAll(sequence.subList(argStartIndex, argEndIndex))
                result.add(")")
            } else {
                result.add(token)
                i++
            }
        }
        return result
    }

    private fun autoCorrectParentheses(sequence: List<String>): List<String> {
        val openParens = sequence.count { it == "(" }
        val closeParens = sequence.count { it == ")" }
        val balance = openParens - closeParens

        return when {
            balance > 0 -> {
                val mutableSequence = sequence.toMutableList()
                repeat(balance) { mutableSequence.add(")") }
                mutableSequence
            }

            balance < 0 -> {
                val mutableSequence = sequence.toMutableList()
                repeat(-balance) { mutableSequence.add(0, "(") }
                mutableSequence
            }

            else -> sequence
        }
    }

    private fun preprocess(sequence: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (token in sequence) {
            val prevToken = result.lastOrNull()

            // MODIFICATION: Disambiguate '|' into '|open' and '|close' tokens.
            if (token == "|") {
                val isOpening =
                    prevToken == null || prevToken in binaryOperators.keys || prevToken == "(" || prevToken == "|open"
                result.add(if (isOpening) "|open" else "|close")
                continue
            }

            if (token == "-" && (prevToken == null || prevToken in binaryOperators.keys || prevToken == "(" || prevToken == "|open")) {
                result.add("_")
                continue
            }

            if (prevToken != null && shouldInsertMultiplication(prevToken, token)) {
                result.add("i×")
            }
            result.add(token)
        }
        return result
    }

    private fun shouldInsertMultiplication(prev: String, current: String): Boolean {
        val prevIsFactor =
            prev.isNumeric() || prev in constants || prev == ")" || prev == "|close" || prev in unaryPostfixOperators
        val currentIsFactor =
            current.isNumeric() || current in constants || current in unaryPrefixOperators || current == "(" || current == "|open"
        val prevIsOperator = prev in allOperators || prev == "(" || prev == "|open"

        return prevIsFactor && currentIsFactor && !prevIsOperator
    }

    //endregion

    //region Infix to Postfix Conversion (Shunting-Yard)

    private fun infixToPostfix(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val ops = Stack<String>()

        for (token in tokens) {
            when {
                token.isNumeric() || token in constants -> output.add(token)
                token in unaryPrefixOperators -> ops.push(token)
                token in unaryPostfixOperators -> output.add(token)
                token in binaryOperators.keys -> {
                    while (ops.isNotEmpty() && ops.peek() != "(" && ops.peek() != "|open" && hasHigherPrecedence(
                            ops.peek(), token
                        )
                    ) {
                        output.add(ops.pop())
                    }
                    ops.push(token)
                }

                token == "(" || token == "|open" -> ops.push(token)

                token == ")" -> {
                    while (ops.isNotEmpty() && ops.peek() != "(") {
                        output.add(ops.pop())
                    }
                    if (ops.isEmpty() || ops.pop() != "(") {
                        throw IllegalArgumentException("Mismatched parentheses.")
                    }
                    if (ops.isNotEmpty() && ops.peek() in unaryPrefixOperators) {
                        output.add(ops.pop())
                    }
                }

                token == "|close" -> {
                    while (ops.isNotEmpty() && ops.peek() != "|open") {
                        output.add(ops.pop())
                    }
                    if (ops.isEmpty() || ops.pop() != "|open") {
                        throw IllegalArgumentException("Mismatched absolute value bars.")
                    }
                    output.add("abs")
                }
            }
        }

        while (ops.isNotEmpty()) {
            val op = ops.pop()
            if (op == "(" || op == "|open") {
                throw IllegalArgumentException("Mismatched parentheses or absolute value bars at the end of expression.")
            }
            output.add(op)
        }
        return output
    }

    private fun hasHigherPrecedence(opOnStack: String, currentOp: String): Boolean {
        val info1 = allOperators[opOnStack] ?: return false
        val info2 = allOperators[currentOp] ?: return false
        return (info1.precedence > info2.precedence) || (info1.precedence == info2.precedence && !info1.isRightAssociative)
    }

    //endregion

    //region Postfix Evaluation

    private fun evaluatePostfix(tokens: List<String>): BigDecimal {
        val stack = Stack<BigDecimal>()

        for (token in tokens) {
            when {
                token.isNumeric() -> stack.push(BigDecimal(token))
                token in constants -> stack.push(constants.getValue(token))
                token in allOperators.keys -> {
                    val info = allOperators.getValue(token)
                    if (stack.size < info.arity) throw IllegalArgumentException("Invalid expression: Not enough operands for operator '$token'.")
                    val operands = List(info.arity) { stack.pop() }.reversed()
                    val result = executeOperation(token, operands)
                    stack.push(result)
                }

                else -> throw IllegalArgumentException("Unknown token in expression: $token")
            }
        }

        if (stack.size != 1) {
            if (stack.isEmpty()) return BigDecimal.ZERO
            throw IllegalArgumentException("Invalid or incomplete expression.")
        }
        return stack.pop()
    }

    private fun executeOperation(op: String, operands: List<BigDecimal>): BigDecimal {
        return when (op) {
            // Binary
            "+", "-" -> if (op == "+") operands[0].add(
                operands[1], mathContext
            ) else operands[0].subtract(operands[1], mathContext)

            "×", "i×" -> operands[0].multiply(operands[1], mathContext)
            "÷" -> {
                if (operands[1].compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Division by zero.")
                operands[0].divide(operands[1], mathContext)
            }

            "^" -> BigDecimalMath.pow(operands[0], operands[1], mathContext)

            // Unary Prefix
            "_" -> operands[0].negate()
            "√" -> BigDecimalMath.sqrt(operands[0], mathContext)
            "∛" -> BigDecimalMath.root(operands[0], BigDecimal(3), mathContext)
            "lg" -> BigDecimalMath.log10(operands[0], mathContext)
            "ln" -> BigDecimalMath.log(operands[0], mathContext)
            "sin", "cos", "tan" -> when (op) {
                "sin" -> BigDecimalMath.sin(operands[0], mathContext)
                "cos" -> BigDecimalMath.cos(operands[0], mathContext)
                else -> BigDecimalMath.tan(operands[0], mathContext)
            }

            "asin", "acos", "atan" -> when (op) {
                "asin" -> BigDecimalMath.asin(operands[0], mathContext)
                "acos" -> BigDecimalMath.acos(operands[0], mathContext)
                else -> BigDecimalMath.atan(operands[0], mathContext)
            }

            "sinh", "cosh", "tanh" -> when (op) {
                "sinh" -> BigDecimalMath.sinh(operands[0], mathContext)
                "cosh" -> BigDecimalMath.cosh(operands[0], mathContext)
                else -> BigDecimalMath.tanh(operands[0], mathContext)
            }

            "asinh", "acosh", "atanh" -> when (op) {
                "asinh" -> BigDecimalMath.asinh(operands[0], mathContext)
                "acosh" -> BigDecimalMath.acosh(operands[0], mathContext)
                else -> BigDecimalMath.atanh(operands[0], mathContext)
            }

            // Unary Postfix
            "!" -> factorial(operands[0])
            "°" -> BigDecimalMath.toRadians(operands[0], mathContext)
            "rad>°" -> BigDecimalMath.toDegrees(operands[0], mathContext)
            "abs" -> operands[0].abs(mathContext)

            else -> throw IllegalArgumentException("Unknown operator: $op")
        }
    }

    //endregion

    //region Math Helpers and Definitions

    private fun String.isNumeric(): Boolean = toBigDecimalOrNull() != null

    private fun factorial(n: BigDecimal): BigDecimal {
        if (n < BigDecimal.ZERO || n.scale() > 0 || n > BigDecimal(4000)) {
            throw ArithmeticException("Factorial is only defined for non-negative integers.")
        }
        var result = BigDecimal.ONE
        var i = BigDecimal.ONE
        while (i <= n) {
            result = result.multiply(i, mathContext)
            i = i.add(BigDecimal.ONE)
        }
        return result
    }

    private val pi by lazy { BigDecimalMath.pi(mathContext) }
    private val e by lazy { BigDecimalMath.e(mathContext) }

    private val constants = mapOf("π" to pi, "e" to e)

    private data class OperatorInfo(
        val precedence: Int, val isRightAssociative: Boolean, val arity: Int
    )

    private val binaryOperators = mapOf(
        "+" to OperatorInfo(2, false, 2),
        "-" to OperatorInfo(2, false, 2),
        "×" to OperatorInfo(3, false, 2),
        "÷" to OperatorInfo(3, false, 2),
        "i×" to OperatorInfo(4, false, 2),
        "^" to OperatorInfo(5, true, 2)
    )

    private val unaryPrefixOperators = setOf(
        "_",
        "√",
        "∛",
        "lg",
        "ln",
        "sin",
        "cos",
        "tan",
        "asin",
        "acos",
        "atan",
        "sinh",
        "cosh",
        "tanh",
        "asinh",
        "acosh",
        "atanh"
    )

    private val unaryPostfixOperators = setOf(
        "!", "°", "rad>°", "abs"
    )

    private val allOperators: Map<String, OperatorInfo> =
        binaryOperators + unaryPrefixOperators.associateWith {
            OperatorInfo(
                6, true, 1
            )
        } + unaryPostfixOperators.associateWith { OperatorInfo(7, false, 1) }

    //endregion
}