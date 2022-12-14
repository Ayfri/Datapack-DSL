package commands

import arguments.literal
import arguments.numbers.TimeNumber
import arguments.time
import functions.Function

class ScheduleFunction(private val fn: Function, val function: String) {
	fun append(time: TimeNumber) = fn.addLine(command("schedule", literal("function"), literal(function), time(time)))
	fun clear() = fn.addLine(command("schedule", literal("clear"), literal(function)))
	fun replace(time: TimeNumber) = fn.addLine(command("schedule", literal("function"), literal(function), time(time)))
}

class Schedule(private val fn: Function) {
	fun append(function: String, time: TimeNumber) = fn.addLine(command("schedule", literal("function"), literal(function), time(time), literal("append")))
	fun clear(function: String) = fn.addLine(command("schedule", literal("clear"), literal(function)))
	fun replace(function: String, time: TimeNumber) = fn.addLine(command("schedule", literal("function"), literal(function), time(time), literal("replace")))
}

val Function.schedules get() = Schedule(this)
fun Function.schedules(block: Schedule.() -> Command) = Schedule(this).block()
fun Function.schedule(function: String) = ScheduleFunction(this, function)
