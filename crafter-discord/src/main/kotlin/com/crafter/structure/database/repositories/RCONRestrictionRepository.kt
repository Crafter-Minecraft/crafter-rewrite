package com.crafter.structure.database.repositories

import com.crafter.structure.database.Database.dbQuery
import com.crafter.structure.database.api.Repository
import com.crafter.structure.database.models.RCONRestrict
import com.crafter.structure.database.models.RCONRestrictModel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object RCONRestrictionRepository : Repository<RCONRestrict>() {
    private val model = RCONRestrictModel

    override suspend fun upsert(data: RCONRestrict): Unit = dbQuery {
        val dataGuildId = data.guildId
        val dataUserId = data.userId

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

    override suspend fun get(primaryKey: String): RCONRestrict? = dbQuery {
        model.selectAll()
            .where { model.guildId eq primaryKey }
            .map { RCONRestrict(it[model.guildId], it[model.userId]) }
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

    override suspend fun delete(primaryKey: String): Unit = dbQuery {
        model.deleteWhere { userId eq userId }
    }
}