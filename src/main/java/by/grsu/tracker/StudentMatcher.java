package by.grsu.tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentMatcher {
    public static  class Student{
        String name;
        String surname;
        String group;
    }
    private List<Student> loadStudent(){
        Gson gson = new Gson();
        try(FileReader reader = new FileReader("data/students.json")){
            Type type = new TypeToken<List<Student>>() {}.getType();
            List<Student> students = gson.fromJson(reader, type);
            return students;

        } catch (IOException e) {return new ArrayList<>();}
    }
    public String findStudents(String text){
        List<Student> student = loadStudent();
        List<String> found = new ArrayList<>();
        for(Student students : student){
            if(text.contains(students.surname)){
                found.add(students.name);
            }
        }
        return String.join(", ", found);
    }
}
