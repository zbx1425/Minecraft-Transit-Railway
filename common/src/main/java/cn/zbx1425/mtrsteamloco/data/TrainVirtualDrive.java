package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.network.PacketVirtualDrive;
import io.netty.buffer.Unpooled;
import mtr.client.ClientData;
import mtr.data.TrainClient;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Random;

public class TrainVirtualDrive extends TrainClient {

    public TrainVirtualDrive(TrainClient trainClient) {
        super(Util.make(() -> {
            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            trainClient.writePacket(packet);
            int prevWriterIndex = packet.writerIndex();
            packet.writerIndex(0);
            packet.writeLong(new Random().nextLong()); // Get a new ID
            packet.writerIndex(prevWriterIndex);
            packet.resetReaderIndex();
            return packet;
        }));
    }

    public double manualRailProgress;
    public float manualSpeed;

    @Override
    public void simulateTrain(Level world, float ticksElapsed, SpeedCallback speedCallback, AnnouncementCallback announcementCallback, AnnouncementCallback lightRailAnnouncementCallback) {
        isCurrentlyManual = true;
        manualNotch = 0;
        nextStoppingIndex = path.size() - 1;
        super.simulateTrain(world, ticksElapsed, speedCallback, announcementCallback, lightRailAnnouncementCallback);

        manualRailProgress += 1 * ticksElapsed;
        if (manualRailProgress >= distances.getLast() - (railLength - trainCars * spacing) / 2) {
            stopDriving();
        }

        railProgress = manualRailProgress;
        speed = manualSpeed;
    }

    public static TrainVirtualDrive activeTrain;

    public static void startDrivingRidingTrain() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        for (TrainClient train : ClientData.TRAINS) {
            if (train.isPlayerRiding(player)) {
                stopDriving();
                activeTrain = new TrainVirtualDrive(train);
                activeTrain.manualRailProgress = train.getRailProgress();
                activeTrain.manualSpeed = train.getSpeed();
                activeTrain.vehicleRidingClient.startRiding(
                        player.getUUID(),
                        train.vehicleRidingClient.getPercentageX(player.getUUID()),
                        train.vehicleRidingClient.getPercentageZ(player.getUUID())
                );
                ClientData.TRAINS.add(activeTrain);
                PacketVirtualDrive.sendVirtualDriveC2S(true);
                train.vehicleRidingClient.stopRiding(player.getUUID());
                break;
            }
        }
    }

    public static void stopDriving() {
        if (activeTrain != null) {
            PacketVirtualDrive.sendVirtualDriveC2S(false);
            activeTrain = null;
            // Will be removed from ClientData later
        }
    }
}
