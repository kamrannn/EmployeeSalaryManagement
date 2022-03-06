package com.app.EmployeeSalaryManagement.helper;

import com.app.EmployeeSalaryManagement.model.User;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSVHelper {
    public static String TYPE = "text/csv";
    static String[] HEADERs = {"Id", "Title", "Description", "Published"};

    public static boolean hasCSVFormat(MultipartFile file) {
        if (!TYPE.equals(file.getContentType())) {
            return false;
        }
        return true;
    }

    public static HashMap<String, Object> csvToTutorials(InputStream is) {
        HashMap<String, Object> map = new HashMap<>();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {
            List<User> users = new ArrayList<User>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord csvRecord : csvRecords) {
                if (csvRecord.get("id").equalsIgnoreCase("")
                        || csvRecord.get("login").equalsIgnoreCase("")
                        || csvRecord.get("name").equalsIgnoreCase("")
                        || csvRecord.get("salary") == null
                        || csvRecord.get("startDate") == null) {
                    map.put("message", "There are some empty fields in the excel file");
                    return map;
                }
                User user = new User(
                        csvRecord.get("id"),
                        csvRecord.get("login"),
                        csvRecord.get("name"),
                        Double.parseDouble(csvRecord.get("salary")),
                        LocalDate.parse(csvRecord.get("startDate"))
                );
                for (User existingUser : users
                ) {
                    if (existingUser.getId().equalsIgnoreCase(user.getId())) {
                        map.put("message", "Duplicate ID's found in the csv file");
                        return map;
                    } else if (existingUser.getLogin().equalsIgnoreCase(user.getLogin())) {
                        map.put("message", "Duplicate Login's found in the csv file");
                        return map;
                    }
                }
                users.add(user);
            }
            map.put("message", "");
            map.put("result", users);
            return map;
        } catch (IOException e) {
            map.put("Exception", e.getMessage());
            return map;
        }
    }
}
