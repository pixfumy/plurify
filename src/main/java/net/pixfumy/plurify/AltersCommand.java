package net.pixfumy.plurify;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class AltersCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = (LiteralArgumentBuilder) CommandManager
                .literal("alters")
                .executes(AltersCommand::execute);
        dispatcher.register(literalArgumentBuilder);
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        SimpleGui altersGui = new MainAltersGui(context.getSource().getPlayer());
        altersGui.open();
        return 0;
    }
}
