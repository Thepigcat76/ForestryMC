/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.genetics;

import java.util.Collection;

import net.minecraft.network.chat.MutableComponent;

import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;

public interface IClimateHelper {
	/**
	 * Determines whether the given temperature and humidity are within the given constraints.
	 *
	 * @param temperature The temperature to test.
	 * @param humidity    The humidity to test.
	 * @param baseTemp    Base temperature for the test.
	 * @param tolTemp     Temperature tolerance to apply.
	 * @param baseHumid   Base humidity for the test.
	 * @param tolHumid    Humidity tolerance to apply.
	 * @return true if both temperature and humidity fit the given constraints.
	 */
	boolean isWithinLimits(EnumTemperature temperature, EnumHumidity humidity,
		EnumTemperature baseTemp, EnumTolerance tolTemp,
		EnumHumidity baseHumid, EnumTolerance tolHumid);

	boolean isWithinLimits(EnumTemperature temperature, EnumTemperature baseTemp, EnumTolerance tolTemp);

	boolean isWithinLimits(EnumHumidity humidity, EnumHumidity baseHumid, EnumTolerance tolHumid);

	/**
	 * Gets a collection of humidities which fit the given parameters.
	 *
	 * @param preferred Base humidity from which to measure.
	 * @param tolerance Tolerance to apply to the base humidity.
	 * @return A collection of humidities which fall within the given parameters.
	 */
	Collection<EnumHumidity> getToleratedHumidity(EnumHumidity preferred, EnumTolerance tolerance);

	/**
	 * Gets a collection of temperatures which fit the given parameters.
	 *
	 * @param preferred Base temperature from which to measure.
	 * @param tolerance Tolerance to apply to the base temperatures.
	 * @return A collection of temperatures which fall within the given parameters.
	 */
	Collection<EnumTemperature> getToleratedTemperature(EnumTemperature preferred, EnumTolerance tolerance);

	/**
	 * Gets a localized, human readable string for the given temperature.
	 *
	 * @param temperature Temperature to generate the string for.
	 * @return A localized, human readable string for the given temperature.
	 */
	MutableComponent toDisplay(EnumTemperature temperature);

	/**
	 * Gets a localized, human readable string for the given humidity.
	 *
	 * @param humidity Humidity to generate the string for.
	 * @return A localized, human readable string for the given humidity.
	 */
	MutableComponent toDisplay(EnumHumidity humidity);
}