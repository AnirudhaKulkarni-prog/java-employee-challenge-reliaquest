package com.reliaquest.api.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ClassBasedNavigableIterableAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.reliaquest.api.common.JsonUtil;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"api.base.url=http://localhost:8113"})
@WireMockTest(httpPort = 8113)
public class EmployeeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    public void setup() {
        baseUrl = "http://localhost:" + port + "/api/v1/employee";
    }

    @Test
    public void testCreateEmployee_success() throws Exception {
        stubFor(post(urlEqualTo("/api/v1/employee"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(JsonUtil.loadJson("create_employee_success.json"))
                        .withStatus(200)));

        EmployeeInput input = new EmployeeInput("Jill", 2000, 32, "Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EmployeeInput> request = new HttpEntity<>(input, headers);
        ResponseEntity<Employee> response = restTemplate.postForEntity(baseUrl, request, Employee.class);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());

        assertEquals("Jill", response.getBody().getEmployee_name());
        assertEquals(2000, response.getBody().getEmployee_salary());
        assertEquals(32, response.getBody().getEmployee_age());
    }

    @Test
    public void testGetAllEmployees_success() throws Exception {
        stubFor(get(urlEqualTo("/api/v1/employee"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(JsonUtil.loadJson("get_all_employees.json"))
                        .withStatus(200)));

        ResponseEntity<Employee[]> response = restTemplate.getForEntity(baseUrl, Employee[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Employee[] employees = response.getBody();
        assertNotNull(employees);
        assertEquals(3, employees.length);

        assertThat(List.of(employees))
                .extracting(Employee::getEmployee_email)
                .containsExactlyInAnyOrder("alice@company.com", "bob@company.com", "charlie@company.com");
    }

    @Test
    public void testGetEmployeeById_success() throws Exception {
        String employeeId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

        stubFor(get(urlEqualTo("/api/v1/employee/3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(JsonUtil.loadJson("employee_by_id.json"))
                        .withStatus(200)));

        ResponseEntity<Employee> response = restTemplate.getForEntity(baseUrl + "/" + employeeId, Employee.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Jill", response.getBody().getEmployee_name());
    }

    @Test
    public void testDeleteEmployeeById_success() throws Exception {
        String employeeId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

        stubFor(get(urlEqualTo("/api/v1/employee/" + employeeId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(JsonUtil.loadJson("employee_by_id.json"))
                        .withStatus(200)));

        stubFor(delete(urlEqualTo("/api/v1/employee"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(JsonUtil.loadJson("delete_employee_by_id.json"))
                        .withStatus(200)));

        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl + "/" + employeeId, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().equals("Jill"));
    }

    @Test
    public void testGetTop10HighestPaidEmployees_success() throws Exception {
        stubFor(get(urlEqualTo("/api/v1/employee"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(JsonUtil.loadJson("top_10_employees.json"))
                        .withStatus(200)));

        ResponseEntity<String[]> response =
                restTemplate.getForEntity(baseUrl + "/topTenHighestEarningEmployeeNames", String[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        String[] topEmployees = response.getBody();
        assertNotNull(topEmployees);
        assertEquals(10, topEmployees.length); // should return only top 10

        assertEquals("Emp11", topEmployees[0]);
    }

    @Test
    public void testGetEmployeeWithHighestSalary_success() throws Exception {
        stubFor(get(urlEqualTo("/api/v1/employee"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(JsonUtil.loadJson("highest_salary.json"))
                        .withStatus(200)));

        ResponseEntity<Integer> response = restTemplate.getForEntity(baseUrl + "/highestSalary", Integer.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10000, response.getBody());
    }

    @Test
    public void testCreateEmployee_failure() throws Exception {
        stubFor(post(urlEqualTo("/api/v1/employee")).willReturn(aResponse().withStatus(502)));

        EmployeeInput input = new EmployeeInput("Jill", 2000, 32, "Manager");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeInput> request = new HttpEntity<>(input, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    }

    @Test
    void shouldReturn400WhenInputIsInvalid() {
        // Invalid input: name/title blank, salary = 0, age = 15 (too young)
        EmployeeInput invalidInput = new EmployeeInput("", 0, 15, "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EmployeeInput> request = new HttpEntity<>(invalidInput, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl, HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {});

        // Basic status and body assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("errors"));

        // Validate that errors list is not empty
        Object errorsObj = response.getBody().get("errors");
        assertTrue(errorsObj instanceof List);

        List<?> errors = (List<?>) errorsObj;
        assertTrue(errors.size() >= 1);
    }
}
