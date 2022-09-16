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
        Connection connection = Jsoup.connect(PAGE_LINK);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            Element dateElement = row.select("time.basic-date").first();
            String vacancyName = titleElement.text();
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            LocalDateTime date = DATE_TIME_PARSER.parse(dateElement.attr("datetime"));
            System.out.printf("%s %s %s%n", vacancyName, link, date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)));
        });
    }
}
