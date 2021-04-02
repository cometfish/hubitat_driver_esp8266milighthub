/*
 * ESP8266_MiLight_Hub presence
 *
 * Monitors the status of the ESP8266 MiLight Hub by sidoh - requires v1.9.0+, and an MQTT broker (https://github.com/sidoh/esp8266_milight_hub)
 * Presence will update to 'not present' if the broker reports that the hub has gone offline.
 * 
 */
metadata {
    definition (name: "MiLight Gateway", namespace: "community", author: "cometfish", importUrl: "https://raw.githubusercontent.com/cometfish/hubitat_driver_esp8266milighthub/master/hubdriver.groovy") {
        capability "Initialize"
        capability "PresenceSensor"
        
        command "disconnect"
		
		attribute "presence", "enum", ["present", "not present"]
    }
}

preferences {
    section("URIs") {
        input "mqttBroker", "text", title: "MQTT Broker Address", required: true
		input "mqttTopic", "text", title: "MQTT Topic", required: true
        input name: "mqttClientID", type: "text", title: "MQTT Client ID", required: true, defaultValue: "hubitat_milight" 
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def installed() {
    log.warn "installed..."
}

def parse(String description) {
    if (logEnable) log.debug description
	
    mqtt = interfaces.mqtt.parseMessage(description)
	if (logEnable) log.debug mqtt
	if (logEnable) log.debug mqtt.topic
	
	json = new groovy.json.JsonSlurper().parseText(mqtt.payload)
	if (logEnable) log.debug json
	
	if (json.status == 'connected' && device.currentState("presence").value!="present") 
	    sendEvent(name: "presence", value: "present", isStateChange: true)
	else if (json.status.contains('disconnected') && device.currentState("presence").value!="not present")
		sendEvent(name: "presence", value: "not present", isStateChange: true)
	state.version = json.version
	state.ipAddress = json.ip_address
	state.last_reset_reason = json.reset_reason
}

def updated() {
    log.info "updated..."
    initialize()
}

def disconnect() {
	if (logEnable) log.info "disconnecting from mqtt"
    interfaces.mqtt.disconnect()
    state.connected = false
}

def uninstalled() {
    disconnect() 
}

def initialize() {
    try {
        //open connection
        def mqttInt = interfaces.mqtt
        mqttInt.connect("tcp://" + settings.mqttBroker, settings.mqttClientID, null, null)
        //give it a chance to start
        pauseExecution(1000)
        if (logEnable) log.info "connection established"
        mqttInt.subscribe(settings.mqttTopic)
    } catch(e) {
        log.debug "initialize error: ${e.message}"
    }
}

def mqttClientStatus(String status){
    log.debug "mqttStatus- error: ${status}"
    switch (status) {
        case "Status: Connection succeeded":
            state.connected = true
            break
        case "disconnected":
            //note: this is NOT called when we deliberately disconnect, only on unexpected disconnect
            state.connected = false
            //try to reconnect after a small wait (so the broker being down doesn't send us into an endless loop of trying to reconnect and lock up the hub)
            runIn(5, initialize)
            break
        default:
            log.info status
            break
    }
}