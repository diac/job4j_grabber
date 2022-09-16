package ru.jobj4.grabber;

import ru.jobj4.grabber.model.Post;

import java.util.List;

public interface Store {
    void save(Post post);

    List<Post> getAll();

    Post findById(int id);
}
