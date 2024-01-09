package it.alesc.steam.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Posts: Table() {
    val appId: Column<Int> = integer("app_id")
    val lastPostInsertDate = timestamp("last_inserted_date")
    override val primaryKey = PrimaryKey(appId, name = "PK_Posts_AppId")
}