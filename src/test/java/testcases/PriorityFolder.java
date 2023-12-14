package testcases;

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

import java.util.HashMap;

public class PriorityFolder {
	public static final Logger log = LogManager.getLogger(PriorityFolder.class);
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
	@Step("Add Priority Without Authorization")
	public void verifyAddPriorityWithoutAuthorization() {
		int fakeLevel = faker.number().numberBetween(1, 10);
		HashMap<Object, Object> data = new HashMap<>();
		data.put("priority", "priority");
		data.put("priorityLevel", fakeLevel);

		requestSpec.basePath("/task/priority/add");
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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("priorityId", 24);
		Object fakePriority1 = faker.job().seniority();
		data.put("priority", fakePriority1);

		requestSpec.basePath("/task/priority/update");
		response = requestSpec.contentType("application/json").body(data).put();
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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("priority", fakePriority1);
		data.put("priorityLevel", fakeLevel);

		requestSpec.basePath("/task/priority/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).post();

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

		deleteSinglePriorityWithAuthorization(fakePriority1);
	}

	@Test(priority = 6)
	@Step("Add Priority With Same Payload As Previous")
	public void addPriorityWithSamePayloadAsPrevious() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("priority", "Low");
		data.put("priorityLevel", 23);

		requestSpec.basePath("/task/priority/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Priority Already Exits");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 422;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 7)
	@Step("Add Priority With Invalid Payload")
	public void addPriorityWithInvalidPayload() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("priorityLevel", 19);

		requestSpec.basePath("/task/priority/add");
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
	@Step("Get All Priority With Authorization")
	public void verifyGetAllPriorityWithAuthorization() {
		requestSpec.basePath("/task/priority/get/all");
		response = requestSpec.auth().basic(username, password).get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 200, "Invalid status code");
		log.info("Response Body: " + response.getBody().asPrettyString());
		String responseBody = response.getBody().asPrettyString();
		Assert.assertEquals(responseBody.contains("Low"), true, "Low value does not exist");

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
	@Step("Update Priority With Authorization")
	public void updatePriorityWithAuthorization() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("priorityId", 40);
		String fakePriority1 = faker.job().seniority();
		data.put("priority", fakePriority1);

		requestSpec.basePath("/task/priority/update");
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

	@Test(priority = 10)
	@Step("Update Priority Without Giving Priority Id")
	public void updatePriorityWithoutGivingPriorityId() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("priority", "Higher");

		requestSpec.basePath("/task/priority/update");
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

	@Test(priority = 11)
	@Step("Update Priority By Giving Non Existing Priority Id")
	public void updatePriorityByGivingNonExistingPriorityId() {
		String fakePriority = faker.job().seniority();
		int fakePriorityId = faker.number().numberBetween(50, 100);
		HashMap<Object, Object> data = new HashMap<>();
		data.put("priorityId", fakePriorityId);
		data.put("priority", fakePriority);

		requestSpec.basePath("/task/priority/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);
		log.info("Is Priority Present For Given Id: " + responseBody.contains("No Priority Present For Given Id"));
		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 404;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		Assert.assertEquals(responseBody.contains("No Priority Present For Given Id"), true);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 12, dependsOnMethods = { "verifyAddPriorityWithAuthorization" })
	@Step("Delete Single Priority With Authorization")
	public String deleteSinglePriorityWithAuthorization(String fakePriority) {
		requestSpec.basePath("/task/priority/delete/single").queryParam("priorityName", fakePriority);
		response = requestSpec.auth().basic(username, password).contentType("application/json").delete();

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
		return fakePriority;
	}

	@Test(priority = 13)
	@Step("Delete Priority With Invalid Priority Name")
	public void deleteSinglePriorityWithInvalidPriorityName() {
		requestSpec.basePath("/task/priority/delete/single").queryParam("priorityName", "Master1");
		response = requestSpec.auth().basic(username, password).contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());
		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 404;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);

		log.info("Response Time: " + response.getTime());
	}
}
