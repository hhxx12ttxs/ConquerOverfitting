package gen;

public class Hero {
	private Class type;
	private Player player;
	private String armor;
	private String trait;
	private String spec;
	private String talent;
	private int rank;
	private int xp;
	private Code code;
	private int[] CharInfo; 
	private boolean COB;
	private boolean PCC;
	private boolean LSA;
	private boolean MOH;
	private boolean KEY;
	private int REM;
	private String gun;
	private int experience;
	private int LevelUnlocked = 0;
	//0=pin(11) 1=insane(10) 2=hard(9) 3=normal(6) 4=default(0)
	//1=? 2=class 3=armor 4=trait 5=spec 6=talent 7=(4-levelunlocked) 8=rank 9=? 10=KEY 11=MOH 12=PCC 13=COB 14=LSA 15=REM 
	public Hero()
	{
		code = new Code(this);
		this.CharInfo = new int[16];
	}
	public void setRankcap(String cap) {
		if (cap.compareTo("12")==0)
			this.LevelUnlocked = 0;
		else if (cap.compareTo("11")==0)
			this.LevelUnlocked = 0;
		else if (cap.compareTo("10")==0)
			this.LevelUnlocked = 1;
		else if (cap.compareTo("9")==0)
			this.LevelUnlocked = 2;
		else if (cap.compareTo("6")==0)
			this.LevelUnlocked = 3;
		else if (cap.compareTo("3")==0)
			this.LevelUnlocked = 4;
		
	}
	
	public void setType(String type) {
		this.type = new Class(type);
		// class
		if (type.compareTo("Sniper") == 0)
			CharInfo[2] = 0;
		else if (type.compareTo("Medic") == 0)
			CharInfo[2] = 1;
		else if (type.compareTo("Tactician") == 0)
			CharInfo[2] = 2;
		else if (type.compareTo("Psychologist") == 0)
			CharInfo[2] = 3;
		else if (type.compareTo("Heavy Ordinance") == 0)
			CharInfo[2] = 4;
		else if (type.compareTo("Demolitions") == 0)
			CharInfo[2] = 5;
		else if (type.compareTo("Cyborg") == 0)
			CharInfo[2] = 6;
		else if (type.compareTo("Pyrotechnician") == 0)
			CharInfo[2] = 7;
		else if (type.compareTo("Watchman") == 0) {
			if (gun.compareTo("Laser Rifle") == 0)
				CharInfo[2] = 8;
			else if (gun.compareTo("Gatling Laser") == 0)
				CharInfo[2] = 9;
			else
				this.code.setValid(false);
		} else if (type.compareTo("Maverick") == 0) {
			if (gun.compareTo("Assault Rifle") == 0)
				CharInfo[2] = 10;
			else if (gun.compareTo("Sniper Rifle") == 0)
				CharInfo[2] = 11;
			else if (gun.compareTo("Chaingun") == 0)
				CharInfo[2] = 12;
			else if (gun.compareTo("Rocket Launcher") == 0)
				CharInfo[2] = 13;
			else if (gun.compareTo("Flamethrower") == 0)
				CharInfo[2] = 14;
			else
				this.code.setValid(false);
		} else if (type.compareTo("Tech Ops") == 0)
			CharInfo[2] = 15;
		else if (type.compareTo("Umbrella Clone") == 0)
			CharInfo[2] = 16;
		else if (type.compareTo("Random") == 0)
			CharInfo[2] = (int) (Math.random()*17);
		else
			this.code.setValid(false);
	}
	public Class getType() {
		return type;
	}
	public void setPlayer(String name) {
		this.player = new Player(name);
	}
	public Player getPlayer() {
		return player;
	}
	public void setArmor(String armor) {
		this.armor = armor;
		if (armor.compareTo("Light") == 0)
			CharInfo[3] = 0;
		else if (armor.compareTo("Medium") == 0)
			CharInfo[3] = 1;
		else if (armor.compareTo("Heavy") == 0)
			CharInfo[3] = 2;
		else if (armor.compareTo("Advanced") == 0)
			CharInfo[3] = 3;
		else if (trait.compareTo("Random") == 0)
			CharInfo[4] = (int) (Math.random()*4);
		else
			this.code.setValid(false);
	}
	public String getArmor() {
		return armor;
	}
	public void setTrait(String trait) {
		this.trait = trait;
		if (trait.compareTo("Skilled") == 0)
			CharInfo[4] = 0;
		else if (trait.compareTo("Gifted") == 0)
			CharInfo[4] = 1;
		else if (trait.compareTo("Survivalist") == 0)
			CharInfo[4] = 2;
		else if (trait.compareTo("Dragoon") == 0)
			CharInfo[4] = 3;
		else if (trait.compareTo("Acrobat") == 0)
			CharInfo[4] = 4;
		else if (trait.compareTo("Swift Learner") == 0)
			CharInfo[4] = 5;
		else if (trait.compareTo("Healer") == 0)
			CharInfo[4] = 6;
		else if (trait.compareTo("Flower Child") == 0)
			CharInfo[4] = 7;
		else if (trait.compareTo("Chem Reliant") == 0)
			CharInfo[4] = 8;
		else if (trait.compareTo("Rad Resistant") == 0)
			CharInfo[4] = 9;
		else if (trait.compareTo("Gadgeteer") == 0)
			CharInfo[4] = 10;
		else if (trait.compareTo("Prowler") == 0)
			CharInfo[4] = 11;
		else if (trait.compareTo("Energizer") == 0)
			CharInfo[4] = 12;
		else if (trait.compareTo("Pack Rat") == 0)
			CharInfo[4] = 13;
		else if (trait.compareTo("Engineer") == 0)
			CharInfo[4] = 14;
		else if (trait.compareTo("Reckless") == 0)
			CharInfo[4] = 15;
		else if (trait.compareTo("Random") == 0)
			CharInfo[4] = (int) (Math.random()*16);
		else 
			this.code.setValid(false);
			
	}
	public String getTrait() {
		return trait;
	}
	public void setSpec(String spec) {
		this.spec = spec;

		if (spec.compareTo("Weaponry") == 0)
			CharInfo[5] = 0;
		else if (spec.compareTo("Power Armor") == 0)
			CharInfo[5] = 1;
		else if (spec.compareTo("Energy Cells") == 0)
			CharInfo[5] = 2;
		else if (spec.compareTo("Cybernetics") == 0)
			CharInfo[5] = 3;
		else if (spec.compareTo("Triage") == 0)
			CharInfo[5] = 4;
		else if (spec.compareTo("Chemistry") == 0)
			CharInfo[5] = 5;
		else if (spec.compareTo("Leadership") == 0)
			CharInfo[5] = 6;
		else if (spec.compareTo("Robotics") == 0)
			CharInfo[5] = 7;
		else if (spec.compareTo("Espionage") == 0)
			CharInfo[5] = 8;
		else if (spec.compareTo("Random") == 0)
			CharInfo[5] = (int) (Math.random()*9);
		else
			this.code.setValid(false);
	}
	public String getSpec() {
		return spec;
	}
	public void setTalent(String talent) {
		this.talent = talent;
		// talent
		if (talent.compareTo("Courage") == 0)
			CharInfo[6] = 0;
		else if (talent.compareTo("Wiring") == 0)
			CharInfo[6] = 1;
		else if (talent.compareTo("Running") == 0)
		{
			if (this.type.getType().compareTo("Heavy Ordinance")==0)
				CharInfo[6] = 6;
			else
				CharInfo[6] = 2;
		}
		else if (talent.compareTo("Spotting") == 0)
			CharInfo[6] = 3;
		else if (talent.compareTo("Toughness") == 0)
		{
			if (this.type.getType().compareTo("Pyrotechnician")==0)
				CharInfo[6] = 1;
			else 
				CharInfo[6] = 4;
		}
		else if (talent.compareTo("Tinkering") == 0)
		{
			CharInfo[6] = 5;
		}
		else if (talent.compareTo("Hacking") == 0)
		{
			if (this.type.getType().compareTo("Maverick")==0)
				CharInfo[6] = 4;
			else 
				CharInfo[6] = 6;
		}
		else
			this.code.setValid(false);
	}
	public String getTalent() {
		return talent;
	}
	public void setRank(String rank) {
		int temp = 0;
		if (rank.compareTo("12") == 0) {
			temp = 12;
			this.setExperience((this.rank - 1) * 2500);
		} else if (rank.compareTo("11") == 0) {
			temp = 11;
			this.setKEY(false);
			this.setMOH(false);
			this.setExperience((this.rank - 1) * 2500);
		} else if (rank.compareTo("10") == 0) {
			temp = 10;
			this.setKEY(false);
			this.setMOH(false);
			this.setExperience((this.rank - 1) * 2500 + this.xp);
		} else if (rank.compareTo("9") == 0) {
			temp = 9;
			this.setKEY(false);
			this.setMOH(false);
			this.setPCC(false);
			this.setExperience((this.rank - 1) * 2500 + this.xp);
		} else if (rank.compareTo("8") == 0) {
			temp = 8;
			this.setKEY(false);
			this.setMOH(false);
			this.setPCC(false);
			this.setExperience((this.rank - 1) * 2500 + this.xp);
		} else if (rank.compareTo("7") == 0) {
			temp = 7;
			this.setKEY(false);
			this.setMOH(false);
			this.setPCC(false);
			this.setExperience((this.rank - 1) * 2500 + this.xp);
		} else if (rank.compareTo("6") == 0) {
			temp = 6;
			this.setKEY(false);
			this.setMOH(false);
			this.setPCC(false);
			this.setExperience((this.rank - 1) * 2500 + this.xp);
		} else if (rank.compareTo("5") == 0) {
			temp = 5;
			this.setKEY(false);
			this.setMOH(false);
			this.setPCC(false);
			this.setExperience((this.rank - 1) * 2500 + this.xp);
		} else if (rank.compareTo("4") == 0) {
			temp = 4;
			this.setKEY(false);
			this.setMOH(false);
			this.setPCC(false);
			this.setExperience((this.rank - 1) * 2500 + this.xp);
		} else if (rank.compareTo("3") == 0) {
			temp = 3;
			this.setKEY(false);
			this.setMOH(false);
			this.setPCC(false);
			this.setExperience((this.rank - 1) * 2500 + this.xp);
		} else if (rank.compareTo("2") == 0) {
			temp = 2;
			this.setKEY(false);
			this.setMOH(false);
			this.setPCC(false);
			this.setExperience((this.rank - 1) * 2500 + this.xp);
		} else if (rank.compareTo("1") == 0) {
			temp = 1;
			this.setKEY(false);
			this.setMOH(false);
			this.setPCC(false);
			this.setExperience((this.rank - 1) * 2500 + this.xp);
		} else
			this.code.setValid(false);
		this.rank = temp;
	}
	public int getRank() {
		return rank;
	}
	public void setXp(int xp) {
		this.xp = xp;
		this.setExperience((this.rank - 1) * 2500 + this.xp);
	}
	public int getXp() {
		return xp;
	}
	public void setCode(Code code) {
		this.code = code;
	}
	public String getCode() {
		return code.getCode(this);
	}
	public void setCharInfo(int[] charInfo) {
		CharInfo = charInfo;
	}
	public int[] getCharInfo() {
		return CharInfo;
	}
	public void setCOB(boolean cOB) {
		this.COB = cOB;
		if (COB) 
			CharInfo[13] = 1;
		else
			CharInfo[13] = 0;
	}
	public boolean isCOB() {
		return COB;
	}
	public void setPCC(boolean pCC) {
		this.PCC = pCC;
		if (PCC) 
			CharInfo[12] = 1;
		else
			CharInfo[12] = 0;
	}
	public boolean isPCC() {
		return PCC;
	}
	public void setLSA(boolean LSA) 
	{
		this.LSA = LSA;
		if (LSA) 
			CharInfo[14] = 1;
		else
			CharInfo[14] = 0;
	}
	public boolean isLSA() 
	{
		return LSA;
	}
	public void setMOH(boolean MOH) 
	{
		this.MOH = MOH;
		if (MOH) 
			CharInfo[11] = 1;
	    else
	    	CharInfo[11] = 0;	    
	}
	public boolean isMOH() 
	{
		return MOH;
	}
	public void setKEY(boolean KEY) {
		this.KEY = KEY;
		if (KEY)
			CharInfo[10] = 1;
	    else
	    	CharInfo[10] = 0;
	}
	public boolean isKEY() {
		return KEY;
	}
	public void setREM(int REM) {
		this.REM = REM;
		if (REM==0) 
			CharInfo[15] = 0;
	    else if(REM==1)
	    	CharInfo[15] = 1;
	    else if(REM==2)
	    	CharInfo[15] = 2;
	    else if(REM==3)
	    	CharInfo[15] = 3;
	    else
	    	this.code.setValid(false);
	}
	public int getREM() 
	{
		return REM;
	}
	public void setGun(String gun) 
	{
		this.gun = gun;
	}
	public String getGun() 
	{
		return gun;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public int getExperience() {
		return experience;
	}

	public void setLevelUnlocked(int levelUnlocked) {
		LevelUnlocked = levelUnlocked;
	}

	public int getLevelUnlocked() {
		return LevelUnlocked;
	}

}

