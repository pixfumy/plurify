package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.AltersIOHelper;
import net.pixfumy.plurify.IAltersOwner;

public class NameAlterGui extends SignGui {
    private final Alter alter;
    private final SimpleGui parent;

    public NameAlterGui(ServerPlayerEntity player, Alter alter, SimpleGui parent) {
        super(player);
        this.alter = alter;
        this.parent = parent;
    }

    @Override
    public void close(boolean alreadyClosed) {
        super.close(alreadyClosed);
        ((IAltersOwner) player).plurify$removeFromAlters(alter);
        alter.setName(this.getLine(0));
        ((IAltersOwner) player).plurify$addToAlters(alter);
        AltersIOHelper.writePlayerAlterEntityDataToFile(player, alter);
        this.parent.open();
    }
}
