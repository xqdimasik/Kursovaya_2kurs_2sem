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

public class Classifier {
    public static class Event {
        String channel;
        String date;
        String text;
        String link;
        String category;
    }

    private Map<String, String[]> getKeyWords() {
        HashMap<String, String[]> keywords = new HashMap<>();
        keywords.put("НИРС", new String[]{"конференция", "олимпиада", "хакатон", "грант", "иннофест", "исследование", "научный", "иннастарт", "разработка"});
        keywords.put("Спорт", new String[]{"турнир", "соревнование", "матч", "спартакиада", "дартс", "футбол", "баскетбол", "волейбол", "кросс", "первенство", "бег", "метание"});
        keywords.put("Патриотическое", new String[]{"победа", "герой", "ветеран", "флаг", "гимн", "герб", "память", "день победы", "мужество", "слава", "знамя", "независимость"});
        keywords.put("Общественная", new String[]{"волонтер", "фестиваль", "субботник", "концерт", "экскурсия", "акция", "мероприятие"});
        keywords.put("Духовно-нравственное", new String[]{"семья", "ценности", "духовный", "нравственный", "библия", "вера"});
        keywords.put("Психологическое", new String[]{"психолог", "буллинг", "стресс", "тревога", "поддержка"});
        keywords.put("Здоровый образ жизни", new String[]{"наркотик", "зависимость", "здоровье", "спорт", "курение", "алкоголь"});
        keywords.put("Кибербезопасность", new String[]{"кибер", "мошенник", "фишинг", "интернет", "безопасность", "взлом"});
        return keywords;
    }

    private String classify(String text) {
        String lowerText = text.toLowerCase();
        Map<String, String[]> keywords = getKeyWords();
        for (Map.Entry<String, String[]> entry : keywords.entrySet()) {
            for (String word : entry.getValue()) {
                if (lowerText.contains(word)) {
                    return entry.getKey();
                }
            }
        }
        return "Прочее";
    }

    public List<Event> classifyAll() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("data/posts.json")) {
            Type type = new TypeToken<List<TelegramParser.Post>>() {}.getType();
            List<TelegramParser.Post> posts = gson.fromJson(reader, type);
            List<Event> events = new ArrayList<>();
            for (TelegramParser.Post post : posts) {
                Event event = new Event();
                event.channel = post.channel;
                event.date = post.date;
                event.text = post.text;
                event.link = post.link;
                event.category = classify(post.text);
                events.add(event);
            }
            saveEvents(events);
            return events;
        } catch (IOException e) {
            System.err.println("Error");
        }
        return new ArrayList<>();
    }

    private void saveEvents (List<Event> events){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(FileWriter writer = new FileWriter("data/events.json")){
            gson.toJson(events, writer);
        }catch(IOException e){
            System.err.println("Error");
        }

    }

}