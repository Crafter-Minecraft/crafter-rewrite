package com.crafter

import kotlinx.coroutines.*
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// Stolen from jda-ktx
// https://github.com/MinnDevelopment/jda-ktx/blob/master/src/main/kotlin/dev/minn/jda/ktx/events/CoroutineEventManager.kt#L43-L59
fun getDefaultScope(
    pool: Executor? = null,
    job: Job? = null,
    errorHandler: CoroutineExceptionHandler? = null,
    context: CoroutineContext = EmptyCoroutineContext
): CoroutineScope {
    val dispatcher = pool?.asCoroutineDispatcher() ?: Dispatchers.Default
    val parent = job ?: SupervisorJob()
    val handler = errorHandler ?: CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()

        if (throwable is Error) {
            parent.cancel()
            throw throwable
        }
    }
    return CoroutineScope(dispatcher + parent + handler + context)
}