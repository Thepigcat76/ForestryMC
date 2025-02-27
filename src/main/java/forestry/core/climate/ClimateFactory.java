package forestry.core.climate;

import forestry.api.climate.IClimateFactory;
import forestry.api.climate.IClimateHousing;
import forestry.api.climate.IClimateListener;
import forestry.api.climate.IClimateTransformer;
import forestry.api.core.ILocatable;

public enum ClimateFactory implements IClimateFactory {
	INSTANCE;

	@Override
	public IClimateTransformer createTransformer(IClimateHousing housing) {
		return new ClimateTransformer(housing);
	}

	@Override
	public IClimateListener createListener(ILocatable locatable) {
		return new ClimateListener(locatable);
	}
}
