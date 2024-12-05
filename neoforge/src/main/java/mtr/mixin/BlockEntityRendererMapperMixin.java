package mtr.mixin;

import mtr.mappings.BlockEntityMapper;
import mtr.mappings.BlockEntityRendererMapper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntityRendererMapper.class)
public abstract class BlockEntityRendererMapperMixin<T extends BlockEntityMapper> extends BlockEntityRendererMapper<T> {

    public BlockEntityRendererMapperMixin(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull T blockEntity) {
        return AABB.INFINITE;
    }
}
