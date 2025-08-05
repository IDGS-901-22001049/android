package com.example.aguainteligente.data.service

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*
import javax.net.ssl.SSLSocketFactory

class MqttManager(private val userId: String?) {

    private var mqttClient: MqttClient? = null
    private val host = "4425e671af564c3aa9b3929cfaaf1c5f.s1.eu.hivemq.cloud"
    private val username = "hivemq.webclient.1754189323882"
    private val password = "1LJA,02&3NOa!Ezg.xln"
    private val port = 8883
    private val brokerUri = "ssl://$host:$port"

    private val clientId = "AndroidClient_${userId ?: UUID.randomUUID().toString().substring(0, 8)}"

    private val _currentFlowLiters = MutableStateFlow(0f)
    val currentFlowLiters = _currentFlowLiters.asStateFlow()

    private val _rainDigital = MutableStateFlow(false)
    val rainDigital = _rainDigital.asStateFlow()

    private val _rainAnalog = MutableStateFlow(0)
    val rainAnalog = _rainAnalog.asStateFlow()

    private val _humidityDigital = MutableStateFlow(false)
    val humidityDigital = _humidityDigital.asStateFlow()

    private val _humidityAnalog = MutableStateFlow(0)
    val humidityAnalog = _humidityAnalog.asStateFlow()

    private val _leakAlert = MutableStateFlow(false)
    val leakAlert: StateFlow<Boolean> = _leakAlert

    companion object {
        private const val FLOW_TOPIC = "agua_inteligente/sensores/flujo"
        private const val RAIN_TOPIC = "agua_inteligente/sensores/lluvia"
        private const val HUMIDITY_TOPIC = "agua_inteligente/sensores/humedad"
        private const val VALVE_CONTROL_TOPIC = "agua_inteligente/valvula/control"
        private const val LEAK_ALERT_TOPIC = "agua_inteligente/alertas/fuga"
    }

    fun connect() {
        try {
            mqttClient = MqttClient(brokerUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions()
            options.isCleanSession = true
            options.userName = username
            options.password = password.toCharArray()
            options.socketFactory = SSLSocketFactory.getDefault()

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT", "Conexión MQTT perdida: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.let {
                        val payload = String(it.payload)
                        Log.d("MQTT", "Mensaje recibido - Tema: $topic, Mensaje: $payload")
                        when (topic) {
                            FLOW_TOPIC -> _currentFlowLiters.value = payload.toFloatOrNull() ?: 0f
                            RAIN_TOPIC -> {
                                val parts = payload.split(',')
                                if (parts.size == 2) {
                                    _rainDigital.value = parts[0] == "1"
                                    _rainAnalog.value = parts[1].toIntOrNull() ?: 0
                                } else {
                                    Log.w("MQTT", "Payload para $RAIN_TOPIC no tiene el formato esperado: $payload")
                                }
                            }
                            HUMIDITY_TOPIC -> {
                                val parts = payload.split(',')
                                if (parts.size == 2) {
                                    _humidityDigital.value = parts[0] == "1"
                                    _humidityAnalog.value = parts[1].toIntOrNull() ?: 0
                                } else {
                                    Log.w("MQTT", "Payload para $HUMIDITY_TOPIC no tiene el formato esperado: $payload")
                                }
                            }
                            LEAK_ALERT_TOPIC -> {
                                _leakAlert.value = payload == "FUGA_DETECTADA"
                            }
                            else -> Log.d("MQTT", "Mensaje recibido en tema no manejado: $topic")
                        }
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}

                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    Log.d("MQTT", "Conexión completa. ¿Reconexión? $reconnect")
                    subscribeToAllTopics()
                }
            })

            mqttClient?.connect(options)
            Log.d("MQTT", "Conectado al broker MQTT.")

        } catch (e: MqttException) {
            Log.e("MQTT", "Error al conectar al broker MQTT: ${e.message}", e)
        }
    }

    private fun subscribeToTopic(topic: String, qos: Int = 0) {
        if (mqttClient?.isConnected == true) {
            try {
                mqttClient?.subscribe(topic, qos)
                Log.d("MQTT", "Suscrito al tema: $topic")
            } catch (e: MqttException) {
                Log.e("MQTT", "Error al suscribirse al tema $topic: ${e.message}", e)
            }
        } else {
            Log.w("MQTT", "No conectado. No se puede suscribir a $topic")
        }
    }

    private fun subscribeToAllTopics() {
        subscribeToTopic(FLOW_TOPIC)
        subscribeToTopic(RAIN_TOPIC)
        subscribeToTopic(HUMIDITY_TOPIC)
        subscribeToTopic(LEAK_ALERT_TOPIC)
    }

    fun publishValveState(state: String) {
        if (mqttClient?.isConnected == true) {
            try {
                val message = MqttMessage(state.toByteArray())
                message.qos = 1
                message.isRetained = true
                mqttClient?.publish(VALVE_CONTROL_TOPIC, message)
                Log.d("MQTT", "Publicado en $VALVE_CONTROL_TOPIC: $state")
            } catch (e: MqttException) {
                Log.e("MQTT", "Error al publicar en $VALVE_CONTROL_TOPIC: ${e.message}", e)
            }
        }
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
            Log.d("MQTT", "Desconectado del broker MQTT.")
        } catch (e: MqttException) {
            Log.e("MQTT", "Error al desconectar del broker MQTT: ${e.message}", e)
        }
    }
}