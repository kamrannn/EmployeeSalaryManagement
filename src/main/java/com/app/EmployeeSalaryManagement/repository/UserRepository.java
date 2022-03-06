package com.app.EmployeeSalaryManagement.repository;

import com.app.EmployeeSalaryManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    List<User> findUserBySalaryBetweenOrderByIdAsc(Double minSalary, Double maxSalary);

    User findUserById(String id);
}
