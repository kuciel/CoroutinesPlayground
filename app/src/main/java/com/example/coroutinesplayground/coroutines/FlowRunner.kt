package com.example.coroutinesplayground.coroutines

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis

class FlowRunner {
    fun run() {
        runFlowWithDelayAndSlowCollector()
    }

    private fun runYielding() {
        println("Flows runSimple")
        yielding().forEach { println("Flows Value: $it") }
    }

    private fun yielding(): Sequence<Int> = sequence {
        for (i in 1..3) {
            Thread.sleep(100)
            yield(i)
        }
    }

    private fun counterFlowRunner() {
        runBlocking {
            launch {
                for (i in 1..5) {
                    println("Flows Thread is not blocked: $i")
                    delay(200)
                }
            }
            counterFlow().collect {
                println("Flows collected from counterFlow: $it")
            }
        }
    }

    private fun callCounterFlowTwice() {
        runBlocking {
            val flow = counterFlow()
            println("Flows calling counterFlow collect first time")
            flow.collect { value -> println("Flows Collected from flow: $value") }
            println("Flows calling coutnerFlow collect second time")
            flow.collect { value -> println("Flows Collected from flow second: $value") }
        }
    }

    private fun counterFlow(): Flow<Int> = flow {
        for (i in 1..5) {
            println("Flows Running flow,counting: $i")
            delay(100)
        }
    }

    private fun callCounterFlowWithTimeout() = runBlocking {
        withTimeoutOrNull(250) {
            counterFlow().collect { value -> println("Flow collected $value") }
        }
    }

    private suspend fun generateStringWithDelay(value: Int): String {
        delay(100)
        return "Flows value: $value"
    }

    private fun mapCounterFlow() {
        runBlocking {
            (1..3).asFlow()
                .map { value -> generateStringWithDelay(value) }
                .collect { valueString -> println("Flows collected $valueString") }
        }
    }

    private fun transformCounterFlow() {
        runBlocking {
            (1..3).asFlow()
                .transform { value ->
                    emit("flows transformImmediate response $value")
                    emit(generateStringWithDelay(value))
                }
                .take(4)
                .collect { value -> println("Flows collected value $value") }

        }
    }

    private fun reduceCounterFlow() {
        runBlocking {
            val reduceResult =
                (1..3).asFlow().reduce { a, b -> a + b }

            println("Flows reduce result = $reduceResult")

            val foldResult = (1..3).asFlow().fold(10) { a, b -> a + b }
            println("Flows fold result = $foldResult")
        }
    }

    private fun sequenceExecutionSample() {
        runBlocking {
            (1..5).asFlow()
                .filter {
                    println("Flows Filtering $it")
                    it > 0
                }
                .map {
                    println("Flows Mapping $it")
                    it + 1
                }
                .collect {
                    println("Flows collected $it")
                }
        }
    }

    private fun flowWithLog(): Flow<Int> = flow {
        runBlocking {
            Log.d("Flows", "$coroutineContext started flowWithLog")
            for (i in 1..3) {
                emit(i)
            }
        }
    }

    private fun runFlowWithLog() {
        runBlocking {
            flowWithLog()
                .flowOn(Dispatchers.IO)
                .collect { value -> Log.d("Flows", "$coroutineContext Collected $value") }
        }
    }

    private fun flowWithDelay(): Flow<Int> = flow {
        for (i in 1..5) {
            println("Flows $this@FlowRunners Emitting $i")
            emit(i)
            delay(100)
        }
    }

    private fun runFlowWithDelayAndSlowCollector() {
        val delayedFlow = flowWithDelay()
        runBlocking {
            val time = measureTimeMillis {
                launch {
                    delayedFlow
                        // .conflate()
                        .flowOn(Dispatchers.IO)
                        .collect { value ->
                            println("Flows first collector collected and delay $value")
                            delay(300)
                        }
                }
                launch {
                    delayedFlow
                        // .conflate()
                        .flowOn(Dispatchers.IO)
//                        .collect { value ->
//                            println("Flows second collector collected and delay $value")
//                            delay(300)
//                        }
                }
            }
            println("Flows overall execution time: $time")
        }
    }

    private fun zipFlows() {
        val numFlow = (1..6).asFlow().onEach { delay(100) }
        val strFlow = flowOf("One", "Two", "Three").onEach { delay(100) }
        runBlocking {
            numFlow.combine(strFlow) { a, b -> "Flows $a -> $b" }
                .collect { println(it) }
        }
    }

    private fun delayedFlow(i: Int): Flow<String> = flow {
        emit("Flows $i: First")
        delay(200)
        emit("Flows $i: Second")
    }

    private fun runDelayedFlow() {
        runBlocking {
            (1..3).asFlow()
                .onEach { delay(100) }
                .flatMapLatest { delayedFlow(it) }
                .collect {
                    println("Flows collected: $it")
                }
        }
    }

    private fun runExceptionCatch() {
        runBlocking {
            val simpleFlow: Flow<String> = flow {
                for (i in 1..3) {
                    println("Flows emitting $i")
                    //check(i <= 1)
                    emit("$i")

                }
            }
            try {
                simpleFlow
                    .onCompletion { cause -> println("Flows flow done: $cause") }
                    .collect { value ->
                        println("Flows value: $value")
                        check(value <= 1.toString())
                    }

            } finally {
                println("Flows Calling finally")
            }
        }
    }

    private fun events(): Flow<Int> = (1..10).asFlow().onCompletion { cause -> println("Flows flow done: $cause") }.onEach {
        coroutineContext.ensureActive()
        println("Flows emitting $it")
        delay(100)

    }

    private fun launchEvents() {
        runBlocking {
            events()
                .onEach {
                    println("Flows event $it") }
                .launchIn(this)
            println("Flows launching done")
        }
    }

    private fun runFlowAndCancel() = runBlocking{

        events()
            .collect {
            if(it == 3) cancel()
            println("Flows collected $it")
        }
    }


}