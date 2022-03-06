package com.app.EmployeeSalaryManagement.controller;

import com.app.EmployeeSalaryManagement.helper.CSVHelper;
import com.app.EmployeeSalaryManagement.message.ApiResponse;
import com.app.EmployeeSalaryManagement.message.ResponseMessage;
import com.app.EmployeeSalaryManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        if (CSVHelper.hasCSVFormat(file)) {
            return userService.save(file);
        } else {
            String message = "Please upload a csv file!";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
        }
    }

    @GetMapping
    public ApiResponse sortedResult(@RequestParam(required = false, defaultValue = "0.0") Double minSalary,
                                    @RequestParam(required = false, defaultValue = "4000.0") Double maxSalary,
                                    @RequestParam(required = false, defaultValue = "0") Integer offset,
                                    @RequestParam(required = false, defaultValue = "0") Integer limit) {

        return userService.getAllUsers(minSalary, maxSalary, offset, limit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable(name = "id") String userId) {
        return userService.getUserById(userId);
    }
}