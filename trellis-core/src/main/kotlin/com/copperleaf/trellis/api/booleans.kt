package com.copperleaf.trellis.api

// Extension Methods
//----------------------------------------------------------------------------------------------------------------------

infix fun <T> Spek<T, Boolean>.and(other: Spek<T, Boolean>): Spek<T, Boolean> = AndSpek(this, other)
infix fun <T> Spek<T, Boolean>.andNot(other: Spek<T, Boolean>): Spek<T, Boolean> = AndSpek(this, NotSpek(other))
infix fun <T> Spek<T, Boolean>.or(other: Spek<T, Boolean>): Spek<T, Boolean> = OrSpek(this, other)
infix fun <T> Spek<T, Boolean>.orNot(other: Spek<T, Boolean>): Spek<T, Boolean> = OrSpek(this, NotSpek(other))

fun <T> Spek<T, Boolean>.not(): Spek<T, Boolean> = NotSpek(this)

operator fun <T> Spek<T, Boolean>.plus(other: Spek<T, Boolean>) = this.and(other)
operator fun <T> Spek<T, Boolean>.unaryMinus() = this.not()

// Base logical operators
//----------------------------------------------------------------------------------------------------------------------

/**
 * Verifies that two speks are both true.
 */
class AndSpek<T>(private val left: Spek<T, Boolean>, private val right: Spek<T, Boolean>) : Spek<T, Boolean> {
    override suspend fun evaluate(candidate: T): Boolean {
        return left.evaluate(candidate) && right.evaluate(candidate)
    }
}

/**
 * Verifies that at least one of two speks are true.
 */
class OrSpek<T>(private val left: Spek<T, Boolean>, private val right: Spek<T, Boolean>) : Spek<T, Boolean> {
    override suspend fun evaluate(candidate: T): Boolean {
        return left.evaluate(candidate) || right.evaluate(candidate)
    }
}

/**
 * Inverts a Boolean spek
 */
class NotSpek<T>(private val base: Spek<T, Boolean>) : Spek<T, Boolean> {
    override suspend fun evaluate(candidate: T): Boolean {
        return !base.evaluate(candidate)
    }
}

