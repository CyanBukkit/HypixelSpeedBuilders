package cn.cyanbukkit.murdermystery.scoreboard;

import java.util.Collection;

public interface IBoard {


    void update(String title,Collection<String> lines);

    void delete();
}
