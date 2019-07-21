/*
 * ESP8266_MiLight_Hub remote driver
 *
 * Reflects commands from MiLight/LimitlessLED remotes via the ESP8266 MiLight Hub by sidoh (https://github.com/sidoh/esp8266_milight_hub)
 * so that you can use the remotes without pairing them to bulbs (ie. you can use them in Hubitat for any device, not just bulbs)
 * 
 */
metadata {
    definition(name: "MiLight Remote Button", namespace: "community", author: "cometfish") {
        capability "Initialize"
        capability "Actuator"
		capability "SwitchLevel"
        capability "PushableButton"
        capability "HoldableButton"
        
		attribute "level", "number"
        attribute "numberOfButtons", "number"
        attribute "pushed", "number"
        attribute "held", "number"
        
        command "disconnect"
    }
}

preferences {
    section("URIs") {
        input "ipAddress", "text", title: "ESP8266 Hub IP Address", required: true
        input name: "mqttBroker", type: "text", title: "MQTT broker IP:Port", required: true
        input name: "mqttClientID", type: "text", title: "MQTT Client ID", required: true, defaultValue: "hubitat_milight" 
	    input "hubID", "text", title: "Hub ID (eg. 0xFFFF)", required: true
        input "lightID", "text", title: "Light Group ID", required: true
        input "lightType", "text", title: "Light Type", required: true
        
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def installed() {
    log.warn "installed..."
}

def parse(String description) {
    //log.debug description
    mqtt = interfaces.mqtt.parseMessage(description)
	if (logEnable) log.debug mqtt
	//log.debug mqtt.topic
	json = new groovy.json.JsonSlurper().parseText(mqtt.payload)
	if (logEnable) log.debug json.state
    if (json.state=="OFF") {
        sendEvent(name: "pushed", value: 1, isStateChange:true)
    }
    else if (json.state=="ON") {
        sendEvent(name: "pushed", value: 2, isStateChange:true)
    }
    if (json.command=="night_mode") {
        sendEvent(name: "held", value: 1, isStateChange:true)
    }
    if (json.brightness!=null) {
        newLevel = Math.round(json.brightness/255.0*100.0)
        //don't allow 0 - turn off for 0, otherwise it can be confusing because the bulb looks off, but doesn't appear to respond to "on"
        if (newLevel<1)
           newLevel = 1
        if (newLevel>100)
           newLevel = 100
        sendEvent(name: "level", value: newLevel, isStateChange:true)
    }
}
def setLevel(level) {
    sendEvent(name: "level", value: level, isStateChange:true)
} 
def updated() {
    log.info "Updated"
    
    if (settings.lightType=="fut089")
        sendEvent(name: "numberOfButtons", value: 2, isStateChange:true)
    else
        sendEvent(name: "numberOfButtons", value: 10, isStateChange:true)
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
        mqttInt.subscribe("milightupdate/${settings.hubID}/${settings.lightType}/${settings.lightID}")
    } catch(e) {
        log.error "initialize error: ${e.message}"
    }
}

def mqttClientStatus(String status){
    if (logEnable) log.debug "mqttStatus: ${status}"
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