package com.crafter.structure.database.repositories

import com.crafter.Encryption
import com.crafter.structure.database.Database.dbQuery
import com.crafter.structure.database.api.Repository
import com.crafter.structure.database.models.RCON
import com.crafter.structure.database.models.RCONModel
import com.crafter.structure.database.models.RCONModel.port
import com.crafter.structure.database.models.RCONRestrictModel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object RCONRepository : Repository<RCON>() {
    private val model = RCONModel

    override suspend fun upsert(data: RCON): Unit = dbQuery {
        val encryptedUserPassword = Encryption.encryptPassword(data.password)

        model.upsert(model.guildId, onUpdate = listOf(
            model.ipv4 to stringLiteral(data.ipv4),
            model.port to intLiteral(data.port),
            model.encryptedPassword to stringLiteral(encryptedUserPassword)
        )) {
            it[guildId] = data.guildId
            it[ipv4] = data.ipv4
            it[port] = data.port
            it[encryptedPassword] = encryptedUserPassword
        }
    }

    override suspend fun get(primaryKey: String): RCON? {
        val query = dbQuery {
            model.selectAll()
                .where { model.guildId eq primaryKey }
                .map { RCON(
                    it[model.guildId],
                    it[model.ipv4],
                    it[model.port],
                    it[model.encryptedPassword]
                ) }
                .singleOrNull()
        }

        return query
    }

    override suspend fun delete(primaryKey: String): Unit =
        dbQuery {
            model.deleteWhere { model.guildId eq primaryKey }
            model.deleteWhere { RCONRestrictModel.guildId eq primaryKey }
        }

    fun getRconPassword(password: String) = Encryption.decryptPassword(password)
}