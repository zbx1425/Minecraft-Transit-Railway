package mtr.render;

import mtr.data.IGui;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class RenderingCache implements IGui {

    private final boolean isSolid;
    private final Identifier textureId;

    private int lightUpdateIndex = 0;
    private static final int UPDATES_PER_FRAME = 2;

    protected final List<RenderingCacheItem> cacheList = new ArrayList<>();

    public RenderingCache(boolean isSolid, String texture) {
        this.isSolid = isSolid;
        textureId = new Identifier(texture);
    }

    public RenderingCache(boolean isSolid, Sprite sprite) {
        this.isSolid = isSolid;
        textureId = new Identifier("textures/" + sprite.getId().getPath() + ".png");
    }

    public void apply(VertexConsumerProvider vertexConsumers, MatrixStack matrices, World world, boolean enableColor) {
        vertexConsumers.getBuffer(RenderLayer.getBlockLayers().get(0));
        final VertexConsumer vertexConsumer = vertexConsumers.getBuffer(isSolid ? MoreRenderLayers.getSolid(textureId) : MoreRenderLayers.getExterior(textureId));

        // Caching light data - how much impact does it have?
        for (int i = 0; i < cacheList.size(); i++) {
            cacheList.get(i).apply(vertexConsumer, matrices, world, enableColor, i >= lightUpdateIndex && i < lightUpdateIndex + UPDATES_PER_FRAME);
        }
        lightUpdateIndex += UPDATES_PER_FRAME;
        if (lightUpdateIndex > cacheList.size()) {
            lightUpdateIndex = 0;
        }
    }

    public void reset() {
        cacheList.clear();
    }

    public abstract static class RenderingCacheItem {

        public final float x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4;
        public final float u1, v1, u2, v2, u3, v3, u4, v4;
        public final int color;
        public final Direction facing;
        public final BlockPos pos;

        public RenderingCacheItem(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4, int color, Direction facing, BlockPos pos) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.x3 = x3;
            this.y3 = y3;
            this.z3 = z3;
            this.x4 = x4;
            this.y4 = y4;
            this.z4 = z4;
            this.u1 = u1;
            this.v1 = v1;
            this.u2 = u2;
            this.v2 = v2;
            this.u3 = u3;
            this.v3 = v3;
            this.u4 = u4;
            this.v4 = v4;
            this.color = color;
            this.facing = facing;
            this.pos = pos;
        }

        public abstract void apply(VertexConsumer vertexConsumer, MatrixStack matrices, World world, boolean enableColor, boolean performLightUpdate);
    }
}
