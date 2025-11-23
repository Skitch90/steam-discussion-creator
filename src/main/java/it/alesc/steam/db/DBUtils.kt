package it.alesc.steam.db

import it.alesc.steam.config.DB
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

object DBUtils {
    fun configureDatabase(db: DB) {
        Database.connect(db.url, "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction {
            SchemaUtils.create(Posts)
        }
    }

    fun retrieveAppsToUpdate(appIdList: List<Int>): List<String> {
        return transaction {
            Posts.selectAll()
                .where { Posts.appId inList appIdList }
                .andWhere { Posts.lastPostInsertDate less Clock.System.now().minus(14.days) }
                .map { it[Posts.appId].toString() }
        }
    }

    fun retrieveAppsToInsert(appIdList: List<Int>): List<String> {
        val appIds = transaction {
            Posts.selectAll().map { it[Posts.appId] }
        }
        return appIdList.subtract(appIds.toSet()).map { it.toString() }
    }

    fun postInsertedRecently(appID: String): Boolean {
        return transaction {
            val timestamp = Posts.select(Posts.lastPostInsertDate)
                .where { Posts.appId eq appID.toInt() }
                .map { it[Posts.lastPostInsertDate] }
                .firstOrNull()
            return@transaction timestamp?.let { it.plus(1.hours) > Clock.System.now() } ?: false
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