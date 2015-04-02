package net.lotrek.dynarec.devices;

import java.util.ArrayList;

public class InterruptController extends MemorySpaceDevice
{
	private Structure controlStructure = new Structure(Byte.class, Byte.class, Integer.class, Byte.class);
	private Register[] instanceRegisters;
	private ArrayList<int[]> monitorList = new ArrayList<>();
	private ArrayList<Long> cachedValues = new ArrayList<>();
	
	public int getOccupationLength()
	{
		return controlStructure.getLength();
	}

	public void executeDeviceCycle()
	{
		if(((byte)instanceRegisters[0].getValue() & 0xf) == 1)
		{
			if(((byte)instanceRegisters[1].getValue() >> 7 & 1) == 0) //add
			{
				monitorList.set((byte)instanceRegisters[1].getValue() & 0x7F, new int[]{(int)instanceRegisters[2].getValue(), (byte)instanceRegisters[3].getValue()});
				cachedValues.set((byte)instanceRegisters[1].getValue() & 0x7F, (Long) Register.getTypeForBytes((byte)instanceRegisters[3].getValue(), this.getProcessor().getMemory(), (int)instanceRegisters[2].getValue()));
			}
			else //remove
				monitorList.remove((byte)instanceRegisters[1].getValue() & 0x7F);
			
			instanceRegisters[0].setValue(Byte.class, (byte)0);
		}
		
		int i = 0;
		for (int[] area : monitorList)
		{
			if((Long) Register.getTypeForBytes(area[1], this.getProcessor().getMemory(), area[0]) != cachedValues.get(i))
			{
				cachedValues.set(i, (Long) Register.getTypeForBytes(area[1], this.getProcessor().getMemory(), area[0]));
				this.getProcessor().interrupt(0, i, area[0]);
			}
			
			i++;
		}
	}

	public void initializeDevice()
	{
		instanceRegisters = controlStructure.getInstance(getOccupationAddr(), this.getProcessor().getMemory());
	}

}
