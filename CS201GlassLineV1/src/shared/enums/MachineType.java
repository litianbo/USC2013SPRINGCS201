
package shared.enums;

/**
 * Enum to keep track of what type of
 * machine the machine is.
 */
public enum MachineType
{

	// Offline Machines
	CROSS_SEAMER
	{
		public String toString()
		{
			return "Cross Seamer";
		}
	},
	DRILL
	{
		public String toString()
		{
			return "Drill";
		}
	},
	GRINDER
	{
		public String toString()
		{
			return "Grinder";
		}
	},
	MANUAL_BREAKOUT
	{
		public String toString()
		{
			return "Manual Breakout";
		}
	},

	// Online Machines
	CUTTER
	{
		public String toString()
		{
			return "Cutter";
		}
	},
	WASHER
	{
		public String toString()
		{
			return "Washer";
		}
	},
	UV_LAMP
	{
		public String toString()
		{
			return "UV Lamp";
		}
	},
	OVEN
	{
		public String toString()
		{
			return "Oven";
		}
	},
	PAINT
	{
		public String toString()
		{
			return "Paint";
		}
	},
	BREAKOUT
	{
		public String toString()
		{
			return "Breakout";
		}
	}
}
