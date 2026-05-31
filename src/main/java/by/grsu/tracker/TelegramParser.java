package by.grsu.tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TelegramParser {

    public static class Post {
        public String channel;
        public String date;
        public String text;
        public String link;
    }

    public static class Channel {
        public String name;
        public boolean selected;
    }

    private List<Channel> loadChannels() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("data/channels.json")) {
            Type type = new TypeToken<List<Channel>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки каналов: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Post> parseChannel(String channelName) {
        List<Post> posts = new ArrayList<>();
        try {
            String url = "https://t.me/s/" + channelName;
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10000).get();
            Elements messages = doc.select(".tgme_widget_message");
            for (Element msg : messages) {
                Post post = new Post();
                post.channel = channelName;
                Element textEl = msg.selectFirst(".tgme_widget_message_text");
                if (textEl == null) continue;
                post.text = textEl.text();
                Element dateEl = msg.selectFirst(".tgme_widget_message_date time");
                if (dateEl != null) post.date = dateEl.attr("datetime");
                Element linkEl = msg.selectFirst(".tgme_widget_message_date");
                if (linkEl != null) post.link = linkEl.attr("href");
                posts.add(post);
            }
        } catch (IOException e) {
            System.err.println("Ошибка парсинга канала " + channelName + ": " + e.getMessage());
        }
        return posts;
    }

    private void savePosts(List<Post> posts) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("data/posts.json")) {
            gson.toJson(posts, writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения постов: " + e.getMessage());
        }
    }

    public List<Post> parseAll() {
        List<Channel> channels = loadChannels();
        List<Post> allPosts = new ArrayList<>();
        for (Channel channel : channels) {
            if (channel.selected) {
                List<Post> parsed = parseChannel(channel.name);
                allPosts.addAll(parsed);
            }
        }
        savePosts(allPosts);
        return allPosts;
    }
}