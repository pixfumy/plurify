package net.pixfumy.plurify;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface IAltersOwner {
    Alter plurify$getCurrentAlter();

    void plurify$setCurrentAlter(Alter currentAlter);

    HashMap<UUID, Alter> plurify$getAlters();

    void plurify$setAlters(HashMap<UUID, Alter> alters);

    void plurify$addToAlters(Alter alter);

    void plurify$removeFromAlters(Alter alter);

    void plurify$switchToAlter(Alter alter);

    boolean plurify$isClientLoaded();

    void plurify$setClientLoaded(boolean isClientLoaded);
}
