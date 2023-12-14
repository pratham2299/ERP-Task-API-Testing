package testcases;

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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("designation", "designation");

		requestSpec.basePath("/employee/designation");
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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("designationId", 7);
		Object fakeDesignation1 = faker.name().firstName();
		data.put("designation", fakeDesignation1);

		requestSpec.basePath("/employee/designation/update");
		response = requestSpec.contentType("application/json").body(data).put();
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
		HashMap<Object, Object> data = new HashMap<>();
		data.put("designation", fakeDesignation1);

		requestSpec.basePath("/employee/designation/add");
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

		deleteSingleDesignationWithAuthorization(fakeDesignation1);
	}

	@Test(priority = 6)
	@Step("Add Designation With Invalid Payload")
	public void addDesignationWithInvalidPayload() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("design", "designation");

		requestSpec.basePath("/employee/designation/add");
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

	@Test(priority = 7)
	@Step("Add Designation With Same Payload As Previous")
	public void addDesignationWithSamePayloadAsPrevious() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("designation", "Project Lead");

		requestSpec.basePath("/employee/designation/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Designation Already Exits");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 422;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
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
		String responseBody = response.getBody().asPrettyString();
		Assert.assertEquals(responseBody.contains("COO"), true, "COO value does not exist");

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
	@Step("Update Designation With Authorization")
	public void updateDesignationWithAuthorization() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("designationId", 13);
		String fakeDesignation1 = faker.job().position();
		data.put("designation", fakeDesignation1);

		requestSpec.basePath("/employee/designation/update");
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
	@Step("Update Designation Without Giving Designation Id")
	public void updateDesignationWithoutGivingDesignationId() {
		HashMap<Object, Object> data = new HashMap<>();
		data.put("designation", "Project Lead");

		requestSpec.basePath("/employee/designation/update");
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
	@Step("Update Designation By Giving Non Existing Designation Id")
	public void updateDesignationByGivingNonExistingDesignationId() {
		String fakeDesignation = faker.job().position();
		HashMap<Object, Object> data = new HashMap<>();
		data.put("designationId", 37);
		data.put("designation", fakeDesignation);

		requestSpec.basePath("/employee/designation/update");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(data).put();
		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No Designation Present For Given Id");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 404;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 12, dependsOnMethods = { "verifyAddDesignationWithAuthorization" })
	@Step("Delete Single Designation With Authorization")
	public String deleteSingleDesignationWithAuthorization(String fakeDesignation) {
		requestSpec.basePath("/employee/designation/delete").queryParam("designation", fakeDesignation);
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
		return fakeDesignation;
	}

	@Test(priority = 13)
	@Step("Delete Designation With Invalid Designation Name")
	public void deleteSingleDesignationWithInvalidDesignationName() {
		requestSpec.basePath("/employee/designation/delete").queryParam("designation", "Lawyer1");
		response = requestSpec.auth().basic(username, password).contentType("application/json").delete();

		log.info("Response Body: " + response.getBody().asPrettyString());

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "No status to delete with Lawyer1.");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		int expectedStatusCode = 404;
		Assert.assertEquals(actualStatusCode, expectedStatusCode);
		log.info("Response Time: " + response.getTime());
	}

}
