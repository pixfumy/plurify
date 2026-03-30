package net.pixfumy.plurify;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IAltersOwner {
    List<Alter> getAlters();

    void setAlters(List<Alter> alters);
}
