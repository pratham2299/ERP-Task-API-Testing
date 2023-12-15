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

public class TagFolder {
	public static final Logger log = LogManager.getLogger(TagFolder.class);
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
	@Step("Add Tag Without Authorization")
	public void verifyAddTagWithoutAuthorization() {
		HashMap<String, Object> tagMap = new HashMap<>();
		tagMap.put("tagName", "tag");
		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(tagMap);

		requestSpec.basePath("/task/tag/add");
		response = requestSpec.contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 2)
	@Step("Get All Tag Without Authorization")
	public void verifyGetAllTagWithoutAuthorization() {
		requestSpec.basePath("/task/tag/get/all");
		response = requestSpec.get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 3)
	@Step("Update Tag Without Authorization")
	public void updateTagWithoutAuthorization() {
		HashMap<String, Object> tagMap = new HashMap<>();
		tagMap.put("tagId", 7);
		String fakeTag1 = faker.company().name();
		tagMap.put("tag", fakeTag1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(tagMap);

		requestSpec.basePath("/task/tag/update");
		response = requestSpec.contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 4)
	@Step("Delete Single Tag Without Authorization")
	public void deleteSingleTagWithoutAuthorization() {
		requestSpec.basePath("/task/tag/delete/single").queryParam("tagName", "tag");
		response = requestSpec.contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 5)
	@Step("Add Tag With Authorization")
	public void verifyAddTagWithAuthorization() {
		String fakeTag1 = faker.company().name();
		HashMap<String, Object> tagMap = new HashMap<>();
		tagMap.put("tagName", fakeTag1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(tagMap);

		requestSpec.basePath("/task/tag/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		// Extract all keys from the response as a Map
		Map<String, ?> allKeys = response.jsonPath().getMap("");

		// Print all keys
		List<String> keyList = new ArrayList<>(allKeys.keySet());
		System.out.println("All Keys: " + keyList);
		// Choose a random key from the list
		String selectedTagId = getRandomTagId(keyList);
		String fakeTag = response.jsonPath().getString(selectedTagId);
		deleteSingleTagWithAuthorization(fakeTag);

		log.info("Response Code: " + response.getStatusCode());

		// Check the response status code
		if (response.getStatusCode() == 201) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 201);
		} else if (response.getStatusCode() == 422) {
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "Tag Already Exits");
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
	@Step("Add Tag With Same Payload As Previous")
	public void addTagWithSamePayloadAsPrevious() {
		HashMap<String, Object> tagMap = new HashMap<>();
		tagMap.put("tagName", "QE");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(tagMap);

		requestSpec.basePath("/task/tag/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Tag Already Exists");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 422, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 7)
	@Step("Add Tag With Invalid Payload")
	public void addTagWithInvalidPayload() {
		String fakeTag = faker.company().name();
		HashMap<String, Object> tagMap = new HashMap<>();
		tagMap.put("tag", fakeTag);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(tagMap);

		requestSpec.basePath("/task/tag/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);
		log.info("Are Fields Missing: " + responseBody.contains("Fields Are Missing"));
		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 400, "Invalid status code");

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Fields are Missing");

		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 8)
	@Step("Get All Tag With Authorization")
	public void verifyGetAllTagWithAuthorization() {
		requestSpec.basePath("/task/tag/get/all");
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

	@Test(priority = 9)
	@Step("Update Tag With Authorization")
	public void updateTagWithAuthorization() {
		HashMap<String, Object> tagMap = new HashMap<>();
		tagMap.put("tagId", 43);
		String fakeTag1 = faker.company().name();
		tagMap.put("tag", fakeTag1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(tagMap);

		requestSpec.basePath("/task/tag/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		Assert.assertEquals(response.jsonPath().getString("43"), fakeTag1);

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
	}

	@Test(priority = 10)
	@Step("Update Tag Without Giving Tag Id")
	public void updateTagWithoutGivingTagId() {
		HashMap<String, Object> tagMap = new HashMap<>();
		tagMap.put("tag", "QA");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(tagMap);

		requestSpec.basePath("/task/tag/update");
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

	@Test(priority = 11)
	@Step("Update Tag By Giving Non Existing Tag Id")
	public void updateTagByGivingNonExistingTagId() {
		HashMap<String, Object> tagMap = new HashMap<>();
		tagMap.put("tagId", 1);
		tagMap.put("tag", "tag");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(tagMap);

		requestSpec.basePath("/task/tag/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 12, dependsOnMethods = { "verifyAddTagWithAuthorization" })
	@Step("Delete Single Tag With Authorization")
	public String deleteSingleTagWithAuthorization(String fakeTag) {
		requestSpec.basePath("/task/tag/delete/single").queryParam("tagName", fakeTag);
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
		return fakeTag;
	}

	@Test(priority = 13)
	@Step("Delete Tag With Invalid Tag Name")
	public void deleteSingleTagWithInvalidTagName() {
		String fakeTagName = "Software Company";
		requestSpec.basePath("/task/tag/delete/single").queryParam("tagName", fakeTagName);
		response = requestSpec.auth().basic(username, password).contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No tag to delete with " + fakeTagName + ".");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	private String getRandomTagId(List<String> keyList) {
		Random random = new Random();
		int randomIndex = random.nextInt(keyList.size());
		return keyList.get(randomIndex);
	}
}
