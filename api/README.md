# Implement this API

#### In this assessment you will be tasked with filling out the functionality of different methods that will be listed further down.

These methods will require some level of API interactions with Mock Employee API at http://localhost:8112/api/v1/employee.

Please keep the following in mind when doing this assessment:
* clean coding practices
* test driven development
* logging
* scalability

### Endpoints to implement

_See `com.reliaquest.api.controller.IEmployeeController` for details._

getAllEmployees()

    output - list of employees
    description - this should return all employees

getEmployeesByNameSearch(...)

    path input - name fragment
    output - list of employees
    description - this should return all employees whose name contains or matches the string input provided

getEmployeeById(...)

    path input - employee ID
    output - employee
    description - this should return a single employee

getHighestSalaryOfEmployees()

    output - integer of the highest salary
    description - this should return a single integer indicating the highest salary of amongst all employees

getTop10HighestEarningEmployeeNames()

    output - list of employees
    description - this should return a list of the top 10 employees based off of their salaries

createEmployee(...)

    body input - attributes necessary to create an employee
    output - employee
    description - this should return a single employee, if created, otherwise error

deleteEmployeeById(...)

    path input - employee ID
    output - name of the employee
    description - this should delete the employee with specified id given, otherwise error

### Testing
Please include proper integration and/or unit tests.

# Employee API Module – ReliaQuest Coding Challenge

This module implements the API logic for interacting with a Mock Employee REST service hosted at:

🔗 `http://localhost:8112/api/v1/employee`

---

## ✅ Functionality Implemented

| HTTP Method | Endpoint                             | Description                                                        |
|-------------|--------------------------------------|--------------------------------------------------------------------|
| `GET`       | `/api/v1/employee`                         | Returns all employees                                              |
| `GET`       | `/api/v1/employee/search/{name}`           | Returns employees matching the given name fragment                 |
| `GET`       | `/api/v1/employee/{id}`                    | Returns a single employee by UUID                                  |
| `GET`       | `/api/v1/employee/highestSalary`           | Returns the highest salary among all employees                     |
| `GET`       | `/api/v1/employee/topTenHighestEarningEmployeeNames`            | Returns the names of the top 10 highest-earning employees          |
| `POST`      | `/api/v1/employee`                         | Creates a new employee and returns the created employee            |
| `DELETE`    | `/api/v1/employee/{id}`                    | Deletes an employee by UUID and returns their name                 |

All endpoints are implemented in the `EmployeeController` and backed by the `EmployeeService` class.

---

## 🧠 Architecture Overview

| Layer       | Package                      | Responsibility                                     |
|------------|------------------------------|---------------------------------------------------|
| Model       | `com.reliaquest.api.model`   | `Employee`, `EmployeeInput` DTOs                  |
| Controller  | `com.reliaquest.api.controller` | REST controller + interface definitions          |
| Service     | `com.reliaquest.api.service` | Core business logic, backend API communication    |
| Exception   | `com.reliaquest.api.exception`| Custom exception handling                         |

---

## 🧪 Testing Strategy

The API module includes **complete unit and integration test coverage**.

### ✅ Unit Tests

- Located under:  
  `src/test/java/com/reliaquest/api/service/`
  
- Tests for `EmployeeService` using:
  - `Mockito`
  - `JsonNode` mock parsing
  - Error and edge case scenarios

### ✅ Integration Tests

- Located under:  
  `src/test/java/com/reliaquest/api/integration/`

- Uses **WireMock** on port `8113` to simulate backend API responses
- Tests full request/response structure using `TestRestTemplate`
- Covers success and error paths for all controller endpoints

### ✅ Test Fixtures

- All test payloads are placed under:  
  `src/test/resources/testdata/`

- These include:
  - Mock JSON responses for backend simulation
  - Expected response payloads for assertions

---

## 🔧 Technologies Used

- Java 17+
- Spring Boot 3.x
- RestTemplate + ObjectMapper (`JsonNode`)
- JUnit 5
- Mockito
- WireMock
- Spotless (code formatting)

---

## 📁 Folder Structure (Relevant to This Module)

```text

api/
├── src/
│ ├── main/
│ │ └── java/com/reliaquest/api/
│ │ ├── controller/ # REST controllers
│ │ ├── service/ # Core service logic
│ │ ├── model/ # DTOs and models
│ │ └── exception/ # Custom exceptions
│ └── test/
│ └── java/com/reliaquest/api/
│ ├── service/ # Unit tests
│ ├── integration/ # WireMock integration tests
│ └── common/ # Common test setup (if any)
│
└── test/resources/testdata/ # JSON fixture files

```
Run the application using:

```bash
./gradlew bootRun
```

Run all tests using:
```bash
./gradlew test
```
Format code using:
```bash
./gradlew spotlessApply
```
