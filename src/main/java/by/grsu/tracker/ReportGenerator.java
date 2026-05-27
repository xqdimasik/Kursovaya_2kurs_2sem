package by.grsu.tracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportGenerator {

    public void generate(String period) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("data/events.json")) {

            Type type = new TypeToken<List<Classifier.Event>>() {}.getType();
            List<Classifier.Event> events = gson.fromJson(reader, type);

            Map<String, List<Classifier.Event>> grouped = new HashMap<>();
            for (Classifier.Event event : events) {
                grouped.computeIfAbsent(event.category, k -> new ArrayList<>()).add(event);
            }

            String filename = "reports/report_" + period + ".txt";
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {

                writer.println("  ОТЧЁТ ФаМИ ГрГУ — период: " + period);
                writer.println("  Сгенерирован: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                writer.println();

                for (Map.Entry<String, List<Classifier.Event>> entry : grouped.entrySet()) {
                    writer.println("[ " + entry.getKey() + " ]");
                    writer.println("----------------------------------------");
                    for (Classifier.Event event : entry.getValue()) {
                        writer.println("Дата:     " + event.date);
                        writer.println("Канал:    " + event.channel);
                        writer.println("Описание: " + event.text.substring(0, Math.min(100, event.text.length())) + "...");
                        writer.println("Студенты: " + (event.students != null && !event.students.isEmpty() ? event.students : "—"));
                        writer.println("Ссылка:   " + event.link);
                        writer.println();
                    }
                }
                System.out.println("Отчёт сохранён: " + filename);
            }

        } catch (IOException e) {
            System.err.println("Ошибка генерации отчёта: " + e.getMessage());
        }
    }
}