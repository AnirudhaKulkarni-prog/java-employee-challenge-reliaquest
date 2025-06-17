package com.reliaquest.api.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeInput {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @Min(value = 1, message = "Salary must be greater than 0")
    private int salary;

    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    private int age;

    @NotBlank(message = "Title must not be blank")
    private String title;
}
