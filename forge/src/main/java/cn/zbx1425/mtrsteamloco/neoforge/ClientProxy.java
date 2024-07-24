package cn.zbx1425.mtrsteamloco.neoforge;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.gui.ConfigScreen;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import mtr.mappings.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class ClientProxy {

    public static void initClient() {

    }


    public static class ModEventBusListener {

        @SubscribeEvent
        public static void onClientSetupEvent(FMLClientSetupEvent event) {
            MainClient.init();
        }
    }

    public static class ForgeEventBusListener {

        @SubscribeEvent
        public static void onDebugOverlay(CustomizeGuiOverlayEvent.DebugText event) {
//            if (Minecraft.getInstance().options.renderDebug) {
//                event.getLeft().add(
//                        "[NTE] Calls: " + MainClient.drawContext.drawCallCount
//                                + ", Batches: " + MainClient.drawContext.batchCount
//                                + ", Faces: " + (MainClient.drawContext.singleFaceCount + MainClient.drawContext.instancedFaceCount)
//                );
//            }
        }

        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(
                    Commands.literal("mtrnte")
                            .then(Commands.literal("config")
                                    .executes(context -> {
                                        Minecraft.getInstance().tell(() -> {
                                            Minecraft.getInstance().setScreen(ConfigScreen.createScreen(Minecraft.getInstance().screen));
                                        });
                                        return 1;
                                    }))
                            .then(Commands.literal("hideriding")
                                    .executes(context -> {
                                        ClientConfig.hideRidingTrain = !ClientConfig.hideRidingTrain;
                                        return 1;
                                    }))
                            .then(Commands.literal("stat")
                                    .executes(context -> {
                                        Minecraft.getInstance().tell(() -> {
                                            String info = RenderUtil.getRenderStatusMessage();
                                            Minecraft.getInstance().player.sendSystemMessage(Text.literal(info));
                                        });
                                        return 1;
                                    }))
            );
        }
    }
}