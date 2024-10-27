package com.crafter.structure.database.api

abstract class Repository {
    abstract suspend fun upsert(data: Map<String, Any>)
    abstract suspend fun get(key: String): Map<String, Any>?
    abstract suspend fun delete(key: String)
}