package forestry.sorting.network.packets;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import forestry.api.genetics.GeneticCapabilities;
import forestry.api.genetics.alleles.AlleleManager;
import forestry.api.genetics.filter.IFilterRuleType;
import forestry.core.network.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;

public record PacketFilterChangeRule(BlockPos pos, Direction facing, IFilterRuleType rule) implements IForestryPacketServer {
	@Override
	public ResourceLocation id() {
		return PacketIdServer.FILTER_CHANGE_RULE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeShort(facing.get3DDataValue());
		buffer.writeShort(AlleleManager.filterRegistry.getId(rule));
	}

	public static PacketFilterChangeRule decode(FriendlyByteBuf buffer) {
		return new PacketFilterChangeRule(buffer.readBlockPos(), Direction.VALUES[buffer.readShort()], Objects.requireNonNull(AlleleManager.filterRegistry.getRule(buffer.readShort())));
	}

	public static void handle(PacketFilterChangeRule msg, ServerPlayer player) {
		TileUtil.getInterface(player.level, msg.pos(), GeneticCapabilities.FILTER_LOGIC, null).ifPresent(logic -> {
			if (logic.setRule(msg.facing(), msg.rule())) {
				logic.getNetworkHandler().sendToPlayers(logic, player.getLevel(), player);
			}
		});
	}
}
