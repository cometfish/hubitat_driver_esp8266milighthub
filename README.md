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