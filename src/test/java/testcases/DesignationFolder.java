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

public class DesignationFolder {
	public static final Logger log = LogManager.getLogger(DesignationFolder.class);
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
	@Step("Add Designation Without Authorization")
	public void verifyAddDesignationWithoutAuthorization() {
		HashMap<String, Object> designationMap = new HashMap<>();
		designationMap.put("designation", "designation");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(designationMap);

		requestSpec.basePath("/employee/designation");
		response = requestSpec.contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 2)
	@Step("Get All Designation Without Authorization")
	public void verifyGetAllDesignationWithoutAuthorization() {
		requestSpec.basePath("/employee/designation/all");
		response = requestSpec.get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 3)
	@Step("Update Designation Without Authorization")
	public void updateDesignationWithoutAuthorization() {
		HashMap<String, Object> designationMap = new HashMap<>();
		designationMap.put("designationId", 7);
		String fakeDesignation1 = faker.name().firstName();
		designationMap.put("designation", fakeDesignation1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(designationMap);

		requestSpec.basePath("/employee/designation/update");
		response = requestSpec.contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Code: " + response.getStatusCode());
	}

	@Test(priority = 4)
	@Step("Delete Single Designation Without Authorization")
	public void deleteSingleDesignationWithoutAuthorization() {
		requestSpec.basePath("/employee/designation/delete").queryParam("designation", "designation");
		response = requestSpec.contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 5)
	@Step("Add Designation With Authorization")
	public void verifyAddDesignationWithAuthorization() {
		String fakeDesignation1 = faker.job().position();
		HashMap<String, Object> designationMap = new HashMap<>();
		designationMap.put("designation", fakeDesignation1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(designationMap);

		requestSpec.basePath("/employee/designation/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		// Extract all keys from the response as a Map
		Map<String, ?> allKeys = response.jsonPath().getMap("");

		// Print all keys
		List<String> keyList = new ArrayList<>(allKeys.keySet());
		System.out.println("All Keys: " + keyList);
		// Choose a random key from the list
		String selectedStatusId = getRandomDesignationId(keyList);
		String fakeDesignation = response.jsonPath().getString(selectedStatusId);
		deleteSingleDesignationWithAuthorization(fakeDesignation);

		log.info("Response Code: " + response.getStatusCode());

		// Check the response status code
		if (response.getStatusCode() == 201) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 201);
		} else if (response.getStatusCode() == 422) {
			String actualMessage = response.jsonPath().getString("message");
			log.info("Message: " + actualMessage);
			Assert.assertEquals(actualMessage, "Designation Already Exits");
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
	@Step("Add Designation With Invalid Payload")
	public void addDesignationWithInvalidPayload() {
		HashMap<String, Object> designationMap = new HashMap<>();
		designationMap.put("design", "designation");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(designationMap);

		requestSpec.basePath("/employee/designation/add");
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

	@Test(priority = 7)
	@Step("Add Designation With Same Payload As Previous")
	public void addDesignationWithSamePayloadAsPrevious() {
		HashMap<String, Object> designationMap = new HashMap<>();
		designationMap.put("designation", "Project Lead");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(designationMap);

		requestSpec.basePath("/employee/designation/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Designation Already Exits");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 422, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 8)
	@Step("Get All Designation With Authorization")
	public void verifyGetAllDesignationWithAuthorization() {
		requestSpec.basePath("/employee/designation/all");
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
	@Step("Update Designation With Authorization")
	public void updateDesignationWithAuthorization() {
		HashMap<String, Object> designationMap = new HashMap<>();
		designationMap.put("designationId", 13);
		String fakeDesignation1 = faker.job().position();
		designationMap.put("designation", fakeDesignation1);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(designationMap);

		requestSpec.basePath("/employee/designation/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		Assert.assertEquals(response.jsonPath().getString("13"), fakeDesignation1);

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
	@Step("Update Designation Without Giving Designation Id")
	public void updateDesignationWithoutGivingDesignationId() {
		HashMap<String, Object> designationMap = new HashMap<>();
		designationMap.put("designation", "Project Lead");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(designationMap);

		requestSpec.basePath("/employee/designation/update");
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
	@Step("Update Designation By Giving Non Existing Designation Id")
	public void updateDesignationByGivingNonExistingDesignationId() {
		String fakeDesignation = faker.job().position();
		HashMap<String, Object> designationMap = new HashMap<>();
		designationMap.put("designationId", 37);
		designationMap.put("designation", fakeDesignation);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(designationMap);

		requestSpec.basePath("/employee/designation/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No Designation Present For Given Id");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 12, dependsOnMethods = { "verifyAddDesignationWithAuthorization" })
	@Step("Delete Single Designation With Authorization")
	public String deleteSingleDesignationWithAuthorization(String fakeDesignation) {
		requestSpec.basePath("/employee/designation/delete").queryParam("designation", fakeDesignation);
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
			Assert.assertEquals(actualMessage, "No designation to delete with " + fakeDesignation + ".");
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
		return fakeDesignation;
	}

	@Test(priority = 13)
	@Step("Delete Designation With Invalid Designation Name")
	public void deleteSingleDesignationWithInvalidDesignationName() {
		String fakeDesignationName = "Lawyer2";
		requestSpec.basePath("/employee/designation/delete").queryParam("designation", fakeDesignationName);
		response = requestSpec.auth().basic(username, password).contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No designation to delete with " + fakeDesignationName + ".");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	private String getRandomDesignationId(List<String> keyList) {
		Random random = new Random();
		int randomIndex = random.nextInt(keyList.size());
		return keyList.get(randomIndex);
	}

}
