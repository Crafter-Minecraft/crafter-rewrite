package com.crafter.structure.database.repositories

import com.crafter.structure.database.Database.dbQuery
import com.crafter.structure.database.api.Repository
import com.crafter.structure.database.models.RCONRestrictModel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object RCONRestrictionRepository : Repository() {
    private val model = RCONRestrictModel

    override suspend fun upsert(data: Map<String, Any>): Unit = dbQuery {
        val dataGuildId = data["guildId"].toString()
        val dataUserId = data["userId"].toString()

        if (!isUserExists(dataGuildId, dataUserId)) {
            model.insert {
                it[guildId] = dataGuildId
                it[userId] = dataUserId
            }
        } else {
            model.upsert(model.guildId, onUpdate = listOf(
                model.userId to stringLiteral(dataUserId)
            )) {
                it[guildId] = dataGuildId
                it[userId] = dataUserId
            }
        }
    }

    override suspend fun get(key: String): Map<String, Any>? = dbQuery {
        model.selectAll()
            .where { model.guildId eq key }
            .map { mapOf("guildId" to it[model.guildId], "userId" to it[model.userId]) }
            .singleOrNull()
    }

    suspend fun isUserExists(guildId: String, userId: String): Boolean = dbQuery {
        val data = model.selectAll()
            .where { Op.build { model.guildId eq guildId }.and { model.userId eq userId } }
            .map { it[model.userId] }
        return@dbQuery data.isNotEmpty()
    }

    suspend fun deleteUser(guildId: String, userId: String) = dbQuery {
        model.deleteWhere { Op.build { model.guildId eq guildId }.and { RCONRestrictModel.userId eq userId } }
    }

    override suspend fun delete(key: String): Unit = dbQuery {
        model.deleteWhere { userId eq userId }
    }
}