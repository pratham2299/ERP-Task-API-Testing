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

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;

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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("status", "Honey");
		data.put("statusLevel", 5);

		requestSpec.basePath("/task/status/add");
		response = requestSpec.contentType("application/json").body(data).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 401;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("status", "Done");
		data.put("statusId", 19);

		requestSpec.basePath("/task/status/update");
		response = requestSpec.contentType("application/json").body(data).put();
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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("status", fakeStatus1);
		data.put("statusLevel", fakeLevel);

		requestSpec.basePath("/task/status/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());

		// Check the response status code
		if (response.getStatusCode() == 201) {
			// Status created successfully
			log.info("Status created successfully!");
		} else if (response.getStatusCode() == 422) {
			// Status already exists
			log.info("Status already exists");
		} else {
			// Handle other status codes if needed
			log.info("Unexpected status code: " + response.getStatusCode());
		}

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

		deleteSingleStatusWithAuthorization(fakeStatus1);
	}

	@Test(priority = 6)
	@Step("Add Status With Same Payload As Previous")
	public void addStatusWithSamePayloadAsPrevious() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("status", "Done");
		data.put("statusLevel", 19);

		requestSpec.basePath("/task/status/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).post();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Status Already Exists");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 422;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 7)
	@Step("Add Status With Invalid Payload")
	public void addStatusWithInvalidPayload() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("statusLevel", 19);

		requestSpec.basePath("/task/status/add");
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

	@Test(priority = 8)
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

//		given().when().get("http://192.168.0.177:10003/task/status/get/all").then().body("status", equalTo("Start"))
//				.statusCode(200).log().all();
	}

	@Test(priority = 9)
	@Step("Update Status With Authorization")
	public void updateStatusWithAuthorization() {
		String fakeStatus1 = faker.name().firstName();
		HashMap<Object, Object> data = new HashMap<>();
		data.put("status", fakeStatus1);
		data.put("statusId", 82);

		requestSpec.basePath("/task/status/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

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

	@Test(priority = 10)
	@Step("Update Status Without Giving Status Id")
	public void updateStatusWithoutGivingStatusId() {
		String fakeStatus = faker.name().lastName();
		HashMap<Object, Object> data = new HashMap<>();
		data.put("status", fakeStatus);

		requestSpec.basePath("/task/status/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 400;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Fields are missing");

		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 11)
	@Step("Update Status By Giving Non Existing Status Id")
	public void updateStatusByGivingNonExistingStatusId() {
		int fakeStatusId = faker.number().numberBetween(30, 80);
		String fakeStatus = faker.name().lastName();
		HashMap<Object, Object> data = new HashMap<>();
		data.put("statusId", fakeStatusId);
		data.put("status", fakeStatus);

		requestSpec.basePath("/task/status/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 404;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No Status Present For Given Id");

		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 12, dependsOnMethods = { "verifyAddStatusWithAuthorization" })
	@Step("Delete Single Status With Authorization")
	public String deleteSingleStatusWithAuthorization(String fakeStatus) {
		requestSpec.basePath("/task/status/delete/single");
		response = requestSpec.given().auth().basic(username, password).queryParam("statusName", fakeStatus).when()
				.delete();

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
		return fakeStatus;
	}

	@Test(priority = 13)
	@Step("Delete Single Status With Invalid Status Name")
	public void deleteSingleStatusWithInvalidStatusName() {
		requestSpec.basePath("/task/status/delete/single").queryParam("statusName", "Car");
		response = requestSpec.auth().basic(username, password).contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 404;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No status to delete with Car.");

		log.info("Response Time: " + response.getTime());
	}

	private String getRandomStatusId(List<String> keyList) {
		Random random = new Random();
		int randomIndex = random.nextInt(keyList.size());
		return keyList.get(randomIndex);
	}
}
