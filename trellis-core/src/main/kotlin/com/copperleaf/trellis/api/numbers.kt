package com.copperleaf.trellis.api

operator fun <T> Spek<T, out Number>.plus(other: Spek<T, out Number>) = AddSpek(this, other)
operator fun <T> Spek<T, Number>.plus(other: Number) = AddSpek(this, ValueSpek(other))

operator fun <T> Spek<T, Number>.minus(other: Spek<T, Number>) = SubtractSpek(this, other)
operator fun <T> Spek<T, Number>.minus(other: Number) = SubtractSpek(this, ValueSpek(other))

class AddSpek<T>(private val left: Spek<T, out Number>, private val right: Spek<T, out Number>) : Spek<T, Number> {
    override suspend fun evaluate(candidate: T): Number {
        return left.evaluate(candidate).toDouble() + right.evaluate(candidate).toDouble()
    }
}

class SubtractSpek<T>(private val left: Spek<T, Number>, private val right: Spek<T, Number>) : Spek<T, Number> {
    override suspend fun evaluate(candidate: T): Number {
        return left.evaluate(candidate).toDouble() - right.evaluate(candidate).toDouble()
    }
}

class MultiplySpek<T>(private val left: Spek<T, out Number>, private val right: Spek<T, out Number>) : Spek<T, Number> {
    override suspend fun evaluate(candidate: T): Number {
        return left.evaluate(candidate).toDouble() * right.evaluate(candidate).toDouble()
    }
}
class DivideSpek<T>(private val left: Spek<T, out Number>, private val right: Spek<T, out Number>) : Spek<T, Number> {
    override suspend fun evaluate(candidate: T): Number {
        return left.evaluate(candidate).toDouble() / right.evaluate(candidate).toDouble()
    }
}

class GreaterThanSpek(private val base: Spek<Number, Number>, private val allowEquals: Boolean = false) : Spek<Number, Boolean> {
    constructor(base: Number, allowEquals: Boolean = false) : this(ValueSpek(base), allowEquals)

    override suspend fun evaluate(candidate: Number): Boolean {
        if(allowEquals) {
            return candidate.toDouble() >= base.evaluate(candidate).toDouble()
        }
        else {
            return candidate.toDouble() > base.evaluate(candidate).toDouble()
        }
    }
}
class GreaterThanOperatorSpek<T>(private val lhs: Spek<T, Number>, private val rhs: Spek<T, Number>, private val allowEquals: Boolean = false) : Spek<T, Boolean> {

    override suspend fun evaluate(candidate: T): Boolean {
        if(allowEquals) {
            return lhs.evaluate(candidate).toDouble() >= rhs.evaluate(candidate).toDouble()
        }
        else {
            return lhs.evaluate(candidate).toDouble() > rhs.evaluate(candidate).toDouble()
        }
    }
}

class LessThanSpek(private val base: Spek<Number, Number>, private val allowEquals: Boolean = false) : Spek<Number, Boolean> {
    constructor(base: Number, allowEquals: Boolean = false) : this(ValueSpek(base), allowEquals)

    override suspend fun evaluate(candidate: Number): Boolean {
        if(allowEquals) {
            return candidate.toDouble() <= base.evaluate(candidate).toDouble()
        }
        else {
            return candidate.toDouble() < base.evaluate(candidate).toDouble()
        }
    }
}
class LessThanOperatorSpek<T>(private val lhs: Spek<T, Number>, private val rhs: Spek<T, Number>, private val allowEquals: Boolean = false) : Spek<T, Boolean> {

    override suspend fun evaluate(candidate: T): Boolean {
        if(allowEquals) {
            return lhs.evaluate(candidate).toDouble() <= rhs.evaluate(candidate).toDouble()
        }
        else {
            return lhs.evaluate(candidate).toDouble() < rhs.evaluate(candidate).toDouble()
        }
    }
}

class LargestSpek<T>(private vararg val bases: Spek<T, out Number>) : Spek<T, Number> {

    override suspend fun evaluate(candidate: T): Number {
        var largest = bases.asList().first().evaluate(candidate)

        for(base in bases.asList().drop(1)) {
            val tempValue = base.evaluate(candidate)
            if(tempValue.toDouble() > largest.toDouble()) {
                largest = tempValue
            }
        }

        return largest
    }
}

class SmallestSpek<T>(private vararg val bases: Spek<T, Number>) : Spek<T, Number> {

    override suspend fun evaluate(candidate: T): Number {
        var smallest = bases.asList().first().evaluate(candidate)

        for(base in bases.asList().drop(1)) {
            val tempValue = base.evaluate(candidate)
            if(tempValue.toDouble() < smallest.toDouble()) {
                smallest = tempValue
            }
        }

        return smallest
    }
}