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
        String students;
    }

    private Map<String, String[]> getKeyWords() {
        Map<String, String[]> keywords = new HashMap<>();

        keywords.put("НИРС", new String[]{"конференция", "статья", "грант", "олимпиада", "хакатон", "исследование","иннастарт", "иннофест", "100 идей", "научный", "лаборатория", "разработка"});
        keywords.put("Спорт", new String[]{"соревнование", "турнир", "матч", "первенство", "кросс", "футбол","баскетбол", "волейбол", "спартакиада", "дартс", "бег", "метание", "тренировка", "чемпионат" });
        keywords.put("Общественная деятельность", new String[]{ "волонтер", "волонтерство", "концерт", "фестиваль", "субботник", "экскурсия", "акция", "благотворительность", "митинг" });
        keywords.put("Идеологическое воспитание", new String[]{ "урок мужества", "день победы", "возложение цветов", "митинг", "ветеран",   "память", "герой", "воинская слава", "знамя", "гимн", "день независимости","день народного единства", "9 мая", "23 февраля", "государственные символы", "флаг", "герб", "патриотизм", "гражданственность"});
        keywords.put("Духовно-нравственное воспитание", new String[]{"духовность", "нравственность", "мораль", "этика", "доброта", "милосердие",  "сострадание", "честь", "достоинство"  });
        keywords.put("Эстетическое воспитание", new String[]{"искусство", "живопись", "музыка", "театр", "красота", "дизайн",  "выставка", "творчество", "эстетика" });
        keywords.put("Воспитание психологической культуры", new String[]{"психолог", "психология", "стресс", "тревога", "поддержка", "буллинг","конфликт", "тренинг", "психическое здоровье" });
        keywords.put("Формирование здорового образа жизни", new String[]{  "зож", "здоровье", "наркотики", "алкоголь", "курение", "зависимость",   "профилактика", "здоровый образ", "спорт" });
        keywords.put("Воспитание физической культуры", new String[]{ "физкультура", "зарядка", "разминка", "гто", "нормативы", "физическая активность"  });
        keywords.put("Семейное и гендерное воспитание", new String[]{"семья", "родители", "дети", "гендер", "равенство", "материнство", "отцовство", "семейные ценности"  });
        keywords.put("Трудовое и профессиональное воспитание", new String[]{"профориентация", "карьера", "работа", "профессия", "трудоустройство","стажировка", "мастер-класс", "профессиональное", "навыки" });
        keywords.put("Бережное отношение к окружающей среде", new String[]{"экология", "природа", "мусор", "уборка", "сортировка", "зеленый","эко", "лес", "чистота", "окружающая среда", "переработка"});
        keywords.put("Культура безопасной жизнедеятельности", new String[]{"безопасность", "чс", "пожар", "эвакуация", "обж", "авария", "правила поведения", "терроризм", "экстремальная ситуация"});
        keywords.put("Культура быта и досуга", new String[]{ "досуг", "отдых", "хобби", "быт", "уют", "праздник", "развлечение", "вечеринка", "культура быта"});
        keywords.put("Поликультурное воспитание", new String[]{"толерантность", "культура", "традиции", "национальность","межнациональный", "дружба народов", "диалог культур"});
        keywords.put("Экономическое воспитание", new String[]{"финансы", "бюджет", "экономика", "деньги", "сбережение", "налоги","бизнес", "финансовая грамотность" });
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
                StudentMatcher matcher = new StudentMatcher();
                event.students = matcher.findStudents(post.text);
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