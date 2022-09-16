package ru.jobj4.grabber;

import ru.jobj4.grabber.model.Post;

import java.util.List;

public interface Parse {

    List<Post> list(String link);
}
