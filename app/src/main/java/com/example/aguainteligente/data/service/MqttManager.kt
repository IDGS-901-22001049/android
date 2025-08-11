package com.example.aguainteligente.data.service

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*
import javax.net.ssl.SSLSocketFactory

class MqttManager(userId: String?) {

    private var mqttClient: MqttClient? = null
    private val host = "96b5cb0dcdef4f3f8e03d057f1f44a73.s1.eu.hivemq.cloud"
    private val username = "hivemq.webclient.1754865091937"
    private val password = "lED892yJ1.CqW,oi&S!a"
    private val port = 8883
    private val brokerUri = "ssl://$host:$port"
    private val clientId = "AndroidClient_${userId ?: UUID.randomUUID().toString().substring(0, 8)}"

    val currentFlowRate = MutableStateFlow(0f)
    val isRaining = MutableStateFlow(false)
    val isHumidityHigh = MutableStateFlow(false)

    companion object {
        private const val FLOW_TOPIC = "agua_inteligente/sensores/flujo"
        private const val RAIN_TOPIC = "agua_inteligente/sensores/lluvia"
        private const val HUMIDITY_TOPIC = "agua_inteligente/sensores/humedad"
        private const val VALVE_CONTROL_TOPIC = "agua_inteligente/valvula/control"
    }

    fun connect() {
        try {
            mqttClient = MqttClient(brokerUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                userName = username
                this.password = this@MqttManager.password.toCharArray()
                socketFactory = SSLSocketFactory.getDefault()
            }

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT", "Connection lost: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.payload?.let { String(it) } ?: return
                    when (topic) {
                        FLOW_TOPIC -> currentFlowRate.value = payload.toFloatOrNull() ?: 0f
                        RAIN_TOPIC -> isRaining.value = payload == "1"
                        HUMIDITY_TOPIC -> isHumidityHigh.value = payload == "1"
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}

                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    Log.d("MQTT", "Connection complete")
                    subscribeToTopics()
                }
            })

            mqttClient?.connect(options)
            Log.d("MQTT", "Connecting to broker...")

        } catch (e: MqttException) {
            Log.e("MQTT", "Error connecting: ${e.message}", e)
        }
    }

    private fun subscribeToTopics() {
        try {
            mqttClient?.subscribe(FLOW_TOPIC, 0)
            mqttClient?.subscribe(RAIN_TOPIC, 0)
            mqttClient?.subscribe(HUMIDITY_TOPIC, 0)
            Log.d("MQTT", "Subscribed to topics")
        } catch (e: MqttException) {
            Log.e("MQTT", "Error subscribing: ${e.message}", e)
        }
    }

    fun sendValvePulse() {
        if (mqttClient?.isConnected == true) {
            try {
                val message = MqttMessage("TOGGLE".toByteArray())
                message.qos = 1
                mqttClient?.publish(VALVE_CONTROL_TOPIC, message)
                Log.d("MQTT", "Published to $VALVE_CONTROL_TOPIC: TOGGLE")
            } catch (e: MqttException) {
                Log.e("MQTT", "Error publishing: ${e.message}", e)
            }
        }
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
            Log.d("MQTT", "Disconnected")
        } catch (e: MqttException) {
            Log.e("MQTT", "Error disconnecting: ${e.message}", e)
        }
    }
}