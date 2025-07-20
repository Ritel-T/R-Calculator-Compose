package com.ritel.calculator.data.model

sealed interface CalculatorButton {
    val symbol: String
}

sealed interface SimpleButton : CalculatorButton
sealed interface ScientificButton : CalculatorButton

data class Numeric(val digit: Int) : SimpleButton, ScientificButton {
    override val symbol = digit.toString()
}

data object Dot : SimpleButton, ScientificButton {
    override val symbol = "."
}

data object Delete : SimpleButton, ScientificButton {
    override val symbol = "⌫"
}

data object Clear : SimpleButton, ScientificButton {
    override val symbol = "AC"
}

data object Equals : SimpleButton, ScientificButton {
    override val symbol = "="
}

sealed interface Operator : SimpleButton, ScientificButton

data object Add : Operator {
    override val symbol = "+"
}

data object Subtract : Operator {
    override val symbol = "-"
}

data object Multiply : Operator {
    override val symbol = "×"
}

data object Divide : Operator {
    override val symbol = "÷"
}
// interface Operator

sealed interface SimpleFunction : SimpleButton
sealed interface ScientificFunction : ScientificButton

data object PlusMinus : SimpleFunction {
    override val symbol = "±"
}

data object Percent : SimpleFunction {
    override val symbol = "%"
}

data object LeftParen : ScientificFunction {
    override val symbol = "("
}

data object RightParen : ScientificFunction {
    override val symbol = ")"
}

data object Power : ScientificFunction {
    override val symbol = "^"
}

data object SquareRoot : ScientificFunction {
    override val symbol = "√"
}

data object CubeRoot : ScientificFunction {
    override val symbol = "∛"
}

data object CommonLog : ScientificFunction {
    override val symbol = "lg"
}

data object NaturalLog : ScientificFunction {
    override val symbol = "ln"
}

data object Sine : ScientificFunction {
    override val symbol = "sin"
}

data object Cosine : ScientificFunction {
    override val symbol = "cos"
}

data object Tangent : ScientificFunction {
    override val symbol = "tan"
}

data object ArcSine : ScientificFunction {
    override val symbol = "asin"
}

data object ArcCosine : ScientificFunction {
    override val symbol = "acos"
}

data object ArcTangent : ScientificFunction {
    override val symbol = "atan"
}

data object Factorial : ScientificFunction {
    override val symbol = "!"
}

data object Pi : ScientificFunction {
    override val symbol = "π"
}

data object EulersNumber : ScientificFunction {
    override val symbol = "e"
}

data object AbsoluteValue : ScientificFunction {
    override val symbol = "|x|"
}

data object Degrees : ScientificFunction {
    override val symbol = "°"
}

data object RadToDeg : ScientificFunction {
    override val symbol = "rad→°"
}

data object LeftArrow : ScientificFunction {
    override val symbol = "←"
}

data object RightArrow : ScientificFunction {
    override val symbol = "→"
}

data object Alternate : ScientificFunction {
    override val symbol = "Alt"
}