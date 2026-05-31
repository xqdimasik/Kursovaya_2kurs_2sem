package by.grsu.tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Classifier {

    public static class Event {
        String channel;
        String date;
        String text;
        String link;
        String category;
        String students;
    }

    public static class Category {
        public String name;
        public String pattern;
    }

    private List<Category> loadCategories() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("data/categories.json")) {
            Type type = new com.google.gson.reflect.TypeToken<List<Category>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки категорий: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String classify(String text) {
        String lowerText = text.toLowerCase();
        List<Category> categories = loadCategories();
        for (Category category : categories) {
            try {
                Pattern p = Pattern.compile(category.pattern,
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                if (p.matcher(lowerText).find()) {
                    return category.name;
                }
            } catch (Exception e) {
                System.err.println("Неверный паттерн в категории " + category.name);
            }
        }
        return "Прочее";
    }

    public List<Event> classifyAll() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("data/posts.json")) {
            Type type = new com.google.gson.reflect.TypeToken<List<TelegramParser.Post>>() {}.getType();
            List<TelegramParser.Post> posts = gson.fromJson(reader, type);
            List<Event> events = new ArrayList<>();
            for (TelegramParser.Post post : posts) {
                Event event = new Event();
                event.channel = post.channel;
                event.date = post.date;
                event.text = post.text;
                event.link = post.link;
                event.category = classify(post.text);
                StudentMatcher matcher = new StudentMatcher();
                event.students = matcher.findStudents(post.text);
                events.add(event);
            }
            saveEvents(events);
            return events;
        } catch (IOException e) {
            System.err.println("Ошибка классификации: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private void saveEvents(List<Event> events) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("data/events.json")) {
            gson.toJson(events, writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения событий: " + e.getMessage());
        }
    }
}