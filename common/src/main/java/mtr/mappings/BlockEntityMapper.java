package mtr.mappings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockEntityMapper extends BlockEntity {

	public BlockEntityMapper(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public final void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider registries) {
		super.loadAdditional(compoundTag, registries);
		readCompoundTag(compoundTag);
	}

	@Override
	public final void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider registries) {
		super.saveAdditional(compoundTag, registries);
		writeCompoundTag(compoundTag);
	}

	public void readCompoundTag(CompoundTag compoundTag) {
	}

	public void writeCompoundTag(CompoundTag compoundTag) {
	}
}
