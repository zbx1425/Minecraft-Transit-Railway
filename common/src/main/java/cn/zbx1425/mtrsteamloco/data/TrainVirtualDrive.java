package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.network.PacketVirtualDrive;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import mtr.client.ClientData;
import mtr.data.TrainClient;
import mtr.path.PathData;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
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
        for (PathData path : this.path) {
            vdMaxSpeed = Math.max(vdMaxSpeed, path.rail.railType.maxBlocksPerTick);
        }
        vdMaxSpeed += 20 / 20f / 3.6f;
    }

    public double vdRailProgress;
    public float vdSpeed;
    public int vdNotch = 0;
    public float vdMaxSpeed;

    public int nextPlatformIndex;
    public double nextPlatformRailProgress;
    public float atpYellowSpeed;
    public float atpRedSpeed;
    public float atpTargetSpeed;
    public double atpTargetDistance;
    public boolean atpEmergencyBrake;
    private int doorOpenedAtPlatformIndex = 0;

    public LongArrayList railAheadLookup = new LongArrayList();

    public int powerNotches = 5;
    public int brakeNotches = 7;
    public float yellowSpeedBrakeRatio = 0.7f;
    public float redSpeedBrakeRatio = 0.9f;

    @Override
    public void simulateTrain(Level world, float ticksElapsed, SpeedCallback speedCallback, AnnouncementCallback announcementCallback, AnnouncementCallback lightRailAnnouncementCallback) {
        isCurrentlyManual = true;
        manualNotch = 0;
        nextStoppingIndex = path.size() - 1;
        super.simulateTrain(world, ticksElapsed, speedCallback, announcementCallback, lightRailAnnouncementCallback);

        float accelDueToFriction = (-0.1f / 400 / 3.6f) * ticksElapsed;
        if (atpEmergencyBrake) {
            vdSpeed = Mth.clamp(vdSpeed - 1.1f * accelerationConstant * ticksElapsed + accelDueToFriction, 0, vdMaxSpeed);
            if (vdSpeed <= 0 && vdNotch < 0) atpEmergencyBrake = false;
        } else {
            if (vdNotch < 0) {
                vdSpeed = Mth.clamp(vdSpeed + getPercentNotch() * (accelerationConstant / yellowSpeedBrakeRatio) * ticksElapsed + accelDueToFriction, 0, vdMaxSpeed);
            } else if (vdNotch > 0) {
                vdSpeed = Mth.clamp(vdSpeed + getPercentNotch() * accelerationConstant * ticksElapsed + accelDueToFriction, 0, vdMaxSpeed);
            } else {
                // Simulate friction
                vdSpeed = Mth.clamp(vdSpeed + accelDueToFriction, 0, vdMaxSpeed);
            }
        }
        if (doorValue > 0 || doorTarget) {
            vdSpeed = 0;
        }
        vdRailProgress += vdSpeed * ticksElapsed;
        if (vdRailProgress >= distances.getLast() - (railLength - trainCars * spacing) / 2) {
            stopDriving();
        }

        // Update next platform
        if (distances.get(nextPlatformIndex) < vdRailProgress - 10) {
            for (int i = nextPlatformIndex; i < path.size(); i++) {
                if (path.get(i).dwellTime > 0) {
                    if (distances.get(i) >= vdRailProgress - 10) {
                        nextPlatformIndex = i;
                        nextPlatformRailProgress = distances.get(i);
                        break;
                    }
                }
                if (i == path.size() - 1) {
                    nextPlatformIndex = i;
                    nextPlatformRailProgress = distances.get(i);
                }
            }
        }

        // Update rail segments ahead
        railAheadLookup.clear();
        atpYellowSpeed = vdMaxSpeed;
        atpRedSpeed = vdMaxSpeed;
        atpTargetSpeed = vdMaxSpeed;
        atpTargetDistance = distances.getLast();
        double speedLimitLiftDistance = distances.getLast();
        float patternSpeedAtLiftDistance = vdMaxSpeed;
        double lookAheadDistance = Math.max(300, Math.pow(vdSpeed, 2) / (2 * accelerationConstant));
        for (int i = getIndex(vdRailProgress - spacing * trainCars, true);
            i < path.size() && distances.get(i) < vdRailProgress + lookAheadDistance; i++) {
            PathData pathSeg = path.get(i);
            railAheadLookup.add(pathSeg.startingPos.asLong());
            if (i > 0 && distances.get(i - 1) < vdRailProgress && distances.get(i) > vdRailProgress - spacing * trainCars) {
                // Persisting speed limit
                atpYellowSpeed = Math.min(atpYellowSpeed, pathSeg.rail.railType.maxBlocksPerTick);
                atpRedSpeed = Math.min(atpRedSpeed, pathSeg.rail.railType.maxBlocksPerTick + 5 / 3.6f / 20);
                speedLimitLiftDistance = distances.get(i) + spacing * trainCars;
            }
            if (pathSeg.dwellTime > 0 && i != doorOpenedAtPlatformIndex) {
                // Stop point pattern
                if (distances.get(i) > vdRailProgress) {
                    float patternSpeed = (float)Math.sqrt(2 * accelerationConstant * (distances.get(i) - vdRailProgress));
                    if (patternSpeed < atpYellowSpeed) {
                        atpYellowSpeed = patternSpeed;
                        atpTargetSpeed = 0;
                        atpTargetDistance = distances.get(i);
                    }
                } else if (distances.get(i) > vdRailProgress - 10) {
                    atpYellowSpeed = 0;
                }
                if (distances.get(i) > speedLimitLiftDistance) {
                    float patternSpeed = (float)Math.sqrt(2 * accelerationConstant * (distances.get(i) - speedLimitLiftDistance));
                    patternSpeedAtLiftDistance = Math.min(patternSpeedAtLiftDistance, patternSpeed);
                } else if (distances.get(i) > speedLimitLiftDistance - 10) {
                    patternSpeedAtLiftDistance = 0;
                }
                if (Math.abs(distances.get(i) - vdRailProgress) < 5 && (doorTarget || doorValue > 0)) {
                    doorOpenedAtPlatformIndex = i;
                }
            } else {
                if (i > 0 && distances.get(i - 1) > vdRailProgress) {
                    // Decelerate to start of speed limit
                    float yellowPatternSpeed = (float) Math.sqrt(2 * accelerationConstant * (distances.get(i - 1) - vdRailProgress)
                                    + Math.pow(pathSeg.rail.railType.maxBlocksPerTick, 2));
                    if (yellowPatternSpeed < atpYellowSpeed) {
                        atpYellowSpeed = yellowPatternSpeed;
                        atpTargetSpeed = pathSeg.rail.railType.maxBlocksPerTick;
                        atpTargetDistance = distances.get(i - 1);
                    }
                    atpRedSpeed = Math.min(atpRedSpeed,
                            (float)Math.sqrt(2 * accelerationConstant / yellowSpeedBrakeRatio * redSpeedBrakeRatio * (distances.get(i - 1) - vdRailProgress)
                                    + Math.pow(pathSeg.rail.railType.maxBlocksPerTick + 5 / 3.6f / 20, 2)));
                }
                if (i > 0 && distances.get(i - 1) > speedLimitLiftDistance) {
                    float patternSpeed = (float) Math.sqrt(2 * accelerationConstant * (distances.get(i - 1) - speedLimitLiftDistance)
                            + Math.pow(pathSeg.rail.railType.maxBlocksPerTick, 2));
                    patternSpeedAtLiftDistance = Math.min(patternSpeedAtLiftDistance, patternSpeed);
                }
            }
        }
//        if (patternSpeedAtLiftDistance < atpTargetDistance) {
//            atpTargetSpeed = patternSpeedAtLiftDistance;
//            atpTargetDistance = speedLimitLiftDistance;
//        }
        if (atpTargetDistance == distances.getLast()) {
            atpTargetSpeed = 0;
        }
        if (doorValue > 0 || doorTarget) {
            atpYellowSpeed = 0;
            atpRedSpeed = 0;
            atpTargetSpeed = -1;
        }
        if (vdSpeed > atpRedSpeed) {
            atpEmergencyBrake = true;
        }

        railProgress = vdRailProgress;
        speed = vdSpeed;
    }

    public static TrainVirtualDrive activeTrain;

    public static void startDrivingRidingTrain() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        for (TrainClient train : ClientData.TRAINS) {
            if (train.isPlayerRiding(player)) {
                stopDriving();
                activeTrain = new TrainVirtualDrive(train);
                activeTrain.vdRailProgress = train.getRailProgress();
                activeTrain.vdSpeed = train.getSpeed();
                activeTrain.vehicleRidingClient.startRiding(
                        player.getUUID(),
                        train.vehicleRidingClient.getPercentageX(player.getUUID()),
                        train.vehicleRidingClient.getPercentageZ(player.getUUID())
                );
                PacketVirtualDrive.sendVirtualDriveC2S(true);
                train.vehicleRidingClient.stopRiding(player.getUUID());
                Minecraft.getInstance().execute(() -> {
                    ClientData.TRAINS.add(activeTrain);
                });
                break;
            }
        }
    }

    public static void stopDriving() {
        if (activeTrain != null) {
            PacketVirtualDrive.sendVirtualDriveC2S(false);
            activeTrain.isRemoved = true;
            Minecraft.getInstance().execute(() -> {
                ClientData.TRAINS.remove(activeTrain);
            });
            activeTrain = null;
        }
    }

    public float getPercentNotch() {
        if (vdNotch < 0) {
            return (float) vdNotch / brakeNotches;
        } else {
            return (float) vdNotch / powerNotches;
        }
    }

    @Override
    public int getTotalDwellTicks() {
        return 114514;
    }
}
