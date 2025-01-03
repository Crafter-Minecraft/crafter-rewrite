package com.crafter.structure.database.repositories

import com.crafter.structure.database.api.Repository
import com.crafter.structure.database.Database.dbQuery
import com.crafter.structure.database.models.Bridge
import com.crafter.structure.database.models.BridgeModel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object BridgeRepository : Repository<Bridge>() {
    private val model = BridgeModel

    override suspend fun upsert(data: Bridge): Unit = dbQuery {
        model.upsert(model.guildId, onUpdate = listOf(
            model.bridgeEnabled to booleanLiteral(data.bridgeEnable),
            model.channelId to stringLiteral(data.channelId),
        )) {
            it[guildId] = data.guildId
            it[bridgeEnabled] = data.bridgeEnable
            it[channelId] = data.channelId
        }
    }

    override suspend fun get(primaryKey: String): Bridge? = dbQuery {
        model.selectAll()
            .where { model.guildId eq primaryKey }
            .map {
                Bridge(it[model.guildId], it[model.bridgeEnabled], it[model.channelId])
            }
            .singleOrNull()
    }

    suspend fun isEnabled(guildId: String): Boolean =
        get(guildId)?.bridgeEnable ?: false

    override suspend fun delete(primaryKey: String): Unit =
        dbQuery { model.deleteWhere { model.guildId eq primaryKey } }
}