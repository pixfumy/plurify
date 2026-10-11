package net.pixfumy.plurify;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.pixfumy.plurify.gui.MainAltersGui;

public class AltersCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = (LiteralArgumentBuilder) Commands
                .literal("alters")
                .executes(AltersCommand::execute);
        dispatcher.register(literalArgumentBuilder);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        SimpleGui altersGui = new MainAltersGui(context.getSource().getPlayer());
        altersGui.open();
        return 0;
    }
}
