package com.ritel.calculator

sealed interface ButtonAction {
    val symbol: String
}

data class Numeric(val digit: Int) : ButtonAction {
    override val symbol: String = digit.toString()
}

data object Dot : ButtonAction {
    override val symbol: String = "."
}

sealed interface Operator : ButtonAction

data object Add : Operator {
    override val symbol: String = "+"
}

data object Subtract : Operator {
    override val symbol: String = "-"
}

data object Multiply : Operator {
    override val symbol: String = "×"
}

data object Divide : Operator {
    override val symbol: String = "÷"
}

sealed interface Function : ButtonAction

data object PlusMinus : Function {
    override val symbol: String = "±"
}

data object Percent : Function {
    override val symbol: String = "%"
}

data object Delete : Function {
    override val symbol: String = "⌫"
}

data object Clear : ButtonAction {
    override val symbol: String = "AC"
}

data object Equals : ButtonAction {
    override val symbol: String = "="
}