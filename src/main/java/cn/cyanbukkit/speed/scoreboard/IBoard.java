package cn.cyanbukkit.speed.scoreboard;

import java.util.Collection;

public interface IBoard {


    void update(String title,Collection<String> lines);

    void delete();
}
