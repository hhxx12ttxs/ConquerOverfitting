package damage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import teambuilder.Abilities;
import teambuilder.Ability;
import teambuilder.Field;
import teambuilder.FieldCondition;
import teambuilder.Gem;
import teambuilder.Gender;
import teambuilder.Item;
import teambuilder.Items;
import teambuilder.Move;
import teambuilder.Moves;
import teambuilder.Plate;
import teambuilder.Pokemon;
import teambuilder.ResistBerry;
import teambuilder.SpeciesList;
import teambuilder.Status;
import teambuilder.TimeSpaceOrb;
import teambuilder.Type;
import teambuilder.TypeBoostingItem;

public class DamageCalc
{
	public static final double divisor = 0x1000;
	
	public static final Ability[] moldbreakers = new Ability[]{Abilities.moldbreaker,Abilities.turboblaze,Abilities.teravolt};
	public static final ArrayList<Ability> waterimmune = new ArrayList<>();
	public static final ArrayList<Ability> eleimmune = new ArrayList<>();
	public static final ArrayList<Move> bullets = new ArrayList<>();
	public static final ArrayList<Move> sounds = new ArrayList<>();
	public static final ArrayList<Move> reckless = new ArrayList<>();
	public static final ArrayList<Move> ironfist = new ArrayList<>();
	public static final ArrayList<Move> megalauncher = new ArrayList<>();
	public static final ArrayList<Move> strongjaw = new ArrayList<>();
	public static final ArrayList<Item> megastones = new ArrayList<>();
	
	static
	{
		Collections.addAll(waterimmune,Abilities.dryskin, Abilities.waterabsorb,Abilities.stormdrain);
		Collections.addAll(eleimmune,Abilities.lightningrod, Abilities.voltabsorb,Abilities.motordrive);
		Collections.addAll(bullets,Moves.acidspray,Moves.aurasphere,Moves.barrage,Moves.bulletseed,Moves.eggbomb,Moves.electroball,
				Moves.focusblast,Moves.gyroball,Moves.iceball,Moves.magnetbomb,Moves.mistball,Moves.mudbomb,Moves.octazooka,Moves.rockwrecker,
				Moves.searingshot,Moves.seedbomb,Moves.shadowball,Moves.sludgebomb,Moves.weatherball,Moves.zapcannon);
		Collections.addAll(sounds,Moves.boomburst,Moves.bugbuzz,Moves.chatter,Moves.confide,Moves.disarmingvoice,Moves.echoedvoice,
				Moves.grasswhistle,Moves.growl,Moves.hypervoice,Moves.metalsound,Moves.nobleroar,Moves.partingshot,Moves.perishsong,
				Moves.relicsong,Moves.roar,Moves.round,Moves.screech,Moves.sing,Moves.snarl,Moves.snore,Moves.supersonic,Moves.uproar);
		Collections.addAll(reckless,Moves.bravebird,Moves.doubleedge,Moves.flareblitz,Moves.headcharge,
				Moves.headsmash,Moves.highjumpkick,Moves.jumpkick,Moves.submission,Moves.takedown,Moves.volttackle,
				Moves.wildcharge,Moves.woodhammer);
		Collections.addAll(ironfist,Moves.bulletpunch,Moves.cometpunch,Moves.dizzypunch,Moves.drainpunch,Moves.dynamicpunch,Moves.firepunch,
				Moves.focuspunch,Moves.hammerarm,Moves.icepunch,Moves.machpunch,Moves.megapunch,Moves.meteormash,Moves.poweruppunch,
				Moves.shadowpunch,Moves.skyuppercut,Moves.thunderpunch);
		Collections.addAll(megalauncher,Moves.aurasphere,Moves.darkpulse,Moves.dragonpulse,Moves.healpulse,Moves.waterpulse);
		Collections.addAll(strongjaw,Moves.bite,Moves.crunch,Moves.firefang,Moves.icefang,Moves.poisonfang,Moves.thunderfang);
		
	}
	
	public static double[] getSinglesDamage(Pokemon attacker, Pokemon defender, Move move, Field field, boolean isCrit)
	{

		
		//formula:
		//{[(2 * level + 10) / 250] * attack/defense * base + 2 }*mods
		//mods are stab, type effectiveness, crit, held items, and a random component
		
		Ability attackab = attacker.getAbility();
		Ability defenseab = defender.getAbility();
		
		field = checkAirLock(attackab,defenseab,field);
		attacker = checkKlutz(attacker);
		defender = checkKlutz(defender);
		
		String id = attacker.getAbility().getID();
		String did = defender.getAbility().getID();
		
		if(did.equals("intimidate"))
		{
			if(id.equals("contrary") || id.equals("defiant"))
					attacker.increaseAttack(1);
			else if(id.equals("clearbody") || id.equals("whitesmoke")
					|| id.equals("hypercutter")){}
			else if(id.equals("simple"))
				attacker.decreaseAttack(2);
			else attacker.decreaseAttack(1);
		}
		
		if(id.equals("download"))
		{
			if(defender.getStats()[4] >= defender.getStats()[2])
			{
				attacker.increaseAttack(1);
			}
			else attacker.increaseSpAttack(1);
		}
		
		if(id.equals("forecast"))
		{
			attacker = checkForecast(attacker,field);
		}
		if(did.equals("forecast"))
		{
			defender = checkForecast(defender,field);
		}
		
		field = checkInfiltrator(attacker, field);
		
		for(Ability a : moldbreakers)
		{
			if(a.equals(attacker.getAbility()))
			{
				defender.setAbility(Abilities.nothing);
				did = "nothing";
			}
		}
		
		
		if((isCrit && (did.equals("shellarmor")) || (did.equals("battlearmor"))))
		{
			isCrit = false;
		}
		
		////////////////////////////
		//  changing move types   //
		////////////////////////////
		
		String mid = move.getID();
		
		if(mid.equals("weatherball"))
		{
			move.setType(field.getSide1().contains(FieldCondition.Sun) ? Type.Fire
	                : field.getSide1().contains(FieldCondition.Rain) ? Type.Water
	                : field.getSide1().contains(FieldCondition.Sandstorm) ? Type.Rock
	                : field.getSide1().contains(FieldCondition.Hail) ? Type.Ice
	                : Type.Normal);
		}
		
		else if(mid.equals("judgment") && attacker.getItem() instanceof Plate)
		{
			move.setType(((Plate)attacker.getItem()).boostedType());
		}
		
		else if(mid.equals("seismictoss") || mid.equals("nightshade"))
		{
			int level = attacker.getLevel();
			if(attackab.getID().equals("parentalbond"))
				return new double[]{2 * level,2 * level};
			return new double[]{level,level};	
		}
		
		double weakness = 1;
		//instead of just checking for two, i'm now checking for every type since trick-or-treat now exists
		for(Type t : defender.getTypes())
		{
		weakness *= getMoveEffectiveness(move,t,field.isForesight() || defenseab.getID().equals("scrappy"),field.isGravity());
		}
		if(weakness == 0)
			return new double[]{0,0};
		
		if((did.equals("wonderguard") && weakness <= 1) ||
				(move.getType().equals(Type.Grass) && did.equals("sapsipper")) || 
				(move.getType().equals(Type.Fire) && did.equals("flashfire")) ||
				(move.getType().equals(Type.Water) && waterimmune.contains(defenseab)) ||
				(move.getType().equals(Type.Electric) && eleimmune.contains(defenseab)) ||
			    (move.getType().equals(Type.Ground) && !field.isGravity() && defenseab.getID().equals("levitate")) ||
				 (bullets.contains(move) && did.equals("bulletproof")) ||
				 (sounds.contains(move) && did.equals("soundproof")))
			{
					return new double[]{0,0};
			}
		
		

		if(move.getType().equals(Type.Ground) && !field.isGravity() && defender.getItem().getID().equals("airballoon"))
			 return new double[]{0,0};
		
		
			////////////////////////////
			//  altering base power   //
			////////////////////////////
		
		double attackspeed = getFinalSpeed(attacker,field);
		double defendspeed = getFinalSpeed(defender,field);
		
		if(mid.equals("electroball"))
		{
			double r = Math.floor(attackspeed / defendspeed);
			if(r >= 4)
				move.setBasePower(150);
			else if (r >= 3)
				move.setBasePower(120);
			else if (r >= 2)
				move.setBasePower(80);
			else move.setBasePower(60);
		}
		else if(mid.equals("gyroball"))
		{
			move.setBasePower((int) Math.round(Math.min(150, Math.floor(25 * defendspeed / attackspeed))));
		}
		else if(mid.equals("payback"))
		{
			if(attacker.getStats()[5] > defender.getStats()[5])
				move.setBasePower(50);
			else move.setBasePower(100);
		}
		else if(mid.equals("punishment"))
		{
			move.setBasePower(Math.min(200, 60 + 20 * defender.countBoosts()));
		}
		else if(mid.equals("storedpower"))
		{
			move.setBasePower(20 + 20 * attacker.countBoosts());
		}
		else if(mid.equals("hex"))
		{
			//as opposed to absolute values
			//we are multiplying relative, because in gen5 it was base 50
			//and in gen6 it was 100
			if(!defender.isHealthy())
				move.setBasePower(move.getBasePower()  * 2);
		}
		else if(mid.equals("acrobatics"))
		{
			if(attacker.getItem() == null || attacker.getItem().getID().equals("flyinggem"))
				move.setBasePower(110);
			else move.setBasePower(55);
		}
		else if(mid.equals("wakeupslap"))
		{
			if(!defender.getStatus().contains(Status.Sleep))
				move.setBasePower(move.getBasePower()  * 2);
		}
		else if(mid.equals("weatherball"))
		{
			if(field.hasWeather())
				move.setBasePower(move.getBasePower() * 2);
		}
		else if(mid.equals("eruption") || mid.equals("waterspout"))
		{
			move.setBasePower(Math.max(1, (int)Math.floor(150 * attacker.getHPFraction())));
		}
		else if(mid.equals("reversal") || mid.equals("flail"))
		{
			double hp = attacker.getHPFraction();
			if(hp > 70)
				move.setBasePower(20);
			else if(hp > 35 && hp <= 70)
				move.setBasePower(40);
			else if(hp > 20 && hp <= 35)
				move.setBasePower(80);
			else if(hp > 10 && hp <= 20)
				move.setBasePower(100);
			else if(hp > 5 && hp <= 10)
				move.setBasePower(150);
			else move.setBasePower(200);
		}
		
		int basepower = move.getBasePower();

		//since a lot of moves are 0 base power, there's no reason to go through the formula
		//when they can't possibly do damage
		if(move.getBasePower() == 0)
			return new double[]{0,0};
		
		///////////////////////////////
		// changing bp based on mods //
		///////////////////////////////
		
		ArrayList<Integer> bpmod = new ArrayList<>();
		
		if(id.equals("techinician") && basepower <= 60 ||
				id.equals("flareboost") && attacker.isBurned() && !move.isPhysical() ||
				id.equals("toxicboost") && attacker.isPoisoned() && move.isPhysical())
			bpmod.add(0x1800);
		
		else if(id.equals("analytic") && attacker.getStats()[5] < defender.getStats()[5])
			bpmod.add(0x14CD);
		
		else if(id.equals("sandforce") && field.isSandstorm())
		{
			ArrayList<Type> types = new ArrayList<Type>();
			types.add(Type.Ground);
			types.add(Type.Rock);
			types.add(Type.Steel);
			if(types.contains(move.getType()))
				bpmod.add(0x14CD);
		}
		
		else if(id.equals("reckless") && reckless.contains(move) ||
				id.equals("ironfist") && ironfist.contains(move))
		{
			bpmod.add(0x1333);
		}
		else if(id.equals("sheerforce") && move.hasSecondaryEffect())
			bpmod.add(0x14CD);
		
		if(did.equals("heatproof") && move.getType().equals(Type.Fire))
			bpmod.add(0x800);
		else if(did.equals("dryskin") && move.getType().equals(Type.Fire))
			bpmod.add(0x1400);
		
		Item i = attacker.getItem();
		if(i instanceof TypeBoostingItem)
		{
			TypeBoostingItem tbi = (TypeBoostingItem)i;
			if(move.getType().equals(tbi.boostedType()))
				bpmod.add(0x1333);
		}
		else if(i instanceof Gem)
		{
			bpmod.add(0x14CD);
		}
		else if(i instanceof Plate)
		{
			Plate plate = (Plate)i;
			if(move.getType().equals(plate.boostedType()))
				bpmod.add(0x1333);
		}
		else if(i.getID().equals("muscleband") && move.isPhysical() ||
				i.getID().equals("wiseglasses") && !move.isPhysical())
			bpmod.add(0x1199);
		else if(i instanceof TimeSpaceOrb)
		{
			TimeSpaceOrb tsi = (TimeSpaceOrb)i;
			if(attacker.getSpecies().equals(tsi.boostedPokemon()))
			{
				if(attacker.getsStabFrom(move))
					bpmod.add(0x1333);
			}
		}
		
		if(field.isHelpingHand())
			bpmod.add(0x1800);
		
		if(attackab.getID().equals("pixilate") && move.getType().equals(Type.Normal))
		{
			move.setType(Type.Fairy);
			bpmod.add(0x14CD);
		}
		else if(attackab.getID().equals("refrigerate") && move.getType().equals(Type.Normal))
		{
			move.setType(Type.Ice);
			bpmod.add(0x14CD);
		}
		else if(attackab.getID().equals("aerilate") && move.getType().equals(Type.Normal))
		{
			move.setType(Type.Flying);
			bpmod.add(0x14CD);
		}
		else if(attackab.getID().equals("normalize"))
			move.setType(Type.Normal);
		else if(attackab.getID().equals("darkaura") || defenseab.getID().equals("darkaura"))
		{
			if(move.getType().equals(Type.Dark))
			{
				if(attackab.getID().equals("aurabreak") || defenseab.getID().equals("aurabreak"))
				{
					bpmod.add(0xAAA);
				}
				else bpmod.add(0x1555);
			}
		}
		else if(id.equals("fairyaura") || defenseab.getID().equals("fairyaura"))
		{
			if(move.getType().equals(Type.Fairy))
			{
				if(attackab.getID().equals("aurabreak") || defenseab.getID().equals("aurabreak"))
				{
					bpmod.add(0xAAA);
				}
				else bpmod.add(0x1555);
			}
		}
		else if(id.equals("toughclaws") && move.isContact())
		{
			bpmod.add(0x1547);
		}
		
		if(mid.equals("facade") && !attacker.isHealthy() ||
				mid.equals("brine") && defender.getHPFraction() <= 0.5 ||
				mid.equals("venoshock") && defender.isPoisoned())
			bpmod.add(0x2000);
		else if (mid.equals("solarbeam") && field.hasWeather() && !field.isSun())
			bpmod.add(0x800);
		else if(mid.equals("knockoff") && defender.getItem() != null ||
				(defender.getSpecies().getID().equals("giratinaorigin") && defender.getItem().getID().equals("griseousorb")) ||
				defender.getSpecies().getID().matches("arceus") && i instanceof Plate)
			bpmod.add(0x1800);
		
		basepower = Math.max(1, pokeRound(basepower * chainMods(bpmod) / divisor));
		
		////////////
		// ATTACK //
		////////////
		
	
		double attack = 1;
		double defense = 1;
		if(mid.equals("foulplay"))
		{
			attack = defender.getStats()[1] * defender.getBoosts()[0];
		}
		else if(move.isPhysical())
		{
			if(defenseab.getID().equals("unaware"))
				attack = attacker.getStats()[1];
			else if(isCrit && attacker.getBoosts()[0] < 1)
				attack = attacker.getStats()[1];
			else attack = attacker.getStats()[1] * attacker.getBoosts()[0];
		}
		else
		{
			if(defenseab.getID().equals("unaware"))
				attack = attacker.getStats()[3];
			else if(isCrit && attacker.getBoosts()[2] < 1)
				attack = attacker.getStats()[3];
			else attack = attacker.getStats()[3] * attacker.getBoosts()[2];
		}
		
		if(id.equals("hustle") && move.isPhysical())
		{
			attack = pokeRound(attack * 3/2.0);
		}
		
		ArrayList<Integer> attackmod = new ArrayList<>();
		
		if(did.equals("thickfat") && (move.getType().equals(Type.Fire) || move.getType().equals(Type.Ice)))
			attackmod.add(0x800);
		
		if(id.equals("guts") && !attacker.isHealthy() && move.isPhysical() ||
				id.equals("overgrow") && attacker.getHPFraction() <= 1/3.0 && move.getType().equals(Type.Grass) ||
				id.equals("blaze") && attacker.getHPFraction() <= 1/3.0 && move.getType().equals(Type.Fire) ||
				id.equals("torrent") && attacker.getHPFraction() <= 1/3.0 && move.getType().equals(Type.Water) ||
				id.equals("swarm") && attacker.getHPFraction() <= 1/3.0 && move.getType().equals(Type.Bug))
		{
			attackmod.add(0x1800);
		}
		
		else if(id.equals("flashfireactivated") && move.getType().equals(Type.Fire))
			attackmod.add(0x1800);
		
		else if(id.equals("solarpower") && field.isSun() && !move.isPhysical() ||
				id.equals("flowergift") && field.isSun() && move.isPhysical())
			attackmod.add(0x1800);
		
		else if(id.equals("defeatist") && attacker.getHPFraction() <= 0.5 ||
				id.equals("slowstart") && move.isPhysical())
			attackmod.add(0x800);
			
		else if(id.equals("hugepower") || id.equals("purepower") && move.isPhysical())
			attackmod.add(0x2000);
		
		String iid = attacker.getItem().getID();
		
		if(iid.equals("thickclub") && (attacker.getSpecies().getID().equals("cubone") || attacker.getSpecies().getID().equals("marowak"))
				&& move.isPhysical() || 
				(iid.equals("deepseatooth") && attacker.getSpecies().getID().equals("clamperl")
				&& !move.isPhysical()) || (iid.equals("lightball") && attacker.getSpecies().getID().equals("pikachu")))
				attackmod.add(0x2000);
		
		else if(iid.equals("souldew") && attacker.getSpecies().getID().matches("lati[oa]s") && !move.isPhysical() ||
				(iid.equals("choiceband") && move.isPhysical()) ||
				(iid.equals("choicespecs") && !move.isPhysical()))
		{
			attackmod.add(0x1800);
		}
		
		attack = Math.max(1,pokeRound(attack * chainMods(attackmod)/divisor));
		
		if(move.usesPhysDef())
		{
			if(attackab.getID().equals("unaware"))
				defense = attacker.getStats()[2];
			if(isCrit && defender.getBoosts()[1] > 1)
				defense = defender.getStats()[2];
			else defense = defender.getStats()[2] * defender.getBoosts()[1];
		}
		else
		{
			if(attackab.getID().equals("unaware"))
				defense = attacker.getStats()[4];
			if(isCrit && defender.getBoosts()[3] > 1)
				defense = defender.getStats()[4];
			else defense = defender.getStats()[4] * defender.getBoosts()[3];
		}
		
		if(field.isSandstorm() && (defender.isType(Type.Rock)))
		{
			defense = pokeRound(defense * 3 / 2.0);
		}
		
		ArrayList<Integer> defmod = new ArrayList<>();
		if(did.equals("marvelscale") && !defender.isHealthy() &&
				move.isPhysical())
			defmod.add(0x1800);
		
		else if(did.equals("flowergift") && field.isSun() && !move.isPhysical())
			defmod.add(0x1800);
		
		String diid = defender.getItem().getID();
		
		if(diid.equals("deepseascale") && defender.getSpecies().getName().equals("clamperl") && !move.isPhysical())
			defmod.add(0x1800);
		else if(diid.equals("metalpowder") && defender.getSpecies().getName().equals("ditto"))
			defmod.add(0x1800);
		else if(diid.equals("souldew") && defender.getSpecies().getName().matches("lati[oa]s"))
			defmod.add(0x1800);
		else if(diid.equals("assaultvest") && !move.isPhysical())
			defmod.add(0x1800);
		else if(diid.equals("eviolite"))
			defmod.add(0x1800);
		
		defense = Math.max(1,pokeRound(defense * chainMods(defmod) / divisor));

		double baseDamage = Math.floor(Math.floor((Math.floor((2 * attacker.getLevel())/5 + 2) * basepower * attack) / defense) / 50 + 2);
		if((field.isSun() && move.getType().equals(Type.Fire)) ||
				(field.isRain() && move.getType().equals(Type.Water)))
		{
			System.out.println("ă");
			baseDamage = pokeRound(baseDamage * 0x1800/divisor);
		}
		if((field.isSun() && move.getType().equals(Type.Water)) ||
				(field.isRain() && move.getType().equals(Type.Fire)))
		{
			baseDamage = pokeRound(baseDamage * 0x800/divisor);
		}
		
		if(isCrit)
			baseDamage = Math.floor(baseDamage * 1.5);
		
		int stabmod = 0x1000;
		if(attacker.getsStabFrom(move))
		{
			if(id.equals("adaptability"))
				stabmod = 0x2000;
			else stabmod = 0x1800;
		}
		else if(id.equals("protean"))
			stabmod = 0x1800;
		
		boolean applyBurn = (attacker.isBurned() && move.isPhysical() && id.equals("guts") && !mid.equals("facade"));
		
		ArrayList<Integer> finalmod = new ArrayList<>();
		
		if(field.isReflect() && move.isPhysical() && !isCrit)
			finalmod.add(0x800);
		if(field.Lightscreen() && !move.isPhysical() && !isCrit)
			finalmod.add(0x800);
		if(did.equals("multiscale") && defender.getHPFraction() == 1)
			finalmod.add(0x800);
		if(id.equals("tintedlens") && weakness < 1)
		{
			finalmod.add(0x2000);
		}
		else if (id.equals("sniper") && isCrit)
			finalmod.add(0x1800);
		
		if((did.equals("solidrock") || did.equals("filter") && weakness > 1))
			finalmod.add(0xC00);
		
		if(iid.equals("expertbelt") && weakness > 1)
		{
			finalmod.add(0x1333);
		}
		else if(iid.equals("lifeorb"))
		{
			finalmod.add(0x14CC);
		}
		
		if(i instanceof ResistBerry)
		{
			ResistBerry rb = (ResistBerry)i;
			if(move.getType().equals(rb.boostedType()) && !did.equals("unnerve"))
			{
				finalmod.add(0x800);
			}
		}
		if(did.equals("furcoat") && move.isPhysical())
			finalmod.add(0x800);
		
		
		double finalmods = chainMods(finalmod)/divisor;
		System.out.println("basepower:"+basepower);
		System.out.println("attack:"+attack);
		System.out.println("defense:"+defense);
		System.out.println("basedamage:"+baseDamage);
		System.out.println("finalmods:"+finalmods);
		System.out.println("weakness:"+weakness);
		System.out.println("stab:"+stabmod/divisor);
		
		double[] damagearray = new double[16];
		for(int j = 0; j < 16; j ++)
		{
			damagearray[j] = Math.floor(baseDamage * (85 + j) / 100);
			damagearray[j] = pokeRound(damagearray[j] * stabmod/divisor);
			damagearray[j] = Math.floor(damagearray[j] * weakness);
			if(applyBurn)
			{
				damagearray[j] = Math.floor(damagearray[j] / 2);
			}
			damagearray[j] = Math.max(1,damagearray[j]);
			damagearray[j] = pokeRound(damagearray[j] * finalmods);
			
			if(id.equals("parentalbond") && move.hitCount().length == 1 && move.hitCount()[0] == 1)
			{
				damagearray[j] = Math.floor(damagearray[j] * 3/ 2.0);
			}
		}
		
		
		return damagearray;
	}
	
	private static Field checkAirLock(Ability a, Ability b, Field f)
	{
		if(!a.equals(Abilities.airlock) && !a.equals(Abilities.cloudnine) && !b.equals(Abilities.airlock) && !b.equals(Abilities.cloudnine))
			return f;
		//we know that weather needs to be removed
		f.removeWeather();
		return f;
	}
	
	private static Pokemon checkKlutz(Pokemon a)
	{
		if(a.getAbility().equals(Abilities.klutz))
			a.setItem(null);
		return a;
	}
	
	private static Field checkInfiltrator(Pokemon a, Field f)
	{
		if(a.getAbility().equals(Abilities.infiltrator))
			f.removeScreens();
		return f;
	}
	
	private static double getMoveEffectiveness(Move move, Type type, boolean hitsGhost, boolean isGravity)
	{
		if(hitsGhost && type.equals(Type.Ghost) && (move.getType().equals(Type.Normal) || move.getType().equals(Type.Fighting)))
			return 1;
		if(isGravity && type.equals(Type.Flying) && move.getType().equals(Type.Ground))
			return 1;
		if(move.getID().equals("freezedry") && type.equals(Type.Water))
			return 2;
		if(move.getID().equals("flyingpress"))
			return Type.getEffectiveness(Type.Fighting, type) * Type.getEffectiveness(Type.Flying,type);
		return Type.getEffectiveness(move.getType(), type);
	}
	
	public static int chainMods(ArrayList<Integer> mods)
	{
		int m = 0x1000;
		for(int i : mods)
		{
			if(i != 0x1000)
			{
				m = ((m * i) + 0x800) >> 12;
			}
		}
		return m;
	}
	
	public static int getFinalSpeed(Pokemon poke, Field field)
	{
		double speed = poke.getStats()[5] * poke.getBoosts()[4];
		
		String id = poke.getItem().getID();
		if(id.equals("choicescarf"))
			speed = Math.floor(speed * 1.5);
		else if(id.equals("machobrace") || id.equals("ironball"))
			speed = Math.floor(speed / 2.0);
		
		id = poke.getAbility().getID();
		if(id.equals("chlorophyll") && field.isSun() ||
				id.equals("sandrush") && field.isSandstorm() ||
				id.equals("swiftswim") && field.isRain())
			speed *= 2;
		System.out.println("Speed is: "+pokeRound(speed));
		return pokeRound(speed);
	}
	
	public static int pokeRound(double num)
	{
		return (num % 1 > 0.5) ? (int)Math.ceil(num) : (int)Math.floor(num);
	}
	
	public static Pokemon checkForecast(Pokemon poke, Field field)
	{
		if(poke.getAbility().getID().equals("forecast") && (poke.getSpecies().getID().equals("castform")))
		{
			if(field.isSun())
				poke.setTypes(new Type[]{Type.Fire});
			else if(field.isRain())
				poke.setTypes(new Type[]{Type.Water});
			else if(field.isHail())
				poke.setTypes(new Type[]{Type.Ice});
			else poke.setTypes(new Type[]{Type.Normal});
		}
		return poke;
	}
	
	public static void main(String[]args)
	{
		Pokemon deus = new Pokemon(SpeciesList.deoxysspeed, Abilities.pressure, Gender.Female, 100, Items.choicescarf, new int[]{304,203,306,226,127,0}, 304, new double[]{1,1,1,1,4,1,1}, null);
		Pokemon snorlacks = new Pokemon(SpeciesList.snorlax, Abilities.waterabsorb, Gender.Male, 100, Items.ironball, new int[]{497,256,234,166,300,86}, 497, new double[]{1,1,1,1,1,0.25,1}, null);
		Field field = new Field();
		Move move = Moves.payback;
		System.out.println(Arrays.toString(getSinglesDamage(snorlacks,deus,move,field,false)));
		
	}
}

