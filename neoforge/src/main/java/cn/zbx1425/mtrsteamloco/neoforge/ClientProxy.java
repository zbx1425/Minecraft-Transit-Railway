package cn.zbx1425.mtrsteamloco.neoforge;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.NTEClientCommand;
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
            NTEClientCommand.register(event.getDispatcher(), Commands::literal);
        }
    }
}