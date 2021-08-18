package mtr.render;

import mtr.gui.IDrawing;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class QuadCache extends RenderingCache {

	public QuadCache(boolean isSolid, String texture) {
		super(isSolid, texture);
	}

	public QuadCache(boolean isSolid, Sprite sprite) {
		super(isSolid, sprite);
	}

	public void addFace(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, Direction facing, int color, BlockPos lightRefPos) {
		final float alpha = ((color >> 24) & 0xFF) / 255F;
		if (alpha != 0) {
			cacheList.add(new QuadCacheItem(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, u1, v2, u2, v2, u2, v1, u1, v1, color, facing, lightRefPos));
		}
	}

	private static class QuadCacheItem extends RenderingCacheItem {

		private int cachedLight = -1;

		public QuadCacheItem(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4, int color, Direction facing, BlockPos pos) {
			super(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, u1, v1, u2, v2, u3, v3, u4, v4, color, facing, pos);
		}

		public void apply(VertexConsumer vertexConsumer, MatrixStack matrices, World world, boolean enableColor, boolean performLightUpdate) {
			if (cachedLight == -1 || performLightUpdate) {
				cachedLight = WorldRenderer.getLightmapCoordinates(world, pos);
			}
			final int usedColor = enableColor ? color : ARGB_WHITE;
			IDrawing.drawTexture(matrices, vertexConsumer, x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, u1, v1, u2, v2, u3, v3, u4, v4, facing, usedColor, cachedLight);
		}
	}
}