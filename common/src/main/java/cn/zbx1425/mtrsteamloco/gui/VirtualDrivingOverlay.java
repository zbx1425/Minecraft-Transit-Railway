package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.data.TrainVirtualDrive;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import mtr.KeyMappings;
import mtr.MTRClient;
import mtr.data.RailwayData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class VirtualDrivingOverlay {

    private static float keyCooldown = 0;
    private static KeyMapping lastKey = null;

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaT) {
        if (TrainVirtualDrive.activeTrain == null) return;
        TrainVirtualDrive train = TrainVirtualDrive.activeTrain;

        anyKeyDown = false;
        if (isKeyDownWithRepeat(KeyMappings.TRAIN_BRAKE, deltaT)) {
            train.vdNotch = Math.max(train.vdNotch - 1, -train.brakeNotches);
        } else if (isKeyDownWithRepeat(KeyMappings.TRAIN_NEUTRAL, deltaT)) {
            train.vdNotch -= (int) Math.signum(train.vdNotch);
        } else if (isKeyDownWithRepeat(KeyMappings.TRAIN_ACCELERATE, deltaT)) {
            train.vdNotch = Math.min(train.vdNotch + 1, train.powerNotches);
        }
        if (!anyKeyDown) {
            lastKey = null;
            keyCooldown = 0;
        }
        if (KeyMappings.TRAIN_TOGGLE_DOORS.consumeClick()) {
            train.toggleDoors();
        }

        // HMI painting
        final int GAUGE_SIZE = 96;
        final int PADDING = 20;
        ResourceLocation hmiTex = Main.id("textures/gui/drive_hmi.png");
        RenderSystem.setShaderTexture(0, hmiTex);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        // Gauge back
        blit(guiGraphics, bufferBuilder,
                PADDING + 1, guiGraphics.guiHeight() - GAUGE_SIZE - PADDING + 1,
                GAUGE_SIZE / 4, GAUGE_SIZE, 0,
                0.125f, 0.5f, 0.125f, 0.5f, 0x88222222);
        blit(guiGraphics, bufferBuilder,
                PADDING, guiGraphics.guiHeight() - GAUGE_SIZE - PADDING,
                GAUGE_SIZE / 4, GAUGE_SIZE, 0,
                0.125f, 0.5f, 0.125f, 0.5f, 0xFFFFFFFF);
        blit(guiGraphics, bufferBuilder,
                PADDING + GAUGE_SIZE / 4, guiGraphics.guiHeight() - GAUGE_SIZE - PADDING,
                GAUGE_SIZE, GAUGE_SIZE, 0,
                0f, 0f, 0.5f, 0.5f, 0x99222222);
        blit(guiGraphics, bufferBuilder,
                PADDING + GAUGE_SIZE / 4, guiGraphics.guiHeight() - GAUGE_SIZE - PADDING,
                GAUGE_SIZE, GAUGE_SIZE, 0,
                0.5f, 0f, 0.5f, 0.5f, 0xffffffff);
        // Speed needle
        guiGraphics.pose().pushPose();
        guiGraphics.pose().rotateAround(
                Axis.ZP.rotationDegrees(-140 + RailwayData.round(train.getSpeed() * 3.6f * 20, 1) / 100 * 280),
                PADDING + GAUGE_SIZE / 4f + GAUGE_SIZE / 2f, guiGraphics.guiHeight() - GAUGE_SIZE / 2f - PADDING, 0
        );
        int needleXOff = PADDING + GAUGE_SIZE / 4 + (GAUGE_SIZE * 3 / 8);
        blit(guiGraphics, bufferBuilder,
                needleXOff, guiGraphics.guiHeight() - GAUGE_SIZE - PADDING,
                GAUGE_SIZE / 4, GAUGE_SIZE, 0,
                0f, 0.5f, 0.125f, 0.5f, 0xffffffff);
        guiGraphics.pose().popPose();
        // Yellow ATP Speed needle
        guiGraphics.pose().pushPose();
        guiGraphics.pose().rotateAround(
                Axis.ZP.rotationDegrees(-140 + RailwayData.round(train.atpYellowSpeed * 3.6f * 20, 1) / 100 * 280),
                PADDING + GAUGE_SIZE / 4f + GAUGE_SIZE / 2f, guiGraphics.guiHeight() - GAUGE_SIZE / 2f - PADDING, 0
        );
        blit(guiGraphics, bufferBuilder,
                needleXOff, guiGraphics.guiHeight() - GAUGE_SIZE - PADDING,
                GAUGE_SIZE / 4, GAUGE_SIZE, 0,
                0.375f, 0.5f, 0.125f, 0.5f, 0xffffffff);
        guiGraphics.pose().popPose();
        // Red ATP Speed needle
        guiGraphics.pose().pushPose();
        guiGraphics.pose().rotateAround(
                Axis.ZP.rotationDegrees(-140 + RailwayData.round(train.atpRedSpeed * 3.6f * 20, 1) / 100 * 280),
                PADDING + GAUGE_SIZE / 4f + GAUGE_SIZE / 2f, guiGraphics.guiHeight() - GAUGE_SIZE / 2f - PADDING, 0
        );
        blit(guiGraphics, bufferBuilder,
                needleXOff, guiGraphics.guiHeight() - GAUGE_SIZE - PADDING,
                GAUGE_SIZE / 4, GAUGE_SIZE, 0,
                0.25f, 0.5f, 0.125f, 0.5f, 0xffffffff);
        guiGraphics.pose().popPose();
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
        // Speed Text
        Font font = Minecraft.getInstance().font;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(PADDING + GAUGE_SIZE / 4f + GAUGE_SIZE / 2f, guiGraphics.guiHeight() - GAUGE_SIZE / 2f - PADDING, 0);
        float speedTextScale = (50 / 730f * GAUGE_SIZE) / font.lineHeight;
        guiGraphics.pose().scale(speedTextScale, speedTextScale, 1);
        int speedKph = (int)Math.ceil(train.getSpeed() * 20 * 3.6F);
        if (speedKph >= 100) {
            guiGraphics.drawCenteredString(font, Integer.toString(speedKph), 0, -font.lineHeight / 2, 0xFFFFFFFF);
        } else if (speedKph >= 10) {
            guiGraphics.drawString(font, Integer.toString(speedKph % 10), 0, -font.lineHeight / 2, 0xFFFFFFFF);
            guiGraphics.drawString(font, Integer.toString(speedKph / 10), -font.width(Integer.toString(speedKph / 10)), -font.lineHeight / 2, 0xFFFFFFFF);
        } else {
            guiGraphics.drawString(font, Integer.toString(speedKph), 0, -font.lineHeight / 2, 0xFFFFFFFF);
        }
        guiGraphics.pose().popPose();

        String notchText = train.vdNotch == 0 ? "N"
                : (train.vdNotch < 0
                    ? "B" + Math.round(-train.getPercentNotch() * 100)
                    : "P" + Math.round(train.getPercentNotch() * 100));
        double distance = train.nextPlatformRailProgress - train.vdRailProgress;
        String distanceText = (distance > -5 && distance < 5)
                ? Math.round(distance * 100) + " cm"
                : Math.round(distance) + " m";
        Component[] infoLines = {
//            Component.literal(train.getDoorValue() > 0 ? "Doors: Open" : "Doors: Closed"),
//            Component.literal("Stop Distance: " + distanceText),
//            Component.literal("ATP R Speed: " + RailwayData.round(train.atpRedSpeed * 20 * 3.6F, 1) + " km/h"),
//            Component.literal("ATP Y Speed: " + RailwayData.round(train.atpYellowSpeed * 20 * 3.6F, 1) + " km/h"),
//            Component.literal(""),
//            Component.literal("Speed: " + RailwayData.round(train.vdSpeed * 20 * 3.6F, 1) + " km/h"),
            Component.literal(notchText)
        };
        // Draw at bottom left corner
        int lineHeight = 10;
        int x = 20;
        int y = guiGraphics.guiHeight() - 20 - lineHeight * infoLines.length;
        for (Component line : infoLines) {
            guiGraphics.drawString(font, line, x, y, 0xFFFFFF);
            y += lineHeight;
        }
    }

    private static void blit(GuiGraphics guiGraphics, BufferBuilder bufferBuilder, int x1, int y1, int width, int height, int blitOffset, float minU, float minV, float deltaU, float deltaV, int color) {
            Matrix4f matrix4f = guiGraphics.pose().last().pose();
            bufferBuilder.addVertex(matrix4f, (float)x1, (float)y1, (float)blitOffset).setUv(minU, minV).setColor(color);
            bufferBuilder.addVertex(matrix4f, (float)x1, (float)(y1 + height), (float)blitOffset).setUv(minU, minV + deltaV).setColor(color);
            bufferBuilder.addVertex(matrix4f, (float)(x1 + width), (float)(y1 + height), (float)blitOffset).setUv(minU + deltaU, minV + deltaV).setColor(color);
            bufferBuilder.addVertex(matrix4f, (float)(x1 + width), (float)y1, (float)blitOffset).setUv(minU + deltaU, minV).setColor(color);
    }

    private static final float KEY_DELAY_BEFORE_REPEAT = 6f;
    private static final float KEY_REPEAT_INTERVAL = 3f;
    private static float lastProcessedTick;
    private static boolean anyKeyDown;

    private static boolean isKeyDownWithRepeat(KeyMapping key, DeltaTracker deltaT) {
        if (key.isDown()) {
            anyKeyDown = true;
            if (lastKey != key) {
                lastKey = key;
                keyCooldown = KEY_DELAY_BEFORE_REPEAT;
                return true;
            }
            if (MTRClient.getGameTick() != lastProcessedTick) {
                keyCooldown -= deltaT.getGameTimeDeltaPartialTick(false);
                lastProcessedTick = MTRClient.getGameTick();
            }
            if (keyCooldown <= 0) {
                keyCooldown = KEY_REPEAT_INTERVAL;
                return true;
            }
        }
        return false;
    }
}
