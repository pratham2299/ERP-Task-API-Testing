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

public class StatusFolder {
	public static final Logger log = LogManager.getLogger(StatusFolder.class);
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
	@Step("Add Status Without Authorization")
	public void verifyAddStatusWithoutAuthorization() {
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> statusMap = new HashMap<>();
		statusMap.put("status", "Car1");
		statusMap.put("statusLevel", 6);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(statusMap);

		requestSpec.basePath("/task/status/add");
		response = requestSpec.contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 2)
	@Step("Get All Status Without Authorization")
	public void verifyGetAllStatusWithoutAuthorization() {
		requestSpec.basePath("/task/status/get/all");
		response = requestSpec.get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 3)
	@Step("Update Status Without Authorization")
	public void verifyUpdateStatusWithoutAuthorization() {
		HashMap<String, Object> statusMap = new HashMap<>();
		statusMap.put("status", "Done");
		statusMap.put("statusId", 19);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(statusMap);

		requestSpec.basePath("/task/status/update");
		response = requestSpec.contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 4)
	@Step("Delete Single Status Without Authorization")
	public void deleteSingleStatusWithoutAuthorization() {
		requestSpec.basePath("/task/status/delete/single").queryParam("statusName", "Passed");
		response = requestSpec.contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 5)
	@Step("Add Status With Authorization")
	public void verifyAddStatusWithAuthorization() {
		int fakeLevel = faker.number().numberBetween(1, 10);
		String fakeStatus1 = faker.name().lastName();
		HashMap<String, Object> statusMap = new HashMap<>();
		statusMap.put("status", fakeStatus1);
		statusMap.put("statusLevel", fakeLevel);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(statusMap);

		requestSpec.basePath("/task/status/add");
		response = requestSpec.given().auth().basic(username, password).contentType("application/json").body(payload)
				.post().then() // Assuming the response code for a successful creation is 201
				.extract().response();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		// Extract all keys from the response as a Map
		Map<String, ?> allKeys = response.jsonPath().getMap("");

		// Print all keys
		List<String> keyList = new ArrayList<>(allKeys.keySet());
		System.out.println("All Keys: " + keyList);
		// Choose a random key from the list
		String selectedStatusId = getRandomStatusId(keyList);
		String fakeStatus = response.jsonPath().getString(selectedStatusId);
		deleteSingleStatusWithAuthorization(fakeStatus);

		log.info("Response Code: " + response.getStatusCode());

		// Check the response status code
		if (response.getStatusCode() == 201) {
			// Status created successfully
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 201);
		} else if (response.getStatusCode() == 422) {
			// Status already exists
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "Status Already Exits");
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
	}

	@Test(priority = 6)
	@Step("Add Status With Same Payload As Previous")
	public void addStatusWithSamePayloadAsPrevious() {
		HashMap<String, Object> statusMap = new HashMap<>();
		statusMap.put("status", "Done");
		statusMap.put("statusLevel", 19);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(statusMap);

		requestSpec.basePath("/task/status/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Status Already Exists");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 422, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 7)
	@Step("Add Status With Invalid Payload")
	public void addStatusWithInvalidPayload() {
		HashMap<String, Object> statusMap = new HashMap<>();
		statusMap.put("statusLevel", 19);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(statusMap);

		requestSpec.basePath("/task/status/add");
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

	@Test(priority = 8)
	@Step("Add Status With Same Status Level In Payload As Previous")
	public void addStatusWithSameStatusLevelInPayloadAsPrevious() {
		String fakeStatus1 = faker.name().lastName();
		HashMap<String, Object> statusMap = new HashMap<>();
		statusMap.put("status", fakeStatus1);
		statusMap.put("statusLevel", 1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(statusMap);

		requestSpec.basePath("/task/status/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Status Already Exists");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 422, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 9)
	@Step("Get All Status With Authorization")
	public void verifyGetAllStatusWithAuthorization() {
		requestSpec.basePath("/task/status/get/all");
		response = requestSpec.auth().basic(username, password).get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 200, "Invalid status code");
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

	@Test(priority = 10)
	@Step("Update Status With Authorization")
	public void updateStatusWithAuthorization() {
		String fakeStatus1 = faker.name().firstName();
		HashMap<String, Object> statusMap = new HashMap<>();
		statusMap.put("status", fakeStatus1);
		statusMap.put("statusId", 82);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(statusMap);

		requestSpec.basePath("/task/status/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		Assert.assertEquals(response.jsonPath().getString("82"), fakeStatus1);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 200, "Invalid status code");
		String contentType = response.getHeader("Content-Type");

		Assert.assertEquals(contentType, "application/json", "invalid content type value");

		String transferEncoding = response.getHeader("Transfer-Encoding");

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

	@Test(priority = 11)
	@Step("Update Status Without Giving Status Id")
	public void updateStatusWithoutGivingStatusId() {
		String fakeStatus = faker.name().lastName();
		HashMap<String, Object> statusMap = new HashMap<>();
		statusMap.put("status", fakeStatus);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(statusMap);

		requestSpec.basePath("/task/status/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 400, "Invalid status code");

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Fields are missing");

		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 12)
	@Step("Update Status By Giving Non Existing Status Id")
	public void updateStatusByGivingNonExistingStatusId() {
		String fakeStatus = faker.name().lastName();
		HashMap<String, Object> statusMap = new HashMap<>();
		statusMap.put("statusId", 1);
		statusMap.put("status", fakeStatus);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(statusMap);

		requestSpec.basePath("/task/status/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No Status Present For Given Id");

		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 13, dependsOnMethods = { "verifyAddStatusWithAuthorization" })
	@Step("Delete Single Status With Authorization")
	public String deleteSingleStatusWithAuthorization(String fakeStatus) {
		requestSpec.basePath("/task/status/delete/single").queryParam("statusName", fakeStatus);
		response = requestSpec.auth().basic(username, password).contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		log.info("Status Code: " + response.statusCode());

		// Check the response status code
		if (response.getStatusCode() == 200) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 200, "Invalid status code");
		} else if (response.getStatusCode() == 404) {
			// Status already exists
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "No status to delete with " + fakeStatus + ".");
		} else if (response.getStatusCode() == 403) {
			// Status already exists
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "This status is mapped with multiple task");
		} else {
			// Handle other status codes if needed
			log.info("Unexpected status code: " + response.getStatusCode());
		}

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
		return fakeStatus;
	}

	@Test(priority = 14)
	@Step("Delete Single Status With Invalid Status Name")
	public void deleteSingleStatusWithInvalidStatusName() {
		String fakeStatusName = "Stopped";
		requestSpec.basePath("/task/status/delete/single").queryParam("statusName", fakeStatusName);
		response = requestSpec.auth().basic(username, password).contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
		// Status already exists
		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No status to delete with " + fakeStatusName + ".");

		log.info("Response Time: " + response.getTime());
	}

	private String getRandomStatusId(List<String> keyList) {
		Random random = new Random();
		int randomIndex = random.nextInt(keyList.size());
		return keyList.get(randomIndex);
	}
}
