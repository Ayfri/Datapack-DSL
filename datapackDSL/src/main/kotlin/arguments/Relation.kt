package arguments

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Encoder
import serializers.LowercaseSerializer

@Serializable(Relation.Companion.RelationSerializer::class)
enum class Relation(val symbol: String) {
	LESS_THAN("<"),
	LESS_THAN_OR_EQUAL_TO("<="),
	EQUAL_TO("="),
	GREATER_THAN_OR_EQUAL_TO(">="),
	GREATER_THAN(">");
	
	companion object {
		val values = values()
		
		object RelationSerializer : KSerializer<Relation> by LowercaseSerializer(values) {
			override fun serialize(encoder: Encoder, value: Relation) {
				encoder.encodeString(value.symbol)
			}
		}
	}
}

class RelationBlock {
	infix fun Number.lessThan(other: Number) = Relation.LESS_THAN
	infix fun Number.lessThanOrEqual(other: Number) = Relation.LESS_THAN_OR_EQUAL_TO
	infix fun Number.equal(other: Number) = Relation.EQUAL_TO
	infix fun Number.greaterThanOrEqual(other: Number) = Relation.GREATER_THAN_OR_EQUAL_TO
	infix fun Number.greaterThan(other: Number) = Relation.GREATER_THAN
}
