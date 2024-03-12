package com.sefeas.wifi;

import com.sefeas.CryptoUtils;
import com.sefeas.beans.*;
import com.sefeas.controllers.UserController;
import com.sefeas.dao.DevicesDAO;
import com.sefeas.dao.ForgetPasswordDAO;
import com.sefeas.dao.LoginDAO;
import com.sefeas.dao.SchedulerDAO;
import com.sefeas.repository.*;
import com.sefeas.server.socket.Connection;
import com.sefeas.server.socket.TcpSocket;
import com.sefeas.service.UsersService;
import com.sefeas.util.DeviceResponse;
import org.apache.catalina.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class WifiUsersJpaPostgresApplicationTests {

	@Autowired
	LoginRepository loginRepository;
//	@Autowired
//	DevicesRepository devicesRepository;
//	@Autowired
//	SchedulerRepository schedulerRepository;
//	@Autowired
//	ShareRoomRepository shareRoomRepository;
	@Autowired
	UserCheckRepository userCheckRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ForgetPasswordDAO forgetPasswordDao;
	@Autowired
	UserController userController;
	@Autowired
	UsersService userService;
//	@Autowired
//	DevicesRepository devicerpo;
//	@Autowired
//	ShareRoomRepository shareroomrpo;
	@Autowired
	LoginDAO loginDAO;
	@Autowired
	DevicesDAO deviceDAO;
	@Autowired
	SchedulerDAO schedulerDAO;
	@Autowired
	TcpSocket serverSocket;
	@Autowired
	CryptoUtils cryptoUtils;


	@Test
	public void testVerifylogin_VerifiedUser() {
		// Create a mock LoginUsers object with correct credentials
		LoginUsers loginUsers = new LoginUsers();
		loginUsers.setMobileNo("9030298666");
		loginUsers.setPassword("1234");

		ForgetPasswordDAO forgetPasswordDAO = mock(ForgetPasswordDAO.class);

		// Make the API call
		Response<LoginUsers> response = userController.verifyLogin(loginUsers);

		// Assert that the response matches the expected behavior
		assertEquals("200", response.getStatus());
		assertEquals("success!", response.getMessage());
		assertTrue(response.isSuccess());
	}


	// what is mock in junit
	//a fake class that can be examined after the test is finished for its interactions with the class under test.
	// For example, you can ask it whether a method was called or how many times it was called.
	@Test
	public void testVerifyMobile_IncorrectCredentials() {
		// Create a mock LoginUsers object with incorrect credentials
		LoginUsers loginUsers = new LoginUsers();
		loginUsers.setMobileNo("9030298666");
		loginUsers.setPassword("1111");

		// Make the API call
		Response<LoginUsers> response = userController.verifyLogin(loginUsers);

		// Assert that the response matches the expected behavior
		assertEquals("400", response.getStatus());
		assertEquals("Entered Mobile Number or password is incorrect! or Mobile Number Not Registred", response.getMessage());
	}
	@Test
	public void testVerifyMobile_UnverifiedUser() {
		// Create a mock LoginUsers object with correct credentials
		LoginUsers loginUsers = new LoginUsers();
		loginUsers.setMobileNo("8328112012"); // this mobile number is not verified
		loginUsers.setPassword("1234");

		// Make the API call
		Response<LoginUsers> response = userController.verifyLogin(loginUsers);

		// Assert that the response matches the expected behavior
		assertEquals("502", response.getStatus());
		assertEquals("Please verify your mobile number", response.getMessage());
	}
	@Test
	public void testSaveDevices_NoDeviceName() {
		// Create a mock Devices object with no device name
		Devices devices = new Devices();
		devices.setMobileNo("9030298666");
		devices.setDeviceId("1234526");
		devices.setDeviceName("");
		devices.setRoomName("Living Room");
		devices.setModelName("Smart Bulb");

		// Make the API call
		Response<Devices> response = userController.saveDevices(devices);

		// Assert that the response matches the expected behavior
		assertEquals("200", response.getStatus());
		assertEquals("success!", response.getMessage());
		assertEquals(devices, response.getData());
	}

	@Test
	public void testSaveDevices_WithDeviceName() {
		// Create a mock Devices object with a device name
		Devices devices = new Devices();
		devices.setMobileNo("1234567800");
		devices.setDeviceId("123456");
		devices.setDeviceName("Smart Bulb 1");
		devices.setRoomName("Living Room");
		devices.setModelName("Smart Bulb");


		// Make the API call
		Response<Devices> response = userController.saveDevices(devices);

		// Assert that the response matches the expected behavior
		assertEquals("200", response.getStatus());
		assertEquals("success!", response.getMessage());
		assertEquals(devices, response.getData());

	}
	@Test
	public void testGetDevices() {
		// Create a mock devices list
		List<Devices> devicesList = new ArrayList<>();
		Devices device1 = new Devices();
		device1.setDeviceName("Smart Bulb 1");
		device1.setRoomName("Living Room");
		device1.setModelName("Smart Bulb");
		devicesList.add(device1);

		Devices device2 = new Devices();
		device2.setDeviceName("Smart Bulb 2");
		device2.setRoomName("Bedroom");
		device2.setModelName("Smart Bulb");
		devicesList.add(device2);

		// Make the API call
		DeviceResponse response = userController.getDevices("1234567890");

		// Assert that the response matches the expected behavior
		assertEquals("200", response.getStatus());
		assertEquals("success!", response.getMessage());
	}
	@Test
	public void testAllOnOff_DeviceOffline() {
		// Create a mock Command object
		Command command = new Command();
		command.setDeviceId("123456");
		command.setMobileNo("9876543210");
		command.setStatus("ON");

		// Make the API call
		ResponseEntity<Object> responseEntity = userController.allOnOff(command);

		// Assert that the response matches the expected behavior
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Response<Object> response = (Response<Object>) responseEntity.getBody();
		assertEquals("200", response.getStatus());
		assertEquals("connection objevct of device is null", response.getMessage());
	}
	@Test
	public void testGetAllUsers(){
		List<Users> usersList = new ArrayList<>();
		Users users=new Users();
		users.setUsername("user1");
		users.setMobileNo("12234567890");
		users.setEmail("user@gmail.com");
		users.setPassword("1234");
		users.setAddress("ecillll");

		Users users1=new Users();
		users1.setUsername("user2");
		users1.setMobileNo("12335567890");
		users1.setEmail("user2@gmail.com");
		users1.setPassword("1234");
		users1.setAddress("vijjjj");

		List<Users> response = userController.getAllUsers();
	}
}
