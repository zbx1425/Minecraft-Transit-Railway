package mtr.render;

import mtr.data.IGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class BlockFaceCache extends RenderingCache {

	private static final BlockModelRenderer BLOCK_MODEL_RENDERER = MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer();
	private static final BlockModelRenderer.AmbientOcclusionCalculator AO_CALCULATOR = BLOCK_MODEL_RENDERER.new AmbientOcclusionCalculator();

	public BlockFaceCache(Sprite sprite) {
		super(true, sprite);
	}

	public void addBlockFace(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4, Direction facing, BlockPos pos, int color) {
		cacheList.add(new BlockFaceCacheItem(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, u1, v1, u2, v2, u3, v3, u4, v4, color | ARGB_BLACK, facing, pos));
	}

	private static class BlockFaceCacheItem extends RenderingCacheItem {

		private int[] light;
		private float[] brightness;

		public BlockFaceCacheItem(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4, int color, Direction facing, BlockPos pos) {
			super(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, u1, v1, u2, v2, u3, v3, u4, v4, color, facing, pos);
		}

		public void apply(VertexConsumer vertexConsumer, MatrixStack matrices, World world, boolean enableColor, boolean performLightUpdate) {
			final BlockState bs = world.getBlockState(pos);
			final float yMax = Math.max(Math.max(y1, y2), Math.max(y3, y4));

			matrices.push();
			final float dY = 0.0625F + mtr.data.IGui.SMALL_OFFSET;
			matrices.translate(pos.getX(), pos.getY() - dY, pos.getZ());

			BlockPos lightRefPos;
			float y2 = this.y2, y3 = this.y3;
			if (facing.getAxis() != Direction.Axis.Y) {
				lightRefPos = pos.offset(facing);
				// Sometimes on very steep slopes, a segment can cover multiple blocks horizontally.
				// This is to check an upper part when lower part is blocked, to prevent black faces from being shown.
				// Because such a steep slope is rarely used, it's not that necessary to divide it into multiple faces.
				while (!world.getBlockState(lightRefPos).isAir()) {
					if (lightRefPos.getY() <= pos.getY() + yMax) {
						lightRefPos = lightRefPos.up();
					} else {
						break;
					}
				}

				// Update the positions to reject the invisible parts, making the lighting area correct.
				final float yRefOffset = lightRefPos.getY() - pos.getY();
				if (yRefOffset > 0) {
					// This assumes y2 and y3 is at the bottom.
					y2 = Math.min(y2, yRefOffset);
					y3 = Math.min(y3, yRefOffset);
				}
			} else {
				lightRefPos = pos;
			}

			int[] vertexData = new int[]{
					Float.floatToIntBits(x1), Float.floatToIntBits(y1), Float.floatToIntBits(z1), 0, Float.floatToIntBits(u1), Float.floatToIntBits(v1), 0, 0,
					Float.floatToIntBits(x2), Float.floatToIntBits(y2), Float.floatToIntBits(z2), 0, Float.floatToIntBits(u2), Float.floatToIntBits(v2), 0, 0,
					Float.floatToIntBits(x3), Float.floatToIntBits(y3), Float.floatToIntBits(z3), 0, Float.floatToIntBits(u3), Float.floatToIntBits(v3), 0, 0,
					Float.floatToIntBits(x4), Float.floatToIntBits(y4), Float.floatToIntBits(z4), 0, Float.floatToIntBits(u4), Float.floatToIntBits(v4), 0, 0,
			};
			BakedQuad quad = new BakedQuad(vertexData, 0, facing, null, true);

			if (this.brightness == null || this.light == null || performLightUpdate) {
				if (MinecraftClient.isAmbientOcclusionEnabled() && (facing.getAxis() != Direction.Axis.Y)) {
					BitSet flags = new BitSet(3);
					float[] box = new float[Direction.values().length * 2];
					BLOCK_MODEL_RENDERER.getQuadDimensions(world, bs, lightRefPos.offset(facing.getOpposite()), vertexData, facing, box, flags);
					AO_CALCULATOR.apply(world, bs, lightRefPos.offset(facing.getOpposite()), facing, box, flags, true);
					brightness = AO_CALCULATOR.brightness.clone();
					light = AO_CALCULATOR.light.clone();
				} else {
					final int light = WorldRenderer.getLightmapCoordinates(world, lightRefPos);
					final float brightness = world.getBrightness(facing, true);
					if (this.brightness == null) this.brightness = new float[4];
					if (this.light == null) this.light = new int[4];
					this.brightness[0] = brightness;
					this.brightness[1] = brightness;
					this.brightness[2] = brightness;
					this.brightness[3] = brightness;
					this.light[0] = light;
					this.light[1] = light;
					this.light[2] = light;
					this.light[3] = light;
				}
			}
			final float r = ((color >> 16) & 0xFF) / 255F;
			final float g = ((color >> 8) & 0xFF) / 255F;
			final float b = (color & 0xFF) / 255F;
			vertexConsumer.quad(matrices.peek(), quad, brightness, r, g, b, light, 0, false);

			matrices.pop();
		}
	}
}