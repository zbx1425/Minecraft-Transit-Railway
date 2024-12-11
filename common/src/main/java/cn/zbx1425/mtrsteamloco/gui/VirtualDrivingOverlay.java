package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.data.TrainVirtualDrive;
import mtr.KeyMappings;
import mtr.MTRClient;
import mtr.data.RailwayData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

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

        String notchText = train.vdNotch == 0 ? "N"
                : (train.vdNotch < 0
                    ? "B" + Math.round(-train.getPercentNotch() * 100)
                    : "P" + Math.round(train.getPercentNotch() * 100));
        double distance = train.nextPlatformRailProgress - train.vdRailProgress;
        String distanceText = (distance > -5 && distance < 5)
                ? Math.round(distance * 100) + " cm"
                : Math.round(distance) + " m";
        Component[] infoLines = {
            Component.literal(train.getDoorValue() > 0 ? "Doors: Open" : "Doors: Closed"),
            Component.literal("Stop Distance: " + distanceText),
            Component.literal("ATP R Speed: " + RailwayData.round(train.atpRedSpeed * 20 * 3.6F, 1) + " km/h"),
            Component.literal("ATP Y Speed: " + RailwayData.round(train.atpYellowSpeed * 20 * 3.6F, 1) + " km/h"),
            Component.literal(""),
            Component.literal("Speed: " + RailwayData.round(train.vdSpeed * 20 * 3.6F, 1) + " km/h"),
            Component.literal("Notch: " + notchText)
        };
        // Draw at bottom left corner
        int lineHeight = 10;
        int x = 20;
        int y = guiGraphics.guiHeight() - 20 - lineHeight * infoLines.length;
        Font font = Minecraft.getInstance().font;
        for (Component line : infoLines) {
            guiGraphics.drawString(font, line, x, y, 0xFFFFFF);
            y += lineHeight;
        }
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
