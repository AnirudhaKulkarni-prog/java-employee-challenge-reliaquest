package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeService employeeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Employee sampleEmployee;
    private JsonNode employeeJson;
    private JsonNode employeeListJson;
    private EmployeeInput sampleInput;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        employeeService = new EmployeeService(restTemplate, objectMapper);

        sampleEmployee = new Employee(
                UUID.fromString("d005f39a-beb8-4390-afec-fd54e91d94ee"),
                "Manish Sisod",
                139082,
                48,
                "Financial Advisor",
                "msisod@company.com");

        sampleInput = new EmployeeInput("Manish Sisod", 139082, 48, "Financial Advisor");

        String singleJson =
                """
        {
          "data": {
            "id": "d005f39a-beb8-4390-afec-fd54e91d94ee",
            "employee_name": "Manish Sisod",
            "employee_salary": 139082,
            "employee_age": 48,
            "employee_title": "Financial Advisor",
            "employee_email": "msisod@company.com"
          },
          "status": "success"
        }
        """;

        String listJson =
                """
        {
          "data": [
            {
              "id": "11111111-1111-1111-1111-111111111111",
              "employee_name": "A",
              "employee_salary": 100,
              "employee_age": 30,
              "employee_title": "Dev",
              "employee_email": "a@company.com"
            },
            {
              "id": "22222222-2222-2222-2222-222222222222",
              "employee_name": "B",
              "employee_salary": 200,
              "employee_age": 40,
              "employee_title": "Lead",
              "employee_email": "b@company.com"
            }
          ],
          "status": "success"
        }
        """;

        employeeJson = objectMapper.readTree(singleJson);
        employeeListJson = objectMapper.readTree(listJson);

        ReflectionTestUtils.setField(employeeService, "SERVER_BASE_URL", "http://localhost:8112");
    }

    @Test
    void testCreateEmployee_success() throws Exception {
        when(restTemplate.postForEntity(anyString(), any(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(employeeJson));

        Employee created = employeeService.createEmployee(sampleInput);
        assertEquals(sampleEmployee.getEmployee_name(), created.getEmployee_name());
        assertEquals(sampleEmployee.getEmployee_email(), created.getEmployee_email());
    }

    @Test
    void testCreateEmployee_error() {
        when(restTemplate.postForEntity(anyString(), any(), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("Timeout"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> employeeService.createEmployee(sampleInput));

        assertEquals("Unable to create employee", ex.getMessage());
    }

    @Test
    void testGetAllEmployees_success() {
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(employeeListJson));

        List<Employee> result = employeeService.getAllEmployees();
        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getEmployee_name());
    }

    @Test
    void testGetAllEmployees_failure() {
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("Server error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> employeeService.getAllEmployees());

        assertEquals("Unable to fetch employees", ex.getMessage());
    }

    @Test
    void testGetEmployeeById_success() {
        when(restTemplate.getForEntity(contains("/d005f39a-beb8-4390-afec-fd54e91d94ee"), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(employeeJson));

        Employee emp = employeeService.getEmployeeById("d005f39a-beb8-4390-afec-fd54e91d94ee");
        assertEquals("Manish Sisod", emp.getEmployee_name());
    }

    @Test
    void testGetEmployeeById_notFound() {
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(RuntimeException.class, () -> employeeService.getEmployeeById("non-existent-id"));
    }

    @Test
    void testDeleteEmployee_success() {
        when(restTemplate.getForEntity(contains("/d005f39a-beb8-4390-afec-fd54e91d94ee"), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(employeeJson));

        ResponseEntity<JsonNode> deleteResponse =
                ResponseEntity.ok(objectMapper.createObjectNode().put("data", true));
        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        eq(JsonNode.class)))
                .thenReturn(deleteResponse);

        String deletedName = employeeService.deleteEmployee("d005f39a-beb8-4390-afec-fd54e91d94ee");
        assertEquals("Manish Sisod", deletedName);
    }

    @Test
    void testDeleteEmployee_failure() {
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class))).thenReturn(ResponseEntity.ok(employeeJson));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("Internal error"));

        assertThrows(RuntimeException.class, () -> employeeService.deleteEmployee("some-id"));
    }

    @Test
    void testGetEmployeesByNameSearch() {
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(employeeListJson));

        List<Employee> result = employeeService.getEmployeesByNameSearch("a");
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getEmployee_name());
    }

    @Test
    void testGetHighestSalary() {
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(employeeListJson));

        int highest = employeeService.getHighestSalary();
        assertEquals(200, highest);
    }

    @Test
    void testGetTopTenHighestEarnerNames() {
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(employeeListJson));

        List<String> topEarners = employeeService.getTopTenHighestEarnerNames();
        assertEquals(List.of("B", "A"), topEarners);
    }
}
