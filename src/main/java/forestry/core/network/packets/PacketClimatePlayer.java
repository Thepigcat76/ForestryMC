package forestry.core.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import forestry.api.climate.IClimateState;
import forestry.core.ClimateHandlerClient;
import forestry.core.network.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.utils.NetworkUtil;

public record PacketClimatePlayer(IClimateState state) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.CLIMATE_PLAYER;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		NetworkUtil.writeClimateState(buffer, state);
	}

	public static PacketClimatePlayer decode(FriendlyByteBuf buffer) {
		return new PacketClimatePlayer(NetworkUtil.readClimateState(buffer));
	}

	public static void handle(PacketClimatePlayer msg, ServerPlayer player) {
		ClimateHandlerClient.setCurrentState(msg.state);
	}
}
