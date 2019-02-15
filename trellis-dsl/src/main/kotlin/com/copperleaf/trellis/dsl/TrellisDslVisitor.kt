package com.copperleaf.trellis.dsl

import com.copperleaf.kudzu.Node
import com.copperleaf.kudzu.child
import com.copperleaf.kudzu.find
import com.copperleaf.kudzu.has
import com.copperleaf.kudzu.hasChild
import com.copperleaf.kudzu.parser.ChoiceNode
import com.copperleaf.kudzu.parser.ExpressionVisitor
import com.copperleaf.kudzu.parser.ManyNode
import com.copperleaf.kudzu.parser.MaybeNode
import com.copperleaf.kudzu.parser.NamedNode
import com.copperleaf.kudzu.parser.SequenceNode
import com.copperleaf.kudzu.parser.WordNode
import com.copperleaf.trellis.api.Spek
import com.copperleaf.trellis.api.ValueSpek
import kotlinx.coroutines.runBlocking

@Suppress("UNCHECKED_CAST")
class TrellisDslVisitor {

    companion object {
        private val visitor = ExpressionVisitor(TrellisDslParser.operators, ::createSpekFromExpressionOrNode)

        fun visit(context: SpekExpressionContext, node: Node) {
            visitor.visit(context, node)
        }

        private fun createSpekFromExpressionOrNode(context: SpekExpressionContext, node: Node): Spek<Any, Any> {
            val hasSubExpr = node
                .child<SequenceNode>()
                .has(NamedNode::class, "subExpr")

            return runBlocking {
                if(hasSubExpr) {
                    createSpekFromExpression(context, node)
                }
                else {
                    createSpekFromTerm(context, node)
                }
            }
        }

        private fun createSpekFromExpression(context: SpekExpressionContext, node: Node): Spek<Any, Any> {
            val subExpr = node
                .child<SequenceNode>()
                .find<NamedNode>("subExpr")
                .child()
            val innerContext = context.copy()
            visitor.visit(innerContext, subExpr)

            return innerContext.value as Spek<Any, Any>
        }

        private fun createSpekFromTerm(context: SpekExpressionContext, node: Node): Spek<Any, Any> {
            val argValues = mutableListOf<Spek<Any, Any>>()

            val spekName = node
                .child<SequenceNode>()
                .child<SequenceNode>()
                .find<WordNode>("spekName")
                .text

            val spekArgsNode = node
                .child<SequenceNode>()
                .child<SequenceNode>()
                .child<MaybeNode>()

            if (spekArgsNode.hasChild()) {
                val argsListNode = spekArgsNode.child<SequenceNode>().child<MaybeNode>()

                if (argsListNode.hasChild()) {
                    val initialArgNode = argsListNode
                        .child<SequenceNode>()
                        .child<SequenceNode>()
                        .find<ChoiceNode>("exprTerm")
                    val initialArgSpek = createSpekFromExpressionOrNode(context, initialArgNode)
                    argValues.add(initialArgSpek)

                    val otherArgs = argsListNode
                        .child<SequenceNode>()
                        .child<MaybeNode>()

                    if (otherArgs.hasChild()) {
                        val manyArgs = otherArgs
                            .child<ManyNode>()
                            .children

                        for (otherArg in manyArgs) {
                            val otherArgNode = otherArg
                                .child<SequenceNode>()
                                .find<ChoiceNode>("exprTerm")
                            val otherArgSpek = createSpekFromExpressionOrNode(context, otherArgNode)
                            argValues.add(otherArgSpek)
                        }
                    }
                }
            }

            return createSpekFromArgs(context, spekName, argValues)
        }

        private fun createSpekFromArgs(context: SpekExpressionContext, spekName: String, args: List<Spek<Any, Any>>): Spek<Any, Any> {
            return context
                .spekIdentifiers
                .firstOrNull { it.name == spekName }
                ?.ctor
                ?.invoke(context, args) as? Spek<Any, Any>
                ?: ValueSpek<Any, String>(spekName) as Spek<Any, Any>
        }
    }

}
