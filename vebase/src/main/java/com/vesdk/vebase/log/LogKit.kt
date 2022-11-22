package com.vesdk.vebase.log

/**
 *  author : lijunguan
 *  date : 2022/6/1 3:15 下午
 *  description :
 */

object LogKit : LogKitProtocol {

    @get:[JvmStatic JvmName("treeCount")]
    val treeCount
        get() = loggerArray.size
    private val loggers = ArrayList<AbsLogger>()

    @Volatile
    private var loggerArray = emptyArray<AbsLogger>()

    override fun v(tag: String?, message: String) {
        loggerArray.forEach { logger -> logger.v(tag, message) }
    }

    override fun d(tag: String?, message: String) {
        loggerArray.forEach { logger -> logger.d(tag, message) }
    }

    override fun i(tag: String?, message: String) {
        loggerArray.forEach { logger -> logger.i(tag, message) }
    }

    override fun w(tag: String?, message: String) {
        loggerArray.forEach { logger -> logger.w(tag, message) }
    }

    override fun e(tag: String?, message: String, t: Throwable?) {
        loggerArray.forEach { logger -> logger.e(tag, message, t) }
    }

    /**
     * 外部注入日志实现，支持注入多个实现，eg: Debug: SyslogImp, FileLogImp,  Release: FileLogImp
     */
    fun inject(logger: AbsLogger) {
        synchronized(loggers) {
            loggers.add(logger)
            loggerArray = loggers.toTypedArray()
        }
    }

    fun dropAllImpl() {
        synchronized(loggers) {
            loggers.clear()
            loggerArray = emptyArray()
        }
    }
}

interface LogKitProtocol {

    fun v(tag: String?, message: String)

    fun d(tag: String?, message: String)

    fun i(tag: String?, message: String)

    fun w(tag: String?, message: String)

    fun e(tag: String?, message: String, t: Throwable?)

}

enum class LogPriority {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
}















