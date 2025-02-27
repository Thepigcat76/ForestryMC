package forestry.core.multiblock;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import forestry.Forestry;
import forestry.api.multiblock.IMultiblockComponent;

/**
 * This is a very static singleton registry class which directs incoming events to sub-objects, which
 * actually manage each individual world's multiblocks.
 *
 * @author Erogenous Beef
 */
public class MultiblockRegistry {
	// World > WorldRegistry map
	private static final Map<LevelAccessor, MultiblockWorldRegistry> registries = new HashMap<>();

	/**
	 * Called before Tile Entities are ticked in the world. Do bookkeeping here.
	 *
	 * @param world The world being ticked
	 */
	public static void tickStart(LevelAccessor world) {
		if (registries.containsKey(world)) {
			MultiblockWorldRegistry registry = registries.get(world);
			registry.processMultiblockChanges();
			registry.tickStart();
		}
	}

	/**
	 * Called when the world has finished loading a chunk.
	 *
	 * @param world  The world which has finished loading a chunk
	 * @param chunkX The X coordinate of the chunk
	 * @param chunkZ The Z coordinate of the chunk
	 */
	public static void onChunkLoaded(LevelAccessor world, int chunkX, int chunkZ) {
		if (registries.containsKey(world)) {
			registries.get(world).onChunkLoaded(chunkX, chunkZ);
		}
	}

	/**
	 * Register a new part in the system. The part has been created either through user action or via a chunk loading.
	 *
	 * @param world The world into which this part is loading.
	 * @param part  The part being loaded.
	 */
	public static void onPartAdded(Level world, IMultiblockComponent part) {
		MultiblockWorldRegistry registry = getOrCreateRegistry(world);
		registry.onPartAdded(part);
	}

	/**
	 * Call to remove a part from world lists.
	 *
	 * @param world The world from which a multiblock part is being removed.
	 * @param part  The part being removed.
	 */
	public static void onPartRemovedFromWorld(Level world, IMultiblockComponent part) {
		if (registries.containsKey(world)) {
			registries.get(world).onPartRemovedFromWorld(part);
		}

	}


	/**
	 * Called whenever a world is unloaded. Unload the relevant registry, if we have one.
	 *
	 * @param world The world being unloaded.
	 */
	public static void onWorldUnloaded(LevelAccessor world) {
		if (registries.containsKey(world)) {
			registries.get(world).onWorldUnloaded();
			registries.remove(world);
		}
	}

	/**
	 * Call to mark a controller as dirty. Dirty means that parts have
	 * been added or removed this tick.
	 *
	 * @param world      The world containing the multiblock
	 * @param controller The dirty controller
	 */
	public static void addDirtyController(LevelAccessor world, IMultiblockControllerInternal controller) {
		if (registries.containsKey(world)) {
			registries.get(world).addDirtyController(controller);
		} else {
			throw new IllegalArgumentException("Adding a dirty controller to a world that has no registered controllers!");
		}
	}

	/**
	 * Call to mark a controller as dead. It should only be marked as dead
	 * when it has no connected parts. It will be removed after the next world tick.
	 *
	 * @param world      The world formerly containing the multiblock
	 * @param controller The dead controller
	 */
	public static void addDeadController(LevelAccessor world, IMultiblockControllerInternal controller) {
		if (registries.containsKey(world)) {
			registries.get(world).addDeadController(controller);
		} else {
			Forestry.LOGGER.warn("Controller {} in world {} marked as dead, but that world is not tracked! Controller is being ignored.", controller.hashCode(), world);
		}
	}

	/**
	 * @param world The world whose controllers you wish to retrieve.
	 * @return An unmodifiable set of controllers active in the given world.
	 */
	public static Set<IMultiblockControllerInternal> getControllersFromWorld(LevelAccessor world) {
		if (registries.containsKey(world)) {
			return registries.get(world).getControllers();
		}
		return ImmutableSet.of();
	}

	/// *** PRIVATE HELPERS *** ///
	//TODO refactor to getOrDefault
	private static MultiblockWorldRegistry getOrCreateRegistry(Level world) {
		if (registries.containsKey(world)) {
			return registries.get(world);
		} else {
			MultiblockWorldRegistry newRegistry = new MultiblockWorldRegistry(world);
			registries.put(world, newRegistry);
			return newRegistry;
		}
	}

}
