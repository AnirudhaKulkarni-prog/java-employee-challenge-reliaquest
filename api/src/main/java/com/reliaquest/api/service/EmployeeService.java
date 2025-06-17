package com.reliaquest.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.base.url}")
    private String SERVER_BASE_URL;

    public List<Employee> getAllEmployees() {
        try {
            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(SERVER_BASE_URL + "/api/v1/employee", JsonNode.class);
            return parseEmployeeList(response.getBody());
        } catch (Exception e) {
            log.error("Failed to fetch all employees", e);
            throw new RuntimeException("Unable to fetch employees");
        }
    }

    public Employee getEmployeeById(String id) {
        try {
            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(SERVER_BASE_URL + "/api/v1/employee" + "/" + id, JsonNode.class);
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new EmployeeNotFoundException("Employee not found with ID: " + id);
            }
            return objectMapper.treeToValue(response.getBody().get("data"), Employee.class);
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching employee by ID {}", id, e);
            throw new RuntimeException("Unable to fetch employee");
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
                .orElseThrow(() -> new RuntimeException("No employees found"));
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
                    restTemplate.postForEntity(SERVER_BASE_URL + "/api/v1/employee", entity, JsonNode.class);
            System.out.println(response);
            return objectMapper.treeToValue(response.getBody().get("data"), Employee.class);
        } catch (Exception e) {
            log.error("Error creating employee", e);
            throw new RuntimeException("Unable to create employee");
        }
    }

    public String deleteEmployee(String id) {
        try {
            // Get employee before deletion to get their name
            Employee employee = getEmployeeById(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", employee.getEmployee_name());

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    SERVER_BASE_URL + "/api/v1/employee", HttpMethod.DELETE, request, JsonNode.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to delete employee");
            }

            return employee.getEmployee_name();
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting employee with ID {}", id, e);
            throw new RuntimeException("Unable to delete employee");
        }
    }

    private List<Employee> parseEmployeeList(JsonNode body) {
        try {
            JsonNode dataNode = body.get("data");
            Employee[] employees = objectMapper.treeToValue(dataNode, Employee[].class);
            return Arrays.asList(employees);
        } catch (Exception e) {
            log.error("Failed to parse employee list", e);
            throw new RuntimeException("Invalid response structure");
        }
    }
}
