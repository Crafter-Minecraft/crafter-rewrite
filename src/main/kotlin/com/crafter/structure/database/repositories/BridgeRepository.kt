package com.crafter.structure.database.repositories

import com.crafter.structure.database.api.Repository
import com.crafter.structure.database.Database.dbQuery
import com.crafter.structure.database.models.BridgeModel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object BridgeRepository : Repository() {
    private val model = BridgeModel

    override suspend fun upsert(data: Map<String, Any>): Unit = dbQuery {
        val isEnabled = data["isEnabled"] as Boolean

        model.upsert(model.guildId, onUpdate = listOf(
            model.bridgeEnabled to booleanLiteral(isEnabled),
            model.channelId to stringLiteral(data["channelId"].toString()),
        )) {
            it[guildId] = data["guildId"].toString()
            it[bridgeEnabled] = isEnabled
            it[channelId] = data["channelId"].toString()
        }
    }

    override suspend fun get(key: String): Map<String, Any>? = dbQuery {
        model.selectAll()
            .where { model.guildId eq key }
            .map { mapOf(
                "isEnabled" to it[model.bridgeEnabled],
                "channelId" to it[model.channelId]
            ) }
            .singleOrNull()
    }

    suspend fun isEnabled(guildId: String) = this.get(guildId)?.get("isEnabled") as? Boolean ?: false

    override suspend fun delete(key: String): Unit =
        dbQuery { model.deleteWhere { model.guildId eq key } }
}