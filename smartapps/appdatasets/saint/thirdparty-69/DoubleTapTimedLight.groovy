/**
 *  Double Tap Mode Switch
 *
 *  Copyright 2014 George Sudarkoff
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 a      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
    name: "Double Tap Timed Light Switch",
    namespace: "com.sudarkoff",
    author: "George Sudarkoff",
    description: "Turn a light for a period of time when an existing switch is tapped OFF twice in a row.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
    page (name: "configPage", install: true, uninstall: true) {
        section ("When this switch is double-tapped OFF...") {
            input "master", "capability.switch", required: true
        }

        section ("Turn it on for this many minutes...") {
            input "duration", "number", required: true
        }

        section ("Notification method") {
            input "sendPushMessage", "bool", title: "Send a push notification?"
        }

        section (title: "More Options", hidden: hideOptionsSection(), hideable: true) {
            input "phone", "phone", title: "Additionally, also send a text message to:", required: false

            input "customName", "text", title: "Assign a name", required: false

            def timeLabel = timeIntervalLabel()
            href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null

            input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            input "modes", "mode", title: "Only when mode is", multiple: true, required: false
        }
    }

    page (name: "timeIntervalInput", title: "Only during a certain time") {
        section {
            input "starting", "time", title: "Starting", required: false
            input "ending", "time", title: "Ending", required: false
        }
    }
}

def installed()
{
    initialize()
}

def updated()
{
    unsubscribe()
    unschedule()
    initialize()
}

def initialize()
{
    if (customName) {
        state.currentAppLabel = customName
    }
    subscribe(master, "switch", switchHandler, [filterEvents: false])
}

def switchHandler(evt) {
    log.info evt.value

    if (allOk) {
        // use Event rather than DeviceState because we may be changing DeviceState to only store changed values
        def recentStates = master.eventsSince(new Date(now() - 4000), [all:true, max: 10]).findAll{it.name == "switch"}
        log.debug "${recentStates?.size()} STATES FOUND, LAST AT ${recentStates ? recentStates[0].dateCreated : ''}"

        if (evt.isPhysical()) {
            if (evt.value == "on") {
                unschedule();
            }
            else if (evt.value == "off" && lastTwoStatesWere("off", recentStates, evt)) {
                log.debug "detected two OFF taps, turning the light ON for ${duration} minutes"
                master.on()
                runIn(duration * 60, switchOff)
                def message = "${master.label} turned on for ${duration} minutes"
                send(message)
            }
        }
        else {
            log.trace "Skipping digital on/off event"
        }
    }
}

def switchOff() {
    master.off()
}

private lastTwoStatesWere(value, states, evt) {
    def result = false
    if (states) {
        log.trace "unfiltered: [${states.collect{it.dateCreated + ':' + it.value}.join(', ')}]"
        def onOff = states.findAll { it.isPhysical() || !it.type }
        log.trace "filtered:   [${onOff.collect{it.dateCreated + ':' + it.value}.join(', ')}]"

        // This test was needed before the change to use Event rather than DeviceState. It should never pass now.
        if (onOff[0].date.before(evt.date)) {
            log.warn "Last state does not reflect current event, evt.date: ${evt.dateCreated}, state.date: ${onOff[0].dateCreated}"
            result = evt.value == value && onOff[0].value == value
        }
        else {
            result = onOff.size() > 1 && onOff[0].value == value && onOff[1].value == value
        }
    }
    result
}

private send(msg) {
    if (sendPushMessage != "No") {
        sendPush(msg)
    }

    if (phone) {
        sendSms(phone, msg)
    }

    log.debug msg
}

// execution filter methods
private getAllOk() {
    modeOk && daysOk && timeOk
}

private getModeOk() {
    def result = !modes || modes.contains(location.mode)
    result
}

private getDaysOk() {
    def result = true
    if (days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        if (location.timeZone) {
            df.setTimeZone(location.timeZone)
        }
        else {
            df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))
        }
        def day = df.format(new Date())
        result = days.contains(day)
    }
    result
}

private getTimeOk() {
    def result = true
    if (starting && ending) {
        def currTime = now()
        def start = timeToday(starting).time
        def stop = timeToday(ending).time
        result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
    }
    log.trace "timeOk = $result"
    result
}

private hideOptionsSection() {
    (phone || starting || ending || customName || days || modes) ? false : true
}

private hhmm(time, fmt = "h:mm a")
{
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    f.format(t)
}

private timeIntervalLabel() {
    (starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}

