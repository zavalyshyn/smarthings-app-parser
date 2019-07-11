/**
 *  Motion based thermostat
 *
 *  Copyright 2014 RBoy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Motion based thermostat",
    namespace: "rboy",
    author: "RBoy",
    description: "Motion sensor based thermostat settings. This thermostat has 2 states, with someone in the room (motion) and room empty (no motion). You can schedule it to work during specific times and during specifics days of the week",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@3x.png")

/**
 *  Automatic HVAC Program
 *
 *  Author: RBoy
 *  Change log:
 *	2015-1-1 -> Added support for multiple sensor selection and default value for timeout
 *	2014-11-24 -> Added support for timeout configuration if there is no motion
 */
preferences {

	section("Choose thermostat ") {
		input "thermostat", "capability.thermostat"
	}

	section("Choose Motion Sensor(s)") {
		input "motionSensor", "capability.motionSensor", multiple: true
	}
    
    section("Switch HVAC mode (auto to cool/heat) based on the outside temperature (optional)") {
		input "temperatureSensor", "capability.temperatureMeasurement", required: false
		input "temperatureH", "number", title: "Switch to heating temperature", required: false, description: "Temperature below which switch to heat mode"
		input "temperatureC", "number", title: "Switch to cooling temperature", required: false, description: "Temperature above which switch to cool mode"
	}

    section("Set operating mode temperatures") {
        input "opHeatSet", "decimal", title: "When Heating", description: "Heating temperature when motion is detected"
        input "opCoolSet", "decimal", title: "When Cooling", description: "Cooling temperature when motion is detected"
    }

    section("Set idle mode temperatures") {
        input "idHeatSet", "decimal", title: "When Heating", description: "Heating temperature when idle is detected"
        input "idCoolSet", "decimal", title: "When Cooling", description: "Cooling temperature when idle is detected"
    }

    section("Set delay while switching from operating to idle mode (no motion detected)") {
        input "idleTimeout", "number", title: "Time in Minutes (0 for immediate)", defaultValue: 15
    }

	section("Select the operating mode time and days (optional)") {
		input "startTime", "time", title: "Start Time", required: false
		input "endTime", "time", title: "End Time", required: false
        input "dayOfWeek", "enum",
                title: "Which day of the week?",
                required: false,
                multiple: true,
                metadata: [
                    values: [
                        'All Week',
                        'Monday to Friday',
                        'Saturday & Sunday',
                        'Monday',
                        'Tuesday',
                        'Wednesday',
                        'Thursday',
                        'Friday',
                        'Saturday',
                        'Sunday'
                        ]
                    ],
                defaultValue: 'All Week'
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	unschedule() // clear any pending timers
	initialize()
}

def initialize() {
	subscribe(temperatureSensor, "temperature", temperatureHandler)
    subscribe(motionSensor, "motion.active", activeMotionHandler)
    subscribe(motionSensor, "motion.inactive", inactiveMotionHandler)
}

// This section sets the HVAC mode based outside temperature. HVAC fan mode is set to "auto".
def temperatureHandler(evt) {
	log.debug "Heat mode switch temperature $temperatureH, cool mode switch temperature $temperatureC"
    
	if (temperatureH == null || temperatureC == null) { // We are in Auto mode or user doesn't want us to switch modes
		return
	}
	
    def extTemp = temperatureSensor.currentTemperature
	log.debug "External temperature is: $extTemp"
	def thermostatState = thermostat.currentThermostatMode
	def thermostatFan = thermostat.currentThermostatFanMode
	log.debug "HVAC current mode $thermostatState"
	log.debug "HVAC Fan current mode $thermostatFan"
	if (extTemp < temperatureH) {
		if (thermostatState == "cool") {
			def hvacmode = "heat"
			thermostat.setThermostatMode(hvacmode)
			log.debug "HVAC mode set to $hvacmode"
		}
	}
	else if (extTemp > temperatureC) {
		if (thermostatState == "heat") {
			def hvacmode = "cool"
			thermostat.setThermostatMode(hvacmode)
			log.debug "HVAC mode set to $hvacmode"
		}
	}
	
	if (thermostatFan != "fanAuto") {
		thermostat.setThermostatFanMode("fanAuto")
		log.debug "HVAC fan mode set to auto"
	}
}

def idleSwitchMode() {
	unschedule() // clear any pending timers, bug with ST platform
    log.info "Setting the scheduled idle temperatures to $idHeatSet and $idCoolSet"
    //sendNotificationEvent("$motionSensor scheduled idle detected, setting $thermostat to $idHeatSet and $idCoolSet")
    setTemperature(idHeatSet, idCoolSet)
}

def inactiveMotionHandler(evt) {
	// Don't unschedule here since pending idle events need to complete as scheduled to avoid infinite loop e.g. delay set to 20 minutes, idle event comes every 5 minutes
    if (idleTimeout != 0) {
        log.info "No motion detected, scheduling switch to idle mode in $idleTimeout minutes"
        def schTime = new Date(now() + (idleTimeout * 60 * 1000)) // current time plus idleTimeout in minutes
        schedule(schTime, idleSwitchMode)
        log.debug "Scheduled idle mode switch at ${schTime.format("EEE MMM dd yyyy HH:mm z", location.timeZone)}"
    }
    else {
        log.info "Setting the idle temperatures to $idHeatSet and $idCoolSet"
        //sendNotificationEvent("$evt.displayName idle detected, setting $thermostat to $idHeatSet and $idCoolSet")
        setTemperature(idHeatSet, idCoolSet)
    }
}
            
def activeMotionHandler(evt) {
	unschedule() // clear any pending timers for idle, we detected motion

	log.debug("Active motion detected, initiating operating temperature set")
    
    def doChange = false
    Calendar localCalendar = Calendar.getInstance(location.timeZone);
    int currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK);
    def currentTime = now()

    // some debugging in order to make sure things are working correclty
    log.debug "Current time: ${(new Date(currentTime)).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}"

	// Check if we are within operating times
    if (startTime != null && endTime != null) {
        def scheduledStart = timeToday(startTime, location.timeZone).time
        def scheduledEnd = timeToday(endTime, location.timeZone).time

    	log.debug("Operating StartTime ${(new Date(scheduledStart)).format("HH:mm z", location.timeZone)}, endTime ${(new Date(scheduledEnd)).format("HH:mm z", location.timeZone)}")

		if (currentTime < scheduledStart || currentTime > scheduledEnd) {
            log.info("Outside operating temperature schedule")
            return
        }
    }

	// Check the condition under which we want this to run now
    // This set allows the most flexibility.
    log.debug("Operating DOW(s): $dayOfWeek")

    if(dayOfWeek.contains('All Week')) {
            doChange = true
    }
    else if((dayOfWeek.contains('Monday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.MONDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Tuesday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.TUESDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Wednesday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.WEDNESDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Thursday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.THURSDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Friday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.FRIDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Saturday') || dayOfWeek.contains('Saturday & Sunday')) && currentDayOfWeek == Calendar.instance.SATURDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Sunday') || dayOfWeek.contains('Saturday & Sunday')) && currentDayOfWeek == Calendar.instance.SUNDAY) {
            doChange = true
    }


    // If we have hit the condition to schedule this then lets do it
    if(doChange == true){
    	log.info("Setting the operating temperature to $opHeatSet and $opCoolSet")
        //sendNotificationEvent("$evt.displayName motion detected, settings $thermostat to $opHeatSet and $opCoolSet")
		setTemperature(opHeatSet, opCoolSet)
    }
    else {
        log.info("Outside operating day of week")
    }
}

// Set the thermostat temperature
private setTemperature(heatSet, coolSet)
{
    thermostat.setHeatingSetpoint(heatSet)
    thermostat.setCoolingSetpoint(coolSet)
}