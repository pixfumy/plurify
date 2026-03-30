package net.pixfumy.plurify.mixin;

import net.pixfumy.plurify.IAltersOwner;
import net.pixfumy.plurify.Alter;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public class PlayerAltersMixin  implements IAltersOwner {
    private List<Alter> alters;

    public List<Alter> getAlters() {
        return alters;
    }

    public void setAlters(List<Alter> alters) {
        this.alters = alters;
    }
}