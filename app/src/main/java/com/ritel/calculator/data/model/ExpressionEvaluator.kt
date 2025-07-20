package com.ritel.calculator.data.model

import ch.obermuhlner.math.big.BigDecimalMath
import java.math.BigDecimal
import java.math.MathContext
import java.util.Stack

/**
 * A robust evaluator for mathematical expressions provided as a sequence of strings.
 *
 * This class implements a two-stage process:
 * 1.  **Infix to Postfix Conversion**: It first preprocesses the input sequence to handle
 * implied multiplication and unary operators. Then, it uses a modified Shunting-yard
 * algorithm to convert the user-friendly infix expression into a computer-friendly
 * postfix (Reverse Polish Notation) expression.
 * 2.  **Postfix Evaluation**: It evaluates the postfix expression to compute the final result.
 *
 * It uses BigDecimal for high-precision arithmetic, powered by the `big-math` library
 * for advanced functions like roots, logarithms, and trigonometric operations.
 *
 * @property mathContext The precision context for all BigDecimal operations.
 */
class ExpressionEvaluator {
    private val mathContext = MathContext.DECIMAL128

    //region Public API

    /**
     * Evaluates a mathematical expression sequence.
     *
     * @param sequence The list of tokens (numbers, operators, functions) representing the expression.
     * @return A [Result] containing the [BigDecimal] answer on success, or an [Exception] on failure.
     */
    fun evaluate(sequence: List<String>): Result<BigDecimal> {
        return try {
            if (sequence.isEmpty()) {
                return Result.success(BigDecimal.ZERO)
            }
            // MODIFICATION: The call to validate() has been removed.
            val preprocessed = preprocess(sequence)
            val postfix = infixToPostfix(preprocessed)
            val result = evaluatePostfix(postfix)
            // Strip trailing zeros for a cleaner representation, but keep scale if non-zero
            Result.success(result.stripTrailingZeros())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //endregion

    //region Preprocessing

    /**
     * Prepares the sequence for parsing by:
     * 1. Distinguishing unary minus from binary subtraction.
     * 2. Inserting an explicit operator for implied multiplication to enforce higher precedence.
     */
    private fun preprocess(sequence: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (i in sequence.indices) {
            val token = sequence[i]
            val prevToken = result.lastOrNull()

            // Convert '-' to unary minus '_' where appropriate
            if (token == "-" && (prevToken == null || prevToken in binaryOperators.keys || prevToken == "(")) {
                result.add("_")
                continue
            }

            // Insert implied multiplication operator 'i×'
            if (prevToken != null && shouldInsertMultiplication(prevToken, token)) {
                result.add("i×")
            }
            result.add(token)
        }
        return result
    }

    private fun shouldInsertMultiplication(prev: String, current: String): Boolean {
        val prevIsFactor =
            prev.isNumeric() || prev in constants || prev == ")" || prev in unaryPostfixOperators
        val currentIsFactor =
            current.isNumeric() || current in constants || current in unaryPrefixOperators || current == "("
        val prevIsOperator = prev in allOperators || prev == "("

        return prevIsFactor && currentIsFactor && !prevIsOperator
    }

    //endregion

    //region Infix to Postfix Conversion (Shunting-Yard)

    /**
     * Converts the preprocessed infix token list to a postfix (RPN) list.
     */
    private fun infixToPostfix(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val ops = Stack<String>()

        for (token in tokens) {
            when {
                token.isNumeric() || token in constants -> output.add(token)
                token in unaryPrefixOperators -> ops.push(token)
                token in unaryPostfixOperators -> output.add(token)
                token in binaryOperators.keys -> {
                    while (ops.isNotEmpty() && ops.peek() != "(" && hasHigherPrecedence(
                            ops.peek(), token
                        )
                    ) {
                        output.add(ops.pop())
                    }
                    ops.push(token)
                }

                token == "(" -> ops.push(token)
                token == ")" -> {
                    while (ops.isNotEmpty() && ops.peek() != "(") {
                        output.add(ops.pop())
                    }
                    // This check correctly handles extra ')' by throwing an error if no matching '(' is on the stack.
                    if (ops.isEmpty() || ops.pop() != "(") {
                        throw IllegalArgumentException("括号不匹配 (Mismatched parentheses)")
                    }
                    if (ops.isNotEmpty() && ops.peek() in unaryPrefixOperators) {
                        output.add(ops.pop())
                    }
                }
            }
        }

        while (ops.isNotEmpty()) {
            val op = ops.pop()
            // If an unmatched parenthesis is left on the stack, it implies an unclosed group at the end of the expression.
            // We can safely ignore it to allow for omitted closing parentheses.
            if (op != "(") {
                output.add(op)
            }
        }

        return output
    }

    private fun hasHigherPrecedence(opOnStack: String, currentOp: String): Boolean {
        val info1 = allOperators[opOnStack] ?: return false
        val info2 = allOperators[currentOp] ?: return false
        return (info1.precedence > info2.precedence) || (info1.precedence == info2.precedence && !info1.isRightAssociative)
    }

    //endregion

    //region Postfix Evaluation (No changes in this section)

    /**
     * Evaluates the postfix (RPN) token list.
     */
    private fun evaluatePostfix(tokens: List<String>): BigDecimal {
        val stack = Stack<BigDecimal>()

        for (token in tokens) {
            when {
                token.isNumeric() -> stack.push(BigDecimal(token))
                token in constants -> stack.push(constants.getValue(token))
                token in allOperators.keys -> {
                    val info = allOperators.getValue(token)
                    if (stack.size < info.arity) throw IllegalArgumentException("表达式无效，运算符 '$token' 缺少操作数 (Invalid expression. Operator '$token' needs ${info.arity} operand(s)).")

                    val operands = List(info.arity) { stack.pop() }.reversed()
                    val result = executeOperation(token, operands)
                    stack.push(result)
                }

                else -> throw IllegalArgumentException("表达式中存在未知符号 (Unknown token in expression): $token")
            }
        }

        if (stack.size != 1) throw IllegalArgumentException("表达式无效或不完整 (The expression is invalid or incomplete).")
        return stack.pop()
    }

    private fun executeOperation(op: String, operands: List<BigDecimal>): BigDecimal {
        return when (op) {
            // Binary
            "+" -> operands[0].add(operands[1], mathContext)
            "-" -> operands[0].subtract(operands[1], mathContext)
            "×", "i×" -> operands[0].multiply(operands[1], mathContext)
            "÷" -> {
                if (operands[1].compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("除数不能为零 (Division by zero).")
                operands[0].divide(operands[1], mathContext)
            }

            "^" -> BigDecimalMath.pow(operands[0], operands[1], mathContext)
            // Unary Prefix
            "_" -> operands[0].negate()
            "√" -> BigDecimalMath.sqrt(operands[0], mathContext)
            "∛" -> BigDecimalMath.root(operands[0], BigDecimal(3), mathContext)
            "lg" -> BigDecimalMath.log10(operands[0], mathContext)
            "ln" -> BigDecimalMath.log(operands[0], mathContext)
            "sin" -> BigDecimalMath.sin(operands[0], mathContext)
            "cos" -> BigDecimalMath.cos(operands[0], mathContext)
            "tan" -> BigDecimalMath.tan(operands[0], mathContext)
            "asin" -> BigDecimalMath.asin(operands[0], mathContext)
            "acos" -> BigDecimalMath.acos(operands[0], mathContext)
            "atan" -> BigDecimalMath.atan(operands[0], mathContext)
            // Unary Postfix
            "!" -> factorial(operands[0])
            "°" -> BigDecimalMath.toRadians(operands[0], mathContext)
            "rad>°" -> BigDecimalMath.toDegrees(operands[0], mathContext)
            else -> throw IllegalArgumentException("未知运算符 (Unknown operator): $op")
        }
    }

    //endregion

    //region Math Helpers and Definitions (No changes in this section)

    private fun String.isNumeric(): Boolean = toBigDecimalOrNull() != null

    private fun factorial(n: BigDecimal): BigDecimal {
        // Check if the number is a non-negative integer
        if (n < BigDecimal.ZERO || n.scale() > 0 || n > BigDecimal(4000)) { // Limit to avoid performance issues/errors
            throw ArithmeticException("阶乘仅支持非负整数 (Factorial is only defined for non-negative integers).")
        }
        var result = BigDecimal.ONE
        var i = BigDecimal.ONE
        while (i <= n) {
            result = result.multiply(i, mathContext)
            i = i.add(BigDecimal.ONE)
        }
        return result
    }

    private val PI by lazy { BigDecimalMath.pi(mathContext) }
    private val E by lazy { BigDecimalMath.e(mathContext) }

    private val constants = mapOf("π" to PI, "e" to E)

    private data class OperatorInfo(
        val precedence: Int, val isRightAssociative: Boolean, val arity: Int
    )

    private val binaryOperators = mapOf(
        "+" to OperatorInfo(2, false, 2),
        "-" to OperatorInfo(2, false, 2),
        "×" to OperatorInfo(3, false, 2), // Explicit multiplication
        "÷" to OperatorInfo(3, false, 2),
        "i×" to OperatorInfo(7, false, 2), // Implied multiplication (higher precedence)
        "^" to OperatorInfo(8, true, 2)
    )

    private val unaryPrefixOperators = setOf(
        "_", "√", "∛", "lg", "ln", "sin", "cos", "tan", "asin", "acos", "atan"
    )

    private val unaryPostfixOperators = setOf(
        "!", "°", "rad>°"
    )

    private val allOperators: Map<String, OperatorInfo> =
        binaryOperators + unaryPrefixOperators.associateWith {
            OperatorInfo(
                6,
                true,
                1
            )
        } + unaryPostfixOperators.associateWith { OperatorInfo(7, false, 1) }

    //endregion
}