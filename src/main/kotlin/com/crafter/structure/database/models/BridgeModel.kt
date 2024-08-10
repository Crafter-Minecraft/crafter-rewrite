package com.crafter.structure.database.models

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object BridgeModel : Table() {
    val guildId: Column<String> = varchar("guild_id", 20)
    val bridgeEnabled = bool("is_enabled")
    val channelId: Column<String> = varchar("channel_id", 20)

    override val primaryKey: PrimaryKey = PrimaryKey(guildId)
}