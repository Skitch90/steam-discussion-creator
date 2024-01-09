package it.alesc.steam.db

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object DBUtils {
    fun configureDatabase() {
        Database.connect("jdbc:sqlite:/data/steam.db", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction {
            SchemaUtils.create(Posts)
        }
    }

    fun trackInsertedPost(appID: String, simulation: Boolean) {
        if (!simulation) {
            transaction {
                val exists = Posts.selectAll().where { Posts.appId eq appID.toInt() }.count() > 0L
                if (exists) {
                    Posts.update({ Posts.appId eq appID.toInt() }) {
                        it[lastPostInsertDate] = Clock.System.now()
                    }
                } else {
                    Posts.insert {
                        it[appId] = appID.toInt()
                        it[lastPostInsertDate] = Clock.System.now()
                    }
                }
            }
        }
    }
}