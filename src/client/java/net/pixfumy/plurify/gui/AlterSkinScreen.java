package net.pixfumy.plurify.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.pixfumy.plurify.ISkinOwner;
import net.pixfumy.plurify.MojangSkinChangeHelper;
import net.pixfumy.plurify.SkinsIOHelper;
import net.pixfumy.plurify.mixin.PlayerSkinTextureDownloaderAccess;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class AlterSkinScreen extends Screen {
    private final ClientPlayerEntity player;
    private final String alterName;
    private final OtherClientPlayerEntity previewPlayer;
    private int frame = 0;
    private ButtonWidget switchClientButton;
    private ButtonWidget switchGlobalButton;

    private NativeImage skinAsNativeImage;
    private File skinFile;
    private PlayerSkinType playerSkinType;

    public AlterSkinScreen(ClientPlayerEntity player, String alterName) {
        super(Text.of(alterName));
        this.player = player;
        this.previewPlayer = new OtherClientPlayerEntity(client.world, new GameProfile(UUID.randomUUID(), "previewPlayer"));
        this.alterName = alterName;
    }

    @Override
    public void init() {
        this.switchClientButton = ButtonWidget.builder(Text.literal("Switch - Client Only"),
                        button -> ((ISkinOwner) player).plurify$setCustomSkin(this.skinAsNativeImage.applyToCopy(i -> i), playerSkinType, alterName))
                .position(width / 2 - 60, 63)
                .size(120, 20)
                .build();

        switchClientButton.active = false;
        this.addDrawableChild(switchClientButton);

        this.switchGlobalButton = ButtonWidget.builder(Text.literal("Switch Globally"), button -> {
            ((ISkinOwner) player).plurify$setCustomSkin(this.skinAsNativeImage.applyToCopy(i -> i), playerSkinType, alterName);
            MojangSkinChangeHelper.setSkinTextureGlobal(skinFile, playerSkinType);
        })
                .position(width / 2 - 60, 85)
                .size(120, 20)
                .build();

        switchGlobalButton.active = false;
        this.addDrawableChild(switchGlobalButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, Colors.WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Drag and drop skin .png here to preview"), this.width / 2, 32, Colors.WHITE);

        // Render preview
        context.fill(25,  60, 150, height - 50, Colors.BLACK);
        context.drawStrokedRectangle(25,  60, 125, height - 110, Colors.WHITE);
        drawEntity(context, 25, 60, 150, height/2 + 56, 50, previewPlayer);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Preview"), 87, 45, Colors.WHITE);

        // Render current player skin
        context.fill(width - 150,  60, width - 25, height - 50, Colors.BLACK);
        context.drawStrokedRectangle(width - 150,  60, 125, height - 110, Colors.WHITE);
        drawEntity(context,  width - 150,  60,  width - 25,  height - 50, 55, player);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Currently Selected"), width - 150 + 62, 45, Colors.YELLOW);
        ++frame;
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void onFilesDropped(List<Path> paths) {
        File droppedFile = paths.getFirst().toFile();
        SkinsIOHelper.writePlayerAlterSkinToFile(player, alterName, droppedFile);
        NativeImage nativeImage = SkinsIOHelper.readPlayerAlterSkinFromFile(player, alterName);
        if (nativeImage != null) {
            this.skinAsNativeImage = nativeImage;

            // remaps 32 x 64 image to 64 x 64, and fills in the unused spaces with black pixels
            PlayerSkinTextureDownloaderAccess.invokeRemapTexture(skinAsNativeImage, this.alterName);

            this.skinFile = SkinsIOHelper.getPlayerAlterSkinFile(player, alterName);
            this.playerSkinType = SkinsIOHelper.determinePlayerSkinType(skinAsNativeImage);

            ((ISkinOwner) previewPlayer).plurify$setCustomSkin(skinAsNativeImage.applyToCopy(i -> i), playerSkinType, alterName + "preview");
            this.switchClientButton.active = true;
            this.switchGlobalButton.active = true;
        }
    }

    public void drawEntity(DrawContext context, int x1, int y1, int x2, int y2, int size, AbstractClientPlayerEntity player) {
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        EntityRenderState entityRenderState = drawEntity(player);
        if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
            livingEntityRenderState.bodyYaw = this.frame;
            livingEntityRenderState.relativeHeadYaw = 0;
            livingEntityRenderState.pitch = 0;

            livingEntityRenderState.width = livingEntityRenderState.width / livingEntityRenderState.baseScale;
            livingEntityRenderState.height = livingEntityRenderState.height / livingEntityRenderState.baseScale;
            livingEntityRenderState.baseScale = 1.0F;
            livingEntityRenderState.limbSwingAnimationProgress = frame;
        }

        Vector3f vector3f = new Vector3f(0.0F, entityRenderState.height / 2.0F, 0.0F);

        if (player instanceof OtherClientPlayerEntity) {
            context.addPlayerSkin(client.getEntityRenderDispatcher().getPlayerRenderer(player).getModel(),
                    player.getSkin().body().texturePath(),
                    size,
                    0, entityRenderState.height / 2.0F + frame, -1.0625F, x1, y1, x2, y2
            );
        } else {
            context.addEntity(entityRenderState, size, vector3f, quaternionf, quaternionf, x1, y1, x2, y2);
        }
    }

    private EntityRenderState drawEntity(AbstractClientPlayerEntity player) {
        EntityRenderManager entityRenderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();
        PlayerEntityRenderer<AbstractClientPlayerEntity> entityRenderer = entityRenderManager.getPlayerRenderer(player);
        EntityRenderState entityRenderState = entityRenderer.getAndUpdateRenderState(player, 1.0F);
        entityRenderState.light = 15728880;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        return entityRenderState;
    }
}
