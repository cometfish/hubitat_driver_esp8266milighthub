# Hubitat Driver for ESP8266 MiLight/LimitlessLED Hub
Add MiLight/LimitlessLED bulbs to Hubitat (requires the [ESP8266 MiLight Hub](https://github.com/sidoh/esp8266_milight_hub) by [sidoh](https://github.com/sidoh))

1. [Setup your ESP8266 MiLight Hub](https://blog.christophermullins.com/2017/02/11/milight-wifi-gateway-emulator-on-an-esp8266/), and make sure it has a fixed IP address
2. Add `lightbulbdriver.groovy` to your Hubitat as a new Driver (under `Drivers Code`)
3. Add a new device for each bulb group to your Hubitat, set device Type to your User driver of 'MiLight-LimitlessLED Light'
4. Configure the Hubitat bulb device with:
    1. the IP address of the hub
    2. your hex Hub address (see the hub documentation for how to discover/set this)
    3. your light type (`cct` for a White-only bulb, `rgb` for an RGB bulb, or `rgb_cct` for an RGBW bulb)
    4. your light Group ID (or `0` for the 'All' group / `0` for an RGB bulb)

You can also monitor the status of the MiLight hub (online/offline) if your hub is version 1.9.0 or higher and you have an MQTT broker:
1. Configure your ESP8266 MiLight Hub for MQTT:
    1. Enter your MQTT broker address under `MQTT server`
    2. Enter your desired topic under `MQTT Client Status Topic`, eg. `milight/status`
2. Add `hubdriver.groovy` to your Hubitat as a new Driver (under `Drivers Code`)
3. Add a new device to your Hubitat, set device Type to your User driver of 'MiLight-LimitlessLED Gateway'
4. Configure the new Hubitat MiLight Gateway device with:
    1. the MQTT broker address of the hub ip:port (eg. `127.0.0.1:1883`)
    2. the MQTT topic you chose earlier (eg. `milight/status`)
5. Save your settings - if they're correct and the hub is online, refreshing the device page will show the hub as `present` as well as listing its IP address and firmware version