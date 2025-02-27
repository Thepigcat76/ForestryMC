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
package forestry.lepidopterology.genetics;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.core.IErrorState;
import forestry.api.core.tooltips.ToolTip;
import forestry.api.genetics.EnumTolerance;
import forestry.api.genetics.alleles.AlleleManager;
import forestry.api.genetics.products.IProductList;
import forestry.api.genetics.products.Product;
import forestry.api.lepidopterology.ButterflyManager;
import forestry.api.lepidopterology.IButterflyCocoon;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.api.lepidopterology.IEntityButterfly;
import forestry.api.lepidopterology.genetics.ButterflyChromosomes;
import forestry.api.lepidopterology.genetics.EnumFlutterType;
import forestry.api.lepidopterology.genetics.IAlleleButterflySpecies;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflyMutation;
import forestry.core.errors.EnumErrorCode;
import forestry.core.genetics.GenericRatings;
import forestry.core.genetics.IndividualLiving;
import forestry.core.utils.ClimateUtil;
import forestry.lepidopterology.ModuleLepidopterology;

import deleteme.BiomeCategory;
import genetics.api.alleles.IAllele;
import genetics.api.alleles.IAlleleValue;
import genetics.api.individual.IChromosome;
import genetics.api.individual.IGenome;
import genetics.api.mutation.IMutationContainer;
import genetics.api.root.IIndividualRoot;
import genetics.api.root.components.ComponentKeys;
import genetics.individual.Genome;
import org.jetbrains.annotations.NotNull;

public class Butterfly extends IndividualLiving implements IButterfly {
	private static final RandomSource rand = RandomSource.create();

	/* CONSTRUCTOR */
	public Butterfly(CompoundTag nbt) {
		super(nbt);
	}

	public Butterfly(IGenome genome) {
		super(genome, null, genome.getActiveValue(ButterflyChromosomes.LIFESPAN));
	}

	public Butterfly(IGenome genome, @Nullable IGenome mate) {
		super(genome, mate);
	}

	@Override
	public IIndividualRoot getRoot() {
		return ButterflyHelper.getRoot();
	}

	@Override
	public void addTooltip(List<Component> list) {
		ToolTip toolTip = new ToolTip();

		IAlleleButterflySpecies primary = genome.getActiveAllele(ButterflyChromosomes.SPECIES);
		IAlleleButterflySpecies secondary = genome.getInactiveAllele(ButterflyChromosomes.SPECIES);
		if (!isPureBred(ButterflyChromosomes.SPECIES)) {
			toolTip.translated("for.butterflies.hybrid", primary.getDisplayName(), secondary.getDisplayName()).style(ChatFormatting.BLUE);
		}

		if (getMate() != null) {
			//TODO ITextComponent.toUpperCase(Locale.ENGLISH));
			toolTip.translated("for.gui.fecundated").style(ChatFormatting.RED);
		}
		toolTip.add(genome.getActiveAllele(ButterflyChromosomes.SIZE).getDisplayName().withStyle(ChatFormatting.YELLOW));
		toolTip.add(genome.getActiveAllele(ButterflyChromosomes.SPEED).getDisplayName().withStyle(ChatFormatting.DARK_GREEN));
		toolTip.singleLine().add(genome.getActiveAllele(ButterflyChromosomes.LIFESPAN).getDisplayName()).text(" ").translated("for.gui.life").style(ChatFormatting.GRAY).create();

		IAlleleValue<EnumTolerance> tempToleranceAllele = getGenome().getActiveAllele(ButterflyChromosomes.TEMPERATURE_TOLERANCE);
		IAlleleValue<EnumTolerance> humidToleranceAllele = getGenome().getActiveAllele(ButterflyChromosomes.HUMIDITY_TOLERANCE);

		toolTip.singleLine().text("T: ").add(AlleleManager.climateHelper.toDisplay(primary.getTemperature())).text(" / ").add(tempToleranceAllele.getDisplayName()).style(ChatFormatting.GREEN).create();
		toolTip.singleLine().text("H: ").add(AlleleManager.climateHelper.toDisplay(primary.getHumidity())).text(" / ").add(humidToleranceAllele.getDisplayName()).style(ChatFormatting.GREEN).create();

		toolTip.add(GenericRatings.rateActivityTime(genome.getActiveValue(ButterflyChromosomes.NOCTURNAL), primary.isNocturnal()).withStyle(ChatFormatting.RED));

		if (genome.getActiveValue(ButterflyChromosomes.TOLERATES_RAIN)) {
			toolTip.translated("for.gui.flyer.tooltip").style(ChatFormatting.WHITE);
		}

		list.addAll(toolTip.getLines());
	}

	@Override
	public IButterfly copy() {
		CompoundTag compoundNBT = new CompoundTag();
		this.write(compoundNBT);
		return new Butterfly(compoundNBT);
	}

	@Override
	public Component getDisplayName() {
		return genome.getPrimary().getDisplayName();
	}

	@Override
	public Set<IErrorState> getCanSpawn(IButterflyNursery nursery, @Nullable IButterflyCocoon cocoon) {
		Level world = nursery.getWorldObj();

		Set<IErrorState> errorStates = new HashSet<>();
		// / Night or darkness requires nocturnal species
		boolean isDaytime = world.isDay();
		if (!isActiveThisTime(isDaytime)) {
			if (isDaytime) {
				errorStates.add(EnumErrorCode.NOT_NIGHT);
			} else {
				errorStates.add(EnumErrorCode.NOT_DAY);
			}
		}

		return addClimateErrors(nursery, errorStates);
	}

	@Override
	public Set<IErrorState> getCanGrow(IButterflyNursery nursery, @Nullable IButterflyCocoon cocoon) {
		Set<IErrorState> errorStates = new HashSet<>();

		return addClimateErrors(nursery, errorStates);
	}

	@NotNull
	private Set<IErrorState> addClimateErrors(IButterflyNursery nursery, Set<IErrorState> errorStates) {
		IAlleleButterflySpecies species = genome.getActiveAllele(ButterflyChromosomes.SPECIES);
		EnumTemperature actualTemperature = nursery.getTemperature();
		EnumTemperature baseTemperature = species.getTemperature();
		EnumTolerance toleranceTemperature = genome.getActiveValue(ButterflyChromosomes.TEMPERATURE_TOLERANCE);
		EnumHumidity actualHumidity = nursery.getHumidity();
		EnumHumidity baseHumidity = species.getHumidity();
		EnumTolerance toleranceHumidity = genome.getActiveValue(ButterflyChromosomes.HUMIDITY_TOLERANCE);
		ClimateUtil.addClimateErrorStates(actualTemperature, actualHumidity, baseTemperature, toleranceTemperature, baseHumidity, toleranceHumidity, errorStates);

		return errorStates;
	}

	@Override
	public boolean canSpawn(Level world, double x, double y, double z) {
		if (!canFly(world)) {
			return false;
		}

		Biome biome = world.getBiome(new BlockPos(x, 0, z)).value();
		IAlleleButterflySpecies species = getGenome().getActiveAllele(ButterflyChromosomes.SPECIES);
		if (!species.getSpawnBiomes().isEmpty()) {
			boolean noneMatched = true;

			if (species.strictSpawnMatch()) {
				var categories = BiomeCategory.getCategoriesFor(biome);
				categories.retainAll(species.getSpawnBiomes());

				if (!categories.isEmpty()) {
					noneMatched = false;
				}
			} else {
				for (BiomeCategory type : species.getSpawnBiomes()) {
					if (type.is(biome)) {
						noneMatched = false;
						break;
					}
				}
			}

			if (noneMatched) {
				return false;
			}
		}

		return isAcceptedEnvironment(world, x, y, z);
	}

	@Override
	public boolean canTakeFlight(Level world, double x, double y, double z) {
		return canFly(world) && isAcceptedEnvironment(world, x, y, z);
	}

	private boolean canFly(Level world) {
		return (!world.isRaining() || getGenome().getActiveValue(ButterflyChromosomes.TOLERATES_RAIN)) && isActiveThisTime(world.isDay());
	}

	@Override
	public boolean isAcceptedEnvironment(Level world, double x, double y, double z) {
		return isAcceptedEnvironment(world, (int) x, (int) y, (int) z);
	}

	private boolean isAcceptedEnvironment(Level world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		Biome biome = world.getBiome(pos).value();
		EnumTemperature biomeTemperature = EnumTemperature.getFromBiome(biome, pos);
		EnumHumidity biomeHumidity = EnumHumidity.getFromValue(biome.getDownfall());
		return AlleleManager.climateHelper.isWithinLimits(biomeTemperature, biomeHumidity,
				getGenome().getActiveAllele(ButterflyChromosomes.SPECIES).getTemperature(), getGenome().getActiveValue(ButterflyChromosomes.TEMPERATURE_TOLERANCE),
				getGenome().getActiveAllele(ButterflyChromosomes.SPECIES).getHumidity(), getGenome().getActiveValue(ButterflyChromosomes.HUMIDITY_TOLERANCE));
	}

	@Override
	@Nullable
	public IButterfly spawnCaterpillar(Level world, IButterflyNursery nursery) {
		// We need a mated queen to produce offspring.
		if (mate == null) {
			return null;
		}

		IChromosome[] chromosomes = new IChromosome[genome.getChromosomes().length];
		IChromosome[] parent1 = genome.getChromosomes();
		IChromosome[] parent2 = mate.getChromosomes();

		// Check for mutation. Replace one of the parents with the mutation
		// template if mutation occured.
		IChromosome[] mutated1 = mutateSpecies(world, nursery, genome, mate);
		if (mutated1 != null) {
			parent1 = mutated1;
		}
		IChromosome[] mutated2 = mutateSpecies(world, nursery, mate, genome);
		if (mutated2 != null) {
			parent2 = mutated2;
		}

		for (int i = 0; i < parent1.length; i++) {
			if (parent1[i] != null && parent2[i] != null) {
				chromosomes[i] = parent1[i].inheritChromosome(rand, parent2[i]);
			}
		}

		return new Butterfly(new Genome(ButterflyHelper.getRoot().getKaryotype(), chromosomes));
	}

	@Nullable
	private static IChromosome[] mutateSpecies(Level world, IButterflyNursery nursery, IGenome genomeOne, IGenome genomeTwo) {

		IChromosome[] parent1 = genomeOne.getChromosomes();
		IChromosome[] parent2 = genomeTwo.getChromosomes();

		IGenome genome0;
		IGenome genome1;
		IAllele allele0;
		IAllele allele1;

		if (rand.nextBoolean()) {
			allele0 = parent1[ButterflyChromosomes.SPECIES.ordinal()].getActiveAllele();
			allele1 = parent2[ButterflyChromosomes.SPECIES.ordinal()].getInactiveAllele();

			genome0 = genomeOne;
			genome1 = genomeTwo;
		} else {
			allele0 = parent2[ButterflyChromosomes.SPECIES.ordinal()].getActiveAllele();
			allele1 = parent1[ButterflyChromosomes.SPECIES.ordinal()].getInactiveAllele();

			genome0 = genomeTwo;
			genome1 = genomeOne;
		}

		IMutationContainer<IButterfly, IButterflyMutation> container = ButterflyHelper.getRoot().getComponent(ComponentKeys.MUTATIONS);
		for (IButterflyMutation mutation : container.getMutations(true)) {
			float chance = mutation.getChance(world, nursery, allele0, allele1, genome0, genome1);
			if (chance > rand.nextFloat() * 100) {
				return ButterflyManager.butterflyRoot.getKaryotype().templateAsChromosomes(mutation.getTemplate());
			}
		}

		return null;
	}

	private boolean isActiveThisTime(boolean isDayTime) {
		if (getGenome().getActiveValue(ButterflyChromosomes.NOCTURNAL)) {
			return true;
		}

		return isDayTime != getGenome().getActiveAllele(ButterflyChromosomes.SPECIES).isNocturnal();
	}

	@Override
	public float getSize() {
		return getGenome().getActiveValue(ButterflyChromosomes.SIZE);
	}

	@Override
	public NonNullList<ItemStack> getLootDrop(IEntityButterfly entity, boolean playerKill, int lootLevel) {
		NonNullList<ItemStack> drop = NonNullList.create();

		PathfinderMob creature = entity.getEntity();
		float metabolism = (float) getGenome().getActiveValue(ButterflyChromosomes.METABOLISM) / 10;
		IProductList products = getGenome().getActiveAllele(ButterflyChromosomes.SPECIES).getButterflyLoot();

		for (Product product : products.getPossibleProducts()) {
			if (creature.level.random.nextFloat() < product.getChance() * metabolism) {
				drop.add(product.copyStack());
			}
		}

		return drop;
	}

	@Override
	public NonNullList<ItemStack> getCaterpillarDrop(IButterflyNursery nursery, boolean playerKill, int lootLevel) {
		NonNullList<ItemStack> drop = NonNullList.create();
		float metabolism = (float) getGenome().getActiveValue(ButterflyChromosomes.METABOLISM) / 10;
		IProductList products = getGenome().getActiveAllele(ButterflyChromosomes.SPECIES).getCaterpillarLoot();
		for (Product product : products.getPossibleProducts()) {
			if (rand.nextFloat() < product.getChance() * metabolism) {
				drop.add(product.copyStack());
			}
		}

		return drop;
	}

	@Override
	public NonNullList<ItemStack> getCocoonDrop(IButterflyCocoon cocoon) {
		NonNullList<ItemStack> drop = NonNullList.create();
		float metabolism = (float) getGenome().getActiveValue(ButterflyChromosomes.METABOLISM) / 10;
		IProductList products = getGenome().getActiveAllele(ButterflyChromosomes.COCOON).getCocoonLoot();

		for (Product product : products.getPossibleProducts()) {
			if (rand.nextFloat() < product.getChance() * metabolism) {
				drop.add(product.copyStack());
			}
		}

		if (ModuleLepidopterology.getSerumChance() > 0) {
			if (rand.nextFloat() < ModuleLepidopterology.getSerumChance() * metabolism) {
				ItemStack stack = ButterflyManager.butterflyRoot.getTypes().createStack(this, EnumFlutterType.SERUM);
				if (ModuleLepidopterology.getSecondSerumChance() > 0) {
					if (rand.nextFloat() < ModuleLepidopterology.getSecondSerumChance() * metabolism) {
						stack.setCount(2);
					}
				}
				drop.add(ButterflyManager.butterflyRoot.getTypes().createStack(this, EnumFlutterType.SERUM));
			}
		}

		if (cocoon.isSolid()) {
			drop.add(ButterflyManager.butterflyRoot.getTypes().createStack(this, EnumFlutterType.BUTTERFLY));
		}

		return drop;
	}

}
