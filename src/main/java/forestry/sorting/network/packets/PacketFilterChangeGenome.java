package forestry.sorting.network.packets;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import forestry.api.genetics.GeneticCapabilities;
import forestry.core.network.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;

import genetics.api.alleles.IAllele;
import genetics.utils.AlleleUtils;

public record PacketFilterChangeGenome(BlockPos pos, Direction facing, short index, boolean active, @Nullable IAllele allele) implements IForestryPacketServer {
	public static void handle(PacketFilterChangeGenome msg, ServerPlayer player) {
		TileUtil.getInterface(player.level, msg.pos(), GeneticCapabilities.FILTER_LOGIC, null).ifPresent(logic -> {
			if (logic.setGenomeFilter(msg.facing(), msg.index(), msg.active(), msg.allele())) {
				logic.getNetworkHandler().sendToPlayers(logic, player.getLevel(), player);
			}
		});
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		NetworkUtil.writeDirection(buffer, facing);
		buffer.writeShort(index);
		buffer.writeBoolean(active);
		if (allele != null) {
			buffer.writeBoolean(true);
			buffer.writeUtf(allele.getRegistryName().toString());
		} else {
			buffer.writeBoolean(false);
		}
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.FILTER_CHANGE_GENOME;
	}

	public static PacketFilterChangeGenome decode(FriendlyByteBuf buffer) {
		BlockPos pos = buffer.readBlockPos();
		Direction facing = NetworkUtil.readDirection(buffer);
		short index = buffer.readShort();
		boolean active = buffer.readBoolean();
		IAllele allele = buffer.readBoolean() ? AlleleUtils.getAllele(buffer.readUtf()) : null;

		return new PacketFilterChangeGenome(pos, facing, index, active, allele);
	}
}
