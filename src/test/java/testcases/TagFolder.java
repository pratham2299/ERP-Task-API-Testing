package testcases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;

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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("tagName", "tag");

		requestSpec.basePath("/task/tag/add");
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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("tagId", 7);
		String fakeTag1 = faker.company().name();
		data.put("tag", fakeTag1);

		requestSpec.basePath("/task/tag/update");
		response = requestSpec.contentType("application/json").body(data).put();
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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("tagName", fakeTag1);

		requestSpec.basePath("/task/tag/add");
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

		deleteSingleTagWithAuthorization(fakeTag1);
	}

	@Test(priority = 6)
	@Step("Add Tag With Same Payload As Previous")
	public void addTagWithSamePayloadAsPrevious() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("tagName", "QE");

		requestSpec.basePath("/task/tag/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Tag Already Exists");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 422;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 7)
	@Step("Add Tag With Invalid Payload")
	public void addTagWithInvalidPayload() {
		String fakeTag = faker.company().name();
		HashMap<Object, Object> data = new HashMap<>();
		data.put("tag", fakeTag);

		requestSpec.basePath("/task/tag/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);
		log.info("Are Fields Missing: " + responseBody.contains("Fields Are Missing"));
		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 400;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Fields are Missing");

		assertThat(response.getBody().asString().contains("message")).as("Body contaisn message").isTrue();
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
		String responseBody = response.getBody().asPrettyString();
		Assert.assertEquals(responseBody.contains("QA"), true, "QA value does not exist");

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
	@Step("Update Tag With Authorization")
	public void updateTagWithAuthorization() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("tagId", 13);
		Object fakeTag1 = faker.company().name();
		data.put("tag", fakeTag1);

		requestSpec.basePath("/task/tag/update");
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
	@Step("Update Tag Without Giving Tag Id")
	public void updateTagWithoutGivingTagId() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("tag", "QA");

		requestSpec.basePath("/task/tag/update");
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
	@Step("Update Tag By Giving Non Existing Tag Id")
	public void updateTagByGivingNonExistingTagId() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("tagId", 37);
		data.put("tag", "tag");

		requestSpec.basePath("/task/tag/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 404;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 12, dependsOnMethods = { "verifyAddTagWithAuthorization" })
	@Step("Delete Single Tag With Authorization")
	public String deleteSingleTagWithAuthorization(String fakeTag) {
		requestSpec.basePath("/task/tag/delete/single").queryParam("tagName", fakeTag);
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
		return fakeTag;
	}

	@Test(priority = 13)
	@Step("Delete Tag With Invalid Tag Name")
	public void deleteSingleTagWithInvalidTagName() {
		requestSpec.basePath("/task/tag/delete/single").queryParam("tagName", "Peon");
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
