package com.crafter.structure.database.models

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

data class RCONRestrict(val guildId: String, val userId: String)

object RCONRestrictModel : Table() {
    val guildId: Column<String> = varchar("guild_id", 20)
    val userId: Column<String> = varchar("user_id", 20)
}