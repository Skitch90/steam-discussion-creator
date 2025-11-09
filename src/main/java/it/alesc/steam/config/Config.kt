package it.alesc.steam.config

data class Config(val db: DB, val credentials: Credentials, val discussion: Discussion, val apps: List<String>)
data class Credentials(val username: String, val password: String)
data class Discussion(val simulation:Boolean = false, val author:String, val title: String, val text: String)
data class DB(val url: String)