package com.lambelly.lambnes.platform.mappers;

import org.apache.log4j.Logger;

import com.lambelly.lambnes.LambNes;
import com.lambelly.lambnes.platform.cpu.NesCpuMemory;
import com.lambelly.lambnes.platform.ppu.PPUNameTable;
import com.lambelly.lambnes.util.BitUtils;

public class MMC1Mapper extends Mapper0 implements Mapper
{
	private Integer[] bank = new Integer[8];
	private int bankIndex = bank.length - 1;
	private int mirroring = 0;
	private int prgRomBankMode = 0;
	private int chrRomBankMode = 0;
	private int[] prgRom = null; 
	private int[] prgRam = null;
	private boolean prgRamChipEnabled = false;
	public static final int MIRRORING_ONE_SCREEN_LOWER_BANK = 0;
	public static final int MIRRORING_ONE_SCREEN_UPPER_BANK = 1;
	public static final int MIRRORING_VERTICAL = 2;
	public static final int PRG_ROM_32K_SWITCH = 0;
	public static final int PRG_ROM_16K_0xC000_SWITCH = 2;
	public static final int PRG_ROM_16K_0x8000_SWITCH = 3;
	public static final int CHR_ROM_8k_SWITCH = 0;
	public static final int CHR_ROM_4k_SWITCH = 1;
	public static final int MIRRORING_HORIZONTAL = 3;
	
	NesCpuMemory cpuMemory = null;
	Logger logger = Logger.getLogger(MMC1Mapper.class);
	
	public MMC1Mapper(NesCpuMemory cpuMemory) 
	{
		this.setCpuMemory(cpuMemory);
	}

	public void setMemoryFromHexAddress(int address, int value) throws IllegalStateException
	{
		super.setMemoryFromHexAddress(address, value);
		
		// prg ram
		if (address >=0x6000 && address <= 0x7FFF)
		{
			logger.info("prg-ram enabled: " + this.isPrgRamChipEnabled() + " value: " + value + " address: " + address);
		}
		
		/*
          registers
		  8000-9FFFh will access register 0, 
		  A000-BFFFh register 1, 
		  C000-DFFFh register 2,
		  E000-FFFFh register 3.  
		*/
		if (address >= 0x8000 && address <= 0xFFFF)
		{
			// check for reset bit
			if (value >> 7 == 1)
			{
				this.reset();
			}
			else
			{
				// only use bit 0
				//logger.info("value is: " + value);
				value = (value & 1);
				//logger.info("first bit of value is: " + value);
				
				this.setValueToBank(value);
				
				if (this.getBankIndex() == 2)
				{
					if (address >= 0x8000 && address <= 0x9FFF)
					{
						//logger.info("write to register 0: " + this.getValueFromBank());
						
						// parse register
						this.setMirroring(this.getValueFromBank() & 0x3);
						this.setPrgRomBankMode((this.getValueFromBank() & 0xC) >>2);
						this.setChrRomBankMode((this.getValueFromBank() & 0x10) >> 4);
						
						// mirroring
						PPUNameTable nameTableA = new PPUNameTable();
						PPUNameTable nameTableB = new PPUNameTable();
						 
						if (this.getMirroring() == MMC1Mapper.MIRRORING_HORIZONTAL)
						{
							LambNes.getPlatform().getPpuMemory().setNameTable0(nameTableA);
							LambNes.getPlatform().getPpuMemory().setNameTable1(nameTableA);
							LambNes.getPlatform().getPpuMemory().setNameTable2(nameTableB);
							LambNes.getPlatform().getPpuMemory().setNameTable3(nameTableB);
						} 
						else if (this.getMirroring() == MMC1Mapper.MIRRORING_VERTICAL)
						{
							LambNes.getPlatform().getPpuMemory().setNameTable0(nameTableA);
							LambNes.getPlatform().getPpuMemory().setNameTable1(nameTableB);
							LambNes.getPlatform().getPpuMemory().setNameTable2(nameTableA);
							LambNes.getPlatform().getPpuMemory().setNameTable3(nameTableB);			
						}
						
						//logger.info(this);
					}
					else if (address >= 0xA000 && address <= 0xBFFF)
					{
						//logger.info("write to register 1: " + this.getValueFromBank() + " chrRomBankMode: " + this.getChrRomBankMode() + " sprite table address: " + LambNes.getPlatform().getPpu().getPpuControlRegister().getSpritePatternTableAddress());
						
						if (this.getChrRomBankMode() == MMC1Mapper.CHR_ROM_4k_SWITCH)
						{
							//logger.info("4k transfer mode");
							// 4k mode
							System.arraycopy(this.getPrgRom(), (this.getValueFromBank() * 4096), LambNes.getPlatform().getPpuMemory().getPatternTable0(), 0, 4096);
						}
						else
						{
							//logger.info("8k transfer mode");
							// transfering entire 8k page into ppu memory.
							int index = this.getValueFromBank() * 4096;
							//Object src, int srcPos, Object dest, int destPos, int length
							System.arraycopy(this.getPrgRom(), index, LambNes.getPlatform().getPpuMemory().getPatternTable0(), 0, 4096);
							System.arraycopy(this.getPrgRom(), index + 4096, LambNes.getPlatform().getPpuMemory().getPatternTable1(), 0, 4096);
						}
					}
					else if (address >= 0xC000 && address <= 0xDFFF)
					{
						//logger.info("write to register 2: " + this.getValueFromBank() + " chrRomBankMode: " + this.getChrRomBankMode());
						
						if (this.getChrRomBankMode() == MMC1Mapper.CHR_ROM_4k_SWITCH)
						{
							System.arraycopy(this.getPrgRom(), (this.getValueFromBank() * 4096), LambNes.getPlatform().getPpuMemory().getPatternTable1(), 0, 4096);
						}
						else
						{
							// 8k mode -- does nothing. NOTHING.
						}
					}
					else if (address >= 0xE000 && address <= 0xFFFF)
					{
						logger.info("write to register 3: " + this.getValueFromBank() + " - " + Integer.toBinaryString(this.getValueFromBank()));
						if (BitUtils.isBitSet(value, 4))
						{
							this.setPrgRamChipEnabled(false);
						}
						else
						{
							this.setPrgRamChipEnabled(true);
						}
						
						if (this.getPrgRomBankMode() == MMC1Mapper.PRG_ROM_32K_SWITCH)
						{
							//logger.info("prg rom 32k switch");
							throw new IllegalStateException("32k switch unimplemented");
						}
						else if (this.getPrgRomBankMode() == MMC1Mapper.PRG_ROM_16K_0x8000_SWITCH)
						{
							//Object src, int srcPos, Object dest, int destPos, int length
							//logger.info("8000: value from bank: " + this.getValueFromBank() + " prgRomLength: " + this.getPrgRom().length);
							System.arraycopy(this.getPrgRom(), (this.getValueFromBank() * 16384), this.getCpuMemory().getMemory(), 0x8000, 16384);
						}
						else if (this.getPrgRomBankMode() == MMC1Mapper.PRG_ROM_16K_0xC000_SWITCH)
						{
							//logger.info("C000: value from bank: " + this.getValueFromBank() + " prgRomLength: " + this.getPrgRom().length);
							System.arraycopy(this.getPrgRom(), (this.getValueFromBank() * 16384), this.getCpuMemory(), 0xC000, 16384);
						}
					}
					
					this.reset();
				}
			}
		}
	}
	
	private void reset()
	{
		// reset bank
		
		// reset bank index
		this.setBankIndex(this.getBank().length - 1);
	}
	
	public void setProgramInstructions(int[] programInstructions)
	{
		this.setPrgRom(programInstructions);
		
		//Object src, int srcPos, Object dest, int destPos, int length
		// load bank0 -- use first page from prgRom
		System.arraycopy(programInstructions, 0, this.getCpuMemory().getMemory(), NesCpuMemory.PRG_ROM_BASE, 16384);
		
		// load bank1 -- use last page from prgRom
		System.arraycopy(programInstructions, (programInstructions.length - 16384), this.getCpuMemory().getMemory(), (NesCpuMemory.PRG_ROM_BASE + 16384), 16384);
	}

	public void setValueToBank(int value)
	{
		//logger.info("setting value: " + value + " to bank index: " + this.getBankIndex());
		this.getBank()[this.getBankIndex()] = value;
		this.decrementBankIndex();
	}
	
	public String toString()
	{
		return "bankIndex: " + this.getBankIndex() + "\n" +
			"mirroring: " + this.getMirroring()  + "\n" +
			"prgRomBankMode: " + this.getPrgRomBankMode() + "\n" +
			"chrRomBankMode: " + this.getChrRomBankMode() + "\n";
	}
	
	public int getValueFromBank()
	{
		return (this.getBank()[3] << 4) | (this.getBank()[4] << 3) | (this.getBank()[5] << 2) | (this.getBank()[6] << 1) | this.getBank()[7];
	}
	
	public Integer[] getBank()
    {
    	return bank;
    }

	public void setBank(Integer[] bank)
    {
    	this.bank = bank;
    }
	
	public void decrementBankIndex()
	{
		this.bankIndex--;
	}

	public int getBankIndex()
    {
    	return bankIndex;
    }

	public void setBankIndex(int bankIndex)
    {
    	this.bankIndex = bankIndex;
    }

	public NesCpuMemory getCpuMemory()
    {
    	return cpuMemory;
    }

	public void setCpuMemory(NesCpuMemory cpuMemory)
    {
    	this.cpuMemory = cpuMemory;
    }

	public int getMirroring()
    {
    	return mirroring;
    }

	public void setMirroring(int mirroring)
    {
    	this.mirroring = mirroring;
    }

	public int getPrgRomBankMode()
    {
    	return prgRomBankMode;
    }

	public void setPrgRomBankMode(int prgRomBankMode)
    {
    	this.prgRomBankMode = prgRomBankMode;
    }

	public int getChrRomBankMode()
    {
    	return chrRomBankMode;
    }

	public void setChrRomBankMode(int chrRomBankMode)
    {
    	this.chrRomBankMode = chrRomBankMode;
    }

	public int[] getPrgRom()
    {
    	return prgRom;
    }

	public void setPrgRom(int[] prgRom)
    {
    	this.prgRom = prgRom;
    }

	public boolean isPrgRamChipEnabled()
    {
    	return prgRamChipEnabled;
    }

	public void setPrgRamChipEnabled(boolean prgRamChipEnabled)
    {
    	this.prgRamChipEnabled = prgRamChipEnabled;
    }
}

