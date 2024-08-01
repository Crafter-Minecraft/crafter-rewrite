package com.crafter.structure.database.repositories

import com.crafter.structure.database.Database.dbQuery
import com.crafter.structure.database.api.Repository
import com.crafter.structure.database.models.RCONModel
import com.crafter.structure.utilities.Encryption
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


object RCONRepository : Repository() {
    private val model = RCONModel
    val encryption = Encryption()

    override suspend fun upsert(data: Map<String, Any>): Unit = dbQuery {
        val encryptedUserPassword = encryption.encryptPassword(data["password"].toString())

        model.upsert(model.guildId, onUpdate = listOf(
            model.ipv4 to stringLiteral(data["ip"].toString()),
            model.port to intLiteral(data["port"] as Int),
            model.encryptedPassword to stringLiteral(encryptedUserPassword)
        )) {
            it[guildId] = data["guild_id"].toString()
            it[ipv4] = data["ip"].toString()
            it[port] = data["port"] as Int
            it[encryptedPassword] = encryptedUserPassword
        }
    }

    override suspend fun get(key: String): Map<String, Any>? {
        val query = dbQuery {
            model.selectAll()
                .where { model.guildId eq key }
                .map { mapOf(
                    "ip" to it[model.ipv4],
                    "port" to it[model.port],
                    "password" to it[model.encryptedPassword]
                ) }
                .singleOrNull()
        }

        return query
    }

    override suspend fun delete(key: String): Unit = dbQuery {
        model.deleteWhere { model.guildId eq key }
    }

    fun getRconPassword(password: String) = encryption.decryptPassword(password)
}