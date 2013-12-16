
package shared.enums;

/**
 * Enum that describes each one of the operations that can be done to the glass
 */
public enum ComponentOperations
{
	NONE,
	BREAKOUT,
	MANUALBREAKOUT
	{
		public String toString()
		{
			return "MANUAL BREAKOUT";
		}
	},
	CROSSSEAMER
	{
		public String toString()
		{
			return "CROSS SEAMER";
		}
	},
	CUTTER,
	DONE,
	DRILL,
	GRINDER,
	OVEN,
	PAINT,
	SHATTERED,
	UVLAMP,
	WASHER
}
