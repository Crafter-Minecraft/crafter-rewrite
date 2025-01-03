package com.crafter.structure.database.api

abstract class Repository<T> {
    abstract suspend fun upsert(data: T)
    abstract suspend fun get(primaryKey: String): T?
    abstract suspend fun delete(primaryKey: String)
}