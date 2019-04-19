/*
 * ESP8266_MiLight_Hub driver
 *
 * Controls MiLight/LimitlessLED bulbs via the ESP8266 MiLight Hub by sidoh (https://github.com/sidoh/esp8266_milight_hub)
 * 
 */
metadata {
    definition(name: "LimitlessLED Light", namespace: "community", author: "cometfish") {
        capability "Actuator"
        capability "Switch"
		capability "SwitchLevel"
        capability "Light"
		
		attribute "nightMode", "boolean"
		attribute "level", "number"
		attribute "colourTemperature", "number" 
		
		
		
		command "nightMode"
		command "setColourTemperature", [[name: "ColourTemperature *", type: "NUMBER", description: "0 for Cool white, up to 100 for Warm white", constraints:[]]]
    }
}

preferences {
    section("URIs") {
        input "ipAddress", "text", title: "ESP8266 Hub IP Address", required: true
		input "hubID", "text", title: "Hub ID (eg. 0xFFFF)", required: true
        input "lightID", "text", title: "Light Group ID", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

def updated() {
    log.info "updated..."
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
}

def parse(String description) {
    if (logEnable) log.debug(description)
}

def on() {
    if (logEnable) log.debug "Sending on request"

    try {
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/cct/" + settings.lightID
		if (logEnable) log.debug url
        def postParams = [
            uri: url,
			contentType: "application/json",
			requestContentType: "application/json",
			body : ["status": "on"]
        ]
    
        httpPost(postParams) { resp ->
		    if (resp.success) {
                sendEvent(name: "switch", value: "on", isStateChange: true)
				sendEvent(name: "nightMode", value: "off", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to on failed: ${e.message}"
    }
}

def off() {
    if (logEnable) log.debug "Sending off request"

    try {
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/cct/" + settings.lightID
		if (logEnable) log.debug url
        def postParams = [
            uri: url,
			contentType: "application/json",
			requestContentType: "application/json",
            body : ["status": "off"]
        ]
    
        httpPost(postParams) { resp ->
		    if (resp.success) {
                sendEvent(name: "switch", value: "off", isStateChange: true)
				sendEvent(name: "nightMode", value: "off", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}

def nightMode() {
    if (logEnable) log.debug "Sending night mode request"

    try {
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/cct/" + settings.lightID
		if (logEnable) log.debug url
        def postParams = [
            uri: url,
			contentType: "application/json",
			requestContentType: "application/json",
            body : ["command": "night_mode" ]
        ]
		
        httpPost(postParams) { resp ->
		    if (resp.success) {
                sendEvent(name: "switch", value: "off", isStateChange: true)
				sendEvent(name: "nightMode", value: "on", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to nightmode failed: ${e.message}"
    }
}


def setLevel(level) {
    if (logEnable) log.debug "Sending level request"

    try {
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/cct/" + settings.lightID
		if (logEnable) log.debug url
        def postParams = [
            uri: url,
			contentType: "application/json",
			requestContentType: "application/json",
            body : ["level": level]
        ]
    
        httpPost(postParams) { resp ->
		    if (resp.success) {
                sendEvent(name: "level", value: level, isStateChange: true)
				
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to level failed: ${e.message} ${level} "
    }
}

def setColourTemperature(colourTemperature) {
    if (logEnable) log.debug "Sending colour temperature request"

    try {
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/cct/" + settings.lightID
		if (logEnable) log.debug url
        def postParams = [
            uri: url,
			contentType: "application/json",
			requestContentType: "application/json",
            body : ["temperature": colourTemperature]
        ]
    
        httpPost(postParams) { resp ->
		    if (resp.success) {
                sendEvent(name: "colourTemperature", value: colourTemperature, isStateChange: true)
				
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to colourTemperature failed: ${e.message} ${colourTemperature} "
    }
}