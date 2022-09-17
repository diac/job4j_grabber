package ru.jobj4.grabber;

import ru.jobj4.grabber.model.Post;
import ru.jobj4.grabber.utils.HabrCareerDateTimeParser;
import ru.jobj4.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        cnn = DriverManager.getConnection(
                cfg.getProperty("jdbc.url"),
                cfg.getProperty("jdbc.username"),
                cfg.getProperty("jdbc.password")
        );
    }

    @Override
    public void save(Post post) {
        String sql = """
                INSERT INTO POST
                    (link, name, text, created)
                VALUES
                    (?, ?, ?, ?)
                ON CONFLICT (link) DO NOTHING;
                """;
        try (PreparedStatement statement = cnn.prepareStatement(sql)) {
            statement.setString(1, post.getLink());
            statement.setString(2, post.getTitle());
            statement.setString(3, post.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post;";
        try (Statement statement = cnn.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                posts.add(fromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        String sql = "SELECT * FROM post WHERE id = ?;";
        try (PreparedStatement statement = cnn.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                post = fromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("app.properties")) {
            Properties config = new Properties();
            config.load(in);
            Parse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
            List<Post> posts = parse.list("https://career.habr.com/vacancies/java_developer");
            try (PsqlStore store = new PsqlStore(config)) {
                for (var post : posts) {
                    store.save(post);
                }
                Post demoPost = store.getAll().get(0);
                System.out.println(demoPost);
                System.out.println(demoPost.getDescription());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Post fromResultSet(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("link"),
                resultSet.getString("text"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }
}
