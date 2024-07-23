package mtr.forge;

import mtr.mappings.NetworkUtilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CompatPacketRegistry {

    public HashMap<ResourceLocation, CompatPacket> packets = new HashMap<>();
    public HashMap<ResourceLocation, Consumer<FriendlyByteBuf>> packetsS2C = new HashMap<>();
    public HashMap<ResourceLocation, NetworkUtilities.PacketCallback> packetsC2S = new HashMap<>();

    public void registerNetworkReceiverS2C(ResourceLocation resourceLocation, Consumer<FriendlyByteBuf> consumer) {
        packetsS2C.put(resourceLocation, consumer);
    }

    public void registerNetworkReceiverC2S(ResourceLocation resourceLocation, NetworkUtilities.PacketCallback consumer) {
        packetsC2S.put(resourceLocation, consumer);
    }

    public void commit(PayloadRegistrar registrar) {
        for (Map.Entry<ResourceLocation, Consumer<FriendlyByteBuf>> entry : packetsS2C.entrySet()) {
            CompatPacket packet = packets.computeIfAbsent(entry.getKey(), CompatPacket::new);
            registrar.playToClient(packet.TYPE, packet.STREAM_CODEC,
                    (arg, iPayloadContext) -> entry.getValue().accept(arg.buffer));
        }
        for (Map.Entry<ResourceLocation, NetworkUtilities.PacketCallback> entry : packetsC2S.entrySet()) {
            CompatPacket packet = packets.computeIfAbsent(entry.getKey(), CompatPacket::new);
            registrar.playToClient(packet.TYPE, packet.STREAM_CODEC,
                    (arg, iPayloadContext) -> entry.getValue().packetCallback(
                            iPayloadContext.player().getServer(), (ServerPlayer)iPayloadContext.player(), arg.buffer));
        }
    }

    public void sendS2C(ServerPlayer player, ResourceLocation id, FriendlyByteBuf payload) {
        CompatPacket packet = packets.computeIfAbsent(id, CompatPacket::new);
        PacketDistributor.sendToPlayer(player, packet.new Payload(payload));
    }

    public void sendC2S(ResourceLocation id, FriendlyByteBuf payload) {
        CompatPacket packet = packets.computeIfAbsent(id, CompatPacket::new);
        PacketDistributor.sendToServer(packet.new Payload(payload));
    }
}
