package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.data.ScriptedCustomTrains;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.data.RailModelRegistry;
import cn.zbx1425.mtrsteamloco.mixin.TrainClientAccessor;
import cn.zbx1425.mtrsteamloco.render.scripting.AbstractScriptContext;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptContextManager;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptResourceUtil;
import cn.zbx1425.mtrsteamloco.render.scripting.eyecandy.EyeCandyScriptContext;
import cn.zbx1425.mtrsteamloco.render.train.NoopTrainRenderer;
import cn.zbx1425.mtrsteamloco.sound.NoopTrainSound;
import mtr.client.ClientData;
import mtr.client.TrainClientRegistry;
import mtr.client.TrainProperties;
import mtr.data.TransportMode;
import mtr.mappings.Text;
import mtr.render.TrainRendererBase;
import mtr.sound.TrainSoundBase;
import mtr.sound.bve.BveTrainSoundConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;

public class CustomResources {

    public static void reset(ResourceManager resourceManager) {
        try {
            MainClient.drawScheduler.reloadShaders(resourceManager);
        } catch (IOException e) {
            Main.LOGGER.error("Failed loading shader:", e);
        }
        MainClient.modelManager.clear();
        MainClient.atlasManager.clear();
    }

    public static void init(ResourceManager resourceManager) {
        Main.LOGGER.info("MTR-NTE has started loading custom resources.");

        EyeCandyRegistry.reload(resourceManager);
        RailModelRegistry.reload(resourceManager);

        ScriptHolder.resetRunner();
        ScriptResourceUtil.init(resourceManager);
        ScriptedCustomTrains.init(resourceManager);

        Main.LOGGER.info("MTR-NTE: "
                + "Uploaded Models: " + MainClient.modelManager.uploadedVertArrays.size()
                + " (" + MainClient.modelManager.vaoCount + " VAOs, "
                + MainClient.modelManager.vboCount + " VBOs)"
        );
    }

    public static void resetComponents() {
        // Notify TrainLoopingSoundInstance to stop
        ClientData.TRAINS.forEach(train -> train.isRemoved = true);
        Minecraft.getInstance().getSoundManager().tick(false);

        // Assign new ScriptContext for BlockEntityEyeCandy
        // Train have it done with train.isRemoved and new TrainRendererBase
        for (AbstractScriptContext scriptCtx : ScriptContextManager.livingContexts.keySet()) {
            if (scriptCtx instanceof EyeCandyScriptContext eyeScriptCtx) {
                eyeScriptCtx.disposeForReload = true;
                eyeScriptCtx.entity.scriptContext = new EyeCandyScriptContext(eyeScriptCtx.entity);
            }
        }

        ScriptContextManager.disposeDeadContexts();

        ClientData.TRAINS.forEach(train -> {
            train.isRemoved = false;
            if (ClientConfig.enableTrainRender) {
                TrainRendererBase renderer = TrainClientRegistry.getTrainProperties(train.trainId).renderer;
                ((TrainClientAccessor) train).setTrainRenderer(renderer.createTrainInstance(train));
            } else {
                ((TrainClientAccessor) train).setTrainRenderer(NoopTrainRenderer.INSTANCE);
            }
            if (ClientConfig.enableTrainSound) {
                TrainSoundBase sound = TrainClientRegistry.getTrainProperties(train.trainId).sound;
                ((TrainClientAccessor) train).setTrainSound(sound.createTrainInstance(train));
            } else {
                ((TrainClientAccessor) train).setTrainSound(NoopTrainSound.INSTANCE);
            }
        });
    }
}
