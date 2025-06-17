package com.reliaquest.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.base.url}")
    private String SERVER_BASE_URL;

    private static final String EMPLOYEE_API = "/api/v1/employee";

    public List<Employee> getAllEmployees() {
        try {
            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(SERVER_BASE_URL + EMPLOYEE_API, JsonNode.class);
            return parseEmployeeList(response.getBody());
        } catch (Exception e) {
            log.error("Failed to fetch all employees", e);
            throw new EmployeeServiceException("Unable to fetch employees", e);
        }
    }

    public Employee getEmployeeById(String id) {
        try {
            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(SERVER_BASE_URL + EMPLOYEE_API + "/" + id, JsonNode.class);

            JsonNode body = response.getBody();
            if (body == null || !body.has("data")) {
                throw new EmployeeServiceException("Missing 'data' in response", null);
            }

            return objectMapper.treeToValue(body.get("data"), Employee.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EmployeeNotFoundException("Employee not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error fetching employee by ID {}", id, e);
            throw new EmployeeServiceException("Unable to fetch employee", e);
        }
    }

    public List<Employee> getEmployeesByNameSearch(String nameFragment) {
        return getAllEmployees().stream()
                .filter(e -> e.getEmployee_name().toLowerCase().contains(nameFragment.toLowerCase()))
                .collect(Collectors.toList());
    }

    public int getHighestSalary() {
        return getAllEmployees().stream()
                .mapToInt(Employee::getEmployee_salary)
                .max()
                .orElseThrow(() -> new EmployeeServiceException("No employees found", null));
    }

    public List<String> getTopTenHighestEarnerNames() {
        return getAllEmployees().stream()
                .sorted(Comparator.comparingInt(Employee::getEmployee_salary).reversed())
                .limit(10)
                .map(Employee::getEmployee_name)
                .collect(Collectors.toList());
    }

    public Employee createEmployee(EmployeeInput input) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("name", input.getName());
            request.put("salary", input.getSalary());
            request.put("age", input.getAge());
            request.put("title", input.getTitle());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<JsonNode> response =
                    restTemplate.postForEntity(SERVER_BASE_URL + EMPLOYEE_API, entity, JsonNode.class);

            JsonNode body = response.getBody();
            if (body == null || !body.has("data")) {
                throw new EmployeeServiceException("Missing 'data' in response", null);
            }

            return objectMapper.treeToValue(body.get("data"), Employee.class);
        } catch (Exception e) {
            log.error("Error creating employee", e);
            throw new EmployeeServiceException("Unable to create employee", e);
        }
    }

    public String deleteEmployee(String id) {
        try {
            Employee employee = getEmployeeById(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", employee.getEmployee_name());

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<JsonNode> response =
                    restTemplate.exchange(SERVER_BASE_URL + EMPLOYEE_API, HttpMethod.DELETE, request, JsonNode.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new EmployeeServiceException("Failed to delete employee", null);
            }

            return employee.getEmployee_name();
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting employee with ID {}", id, e);
            throw new EmployeeServiceException("Unable to delete employee", e);
        }
    }

    private List<Employee> parseEmployeeList(JsonNode body) {
        try {
            if (body == null || !body.has("data")) {
                throw new EmployeeServiceException("Missing 'data' field in response", null);
            }
            JsonNode dataNode = body.get("data");
            Employee[] employees = objectMapper.treeToValue(dataNode, Employee[].class);
            return Arrays.asList(employees);
        } catch (Exception e) {
            log.error("Failed to parse employee list", e);
            throw new EmployeeServiceException("Invalid response structure", e);
        }
    }
}
