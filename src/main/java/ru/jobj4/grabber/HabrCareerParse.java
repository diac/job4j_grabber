package ru.jobj4.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.jobj4.grabber.model.Post;
import ru.jobj4.grabber.utils.DateTimeParser;
import ru.jobj4.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        List<Post> posts = new ArrayList<>();
        Parse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        for (int pageIndex = 1; pageIndex <= 5; pageIndex++) {
            String url = PAGE_LINK + String.format("?page=%d", pageIndex);
            posts.addAll(parse.list(url));
        }
        for (var post : posts) {
            System.out.println(post.toString());
        }
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        try {
            Elements rows = new Elements();
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            rows.addAll(document.select(".vacancy-card__inner"));
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select("time.basic-date").first();
                String vacancyName = titleElement.text();
                String postLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String description = retrieveDescription(postLink);
                LocalDateTime date = dateTimeParser.parse(dateElement.attr("datetime"));
                posts.add(new Post(vacancyName, postLink, description, date));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return posts;
    }

    private static String retrieveDescription(String link) {
        String description = "";
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            Element vacancyDescription
                    = document.select(".vacancy-show .page-section .collapsible-description .style-ugc")
                    .first();
            description = vacancyDescription.html();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return description;
    }
}
