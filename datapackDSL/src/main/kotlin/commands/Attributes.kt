package commands

import arguments.*
import functions.Function
import kotlinx.serialization.Serializable
import serializers.LowercaseSerializer
import java.util.*

@Serializable(AttributeModifierOperation.Companion.AttributeModifierOperationSerializer::class)
enum class AttributeModifierOperation {
	ADD,
	MULTIPLY,
	MULTIPLY_BASE;

	companion object {
		val values = values()

		object AttributeModifierOperationSerializer : LowercaseSerializer<AttributeModifierOperation>(values)
	}
}

class AttributeBase(private val fn: Function, private val target: Argument.Selector, private val attribute: Argument.Attribute) {
	fun get() = fn.addLine(command("attribute", target, attribute, literal("base"), literal("get")))
	fun set(value: Double) = fn.addLine(command("attribute", target, attribute, literal("base"), literal("set"), float(value)))
}

class AttributeModifiers(private val fn: Function, private val target: Argument.Selector, private val attribute: Argument.Attribute) {
	fun add(id: UUID, name: String, value: Double, operation: AttributeModifierOperation) =
		fn.addLine(command("attribute", target, attribute, literal("modifier"), literal("add"), uuid(id), literal(name), float(value), literal(operation.asArg())))

	fun add(name: String, value: Double, operation: AttributeModifierOperation) =
		fn.addLine(command("attribute", target, attribute, literal("modifier"), literal("add"), uuid(UUID.randomUUID()), literal(name), float(value), literal(operation.asArg())))

	fun get(id: UUID, scale: Double? = null) = fn.addLine(command("attribute", target, attribute, literal("modifier"), literal("value"), literal("get"), uuid(id), float(scale)))
	fun remove(id: UUID) = fn.addLine(command("attribute", target, attribute, literal("modifier"), literal("remove"), uuid(id)))
}

class Attribute(private val fn: Function, private val target: Argument.Selector, private val attribute: Argument.Attribute) {
	val base = AttributeBase(fn, target, attribute)
	fun base(block: AttributeBase.() -> Command) = base.run(block)

	val modifiers = AttributeModifiers(fn, target, attribute)
	fun modifiers(block: AttributeModifiers.() -> Command) = modifiers.run(block)

	fun get(scale: Double? = null) = fn.addLine(command("attribute", target, attribute, literal("get"), float(scale)))
}

class AttributeTarget(private val fn: Function, private val target: Argument.Selector) {
	fun get(attribute: generated.Attributes) = Attribute(fn, target, attribute(attribute))
	fun get(attribute: String) = Attribute(fn, target, attribute(attribute))
}

class Attributes(private val fn: Function) {
	fun get(target: Argument.Selector, attribute: generated.Attributes) = Attribute(fn, target, attribute(attribute))
	fun get(target: Argument.Selector, block: AttributeTarget.() -> Command) = AttributeTarget(fn, target).run(block)
}

fun Function.attributes(block: Attributes.() -> Command) = Attributes(this).run(block)
fun Function.attributes(target: Argument.Selector, block: AttributeTarget.() -> Unit) = AttributeTarget(this, target).run(block)
fun Function.attributes(target: Argument.Selector, attribute: generated.Attributes, block: Attribute.() -> Unit) =
	Attribute(this, target, attribute(attribute)).run(block)

fun Function.attributes(target: Argument.Selector, attribute: String, block: Attribute.() -> Unit) = Attribute(this, target, attribute(attribute)).run(block)
