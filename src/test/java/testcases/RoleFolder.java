package testcases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;
import com.google.gson.Gson;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RoleFolder {
	public static final Logger log = LogManager.getLogger(RoleFolder.class);
	public RequestSpecification requestSpec;
	public Response response;
	public String username = "BIE004";
	public String password = "Pass@123";

	private Faker faker = new Faker();

	@BeforeClass
	public void baseURL() {
		// Define the base URL of the API
		requestSpec = RestAssured.given();

		requestSpec.baseUri("http://192.168.0.173:10003");
	}

	@Test(priority = 1)
	@Step("Add Role Without Authorization")
	public void verifyAddRoleWithoutAuthorization() {
		int fakeLevel = faker.number().numberBetween(1, 10);
		HashMap<String, Object> roleMap = new HashMap<>();
		roleMap.put("role", "role");
		roleMap.put("roleLevel", fakeLevel);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(roleMap);

		requestSpec.basePath("/employee/role/add");
		response = requestSpec.contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 2)
	@Step("Get All Role Without Authorization")
	public void verifyGetAllRoleWithoutAuthorization() {
		requestSpec.basePath("/employee/role/all");
		response = requestSpec.get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 3)
	@Step("Update Role Without Authorization")
	public void updateRoleWithoutAuthorization() {
		HashMap<String, Object> roleMap = new HashMap<>();
		roleMap.put("roleId", 24);
		String fakeRole1 = faker.job().position();
		roleMap.put("role", fakeRole1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(roleMap);

		requestSpec.basePath("/employee/role/update");
		response = requestSpec.contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Code: " + response.getStatusCode());
	}

	@Test(priority = 4)
	@Step("Delete Single Role With Authorization")
	public void deleteSingleRoleWithoutAuthorization() {
		requestSpec.basePath("/employee/role/delete");
		response = requestSpec.contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 5)
	@Step("Get Role By Level Without Authorization")
	public void getRoleByLevelWithoutAuthorization() {
		int fakeLevel = faker.number().numberBetween(1, 10);
		requestSpec.basePath("/employee/role/level" + fakeLevel);
		response = requestSpec.get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Body: " + response.getBody().asPrettyString());
	}

	@Test(priority = 6)
	@Step("Add Role With Authorization")
	public void verifyAddRoleWithAuthorization() {
		String fakeRole1 = faker.job().position();
		int fakeLevel = faker.number().numberBetween(1, 10);
		HashMap<String, Object> roleMap = new HashMap<>();
		roleMap.put("role", fakeRole1);
		roleMap.put("roleLevel", fakeLevel);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(roleMap);

		requestSpec.basePath("/employee/role/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		// Extract all keys from the response as a Map
		Map<String, ?> allKeys = response.jsonPath().getMap("");

		// Print all keys
		List<String> keyList = new ArrayList<>(allKeys.keySet());
		System.out.println("All Keys: " + keyList);

		// Choose a random key from the list
		String selectedRoleId = getRandomRoleId(keyList);

		log.info("Response Code: " + response.getStatusCode());

		// Check the response status code
		if (response.getStatusCode() == 201) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 201);
		} else if (response.getStatusCode() == 422) {
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "Role Already Exists");
		} else {
			// Handle other status codes if needed
			log.info("Unexpected status code: " + response.getStatusCode());
		}

		log.info("Response Time: " + response.getTime());

		String contentType = response.getHeader("Content-Type");
		log.info("Content Type header value is: " + contentType);
		Assert.assertEquals(contentType, "application/json", "invalid content type value");

		String transferEncoding = response.getHeader("Transfer-Encoding");
		log.info("Transfer Encoding header value is: " + transferEncoding);
		Assert.assertEquals(transferEncoding, "chunked", "invalid transfer encoding value");

		String connection = response.getHeader("Connection");
		log.info("Connection header value is: " + connection);
		Assert.assertEquals(connection, "keep-alive", "invalid connection value");

		// read all header key value
		Headers headersList = response.getHeaders();

		for (Header header : headersList) {
			log.info("Key: " + header.getName() + " Value: " + header.getValue());
		}

		String fakeRole = response.jsonPath().getString(selectedRoleId);
		addRoleWithSamePayloadAsPrevious(fakeRole);
		Integer roleId = Integer.parseInt(selectedRoleId);
		deleteSingleRoleWithAuthorization(roleId);
	}

	@Test(priority = 7, dependsOnMethods = "verifyAddRoleWithAuthorization")
	@Step("Add Role With Same Payload As Previous")
	public String addRoleWithSamePayloadAsPrevious(String fakeRole) {
		HashMap<String, Object> roleMap = new HashMap<>();
		roleMap.put("role", "Front_End_Developer");
		roleMap.put("roleLevel", 4);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(roleMap);

		requestSpec.basePath("/employee/role/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Role Already Exists");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 422, "Invalid status code");
		log.info("Response Time: " + response.getTime());

		return fakeRole;
	}

	@Test(priority = 8)
	@Step("Add Role With Invalid Payload")
	public void addRoleWithInvalidPayload() {
		HashMap<String, Object> roleMap = new HashMap<>();
		roleMap.put("roleLevel", 10);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(roleMap);

		requestSpec.basePath("/employee/role/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Fields are Missing");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 400, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 9)
	@Step("Add Role With Same Role Level In Payload As Previous")
	public void addRoleWithSameRoleLevelInPayloadAsPrevious() {
		String fakeRole1 = faker.job().position();
		HashMap<String, Object> roleMap = new HashMap<>();
		roleMap.put("role", fakeRole1);
		roleMap.put("roleLevel", 2);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(roleMap);

		requestSpec.basePath("/employee/role/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		if (response.getStatusCode() == 422) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 422, "Invalid status code");
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "Role Already Exists");
		}

		log.info("Response Code: " + response.getStatusCode());
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 10)
	@Step("Get All Role With Authorization")
	public void verifyGetAllRoleWithAuthorization() {
		requestSpec.basePath("/employee/role/all");
		response = requestSpec.auth().basic(username, password).get();

		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 200, "Invalid status code");
		log.info("Response Body: " + response.getBody().asPrettyString());

		// Extract all keys from the response as a Map
		Map<String, ?> allKeys = response.jsonPath().getMap("");

		// Print all keys
		List<String> keyList = new ArrayList<>(allKeys.keySet());
		System.out.println("All Keys: " + keyList);

		// Choose a random key from the list
		String selectedRoleId = getRandomRoleId(keyList);
		System.out.println(selectedRoleId);
		Integer roleId = Integer.parseInt(selectedRoleId);
		updateRoleWithAuthorization(roleId);

		String contentType = response.getHeader("Content-Type");
		log.info("Content Type header value is: " + contentType);
		Assert.assertEquals(contentType, "application/json", "invalid content type value");

		String transferEncoding = response.getHeader("Transfer-Encoding");
		log.info("Transfer Encoding header value is: " + transferEncoding);
		Assert.assertEquals(transferEncoding, "chunked", "invalid transfer encoding value");

		String connection = response.getHeader("Connection");
		log.info("Connection header value is: " + connection);
		Assert.assertEquals(connection, "keep-alive", "invalid connection value");

		// read all header key value
		Headers headersList = response.getHeaders();

		for (Header header : headersList) {
			log.info("Key: " + header.getName() + " Value: " + header.getValue());
		}
	}

	@Test(priority = 11, dependsOnMethods = "verifyGetAllRoleWithAuthorization")
	@Step("Update Role With Authorization")
	public Integer updateRoleWithAuthorization(Integer roleId) {
		HashMap<String, Object> roleMap = new HashMap<>();
		roleMap.put("roleId", roleId);
		String fakeRole1 = faker.job().position();
		roleMap.put("role", fakeRole1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(roleMap);

		requestSpec.basePath("/employee/role/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String roleId1 = String.valueOf(roleId);
		Assert.assertEquals(response.jsonPath().getString(roleId1), "ROLE_" + fakeRole1.toUpperCase());

		log.info("Response Code: " + response.getStatusCode());

		String contentType = response.getHeader("Content-Type");
		log.info("Content Type header value is: " + contentType);
		Assert.assertEquals(contentType, "application/json", "invalid content type value");

		String transferEncoding = response.getHeader("Transfer-Encoding");
		log.info("Transfer Encoding header value is: " + transferEncoding);
		Assert.assertEquals(transferEncoding, "chunked", "invalid transfer encoding value");

		String connection = response.getHeader("Connection");
		log.info("Connection header value is: " + connection);
		Assert.assertEquals(connection, "keep-alive", "invalid connection value");

		// read all header key value
		Headers headersList = response.getHeaders();

		for (Header header : headersList) {
			log.info("Key: " + header.getName() + " Value: " + header.getValue());
		}

		return roleId;
	}

	@Test(priority = 12)
	@Step("Update Role Without Giving Role Id")
	public void updateRoleWithoutGivingRoleId() {
		HashMap<String, Object> roleMap = new HashMap<>();
		roleMap.put("role", "Front_End_Developer");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(roleMap);

		requestSpec.basePath("/employee/role/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Fields are missing");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 400, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 13)
	@Step("Update Role By Giving Non Existing Role Id")
	public void updateRoleByGivingNonExistingRoleId() {
		String fakeRole = faker.job().position();
		HashMap<String, Object> roleMap = new HashMap<>();
		roleMap.put("roleId", 1);
		roleMap.put("role", fakeRole);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(roleMap);

		requestSpec.basePath("/employee/role/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No Role Present For Given Id");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 14, dependsOnMethods = { "verifyAddRoleWithAuthorization" })
	@Step("Delete Single Role With Authorization")
	public Integer deleteSingleRoleWithAuthorization(Integer fakeRoleId) {
		HashMap<String, Object> roleMap = new HashMap<>();
		roleMap.put("roleId", fakeRoleId);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(roleMap);

		requestSpec.basePath("employee/role/delete");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).delete();

		log.info("Response Body: " + response.getBody().asPrettyString());

		// Check the response status code
		if (response.getStatusCode() == 200) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 200, "Invalid status code");
		} else if (response.getStatusCode() == 404) {
			// Status already exists
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "Role Not Found");
		} else {
			// Handle other status codes if needed
			log.info("Unexpected status code: " + response.getStatusCode());
		}

		log.info("Status Code: " + response.statusCode());

		String contentType = response.getHeader("Content-Type");
		log.info("Content Type header value is: " + contentType);
		Assert.assertEquals(contentType, "application/json", "invalid content type value");

		String transferEncoding = response.getHeader("Transfer-Encoding");
		log.info("Transfer Encoding header value is: " + transferEncoding);
		Assert.assertEquals(transferEncoding, "chunked", "invalid transfer encoding value");

		String connection = response.getHeader("Connection");
		log.info("Connection header value is: " + connection);
		Assert.assertEquals(connection, "keep-alive", "invalid connection value");

		// read all header key value
		Headers headersList = response.getHeaders();

		for (Header header : headersList) {
			log.info("Key: " + header.getName() + " Value: " + header.getValue());
		}
		return fakeRoleId;
	}

	@Test(priority = 15)
	@Step("Delete Role With Invalid Role Id")
	public void deleteSingleRoleWithInvalidRoleId() {
		int fakeRoleId = faker.number().numberBetween(30, 100);
		HashMap<Object, Object> data = new HashMap<>();
		data.put("roleId", fakeRoleId);

		requestSpec.basePath("/employee/role/delete");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).delete();

		log.info("Response Body: " + response.getBody().asPrettyString());

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Role Not Found");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 16)
	@Step("Get Role By Level With Authorization")
	public void getRoleByLevelWithAuthorization() {
		int fakeLevel = faker.number().numberBetween(1, 5);
		requestSpec.basePath("/employee/role/level/" + fakeLevel);
		response = requestSpec.auth().basic(username, password).get();
		log.info("Status Code: " + response.statusCode());

		// Check the response status code
		if (response.getStatusCode() == 200) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 200, "Invalid status code");
		} else if (response.getStatusCode() == 404) {
			// Status already exists
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "Level Not Found");
		} else {
			// Handle other status codes if needed
			log.info("Unexpected status code: " + response.getStatusCode());
		}

		log.info("Response Body: " + response.getBody().asPrettyString());

		String contentType = response.getHeader("Content-Type");
		log.info("Content Type header value is: " + contentType);
		Assert.assertEquals(contentType, "application/json", "invalid content type value");

		String transferEncoding = response.getHeader("Transfer-Encoding");
		log.info("Transfer Encoding header value is: " + transferEncoding);
		Assert.assertEquals(transferEncoding, "chunked", "invalid transfer encoding value");

		String connection = response.getHeader("Connection");
		log.info("Connection header value is: " + connection);
		Assert.assertEquals(connection, "keep-alive", "invalid connection value");

		// read all header key value
		Headers headersList = response.getHeaders();

		for (Header header : headersList) {
			log.info("Key: " + header.getName() + " Value: " + header.getValue());
		}
	}

	@Test(priority = 17)
	@Step("Get Role By Level With Invalid Role Level")
	public void getRoleByLevelWithInvalidRoleLevel() {
		int fakeLevel = faker.number().numberBetween(11, 20);
		requestSpec.basePath("/employee/role/level/" + fakeLevel);
		response = requestSpec.auth().basic(username, password).get();

		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");

		log.info("Response Body: " + response.getBody().asPrettyString());

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Level Not Found");
	}

	private String getRandomRoleId(List<String> keyList) {
		Random random = new Random();
		int randomIndex = random.nextInt(keyList.size());
		return keyList.get(randomIndex);
	}
}
