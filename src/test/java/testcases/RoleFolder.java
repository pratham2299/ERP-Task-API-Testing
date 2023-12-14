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
import org.testng.asserts.SoftAssert;

import com.github.javafaker.Faker;
import static io.restassured.RestAssured.given;

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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("role", "role");
		data.put("roleLevel", fakeLevel);

		requestSpec.basePath("/employee/role/add");
		response = requestSpec.contentType("application/json").body(data).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 401;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());

//		assertThat(response.getBody().asString().contains("id")).as("Body contains id").isTrue();
//
//		assertThat(response.getBody().asString().contains("error")).as("Order deleted").isTrue();
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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("roleId", 24);
		Object fakeRole1 = faker.job().position();
		data.put("role", fakeRole1);

		requestSpec.basePath("/employee/role/update");
		response = requestSpec.contentType("application/json").body(data).put();
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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("role", fakeRole1);
		data.put("roleLevel", fakeLevel);

		requestSpec.basePath("/employee/role/add");
		response = requestSpec.given().auth().basic(username, password).contentType("application/json").body(data)
				.post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 201;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
//		double actualResponseTime = response.getTime();
//		SoftAssert softAssert = new SoftAssert();
//		softAssert.assertEquals(actualResponseTime < 200, true, "Response time is more than 200 ms");
//		softAssert.assertAll();
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

	@Test(priority = 7)
	@Step("Add Role With Same Payload As Previous")
	public void addRoleWithSamePayloadAsPrevious() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("role", "Front_End_Developer");
		data.put("roleLevel", 2);

		requestSpec.basePath("/employee/role/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 422;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 8)
	@Step("Add Role With Invalid Payload")
	public void addRoleWithInvalidPayload() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("roleLevel", 10);

		requestSpec.basePath("/employee/role/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Fields are Missing");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 400;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 9)
	@Step("Get All Role With Authorization")
	public void verifyGetAllRoleWithAuthorization() {
		requestSpec.basePath("/employee/role/all");
		response = requestSpec.auth().basic(username, password).get();
//		response = given().auth().basic(username, password).when().get("/employee/role/all").then().statusCode(200)
//				.extract().response();
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

		deleteSingleRoleWithAuthorization(selectedRoleId);

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
//		given().when().get("http://192.168.0.177:10003/task/status/get/all").then().body("status", equalTo("Start"))
//				.statusCode(200).log().all();
	}

	@Test(priority = 10)
	@Step("Update Role With Authorization")
	public void updateRoleWithAuthorization() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("roleId", 10);
		String fakeRole1 = faker.job().position();
		data.put("role", fakeRole1);

		requestSpec.basePath("/employee/role/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

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

	@Test(priority = 11)
	@Step("Update Role Without Giving Role Id")
	public void updateRoleWithoutGivingRoleId() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("role", "Front_End_Developer");

		requestSpec.basePath("/employee/role/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Fields are missing");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 400;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 12)
	@Step("Update Role By Giving Non Existing Role Id")
	public void updateRoleByGivingNonExistingRoleId() {
		String fakeRole = faker.job().position();
		HashMap<Object, Object> data = new HashMap<>();
		data.put("roleId", 37);
		data.put("role", fakeRole);

		requestSpec.basePath("/employee/role/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No Role Present For Given Id");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 404;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 13, dependsOnMethods = { "verifyGetAllRoleWithAuthorization" })
	@Step("Delete Single Role With Authorization")
	public String deleteSingleRoleWithAuthorization(String fakeRoleId) {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("roleId", fakeRoleId);
		requestSpec.basePath("employee/role/delete");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		log.info("Status Code: " + response.statusCode());
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
		return fakeRoleId;
	}

	@Test(priority = 14)
	@Step("Delete Role With Invalid Role Id")
	public void deleteSingleRoleWithInvalidRoleId() {
		int fakeRoleId = faker.number().numberBetween(30, 50);
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
		int expectedStatusCode = 404;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 15)
	@Step("Get Role By Level With Authorization")
	public void getRoleByLevelWithAuthorization() {
		int fakeLevel = faker.number().numberBetween(1, 5);
		requestSpec.basePath("/employee/role/level/" + fakeLevel);
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

	@Test(priority = 16)
	@Step("Get Role By Level With Invalid Role Level")
	public void getRoleByLevelWithInvalidRoleLevel() {
		int fakeLevel = faker.number().numberBetween(11, 20);
		requestSpec.basePath("/employee/role/level" + fakeLevel);
		response = requestSpec.auth().basic(username, password).get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 404, "Invalid status code");
		log.info("Response Body: " + response.getBody().asPrettyString());
	}

	private String getRandomRoleId(List<String> keyList) {
		Random random = new Random();
		int randomIndex = random.nextInt(keyList.size());
		return keyList.get(randomIndex);
	}
}
