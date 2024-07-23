package mtr.mappings;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LoomBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockDirectionalMapper extends HorizontalDirectionalBlock {

	public BlockDirectionalMapper(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		// Currently, its only use is when the block list report is being generated.
		// TODO: Properly implement this
		return LoomBlock.CODEC;
	}

	@Override
	public final void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource randomSource) {
		super.tick(state, world, pos, randomSource);
		tick(state, world, pos);
	}

	@Override
	public final void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource randomSource) {
		super.randomTick(state, world, pos, randomSource);
		randomTick(state, world, pos);
	}

	public void tick(BlockState state, ServerLevel world, BlockPos pos) {
	}

	public void randomTick(BlockState state, ServerLevel world, BlockPos pos) {
	}
}
