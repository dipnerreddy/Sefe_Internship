@Validated
@RestController
@Api(value = "CONTROLLER", description = "All controllers present here")
@RequestMapping("/wifi")
public class UserController {

//////////// Written & Updated by Avuthu Dipner Reddy

	@Operation(summary = "Add device", description = "This approach had two functionalities. \n"
			+ " 1) A user can add new devices in existing or in a new roon  \n"
			+ " 2) And show that newly added room to the user by sending the newly added information withthe help of connection object", tags = "Post")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "New device is added and send the devices data to user mobile_no ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Devices.class))),
			@ApiResponse(responseCode = "400", description = "In the url pls enter HTTPS:// ", content = @Content(mediaType = "application/json")),

			@ApiResponse(responseCode = "404", description = "a server could not find a client-requested webpage", content = @Content(mediaType = "application/json"))})
	@PostMapping(value = "/postDevice", produces = "application/json")
	public Response<Devices> saveDevices(@Valid @RequestBody Devices devices) {
		String mobileNo = devices.getMobileNo();
		String deviceId = devices.getDeviceId();
		String deviceName = devices.getDeviceName();
		String roomName = devices.getRoomName();
		String modelName = devices.getModelName();
		log.info("device ID entered:" + deviceId + deviceName);

		log.info("going into device DAO searchRoom method");

		List<Room> rooms = deviceDAO.searchRoom(mobileNo);
		boolean roomExists = false;
		for (Room room : rooms) {
			if (Objects.equals(room.getRoomName(), devices.getRoomName())) {
				roomExists = true;
				break;
			}
		}
		if (roomExists) {
			log.info("Room existis");

			String sssave;
			String roomId;
			String setRoomId;

			String ssave = "";
			if (deviceName == "") {
				deviceName = deviceId;
				log.info("into if" + deviceName);
				ssave = deviceDAO.saveDevices(deviceId, deviceName, mobileNo, modelName,  roomName,true, true);
				Long id=  deviceDAO.getRoomId(mobileNo, roomName);
				setRoomId= String.valueOf(deviceDAO.setRoomId(id, mobileNo,roomName));
			} else {
				log.info("into else:" + deviceName);
				ssave = deviceDAO.saveDevices(deviceId, deviceName, mobileNo, modelName,roomName, true, true);

				log.info("going into room id");
				Long id=  deviceDAO.getRoomId(mobileNo, roomName);
				setRoomId= String.valueOf(deviceDAO.setRoomId(id,mobileNo,roomName));
				log.info("coming outt");
			}

			String serverId = "49.207.2.61", commandType = "C", dataSent = "", command = "", message = "";

			int desAddressLen = mobileNo.length(), sourceLen = serverId.length();
			List<Devices> deviceslist = deviceDAO.getdevices(mobileNo);
			log.info("device list from DB" + deviceslist);

			Gson gson = new Gson();
			jsonData = gson.toJson(deviceslist);
			log.info("objects to readable data:" + jsonData);

			JSONArray jsarray = new JSONArray(jsonData);
			for (int i = 0; i < jsarray.length(); i++) {
				JSONObject jsobj = jsarray.getJSONObject(i);
				if (i == 0) {
					log.info("objects to readable data:" + jsobj.getString("roomName"));
					dataSent = dataSent + jsobj.getString("roomName") + "," + jsobj.getString("deviceId") + ","
							+ jsobj.getString("modelName") + "," + jsobj.getString("deviceName");
				} else
					dataSent = dataSent + "," + jsobj.getString("roomName") + "," + jsobj.getString("deviceId") + ","
							+ jsobj.getString("modelName") + "," + jsobj.getString("deviceName");

			}

			log.info("data from Db" + dataSent);
			int dataLen = dataSent.length();
			if (dataLen < 100) {
				command = "SOP" + "0" + dataLen + desAddressLen + mobileNo + sourceLen + serverId + commandType + dataSent
						+ "EOP";

			} else {
				command = "SOP" + dataLen + desAddressLen + mobileNo + sourceLen + serverId + commandType + dataSent
						+ "EOP";

			}
			log.info("Command Formed:" + command);

			Connection connection = getServerSocket().getConnections().get(mobileNo);

			if (connection != null) {
				connection.send(command.getBytes());
				messageReceived(connection, message);
				log.info("message" + message + command.getBytes());
			}

			if (ssave.equals("")) {
				return new Response<>("200", "Device not saved!!", null, false);

			} else
				return new Response<>("200", "success!", devices, true);


		}
		else {
			return new Response<>("200", "Create a room 1st and then add that device into that room", null, true);
		}

	}


	@Operation(summary = " Adding A Room", description = "This method is used to add a new room", tags = "Post")

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Room created Successfully  ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Room.class))),
			@ApiResponse(responseCode = "400", description = "In the url pls enter HTTPS:// ", content = @Content(mediaType = "application/json")),

	})
	@PostMapping(value = "/postRoom", produces = "application/json")
	public Response<Devices> saveRoom(@Valid @RequestBody Room room) {

		String roomName=room.getRoomName();
		String mobileNo =room.getMobileNo();
		Optional<Users> usernameEntry = userService.findByMobileNo(mobileNo);

		String ssave = "";
		String sssave= "";
		if(usernameEntry.isPresent()) {
			ssave = deviceDAO.saveRoom(roomName, mobileNo);
			Long id=deviceDAO.getUserId(mobileNo);
			sssave= String.valueOf(deviceDAO.saveUserIdInRoom(mobileNo,id,roomName));


			if (ssave.equals("")) {
				return new Response<>("200", "Device not saved!!", null, false);

			} else
				return new Response<>("200", "success!", null, true);
		}
		else {
			return new Response<>("200", "Room not saved", null, true);
		}
	}



	@Operation(summary = " Deleting A Room", description = "This method is used to delete a room", tags = "Delete")

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Room deleted Successfully  ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Room.class))),
			@ApiResponse(responseCode = "400", description = "In the url pls enter HTTPS:// ", content = @Content(mediaType = "application/json")),

	})

	@DeleteMapping(value = "/deleteRoom",produces = "application/json")
	public Response<Room> deleteRoom(@Valid @RequestBody Room room){
		String mobileNo=room.getMobileNo();
		String roomName=room.getRoomName();


		deviceDAO.deleteDevices(mobileNo,roomName);

		deviceDAO.deleteRoom(mobileNo,roomName);
			return new Response<>("200", "success!", null, true);
	}

	@Operation(summary = " Deleting A User", description = "This method is used to delete a User and all his details", tags = "Delete")

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User deleted Successfully  ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Room.class))),
			@ApiResponse(responseCode = "400", description = "In the url pls enter HTTPS:// ", content = @Content(mediaType = "application/json")),

	})

	@DeleteMapping(value = "/deleteUser",produces = "application/json")
	public Response<Users> deleteUser(@Valid @RequestBody Users users){
		String mobileNo=users.getMobileNo();
		Long id=deviceDAO.getUserId(mobileNo);
		userService.deleteUser(id);
		return new Response<>("200", "success!", null, true);
	}


	@Operation(summary = "Share Room API", description = "this API is used to share the room with other registred users only three times.Here first the source mobile otp check and then stores the data in shareroom table with otp_verified as false.With  destination mobile no otp will be checked ,shareroom table  otp_verified=true and then device will be shared", tags = "Post")

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The room had shared successfully, Enter OPT in /VerifyShareRoomOTP ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShareRooms.class))),
			@ApiResponse(responseCode = "400", description = "In the url pls enter HTTPS:// ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShareRooms.class))),
			@ApiResponse(responseCode = "500", description = "mobile number is not registered ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShareRooms.class)))})
	@PostMapping(value = "/SrRoom", produces = "application/json")
	public Response<ShareRooms> srRoom(@Valid @RequestBody ShareRooms rooms){
		String dMobileNo=rooms.getdestinationMobile();
		String roomName=rooms.getRoomName();
		String sMobileNo=rooms.getSourceMobile();
		boolean activeDeactive=rooms.isActiveDeactive();
		String roomAndMobile = roomName + "," + sMobileNo;
		log.info(dMobileNo);
		log.info(sMobileNo);


		Optional<Users> usernameEntry = userService.findByMobileNo(dMobileNo);
		log.info("usernameEntry.isPresent():" + usernameEntry.isPresent());
		if (usernameEntry.isPresent()) {
			List<Devices> deviceslist = new ArrayList<>();

			deviceDAO.saveRoom(roomName,dMobileNo);
			deviceslist = deviceDAO.getdevices1(sMobileNo, roomName);
			log.info("Sixze of the list ):" + deviceslist.size() );
			if (!deviceslist.isEmpty()) {
				for (Devices srlistitem : deviceslist) {
					log.info("device list is not empty and entered into for loop");
					Long roomId =deviceDAO.getRoomId(sMobileNo,roomName);
					Long userId=deviceDAO.getUserId(dMobileNo);
					deviceDAO.shareRoomRelation1(roomId,userId,roomName,sMobileNo,dMobileNo,roomAndMobile,activeDeactive,false);
					deviceDAO.saveDevices(srlistitem.getDeviceId(), srlistitem.getDeviceName(), dMobileNo,
							srlistitem.getModelName(),roomAndMobile, false, activeDeactive);

					log.info(roomId +" <---- this is roomID &&&& "+userId+" <---- this is user id");

				}
				return new Response<>("200","shared",null,true);
			}

		}else {
			return new Response<>("200","Destination mobile no is not present",null,true);
		}
		return null;
	}

	@Operation(summary = "All rooms with devices", description = "Devices in Room shared with the specified mobile_no are retrieved  with otpverfied =true ", tags = "Get")

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "shared device with specified  mobile_no are retrieved ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceResponse.class))),
			@ApiResponse(responseCode = "400", description = "In the url pls enter HTTPS:// ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceResponse.class))),

	})

	@GetMapping(value = "/getSharedDevices", produces = "application/json")
	public DeviceResponse getSharedDevices(@Valid @RequestBody ShareRoomRelation room){
		String mobileNo=room.getSourceMobile();

		List<ShareRoomRelation> deviceslist = new ArrayList<>();


		List<ShareRoomRelation> SrRoomList=deviceDAO.getSharedRoomList(mobileNo);
		if (!SrRoomList.isEmpty()) {
			StringBuilder sharedRoomBuilder = new StringBuilder();

			for(ShareRoomRelation SrRelation : deviceslist){
				String MobileNo=SrRelation.getSourceMobile();
				String RoomName= SrRelation.getRoomName();

				String roomDetails=RoomName+","+MobileNo;

				List<ShareRoomRelation> deviceslist1=deviceDAO.getAllSharedRoomDetails(roomDetails);
				Gson gson = new Gson();
				String Data = gson.toJson(deviceslist1);
				log.info("this is gson data" + Data);
				String replacedata = Data.replace("\"", "").replace("id:0,", "").replace("},{", "}\r\n{").replace("[", "")
						.replace(",", "").replace("{deviceName:", "").replace(",modelName:", "").replace("}", "")
						.replace("]", "").replace("modelName:", "").replace("\r\n", "").replace("{roomName:", "")
						.replace("deviceName:", "");
				log.info("getDevices Data:" + replacedata);
				sharedRoomBuilder.append(replacedata);
			}
			String sharedDevices = new String(sharedRoomBuilder);
			return new DeviceResponse("200", "success!", sharedDevices, true);
		}
		else {
			return new DeviceResponse("200","Destination mobile no is not present",null,true);
		}
	}

	@Operation(summary = "All Shared rooms are retrieved", description = "Shared rooms with the specified mobile_no are retrieved  with otpverfied =true ", tags = "Get")

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "shared device with specified  mobile_no are retrieved ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceResponse.class))),
			@ApiResponse(responseCode = "400", description = "In the url pls enter HTTPS:// ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceResponse.class))),

	})
	@GetMapping(value = "/getSrRooms", produces = "application/json")
	public DeviceResponse showSrRooms(@Valid @RequestBody ShareRoomRelation room){
		String mobileNo=room.getSourceMobile();
		log.info("this is device: "+ mobileNo);
		List<ShareRoomRelation> deviceslist = new ArrayList<>();


		List<ShareRoomRelation> SrRoomList=deviceDAO.getSharedRoomList(mobileNo);
		if (!SrRoomList.isEmpty()) {
			StringBuilder sharedRoomBuilder = new StringBuilder();

			for(ShareRoomRelation SrRelation : SrRoomList){
				String MobileNo=SrRelation.getSourceMobile();
				String RoomName= SrRelation.getRoomName();

				String roomDetails=RoomName+","+MobileNo;

				List<ShareRoomRelation> deviceslist1=deviceDAO.getAllSharedRoomDetails1(roomDetails);

				log.info("this is device: "+ deviceslist1.size());
				Gson gson = new Gson();
				String Data = gson.toJson(deviceslist1);
				log.info("this is gson data" + Data);
				String replacedata = Data.replace("\"", "").replace("id:,", "").replace("},{", "}\r\n{").replace("[", "")
						.replace(",roomId:", "").replace("{deviceName:", "").replace(",userId:", "").replace(",modelName:", "").replace("}", "")
						.replace("]", "").replace("modelName:", "").replace("\r\n", "}").replace("{roomName:", "")
						.replace("deviceName:", "").replace(",sourceMobile:", "").replace(",roomName:", "").replace(",destinationMobile:", "")
						.replace(",otpVerified:","").replace(",activeDeactive:","").replace(",activeDeactive:","").replace(",roomDetails:","");

				String cleanedData = replacedata.replaceAll("\"?id\"?:\\d+\\s*", "");
				sharedRoomBuilder.append(cleanedData);
				log.info("getDevices Data:" + replacedata);
			}
			String sharedDevices = new String(sharedRoomBuilder);
			return new DeviceResponse("200", "success!", sharedDevices, true);
		}
		else {
			return new DeviceResponse("200","Destination mobile no is not present",null,true);
		}
	}

	@Operation(summary = " delete shared room", description = "This method is used when the owner wanted to remove the shared room ", tags = "Delete")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SharedRoom is deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShareRooms.class))),

			@ApiResponse(responseCode = "400", description = "In the url pls enter HTTPS:// ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShareRooms.class)))})

	@DeleteMapping(value = "/deleteSrRoom",produces = "application/json")
	public Response<ShareRoomRelation> deleteSrRoom(@Valid @RequestBody ShareRoomRelation room){
		String sMobileNo=room.getSourceMobile();
		String dMobileno=room.getDestinationMobile();
		String roomName=room.getRoomName();
		String roomDetails=roomName+","+sMobileNo;

		deviceDAO.deleteSrRooms(dMobileno,roomDetails);

		return new Response<>("200", "Room Deleted!", null, true);
	}

}
