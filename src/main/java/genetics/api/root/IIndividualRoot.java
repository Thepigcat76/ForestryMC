package genetics.api.root;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import genetics.api.alleles.IAllele;
import genetics.api.individual.IChromosomeType;
import genetics.api.individual.IGenome;
import genetics.api.individual.IGenomeWrapper;
import genetics.api.individual.IIndividual;
import genetics.api.individual.IKaryotype;
import genetics.api.organism.IOrganismHandler;
import genetics.api.organism.IOrganismType;
import genetics.api.organism.IOrganismTypes;
import genetics.api.root.components.ComponentKey;
import genetics.api.root.components.IRootComponent;
import genetics.api.root.components.IRootComponentContainer;
import genetics.api.root.translator.IIndividualTranslator;

/**
 * The IGeneticRoot offers several functions to create {@link IIndividual}s and to wrap the genome of a
 * {@link IIndividual}.
 * <p>
 * The IGeneticDefinition is wraps every interface like the {@link IIndividualTranslator}, the {@link IKaryotype}, etc.
 * that are important for the handling of the individual. And it provides the custom implementation of the
 * {@link IIndividualRoot} interface that specifies the individual and can be used to create a instance of it.
 *
 * @param <I> The type of the individual that this root provides.
 */
public interface IIndividualRoot<I extends IIndividual> {
	/* Individual Creation */

	/**
	 * Uses the information that the NBT-Data contains to create a {@link IIndividual}.
	 */
	I create(CompoundTag compound);

	/**
	 * Creates a {@link IIndividual} that contains this genome.
	 */
	I create(IGenome genome);

	/**
	 * Creates a {@link IIndividual} that contains the two genome.
	 */
	I create(IGenome genome, IGenome mate);

	@Nullable
	default I create(ItemStack stack) {
		return getTypes().createIndividual(stack);
	}

	default ItemStack createStack(I individual, IOrganismType type) {
		return getTypes().createStack(individual, type);
	}

	@Nullable
	default IOrganismType getType(ItemStack itemStack) {
		return getTypes().getType(itemStack);
	}

	default IAllele[] getTemplate(String identifier) {
		return getTemplates().getTemplate(identifier);
	}

	Class<? extends I> getMemberClass();

	/**
	 * Creates a optional that describes an {@link IIndividual} that contains the {@link IAllele} template that is
	 * associated with the given identifier.
	 *
	 * @param templateIdentifier A identifier that is associate with a {@link IAllele} template at the
	 *                           {@link ITemplateContainer} of this root.
	 */
	@Nullable
	I create(String templateIdentifier);

	/**
	 * Creates a {@link IIndividual} that contains the alleles of the template in a genome.
	 *
	 * @param template The alleles of the genome.
	 */
	default I templateAsIndividual(IAllele[] template) {
		return templateAsIndividual(template, null);
	}

	/**
	 * Creates a {@link IIndividual} that contains the alleles of the two templates in a genome.
	 *
	 * @param templateActive   The active alleles of the genome.
	 * @param templateInactive The inactive alleles of the genome.
	 */
	default I templateAsIndividual(IAllele[] templateActive, @Nullable IAllele[] templateInactive) {
		IGenome genome = getKaryotype().templateAsGenome(templateActive, templateInactive);
		return create(genome);
	}

	/**
	 * A instance of an {@link IIndividual} that is used if a item has lost its generic data.
	 */
	I getDefaultMember();

	/* Item Stacks */

	/**
	 * Creates an {@link ItemStack} that uses the {@link IAllele} template of the given allele and has the
	 * given organism type.
	 *
	 * @param allele The template identifier
	 * @param type   The type whose {@link IOrganismHandler} will be used to create the stack with
	 *               {@link IOrganismHandler#createStack(IIndividual)}.
	 * @return A stack with the given {@link IOrganismType} and the allele template of the given allele.
	 */
	ItemStack createStack(IAllele allele, IOrganismType type);

	boolean isMember(ItemStack stack);

	/* Genome */

	/**
	 * Creates a wrapper that can be used to give access to the values of the alleles that the genome contains.
	 */
	IGenomeWrapper createWrapper(IGenome genome);

	/* Individuals */

	List<I> getIndividualTemplates();
	/* Components */

	/**
	 * Returns the template container that contains all registered templates for the individual of this root.
	 * Templates have to be registered at the {@link IIndividualRootBuilder} of the root before the root itself was
	 * built.
	 *
	 * @return The template container of this root.
	 */
	ITemplateContainer<I> getTemplates();

	/**
	 * The Karyotype defines how many {@link IChromosomeType}s the {@link IGenome} of an
	 * {@link IIndividual} has.
	 *
	 * @return The karyotype of this root.
	 */
	IKaryotype getKaryotype();

	/**
	 * A translator that can be used to translate {@link BlockState} and
	 * {@link ItemStack} without any genetic information  into {@link IIndividual}s or into a {@link ItemStack} that
	 * contains a {@link genetics.api.organism.IOrganism}.
	 *
	 * @return A translator that can be used to translate {@link BlockState} and
	 * {@link ItemStack} into {@link IIndividual}s.
	 */
	IIndividualTranslator<I> getTranslator();

	/**
	 * Translates {@link BlockState}s into genetic data.
	 */
	@Nullable
	default I translateMember(BlockState objectToTranslate) {
		return getTranslator().translateMember(objectToTranslate);
	}

	/**
	 * Translates {@link ItemStack}s into genetic data.
	 */
	@Nullable
	default I translateMember(ItemStack objectToTranslate) {
		return getTranslator().translateMember(objectToTranslate);
	}

	/**
	 * Translates a {@link BlockState}s into genetic data and returns a {@link ItemStack} that contains this data.
	 */
	default ItemStack getGeneticEquivalent(BlockState objectToTranslate) {
		return getTranslator().getGeneticEquivalent(objectToTranslate);
	}

	/**
	 * Translates {@link ItemStack}s into genetic data and returns a other {@link ItemStack} that contains this data.
	 */
	default ItemStack getGeneticEquivalent(ItemStack objectToTranslate) {
		return getTranslator().getGeneticEquivalent(objectToTranslate);
	}

	/**
	 * All registered {@link IOrganismType}s with there mapped {@link IOrganismHandler}s.
	 *
	 * @return A object that contains all registered types of this root.
	 */
	IOrganismTypes<I> getTypes();

	/* Util */

	/**
	 * @return The string based unique identifier of this definition.
	 */
	String getUID();

	boolean hasComponent(ComponentKey<?> key);

	@Nullable
	<C extends IRootComponent<I>> C getComponentSafe(ComponentKey<?> key);

	<C extends IRootComponent<I>> C getComponent(ComponentKey<?> key);

	IRootComponentContainer<I> getComponentContainer();

	IDisplayHelper<I> getDisplayHelper();

	IRootDefinition<? extends IIndividualRoot<I>> getDefinition();

	<T extends IIndividualRoot<?>> T cast();
}
