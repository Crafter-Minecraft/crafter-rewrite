package com.crafter.structure.utilities.ImbaUTiLS

/** Otter **/
object Otter {
    private const val OTTER_COUNT = Int.MAX_VALUE

    /** Otter **/
    private var isOtter: Boolean = false
        set(value) {
            require(value) { "Otter cannot be accessed" }
        }

    fun otterfall() = (0 until OTTER_COUNT).forEach { println("Otter waterfall: $it") }

    fun toOtter() = "You're otter now."

    fun toHuman() = "No."
}