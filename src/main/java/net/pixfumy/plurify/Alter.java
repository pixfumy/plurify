package net.pixfumy.plurify;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class Alter {
    private Text name;
    private PlayerInventory playerInventory;

    public Alter(Text name, PlayerInventory playerInventory) {
        this.name = name;
        this.playerInventory = playerInventory;
    }

    public Text getName() {
        return this.name;
    }

    public void setName(Text name) {
        this.name = name;
    }

    public PlayerInventory getPlayerInventory() {
        return this.playerInventory;
    }

    public void setPlayerInventory(PlayerInventory playerInventory) {
        this.playerInventory = playerInventory;
    }

}
