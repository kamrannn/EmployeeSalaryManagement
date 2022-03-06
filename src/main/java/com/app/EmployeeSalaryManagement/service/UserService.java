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

/**
 * The type User service.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * Instantiates a new User service.
     *
     * @param userRepository the user repository
     */
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Save Employee through CSV file.
     *
     * @param file the file
     * @return the response entity
     */
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
                JSONObject userJsonObject = jsonArray.getJSONObject(i);
                User user = new User();
                user.setId(userJsonObject.getString("id"));
                user.setLogin(userJsonObject.getString("login"));
                user.setName(userJsonObject.getString("name"));
                user.setSalary(userJsonObject.getDouble("salary"));
                user.setStartDate(LocalDate.parse(userJsonObject.getString("startDate")));
                users.add(user);
            }
            userRepository.saveAll(users);
            String message = "Uploaded all the students successfully by using file with name: " + file.getOriginalFilename();
            return new ResponseEntity<>(new ResponseMessage(message), HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Gets all users.
     *
     * @param minSalary the min salary
     * @param maxSalary the max salary
     * @param offset    the offset
     * @param limit     the limit
     * @return the all users
     */
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

    /**
     * Gets user by id.
     *
     * @param userId the user id
     * @return the user by id
     */
    public ApiResponse getUserById(String userId) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            User user = userRepository.findUserById(userId);
            if (null != user) {
                apiResponse.setResults(user);
                apiResponse.setMessage("Successfully fetched the user against ID: " + userId);
            } else {
                apiResponse.setResults(null);
                apiResponse.setMessage("User not found against this Id");
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

    /**
     * Add new User Service
     *
     * @param user the user
     * @return the api response
     */
    public ApiResponse addUser(User user) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            if (user.getId().equalsIgnoreCase("")) {
                apiResponse.setMessage("Id is not Present in one of the fields");
                apiResponse.setResults(null);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            } else if (user.getLogin().equalsIgnoreCase("")) {
                apiResponse.setMessage("Login is not Present in one of the fields");
                apiResponse.setResults(null);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            } else if (user.getName().equalsIgnoreCase("")) {
                apiResponse.setMessage("Name is not Present in one of the fields");
                apiResponse.setResults(null);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            } else if (user.getSalary().longValue() < 0) {
                apiResponse.setMessage("Salary is not Present in one of the fields");
                apiResponse.setResults(null);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            } else if (user.getStartDate() == null) {
                apiResponse.setMessage("Date is not Present in one of the fields");
                apiResponse.setResults(null);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            } else {
                userRepository.save(user);
                apiResponse.setResults(user);
                apiResponse.setStatus(HttpStatus.CREATED.value());
            }
            return apiResponse;
        } catch (Exception e) {
            apiResponse.setResults(null);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return apiResponse;
        }
    }

    /**
     * Update the User Service.
     *
     * @param user the user
     * @return the api response
     */
    public ApiResponse updateUser(User user) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            if (user.getId().equalsIgnoreCase("")) {
                apiResponse.setMessage("Id is not Present in one of the fields");
                apiResponse.setResults(null);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            } else if (user.getLogin().equalsIgnoreCase("")) {
                apiResponse.setMessage("Login is not Present in one of the fields");
                apiResponse.setResults(null);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            } else if (user.getName().equalsIgnoreCase("")) {
                apiResponse.setMessage("Name is not Present in one of the fields");
                apiResponse.setResults(null);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            } else if (user.getSalary().longValue() < 0) {
                apiResponse.setMessage("Salary is not Present in one of the fields");
                apiResponse.setResults(null);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            } else if (user.getStartDate() == null) {
                apiResponse.setMessage("Kindly check the date");
                apiResponse.setResults(null);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            } else {
                User existingUser = userRepository.findUserById(user.getId());
                if (null != existingUser) {
                    existingUser.setLogin(user.getLogin());
                    existingUser.setName(user.getName());
                    existingUser.setSalary(user.getSalary());
                    existingUser.setStartDate(user.getStartDate());
                    userRepository.save(existingUser);
                    apiResponse.setResults(existingUser);
                    apiResponse.setStatus(HttpStatus.CREATED.value());
                    apiResponse.setMessage("Successfully Updated");
                }
            }
            return apiResponse;
        } catch (Exception e) {
            apiResponse.setResults(null);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return apiResponse;
        }
    }

    /**
     * Delete user by id api response.
     *
     * @param userId the user id
     * @return the api response
     */
    public ApiResponse deleteUserById(String userId) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            User user = userRepository.findUserById(userId);
            if (null != user) {
                userRepository.delete(user);
                apiResponse.setMessage("Successfully deleted");
            } else {
                apiResponse.setMessage("No such employee");
            }
            apiResponse.setResults(null);
            apiResponse.setStatus(HttpStatus.OK.value());
            return apiResponse;
        } catch (Exception e) {
            apiResponse.setResults(null);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return apiResponse;
        }
    }
}
