package com.copperleaf.trellis.api

import com.copperleaf.trellis.introspection.visitor.SpekVisitor

interface Spek<T, U> {
    suspend fun evaluate(visitor: SpekVisitor, candidate: T): U
    val children: List<Spek<*, *>>
        get() = emptyList()
}
