package testcases;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PriorityFolder {
	public static final Logger log = LogManager.getLogger(PriorityFolder.class);
	public RequestSpecification requestSpec;
	public Response response;
	public String username = "BIE004";
	public String password = "Pass@123";

	private Faker faker = new Faker();
	public String selectedPriorityId;

	@BeforeClass
	public void baseURL() {
		// Define the base URL of the API
		requestSpec = RestAssured.given();

		requestSpec.baseUri("http://192.168.0.173:10003");
	}

	@Test(priority = 1)
	@Step("Add Priority Without Authorization")
	public void verifyAddPriorityWithoutAuthorization() {
		int fakeLevel = faker.number().numberBetween(1, 10);
		HashMap<String, Object> priorityMap = new HashMap<>();
		priorityMap.put("priority", "priority");
		priorityMap.put("priorityLevel", fakeLevel);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(priorityMap);

		requestSpec.basePath("/task/priority/add");
		response = requestSpec.contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 2)
	@Step("Get All Priority Without Authorization")
	public void verifyGetAllPriorityWithoutAuthorization() {
		requestSpec.basePath("/task/priority/get/all");
		response = requestSpec.get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 3)
	@Step("Update Priority Without Authorization")
	public void updatePriorityWithoutAuthorization() {
		HashMap<String, Object> priorityMap = new HashMap<>();
		priorityMap.put("priorityId", 24);
		String fakePriority1 = faker.job().seniority();
		priorityMap.put("priority", fakePriority1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(priorityMap);

		requestSpec.basePath("/task/priority/update");
		response = requestSpec.contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Code: " + response.getStatusCode());
	}

	@Test(priority = 4)
	@Step("Delete Single Priority Without Authorization")
	public String deleteSinglePriorityWithoutAuthorization(String fakePriority) {
		requestSpec.basePath("/task/priority/delete/single").queryParam("priorityName", fakePriority);
		response = requestSpec.contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		return fakePriority;
	}

	@Test(priority = 5)
	@Step("Add Priority With Authorization")
	public void verifyAddPriorityWithAuthorization() {
		String fakePriority1 = faker.job().seniority();
		int fakeLevel = faker.number().numberBetween(1, 10);
		HashMap<String, Object> priorityMap = new HashMap<>();
		priorityMap.put("priority", fakePriority1);
		priorityMap.put("priorityLevel", fakeLevel);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(priorityMap);

		requestSpec.basePath("/task/priority/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		// Extract all keys from the response as a Map
		Map<String, ?> allKeys = response.jsonPath().getMap("");

		// Print all keys
		List<String> keyList = new ArrayList<>(allKeys.keySet());
		System.out.println("All Keys: " + keyList);
		// Choose a random key from the list
		selectedPriorityId = getRandomPriorityId(keyList);
		String fakePriority = response.jsonPath().getString(selectedPriorityId);
		addPriorityWithSamePayloadAsPrevious(fakePriority);
		deleteSinglePriorityWithAuthorization(fakePriority);

		log.info("Response Code: " + response.getStatusCode());

		// Check the response status code
		if (response.getStatusCode() == 201) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 201);
		} else if (response.getStatusCode() == 422) {
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "Priority Already Exits");
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

	@Test(priority = 6, dependsOnMethods = "verifyAddPriorityWithAuthorization")
	@Step("Add Priority With Same Payload As Previous")
	public String addPriorityWithSamePayloadAsPrevious(String fakePriority) {
		int fakePriorityLevel = faker.number().numberBetween(1, 10);
		HashMap<String, Object> priorityMap = new HashMap<>();
		priorityMap.put("priority", fakePriority);
		priorityMap.put("priorityLevel", fakePriorityLevel);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(priorityMap);

		requestSpec.basePath("/task/priority/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Priority Already Exits");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 422, "Invalid status code");
		log.info("Response Time: " + response.getTime());

		return fakePriority;
	}

	@Test(priority = 7)
	@Step("Add Priority With Invalid Payload")
	public void addPriorityWithInvalidPayload() {
		HashMap<String, Object> priorityMap = new HashMap<>();
		priorityMap.put("priorityLevel", 19);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(priorityMap);

		requestSpec.basePath("/task/priority/add");
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
	@Step("Add Status With Same Priority Level In Payload As Previous")
	public void addStatusWithSamePriorityLevelInPayloadAsPrevious() {
		HashMap<String, Object> priorityMap = new HashMap<>();
		priorityMap.put("priority", "Low");
		priorityMap.put("priorityLevel", 1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(priorityMap);

		requestSpec.basePath("/task/priority/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Priority Already Exits");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 422, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 9)
	@Step("Get All Priority With Authorization")
	public void verifyGetAllPriorityWithAuthorization() {
		requestSpec.basePath("/task/priority/get/all");
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
		selectedPriorityId = getRandomPriorityId(keyList);
		System.out.println(selectedPriorityId);
		Integer priorityId = Integer.parseInt(selectedPriorityId);
		updatePriorityWithAuthorization(priorityId);

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

	@Test(priority = 10, dependsOnMethods = "verifyGetAllPriorityWithAuthorization")
	@Step("Update Priority With Authorization")
	public void updatePriorityWithAuthorization(Integer priorityId) {
		HashMap<String, Object> priorityMap = new HashMap<>();
		priorityMap.put("priorityId", priorityId);
		String fakePriority1 = faker.job().seniority();
		priorityMap.put("priority", fakePriority1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(priorityMap);

		requestSpec.basePath("/task/priority/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String priorityId1 = String.valueOf(priorityId);
		Assert.assertEquals(response.jsonPath().getString(priorityId1), fakePriority1);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 200, "Invalid status code");

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

	@Test(priority = 11)
	@Step("Update Priority Without Giving Priority Id")
	public void updatePriorityWithoutGivingPriorityId() {
		HashMap<String, Object> priorityMap = new HashMap<>();
		priorityMap.put("priority", "Higher");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(priorityMap);

		requestSpec.basePath("/task/priority/update");
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

	@Test(priority = 12)
	@Step("Update Priority By Giving Non Existing Priority Id")
	public void updatePriorityByGivingNonExistingPriorityId() {
		String fakePriority = faker.job().seniority();
		HashMap<String, Object> priorityMap = new HashMap<>();
		priorityMap.put("priorityId", 1);
		priorityMap.put("priority", fakePriority);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(priorityMap);

		requestSpec.basePath("/task/priority/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 13, dependsOnMethods = { "verifyAddPriorityWithAuthorization" })
	@Step("Delete Single Priority With Authorization")
	public String deleteSinglePriorityWithAuthorization(String fakePriority) {
		requestSpec.basePath("/task/priority/delete/single").queryParam("priorityName", fakePriority);
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
		} else if (response.getStatusCode() == 403) {
			// Status already exists
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "This priority is mapped with multiple task");
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
		return fakePriority;
	}

	@Test(priority = 14)
	@Step("Delete Priority With Invalid Priority Name")
	public void deleteSinglePriorityWithInvalidPriorityName() {
		String fakePriorityName = "Master2";
		requestSpec.basePath("/task/priority/delete/single").queryParam("priorityName", fakePriorityName);
		response = requestSpec.auth().basic(username, password).contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");

		log.info("Response Time: " + response.getTime());
	}

	private String getRandomPriorityId(List<String> keyList) {
		Random random = new Random();
		int randomIndex = random.nextInt(keyList.size());
		return keyList.get(randomIndex);
	}
}
