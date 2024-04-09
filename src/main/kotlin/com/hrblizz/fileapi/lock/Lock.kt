package com.hrblizz.fileapi.lock

/**
 * Annotation for locking the method execution based on a specified key using Redis-based distributed locking.
 *
 *
 * The @Lock annotation is used to ensure that a method is executed exclusively
 * across multiple instances or threads for a given key value. It prevents concurrent
 * execution of the annotated method for the same key in a distributed environment.
 *
 *
 * The key is specified using the 'key' attribute, which supports the following format:
 *
 *  * Static string value: The key should be enclosed in single quotes (e.g., 'myLockKey').
 *
 *
 *
 * Example:
 * <pre>
 * &#064;Lock(key = "'myStaticLockKey'")
 * public void processAccount() {
 * // Method body
 * }
</pre> *
 * In this example, the 'key' is set to "'myStaticLockKey'", which means the lock will be
 * based on the static string value "myStaticLockKey".
 *
 *
 * The @Lock annotation uses Redis as the distributed locking mechanism. It leverages the
 * atomic operations provided by Redis to acquire and release locks across multiple instances
 * or threads. The lock is acquired using the Redis 'SET' command with the 'NX' and 'EX' options,
 * ensuring that only one instance or thread can acquire the lock for a specific key at a time.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class Lock(
    /**
     * The key to lock the method execution.
     *
     *
     * The key supports the following format:
     *
     *  * Static string value: The key should be enclosed in single quotes (e.g., 'myLockKey').
     *
     *
     * @return the key or SpEL expression for the key
     */
    val key: String,
    /**
     * The timeout value for the lock in milliseconds.
     *
     *
     * Specifies the maximum time the lock should be held before automatically releasing it.
     * Default value is 60,000 milliseconds (60 seconds).
     *
     * @return the timeout value in milliseconds
     */
    val timeoutMillis: Long = 60000,
    /**
     * Determines whether lock acquisition should be retried if it fails.
     *
     *
     * If set to 'true' (default), the method will attempt to acquire the lock multiple times
     * before giving up. If set to 'false', the method will immediately throw an exception if
     * the lock cannot be acquired.
     *
     * @return true if lock acquisition should be retried, false otherwise
     */
    val retry: Boolean = true
)
