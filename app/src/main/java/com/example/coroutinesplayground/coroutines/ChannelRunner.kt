package com.example.coroutinesplayground.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach

class ChannelRunner {


    fun runChannelSamples() {
        runBlocking {
            launchTickerChannel()
        }
    }

    private suspend fun sendReceiveChannel() {
        val channel = Channel<Int>()

        coroutineScope {
            launch {
                for (x in 1..5) {
                    channel.send(x)
                }
                channel.close()
            }
            while (!channel.isClosedForReceive) {
                println("Channels ${channel.receive()}")
            }
            println("Channels Done")
        }
    }

    private fun CoroutineScope.produceNumbers(): ReceiveChannel<Int> = produce {
        for (x in 1..5) {
            channel.send(x)
        }
    }

    private fun channelProducer() {
        runBlocking {
            val numbers = produceNumbers()
            numbers.consumeEach { println("Channels consumed $it") }
            println("Channels I'm Done")
        }
    }

    private fun CoroutineScope.infiniteProducer() = produce<Int> {
        var x = 1
        while (true) send(x++)
    }

    fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
        for (x in numbers) send(x * x)
    }

    private fun channelsPipeline() {
        runBlocking {
            val numbers = infiniteProducer()
            val squares = square(numbers)
            repeat(5) {
                println("Channels ${squares.receive()}")
            }
            coroutineContext.cancelChildren()
        }
    }

    private fun CoroutineScope.produceDelayedNumbers(): ReceiveChannel<Int> = produce {
        var x = 1
        while (true) {
            channel.send(x++)
            delay(100)
        }
    }

    fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
        for (msg in channel) {
            println("Channels Processor #$id received $msg")
        }
    }

    private fun runLaunchProcessor() {
        runBlocking {
            val delayedProducer = produceDelayedNumbers()
            repeat(5) { launchProcessor(it, delayedProducer) }
            delay(1000)
            delayedProducer.cancel()
        }
    }

    private suspend fun sendString(channel: SendChannel<String>, s: String) {
        while (true) {
            channel.send(s)
        }
    }

    private fun launchMultipleProducers() = runBlocking {

        val channel = Channel<String>()

        launch { sendString(channel, "First") }
        launch { sendString(channel, "Second") }
        repeat(15) {
            println("Channels ${channel.receive()}")
        }
        coroutineContext.cancelChildren()
    }

    private fun launchBufferedChannel() {
        runBlocking {
            val channel = Channel<Int>(3)
            val sender = launch {
                repeat(10) {
                    println("Channels sending $it")
                    channel.send(it)
                }
            }
            delay(300)
            sender.cancel()
            println("Channels done")
        }
    }

    private fun launchTickerChannel() = runBlocking {
        val tickerChannel = ticker(delayMillis = 100, initialDelayMillis = 0) // create ticker channel
        var nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Channels Initial element is available immediately: $nextElement") // no initial delay

        nextElement = withTimeoutOrNull(50) { tickerChannel.receive() } // all subsequent elements have 100ms delay
        println("Channels  Next element is not ready in 50 ms: $nextElement")

        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
        println("Channels  Next element is ready in 100 ms: $nextElement")

        // Emulate large consumption delays
        println("Channels  Consumer pauses for 150ms")
        delay(150)
        // Next element is available immediately
        nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Channels  Next element is available immediately after large consumer delay: $nextElement")
        // Note that the pause between `receive` calls is taken into account and next element arrives faster
        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
        println("Channels Next element is ready in 50ms after consumer pause in 150ms: $nextElement")

        tickerChannel.cancel() // indicate that no more elements are needed
    }
}