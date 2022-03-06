package com.app.EmployeeSalaryManagement.service;

import com.app.EmployeeSalaryManagement.helper.CSVHelper;
import com.app.EmployeeSalaryManagement.message.ApiResponse;
import com.app.EmployeeSalaryManagement.message.ResponseMessage;
import com.app.EmployeeSalaryManagement.model.User;
import com.app.EmployeeSalaryManagement.repository.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public ResponseEntity<Object> save(MultipartFile file) {
        try {
            List<User> users = new ArrayList<>();
            HashMap<String, Object> objectHashMap = CSVHelper.csvToTutorials(file.getInputStream());
            JSONObject jsonObject = new JSONObject(objectHashMap);
            if (!jsonObject.getString("message").isEmpty()) {
                return new ResponseEntity<>(jsonObject.getString("message"), HttpStatus.BAD_REQUEST);
            }

            JSONArray jsonArray = jsonObject.getJSONArray("result");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                User user = new User();
                user.setId(explrObject.getString("id"));
                user.setLogin(explrObject.getString("login"));
                user.setName(explrObject.getString("name"));
                user.setSalary(explrObject.getDouble("salary"));
                user.setStartDate(LocalDate.parse(explrObject.getString("startDate")));
                users.add(user);
            }

            for (User user : users
            ) {
                if (user.getId().equalsIgnoreCase("")) {
                    return new ResponseEntity<>("Id is not Present in one of the fields", HttpStatus.OK);
                } else if (user.getLogin().equalsIgnoreCase("")) {
                    return new ResponseEntity<>("Login is not Present in one of the fields", HttpStatus.OK);
                } else if (user.getName().equalsIgnoreCase("")) {
                    return new ResponseEntity<>("Name is not Present in one of the fields", HttpStatus.OK);
                } else if (user.getSalary().equals(null)) {
                    return new ResponseEntity<>("Salary is not Present in one of the fields", HttpStatus.OK);
                } else if (user.getStartDate().equals(null)) {
                    return new ResponseEntity<>("Date is not Present in one of the fields", HttpStatus.OK);
                } else {
                    userRepository.save(user);
                }
            }
            String message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return new ResponseEntity<>(new ResponseMessage(message), HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ApiResponse getAllUsers(Double minSalary, Double maxSalary, Integer offset, Integer limit) {
        ApiResponse apiResponse = new ApiResponse();
        try {

            List<User> usersList = userRepository.findUserBySalaryBetweenOrderByIdAsc(minSalary, maxSalary);
            if (usersList.isEmpty()) {
                apiResponse.setMessage("There is no user in the database");
                apiResponse.setResults(null);
            } else {
                if (offset > usersList.size()) {
                    apiResponse.setMessage("We don't have that much records in our database, Kindly change the offset value");
                    apiResponse.setResults(null);
                } else {
                    List<User> tempList = new ArrayList<>();
                    for (int i = offset; i <= usersList.size(); i++) {
                        try {
                            tempList.add(usersList.get(i));
                        } catch (Exception e) {
                            break;
                        }
                    }
                    List<User> tempList2 = new ArrayList<>();
                    if (limit > 0) {
                        for (int i = 0; i < limit; i++) {
                            tempList2.add(tempList.get(i));
                        }
                        apiResponse.setMessage("Successfully fetched the users from the database");
                        apiResponse.setResults(tempList2);
                    } else if (limit == 0) {
                        apiResponse.setMessage("Successfully fetched the users from the database");
                        apiResponse.setResults(tempList);
                    } else {
                        apiResponse.setResults(null);
                        apiResponse.setMessage("Kindly enter the limit value 0 or more than 0");
                    }
                }
                apiResponse.setStatus(HttpStatus.OK.value());
                return apiResponse;
            }
            apiResponse.setStatus(HttpStatus.OK.value());
            return apiResponse;
        } catch (Exception e) {
            apiResponse.setResults(null);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return apiResponse;
        }
    }

    public ResponseEntity<Object> getUserById(String userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                return new ResponseEntity<>(user.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found against this Id", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
