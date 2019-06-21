/*
 * ESP8266_MiLight_Hub driver
 *
 * Controls MiLight bulbs via the ESP8266 MiLight Hub by sidoh (https://github.com/sidoh/esp8266_milight_hub)
 * 
 */
metadata {
    definition(name: "MiLight Light", namespace: "community", author: "cometfish", importUrl: "https://raw.githubusercontent.com/cometfish/hubitat_driver_esp8266milighthub/master/lightbulbdriver.groovy") {
        capability "Actuator"
		capability "Bulb"
        capability "Switch"
		capability "SwitchLevel"
        capability "Light"
		capability "ColorControl"
		
		attribute "nightMode", "boolean"
		attribute "level", "number"
		attribute "colorTemperature", "number" 
		
		command "nightMode"
		command "setColorTemperature", [[name: "ColorTemperature *", type: "number", description: "3000 for Warm white, up to 6500 for Cool white", constraints:[]]]
    }
}

preferences {
    section("URIs") {
        input "ipAddress", "text", title: "ESP8266 Hub IP Address", required: true
	    input "hubID", "text", title: "Hub ID (eg. 0xFFFF)", required: true
        input "lightID", "text", title: "Light Group ID", required: true
        input "lightType", "text", title: "Light Type", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def hsvToRGB(float conversionHue = 0, float conversionSaturation = 100, float conversionValue = 100){
    // Accepts conversionHue (0-100), conversionSaturation (0-100), and conversionValue (0-100)
    // Returns RGB map ([ red: 0-255, green: 0-255, blue: 0-255 ])
    
    // Check HSV limits
    conversionHue > hueMax ? ( conversionHue = 1 ) : ( conversionHue < 0 ? ( conversionHue = 0 ) : ( conversionHue /= 100 ) )
    conversionSaturation > 100 ? ( conversionSaturation = 1 ) : ( conversionSaturation < 0 ? ( conversionSaturation = 0 ) : ( conversionSaturation /= 100 ) )
    conversionValue > 100 ? ( conversionValue = 1 ) : ( conversionValue < 0 ? ( conversionValue = 0 ) : ( conversionValue /= 100 ) ) 
        
    int h = (int)(conversionHue * 6);
    float f = conversionHue * 6 - h;
    float p = conversionValue * (1 - conversionSaturation);
    float q = conversionValue * (1 - f * conversionSaturation);
    float t = conversionValue * (1 - (1 - f) * conversionSaturation);
    
    conversionValue *= 255
    f *= 255
    p *= 255
    q *= 255
    t *= 255
            
    if      (h==0) { rgbMap = [red: conversionValue, green: t, blue: p] }
    else if (h==1) { rgbMap = [red: q, green: conversionValue, blue: p] }
    else if (h==2) { rgbMap = [red: p, green: conversionValue, blue: t] }
    else if (h==3) { rgbMap = [red: p, green: q, blue: conversionValue] }
    else if (h==4) { rgbMap = [red: t, green: p, blue: conversionValue] }
    else if (h==5) { rgbMap = [red: conversionValue, green: p,blue: q]  }
    else           { rgbMap = [red: 0, green: 0, blue: 0] }

    return rgbMap
}

def setColor(value){
    if (value.hue == null || value.saturation == null)
        return

    if (logEnable)
        log.warn "setColor"
    
	rgbColors = hsvToRGB(value.hue, value.saturation, value.level)
	
    if (logEnable)
        log.debug "hsvToRGB ${rgbColors}"

    try {
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/" + settings.lightType + "/" + settings.lightID
		if (logEnable) log.debug url
        def postParams = [
            uri: url,
			contentType: "application/json",
			requestContentType: "application/json",
			body : ["status": "on","color":["r":rgbColors['red'],"g":rgbColors['green'],"b":rgbColors['blue']]]
        ]

        if (logEnable)
		    log.debug "postParams ${postParams}"
    
        httpPost(postParams) { resp ->
		    if (resp.success) {
                sendEvent(name: "hue", value: value.hue, isStateChange: true)
				sendEvent(name: "saturation", value: value.saturation, isStateChange: true)
                if (value.level)
                    sendEvent(name: "level", value: value.level, isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to setColor failed: ${e.message}"
    }  
}

def updated() {
    log.info "updated..."
    log.warn "debug logging is: ${logEnable == true}"
}

def parse(String description) {
    if (logEnable) log.debug(description)
}

def on() {
    if (logEnable) log.debug "Sending on request"

    try {
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/"+settings.lightType+"/" + settings.lightID
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
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/"+settings.lightType+"/" + settings.lightID
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
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/"+settings.lightType+"/" + settings.lightID
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
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/"+settings.lightType+"/" + settings.lightID
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

def setColorTemperature(colorTemperature) {
    if (logEnable) log.debug "Sending color temperature request"

    //limitlessled bulbs range from 3000-6500K, 100 to 0
	if (colorTemperature<3000)
        colorTemperature = 3000
    else if (colorTemperature>6500)
	    colorTemperature = 6500
    lltemp = 100 - Math.round(((colorTemperature - 3000)/(6500-3000))* 100)
	if (logEnable) log.debug lltemp
    try {
		def url = "http://" + settings.ipAddress + "/gateways/" + settings.hubID + "/"+settings.lightType+"/" + settings.lightID
		if (logEnable) log.debug url
        def postParams = [
            uri: url,
			contentType: "application/json",
			requestContentType: "application/json",
            body : ["temperature": lltemp]
        ]
    
        httpPost(postParams) { resp ->
		    if (resp.success) {
                sendEvent(name: "colorTemperature", value: colorTemperature, isStateChange: true)
				
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to colorTemperature failed: ${e.message} ${colorTemperature} "
    }
}
