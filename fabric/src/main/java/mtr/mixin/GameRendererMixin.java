package mtr.mixin;

import mtr.MTRClient;
import mtr.render.RenderTrains;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    void renderFramePre(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        MTRClient.incrementGameTick();
        RenderTrains.simulate();
    }
}
