package com.reliaquest.api.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"api.base.url=http://localhost:8113"
        })
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
                        .withBody(
                                """
                            {
                              "status": "Successfully processed request.",
                              "data": {
                                "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                "employee_name": "Jill",
                                "employee_salary": 2000,
                                "employee_age": 32,
                                "employee_title": "Manager",
                                "employee_email": "jillj@company.com"
                              }
                            }
                        """)
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
                        .withBody(
                                """
                            {
                              "status": "Successfully processed request.",
                              "data": [
                                {
                                  "id": "1a2b3c4d-1111-2222-3333-444455556666",
                                  "employee_name": "Alice",
                                  "employee_salary": 5000,
                                  "employee_age": 30,
                                  "employee_title": "Engineer",
                                  "employee_email": "alice@company.com"
                                },
                                {
                                  "id": "1a2b3c4d-7777-8888-9999-000011112222",
                                  "employee_name": "Bob",
                                  "employee_salary": 4500,
                                  "employee_age": 28,
                                  "employee_title": "Analyst",
                                  "employee_email": "bob@company.com"
                                },
                                {
                                  "id": "d9c77364-bf4e-4e3b-900f-7f7a12ac9e99",
                                  "employee_name": "Charlie",
                                  "employee_salary": 4700,
                                  "employee_age": 35,
                                  "employee_title": "Consultant",
                                  "employee_email": "charlie@company.com"
                                }
                              ]
                            }
                        """)
                        .withStatus(200)));

        ResponseEntity<Employee[]> response = restTemplate.getForEntity(baseUrl, Employee[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals("alice@company.com", response.getBody()[0].getEmployee_email());
        assertEquals("bob@company.com", response.getBody()[1].getEmployee_email());
        assertEquals("charlie@company.com", response.getBody()[2].getEmployee_email());
    }

    @Test
    public void testGetEmployeeById_success() throws Exception {
        String employeeId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

        stubFor(get(urlEqualTo("/api/v1/employee/3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                        {
                          "status": "Successfully processed request.",
                          "data":
                            {
                              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                              "employee_name": "Jill",
                              "employee_salary": 2000,
                              "employee_age": 32,
                              "employee_title": "Manager",
                              "employee_email": "jillj@company.com"
                            }

                        }
                    """)
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
                        .withBody(
                                """
                        {
                          "status": "Successfully processed request.",
                          "data":
                            {
                              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                              "employee_name": "Jill",
                              "employee_salary": 2000,
                              "employee_age": 32,
                              "employee_title": "Manager",
                              "employee_email": "jillj@company.com"
                            }

                        }
                    """)
                        .withStatus(200)));

        // Stub DELETE call by name

        stubFor(delete(urlEqualTo("/api/v1/employee"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                        {
                           "data": true,
                          "status": "Successfully processed request."

                        }
                    """)
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
                        .withBody(
                                """
                    {
                      "status": "Successfully processed request.",
                      "data": [
                        { "id": "11111111-1111-1111-1111-111111111111", "employee_name": "Emp1", "employee_salary": 1100, "employee_age": 25, "employee_title": "Developer", "employee_email": "emp1@company.com" },
                        { "id": "22222222-2222-2222-2222-222222222222", "employee_name": "Emp2", "employee_salary": 1200, "employee_age": 26, "employee_title": "Tester", "employee_email": "emp2@company.com" },
                        { "id": "33333333-3333-3333-3333-333333333333", "employee_name": "Emp3", "employee_salary": 1300, "employee_age": 27, "employee_title": "Manager", "employee_email": "emp3@company.com" },
                        { "id": "44444444-4444-4444-4444-444444444444", "employee_name": "Emp4", "employee_salary": 1400, "employee_age": 28, "employee_title": "Analyst", "employee_email": "emp4@company.com" },
                        { "id": "55555555-5555-5555-5555-555555555555", "employee_name": "Emp5", "employee_salary": 1500, "employee_age": 29, "employee_title": "Designer", "employee_email": "emp5@company.com" },
                        { "id": "66666666-6666-6666-6666-666666666666", "employee_name": "Emp6", "employee_salary": 1600, "employee_age": 30, "employee_title": "Engineer", "employee_email": "emp6@company.com" },
                        { "id": "77777777-7777-7777-7777-777777777777", "employee_name": "Emp7", "employee_salary": 1700, "employee_age": 31, "employee_title": "Architect", "employee_email": "emp7@company.com" },
                        { "id": "88888888-8888-8888-8888-888888888888", "employee_name": "Emp8", "employee_salary": 1800, "employee_age": 32, "employee_title": "Consultant", "employee_email": "emp8@company.com" },
                        { "id": "99999999-9999-9999-9999-999999999999", "employee_name": "Emp9", "employee_salary": 1900, "employee_age": 33, "employee_title": "Lead", "employee_email": "emp9@company.com" },
                        { "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "employee_name": "Emp10", "employee_salary": 2000, "employee_age": 34, "employee_title": "Advisor", "employee_email": "emp10@company.com" },
                        { "id": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", "employee_name": "Emp11", "employee_salary": 2100, "employee_age": 35, "employee_title": "Director", "employee_email": "emp11@company.com" }
                      ]
                    }
                """)
                        .withStatus(200)));

        ResponseEntity<String[]> response =
                restTemplate.getForEntity(baseUrl + "/topTenHighestEarningEmployeeNames", String[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        String[] topEmployees = response.getBody();
        assertNotNull(topEmployees);
        assertEquals(10, topEmployees.length); // should return only top 10

        // Top employee should be Emp11 with salary 2100
        assertEquals("Emp11", topEmployees[0]);
    }

    @Test
    public void testGetEmployeeWithHighestSalary_success() throws Exception {
        stubFor(get(urlEqualTo("/api/v1/employee"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                        {
                          "status": "Successfully processed request.",
                          "data": [
                            {"employee_name": "A", "employee_salary": 10000, "employee_age": 30, "employee_title": "Dev", "employee_email": "a@x.com", "id":"44444444-4444-4444-4444-444444444444"},
                            {"employee_name": "B", "employee_salary": 9000, "employee_age": 31, "employee_title": "Dev", "employee_email": "b@x.com", "id":"33333333-4444-4444-4444-444444444444"}
                          ]
                        }
                    """)
                        .withStatus(200)));

        ResponseEntity<Integer> response = restTemplate.getForEntity(baseUrl + "/highestSalary", Integer.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10000, response.getBody());
    }

    @Test
    public void testCreateEmployee_failure() throws Exception {
        stubFor(post(urlEqualTo("/api/v1/employee")).willReturn(aResponse().withStatus(500)));

        EmployeeInput input = new EmployeeInput("Jill", 2000, 32, "Manager");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeInput> request = new HttpEntity<>(input, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
