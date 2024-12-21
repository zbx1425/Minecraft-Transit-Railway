package cn.zbx1425.mtrsteamloco.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DynamicTexture.class)
public interface DynamicImageAccessor {

    @Accessor(value = "pixels")
    void mtrsteamloco$setPixels(NativeImage pixels);
}
