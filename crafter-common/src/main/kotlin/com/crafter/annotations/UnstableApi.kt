package com.crafter.annotations

@RequiresOptIn(
    message = "This future is unstable and not recommended for use, wait for fixes / reworking",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class UnstableApi