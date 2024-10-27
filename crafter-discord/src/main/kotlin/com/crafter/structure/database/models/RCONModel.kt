package com.crafter.structure.database.models

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object RCONModel : Table() {
    val guildId: Column<String> = varchar("guild_id", 20)
    val ipv4: Column<String> = varchar("ip", 20).uniqueIndex()
    val port: Column<Int> = integer("port").uniqueIndex()
    val encryptedPassword: Column<String> = varchar("encrypted_password", 255)

    override val primaryKey: PrimaryKey = PrimaryKey(guildId)
}