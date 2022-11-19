package arguments.numbers

class RotNumber(val value: Double, val type: Type = Type.WORLD) {
	enum class Type {
		RELATIVE,
		WORLD
	}
	
	operator fun plus(other: RotNumber) = RotNumber(value + other.value, type)
	operator fun minus(other: RotNumber) = RotNumber(value - other.value, type)
	operator fun times(other: RotNumber) = RotNumber(value * other.value, type)
	operator fun div(other: RotNumber) = RotNumber(value / other.value, type)
	operator fun rem(other: RotNumber) = RotNumber(value % other.value, type)
	
	fun toRelative() = RotNumber(value, Type.RELATIVE)
	fun toWorld() = RotNumber(value, Type.WORLD)
	
	override fun toString() = when (type) {
		Type.RELATIVE -> "~${value.strUnlessZero}"
		Type.WORLD -> value.str
	}
}

val Number.rot get() = RotNumber(toDouble())
val Number.relativeRot get() = RotNumber(toDouble(), RotNumber.Type.RELATIVE)
fun rot(value: Number = 0, type: RotNumber.Type = RotNumber.Type.RELATIVE) = RotNumber(value.toDouble(), type)