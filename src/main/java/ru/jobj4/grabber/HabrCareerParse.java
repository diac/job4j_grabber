package ru.jobj4.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.jobj4.grabber.utils.DateTimeParser;
import ru.jobj4.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final DateTimeParser DATE_TIME_PARSER = new HabrCareerDateTimeParser();

    public static void main(String[] args) throws IOException {
        Elements rows = new Elements();
        for (int pageIndex = 1; pageIndex <= 5; pageIndex++) {
            String url = PAGE_LINK + String.format("?page=%d", pageIndex);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();
            rows.addAll(document.select(".vacancy-card__inner"));
        }
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            Element dateElement = row.select("time.basic-date").first();
            String vacancyName = titleElement.text();
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            LocalDateTime date = DATE_TIME_PARSER.parse(dateElement.attr("datetime"));
            System.out.printf(
                    "%s %s %s%n",
                    vacancyName,
                    link,
                    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
            );
            System.out.println(retrieveDescription(link));
            System.out.println("=========\n");
        });
    }

    private static String retrieveDescription(String link) {
        String description = "";
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            Element vacancyDescription = document.select(".vacancy-show .page-section .collapsible-description .style-ugc").first();
            description = vacancyDescription.html();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return description;
    }
}
