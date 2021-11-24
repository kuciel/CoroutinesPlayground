package com.example.coroutinesplayground.coroutines

import kotlinx.coroutines.*

class CoroutinesRunner {
    fun runCoroutine() {
        runBlocking {
            launch {
                callAsync()
            }
            println("After launch")
        }
        println("After runBlockingDone")
    }

    suspend fun doDelay() {
        coroutineScope {
            val job = launch {
                delay(200)
                println("After 200 delay")
            }
            launch {
                job.join()
                delay(100)
                println("After 100 delay")
            }
            repeat(10) {
                launch {
                    delay(50)
                    println("After $it done")
                }
            }
        }
        println("After no delay")
    }

    private suspend fun jobCancellation() = coroutineScope {
        val job = launch {
            repeat(1000) { i ->
                delay(400)
                println("Cancellation test - job: sleeping $i")
            }
        }
        delay(1500)
        println("Cancellation cancel job")
        job.cancel()
        job.join()
        println("Cancellation after join")
    }

    private suspend fun jobCancellationComptuation() = coroutineScope {
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = System.currentTimeMillis()
            var i = 0
            try {
                while (i < 10 && isActive) {
                    if (System.currentTimeMillis() >= nextPrintTime) {
                        println("Cancellation iteration no: ${i++}")
                        nextPrintTime += 300L
                    }
                }
            } finally {
                withContext(NonCancellable) {
                    println("Cancellation calling finally")
                    delay(1000)
                    println("Cancellation calling finally after delay")
                }
            }
        }
        println("Cancellation before delay")
        delay(400)
        println("Cancellation before cancel and join")
        job.cancelAndJoin()
        println("Cancellation after cancel and join")

    }

    private suspend fun timeout() {
        withTimeout(300L) {
            repeat(10) { i ->
                println("I'm sleeping $i")
                delay(200L)
            }
        }
    }

    private suspend fun callAsync() = coroutineScope{
        println("Before sync sum")
        var sum = suspendFunA() + suspendFunB()
        println("After sync sum = $sum")

        val resA = async {suspendFunA()}
        val resB = async {suspendFunB()}
        sum = resA.await() + resB.await()
        println("After async sum = $sum")
    }

    private suspend fun suspendFunA(): Int {
        delay(100)
        return 10
    }

    private suspend fun suspendFunB(): Int {
        delay(200)
        return 20
    }

    suspend fun concurrentSum(): Int = coroutineScope {
        val one = async {suspendFunA()}
        val two = async {suspendFunB()}
        one.await()+two.await()
    }
}