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


This module (api) acts as a REST client abstraction for interacting with the Mock Employee API hosted at http://localhost:8112/api/v1/employee.

Core logic is implemented in the service class:
com.reliaquest.api.service.EmployeeService

It uses RestTemplate for HTTP interactions and ObjectMapper (Jackson) to handle dynamic JSON (JsonNode).

API response parsing and filtering are done in-memory as required (e.g., for top 10 earners).

Exception handling and logging are included as part of production-ready standards.

📁 Key Classes
Package	Class	Responsibility
com.reliaquest.api.model	Employee, EmployeeInput	Data models
com.reliaquest.api.service	EmployeeService	Main service with business logic
com.reliaquest.api.controller	IEmployeeController	Interface and it's implementation for expected controller endpoints
com.reliaquest.api.exception	Custom error handling

🧪 Testing Strategy
Unit and integration tests are located under:


src/test/java/com.reliaquest.api/
├── common/         # Common test utilities
├── integration/    # Integration tests (using WireMock)
├── service/        # Unit tests for EmployeeService
└── ApiApplicationTest.java


Unit Tests:
Target EmployeeService using mocks and sample JSON responses via JsonNode.

Integration Tests:
Use WireMock to simulate backend API on port 8113.
Real HTTP calls and assertions on full response structure.

Fixtures:
All test request/response JSON payloads are extracted to:

src/test/resources/testdata/
