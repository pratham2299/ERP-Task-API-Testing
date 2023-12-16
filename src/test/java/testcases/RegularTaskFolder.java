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

public class RegularTaskFolder {
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
	@Step("Add Regular Task Without Authorization")
	public void verifyAddRegularTaskWithoutAuthorization() {
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> payloadMap = new HashMap<>();
		payloadMap.put("regularTaskName", "Gmail");

		HashMap<String, Object> employeeMap = new HashMap<>();
		employeeMap.put("empId", 1);

		payloadMap.put("employee", employeeMap);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(payloadMap);

		requestSpec.basePath("/task/regular/add");
		response = requestSpec.contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 2)
	@Step("Get All Regular Task For Employee Without Authorization")
	public void verifyGetAllRegularTaskForEmployeeWithoutAuthorization() {
		HashMap<String, Object> employeeMap = new HashMap<>();
		employeeMap.put("empId", 1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(employeeMap);
		requestSpec.basePath("/task/regular/get");
		response = requestSpec.body(payload).post();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 3)
	@Step("Update Regular Task Without Authorization")
	public void verifyUpdateRegularTaskWithoutAuthorization() {
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> regularTaskMap = new HashMap<>();
		regularTaskMap.put("regularTaskName", "Gmail");
		regularTaskMap.put("regularTaskId", 5);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(regularTaskMap);

		requestSpec.basePath("/task/regular/update");
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
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> regularTaskMap = new HashMap<>();
		regularTaskMap.put("regularTaskId", 5);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(regularTaskMap);

		requestSpec.basePath("/task/regular/delete");
		response = requestSpec.contentType("application/json").body(payload).delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 5)
	@Step("Add Regular Task With Authorization")
	public void verifyAddRegularTaskWithAuthorization() {
		String fakeRegularTaskName = faker.food().dish();
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> payloadMap = new HashMap<>();
		payloadMap.put("regularTaskName", fakeRegularTaskName);

		HashMap<String, Object> employeeMap = new HashMap<>();
		employeeMap.put("empId", 1);

		payloadMap.put("employee", employeeMap);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(payloadMap);

		requestSpec.basePath("/task/regular/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		// Extract all keys from the response as a Map
		Map<String, ?> allKeys = response.jsonPath().getMap("");

		// Print all keys
		List<String> keyList = new ArrayList<>(allKeys.keySet());
		System.out.println("All Keys: " + keyList);
		// Choose a random key from the list
		String selectedRegularTaskId = getRandomRegularTaskId(keyList);

		// Check the response status code
		if (response.getStatusCode() == 201) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 201);
			log.info("Response Status Code: " + actualStatusCode);
		} else if (response.getStatusCode() == 409) {
			int actualStatusCode = response.getStatusCode();
			log.info("Response Status Code: " + actualStatusCode);
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "409 CONFLICT");
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

		String regularTaskName = response.jsonPath().getString(selectedRegularTaskId);
		addRegularTaskWithSamePayloadAsPrevious(regularTaskName);
		
		Integer regularTaskId = Integer.parseInt(selectedRegularTaskId);
		deleteSingleRegularTaskWithAuthorization(regularTaskId);
	}

	@Test(priority = 6, dependsOnMethods = "verifyAddRegularTaskWithAuthorization")
	@Step("Add Regular Task With Same Payload As Previous")
	public String addRegularTaskWithSamePayloadAsPrevious(String regularTaskName) {
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> payloadMap = new HashMap<>();
		payloadMap.put("regularTaskName", "Gmail");

		HashMap<String, Object> employeeMap = new HashMap<>();
		employeeMap.put("empId", 1);

		payloadMap.put("employee", employeeMap);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(payloadMap);

		requestSpec.basePath("/task/regular/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "409 CONFLICT");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 409, "Invalid status code");
		log.info("Response Time: " + response.getTime());

		return regularTaskName;
	}

	@Test(priority = 7)
	@Step("Add Regular Task With Invalid Payload")
	public void addRegularTaskWithInvalidPayload() {
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> payloadMap = new HashMap<>();

		HashMap<String, Object> employeeMap = new HashMap<>();
		employeeMap.put("empId", 1);

		payloadMap.put("employee", employeeMap);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(payloadMap);

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
	@Step("Get All Regular Task With Authorization")
	public void verifyGetAllRegularTaskWithAuthorization() {
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> employeeMap = new HashMap<>();
		employeeMap.put("empId", 1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(employeeMap);

		requestSpec.basePath("/task/regular/get");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();
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
		String selectedStatusId = getRandomRegularTaskId(keyList);
		Integer regularTaskId = Integer.parseInt(selectedStatusId);
		verifyUpdateRegularTaskWithAuthorization(regularTaskId);

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

	@Test(priority = 8)
	@Step("Get All Regular Task With Invalid Employee Id")
	public void verifyGetAllRegularTaskWithInvalidEmployeeId() {
		int fakeEmployeeId = faker.number().numberBetween(10, 50);
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> employeeMap = new HashMap<>();
		employeeMap.put("empId", fakeEmployeeId);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(employeeMap);

		requestSpec.basePath("/task/regular/get");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Employee Not Found");

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

	@Test(priority = 10, dependsOnMethods = { "verifyGetAllRegularTaskWithAuthorization" })
	@Step("Update Regular Task With Authorization")
	public Integer verifyUpdateRegularTaskWithAuthorization(Integer regularTaskId) {
		String fakeRegularTaskName = faker.food().dish();
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> regularTaskMap = new HashMap<>();
		regularTaskMap.put("regularTaskName", fakeRegularTaskName);
		regularTaskMap.put("regularTaskId", regularTaskId);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(regularTaskMap);

		requestSpec.basePath("/task/regular/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String regularTaskId1 = String.valueOf(regularTaskId);
		Assert.assertEquals(response.jsonPath().getString(regularTaskId1), fakeRegularTaskName);

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
		return regularTaskId;
	}

	@Test(priority = 11)
	@Step("Update Status Without Giving Regular Task Id")
	public void updateStatusWithoutGivingRegularTaskId() {
		String fakeRegularTaskName = faker.food().dish();
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> regularTaskMap = new HashMap<>();
		regularTaskMap.put("regularTaskName", fakeRegularTaskName);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(regularTaskMap);

		requestSpec.basePath("/task/regular/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 400, "Invalid status code");

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Missing Fields");

		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 12)
	@Step("Update Status By Giving Non Existing Regular Task Id")
	public void updateStatusByGivingNonExistingRegularTaskId() {
		String fakeRegularTaskName = faker.food().dish();
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> regularTaskMap = new HashMap<>();
		regularTaskMap.put("regularTaskId", 1);
		regularTaskMap.put("regularTaskName", fakeRegularTaskName);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(regularTaskMap);

		requestSpec.basePath("/task/regular/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No Regular Tasks Present");

		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 13, dependsOnMethods = { "verifyAddRegularTaskWithAuthorization" })
	@Step("Delete Single Regular Task With Authorization")
	public Integer deleteSingleRegularTaskWithAuthorization(Integer fakeRegularTaskId) {
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> regularTaskMap = new HashMap<>();
		regularTaskMap.put("regularTaskId", fakeRegularTaskId);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(regularTaskMap);
		requestSpec.basePath("/task/regular/delete");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		log.info("Status Code: " + response.statusCode());

		// Check the response status code
		if (response.getStatusCode() == 200) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 200, "Invalid status code");
			log.info("Response Status Code: " + actualStatusCode);
		} else if (response.getStatusCode() == 404) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
			log.info("Response Status Code: " + actualStatusCode);
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "Regular Task Not Found");
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
		return fakeRegularTaskId;
	}

	@Test(priority = 14)
	@Step("Delete Single Regular Task With Invalid Regular Task Id")
	public void deleteSingleRegularTaskWithInvalidRegularTaskId() {
		int fakeRegularTaskId = faker.number().numberBetween(1, 5);
		// Create a HashMap to represent the JSON payload
		HashMap<String, Object> regularTaskMap = new HashMap<>();
		regularTaskMap.put("regularTaskId", fakeRegularTaskId);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(regularTaskMap);
		requestSpec.basePath("/task/regular/delete");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).delete();

		log.info("Response Body: " + response.getBody().asPrettyString());

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
		// Status already exists
		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Regular Task Not Found");

		log.info("Response Time: " + response.getTime());
	}

	private String getRandomRegularTaskId(List<String> keyList) {
		Random random = new Random();
		int randomIndex = random.nextInt(keyList.size());
		return keyList.get(randomIndex);
	}
}
