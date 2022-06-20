package com.ashikvetrivelu.pitchtuner.aspect

import com.google.common.base.Stopwatch
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

@Aspect
class LoggingAspect {

    @Pointcut("execution(@PerfLogMonitor * *.*(..)")
    fun annotatedMethod() {}

    @Around("annotatedMethod()")
    fun logTimingStat(joinPoint: ProceedingJoinPoint) {
        val stopWatch: Stopwatch = Stopwatch.createStarted()
        val logger = LoggerFactory.getLogger(joinPoint.target.javaClass)
        val methodName = joinPoint.signature.declaringTypeName
        joinPoint.proceed()
        val elapsed = stopWatch.elapsed(TimeUnit.NANOSECONDS)
        logger.info("PerfStatMonitor elapsed={}ms method={}", elapsed, methodName)

    }

}