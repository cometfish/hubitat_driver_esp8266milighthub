# Hubitat Driver for ESP8266 MiLight/LimitlessLED Hub
Add MiLight/LimitlessLED bulbs to Hubitat (requires the [ESP8266 MiLight Hub](https://github.com/sidoh/esp8266_milight_hub) by [sidoh](https://github.com/sidoh))

1. [Setup your ESP8266 MiLight Hub](https://blog.christophermullins.com/2017/02/11/milight-wifi-gateway-emulator-on-an-esp8266/), and make sure it has a fixed IP address
2. Add lightbulbdriver.groovy to your Hubitat as a new Driver (under `Drivers Code`)
3. Add a new device for each bulb group to your Hubitat, set device Type to your User driver of 'LimitlessLED Light'
4. Configure the Hubitat bulb device with the IP address of the hub, your hex Hub address (see the hub documentation for how to discover/set this), and your light Group ID (or `0` for the 'All' group)