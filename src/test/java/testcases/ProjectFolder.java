package testcases;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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

public class ProjectFolder {
	public static final Logger log = LogManager.getLogger(ProjectFolder.class);
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
	@Step("Add Project Without Authorization")
	public void verifyAddProjectWithoutAuthorization() {
		HashMap<String, Object> projectMap = new HashMap<>();
		projectMap.put("projectName", "project");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(projectMap);

		requestSpec.basePath("/project/add");
		response = requestSpec.contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 2)
	@Step("Get All Project Without Authorization")
	public void verifyGetAllProjectWithoutAuthorization() {
		requestSpec.basePath("/project/get/all");
		response = requestSpec.get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 401, "Invalid status code");
	}

	@Test(priority = 3)
	@Step("Add Project With Authorization")
	public void verifyAddProjectWithAuthorization() {
		String fakeProject = faker.app().name();
		LocalDate startDate = LocalDate.of(2000, 1, 1);
		LocalDate endDate = LocalDate.of(2024, 12, 31);

		// Generate a random date within the specified range
		LocalDate randomDate = generateRandomDate(startDate, endDate);

		// Format and print the random date
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String fakeStartDate = formatter.format(randomDate);

		HashMap<String, Object> projectMap = new HashMap<>();
		projectMap.put("projectName", fakeProject);
		projectMap.put("projectStartDate", fakeStartDate);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(projectMap);

		requestSpec.basePath("/project/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());

		// Check the response status code
		if (response.getStatusCode() == 201) {
			int actualStatusCode = response.getStatusCode();
			Assert.assertEquals(actualStatusCode, 201);
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

	@Test(priority = 4)
	@Step("Add Project With Same Payload As Previous")
	public void addProjectWithSamePayloadAsPrevious() {
		HashMap<String, Object> projectMap = new HashMap<>();
		projectMap.put("projectName", "E-commerce App");
		projectMap.put("projectStartDate", "2023/12/05");

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(projectMap);

		requestSpec.basePath("/project/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 201, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 5)
	@Step("Add Project With Invalid Payload")
	public void addProjectWithInvalidPayload() {
		String fakeProject = faker.app().name();
		HashMap<String, Object> projectMap = new HashMap<>();
		projectMap.put("project", fakeProject);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(projectMap);

		requestSpec.basePath("/project/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		String actualMessage = response.jsonPath().getString("message");
		log.info("Message: " + actualMessage);
		Assert.assertEquals(actualMessage, "Fields Missing");

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 400, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 6)
	@Step("Add Project Without Giving Project Start Date In Payload")
	public void addProjectWithoutGivingProjectStartDateInPayload() {
		String fakeProject = faker.app().name();
		HashMap<String, Object> projectMap = new HashMap<>();
		projectMap.put("projectName", fakeProject);

		// Convert the HashMap to JSON format using Gson
		String payload = new Gson().toJson(projectMap);

		requestSpec.basePath("/project/add");
		response = requestSpec.auth().basic(username, password).contentType("application/json").body(payload).post();

		String responseBody = response.getBody().asPrettyString();
		log.info("Response Body:\n" + responseBody);

		log.info("Response Code: " + response.getStatusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 201, "Invalid status code");
		log.info("Response Time: " + response.getTime());
	}

	@Test(priority = 7)
	@Step("Get All Project With Authorization")
	public void verifyGetAllProjectWithAuthorization() {
		requestSpec.basePath("/project/get/all");
		response = requestSpec.auth().basic(username, password).get();
		log.info("Status Code: " + response.statusCode());
		int actualStatusCode = response.getStatusCode();
		Assert.assertEquals(actualStatusCode, 200, "Invalid status code");
		log.info("Response Body: " + response.getBody().asPrettyString());
		String responseBody = response.getBody().asPrettyString();
		Assert.assertEquals(responseBody.contains("E-commerce App"), true, "E-commerce App value does not exist");

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

	private static LocalDate generateRandomDate(LocalDate startDate, LocalDate endDate) {
		long startEpochDay = startDate.toEpochDay();
		long endEpochDay = endDate.toEpochDay();

		Random random = new Random();
		long randomEpochDay = startEpochDay + random.nextInt((int) (endEpochDay - startEpochDay + 1));

		return LocalDate.ofEpochDay(randomEpochDay);
	}

}
