package com.crafter.structure.database.models

import org.jetbrains.exposed.sql.Table

object RCONRestrictModel : Table() {
    val guildId = varchar("guild_id", 20)
    val userId = varchar("user_id", 20)
}