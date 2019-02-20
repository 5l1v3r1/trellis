package com.copperleaf.trellis.dsl

import com.copperleaf.trellis.api.EmptyVisitor
import com.copperleaf.trellis.api.Spek
import com.copperleaf.trellis.api.SpekVisitor
import com.copperleaf.trellis.api.ValueSpek
import com.copperleaf.trellis.api.andNot
import com.copperleaf.trellis.api.or
import com.copperleaf.trellis.api.visiting
import com.copperleaf.trellis.dsl.TrellisDslVisitor.Companion.create
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expect
import strikt.assertions.isEqualTo

@Suppress("UNCHECKED_CAST")
class TrellisReadmeBooleanExpressionsTest {

    @ParameterizedTest
    @MethodSource("testArgs")
    fun testBuilderFormat(user: User, hasPermission: Boolean) {
        val permissionSpek = HasExplicitCapabilitySpek("write")
            .or(IsRoleSpek("author").andNot(HasExplicitCapabilityRevokedSpek("write")))
            .or(IsSuperuserSpek())

        expect {
            that(permissionSpek.evaluate(EmptyVisitor, user)).isEqualTo(hasPermission)
        }
    }

    @ParameterizedTest
    @MethodSource("testArgs")
    fun testExpressionFormat(user: User, hasPermission: Boolean) {
        val permissionSpek: Spek<User, Boolean> = create(
            context,
            """
            |cap(write) or (role(author) and not capRevoked(write)) or superuser()
            """.trimMargin()
        )

        expect {
            that(permissionSpek.evaluate(EmptyVisitor, user)).isEqualTo(hasPermission)
        }
    }

    companion object {

        @JvmStatic
        fun testArgs(): List<Arguments> = listOf(
            Arguments.of(User("author"), true),
            Arguments.of(User("editor"), false),
            Arguments.of(User("superuser"), true),

            Arguments.of(User("author", listOf("read")), true),
            Arguments.of(User("editor", listOf("read")), false),
            Arguments.of(User("superuser", listOf("read")), true),

            Arguments.of(User("author", listOf("write")), true),
            Arguments.of(User("editor", listOf("write")), true),
            Arguments.of(User("superuser", listOf("write")), true),

            Arguments.of(User("author", listOf("read", "write")), true),
            Arguments.of(User("editor", listOf("read", "write")), true),
            Arguments.of(User("superuser", listOf("read", "write")), true),

            Arguments.of(User("author", emptyList(), listOf("write")), false),
            Arguments.of(User("editor", emptyList(), listOf("write")), false),
            Arguments.of(User("superuser", emptyList(), listOf("write")), true),

            Arguments.of(User("author", listOf("write"), listOf("write")), true),
            Arguments.of(User("editor", listOf("write"), listOf("write")), true),
            Arguments.of(User("superuser", listOf("write"), listOf("write")), true)
        )
    }

    val context = SpekExpressionContext {
        register("role") { cxt, args ->
            IsRoleSpek(args.first().typeSafe<Any, Any, Any, String>(cxt))
        }
        register("superuser") { _, _ ->
            IsSuperuserSpek()
        }
        register("cap") { cxt, args ->
            HasExplicitCapabilitySpek(args.first().typeSafe<Any, Any, Any, String>(cxt))
        }
        register("capRevoked") { cxt, args ->
            HasExplicitCapabilityRevokedSpek(args.first().typeSafe<Any, Any, Any, String>(cxt))
        }
    }

    data class User(
        val role: String,
        val capabilities: List<String> = emptyList(),
        val revokedCapabilities: List<String> = emptyList()
    )

    class IsRoleSpek(private val roleName: Spek<Any, String>) : Spek<User, Boolean> {
        constructor(roleName: String) : this(ValueSpek(roleName))

        override val children = listOf(roleName)

        override suspend fun evaluate(visitor: SpekVisitor, candidate: User): Boolean {
            return visiting(visitor) {
                val role = roleName.evaluate(visitor, candidate)
                candidate.role == role
            }
        }
    }

    class IsSuperuserSpek : Spek<User, Boolean> {
        override suspend fun evaluate(visitor: SpekVisitor, candidate: User): Boolean {
            return visiting(visitor) { candidate.role == "superuser" }
        }
    }

    class HasExplicitCapabilitySpek(private val capabilityName: Spek<Any, String>) : Spek<User, Boolean> {
        constructor(capabilityName: String) : this(ValueSpek(capabilityName))

        override val children = listOf(capabilityName)

        override suspend fun evaluate(visitor: SpekVisitor, candidate: User): Boolean {
            return visiting(visitor) {
                val capability = capabilityName.evaluate(visitor, candidate)
                candidate.capabilities.contains(capability)
            }
        }
    }

    class HasExplicitCapabilityRevokedSpek(private val capabilityName: Spek<Any, String>) : Spek<User, Boolean> {
        constructor(capabilityName: String) : this(ValueSpek(capabilityName))

        override val children = listOf(capabilityName)

        override suspend fun evaluate(visitor: SpekVisitor, candidate: User): Boolean {
            return visiting(visitor) {
                val capability = capabilityName.evaluate(visitor, candidate)
                candidate.revokedCapabilities.contains(capability)
            }
        }
    }
}
