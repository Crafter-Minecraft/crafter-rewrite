package com.crafter.structure.database

import com.crafter.Property
import com.crafter.structure.database.models.RCONModel
import com.crafter.discord.Initializable
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
        val driver = Property("storage.driverClassName").getString()
        val jdbcUrl = Property("storage.jdbcURL").getString()
        val password = Property("storage.password").getString()

        val database = ExposedDatabase.connect(
            jdbcUrl,
            driver,
            user = "magmigo",
            password = password
        )

        transaction(database) {
            SchemaUtils.setSchema(Schema("public"))

            register()
        }
    }

    private fun register() {
        SchemaUtils.create(RCONModel)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}