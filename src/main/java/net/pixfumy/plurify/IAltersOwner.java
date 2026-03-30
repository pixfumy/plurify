package net.pixfumy.plurify;

import java.util.HashMap;
import java.util.List;

public interface IAltersOwner {
    Alter plurify$getCurrentAlter();

    void plurify$setCurrentAlter(Alter currentAlter);

    HashMap<String, Alter> plurify$getAlters();

    void plurify$setAlters(HashMap<String, Alter> alters);

    void plurify$addToAlters(Alter alter);

    void plurify$removeFromAlters(Alter alter);

    void plurify$switchToAlter(Alter alter);
}
