/*
 * ESP8266_MiLight_Hub presence
 *
 * Monitors the status of the ESP8266 MiLight Hub by sidoh - requires v1.9.0+, and an MQTT broker (https://github.com/sidoh/esp8266_milight_hub)
 * Presence will update to 'not present' if the broker reports that the hub has gone offline.
 * 
 */
metadata {
    definition (name: "MiLight Gateway", namespace: "community", author: "cometfish", importUrl: "https://raw.githubusercontent.com/cometfish/hubitat_driver_esp8266milighthub/master/hubdriver.groovy") {
        capability "PresenceSensor"
		
		attribute "presence", "enum", ["present", "not present"]
    }
}

preferences {
    section("URIs") {
        input "mqttBroker", "string", title: "MQTT Broker Address", required: true
		input "mqttTopic", "string", title: "MQTT Topic", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

import static hubitat.helper.InterfaceUtils.alphaV1mqttConnect
import static hubitat.helper.InterfaceUtils.alphaV1mqttDisconnect
import static hubitat.helper.InterfaceUtils.alphaV1mqttSubscribe
import static hubitat.helper.InterfaceUtils.alphaV1parseMqttMessage

def installed() {
    log.warn "installed..."
}

def parse(String description) {
    if (logEnable) log.debug description
	
    mqtt = alphaV1parseMqttMessage(description)
	if (logEnable) log.debug mqtt
	if (logEnable) log.debug mqtt.topic
	
	json = new groovy.json.JsonSlurper().parseText(mqtt.payload)
	if (logEnable) log.debug json
	
	if (json.status == 'connected') 
	    sendEvent(name: "presence", value: "present", isStateChange: true)
	else if (json.status.contains('disconnected'))
		sendEvent(name: "presence", value: "not present", isStateChange: true)
	state.version = json.version
	state.ipAddress = json.ip_address
	state.last_reset_reason = json.reset_reason
}

def updated() {
    log.info "updated..."
    initialize()
}

def uninstalled() {
    log.info "disconnecting from mqtt"
    alphaV1mqttDisconnect(device)
}

def initialize() {
    try {
        //open connection
        alphaV1mqttConnect(device, "tcp://" + settings.mqttBroker, "hubitat_milighthub", null, null)
        //give it a chance to start
        pauseExecution(1000)
        if (logEnable) log.info "connection established"
        alphaV1mqttSubscribe(device, settings.mqttTopic)
    } catch(e) {
        log.debug "initialize error: ${e.message}"
    }
}

def mqttClientStatus(String status){
    log.debug "mqttStatus- error: ${status}"
}