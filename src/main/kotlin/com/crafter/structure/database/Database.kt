package com.crafter.structure.database

import com.crafter.Property
import com.crafter.structure.database.models.RCONModel
import com.crafter.discord.Initializable
import com.crafter.structure.database.models.BridgeModel
import com.crafter.structure.database.models.RCONRestrictModel
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database as ExposedDatabase

/**
  A database singleton.
 **/
object Database : Initializable {
    override fun initialize() {
        val driver = Property("storage.db.driverClassName").getString()
        val jdbcUrl = Property("storage.db.jdbcURL").getString()
        val username = Property("storage.db.username").getString()
        val password = Property("storage.db.password").getString()

        val database = ExposedDatabase.connect(
            jdbcUrl,
            driver,
            user = username,
            password = password
        )

        transaction(database) {
            SchemaUtils.setSchema(Schema("public"))

            register()
        }
    }

    private fun register() {
        SchemaUtils.create(RCONModel)
        SchemaUtils.create(BridgeModel)
        SchemaUtils.create(RCONRestrictModel)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}