package cn.zbx1425.mtrsteamloco.neoforge;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.NTEClientCommand;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.train.SteamSmokeParticle;
import mtr.mappings.Text;
import mtr.screen.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ClientProxy {

    public static void initClient() {

    }

    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (modContainer, arg) -> new ConfigScreen());
    }

    public static class ModEventBusListener {

        @SubscribeEvent
        public static void onClientSetupEvent(FMLClientSetupEvent event) {
            MainClient.init();
        }

        @SubscribeEvent
        public static void onRegistryParticleFactory(RegisterParticleProvidersEvent event) {
            Minecraft.getInstance().particleEngine.register(Main.PARTICLE_STEAM_SMOKE, SteamSmokeParticle.Provider::new);
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