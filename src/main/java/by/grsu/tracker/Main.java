package by.grsu.tracker;

public class Main {
    public static void main(String[] args) {
      TelegramParser parser = new TelegramParser();
      parser.parseAll();

        Classifier classifier = new Classifier();
        classifier.classifyAll();

        ReportGenerator generator = new ReportGenerator();
        generator.generate("май-2026");



        }
    }
