package mtr.render;

import mtr.block.BlockRail;
import mtr.data.Pos3f;
import mtr.data.Rail;
import mtr.gui.IGui;
import mtr.item.ItemRailModifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.apache.http.cookie.SM;

import java.util.Collection;

public class RenderRail extends BlockEntityRenderer<BlockRail.TileEntityRail> implements IGui {

	public RenderRail(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void render(BlockRail.TileEntityRail entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		final World world = entity.getWorld();
		if (world == null) {
			return;
		}

		final Collection<Rail> railList = entity.railMap.values();
		final BlockPos pos = entity.getPos();
		final ClientPlayerEntity player = MinecraftClient.getInstance().player;
		final boolean renderColors = player != null && player.isHolding(item -> item instanceof ItemRailModifier);

		matrices.push();
		matrices.translate(-pos.getX(), 0.0625 + SMALL_OFFSET - pos.getY(), -pos.getZ());

		for (final Rail rail : railList) {
			rail.render((h, k, r, t1, t2, y1, y2, isStraight, isEnd) -> {
				final Pos3f rc1 = Rail.getPositionXZ(h, k, r, t1, -1, isStraight);
				final Pos3f rc2 = Rail.getPositionXZ(h, k, r, t1, 1, isStraight);
				final Pos3f rc3 = Rail.getPositionXZ(h, k, r, t2, 1, isStraight);
				final Pos3f rc4 = Rail.getPositionXZ(h, k, r, t2, -1, isStraight);

				final float textureOffsetRail = (((int) (rc1.x + rc2.x)) % 4) * 0.25F;
				final BlockPos pos2 = new BlockPos(rc1.x, y1, rc2.x);
				final int light2 = LightmapTextureManager.pack(world.getLightLevel(LightType.BLOCK, pos2), world.getLightLevel(LightType.SKY, pos2));
				final int color = renderColors || rail.railType == Rail.RailType.PLATFORM ? rail.railType.color : -1;

				IGui.drawTexture(
					matrices, vertexConsumers, "textures/block/rail.png",
					rc1.x, y1, rc1.z, rc2.x, y1 + SMALL_OFFSET, rc2.z,
					rc3.x, y2, rc3.z, rc4.x, y2 + SMALL_OFFSET, rc4.z,
					0, 0.1875F + textureOffsetRail, 1, 0.3125F + textureOffsetRail,
					Direction.UP, color, light2
				);
				IGui.drawTexture(
					matrices, vertexConsumers, "textures/block/rail.png",
					rc4.x, y2 + SMALL_OFFSET / 4, rc4.z, rc3.x, y2, rc3.z,
					rc2.x, y1 + SMALL_OFFSET / 4, rc2.z, rc1.x, y1, rc1.z,
					0, 0.1875F + textureOffsetRail, 1, 0.3125F + textureOffsetRail,
					Direction.UP, color, light2
				);

				// Render ballast
				if (true) { // was supposed to be if (!isEnd), but it seems isEnd might be true on non-end blocks
							// especially on curve rails
							// TODO: Figure out the correct way to determine if it's at the rail nodes' position
					// SMALL_OFFSET is to Prevent Z-fighting with existing blocks
					// Maybe can be removed if isEnd can be figured out correctly
					final Pos3f bc1 = Rail.getPositionXZ(h, k, r, t1, -1.5F, isStraight).add(SMALL_OFFSET, 0, SMALL_OFFSET);
					final Pos3f bc2 = Rail.getPositionXZ(h, k, r, t1, 1.5F, isStraight).add(SMALL_OFFSET, 0, SMALL_OFFSET);
					final Pos3f bc3 = Rail.getPositionXZ(h, k, r, t2, 1.5F, isStraight).add(SMALL_OFFSET, 0, SMALL_OFFSET);
					final Pos3f bc4 = Rail.getPositionXZ(h, k, r, t2, -1.5F, isStraight).add(SMALL_OFFSET, 0, SMALL_OFFSET);

					final float dY = 1F / 16; // To make the bottom level with other blocks. TODO: Why 16/1?
					final float yb1 = y1 - dY;
					final float yb2 = y2 - dY;
					final float yf = (float) Math.floor(Math.min(yb1, yb2)) - dY;

					// Top of ballast.
					IGui.drawTexture(
							matrices, vertexConsumers, "textures/block/gravel.png",
							bc1.x, yb1, bc1.z, bc2.x, yb1, bc2.z,
							bc3.x, yb2, bc3.z, bc4.x, yb2, bc4.z,
							0, 0, 3, 1,
							Direction.UP, color, light2
					);
					/*IGui.drawTexture(
							matrices, vertexConsumers, "textures/block/gravel.png",
							bc4.x, yb2, bc4.z, bc3.x, yb2, bc3.z,
							bc2.x, yb1, bc2.z, bc1.x, yb1, bc1.z,
							0, 0, 3, 1,
							Direction.UP, color, light2
					);*/
					// TODO: Maybe there are unnecessary draw calls
					// Draw 3D ballast below a slope rail.
					if (y1 != yf || y2 != yf) {
						// Bottom of ballast.
						IGui.drawTexture(
								matrices, vertexConsumers, "textures/block/gravel.png",
								bc1.x, yf, bc1.z, bc2.x, yf, bc2.z,
								bc3.x, yf, bc3.z, bc4.x, yf, bc4.z,
								0, 0, 3, 1,
								Direction.DOWN, color, light2
						);
						// Left-Right sides of ballast.
						// TODO: Calculate normal correctly
						IGui.drawTexture(
								matrices, vertexConsumers, "textures/block/gravel.png",
								bc1.x, yb1, bc1.z, bc4.x, yb2, bc4.z,
								bc4.x, yf, bc4.z, bc1.x, yf, bc1.z,
								0, 0, 1, 1,
								Direction.UP, color, light2
						);
						IGui.drawTexture(
								matrices, vertexConsumers, "textures/block/gravel.png",
								bc2.x, yf, bc2.z, bc3.x, yf, bc3.z,
								bc3.x, yb2, bc3.z, bc2.x, yb1, bc2.z,
								0, 0, 1, 1,
								Direction.UP, color, light2
						);
						// Front-back sides of ballast.
						IGui.drawTexture(
								matrices, vertexConsumers, "textures/block/gravel.png",
								bc1.x, yb1, bc1.z, bc2.x, yb1, bc2.z,
								bc2.x, yf, bc2.z, bc1.x, yf, bc1.z,
								0, 0, 3, 1,
								Direction.UP, color, light2
						);
						IGui.drawTexture(
								matrices, vertexConsumers, "textures/block/gravel.png",
								bc3.x, yb2, bc3.z, bc4.x, yb2, bc4.z,
								bc4.x, yf, bc4.z, bc3.x, yf, bc3.z,
								0, 0, 3, 1,
								Direction.UP, color, light2
						);
					}
				}
			});
		}

		matrices.pop();
	}

	@Override
	public boolean rendersOutsideBoundingBox(BlockRail.TileEntityRail blockEntity) {
		return true;
	}
}