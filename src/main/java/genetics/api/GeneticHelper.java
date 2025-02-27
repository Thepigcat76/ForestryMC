package genetics.api;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

import genetics.api.alleles.Allele;
import genetics.api.alleles.IAllele;
import genetics.api.alleles.IAlleleValue;
import genetics.api.individual.IChromosomeAllele;
import genetics.api.individual.IChromosomeType;
import genetics.api.individual.IChromosomeValue;
import genetics.api.individual.IGenome;
import genetics.api.individual.IIndividual;
import genetics.api.organism.EmptyOrganismType;
import genetics.api.organism.IOrganism;
import genetics.api.organism.IOrganismHandler;
import genetics.api.organism.IOrganismType;
import genetics.api.root.EmptyRootDefinition;
import genetics.api.root.IIndividualRoot;
import genetics.api.root.IRootDefinition;

/**
 * A helper class that contains some help methods.
 */
public class GeneticHelper {
	public static Capability<IOrganism> ORGANISM = CapabilityManager.get(new CapabilityToken<>() {});
	public static final IOrganism<?> EMPTY = EmptyOrganism.INSTANCE;

	public static boolean isValidTemplate(@Nullable IAllele[] template, IRootDefinition<?> root) {
		return template != null && template.length >= root.map(value -> value.getTemplates().getKaryotype().size()).orElse(0);
	}

	public static boolean isValidTemplate(@Nullable IAllele[] template, IIndividualRoot<?> root) {
		return template != null && template.length >= root.getTemplates().getKaryotype().size();
	}

	@Nullable
	public static IGenome genomeFromTemplate(String templateName, IRootDefinition<?> rootDef) {
		return rootDef.map(root -> {
			IAllele[] template = root.getTemplates().getTemplate(templateName);
			if (GeneticHelper.isValidTemplate(template, root)) {
				return root.getKaryotype().templateAsGenome(template);
			}
			return null;
		}).orElse(null);
	}

	public static <I extends IIndividual> IOrganism<I> createOrganism(ItemStack itemStack, IOrganismType type, IRootDefinition<? extends IIndividualRoot<I>> root) {
		IGeneticFactory geneticFactory = GeneticsAPI.apiInstance.getGeneticFactory();
		return geneticFactory.createOrganism(itemStack, type, root);
	}

	@SuppressWarnings("unchecked")
	public static <I extends IIndividual> IOrganism<I> getOrganism(ItemStack itemStack) {
		return itemStack.getCapability(ORGANISM).orElse(EMPTY);
	}

	public static <I extends IIndividual> boolean setIndividual(ItemStack itemStack, I individual) {
		IOrganism<I> organism = getOrganism(itemStack);
		return organism.setIndividual(individual);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <I extends IIndividual> I getIndividual(ItemStack itemStack) {
		return (I) itemStack.getCapability(ORGANISM).orElse(EMPTY).getIndividual();
	}

	public static IOrganismHandler<IIndividual> getOrganismHandler(IIndividualRoot<IIndividual> root, IOrganismType type) {
		IOrganismHandler<IIndividual> handler = root.getTypes().getHandler(type);
		if (handler == null) {
			throw new IllegalArgumentException(String.format("No organism handler was registered for the organism type '%s'", type.getName()));
		}
		return handler;
	}

	private enum EmptyOrganism implements IOrganism<IIndividual> {
		INSTANCE;

		@Nullable
		@Override
		public IIndividual getIndividual() {
			return null;
		}

		@Override
		public boolean setIndividual(IIndividual individual) {
			return false;
		}

		@Override
		public IRootDefinition<? extends IIndividualRoot<IIndividual>> getDefinition() {
			return EmptyRootDefinition.empty();
		}

		@Override
		public IOrganismType getType() {
			return EmptyOrganismType.INSTANCE;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public IAllele getAllele(IChromosomeType type, boolean active) {
			return Allele.EMPTY;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <A extends IAllele> A getAllele(IChromosomeAllele<A> type, boolean active) {
			return (A) Allele.EMPTY;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <V> IAlleleValue<V> getAllele(IChromosomeValue<V> type, boolean active) {
			return (IAlleleValue<V>) Allele.EMPTY;
		}


		@Nullable
		@Override
		public IAllele getAlleleDirectly(IChromosomeType type, boolean active) {
			return null;
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
			return LazyOptional.empty();
		}
	}
}
