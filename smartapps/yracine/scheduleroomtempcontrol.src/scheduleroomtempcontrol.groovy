/**
 *  ScheduleRoomTempControl
 *
 *  Copyright 2015 Yves Racine
 *  LinkedIn profile: ca.linkedin.com/pub/yves-racine-m-sc-a/0/406/4b/
 *
 *  Developer retains all right, title, copyright, and interest, including all copyright, patent rights, trade secret 
 *  in the Background technology. May be subject to consulting fees under the Agreement between the Developer and the Customer. 
 *  Developer grants a non exclusive perpetual license to use the Background technology in the Software developed for and delivered 
 *  to Customer under this Agreement. However, the Customer shall make no commercial use of the Background technology without
 *  Developer's written consent.
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *
 *  Software Distribution is restricted and shall be done only with Developer's written approval.
 */
 
definition(
	name: "ScheduleRoomTempControl",
	namespace: "yracine",
	author: "Yves Racine",
	description: "Enable better temp control in rooms based on Smart Vents",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)



preferences {

	page(name: "dashboardPage")
	page(name: "generalSetupPage")
	page(name: "roomsSetupPage")
	page(name: "zonesSetupPage")
	page(name: "schedulesSetupPage")
	page(name: "configDisplayPage")
	page(name: "NotificationsPage")
	page(name: "roomsSetup")
	page(name: "zonesSetup")
	page(name: "schedulesSetup")
}

def dashboardPage() {
	def scale= getTemperatureScale()
	dynamicPage(name: "dashboardPage", title: "Dashboard", uninstall: true, nextPage: generalSetupPage,submitOnChange: true) {
		section("Tap Running Schedule(s) Config for latest info\nPress Next (upper right) for initial Setup") {
			if (roomsCount && zonesCount && schedulesCount) {
				paragraph "Last Running Schedule: $state.lastScheduleName\n" +
					"ActiveZone(s): ${state?.activeZones}"
				if (state?.avgTempDiff)  { 
					paragraph "AvgTempDiffInZone: ${state?.avgTempDiff}$scale"                   
				}
				if (thermostat) {                	
					String mode = thermostat?.currentThermostatMode.toString()
					def operatingState=thermostat.currentThermostatOperatingState                
					def heatingSetpoint,coolingSetpoint
					switch (mode) { 
 						case 'cool':
							coolingSetpoint = thermostat.currentValue('coolingSetpoint')
						break                            
 						case 'auto': 
							coolingSetpoint = thermostat.currentValue('coolingSetpoint')
						case 'emergency heat':
						case 'auto': 
						case 'off': 
							try {                    
		 						heatingSetpoint = thermostat?.currentValue('heatingSetpoint')
							} catch (e) {
								log.debug("dashboardPage>not able to get heatingSetpoint from $thermostat,exception $e")                      
							}                        
							heatingSetpoint=  (heatingSetpoint)? heatingSetpoint: (scale=='C')?21:72                        
						break
					}        
					def dParagraph = "TstatMode: $mode\n" +
						"TstatOperatingState $operatingState\n" 
					if (coolingSetpoint)  { 
						dParagraph = dParagraph + "CoolingSetpoint: ${coolingSetpoint}$scale\n"
					}     
					if (heatingSetpoint)  { 
						dParagraph = dParagraph + "HeatingSetpoint: ${heatingSetpoint}$scale\n" 
					}     
					paragraph image: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png", dParagraph 
				}                        
				if ((state?.closedVentsCount) || (state?.openVentsCount)) {
					paragraph "    ** SMART VENTS SUMMARY **\n              For Active Zone(s)\n" 
					paragraph image: "http://cdn.device-icons.smartthings.com/vents/vent-open-icn@2x.png", "OpenVentsCount: ${state?.openVentsCount}\n" +
						"MaxOpenLevel: ${state?.maxOpenLevel}%\n" +
						"MinOpenLevel: ${state?.minOpenLevel}%\n" +
						"AvgVentLevel: ${state?.avgVentLevel}%\n" +
						"MinVentTemp: ${state?.minTempInVents}${scale}\n" +
						"MaxVentTemp: ${state?.maxTempInVents}${scale}\n" +
						"AvgVentTemp: ${state?.avgTempInVents}${scale}"
					if (state?.totalVents) {
						paragraph image: "http://cdn.device-icons.smartthings.com/vents/vent-icn@2x.png","ClosedVentsInZone: ${state?.closedVentsCount}\n" +
						 "ClosedVentsTotal: ${state?.totalClosedVents}\n" +
						"RatioClosedVents: ${state.ratioClosedVents}%\n" +
						"VentsTotal: ${state?.totalVents}\n" 
					}
				}                
				href(name: "toConfigurationDisplayPage", title: "Running Schedule(s) Config", page: "configDisplayPage") 
			}
		} /* end section dashboard */
		section("ABOUT") {
			paragraph "ScheduleRoomTempControl, the smartapp that enables better temp control in rooms based on Smart Vents"
			paragraph "Version 2.0.6"

			paragraph "Copyright©2015 Yves Racine"
				href url:"http://www.maisonsecomatiq.com/#!home/mainPage", style:"embedded", required:false, title:"More information..."  
					description: "http://www.maisonsecomatiq.com/#!home/mainPage"
		} /* end section about  */
	}
}

def generalSetupPage() {
	dynamicPage(name: "generalSetupPage", nextPage: roomsSetupPage) {
		section("Main thermostat at home (used for vent adjustment) [optional]") {
			input (name:"thermostat", type: "capability.thermostat", title: "Which main thermostat?",required:false)
		}
		section("Rooms count") {
			input (name:"roomsCount", title: "Rooms count (max=16)?", type: "number",refreshAfterSelection: true)
		}
		section("Zones count") {
			input (name:"zonesCount", title: "Zones count (max=8)?", type:"number",refreshAfterSelection: true)
		}
		section("Schedules count") {
			input (name:"schedulesCount", title: "Schedules count (max=12)?", type: "number",refreshAfterSelection: true)
		}
		if (thermostat) {
			section {
				href(name: "toRoomPage", title: "Room Setup", page: "roomsSetupPage")
				href(name: "toZonePage", title: "Zone Setup", page: "zonesSetupPage")
				href(name: "toSchedulePage", title: "Schedule Setup", page: "schedulesSetupPage")
				href(name: "toNotificationsPage", title: "Notification & Options Setup", page: "NotificationsPage")
			}                
		}
		section("Disable or Modify the safeguards [default=some safeguards are implemented to avoid damaging your HVAC by closing too many vents]") {
			input (name:"fullyCloseVentsFlag", title: "Bypass all safeguards & allow closing the vents totally?", type:"bool",required:false)
			input (name:"minVentLevelInZone", title: "Safeguard's Minimum Vent Level in Zone", type:"number", required: false, description: "[default=10%]")
			input (name:"minVentLevelOutZone", title: "Safeguard's Minimum Vent Level Outside of the Zone", type:"number", required: false, description: "[default=25%]")
			input (name:"maxVentTemp", title: "Safeguard's Maximum Vent Temp", type:"number", required: false, description: "[default= 131F/55C]")
			input (name:"minVentTemp", title: "Safeguard's Minimum Vent Temp", type:"number", required: false, description: "[default= 45F/7C]")
		}       
		section("What do I use for the Master on/off switch to enable/disable smartapp processing? [optional]") {
			input (name:"powerSwitch", type:"capability.switch", required: false,description: "Optional")
		}
		section {
			href(name: "toDashboardPage", title: "Back to Dashboard Page", page: "dashboardPage")
		}
	}
}

def roomsSetupPage() {

	dynamicPage(name: "roomsSetupPage", title: "Room Setup", nextPage: zonesSetupPage) {
		section("Press each room slot below to complete setup") {
			for (int i = 1; i <= settings.roomsCount; i++) {
				href(name: "toRoomPage$i", page: "roomsSetup", params: [indiceRoom: i], required:false, description: roomHrefDescription(i), title: roomHrefTitle(i), state: roomPageState(i) )
			}
		}            
		section {
			href(name: "toGeneralSetupPage", title: "Back to General Setup Page", page: "generalSetupPage")
		}
	}
}        

def roomPageState(i) {

	if (settings."roomName${i}" != null) {
		return 'complete'
	} else {
		return 'incomplete'
	}
  
}

def roomHrefTitle(i) {
	def title = "Room ${i}"
	return title
}

def roomHrefDescription(i) {
	def description ="Room no ${i} "

	if (settings."roomName${i}" !=null) {
		description += settings."roomName${i}"		    	
	}
	return description
}

def roomsSetup(params) {
	def indiceRoom=0    

	// Assign params to indiceZone.  Sometimes parameters are double nested.
	if (params?.indiceRoom || params?.params?.indiceRoom) {

		if (params.indiceRoom) {
			indiceRoom = params.indiceRoom
		} else {
			indiceRoom = params.params.indiceRoom
		}
	}    
 
	indiceRoom=indiceRoom.intValue()

	dynamicPage(name: "roomsSetup", title: "Rooms Setup", uninstall: true, nextPage: zonesSetupPage) {

		section("Room ${indiceRoom} Setup") {
			input "roomName${indiceRoom}", title: "Room Name", "string"
		}
		section("Room ${indiceRoom}-TempSensor [optional]") {
			input "tempSensor${indiceRoom}", title: "Temp sensor for better temp adjustment", "capability.temperatureMeasurement", 
				required: false, description: "Optional"

		}
		section("Room ${indiceRoom}-Vents Setup [optional]")  {
			for (int j = 1;(j <= 5); j++)  {
				input "ventSwitch${j}${indiceRoom}", title: "Vent switch no ${j} in room", "capability.switch", 
					required: false, description: "Optional"
				input "ventLevel${j}${indiceRoom}", title: "set vent no ${j}'s level in room [optional, range 0-100]", "number", 
						required: false, description: "blank:calculated by smartapp"
			}           
		}           
		section("Room ${indiceRoom}-Motion Detection parameters [optional]") {
			input "motionSensor${indiceRoom}", title: "Motion sensor (if any) to detect if room is occupied", "capability.motionSensor", 
				required: false, description: "Optional"
			input "needOccupiedFlag${indiceRoom}", title: "Will do vent adjustement only when Occupied [default=false]", "bool",  
				required: false, description: "Optional"
			input "residentsQuietThreshold${indiceRoom}", title: "Threshold in minutes for motion detection [default=15 min]", "number", 
				required: false, description: "Optional"
			input "occupiedMotionOccNeeded${indiceRoom}", title: "Motion counter for positive detection [default=1 occurence]", "number", 
				required: false, description: "Optional"
		}
		section {
			href(name: "toRoomsSetupPage", title: "Back to Rooms Setup Page", page: "roomsSetupPage")
		}
	}
}


def configDisplayPage() {
	def fullyCloseVents = (settings.fullyCloseVentsFlag) ?: false 	
	String nowInLocalTime = new Date().format("yyyy-MM-dd HH:mm", location.timeZone) 
	float desiredTemp 
	def key 
	def scale=getTemperatureScale()     
	def currTime = now()	 
	boolean foundSchedule=false 
	String bypassSafeguardsString= (fullyCloseVents)?'true':'false'                             
	String setpointFlagString= (noSetPoints=='false')?'true':'false'                             
	float currentTempAtTstat =(scale=='C')?21:72 	// set a default value 
	String mode, operatingState
	int nbClosedVents=0, nbOpenVents=0, totalVents=0, nbRooms=0 
	def MIN_OPEN_LEVEL_IN_ZONE=(minVentLevelInZone)?((minVentLevelInZone>=0 && minVentLevelInZone <100)?minVentLevelInZone:10):10
	int min_open_level=100, max_open_level=0, total_level_vents=0     
	float min_temp_in_vents=200, max_temp_in_vents=0, total_temp_diff=0, target_temp     
	if (thermostat) { 
		currentTempAtTstat = thermostat.currentTemperature.toFloat().round(1) 
		mode = thermostat.currentThermostatMode.toString()
		operatingState=thermostat.currentThermostatOperatingState
	}         

	if (detailedNotif) {
		log.debug "configDisplayPage>About to display Running Schedule(s) Configuration"
	}        
	dynamicPage(name: "configDisplayPage", title: "Running Schedule(s) Config", nextPage: NotificationsPage,submitOnChange: true) {
		section {
			href(name: "toDashboardPage", title: "Back to Dashboard Page", page: "dashboardPage")
		}
        
		section("General") {
			if (thermostat) {                	
				def heatingSetpoint,coolingSetpoint
				switch (mode) { 
					case 'cool':
 						coolingSetpoint = thermostat.currentValue('coolingSetpoint')
						target_temp= coolingSetpoint.toFloat()                        
					break                        
					case 'auto': 
 						coolingSetpoint = thermostat.currentValue('coolingSetpoint')
						target_temp= coolingSetpoint.toFloat()                        
					case 'heat':
					case 'emergency heat':
					case 'auto': 
					case 'off': 
						try {                    
		 					heatingSetpoint = thermostat?.currentValue('heatingSetpoint')
						} catch (e) {
							log.debug("ConfigDisplayPage>not able to get heatingSetpoint from $thermostat, exception $e")                      
						}                        
						heatingSetpoint=  (heatingSetpoint)? heatingSetpoint: (scale=='C')?21:72                        
						if (mode == 'auto') {
							target_temp= ((coolingSetpoint + heatingSetpoint)/2).toFloat().round(1)
						} else {                        
							target_temp =heatingSetpoint.toFloat()                   
						}   
					break                        
				}                        
				paragraph " TstatMode: $mode\n TstatOperatingState $operatingState"
				if (coolingSetpoint)  { 
					paragraph " TstatCoolingSetpoint: ${coolingSetpoint}$scale"
				}                        
				if (heatingSetpoint)  { 
					paragraph " TstatHeatingSetpoint: ${heatingSetpoint}$scale"
				}    
			}                
			if (state?.avgTempDiff)  {   
				paragraph " AvgTempDiffInZone: ${state?.avgTempDiff.toFloat().round(1)}$scale"                     
			}  
			paragraph " BypassSafeguards: ${bypassSafeguardsString}" 
		}
		for (int i = 1;((i <= settings.schedulesCount) && (i <= 12)); i++) {
        
			key = "selectedMode$i"
			def selectedModes = settings[key]
			key = "scheduleName$i"
			def scheduleName = settings[key]
			boolean foundMode=false        
			if (detailedNotif) {
				log.debug("configDisplayPage>looping thru schedules, now at $scheduleName")
			}
			selectedModes.each {
				if (it==location.mode) {
					foundMode=true            
				}            
			}        
			if ((selectedModes != null) && (!foundMode)) {
        
				if (detailedNotif) {
					log.debug "configDisplayPage>schedule=${scheduleName} does not apply,location.mode= $location.mode, selectedModes=${selectedModes},foundMode=${foundMode}, continue"
				}                    
				continue			
			}
			key = "begintime$i"
			def startTime = settings[key]
			if (startTime == null) {
        			continue
			}
			def startTimeToday = timeToday(startTime,location.timeZone)
			key = "endtime$i"
			def endTime = settings[key]
			def endTimeToday = timeToday(endTime,location.timeZone)
			if ((currTime < endTimeToday.time) && (endTimeToday.time < startTimeToday.time)) {
				startTimeToday = startTimeToday -1        
				if (detailedNotif) {
					log.debug "configDisplayPage>schedule ${scheduleName}, subtracted - 1 day, new startTime=${startTimeToday.time}"
				}                    
			}            
			if ((currTime > endTimeToday.time) && (endTimeToday.time < startTimeToday.time)) {
				endTimeToday = endTimeToday +1        
				if (detailedNotif) {
					log.debug "configDisplayPage>schedule ${scheduleName}, added + 1 day, new endTime=${endTimeToday.time}"
				}				                    
			}   
            
			String startInLocalTime = startTimeToday.format("yyyy-MM-dd HH:mm", location.timeZone)
			String endInLocalTime = endTimeToday.format("yyyy-MM-dd HH:mm", location.timeZone)
            
			if ((currTime >= startTimeToday.time) && (currTime <= endTimeToday.time) && (IsRightDayForChange(i))) {
				foundSchedule=true
                
				if (detailedNotif) {
					log.debug("configDisplayPage>$scheduleName is good to go..")
				}
				key = "givenClimate${i}"
				def climate = settings[key]
                
				key = "includedZones$i"
				def zones = settings[key]
				key = "desiredCool${i}"
				def desiredCool = settings[key]
				key = "desiredHeat${i}"
				def desiredHeat = settings[key]
				key = "desiredCool${i}"
				def desiredCoolTemp = (settings[key])?: ((scale=='C') ? 23:75)
				key = "desiredHeat${i}"
				def desiredHeatTemp = (settings[key])?: ((scale=='C') ? 21:72)
				key = "adjustVentsEveryCycleFlag${i}"
				String adjustVentsEveryCycleString = (settings[key])?'true':'false'
				key = "setVentLevel${i}"
				def setLevel = settings[key]
				key = "resetLevelOverrideFlag${i}"
				def resetLevelOverrideString=(settings[key])?'true':'false'
  
				if (detailedNotif) {
					log.debug("configDisplayPage>about to display schedule $scheduleName..")
				}
				                
				section("Running Schedule(s)") {
					paragraph "Schedule $scheduleName" 
					paragraph " BypassSetLevelOverrideinZone(s): ${resetLevelOverrideString}"
					paragraph " AdjustVentsEveryCycle: $adjustVentsEveryCycleString"
					paragraph " StartTime: $startInLocalTime"   
					paragraph " EndTime: $endInLocalTime"  
					key = "noSetpointsFlag$i"
					def noSetpointInSchedule = settings[key]?: false
					def setpointsAtThermostat = (noSetpointInSchedule==true)?'false':'true'                    
					paragraph " SetpointsAtThermostat: $setpointsAtThermostat"  
					if (!noSetpointInSchedule) {
						if (climate) {
							paragraph " EcobeeProgramSet: $climate" 
						} else {
							if (desiredCoolTemp) {                            
								paragraph " DesiredCool: ${desiredCoolTemp}$scale" 
							}                                
							if (desiredHeatTemp) {                            
								paragraph " DesiredHeat: ${desiredHeatTemp}$scale"
							}
						}                                
					}                            
                    
					if (selectedModes) {                    
						paragraph " STHelloModes: $selectedModes"
					}                        
					paragraph " Includes: $zones" 
				}
				state?.activeZones = zones // save the zones for the dashboard                
				for (zone in zones) { 
						def zoneDetails=zone.split(':') 
	 					def indiceZone = zoneDetails[0] 
						def zoneName = zoneDetails[1] 
						key = "includedRooms$indiceZone" 
						def rooms = settings[key] 
						if (mode=='cool') { 
							key = "desiredCoolTemp$indiceZone" 
							desiredCool= settings[key]                         
							if (desiredCool) { 
								desiredTemp= desiredCool.toFloat() 
							} else { 
 								desiredTemp = (scale=='C') ? 23:75 		// by default, 23C/75F is the target cool temp 
	 						}                 
                            
						} else { 
							key = "desiredHeatTemp$indiceZone" 
							desiredHeat= settings[key] 
 
							if (desiredHeat) { 
								desiredTemp= desiredHeat.toFloat() 
							} else {
								desiredTemp = (scale=='C') ? 21:72 		// by default, 21C/72F is the target heat temp 
							}                 
						} 
						section("Active Zone(s) in Schedule $scheduleName") { 
							paragraph "Zone $zoneName"  
							if (desiredTemp) {                         
								paragraph " TempThresholdForVents: ${desiredTemp}$scale"  
							}                             
							paragraph "Includes: $rooms"  
						} 
						for (room in rooms) { 
							def roomDetails=room.split(':') 
							def indiceRoom = roomDetails[0] 
							def roomName = roomDetails[1] 
							key = "needOccupiedFlag$indiceRoom" 
							def needOccupied = (settings[key]) ?: false 
							log.debug("configDisplayPage>looping thru all rooms,now room=${roomName},indiceRoom=${indiceRoom}, needOccupied=${needOccupied}") 
							key = "motionSensor${indiceRoom}" 
							def motionSensor = (settings[key])  
							key = "tempSensor${indiceRoom}" 
							def tempSensor = (settings[key])  
							def tempAtSensor =getSensorTempForAverage(indiceRoom)			 
							if (tempAtSensor == null) { 
								tempAtSensor= currentTempAtTstat				             
							} 
							section("Room(s) in Zone $zoneName") { 
	 							nbRooms++                         
								paragraph "$roomName"  
								if (tempSensor) {                             
									paragraph " TempSensor: $tempSensor"  
								}                                 
								if (tempAtSensor) { 
 									if (desiredTemp) {                             
	 									float temp_diff = (tempAtSensor.toFloat() - desiredTemp).round(1)  
										paragraph " CurrentTempOffSet: ${temp_diff.round(1)} $scale"  
										total_temp_diff = total_temp_diff + temp_diff                                     
									}                                     
									paragraph " CurrentTempInRoom: ${tempAtSensor}$scale" 
	 							}                                 
		 
								if (motionSensor) {      
									def countActiveMotion=isRoomOccupied(motionSensor, indiceRoom)
									String needOccupiedString= (needOccupied)?'true':'false'
									if (!needOccupied) {                                
										paragraph " MotionSensor: $motionSensor\n" +
											" NeedToBeOccupied: ${needOccupiedString}" 
									} else {                                        
										key = "residentsQuietThreshold${indiceRoom}"
										def threshold = (settings[key]) ?: 15 // By default, the delay is 15 minutes 
										String thresholdString = threshold   
										key = "occupiedMotionOccNeeded${indiceRoom}"
										def occupiedMotionOccNeeded= (settings[key]) ?:1
										key = "occupiedMotionTimestamp${indiceRoom}"
										def lastMotionTimestamp = (state[key])
										String lastMotionInLocalTime                                     
										def isRoomOccupiedString=(countActiveMotion>=occupiedMotionOccNeeded)?'true':'false'                                
										if (lastMotionTimestamp) {                                    
											lastMotionInLocalTime= new Date(lastMotionTimestamp).format("yyyy-MM-dd HH:mm", location.timeZone)
										}						                                    
                                    
										paragraph " MotionSensor: $motionSensor\n" +
											" IsRoomOccupiedNow: ${isRoomOccupiedString}\n" + 
											" NeedToBeOccupied: ${needOccupiedString}\n" + 
											" OccupiedThreshold: ${thresholdString} minutes\n"+ 
											" MotionCountNeeded: ${occupiedMotionOccNeeded}\n" + 
											" OccupiedMotionCounter: ${countActiveMotion}\n" +
											" LastMotionTime: ${lastMotionInLocalTime}"
									}
								}                                
								paragraph "** VENTS in $roomName **" 
								float total_temp_in_vents=0                            
								for (int j = 1;(j <= 5); j++)  {
								key = "ventSwitch${j}$indiceRoom"
								def ventSwitch = settings[key]
								if (ventSwitch != null) {
									float temp_in_vent=ventSwitch.currentValue("temperature")                                
									// compile some stats for the dashboard                    
									if (temp_in_vent) {                                   
										min_temp_in_vents=(temp_in_vent < min_temp_in_vents)? temp_in_vent.toFloat().round(1) : min_temp_in_vents
										max_temp_in_vents=(temp_in_vent > max_temp_in_vents)? temp_in_vent.toFloat().round(1) : max_temp_in_vents
										total_temp_in_vents=total_temp_in_vents + temp_in_vent
									}                                        
									def switchLevel = ventSwitch.currentValue("level")							                        
									totalVents++                                    
									paragraph "$ventSwitch\n\n  CurrentVentLevel: ${switchLevel}%"
									if (switchLevel) {                                    
										// compile some stats for the dashboard                    
										if (temp_in_vent) {                                   
											min_temp_in_vents=(temp_in_vent < min_temp_in_vents)? temp_in_vent.toFloat().round(1) : min_temp_in_vents
											max_temp_in_vents=(temp_in_vent > max_temp_in_vents)? temp_in_vent.toFloat().round(1) : max_temp_in_vents
											total_temp_in_vents=total_temp_in_vents + temp_in_vent
										}	                                        
										if (switchLevel>=MIN_OPEN_LEVEL_IN_ZONE) {
    										nbOpenVents++                                    
										} else {
    										nbClosedVents++                                    
										}                                        
									}                                        
                            
									input "ventLevel${j}${indiceRoom}", title: "  override vent level[optional,range 0-100]", "number", 
										required: false, description: "  blank:calculated by smartapp"
								}                            
							}  
						} /* end section rooms */
					} /* end for rooms */
				} /* end for zones */
			} /* end if current schedule */ 
		} /* end for schedules */
		state?.closedVentsCount= nbClosedVents                                  
		state?.openVentsCount= nbOpenVents         
		state?.minOpenLevel= min_open_level
		state?.maxOpenLevel= max_open_level
		state?.minTempInVents=min_temp_in_vents
		state?.maxTempInVents=max_temp_in_vents
		if (detailedNotif) {
			log.debug("configDisplayPage>foundSchedule=$foundSchedule")
		}
		if (total_temp_in_vents) {
			state?.avgTempInVents= (total_temp_in_vents/(nbOpenVents + nbClosedVents)).toFloat().round(1)
		}		        
		if (total_level_vents) {    
			state?.avgVentLevel= (total_level_vents/(nbOpenVents + nbClosedVents)).toFloat().round(1)
		}		        
		nbClosedVents=0        
		nbOpenVents=0    
		// Loop thru all smart vents to get the total count of vents (open,closed)
		for (indiceRoom in 1..roomsCount) {
			for (int j = 1;(j <= 5); j++)  {
				key = "ventSwitch${j}$indiceRoom"
				def ventSwitch = settings[key]
				if (ventSwitch != null) {
					def switchLevel = ventSwitch.currentValue("level")							                        
					if (switchLevel>MIN_OPEN_LEVEL_IN_ZONE) {
						nbOpenVents++                                    
					} else {
    					nbClosedVents++                                    
					}                                        
				} /* end if ventSwitch != null */
			} /* end for switches null */
		} /* end for vent rooms */

		// More stats for dashboard
		if (total_temp_diff ) {
			state?.avgTempDiff = (total_temp_diff/nbRooms).round(1)			        
		}            
		state?.totalVents=nbClosedVents+nbOpenVents
		state?.totalClosedVents=nbClosedVents
		if (nbClosedVents) {
			float ratioClosedVents=((nbClosedVents/state?.totalVents).toFloat()*100)
			state?.ratioClosedVents=ratioClosedVents.round(1)
		} else {
			state?.ratioClosedVents=0
		}
		if (!foundSchedule) {         
			section {
				paragraph "\n\nNo Schedule running at this time $nowInLocalTime" 
			}	                
		}
		section {
			href(name: "toDashboardPage", title: "Back to Dashboard Page", page: "dashboardPage")
		}
	} /* end dynamic page */                
}


def zoneHrefDescription(i) {
	def description ="Zone no ${i} "

	if (settings."zoneName${i}" !=null) {
		description += settings."zoneName${i}"		    	
	}
	return description
}

def zonePageState(i) {

	if (settings."zoneName${i}" != null) {
		return 'complete'
	} else {
		return 'incomplete'
	}
  
}

def zoneHrefTitle(i) {
	def title = "Zone ${i}"
	return title
}

def zonesSetupPage() {

	dynamicPage(name: "zonesSetupPage", title: "Zones Setup", nextPage: schedulesSetupPage) {
		section("Press each zone slot below to complete setup") {
			for (int i = 1; i <= settings.zonesCount; i++) {
				href(name: "toZonePage$i", page: "zonesSetup", params: [indiceZone: i], required:false, description: zoneHrefDescription(i), title: zoneHrefTitle(i), state: zonePageState(i) )
			}
		}            
		section {
			href(name: "toGeneralSetupPage", title: "Back to General Setup Page", page: "generalSetupPage")
		}
	}
}        

def zonesSetup(params) {

	def rooms = []
	for (i in 1..settings.roomsCount) {
		def key = "roomName$i"
		def room = "${i}:${settings[key]}"
		rooms = rooms + room
	}
	def indiceZone=0    

	// Assign params to indiceZone.  Sometimes parameters are double nested.
	if (params?.indiceZone || params?.params?.indiceZone) {

		if (params.indiceZone) {
			indiceZone = params.indiceZone
		} else {
			indiceZone = params.params.indiceZone
		}
	}    
	indiceZone=indiceZone.intValue()
	dynamicPage(name: "zonesSetup", title: "Zones Setup") {
		section("Zone ${indiceZone} Setup") {
			input (name:"zoneName${indiceZone}", title: "Zone Name", type: "text",
				defaultValue:settings."zoneName${indiceZone}")
		}
		section("Zone ${indiceZone}-Included rooms") {
			input (name:"includedRooms${indiceZone}", title: "Rooms included in the zone", type: "enum",
				options: rooms,
				multiple: true,
				defaultValue:settings."includedRooms${indiceZone}")
		}
		section("Zone ${indiceZone}-Cool Temp threshold in the zone (below it, when cooling, the vents are -partially- closed)") {
			input (name:"desiredCoolTemp${indiceZone}", type:"decimal", title: "Cool Temp Threshold [default = 75F/23C] ", 
				required: false,defaultValue:settings."desiredCoolTemp${indiceZone}")			                
		}
		section("Zone ${indiceZone}-Heat Temp threshold in the zone (above it, when heating, the vents are -partially- closed)") {
			input (name:"desiredHeatTemp${indiceZone}", type:"decimal", title: "Heat Temp Threshold [default = 72F/21C]", 
				required: false, defaultValue:settings."desiredHeatTemp${indiceZone}")			                
		}
		section {
			href(name: "toZonesSetupPage", title: "Back to Zones Setup Page", page: "zonesSetupPage")
		}
	}            
}

def scheduleHrefDescription(i) {
	def description ="Schedule no ${i} " 
	if (settings."scheduleName${i}" !=null) {
		description += settings."scheduleName${i}"		    
	}
	return description
}

def schedulePageState(i) {

	if (settings."scheduleName${i}"  != null) {
		return 'complete'
	} else {
		return 'incomplete'
	}	
    
}

def scheduleHrefTitle(i) {
	def title = "Schedule ${i}"
	return title
}

def schedulesSetupPage() {
	dynamicPage(name: "schedulesSetupPage", title: "Schedule Setup", nextPage: NotificationsPage) {
		section("Press each schedule slot below to complete setup") {
			for (int i = 1; i <= settings.schedulesCount; i++) {
				href(name: "toSchedulePage$i", page: "schedulesSetup", params: [indiceSchedule: i],required:false, description: scheduleHrefDescription(i), title: scheduleHrefTitle(i), state: schedulePageState(i) )
			}
		}            
		section {
			href(name: "toGeneralSetupPage", title: "Back to General Setup Page", page: "generalSetupPage")
		}
	}
}        

def schedulesSetup(params) {
    
	def ecobeePrograms=[]
	// try to get the thermostat programs list (ecobee)
	try {
		ecobeePrograms = thermostat?.currentClimateList.toString().minus('[').minus(']').tokenize(',')
		ecobeePrograms.sort()        
	} catch (any) {
		if (detailedNotif) {
			log.debug("Not able to get the list of climates (ecobee)")    	
		}            
	}    
    
    
	if (detailedNotif) {
		log.debug "programs: $ecobeePrograms"
	}        

	def zones = []
    
	for (i in 1..settings.zonesCount) {
		def key = "zoneName$i"
		def zoneName =  "${i}:${settings[key]}"   
		zones = zones + zoneName
	}

	
	def enumModes=[]
	location.modes.each {
		enumModes << it.name
	}    
	def indiceSchedule=1
	// Assign params to indiceSchedule.  Sometimes parameters are double nested.
	if (params?.indiceSchedule || params?.params?.indiceSchedule) {

		if (params.indiceSchedule) {
			indiceSchedule = params.indiceSchedule
		} else {
			indiceSchedule = params.params.indiceSchedule
		}
	}    
	indiceSchedule=indiceSchedule.intValue()
	dynamicPage(name: "schedulesSetup", title: "Schedule Setup") {
		section("Schedule ${indiceSchedule} Setup") {
			input (name:"scheduleName${indiceSchedule}", title: "Schedule Name", type: "text",
				defaultValue:settings."scheduleName${indiceSchedule}")
		}
		section("Schedule ${indiceSchedule}-Included zones") {
			input (name:"includedZones${indiceSchedule}", title: "Zones included in this schedule", type: "enum",
				defaultValue:settings."includedZones${indiceSchedule}",
				options: zones,
 				multiple: true)
		}
		section("Schedule ${indiceSchedule}- Day & Time of the desired Heating/Cooling settings for the selected zone(s)") {
			input (name:"dayOfWeek${indiceSchedule}", type: "enum",
				title: "Which day of the week to trigger the zoned heating/cooling settings?",
				defaultValue:settings."dayOfWeek${indiceSchedule}",                 
				multiple: false,
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
				])
			input (name:"begintime${indiceSchedule}", type: "time", title: "Beginning time to trigger the zoned heating/cooling settings",
				defaultValue:settings."begintime${indiceSchedule}")
			input (name:"endtime${indiceSchedule}", type: "time", title: "End time",
				defaultValue:settings."endtime${indiceSchedule}")
		}
		section("Schedule ${indiceSchedule}-Select the program/climate at ecobee thermostat to be applied [optional,for ecobee only]") {
			input (name:"givenClimate${indiceSchedule}", type:"enum", title: "Which ecobee program? ", options: ecobeePrograms, 
				required: false, defaultValue:settings."givenClimate${indiceSchedule}", description: "Optional")
		}
		section("Schedule ${indiceSchedule}-Set Thermostat's Cooling setpoint during the schedule [optional, when no ecobee climate is specified]") {
			input (name:"desiredCool${indiceSchedule}", type:"decimal", title: "Cooling Setpoint, default = 75F/23C", 
				required: false,defaultValue:settings."desiredCool${indiceSchedule}", description: "Optional")			                
		}
		section("Schedule ${indiceSchedule}-Set Thermostat's Heating setpoint during the schedule [optional, when no ecobee climate is specified]") {
			input (name:"desiredHeat${indiceSchedule}", type:"decimal", title: "Heating Setpoint, default=72F/21C", 
				required: false, defaultValue:settings."desiredHeat${indiceSchedule}", description: "Optional")			                
		}
		section("Schedule ${indiceSchedule}-Vent Settings for the Schedule") {
			input (name: "setVentLevel${indiceSchedule}", type:"number",  title: "Set all Vents in Zone(s) to a specific Level during the Schedule [range 0-100]", 
				required: false, defaultValue:settings."setVentLevel${indiceSchedule}", description: "blank: calculated by smartapp")
			input (name: "resetLevelOverrideFlag${indiceSchedule}", type:"bool",  title: "Bypass all vents overrides in zone(s) during the Schedule (default=false)?", 
				required: false, defaultValue:settings."resetLevelOverrideFlag${indiceSchedule}", description: "Optional")
			input (name: "adjustVentsEveryCycleFlag${indiceSchedule}", type:"bool",  title: "Adjust vent settings every 5 minutes (default=only when heating/cooling/fan running)?", 
				required: false, defaultValue:settings."adjustVentsEveryCycleFlag${indiceSchedule}", description: "Optional")
		}
		section("Schedule ${indiceSchedule}-Set for specific mode(s) [default=all]")  {
			input (name:"selectedMode${indiceSchedule}", type:"enum", title: "Choose Mode", options: enumModes, 
				required: false, multiple:true,defaultValue:settings."selectedMode${indiceSchedule}", description: "Optional")
		}
		section("Do not set the thermostat setpoints in this schedule [optional, default=The thermostat setpoints are set]") {
			input (name:"noSetpointsFlag${indiceSchedule}", title: "Do not set the thermostat setpoints?", type:"bool", 
				required:false, defaultValue:settings."noSetpointsFlag${indiceSchedule}")
		}
		section {
			href(name: "toSchedulesSetupPage", title: "Back to Schedules Setup Page", page: "schedulesSetupPage")
		}
	}        
}

def NotificationsPage() {
	dynamicPage(name: "NotificationsPage", title: "Other Options", install: true) {
		section("Notifications") {
			input "sendPushMessage", "enum", title: "Send a push notification?", metadata: [values: ["Yes", "No"]], required: false
			input "phone", "phone", title: "Send a Text Message?", required: false
		}
		section("Detailed Notifications") {
			input "detailedNotif", "bool", title: "Detailed Notifications?", required:
				false
		}
		section([mobileOnly: true]) {
			label title: "Assign a name for this SmartApp", required: false
		}
		section {
			href(name: "toGeneralSetupPage", title: "Back to General Setup Page", page: "generalSetupPage")
		}
	}
}



def installed() {
	state?.closedVentsCount= 0
	state?.openVentsCount=0
	state?.totalVents=0
	state?.ratioClosedVents=0
	state?.avgTempDiff=0.0
	state?.activeZones=[]
	initialize()
}

def updated() {
	unsubscribe()
	unschedule()
	initialize()
}

def offHandler(evt) {
	log.debug "$evt.name: $evt.value"
}

def onHandler(evt) {
	log.debug "$evt.name: $evt.value"
	rescheduleIfNeeded(evt)   // Call rescheduleIfNeeded to work around ST scheduling issues
}

def ventTemperatureHandler(evt) {
	log.debug "vent temperature: $evt.value"
	float ventTemp = evt.value.toFloat()
	def scale = getTemperatureScale()
	def MAX_TEMP_VENT_SWITCH = (maxVentTemp)?:(scale=='C')?55:131  //Max temperature inside a ventSwitch
	def MIN_TEMP_VENT_SWITCH = (minVentTemp)?:(scale=='C')?7:45   //Min temperature inside a ventSwitch
	def currentHVACMode = thermostat?.currentThermostatMode.toString()
	currentHVACMode=(currentHVACMode)?:'auto'	// set auto by default
    
	if ((currentHVACMode in ['heat','auto', 'emergency heat']) && (ventTemp >= MAX_TEMP_VENT_SWITCH)) {
		if (fullyCloseVentsFlag) {
			// Safeguards are not implemented as requested     
			log.warn "ventTemperatureHandler>vent temperature is not within range ($evt.value>$MAX_TEMP_VENT_SWITCH) ,but safeguards are not implemented as requested"
			return    
		}    
    
		// Open all vents just to be safe
		open_all_vents()
		send("ScheduleRoomTempControl>current HVAC mode is ${currentHVACMode}, found one of the vents' value too hot (${evt.value}), opening all vents to avoid any damage")
	} /* if too hot */           
	if ((currentHVACMode in ['cool','auto']) && (ventTemp <= MIN_TEMP_VENT_SWITCH)) {
		if (fullyCloseVentsFlag) {
			// Safeguards are not implemented as requested     
			log.warn "ventTemperatureHandler>vent temperature is not within range, ($evt.value<$MIN_TEMP_VENT_SWITCH) but safeguards are not implemented as requested"
			return    
		}    
		// Open all vents just to be safe
		open_all_vents()
		send("ScheduleRoomTempControl>current HVAC mode is ${currentHVACMode}, found one of the vents' value too cold (${evt.value}), opening all vents to avoid any damage")
	} /* if too cold */ 

}


def thermostatOperatingHandler(evt) {
	log.debug "Thermostat Operating now: $evt.value"
	state?.operatingState=evt.value    
	rescheduleIfNeeded(evt)   // Call rescheduleIfNeeded to work around ST scheduling issues
}

def heatingSetpointHandler(evt) {
	log.debug "heating Setpoint now: $evt.value"
	setZoneSettings()
}
def coolingSetpointHandler(evt) {
	log.debug "cooling Setpoint now: $evt.value"
	setZoneSettings()
}

def changeModeHandler(evt) {
	log.debug "Changed mode, $evt.name: $evt.value"
	rescheduleIfNeeded(evt)   // Call rescheduleIfNeeded to work around ST scheduling issues
}


def motionEvtHandler(evt, indice) {
	if (evt.value == "active") {
		def key= "roomName${indice}"    
		def roomName= settings[key]
		key = "occupiedMotionTimestamp${indice}"       
		state[key]= now()        
		log.debug "Motion at home in ${roomName},occupiedMotionTimestamp=${state[key]}"
		if (detailedNotif) {
			send("ScheduleRoomTempControl>motion at home in ${roomName}, occupiedMotionTimestamp=${state[key]}")
		}
		if (state?.setPresentOrAway == 'Away') {
			set_main_tstat_to_AwayOrPresent('present')
		}        
	}
}


def motionEvtHandler1(evt) {
	int i=1
	motionEvtHandler(evt,i)    
}

def motionEvtHandler2(evt) {
	int i=2
	motionEvtHandler(evt,i)    
}

def motionEvtHandler3(evt) {
	int i=3
	motionEvtHandler(evt,i)    
}

def motionEvtHandler4(evt) {
	int i=4
	motionEvtHandler(evt,i)    
}

def motionEvtHandler5(evt) {
	int i=5
	motionEvtHandler(evt,i)    
}

def motionEvtHandler6(evt) {
	int i=6
	motionEvtHandler(evt,i)    
}

def motionEvtHandler7(evt) {
	int i=7
	motionEvtHandler(evt,i)    
}

def motionEvtHandler8(evt) {
	int i=8
	motionEvtHandler(evt,i)    
}

def motionEvtHandler9(evt) {
	int i=9
	motionEvtHandler(evt,i)    
}

def motionEvtHandler10(evt) {
	int i=10
	motionEvtHandler(evt,i)    
}

def motionEvtHandler11(evt) {
	int i=11
	motionEvtHandler(evt,i)    
}

def motionEvtHandler12(evt) {
	int i=12
	motionEvtHandler(evt,i)    
}

def motionEvtHandler13(evt) {
	int i=13
	motionEvtHandler(evt,i)    
}

def motionEvtHandler14(evt) {
	int i=14
	motionEvtHandler(evt,i)    
}

def motionEvtHandler15(evt) {
	int i=15
	motionEvtHandler(evt,i)    
}

def motionEvtHandler16(evt) {
	int i=16
	motionEvtHandler(evt,i)    
}

def initialize() {

	if (powerSwitch) {
		subscribe(powerSwitch, "switch.off", offHandler, [filterEvents: false])
		subscribe(powerSwitch, "switch.on", onHandler, [filterEvents: false])
	}
	subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler)    
	subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler)
	subscribe(thermostat, "thermostatOperatingState", thermostatOperatingHandler)
    
	subscribe(location, changeModeHandler)

	// Initialize state variables
	state.lastScheduleLastName=""
	state.lastStartTime=null 
	state.scheduleHeatSetpoint=0  
	state.scheduleCoolSetpoint=0    
	state.setPresentOrAway=''
	state.programSetTime = ""
	state.programSetTimestamp = null
	state.operatingState=""
    
	subscribe(app, appTouch)

	// subscribe all vents to check their temperature on a regular basis
    
	for (indiceRoom in 1..roomsCount) {
		for (int j = 1;(j <= 5); j++)  {
			def key = "ventSwitch${j}$indiceRoom"
			def vent = settings[key]
			if (vent) {
				subscribe(vent, "temperature", ventTemperatureHandler)
			} /* end if vent != null */
		} /* end for vent switches */
	} /* end for rooms */

	// subscribe all motion sensors to check for active motion in rooms
    
	for (int i = 1;
		((i <= settings.roomsCount) && (i <= 16)); i++) {
		def key = "motionSensor${i}"
		def motionSensor = settings[key]
        
		if (motionSensor) {
			// associate the motionHandler to the list of motionSensors in rooms   	 
			subscribe(motionSensor, "motion", "motionEvtHandler${i}", [filterEvents: false])
		}            
	}        
   
    
	state?.poll = [ last: 0, rescheduled: now() ]

	Integer delay =5 				// wake up every 5 minutes to apply zone settings if any
	log.debug "initialize>scheduling setZoneSettings every ${delay} minutes to check for zone settings to be applied"

	//Subscribe to different events (ex. sunrise and sunset events) to trigger rescheduling if needed
	subscribe(location, "sunrise", rescheduleIfNeeded)
	subscribe(location, "sunset", rescheduleIfNeeded)
	subscribe(location, "mode", rescheduleIfNeeded)
	subscribe(location, "sunriseTime", rescheduleIfNeeded)
	subscribe(location, "sunsetTime", rescheduleIfNeeded)

	rescheduleIfNeeded()
}

def rescheduleIfNeeded(evt) {
	if (evt) log.debug("rescheduleIfNeeded>$evt.name=$evt.value")
	Integer delay = 5 // By default, schedule SetZoneSettings() every 5 min.
	BigDecimal currentTime = now()    
	BigDecimal lastPollTime = (currentTime - (state?.poll["last"]?:0))  
	if (lastPollTime != currentTime) {    
		Double lastPollTimeInMinutes = (lastPollTime/60000).toDouble().round(1)      
		log.info "rescheduleIfNeeded>last poll was  ${lastPollTimeInMinutes.toString()} minutes ago"
	}
	if (((state?.poll["last"]?:0) + (delay * 60000) < currentTime) && canSchedule()) {
		log.info "recheduleIfNeeded>scheduling setZoneSettings in ${delay} minutes.."
		try {
			runEvery5Minutes(setZoneSettings)
		} catch (e) {
			log.debug("rescheduleIfNeeded>exception $e while trying to reschedule smartapp")
		}        
		setZoneSettings()    
	}
    
	// Update rescheduled state
    
	if (!evt) state.poll["rescheduled"] = now()
}

def appTouch(evt) {
	state.lastScheduleName=""	// force reset of the zone settings
	state.lastStartTime=null    
	rescheduleIfNeeded()
}

def setZoneSettings() {

	log.debug "Begin of setZoneSettings Fcn"
	Integer delay = 5 // By default, schedule SetZoneSettings() every 5 min.

	//schedule the rescheduleIfNeeded() function
	state?.poll["last"] = now()
    
	if (((state?.poll["rescheduled"]?:0) + (delay * 60000)) < now()) {
		log.info "setZoneSettings>Scheduling rescheduleIfNeeded() in ${delay} minutes.."
		schedule("0 0/${delay} * * * ?", rescheduleIfNeeded)
		// Update rescheduled state
		state?.poll["rescheduled"] = now()
	}
	if (powerSwitch?.currentSwitch == "off") {
		if (detailedNotif) {
			send("ScheduleRoomTempControl>${powerSwitch.name} is off, schedule processing on hold...")
		}
		return
	}

	def currTime = now()
	boolean initialScheduleSetup=false        
	boolean foundSchedule=false

	if (thermostat) {
		/* Poll or refresh the thermostat to get latest values */
		if  (thermostat?.hasCapability("Polling")) {
			try {        
				thermostat.poll()
			} catch (e) {
				if (detailedNotif) {
					log.debug("setZoneSettings>not able to do a poll() on ${thermostat}, exception ${e}")
				}                    
			}                    
		}  else if  (thermostat?.hasCapability("Refresh")) {
			try {        
				thermostat.refresh()
			} catch (e) {
				if (detailedNotif) {
					log.debug("setZoneSettings>not able to do a refresh() on ${thermostat}, exception ${e}")
				}                    
			}                
		}                    
	}                    

	def ventSwitchesOn = []
    
	String nowInLocalTime = new Date().format("yyyy-MM-dd HH:mm", location.timeZone)
	for (int i = 1;((i <= settings.schedulesCount) && (i <= 12)); i++) {
        
		def key = "selectedMode$i"
		def selectedModes = settings[key]
		key = "scheduleName$i"
		def scheduleName = settings[key]
        
		key = "noSetpointsFlag$i"
		def noSetpointInSchedule = settings[key]?: false
        
		boolean foundMode=false        
		selectedModes.each {
        
			if (it==location.mode) {
				foundMode=true            
			}            
		}        
        
		if ((selectedModes != null) && (!foundMode)) {
			if (detailedNotif) {	    
				log.debug "setZoneSettings>schedule=${scheduleName} does not apply,location.mode= $location.mode, selectedModes=${selectedModes},foundMode=${foundMode}, continue"
			}                
			continue			
		}
		key = "begintime$i"
		def startTime = settings[key]
		if (startTime == null) {
        		continue
		}
		def startTimeToday = timeToday(startTime,location.timeZone)
		key = "endtime$i"
		def endTime = settings[key]
		def endTimeToday = timeToday(endTime,location.timeZone)
		if ((currTime < endTimeToday.time) && (endTimeToday.time < startTimeToday.time)) {
			startTimeToday = startTimeToday -1        
			if (detailedNotif) {
				log.debug "setZoneSettings>schedule ${scheduleName}, subtracted - 1 day, new startTime=${startTimeToday.time}"
			}                
		}            
		if ((currTime > endTimeToday.time) && (endTimeToday.time < startTimeToday.time)) {
			endTimeToday = endTimeToday +1        
			if (detailedNotif) {
				log.debug "setZoneSettings>schedule ${scheduleName}, added + 1 day, new endTime=${endTimeToday.time}"
			}                
		}        
		String startInLocalTime = startTimeToday.format("yyyy-MM-dd HH:mm", location.timeZone)
		String endInLocalTime = endTimeToday.format("yyyy-MM-dd HH:mm", location.timeZone)

		if (detailedNotif) {
			log.debug "setZoneSettings>found schedule ${scheduleName},original startTime=$startTime,original endTime=$endTime,nowInLocalTime= ${nowInLocalTime},startInLocalTime=${startInLocalTime},endInLocalTime=${endInLocalTime}," +
        		"currTime=${currTime},begintime=${startTimeToday.time},endTime=${endTimeToday.time},lastScheduleName=$state.lastScheduleName, lastStartTime=$state.lastStartTime"
		}    	    
		def ventSwitchesZoneSet = []        
		if ((currTime >= startTimeToday.time) && (currTime <= endTimeToday.time) && (state.lastStartTime != startTimeToday.time) && (IsRightDayForChange(i))) {
        
			// let's set the given schedule
			initialScheduleSetup=true
			foundSchedule=true

			if (detailedNotif) {
				send("ScheduleRoomTempControl>now running schedule ${scheduleName},about to set zone settings as requested")
			}
            
			if ((thermostat) && (!noSetpointInSchedule)){
				if (detailedNotif) {
					send("setZoneSettings>schedule ${scheduleName},about to set the thermostat setpoint")
				}
 				set_thermostat_setpoint_in_zone(i)
			}            
			// set the zoned vent switches to 'on' and adjust them according to the ambient temperature
               
			ventSwitchesZoneSet= adjust_vent_settings_in_zone(i)
			if (detailedNotif) {
				log.debug "setZoneSettings>schedule ${scheduleName},list of Vents turned 'on'= ${ventSwitchesZoneSet}"
			}                
 			ventSwitchesOn = ventSwitchesOn + ventSwitchesZoneSet              
			state.lastScheduleName = scheduleName
			state?.lastStartTime = startTimeToday.time
		}
		else if ((state.lastScheduleName == scheduleName) && (currTime >= startTimeToday.time) && (currTime <= endTimeToday.time) && (IsRightDayForChange)) {
			// We're in the middle of a schedule run
        
			if (detailedNotif) {
				log.debug "setZoneSettings>schedule ${scheduleName},currTime= ${currTime}, current time is OK for execution, we're in the middle of a schedule run"
			}                
			foundSchedule=true
			// let's adjust the vent settings according to desired Temp only if thermostat is not idle or was not idle at the last run
			key = "adjustVentsEveryCycleFlag$i"
			def adjustVentSettings = (settings[key]) ?: false
			if (detailedNotif) {
				log.debug "setZoneSettings>adjustVentsEveryCycleFlag=$adjustVentSettings"			
			}
			if (thermostat) {
				// Check the operating State before adjusting the vents again.
				String operatingState = thermostat.currentThermostatOperatingState           
				if ((adjustVentSettings) || ((operatingState?.toUpperCase() !='IDLE') ||
					((state?.operatingState.toUpperCase() =='HEATING') || (state?.operatingState.toUpperCase() =='COOLING'))))
				{            
					if (detailedNotif) {
						log.debug "setZoneSettings>thermostat ${thermostat}'s Operating State is ${operatingState} or was just recently " +
							"${state?.operatingState}, adjusting the vents for schedule ${scheduleName}"
					}                            
					ventSwitchesZoneSet=adjust_vent_settings_in_zone(i)
					ventSwitchesOn = ventSwitchesOn + ventSwitchesZoneSet     
				}                    
				state?.operatingState =operatingState            
			}  else if (adjustVentSettings) {
				ventSwitchesZoneSet=adjust_vent_settings_in_zone(i)
				ventSwitchesOn = ventSwitchesOn + ventSwitchesZoneSet     
            
			}   
		} else {
			if (detailedNotif) {
				send("ScheduleRoomTempControl>schedule: ${scheduleName},change not scheduled at this time ${nowInLocalTime}...")
			}
		}

	} /* end for */
    
	if ((ventSwitchesOn !=[]) || (initialScheduleSetup)) {
		if (detailedNotif) {
			log.debug "setZoneSettings>list of Vents turned on= ${ventSwitchesOn}"
		}
		turn_off_all_other_vents(ventSwitchesOn)
	}
	if (!foundSchedule) {
		if (detailedNotif) {
			send "ScheduleRoomTempControl>No schedule applicable at this time ${nowInLocalTime}"
		}
	} 
}


private def isRoomOccupied(sensor, indiceRoom) {
	def key ="occupiedMotionOccNeeded${indiceRoom}"
	def nbMotionNeeded = (settings[key]) ?: 1
    // If mode is Night, then consider the room occupied.
    
	if (location.mode == "Night") {
		return nbMotionNeeded
    
	}    
	if (thermostat) {
		try {    
			String currentProgName = thermostat.currentClimateName
			if (currentProgName?.toUpperCase().contains('SLEEP')) { 
				// Rooms are considered occupied when the ecobee program is set to 'SLEEP'    
				return nbMotionNeeded
			} 
		} catch (any) {
			if (detailedNotif) {
				log.debug("isRoomOccupied>not an ecobee thermostat, continue")           
			}            
		}        
	}    
   
	key = "residentsQuietThreshold$indiceRoom"
	def threshold = (settings[key]) ?: 15 // By default, the delay is 15 minutes 

	key = "roomName$indiceRoom"
	def roomName = settings[key]


	def t0 = new Date(now() - (threshold * 60 * 1000))
	def recentStates = sensor.statesSince("motion", t0)
	def countActive =recentStates.count {it.value == "active"}
 	if (countActive>0) {
		if (detailedNotif) {
			log.debug "isRoomOccupied>room ${roomName} has been occupied, motion was detected at sensor ${sensor} in the last ${threshold} minutes"
			log.debug "isRoomOccupied>room ${roomName}, is motion counter (${countActive}) for the room >= motion occurence needed (${nbMotionNeeded})?"
		}            
		if (countActive >= nbMotionNeeded) {
			return countActive
		}            
 	}
	return 0
}

private def verify_presence_based_on_motion_in_rooms() {

	def result=false
	for (i in 1..roomsCount) {

		def key = "roomName$i"
		def roomName = settings[key]
		key = "motionSensor$i"
		def motionSensor = settings[key]
		if (motionSensor != null) {

			if (isRoomOccupied(motionSensor,i)) {
				if (detailedNotif) {
					log.debug("verify_presence_based_on_motion>in ${roomName},presence detected, return true")
				}                    
				return true
			}                
		}
	} /* end for */        
	return result
}

private def getSensorTempForAverage(indiceRoom, typeSensor='tempSensor') {
	def key 
	def currentTemp=null
	    
	if (typeSensor == 'tempSensor') {
		key = "tempSensor$indiceRoom"
	} else {
		key = "roomTstat$indiceRoom"
	}
	def tempSensor = settings[key]
	if (tempSensor != null) {
		if (detailedNotif) {    
			log.debug("getTempSensorForAverage>found sensor ${tempSensor}")
		}            
		if (tempSensor.hasCapability("Refresh")) {
			// do a refresh to get the latest temp value
			try {        
				tempSensor.refresh()
			} catch (e) {
				if (detailedNotif) {
					log.debug("getSensorTempForAverage>not able to do a refresh() on $tempSensor")
				}                    
			}                
		}        
		currentTemp = tempSensor.currentTemperature?.toFloat().round(1)
	}
	return currentTemp
}



private def getAllTempsForAverage(indiceZone) {
	def tempAtSensor

	def indoorTemps = []
	def key = "includedRooms$indiceZone"
	def rooms = settings[key]
	for (room in rooms) {

		def roomDetails=room.split(':')
		def indiceRoom = roomDetails[0]
		def roomName = roomDetails[1]

		key = "needOccupiedFlag$indiceRoom"
		def needOccupied = (settings[key]) ?: false
		if (detailedNotif) {
			log.debug("getAllTempsForAverage>looping thru all rooms,now room=${roomName},indiceRoom=${indiceRoom}, needOccupied=${needOccupied}")
		}
		if (needOccupied) {

			key = "motionSensor$indiceRoom"
			def motionSensor = settings[key]
			if (motionSensor != null) {

				if (isRoomOccupied(motionSensor, indiceRoom)) {

					tempAtSensor = getSensorTempForAverage(indiceRoom)
					if (tempAtSensor != null) {
						indoorTemps = indoorTemps + tempAtSensor.toFloat().round(1)
						if (detailedNotif) {
							log.debug("getAllTempsForAverage>added ${tempAtSensor.toString()} due to occupied room ${roomName} based on ${motionSensor}")
						}                            
					}
					tempAtSensor = getSensorTempForAverage(indiceRoom,'roomTstat')
					if (tempAtSensor != null) {
						indoorTemps = indoorTemps + tempAtSensor.toFloat().round(1)
						if (detailedNotif) {
							log.debug("getAllTempsForAverage>added ${tempAtSensor.toString()} due to occupied room ${roomName} based on ${motionSensor}")
						}                            
					}
				}
			}
		} else {
			tempAtSensor = getSensorTempForAverage(indiceRoom)
			if (tempAtSensor != null) {
				if (detailedNotif) {
					log.debug("getAllTempsForAverage>added ${tempAtSensor.toString()} in room ${roomName}")
				}                    
				indoorTemps = indoorTemps + tempAtSensor.toFloat().round(1)
			}
			tempAtSensor = getSensorTempForAverage(indiceRoom,'roomTstat')
			if (tempAtSensor != null) {
				indoorTemps = indoorTemps + tempAtSensor.toFloat().round(1)
				if (detailedNotif) {
	 				log.debug("getAllTempsForAverage>added ${tempAtSensor.toString()} in room ${roomName}")
				}                    
			}
		}
	} /* end for */
	return indoorTemps

}

private def set_thermostat_setpoint_in_zone(indiceSchedule) {
	def scale = getTemperatureScale()
	float desiredHeat, desiredCool

	def key = "scheduleName$indiceSchedule"
	def scheduleName = settings[key]
	key = "includedZones$indiceSchedule"
	def zones = settings[key]
	key = "givenClimate$indiceSchedule"
	def climateName = settings[key]

	float currentTemp = thermostat?.currentTemperature.toFloat().round(1)
	String mode = thermostat?.currentThermostatMode.toString()
	if (mode in ['heat', 'auto', 'emergency heat']) {
		if ((climateName) && (thermostat.hasCommand("setClimate"))) {
			try {
				thermostat.setClimate("", climateName)
				thermostat.refresh() // to get the latest setpoints
			} catch (any) {
				if (detailedNotif) {
					send("ScheduleRoomTempControl>schedule ${scheduleName}:not able to set climate ${climateName} for heating at the thermostat ${thermostat}")
				}
				if (detailedNotif) {
					log.debug("adjust_thermostat_setpoint_in_zone>schedule ${scheduleName}:not able to set climate  ${climateName} for heating at the thermostat ${thermostat}")
				}                    
			}                
			desiredHeat = thermostat.currentHeatingSetpoint
			if (detailedNotif) {
				log.debug("set_thermostat_setpoint_in_zone>schedule ${scheduleName},according to climateName ${climateName}, desiredHeat=${desiredHeat}")
			}                
		} else {
			if (detailedNotif) {
				log.debug("set_thermostat_setpoint_in_zone>schedule ${scheduleName}:no climate to be applied for heatingSetpoint")
			}                
			key = "desiredHeat$indiceSchedule"
			def heatTemp = settings[key]
			if (!heatTemp) {
				if (detailedNotif) {
					log.debug("set_thermostat_setpoint_in_zone>schedule ${scheduleName}:about to apply default heat settings")
				}                    
				desiredHeat = (scale=='C') ? 21:72 					// by default, 21C/72F is the target heat temp
			} else {
				desiredHeat = heatTemp.toFloat()
			}
			if (detailedNotif) {
				log.debug("set_thermostat_setpoint_in_zone>schedule ${scheduleName},desiredHeat=${desiredHeat}")
			}                
			thermostat?.setHeatingSetpoint(desiredHeat)
		} 
		if (detailedNotif) {
			send("ScheduleRoomTempControl>schedule ${scheduleName},in zones=${zones},heating setPoint now =${desiredHeat}")
		}
		if (scheduleName != state.lastScheduleLastName) {
			state.scheduleHeatSetpoint=desiredHeat 
		}  
	}        
	if (mode in ['cool', 'auto']) {
		if ((climateName) && (thermostat.hasCommand("setClimate"))) {
			try {
				thermostat?.setClimate("", climateName)
				thermostat.refresh() // to get the latest setpoints
			} catch (any) {
				if (detailedNotif) {
					send("ScheduleRoomTempControl>schedule ${scheduleName},not able to set climate ${climateName} for cooling at the thermostat(s) ${thermostat}")
				}
			}                
			desiredCool = thermostat.currentCoolingSetpoint
			if (detailedNotif) {
				log.debug("set_thermostat_setpoint_in_zone>schedule ${scheduleName},according to climateName ${climateName}, desiredCool=${desiredCool}")
			}                
		} else {
			if (detailedNotif) {
				log.debug("set_thermostat_setpoint_in_zone>schedule ${scheduleName}:no climate to be applied for coolingSetpoint")
			}                
			key = "desiredCool$indiceSchedule"
			def coolTemp = settings[key]
			if (!coolTemp) {
				log.debug("set_thermostat_setpoint_in_zone>schedule ${scheduleName},about to apply default cool settings")
				desiredCool = (scale=='C') ? 23:75					// by default, 23C/75F is the target cool temp
			} else {
				desiredCool = coolTemp.toFloat()
			}
            
			if (detailedNotif) {
				log.debug("set_thermostat_setpoint_in_zone>schedule ${scheduleName},desiredCool=${desiredCool}")
			}                
		} 
		if (detailedNotif) {
			send("ScheduleRoomTempControl>schedule ${scheduleName}, in zones=${zones},cooling setPoint now =${desiredCool}")
		}            
		if (scheduleName != state.lastScheduleLastName) {
			state.scheduleCoolSetpoint=desiredCool 
		}        
		thermostat?.setCoolingSetpoint(desiredCool)
	} /* else if mode == 'cool' */

}


private def adjust_vent_settings_in_zone(indiceSchedule) {
	def MIN_OPEN_LEVEL_IN_ZONE=(minVentLevelInZone)?((minVentLevelInZone>=0 && minVentLevelInZone <100)?minVentLevelInZone:10):10
	float desiredTemp,total_temp_in_vents=0
	def indiceRoom
	boolean closedAllVentsInZone=true
	int nbVents=0, openVentsCount=0,total_level_vents=0
	def switchLevel    
	def ventSwitchesOnSet=[]
	def scale= getTemperatureScale()
    
	def key = "scheduleName$indiceSchedule"
	def scheduleName = settings[key]

	key = "includedZones$indiceSchedule"
	def zones = settings[key]
 
	if (detailedNotif) {
		log.debug("adjust_vent_settings_in_zone>schedule ${scheduleName}: zones= ${zones}")
	}        

	float currentTempAtTstat =(scale=='C')?21:72
	String mode
	if (thermostat) {
		currentTempAtTstat = thermostat.currentTemperature.toFloat().round(1)
 		mode = thermostat.currentThermostatMode.toString()
	}        
	key = "setVentLevel${indiceSchedule}"
	def defaultSetLevel = settings[key]
	key = "resetLevelOverrideFlag${indiceSchedule}"	
	boolean resetLevelOverrideFlag = settings[key]
	def fullyCloseVents = (fullyCloseVentsFlag) ?: false
	state?.activeZones=zones
	def min_open_level=100, max_open_level=0, nbRooms=0    
	float min_temp_in_vents=200, max_temp_in_vents=0, total_temp_diff=0    
    
	state?.activeZones = zones // save the zones for the dashboard                
  	
	for (zone in zones) {

		def zoneDetails=zone.split(':')
		def indiceZone = zoneDetails[0]
		def zoneName = zoneDetails[1]
		key = "includedRooms$indiceZone"
		def rooms = settings[key]
		if (mode=='cool') {
			key = "desiredCoolTemp$indiceZone"
			def desiredCool= settings[key]
			if (!desiredCool) {            
				desiredCool = (scale=='C') ? 23:75 		// by default, 23C/75F is the target cool temp
			}                
			desiredTemp= desiredCool.toFloat()                
		} else if (mode == 'auto') {
			key = "desiredCoolTemp$indiceZone"
			def desiredCool= settings[key]
			if (!desiredCool) {            
				desiredCool = (scale=='C') ? 23:75 		// by default, 23C/75F is the target cool temp
			}                
			key = "desiredHeatTemp$indiceZone"
			def desiredHeat= settings[key]
			if (!desiredHeat) {            
				desiredHeat = (scale=='C') ? 21:72 		// by default, 21C/72F is the target heat temp
			}                
			desiredTemp= ((desiredHeat+desiredCool)/2).toFloat().round(1)
        
		} else {
			key = "desiredHeatTemp$indiceZone"
			def desiredHeat= settings[key]
			if (!desiredHeat) {            
				desiredHeat = (scale=='C') ? 21:72 		// by default, 21C/72F is the target heat temp
			}                
            
			desiredTemp= desiredHeat.toFloat()
		}
		for (room in rooms) {
        
			nbRooms++        
			if (detailedNotif) {
				log.debug("adjust_vent_settings_in_zone>schedule ${scheduleName}, desiredTemp=${desiredTemp}")
			}                

			switchLevel =null	// initially set to null for check later
			def roomDetails=room.split(':')
			indiceRoom = roomDetails[0]
			def roomName = roomDetails[1]
			key = "needOccupiedFlag$indiceRoom"
			def needOccupied = (settings[key]) ?: false
			if (detailedNotif) {
				log.debug("adjust_vent_settings_in_zone>looping thru all rooms,now room=${roomName},indiceRoom=${indiceRoom}, needOccupied=${needOccupied}")	
			}                

			if (needOccupied) {
				key = "motionSensor$indiceRoom"
				def motionSensor = settings[key]
				if (motionSensor != null) {
					if (!isRoomOccupied(motionSensor, indiceRoom)) {
						switchLevel = (fullyCloseVents)? 0: MIN_OPEN_LEVEL_IN_ZONE // setLevel at a minimum as the room is not occupied.
						if (detailedNotif) {
							log.debug("adjust_vent_settings_in_zone>schedule ${scheduleName}, in zone ${zoneName}, room = ${roomName},not occupied,vents set to mininum level=${switchLevel}")
						}                            
					}
				}
			} 
			if (switchLevel ==null) {
				def tempAtSensor =getSensorTempForAverage(indiceRoom)			
				if (tempAtSensor == null) {
					tempAtSensor= currentTempAtTstat				            
				}
                
				float temp_diff_at_sensor = (tempAtSensor - desiredTemp).toFloat().round(1)
				total_temp_diff =  total_temp_diff + temp_diff_at_sensor                
				if (detailedNotif) {
					log.debug("adjust_vent_settings_in_zone>thermostat mode = ${mode}, schedule ${scheduleName}, in zone ${zoneName}, room ${roomName}, temp_diff_at_sensor=${temp_diff_at_sensor}")
				}                    
				if ((mode=='cool') || ((mode=='auto') && (currentTempAtTstat>desiredTemp)))  {
					switchLevel=(temp_diff_at_sensor <=0)? ((fullyCloseVents) ? 0: MIN_OPEN_LEVEL_IN_ZONE): 100
				} else  {
					switchLevel=(temp_diff_at_sensor >=0)? ((fullyCloseVents) ? 0: MIN_OPEN_LEVEL_IN_ZONE): 100
				}                
			} 
                
			for (int j = 1;(j <= 5); j++)  {
				key = "ventSwitch${j}$indiceRoom"
				def ventSwitch = settings[key]
				if (ventSwitch != null) {
					if (detailedNotif) {
						log.debug("adjust_vent_settings_in_zone>schedule ${scheduleName}, in zone ${zoneName}, room ${roomName},switchLevel to be set=${switchLevel}")
					}                        
					float temp_in_vent=ventSwitch.currentValue("temperature")     
					// compile some stats for the dashboard                    
					min_temp_in_vents=(temp_in_vent < min_temp_in_vents)? temp_in_vent.toFloat().round(1): min_temp_in_vents
					max_temp_in_vents=(temp_in_vent > max_temp_in_vents)? temp_in_vent.toFloat().round(1): max_temp_in_vents
					total_temp_in_vents=total_temp_in_vents + temp_in_vent
					def switchOverrideLevel=null                 
					nbVents++
					if (!resetLevelOverrideFlag) {
						key = "ventLevel${j}$indiceRoom"
						switchOverrideLevel = settings[key]
					}                        
					if (switchOverrideLevel) {                
						if (detailedNotif) {
							log.debug "adjust_vent_settings_in_zone>in zone=${zoneName},room ${roomName},set ${ventSwitch} at switchOverrideLevel =${switchOverrideLevel}%"
						}                            
						switchLevel = ((switchOverrideLevel >= 0) && (switchOverrideLevel<= 100))? switchOverrideLevel:switchLevel                     
					} else if (defaultSetLevel)  {
						if (detailedNotif) {
							log.debug "adjust_vent_settings_in_zone>in zone=${zoneName},room ${roomName},set ${ventSwitch} at defaultSetLevel =${defaultSetLevel}%"
						}                            
						switchLevel = ((defaultSetLevel >= 0) && (defaultSetLevel<= 100))? defaultSetLevel:switchLevel                     
					}
					setVentSwitchLevel(indiceRoom, ventSwitch, switchLevel)                    
					// compile some stats for the dashboard                    
					min_open_level=(switchLevel.toInteger() < min_open_level)? switchLevel.toInteger() : min_open_level
					max_open_level=(switchLevel.toInteger() > max_open_level)? switchLevel.toInteger() : max_open_level
					total_level_vents=total_level_vents + switchLevel.toInteger()
					if (switchLevel >= MIN_OPEN_LEVEL_IN_ZONE) {      // make sure that the vents are set to a minimum level, otherwise they are considered to be closed              
						ventSwitchesOnSet.add(ventSwitch)
						closedAllVentsInZone=false
						openVentsCount++    
					}                        
				}                
			} /* end for ventSwitch */                             
		} /* end for rooms */
	} /* end for zones */

	if ((!fullyCloseVents) && (closedAllVentsInZone) && (nbVents)) {
		    	
		switchLevel= MIN_OPEN_LEVEL_IN_ZONE
		ventSwitchesOnSet=control_vent_switches_in_zone(indiceSchedule, switchLevel)		    
		if (detailedNotif) {
			send("ScheduleRoomTempControl>schedule ${scheduleName},safeguards on:set all ventSwitches at ${switchLevel}% to avoid closing all of them")
		}
	}    
	// Save the stats for the dashboard
    
	state?.openVentsCount=openVentsCount
	state?.maxOpenLevel=max_open_level
	state?.minOpenLevel=min_open_level
	state?.minTempInVents=min_temp_in_vents
	state?.maxTempInVents=max_temp_in_vents
	if (total_temp_in_vents) {
		state?.avgTempInVents= (total_temp_in_vents/nbVents).toFloat().round(1)
	}		        
	if (total_level_vents) {    
		state?.avgVentLevel= (total_level_vents/nbVents).toFloat().round(1)
	}		        
	if (total_temp_diff) {
		state?.avgTempDiff = (total_temp_diff/ nbRooms).toFloat().round(1)    
	}		        
	return ventSwitchesOnSet    
}

private def turn_off_all_other_vents(ventSwitchesOnSet) {
	def MIN_OPEN_LEVEL_SMALL=(minVentLevelOutZone)?((minVentLevelOutZone>=0 && minVentLevelOutZone <100)?minVentLevelOutZone:25):25
	def MIN_OPEN_LEVEL_IN_ZONE=(minVentLevelInZone)?((minVentLevelInZone>=0 && minVentLevelInZone <100)?minVentLevelInZone:10):10
	def foundVentSwitch
	int nbClosedVents=0, totalVents=0
	float MAX_RATIO_CLOSED_VENTS=50 // not more than 50% of the smart vents should be closed at once
	def closedVentsSet=[]
	def fullyCloseVents = (fullyCloseVentsFlag) ?: false
    
	for (indiceRoom in 1..roomsCount) {
		for (int j = 1;(j <= 5); j++)  {
			def key = "ventSwitch${j}$indiceRoom"
			def ventSwitch = settings[key]
			if (ventSwitch != null) {
				totalVents++

				if (detailedNotif) {
					log.debug "turn_off_all_other_vents>found=${ventSwitch}, checking if it's in the ventOn set"
				}                    
				foundVentSwitch = ventSwitchesOnSet.find{it == ventSwitch}
				if (foundVentSwitch ==null) {
					if (detailedNotif) {
						log.debug "turn_off_all_other_vents>${ventSwitch} not found in ventOn set, need to be closed "
					}                        
					nbClosedVents++ 
					closedVentsSet.add(ventSwitch)                        
				} else {
					def ventLevel= ventSwitch.currentValue("level")							                        
					if ((ventLevel!=null) && (ventLevel < MIN_OPEN_LEVEL_IN_ZONE)) { // below minimum level in Zone is considered as closed.
						nbClosedVents++ 
						closedVentsSet.add(ventSwitch)                        
						if (detailedNotif) {
							log.debug("turn_off_all_other_vents>${ventSwitch}'s level=${ventLevel} is lesser than minimum level ${MIN_OPEN_LEVEL_IN_ZONE}")
						}                            
  					}                        
				} /* else if foundSwitch==null */                    
			}   /* end if ventSwitch */                  
		}  /* end for ventSwitch */         
	} /* end for rooms */
	state?.closedVentsCount= nbClosedVents                     
	state?.totalVents=totalVents 
	state?.ratioClosedVents =0   
    
	if (totalVents >0) {    
		float ratioClosedVents=((nbClosedVents/totalVents).toFloat()*100)
		state?.ratioClosedVents=ratioClosedVents.round(1)
    
		if ((!fullyCloseVents) && (ratioClosedVents > MAX_RATIO_CLOSED_VENTS)) {
			if (detailedNotif) {
				send("ScheduleRoomTempControl>ratio of closed vents is too high (${ratioClosedVents.round()}%), opening ${closedVentsSet} at minimum level of ${MIN_OPEN_LEVEL_SMALL}%")
			}
			closedVentsSet.each {
				setVentSwitchLevel(null, it, MIN_OPEN_LEVEL_SMALL)
			}        
		} else {
			closedVentsSet.each {
				setVentSwitchLevel(null, it, 0)
				if (detailedNotif) {
					log.debug("turn_off_all_other_vents>turned off ${it} as requested to create the desired zone(s)")
				}                    
			}        
		}        
	} /* if totalVents >0 */        
}


private def open_all_vents() {
	// Turn on all vents        
	for (indiceRoom in 1..roomsCount) {
		for (int j = 1;(j <= 5); j++)  {
			def key = "ventSwitch${j}$indiceRoom"
			def vent = settings[key]
				if (vent != null) {
					setVentSwitchLevel(null, vent, 100)	
			} /* end if vent != null */
		} /* end for vent switches */
	} /* end for rooms */
}

private def getTemperatureInVent(ventSwitch) {
	def temp=null
	try {
		temp = ventSwitch.currentTemperature
	} catch (any) {
		log.debug("getTemperatureInVent>Not able to current Temperature from ${ventSwitch}")
	}    
	return temp    
}

private def setVentSwitchLevel(indiceRoom, ventSwitch, switchLevel=100) {
	def roomName
    
	if (indiceRoom) {
		def key = "roomName$indiceRoom"
		roomName = settings[key]
	}
	try {
		ventSwitch.setLevel(switchLevel)
		if (roomName) {       
			if (detailedNotif) {
				send("ScheduleRoomTempControl>set ${ventSwitch} at level ${switchLevel} in room ${roomName} to reach desired temperature")
			}
		}            
	} catch (e) {
		if (switchLevel >0) {
			ventSwitch.on()        
			log.error "setVentSwitchLevel>not able to set ${ventSwitch} at ${switchLevel} (exception $e), trying to turn it on"
		} else {
			ventSwitch.off()        
			log.error "setVentSwitchLevel>not able to set ${ventSwitch} at ${switchLevel} (exception $e), trying to turn it off"
		}
	}
    
}

private def control_vent_switches_in_zone(indiceSchedule, switchLevel=100) {
	def key = "scheduleName$indiceSchedule"
	def scheduleName = settings[key]

	key = "includedZones$indiceSchedule"
	def zones = settings[key]
	def ventSwitchesOnSet=[]
    
	for (zone in zones) {

		def zoneDetails=zone.split(':')
		def indiceZone = zoneDetails[0]
		def zoneName = zoneDetails[1]
		key = "includedRooms$indiceZone"
		def rooms = settings[key]
    
		for (room in rooms) {
			def roomDetails=room.split(':')
			def indiceRoom = roomDetails[0]
			def roomName = roomDetails[1]

			for (int j = 1;(j <= 5); j++)  {
	                
				key = "ventSwitch${j}$indiceRoom"
				def ventSwitch = settings[key]
				if (ventSwitch != null) {
					ventSwitchesOnSet.add(ventSwitch)
					setVentSwitchLevel(indiceRoom, ventSwitch, switchLevel)
				}
			} /* end for ventSwitch */
		} /* end for rooms */
	} /* end for zones */
	return ventSwitchesOnSet
}


def IsRightDayForChange(indiceSchedule) {

	def key = "scheduleName$indiceSchedule"
	def scheduleName = settings[key]

	key ="dayOfWeek$indiceSchedule"
	def dayOfWeek = settings[key]
	def makeChange = false
	Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
	int currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK);

	// Check the condition under which we want this to run now
	// This set allows the most flexibility.
	if (dayOfWeek == 'All Week') {
		makeChange = true
	} else if ((dayOfWeek == 'Monday' || dayOfWeek == 'Monday to Friday') && currentDayOfWeek == Calendar.instance.MONDAY) {
		makeChange = true
	} else if ((dayOfWeek == 'Tuesday' || dayOfWeek == 'Monday to Friday') && currentDayOfWeek == Calendar.instance.TUESDAY) {
		makeChange = true
	} else if ((dayOfWeek == 'Wednesday' || dayOfWeek == 'Monday to Friday') && currentDayOfWeek == Calendar.instance.WEDNESDAY) {
		makeChange = true
	} else if ((dayOfWeek == 'Thursday' || dayOfWeek == 'Monday to Friday') && currentDayOfWeek == Calendar.instance.THURSDAY) {
		makeChange = true
	} else if ((dayOfWeek == 'Friday' || dayOfWeek == 'Monday to Friday') && currentDayOfWeek == Calendar.instance.FRIDAY) {
		makeChange = true
	} else if ((dayOfWeek == 'Saturday' || dayOfWeek == 'Saturday & Sunday') && currentDayOfWeek == Calendar.instance.SATURDAY) {
		makeChange = true
	} else if ((dayOfWeek == 'Sunday' || dayOfWeek == 'Saturday & Sunday') && currentDayOfWeek == Calendar.instance.SUNDAY) {
		makeChange = true
	}

	return makeChange
    
}


private send(msg) {
	if (sendPushMessage != "No") {
		sendPush(msg)
	}

	if (phone) {
		log.debug("sending text message")
		sendSms(phone, msg)
	}
	log.debug msg
}