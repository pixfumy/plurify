package net.pixfumy.plurify.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerModelType;
import net.pixfumy.plurify.ISkinOwner;
import net.pixfumy.plurify.MojangSkinChangeHelper;
import net.pixfumy.plurify.SkinsIOHelper;
import net.pixfumy.plurify.networking.CloseAlterSkinScreenC2SPayload;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class AlterSkinScreen extends Screen {
    private final LocalPlayer player;

    private final String alterId;
    private final RemotePlayer previewPlayer;
    private int frame = 0;
    private Button switchClientButton;
    private Button switchGlobalButton;
    private Button doneButton;

    private final Model.Simple wideModel = new Model.Simple(minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER), RenderTypes::entityTranslucent);
    private final Model.Simple slimModel = new Model.Simple(minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), RenderTypes::entityTranslucent);

    private NativeImage skinAsNativeImage;

    public AlterSkinScreen(LocalPlayer player, String alterName, String alterId) {
        super(Component.nullToEmpty(alterName));
        this.player = player;
        this.previewPlayer = new RemotePlayer(minecraft.level, new GameProfile(UUID.randomUUID(), "previewPlayer"));

        this.alterId = alterId;
        this.skinAsNativeImage = ((ISkinOwner) player).plurify$getSkinTextureAsNativeImage();
    }

    @Override
    public void init() {
        this.previewPlayer.setId(-1);
        this.previewPlayer.getEntityData().set(Avatar.DATA_PLAYER_MODE_CUSTOMISATION,
                player.getEntityData().get(Avatar.DATA_PLAYER_MODE_CUSTOMISATION)
        );

        this.switchClientButton = Button.builder(Component.translatable("plurify.gui.alter_skin_screen.switch_client_only"),
                        button -> ((ISkinOwner) player).plurify$setCustomSkin(this.skinAsNativeImage, alterId))
                .pos(width / 2 - 60, 63)
                .size(120, 20)
                .build();

        switchClientButton.active = false;
        this.addRenderableWidget(switchClientButton);

        this.switchGlobalButton = Button.builder(Component.translatable("plurify.gui.alter_skin_screen.switch_globally"), button -> {

            ((ISkinOwner) player).plurify$setCustomSkin(this.skinAsNativeImage, alterId);

            MojangSkinChangeHelper.setSkinTextureGlobal(
                    SkinsIOHelper.getPlayerAlterSkinFile(player, alterId),
                    ((ISkinOwner) player).plurify$getPlayerSkinType()
            );
        })
                .pos(width / 2 - 60, 85)
                .size(120, 20)
                .build();

        switchGlobalButton.active = skinAsNativeImage != null;
        this.addRenderableWidget(switchGlobalButton);

        this.doneButton = Button.builder(Component.translatable("gui.done"),
                        button -> this.onClose())
                .pos(width / 2 - 50, height - 50)
                .size(100, 20)
                .build();

        this.addRenderableWidget(doneButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 20, CommonColors.WHITE);
        graphics.centeredText(this.font, Component.translatable("plurify.gui.alter_skin_screen.drag_and_drop"), this.width / 2, 32, CommonColors.WHITE);

        // Render preview

        graphics.fill(25,  60, 150, height - 50, CommonColors.BLACK);
        graphics.outline(25,  60, 125, height - 110, CommonColors.WHITE);
        graphics.pose().pushMatrix();
        drawEntity(graphics, 25, 60, 150, height/2 + 56, 50, previewPlayer);
        graphics.pose().popMatrix();
        graphics.centeredText(this.font, Component.translatable("plurify.gui.alter_skin_screen.preview_skin"), 87, 45, CommonColors.WHITE);

        // Render current player skin
        graphics.fill(width - 150,  60, width - 25, height - 50, CommonColors.BLACK);
        graphics.outline(width - 150,  60, 125, height - 110, CommonColors.WHITE);
        graphics.pose().pushMatrix();
        drawEntity(graphics,  width - 150,  60,  width - 25,  height - 50, 55, player);
        graphics.pose().popMatrix();
        graphics.centeredText(this.font, Component.translatable("plurify.gui.alter_skin_screen.current_skin"), width - 150 + 62, 45, CommonColors.YELLOW);

        ++frame;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        ClientPlayNetworking.send(new CloseAlterSkinScreenC2SPayload(alterId));
        super.onClose();
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        File droppedFile = paths.getFirst().toFile();
        SkinsIOHelper.writePlayerAlterSkinToFile(player, alterId, droppedFile);
        NativeImage nativeImage = SkinsIOHelper.readPlayerAlterSkinFromFile(player, alterId);
        if (nativeImage != null) {
            this.skinAsNativeImage = nativeImage;

            ((ISkinOwner) previewPlayer).plurify$setCustomSkin(skinAsNativeImage, alterId + "_preview");
            this.switchClientButton.active = true;
            this.switchGlobalButton.active = true;
        }
    }

    public void drawEntity(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int size, AbstractClientPlayer player) {
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        EntityRenderState entityRenderState = getEntityRenderState(player);
        if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
            livingEntityRenderState.bodyRot = this.frame;
            livingEntityRenderState.yRot = 0;
            livingEntityRenderState.xRot = 0;

            livingEntityRenderState.boundingBoxWidth = livingEntityRenderState.boundingBoxWidth / livingEntityRenderState.scale;
            livingEntityRenderState.boundingBoxHeight = livingEntityRenderState.boundingBoxHeight / livingEntityRenderState.scale;
            livingEntityRenderState.scale = 1.0F;
            livingEntityRenderState.walkAnimationPos = frame;
        }

        Vector3f vector3f = new Vector3f(0.0F, entityRenderState.boundingBoxHeight / 2.0F, 0.0F);

        Model.Simple modelSimple = ((ISkinOwner) player).plurify$getPlayerSkinType() == PlayerModelType.WIDE ?
                wideModel : slimModel;

        if (player instanceof RemotePlayer) {
            graphics.skin(modelSimple,
                    player.getSkin().body().texturePath(),
                    size,
                    player.getXRot(),
                    player.getYRot() + frame,
                    -1.0625f,
                    x1,
                    y1,
                    x2,
                    y2
            );
        } else {
            graphics.entity(entityRenderState, size, vector3f, quaternionf, quaternionf, x1, y1, x2, y2);
        }
    }

    private EntityRenderState getEntityRenderState(AbstractClientPlayer player) {
        EntityRenderDispatcher entityRenderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        AvatarRenderer<AbstractClientPlayer> entityRenderer = (AvatarRenderer<AbstractClientPlayer>) entityRenderManager.getRenderer(player);
        EntityRenderState entityRenderState = entityRenderer.createRenderState(player, 1.0F);
        entityRenderState.lightCoords = 15728880;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        return entityRenderState;
    }
}
