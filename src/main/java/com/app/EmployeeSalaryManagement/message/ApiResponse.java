package com.app.EmployeeSalaryManagement.message;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ApiResponse {
    Integer status;
    String message;
    Object results;
}
