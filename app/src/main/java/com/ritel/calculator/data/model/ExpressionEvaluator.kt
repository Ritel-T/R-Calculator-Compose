package com.ritel.calculator.data.model

import ch.obermuhlner.math.big.BigDecimalMath
import java.math.BigDecimal
import java.math.MathContext
import java.util.Stack

/**
 * 一个用于解析和计算数学表达式的工具对象。
 *
 * 支持的操作:
 * - 基础运算: +, -, *, ×, /, ÷
 * - 函数: sin, cos, tan, asin, acos, atan, √ (sqrt), ∛ (cbrt), lg (log10), ln (log), abs (||)
 * - 常量: π, e
 * - 运算符: ^ (幂), ! (阶乘)
 * - 单位: ° (度), rad→° (弧度转度)
 *
 * 特性:
 * - 隐式乘法: 例如 2π, 3sin(45), (1+2)(3+4)
 * - 函数可不带括号: 例如 sin30, √4 (会运算紧随其后的一个数字)
 * - 精度: DECIMAL128 (约34位有效数字)
 * - 错误处理: 返回 Kotlin 的 Result 类型，封装成功或异常
 */
object ExpressionEvaluator {

    // 1. 定义与设置
    // ======================================================================================

    private val mathContext = MathContext.DECIMAL128
    private val PI = BigDecimalMath.pi(mathContext)
    private val E = BigDecimalMath.e(mathContext)

    // 定义所有支持的词元(Token)类型
    private sealed class Token
    private data class NumberToken(val value: BigDecimal) : Token()
    private data class OperatorToken(val symbol: String) : Token()
    private data class FunctionToken(val name: String) : Token()
    private object LeftParenToken : Token()
    private object RightParenToken : Token()

    // 定义运算符的属性：优先级和结合性
    private data class OperatorInfo(val precedence: Int, val isRightAssociative: Boolean = false)

    private val operators = mapOf(
        "+" to OperatorInfo(1),
        "-" to OperatorInfo(1),
        "*" to OperatorInfo(2),
        "×" to OperatorInfo(2),
        "/" to OperatorInfo(2),
        "÷" to OperatorInfo(2),
        "·" to OperatorInfo(3), // 隐式乘法，优先级更高
        "^" to OperatorInfo(4, isRightAssociative = true),
        // 函数和一元操作符有更高的优先级
        "neg" to OperatorInfo(5), // 一元负号
        "!" to OperatorInfo(6),
        "°" to OperatorInfo(6),
        "rad→°" to OperatorInfo(6) // 后缀操作
    )

    /**
     * 公开的求值入口函数
     * @param expressionString 输入的数学表达式字符串
     * @return Result<BigDecimal> 成功时包含结果，失败时包含异常
     */
    fun evaluate(expressionString: String): Result<BigDecimal> {
        return try {
            // 步骤 1: 词法分析
            val tokens = tokenize(expressionString)
            // 步骤 2: 转换为 RPN
            val rpn = toRPN(tokens)
            // 步骤 3: 求值 RPN
            val result = evaluateRPN(rpn)
            Result.success(result)
        } catch (e: Exception) {
            // 捕获所有可能的错误，例如格式错误、除以零等
            Result.failure(e)
        }
    }


    // 2. 步骤一：词法分析 (Tokenization)
    // ======================================================================================

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val s = expr.replace(" ", "")
            .replace("×", "*")
            .replace("÷", "/")

        while (i < s.length) {
            val char = s[i]
            val lastToken = tokens.lastOrNull()

            // 检查是否需要插入隐式乘法
            fun addImplicitMultiplication() {
                if (lastToken is NumberToken || lastToken is RightParenToken || (lastToken is OperatorToken && setOf(
                        "!", "°", "rad→°"
                    ).contains(lastToken.symbol))
                ) {
                    tokens.add(OperatorToken("·"))
                }
            }

            when {
                // 数字 (例如: 123, 3.14, .5)
                char.isDigit() || (char == '.' && i + 1 < s.length && s[i + 1].isDigit()) -> {
                    addImplicitMultiplication()
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    tokens.add(NumberToken(BigDecimal(s.substring(start, i))))
                }

                // 字母或特殊符号开头的函数/常量
                char.isLetter() || char in "√∛πe|" -> {
                    // 解析连续的字母或特殊符号
                    val start = i
                    i++
                    while (i < s.length && (s[i].isLetter() || (s.substring(
                            start, i
                        ) == "rad" && "→°".startsWith(s[i])))
                    ) i++
                    val name = s.substring(start, i)

                    when (name) {
                        "π" -> {
                            addImplicitMultiplication(); tokens.add(NumberToken(PI))
                        }

                        "e" -> {
                            addImplicitMultiplication(); tokens.add(NumberToken(E))
                        }

                        "sin", "cos", "tan", "asin", "acos", "atan", "lg", "ln", "√", "∛" -> {
                            addImplicitMultiplication()
                            tokens.add(FunctionToken(name))
                            // 处理 sin30, √4 这类不带括号的情况
                            if (i >= s.length || s[i] != '(') {
                                tokens.add(LeftParenToken)
                                val numberTokens = tokenize(s.substring(i)) // 递归解析后面的数字
                                val firstNumber = numberTokens.firstOrNull()
                                if (firstNumber is NumberToken) {
                                    tokens.add(firstNumber)
                                    i += firstNumber.value.toPlainString().length
                                } else {
                                    throw IllegalArgumentException("函数 '$name' 后必须紧跟数字或括号表达式")
                                }
                                tokens.add(RightParenToken)
                            }
                        }

                        "rad→°" -> tokens.add(OperatorToken("rad→°")) // 作为后缀操作符
                        "|" -> { // 处理绝对值
                            if (lastToken is NumberToken || lastToken is RightParenToken) {
                                // 当作右括号
                                tokens.add(RightParenToken)
                            } else {
                                // 当作 abs(
                                addImplicitMultiplication()
                                tokens.add(FunctionToken("abs"))
                                tokens.add(LeftParenToken)
                            }
                        }

                        else -> throw IllegalArgumentException("未知函数或常量: '$name'")
                    }
                }

                // 括号
                char == '(' -> {
                    addImplicitMultiplication(); tokens.add(LeftParenToken); i++
                }

                char == ')' -> {
                    tokens.add(RightParenToken); i++
                }

                // 操作符
                else -> {
                    when (char) {
                        '+', '*', '/', '^', '!' -> {
                            tokens.add(OperatorToken(char.toString())); i++
                        }

                        '°' -> {
                            tokens.add(OperatorToken("°")); i++
                        } // 作为后缀操作符
                        '-' -> {
                            // 判断是一元负号还是二元减号
                            if (lastToken == null || lastToken is LeftParenToken || lastToken is OperatorToken) {
                                tokens.add(FunctionToken("neg")) // 一元负号
                            } else {
                                tokens.add(OperatorToken("-")) // 二元减法
                            }
                            i++
                        }

                        else -> throw IllegalArgumentException("非法字符: '$char'")
                    }
                }
            }
        }
        return tokens
    }


    // 3. 步骤二：调度场算法 (Shunting-Yard)
    // ======================================================================================

    private fun toRPN(tokens: List<Token>): List<Token> {
        val outputQueue = mutableListOf<Token>()
        val operatorStack = Stack<Token>()

        for (token in tokens) {
            when (token) {
                is NumberToken -> outputQueue.add(token)
                is FunctionToken -> operatorStack.push(token)
                is OperatorToken -> {
                    while (!operatorStack.isEmpty() && operatorStack.peek() !is LeftParenToken && ((operatorStack.peek() is FunctionToken) || (operatorStack.peek() as? OperatorToken)?.let { o2 ->
                            val o1 = operators[token.symbol]!!
                            val o2Info = operators[o2.symbol]!!
                            (o2Info.precedence > o1.precedence) || (o2Info.precedence == o1.precedence && !o1.isRightAssociative)
                        } == true)) {
                        outputQueue.add(operatorStack.pop())
                    }
                    operatorStack.push(token)
                }

                is LeftParenToken -> operatorStack.push(token)
                is RightParenToken -> {
                    while (!operatorStack.isEmpty() && operatorStack.peek() !is LeftParenToken) {
                        outputQueue.add(operatorStack.pop())
                    }
                    if (operatorStack.isEmpty()) throw IllegalArgumentException("括号不匹配")
                    operatorStack.pop() // 弹出 '('
                }
            }
        }
        while (!operatorStack.isEmpty()) {
            val op = operatorStack.pop()
            if (op is LeftParenToken) throw IllegalArgumentException("括号不匹配")
            outputQueue.add(op)
        }
        return outputQueue
    }


    // 4. 步骤三：RPN 求值 (Evaluation)
    // ======================================================================================

    private fun evaluateRPN(rpnTokens: List<Token>): BigDecimal {
        val stack = Stack<BigDecimal>()

        for (token in rpnTokens) {
            when (token) {
                is NumberToken -> stack.push(token.value)
                is OperatorToken -> {
                    // 后缀操作符 (一元)
                    if (setOf("!", "°", "rad→°").contains(token.symbol)) {
                        if (stack.isEmpty()) throw IllegalArgumentException("缺少操作数")
                        val operand = stack.pop()
                        val result = when (token.symbol) {
                            "!" -> BigDecimalMath.factorial(operand.intValueExact())
                            "°" -> operand.multiply(PI, mathContext)
                                .divide(BigDecimal(180), mathContext)

                            "rad→°" -> operand.multiply(BigDecimal(180), mathContext)
                                .divide(PI, mathContext)

                            else -> throw IllegalStateException()
                        }
                        stack.push(result)
                    } else { // 双元操作符
                        if (stack.size < 2) throw IllegalArgumentException("缺少操作数")
                        val b = stack.pop()
                        val a = stack.pop()
                        val result = when (token.symbol) {
                            "+" -> a.add(b, mathContext)
                            "-" -> a.subtract(b, mathContext)
                            "*" -> a.multiply(b, mathContext)
                            "·" -> a.multiply(b, mathContext) // 隐式乘法
                            "/" -> a.divide(b, mathContext)
                            "^" -> BigDecimalMath.pow(a, b, mathContext)
                            else -> throw IllegalArgumentException("未知运算符: ${token.symbol}")
                        }
                        stack.push(result)
                    }
                }

                is FunctionToken -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("函数 '${token.name}' 缺少参数")
                    val operand = stack.pop()
                    val result = when (token.name) {
                        "sin" -> BigDecimalMath.sin(operand, mathContext)
                        "cos" -> BigDecimalMath.cos(operand, mathContext)
                        "tan" -> BigDecimalMath.tan(operand, mathContext)
                        "asin" -> BigDecimalMath.asin(operand, mathContext)
                        "acos" -> BigDecimalMath.acos(operand, mathContext)
                        "atan" -> BigDecimalMath.atan(operand, mathContext)
                        "lg" -> BigDecimalMath.log10(operand, mathContext)
                        "log" -> BigDecimalMath.log(operand, mathContext)
                        "√" -> BigDecimalMath.sqrt(operand, mathContext)
                        "∛" -> BigDecimalMath.root(operand, 3.toBigDecimal(), mathContext)
                        "neg" -> operand.negate()
                        "abs" -> operand.abs()
                        else -> throw IllegalArgumentException("未知函数: ${token.name}")
                    }
                    stack.push(result)
                }

                else -> throw IllegalStateException("RPN队列中存在非法Token")
            }
        }

        if (stack.size != 1) throw IllegalArgumentException("表达式格式错误，最终栈中数量不为1")
        return stack.pop()
    }
}