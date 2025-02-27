/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core.tiles;

import javax.annotation.Nullable;
import java.util.Random;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.api.genetics.ForestryComponentKeys;
import forestry.api.genetics.IResearchHandler;
import forestry.api.genetics.alleles.IAlleleForestrySpecies;
import forestry.core.network.IStreamable;
import forestry.core.utils.NetworkUtil;

import genetics.api.individual.IIndividual;
import genetics.utils.RootUtils;

public class EscritoireGame implements INbtWritable, INbtReadable, IStreamable {
	private static final Random rand = new Random();
	public static final int BOUNTY_MAX = 16;

	public enum Status {
		EMPTY, PLAYING, FAILURE, SUCCESS;
		public static final Status[] VALUES = values();
	}

	private EscritoireGameBoard gameBoard;
	private long lastUpdate;
	private int bountyLevel;
	private Status status = Status.EMPTY;

	public EscritoireGame() {
		gameBoard = new EscritoireGameBoard();
	}

	@Nullable
	public EscritoireGameToken getToken(int index) {
		return gameBoard.getToken(index);
	}

	public Status getStatus() {
		return status;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	@Override
	public CompoundTag write(CompoundTag compoundNBT) {
		compoundNBT.putInt("bountyLevel", bountyLevel);
		compoundNBT.putLong("lastUpdate", lastUpdate);
		gameBoard.write(compoundNBT);

		compoundNBT.putInt("Status", status.ordinal());
		return compoundNBT;
	}

	@Override
	public void read(CompoundTag nbt) {
		bountyLevel = nbt.getInt("bountyLevel");
		lastUpdate = nbt.getLong("lastUpdate");
		gameBoard = new EscritoireGameBoard(nbt);

		if (nbt.contains("Status")) {
			int statusOrdinal = nbt.getInt("Status");
			status = Status.values()[statusOrdinal];
		}

		lastUpdate = System.currentTimeMillis();
	}

	/* NETWORK */
	@Override
	public void writeData(FriendlyByteBuf data) {
		data.writeInt(bountyLevel);
		gameBoard.writeData(data);
		NetworkUtil.writeEnum(data, status);
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		bountyLevel = data.readInt();
		gameBoard.readData(data);
		status = NetworkUtil.readEnum(data, Status.VALUES);
	}

	/* INTERACTION */
	public void initialize(ItemStack specimen) {
		reset();
		if (gameBoard.initialize(specimen)) {
			status = Status.PLAYING;
			bountyLevel = BOUNTY_MAX;
			lastUpdate = System.currentTimeMillis();
		}
	}

	public void probe(ItemStack specimen, Container inventory, int startSlot, int slotCount) {
		if (status != Status.PLAYING) {
			return;
		}

		IIndividual individual = RootUtils.getIndividual(specimen);
		if (individual == null) {
			return;
		}

		if (bountyLevel > 1) {
			bountyLevel--;
		}

		IAlleleForestrySpecies species = individual.getGenome().getPrimary(IAlleleForestrySpecies.class);
		IResearchHandler handler = species.getRoot().getComponent(ForestryComponentKeys.RESEARCH);
		gameBoard.hideProbedTokens();

		int revealCount = getSampleSize(slotCount);
		for (int i = 0; i < revealCount; i++) {
			ItemStack sample = inventory.removeItem(startSlot + i, 1);
			if (!sample.isEmpty()) {
				if (rand.nextFloat() < handler.getResearchSuitability(species, sample)) {
					gameBoard.probe();
				}
			}
		}

		lastUpdate = System.currentTimeMillis();
	}

	public void reset() {
		bountyLevel = BOUNTY_MAX;
		gameBoard.reset();
		status = Status.EMPTY;

		lastUpdate = System.currentTimeMillis();
	}

	public void choose(int tokenIndex) {
		if (getStatus() != Status.PLAYING) {
			return;
		}

		EscritoireGameToken token = gameBoard.getToken(tokenIndex);
		if (token != null) {
			status = gameBoard.choose(token);
			lastUpdate = System.currentTimeMillis();
		}
	}

	public int getBountyLevel() {
		return bountyLevel;
	}

	/* RETRIEVAL */
	public int getSampleSize(int slotCount) {
		if (status == Status.EMPTY) {
			return 0;
		}

		int samples = gameBoard.getTokenCount() / 4;
		samples = Math.max(samples, 2);
		return Math.min(samples, slotCount);
	}
}
