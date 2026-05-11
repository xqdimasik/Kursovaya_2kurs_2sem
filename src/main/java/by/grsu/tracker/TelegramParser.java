package by.grsu.tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TelegramParser {
    private static  final String[]CHANNELS = {"famigrsu", "grsu_official", "kupaly_brsm"};

    public static class Post{
        public String channel;
        public String date;
        public String text;
        public String link;
    }
        private List<Post> parseChannel(String channelName){
        List<Post> posts = new ArrayList<>();
        String url;
        try {
            url = "https://t.me/s/" + channelName;
            Document doc = Jsoup.connect(url).userAgent("Mozzila/5.0").timeout(10000).get();
            Elements message = doc.select(".tgme_widget_message");
            for(Element msg : message){
                Post post = new Post();
                post.channel = channelName;
                Element textEl = msg.selectFirst(".tgme_widget_message_text");
                if(textEl == null){
                    continue;
                }else{post.text = textEl.text();}
                Element dateEl = msg.selectFirst(".tgme_widget_message_date time");
                if(dateEl != null){
                    post. date = dateEl.attr("datetime");
                }else{}
                Element linkEl = msg.selectFirst(".tgme_widget_message_date");
                if(linkEl != null){
                    post.link = linkEl.attr("href");
                }else{}
                posts.add(post);
            }

        }catch(IOException e){
            System.err.println(channelName + e.getMessage());
        }
        return posts;
        }

    private void savePosts(List<Post> posts) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("data/posts.json")) {
            gson.toJson(posts, writer);
        } catch (IOException e) {
            System.err.println();
        }
    }
    public List<Post> parseAll(){
        List<Post> allPosts = new ArrayList<>();
        for(String channel : CHANNELS){
            List<Post> parsed = parseChannel(channel);
            allPosts.addAll(parsed);
        }
         savePosts(allPosts);
         return allPosts;
    }

}


