package no.nav.helse.rapids_rivers

import io.ktor.server.engine.ApplicationEngine

@Deprecated("Bruk heller TestRapid: den trenger ingen ktor")
fun inMemoryRapid(config: InMemoryRapidConfig.() -> Unit) = InMemoryRapidConfig().apply(config).build()

@Deprecated("Bruk heller TestRapid: den trenger ingen ktor")
class InMemoryRapid(private val ktor: ApplicationEngine) : RapidsConnection() {
    private val messagesSendt = mutableListOf<RapidMessage>()
    val outgoingMessages get() = messagesSendt.toList()

    override fun publish(message: String): () -> Unit {
        messagesSendt.add(RapidMessage(null, message))
        return {  }
    }

    override fun publish(key: String, message: String): () -> Unit {
        messagesSendt.add(RapidMessage(key, message))
        return {  }
    }

    override fun start() {
        ktor.start(wait = false)
    }

    override fun stop() {
        ktor.stop(5000, 5000)
    }

    fun sendToListeners(message: String) {
        val context = object: MessageContext {
            override fun publish(message: String): () -> Unit {
                this@InMemoryRapid.publish(message)
                return {  }
            }

            override fun publish(key: String, message: String): () -> Unit {
                this@InMemoryRapid.publish(key, message)
                return {  }
            }
        }

        listeners.forEach { it.onMessage(message, context) }
    }

    data class RapidMessage(val key: String?, val value: String)
}

class InMemoryRapidConfig internal constructor() {
    private val ktor = KtorBuilder()

    fun ktor(config: KtorBuilder.() -> Unit) {
        ktor.config()
    }

    internal fun build() = InMemoryRapid(ktor.build())
}
