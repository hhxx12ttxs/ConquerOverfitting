/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.l2emuproject.gameserver.world.object;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.LoginServerThread;
import net.l2emuproject.gameserver.Shutdown;
import net.l2emuproject.gameserver.Shutdown.DisableType;
import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.datatables.CharNameTable.ICharacterInfo;
import net.l2emuproject.gameserver.datatables.CharTemplateTable;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.datatables.GmListTable;
import net.l2emuproject.gameserver.datatables.HeroSkillTable;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.datatables.NobleSkillTable;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.PetDataTable;
import net.l2emuproject.gameserver.datatables.RecordTable;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.entity.ai.L2CharacterAI;
import net.l2emuproject.gameserver.entity.ai.L2PlayerAI;
import net.l2emuproject.gameserver.entity.ai.L2SummonAI;
import net.l2emuproject.gameserver.entity.appearance.PcAppearance;
import net.l2emuproject.gameserver.entity.base.ClassId;
import net.l2emuproject.gameserver.entity.base.ClassLevel;
import net.l2emuproject.gameserver.entity.base.Experience;
import net.l2emuproject.gameserver.entity.base.Race;
import net.l2emuproject.gameserver.entity.base.SubClass;
import net.l2emuproject.gameserver.entity.effects.PcEffects;
import net.l2emuproject.gameserver.entity.itemcontainer.Inventory;
import net.l2emuproject.gameserver.entity.itemcontainer.ItemContainer;
import net.l2emuproject.gameserver.entity.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.entity.itemcontainer.PcRefund;
import net.l2emuproject.gameserver.entity.itemcontainer.PcWarehouse;
import net.l2emuproject.gameserver.entity.itemcontainer.PetInventory;
import net.l2emuproject.gameserver.entity.player.PlayerBirthday;
import net.l2emuproject.gameserver.entity.player.PlayerCertification;
import net.l2emuproject.gameserver.entity.player.PlayerCustom;
import net.l2emuproject.gameserver.entity.player.PlayerDuel;
import net.l2emuproject.gameserver.entity.player.PlayerEventData;
import net.l2emuproject.gameserver.entity.player.PlayerFish;
import net.l2emuproject.gameserver.entity.player.PlayerHenna;
import net.l2emuproject.gameserver.entity.player.PlayerObserver;
import net.l2emuproject.gameserver.entity.player.PlayerOlympiad;
import net.l2emuproject.gameserver.entity.player.PlayerRecipe;
import net.l2emuproject.gameserver.entity.player.PlayerSettings;
import net.l2emuproject.gameserver.entity.player.PlayerTeleportBookmark;
import net.l2emuproject.gameserver.entity.player.PlayerTransformation;
import net.l2emuproject.gameserver.entity.player.PlayerVitality;
import net.l2emuproject.gameserver.entity.reference.ClearableReference;
import net.l2emuproject.gameserver.entity.reference.ImmutableReference;
import net.l2emuproject.gameserver.entity.shot.CharShots;
import net.l2emuproject.gameserver.entity.shot.PcShots;
import net.l2emuproject.gameserver.entity.skills.PcSkills;
import net.l2emuproject.gameserver.entity.stat.CharStat;
import net.l2emuproject.gameserver.entity.stat.PcStat;
import net.l2emuproject.gameserver.entity.status.CharStatus;
import net.l2emuproject.gameserver.entity.status.PcStatus;
import net.l2emuproject.gameserver.entity.view.CharLikeView;
import net.l2emuproject.gameserver.entity.view.PcView;
import net.l2emuproject.gameserver.events.global.blockchecker.HandysBlockCheckerManager;
import net.l2emuproject.gameserver.events.global.dimensionalrift.DimensionalRiftManager;
import net.l2emuproject.gameserver.events.global.fortsiege.Fort;
import net.l2emuproject.gameserver.events.global.fortsiege.FortManager;
import net.l2emuproject.gameserver.events.global.fortsiege.FortSiegeManager;
import net.l2emuproject.gameserver.events.global.olympiad.Olympiad;
import net.l2emuproject.gameserver.events.global.sevensigns.SevenSigns;
import net.l2emuproject.gameserver.events.global.sevensigns.SevenSignsFestival;
import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.events.global.siege.CastleManager;
import net.l2emuproject.gameserver.events.global.siege.L2SiegeClan;
import net.l2emuproject.gameserver.events.global.siege.Siege;
import net.l2emuproject.gameserver.events.global.siege.SiegeManager;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWard;
import net.l2emuproject.gameserver.handler.ItemHandler;
import net.l2emuproject.gameserver.handler.SkillHandler;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.l2emuproject.gameserver.handler.skillhandlers.TakeCastle;
import net.l2emuproject.gameserver.handler.skillhandlers.TakeFort;
import net.l2emuproject.gameserver.items.ItemsAutoDestroy;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.manager.AntiFeedManager;
import net.l2emuproject.gameserver.manager.instances.Instance;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.network.Disconnection;
import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.clientpackets.ConfirmDlgAnswer.AnswerHandler;
import net.l2emuproject.gameserver.network.serverpackets.AbstractNpcInfo;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.CameraMode;
import net.l2emuproject.gameserver.network.serverpackets.ChangeWaitType;
import net.l2emuproject.gameserver.network.serverpackets.CharInfo;
import net.l2emuproject.gameserver.network.serverpackets.ConfirmDlg;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.network.serverpackets.EffectInfoPacket.EffectInfoPacketList;
import net.l2emuproject.gameserver.network.serverpackets.EtcStatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExGetOnAirShip;
import net.l2emuproject.gameserver.network.serverpackets.ExManagePartyRoomMember;
import net.l2emuproject.gameserver.network.serverpackets.ExOlympiadUserInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import net.l2emuproject.gameserver.network.serverpackets.ExSetCompassZoneCode;
import net.l2emuproject.gameserver.network.serverpackets.ExSpawnEmitter;
import net.l2emuproject.gameserver.network.serverpackets.ExStartScenePlayer;
import net.l2emuproject.gameserver.network.serverpackets.ExStorageMaxCount;
import net.l2emuproject.gameserver.network.serverpackets.FriendList;
import net.l2emuproject.gameserver.network.serverpackets.FriendStatusPacket;
import net.l2emuproject.gameserver.network.serverpackets.GameGuardQuery;
import net.l2emuproject.gameserver.network.serverpackets.GetOnVehicle;
import net.l2emuproject.gameserver.network.serverpackets.HennaInfo;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.ItemList;
import net.l2emuproject.gameserver.network.serverpackets.L2GameServerPacket;
import net.l2emuproject.gameserver.network.serverpackets.MagicEffectIcons;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.MyTargetSelected;
import net.l2emuproject.gameserver.network.serverpackets.NicknameChanged;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.PartySmallWindowUpdate;
import net.l2emuproject.gameserver.network.serverpackets.PartySpelled;
import net.l2emuproject.gameserver.network.serverpackets.PetInventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import net.l2emuproject.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.l2emuproject.gameserver.network.serverpackets.PledgeSkillList;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreListBuy;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreListSell;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreManageListSell;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreMsgSell;
import net.l2emuproject.gameserver.network.serverpackets.QuestList;
import net.l2emuproject.gameserver.network.serverpackets.RecipeShopMsg;
import net.l2emuproject.gameserver.network.serverpackets.RecipeShopSellList;
import net.l2emuproject.gameserver.network.serverpackets.RelationChanged;
import net.l2emuproject.gameserver.network.serverpackets.Ride;
import net.l2emuproject.gameserver.network.serverpackets.SetupGauge;
import net.l2emuproject.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.ShortCutInit;
import net.l2emuproject.gameserver.network.serverpackets.SkillCoolTime;
import net.l2emuproject.gameserver.network.serverpackets.SkillList;
import net.l2emuproject.gameserver.network.serverpackets.Snoop;
import net.l2emuproject.gameserver.network.serverpackets.SocialAction;
import net.l2emuproject.gameserver.network.serverpackets.SpecialCamera;
import net.l2emuproject.gameserver.network.serverpackets.StaticPacket;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.StopMove;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.TargetSelected;
import net.l2emuproject.gameserver.network.serverpackets.TargetUnselected;
import net.l2emuproject.gameserver.network.serverpackets.TradeDone;
import net.l2emuproject.gameserver.network.serverpackets.TradeOtherDone;
import net.l2emuproject.gameserver.network.serverpackets.TradeStart;
import net.l2emuproject.gameserver.network.serverpackets.TutorialCloseHtml;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.gameserver.network.serverpackets.ValidateLocation;
import net.l2emuproject.gameserver.services.attribute.Attributes;
import net.l2emuproject.gameserver.services.blocklist.BlockList;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.clan.L2ClanMember;
import net.l2emuproject.gameserver.services.crafting.L2ManufactureList;
import net.l2emuproject.gameserver.services.crafting.RecipeService;
import net.l2emuproject.gameserver.services.cursedweapons.CursedWeapon;
import net.l2emuproject.gameserver.services.cursedweapons.CursedWeaponsService;
import net.l2emuproject.gameserver.services.duel.Duel;
import net.l2emuproject.gameserver.services.duel.DuelService;
import net.l2emuproject.gameserver.services.friendlist.L2FriendList;
import net.l2emuproject.gameserver.services.party.L2Party;
import net.l2emuproject.gameserver.services.party.L2PartyRoom;
import net.l2emuproject.gameserver.services.party.PartyRoomManager;
import net.l2emuproject.gameserver.services.quest.L2Marker;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.recommendation.RecommendationService;
import net.l2emuproject.gameserver.services.shortcuts.L2ShortCut;
import net.l2emuproject.gameserver.services.transactions.L2Request;
import net.l2emuproject.gameserver.services.transactions.TradeList;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.gameserver.skills.SkillUsageRequest;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.skills.conditions.ConditionGameTime;
import net.l2emuproject.gameserver.skills.conditions.ConditionPlayerHp;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.skills.funcs.Func;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillSummon;
import net.l2emuproject.gameserver.skills.skilllearn.L2CertificationSkillsLearn;
import net.l2emuproject.gameserver.skills.skilllearn.L2SkillLearn;
import net.l2emuproject.gameserver.skills.skilllearn.L2TransformSkillLearn;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.restriction.AvailableRestriction;
import net.l2emuproject.gameserver.system.restriction.ObjectRestrictions;
import net.l2emuproject.gameserver.system.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.system.taskmanager.AbstractIterativePeriodicTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.AttackStanceTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.LeakTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.MovementController;
import net.l2emuproject.gameserver.system.taskmanager.PacketBroadcaster.BroadcastMode;
import net.l2emuproject.gameserver.system.taskmanager.SQLQueue;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.system.time.GameTimeController;
import net.l2emuproject.gameserver.system.util.Broadcast;
import net.l2emuproject.gameserver.system.util.FloodProtector;
import net.l2emuproject.gameserver.system.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.templates.chars.L2PcTemplate;
import net.l2emuproject.gameserver.templates.item.L2Armor;
import net.l2emuproject.gameserver.templates.item.L2ArmorType;
import net.l2emuproject.gameserver.templates.item.L2EtcItemType;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.templates.item.L2Weapon;
import net.l2emuproject.gameserver.templates.item.L2WeaponType;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.L2WorldRegion;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.knownlist.CharKnownList;
import net.l2emuproject.gameserver.world.knownlist.PcKnownList;
import net.l2emuproject.gameserver.world.mapregion.MapRegionManager;
import net.l2emuproject.gameserver.world.mapregion.TeleportWhereType;
import net.l2emuproject.gameserver.world.npc.L2PetData;
import net.l2emuproject.gameserver.world.object.instance.L2AirShipInstance;
import net.l2emuproject.gameserver.world.object.instance.L2BoatInstance;
import net.l2emuproject.gameserver.world.object.instance.L2ClassMasterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2CubicInstance;
import net.l2emuproject.gameserver.world.object.instance.L2DefenderInstance;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;
import net.l2emuproject.gameserver.world.object.instance.L2FestivalMonsterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2GuardInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2NpcInstance;
import net.l2emuproject.gameserver.world.object.instance.L2PetInstance;
import net.l2emuproject.gameserver.world.object.instance.L2StaticObjectInstance;
import net.l2emuproject.gameserver.world.object.instance.L2SummonInstance;
import net.l2emuproject.gameserver.world.object.instance.L2TamedBeastInstance;
import net.l2emuproject.gameserver.world.zone.L2JailZone;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.gameserver.world.zone.ZoneManager;
import net.l2emuproject.lang.L2Math;
import net.l2emuproject.lang.L2System;
import net.l2emuproject.lang.Replaceable;
import net.l2emuproject.network.mmocore.InvalidPacketException;
import net.l2emuproject.sql.SQLQuery;
import net.l2emuproject.tools.geometry.Point3D;
import net.l2emuproject.tools.random.Rnd;
import net.l2emuproject.util.ArrayBunch;
import net.l2emuproject.util.L2Arrays;
import net.l2emuproject.util.L2Collections;
import net.l2emuproject.util.SingletonList;
import net.l2emuproject.util.SingletonMap;

import org.apache.commons.lang.ArrayUtils;

/**
 * This class represents all player characters in the world.
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).<BR><BR>
 *
 * @version $Revision: 1.66.2.41.2.33 $ $Date: 2005/04/11 10:06:09 $
 */
public final class L2Player extends L2Playable implements ICharacterInfo
{
	public static final L2Player[] EMPTY_ARRAY = new L2Player[0];

	// Characters of an account
	private static final String RESTORE_CHARS_FOR_ACCOUNT		= "SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?";
	
	// Character Skill Reuse SQL String Definitions:
	private static final String RESTORE_SKILL_REUSES			= "SELECT skillId,reuseDelay,expiration FROM character_skill_reuses WHERE charId=?";
	private static final String ADD_SKILL_REUSE					= "INSERT INTO character_skill_reuses (charId,skillId,reuseDelay,expiration) VALUES (?,?,?,?)";
	private static final String DELETE_SKILL_REUSES				= "DELETE FROM character_skill_reuses WHERE charId=?";

	// Character Character SQL String Definitions:
	private static final String INSERT_CHARACTER				= "INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,fame,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,pledge_rank) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String	UPDATE_CHARACTER				= "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,fame=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,in_jail=?,jail_timer=?,newbie=?,nobless=?,pledge_rank=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,banchat_timer=?,char_name=?,death_penalty_level=?,vitality_points=?,bookmarkslot=? WHERE charId=?";
	private static final String	RESTORE_CHARACTER				= "SELECT account_name, charId, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, expBeforeDeath, sp, karma, fame, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, banchat_timer, newbie, nobless, pledge_rank, subpledge, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally, clan_join_expiry_time,clan_create_expiry_time,charViP,death_penalty_level,vitality_points,bookmarkslot FROM characters WHERE charId=?";

	// Character Subclass SQL String Definitions:
	private static final String	RESTORE_CHAR_SUBCLASSES			= "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE charId=? ORDER BY class_index ASC";
	private static final String	ADD_CHAR_SUBCLASS				= "INSERT INTO character_subclasses (charId,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	private static final String	UPDATE_CHAR_SUBCLASS			= "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE charId=? AND class_index =?";
	private static final String	DELETE_CHAR_SUBCLASS			= "DELETE FROM character_subclasses WHERE charId=? AND class_index=?";

	public static final byte		REQUEST_TIMEOUT					= 15;

	public static final byte		STORE_PRIVATE_NONE				= 0;
	public static final byte		STORE_PRIVATE_SELL				= 1;
	public static final byte		STORE_PRIVATE_BUY				= 3;
	public static final byte		STORE_PRIVATE_MANUFACTURE		= 5;
	public static final byte		STORE_PRIVATE_PACKAGE_SELL		= 8;

	/** The table containing all minimum level needed for each Expertise (None, D, C, B, A, S, S80, S84)*/
	private static final int[]	EXPERTISE_LEVELS				=
	{
		SkillTreeTable.getInstance().getExpertiseLevel(0), // NONE
		SkillTreeTable.getInstance().getExpertiseLevel(1), // D
		SkillTreeTable.getInstance().getExpertiseLevel(2), // C
		SkillTreeTable.getInstance().getExpertiseLevel(3), // B
		SkillTreeTable.getInstance().getExpertiseLevel(4), // A
		SkillTreeTable.getInstance().getExpertiseLevel(5), // S
		SkillTreeTable.getInstance().getExpertiseLevel(6), // S80
		SkillTreeTable.getInstance().getExpertiseLevel(7)  //S84
	};

	private static final int[] COMMON_CRAFT_LEVELS = { 5, 20, 28, 36, 43, 49, 55, 62 };

	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}
		
		public L2Player getPlayer()
		{
			return L2Player.this;
		}
		
		public void doPickupItem(L2Object object)
		{
			L2Player.this.doPickupItem(object);
		}
		
		public void doInteract(L2Character target)
		{
			L2Player.this.doInteract(target);
		}
		
		@Override
		public void doAttack(L2Character target)
		{
			super.doAttack(target);
			
			if (target.getActingPlayer() != null 
					&& getSiegeState() > 0 && isInsideZone(L2Zone.FLAG_SIEGE)
					&& target.getActingPlayer().getSiegeState() == getSiegeState()
					&& target.getActingPlayer() != L2Player.this 
					&& target.getActingPlayer().getSiegeSide() == getSiegeSide())
			{
				// 
				if (TerritoryWarManager.getInstance().isTWInProgress())
					sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
				else
					sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Cancel the recent fake-death protection instantly if the player attacks or casts spells
			setRecentFakeDeath(false);
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
			super.doCast(skill);
			
			// Cancel the recent fake-death protection instantly if the player attacks or casts spells
			setRecentFakeDeath(false);
			if (skill == null)
				return;
			if (!skill.isOffensive())
				return;
			
			switch (skill.getTargetType())
			{
				case TARGET_GROUND:
					return;
				default:
				{
					for (L2CubicInstance cubic : getCubics().values())
						if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
							cubic.doAction();
				}
					break;
			}
		}
	}

	private L2GameClient					_client;

	private final PcAppearance				_appearance;

	/** Sitting down and Standing up fix */
	private long							_lastSitStandRequest	= 0;

	/** The Identifier of the L2Player */
	private int								_charId					= 0x00030b7a;

	/** The Experience of the L2Player before the last Death Penalty */
	private long							_expBeforeDeath;

	/** The Karma of the L2Player (if higher than 0, the name of the L2Player appears in red) */
	private int								_karma;

	/** The number of player killed during a PvP (the player killed was PvP Flagged) */
	private int								_pvpKills;

	/** The PK counter of the L2Player (= Number of non PvP Flagged player killed) */
	private int								_pkKills;

	/** The Siege state of the L2Player */
	private byte							_siegeState				= SIEGE_STATE_NOT_INVOLVED;
	/** The id of castle/fort which the L2Player is registered for siege */
	private int								_siegeSide				= 0;
	private boolean							_isInSiege				= false;

	private int								_lastCompassZone;																	// The last compass zone update send to the client

	private boolean							_isIn7sDungeon			= false;

	private int								_subPledgeType			= 0;

	/** L2Player's pledge rank*/
	private int								_pledgeRank;

	/** Level at which the player joined the clan as an accedemy member*/
	private int								_lvlJoinedAcademy		= 0;

	/** The random number of the L2Player */
	//private static final Random _rnd = new Random();
	private int								_curWeightPenalty		= 0;

	private long							_deleteTimer;
	private PcInventory						_inventory;
	private PcWarehouse						_warehouse;
	private PcRefund 						_refund;
	private final PcSkills					_pcSkills = new PcSkills(this);

	/** True if the L2Player is sitting */
	private boolean							_waitTypeSitting;

	/** True if the L2Player is using the relax skill */
	private boolean							_relax;
	
	/** Boat and AirShip */
    private L2Vehicle 						_vehicle = null;
    private Point3D 						_inVehiclePosition;

	/** Last NPC Id talked on a quest */
	private int								_questNpcObject			= 0;

	/** Bitmask used to keep track of one-time/newbie quest rewards */
	private int								_newbie;

	/** The table containing all Quests began by the L2Player */
	private final Map<String, QuestState>	_quests					= new SingletonMap<String, QuestState>();

	private TradeList						_activeTradeList;
	private ItemContainer					_activeWarehouse;
	private L2ManufactureList				_createList;
	private TradeList						_sellList;
	private TradeList						_buyList;

	private L2Player[] _snoopers = L2Player.EMPTY_ARRAY; // List of GMs snooping this player
	private L2Player[] _snoopedPlayers = L2Player.EMPTY_ARRAY; // List of players being snooped by this GM

	/** The Private Store type of the L2Player (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5) */
	private int								_privatestore;
	private ClassId							_skillLearningClassId;

	private boolean							_isRidingStrider		= false;
	private boolean							_isRidingRedStrider		= false;
	private boolean							_isRidingHorse			= false;
	private boolean 						_isFlyingMounted 		= false;

	/** The L2Summon of the L2Player */
	private L2Summon						_summon					= null;
	/** The L2Decoy of the L2Player */
	private L2Decoy							_decoy					= null;
	/** The L2Trap of the L2Player */
	private L2Trap							_trap					= null;
	/** The L2Agathion of the L2Player */
	private int								_agathionId				= 0;
	// Apparently, a L2Player CAN have both a summon AND a tamed beast at the same time!!
	private L2TamedBeastInstance			_tamedBeast				= null;

	// Client radar
	private L2Marker							_radar;

	// These values are only stored temporarily
	private boolean							_lookingForParty;
	private boolean							_partyMatchingAllLevels;
	private int								_partyMatchingRegion;
	private L2PartyRoom						_partyRoom;
	private L2Party							_party;
	// Clan related attributes

	/** The Clan Identifier of the L2Player */
	private int								_clanId;

	/** The Clan object of the L2Player */
	private L2Clan							_clan;

	/** Apprentice and Sponsor IDs */
	private int								_apprentice				= 0;
	private int								_sponsor				= 0;

	private long							_clanJoinExpiryTime;
	private long							_clanCreateExpiryTime;

	private long							_onlineTime;
	private long							_onlineBeginTime;

	// GM Stuff
	private boolean							_isGm;
	private int								_accessLevel;

	private boolean							_messageRefusal			= false;													// Message refusal mode
	private boolean							_dietMode				= false;													// Ignore weight penalty
	private boolean							_tradeRefusal			= false;													// Trade refusal
	private boolean							_exchangeRefusal		= false;													// Exchange refusal

	// This is needed to find the inviting player for Party response
	// There can only be one active party request at once
	private L2Player						_activeRequester;
	private long							_requestExpireTime		= 0;
	private L2Request						_request;
	private L2ItemInstance					_arrowItem;
	private L2ItemInstance					_boltItem;

	// Used for protection after teleport
	private long							_protectEndTime			= 0;

	// Protects a char from agro mobs when getting up from fake death
	private long							_recentFakeDeathEndTime	= 0;

	/** The fists L2Weapon of the L2Player (used when no weapon is equipped) */
	private L2Weapon						_fistsWeaponItem;

	private long							_uptime;
	private final String					_accountName;

	private Map<Integer, String>			_chars;

	private int								_mountType;
	private int								_mountNpcId;
	private int 							_mountLevel;

	/** The current higher Expertise of the L2Player (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7)*/
	private int								_expertiseIndex;																	// Index in EXPERTISE_LEVELS
	private int								_expertiseWeaponPenalty;
	private int								_expertiseArmorPenalty;

	private boolean							_isEnchanting			= false;
	private L2ItemInstance					_activeEnchantItem		= null;
	private L2ItemInstance					_activeEnchantSupportItem = null;
	private L2ItemInstance					_activeEnchantAttrItem	= null;
	private long							_activeEnchantTimestamp = 0;

	public static final byte ONLINE_STATE_LOADED = 0;
	public static final byte ONLINE_STATE_ONLINE = 1;
	public static final byte ONLINE_STATE_DELETED = 2;

	private byte _isOnline = ONLINE_STATE_LOADED;

	protected boolean						_inventoryDisabled		= false;

	protected Map<Integer, L2CubicInstance>	_cubics					= new SingletonMap<Integer, L2CubicInstance>().shared();

	/** The L2NpcInstance corresponding to the last Folk wich one the player talked. */
	private L2Npc							_lastFolkNpc			= null;

	private int								_clanPrivileges			= 0;

	/** L2Player's pledge class (knight, Baron, etc.)*/
	private int								_pledgeClass			= 0;

	public int								_telemode				= 0;

	/** new loto ticket **/
	private final int						_loto[]					= new int[5];
	/** new race ticket **/
	private final int						_race[]					= new int[2];

	private BlockList						_blockList;
	private L2FriendList					_friendList;

	private int								_team					= 0;
	private int								_wantsPeace				= 0;

	// Death Penalty Buff Level
	private int								_deathPenaltyBuffLevel	= 0;

	// Self resurrect during siege
	private boolean							_charmOfCourage			= false;

	private boolean							_hero					= false;
	private boolean							_noble					= false;

	/** ally with ketra or varka related vars*/
	private int								_alliedVarkaKetra		= 0;

	/**
	 * IMO we don't need it, as we have FIFO packet execution.
	 */
	private final ReentrantLock 			_subclassLock = new ReentrantLock();
	/** The list of sub-classes this character has. */
	private Map<Integer, SubClass>			_subClasses;
	protected int							_baseClass;
	protected int							_activeClass;
	protected int							_classIndex				= 0;

	/** data for mounted pets */
	private int								_controlItemId;
	private L2PetData						_data;
	private int								_curFeed;
	protected Future<?>						_mountFeedTask;
	private ScheduledFuture<?>				_dismountTask;

	private long							_lastAccess;

	private ScheduledFuture<?>				_taskRentPet;
	private ScheduledFuture<?>				_taskWater;

	/** Bypass validations */
	private List<String>					 _validBypass;
	private List<String> 					_validBypass2;
	
	private List<String>					_validLink;

	/** The number of evaluation points obtained by this player */
	private int								_evalPoints;

	/** The number of evaluations this player can give */
	private int								_evaluations;

	/** List of players this player already evaluated */
	private final List<Integer>				_evaluated				= new SingletonList<Integer>();

	private boolean							_inCrystallize;

	private boolean							_inCraftMode;

	/** Store object used to summon the strider you are mounting **/
	private int								_mountObjectID			= 0;

	/** character VIP **/
	private boolean							_charViP				= false;

	private boolean							_inJail					= false;
	private long							_jailTimer				= 0;

	private boolean							_maried					= false;
	private int								_partnerId				= 0;
	private int								_coupleId				= 0;
	private boolean							_maryrequest			= false;
	private boolean							_maryaccepted			= false;

	private int								_clientRevision			= 0;

	/* Flag to disable equipment/skills while wearing formal wear **/
	private boolean							_IsWearingFormalWear	= false;

	private L2StaticObjectInstance			_throne;

	// Absorbed Souls
	private int								_souls					= 0;
	private ScheduledFuture<?>				_soulTask				= null;
	private int								_lastSoulConsume		= 0;

	// Force charges
	private int								_charges				= 0;
	private ScheduledFuture<?>				_chargeTask				= null;

	public int								_fame = 0;					// The Fame of this L2Player
	private ScheduledFuture<?>				_fameTask;

	private ScheduledFuture<?>				_teleportWatchdog;

	// Id of the afro hair cut
	private int								_afroId					= 0;

	private long							_lastTargetChange;
	private int								_lastTargetId;

	private boolean							_illegalWaiting;

	private long							_nextJumpTime;
	
	// extension management
	private PlayerTeleportBookmark 			_teleBookmarkExtension 		= null;
	private PlayerVitality 					_vitalityExtension 			= null;
	private PlayerCertification 			_certificationExtension 	= null;
	private PlayerBirthday 					_birthdayExtension 			= null;
	private PlayerTransformation 			_transformationExtension 	= null;
	private PlayerHenna 					_hennaExtension 			= null;
	private PlayerRecipe 					_recipeExtension			= null;
	private PlayerCustom 					_customExtension 			= null;
	private PlayerObserver 					_observerExtension 			= null;
	private PlayerOlympiad 					_olympiadExtension 			= null;
	private PlayerFish 						_fishExtension 				= null;
	private PlayerDuel						_duelExtension				= null;
	private PlayerSettings					_settingsExtension			= null;
	private PlayerEventData 				_playerEventData 			= null;

	@Override
	public final boolean isAllSkillsDisabled()
	{
		return super.isAllSkillsDisabled() || isTryingToSitOrStandup();
	}

	@Override
	public final boolean isAttackingDisabled()
	{
		return super.isAttackingDisabled() || _combatFlagEquipped || isTryingToSitOrStandup();
	}

	@Override
	public boolean isInProtectedAction()
	{
		return super.isInProtectedAction() || isTryingToSitOrStandup();
	}

	/**
	 * Create a new L2Player and add it in the characters table of the database.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a new L2Player with an account name </li>
	 * <li>Set the name, the Hair Style, the Hair Color and  the Face type of the L2Player</li>
	 * <li>Add the player in the characters table of the database</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2Player
	 * @param accountName The name of the L2Player
	 * @param name The name of the L2Player
	 * @param hairStyle The hair style Identifier of the L2Player
	 * @param hairColor The hair color Identifier of the L2Player
	 * @param face The face type Identifier of the L2Player
	 *
	 * @return The L2Player added to the database or null
	 *
	 */
	public static L2Player create(int objectId, L2PcTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face,
			boolean sex)
	{
		// Create a new L2Player with an account name
		PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
		L2Player player = new L2Player(objectId, template, accountName, app);

		// Set the name of the L2Player
		player.setName(name);

		// Set the base class ID to that of the actual class ID.
		player.setBaseClass(player.getClassId());

		// Kept for backwards compabitility.
		player.setNewbie(1);

		// Add the player in the characters table of the database
		boolean ok = player.createDb();

		if (!ok)
			return null;

		return player;
	}

	@Override
	public String getAccountName()
	{
		if (getClient() == null)
			return _accountName;
		return getClient().getAccountName();
	}

	public int getRelation(L2Player target)
	{
		int result = 0;

		if (getClan() != null)
			result |= RelationChanged.RELATION_CLAN_MEMBER;

		if (isClanLeader())
			result |= RelationChanged.RELATION_LEADER;

		L2Party party = getParty();
		if (party != null && party == target.getParty())
		{
			result |= RelationChanged.RELATION_HAS_PARTY;

			switch (party.getPartyMembers().indexOf(this))
			{
			case 0:
				result |= RelationChanged.RELATION_PARTYLEADER; // 0x10
				break;
			case 1:
				result |= RelationChanged.RELATION_PARTY4; // 0x8
				break;
			case 2:
				result |= RelationChanged.RELATION_PARTY3+RelationChanged.RELATION_PARTY2+RelationChanged.RELATION_PARTY1; // 0x7
				break;
			case 3:
				result |= RelationChanged.RELATION_PARTY3+RelationChanged.RELATION_PARTY2; // 0x6
				break;
			case 4:
				result |= RelationChanged.RELATION_PARTY3+RelationChanged.RELATION_PARTY1; // 0x5
				break;
			case 5:
				result |= RelationChanged.RELATION_PARTY3; // 0x4
				break;
			case 6:
				result |= RelationChanged.RELATION_PARTY2+RelationChanged.RELATION_PARTY1; // 0x3
				break;
			case 7:
				result |= RelationChanged.RELATION_PARTY2; // 0x2
				break;
			case 8:
				result |= RelationChanged.RELATION_PARTY1; // 0x1
				break;
			}
		}

		if (getSiegeState() != SIEGE_STATE_NOT_INVOLVED)
		{
			if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(this) != 0)
			{
				result |= RelationChanged.RELATION_TERRITORY_WAR;
			}
			else
			{
				result |= RelationChanged.RELATION_INSIEGE;
				if (getSiegeState() != target.getSiegeState())
					result |= RelationChanged.RELATION_ENEMY;
				else
					result |= RelationChanged.RELATION_ALLY;
				if (getSiegeState() == SIEGE_STATE_ATTACKER)
					result |= RelationChanged.RELATION_ATTACKER;	
			}
		}

		if (getClan() != null && target.getClan() != null)
		{
			if (target.getSubPledgeType() != L2Clan.SUBUNIT_ACADEMY && getSubPledgeType() != L2Clan.SUBUNIT_ACADEMY && target.getClan().isAtWarWith(getClan().getClanId()))
			{
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if (getClan().isAtWarWith(target.getClan().getClanId()))
					result |= RelationChanged.RELATION_MUTUAL_WAR;
			}
		}
		if (getBlockCheckerArena() != -1)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			HandysBlockCheckerManager.ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(getBlockCheckerArena());
			if (holder.getPlayerTeam(this) == 0)
				result |= RelationChanged.RELATION_ENEMY;
			else
				result |= RelationChanged.RELATION_ALLY;
			result |= RelationChanged.RELATION_ATTACKER;
		}
		return result;
	}

	public Map<Integer, String> getAccountChars()
	{
		if (_chars == null)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();

				// Retrieve the name and ID of the other characters assigned to this account.
				PreparedStatement statement = con.prepareStatement(RESTORE_CHARS_FOR_ACCOUNT);
				statement.setString(1, getAccountName());
				statement.setInt(2, getObjectId());
				ResultSet rset = statement.executeQuery();

				while (rset.next())
				{
					if (_chars == null)
						_chars = new HashMap<Integer, String>();

					_chars.put(rset.getInt("charId"), rset.getString("char_name"));
				}

				rset.close();
				statement.close();
			}
			catch (SQLException e)
			{
				_log.warn("", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			if (_chars == null)
				_chars = L2Collections.emptyMap();
		}

		return _chars;
	}

	private void initPcStatusUpdateValues()
	{
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}

	/**
	 * Constructor of L2Player (use L2Character constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2Player </li>
	 * <li>Set the name of the L2Player</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the L2Player to 1</B></FONT><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2Player
	 * @param accountName The name of the account including this L2Player
	 *
	 */
	private L2Player(int objectId, L2PcTemplate template, String accountName, PcAppearance app)
	{
		super(objectId, template);
		getKnownList(); // Init knownlist
		getStat(); // Init stats
		getStatus(); // Init status
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();

		_accountName = accountName;
		app.setOwner(this);
		_appearance = app;

		// Create an AI
		getAI();

		// Retrieve from the database all items of this L2Player and add them to _inventory
		getInventory().restore();
		getWarehouse();
		getPlayerVitality().startVitalityTask();
	}

	@Override
	protected CharKnownList initKnownList()
	{
		return new PcKnownList(this);
	}

	@Override
	public final PcKnownList getKnownList()
	{
		return (PcKnownList)_knownList;
	}

	@Override
	protected CharLikeView initView()
	{
		return new PcView(this);
	}

	@Override
	public PcView getView()
	{
		return (PcView)_view;
	}

	@Override
	protected CharStat initStat()
	{
		return new PcStat(this);
	}

	@Override
	public final PcStat getStat()
	{
		return (PcStat)_stat;
	}

	@Override
	protected CharStatus initStatus()
	{
		return new PcStatus(this);
	}

	@Override
	public final PcStatus getStatus()
	{
		return (PcStatus)_status;
	}
	
	@Override
	protected PcEffects initEffects()
	{
		return new PcEffects(this);
	}
	
	@Override
	public PcEffects getEffects()
	{
		return (PcEffects)_effects;
	}
	
	public final PcAppearance getAppearance()
	{
		return _appearance;
	}

	@Override
	public void setTitle(String value)
	{
		if (value.length() > 16)
			value = value.substring(0, 15);

		super.setTitle(value);
	}

	/**
	 * Return the base L2PcTemplate link to the L2Player.<BR><BR>
	 */
	public final L2PcTemplate getBaseTemplate()
	{
		return CharTemplateTable.getInstance().getTemplate(_baseClass);
	}

	/** Return the L2PcTemplate link to the L2Player. */
	@Override
	public final L2PcTemplate getTemplate()
	{
		return (L2PcTemplate) super.getTemplate();
	}

	public void setTemplate(ClassId newclass)
	{
		super.setTemplate(CharTemplateTable.getInstance().getTemplate(newclass));
	}

	@Override
	protected L2CharacterAI initAI()
	{
		return new L2PlayerAI(new L2Player.AIAccessor());
	}

	/** Return the Level of the L2Player. */
	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}

	/**
	 * Return the _newbie rewards state of the L2Player.<BR><BR>
	 */
	public int getNewbie()
	{
		return _newbie;
	}

	/**
	 * Set the _newbie rewards state of the L2Player.<BR><BR>
	 *
	 * @param newbieRewards The Identifier of the _newbie state<BR><BR>
	 *
	 */
	public void setNewbie(int newbieRewards)
	{
		_newbie = newbieRewards;
	}

	public void setBaseClass(int baseClass)
	{
		_baseClass = baseClass;
	}

	public void setBaseClass(ClassId classId)
	{
		_baseClass = classId.ordinal();
	}

	public boolean isInStoreMode()
	{
		return (getPrivateStoreType() > 0);
	}

	public boolean isInCraftMode()
	{
		return _inCraftMode;
	}

	public void isInCraftMode(boolean b)
	{
		_inCraftMode = b;
	}

	/**
	 * Returns the Id for the last talked quest NPC.<BR><BR>
	 */
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}

	public void setLastQuestNpcObject(int npcId)
	{
		_questNpcObject = npcId;
	}

	/**
	 * Return the QuestState object corresponding to the quest name.<BR><BR>
	 *
	 * @param quest The name of the quest
	 *
	 */
	public QuestState getQuestState(String quest)
	{
		return _quests.get(quest);
	}

	/**
	 * Add a QuestState to the table _quest containing all quests began by the L2Player.<BR><BR>
	 *
	 * @param qs The QuestState to add to _quest
	 *
	 */
	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuestName(), qs);
	}

	/**
	 * Remove a QuestState from the table _quest containing all quests began by the L2Player.<BR><BR>
	 *
	 * @param quest The name of the quest
	 *
	 */
	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}

	private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state)
	{
		int len = questStateArray.length;
		QuestState[] tmp = new QuestState[len + 1];
		System.arraycopy(questStateArray, 0, tmp, 0, len);
		tmp[len] = state;
		return tmp;
	}

	/**
	 * Return a table containing all Quest in progress from the table _quests.<BR><BR>
	 */
	public Quest[] getAllActiveQuests()
	{
		ArrayBunch<Quest> quests = new ArrayBunch<Quest>();

		for (QuestState qs : _quests.values())
		{
			if (qs == null)
				continue;

			int questId = qs.getQuest().getQuestIntId();
			if ((questId > 19999) || (questId < 1))
				continue;

			if (!qs.isStarted())
				continue;

			quests.add(qs.getQuest());
		}

		return quests.moveToArray(new Quest[quests.size()]);
	}

	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
	 *
	 * @param npc The Identifier of the L2Attackable attacked
	 *
	 */
	public QuestState[] getQuestsForAttacks(L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2Player quests
		for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
		{
			// Check if the Identifier of the L2Attackable attck is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2Player QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]
					                        { getQuestState(quest.getName()) };
				else
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
	 *
	 * @param npc The Identifier of the L2Attackable killed
	 *
	 */
	public QuestState[] getQuestsForKills(L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2Player quests
		for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
		{
			// Check if the Identifier of the L2Attackable killed is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2Player QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]
					                        { getQuestState(quest.getName()) };
				else
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	/**
	 * Return a table containing all QuestState from the table _quests in which the L2Player must talk to the NPC.<BR><BR>
	 *
	 * @param npcId The Identifier of the NPC
	 *
	 */
	public QuestState[] getQuestsForTalk(int npcId)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2Player quests
		Quest[] quests = NpcTable.getInstance().getTemplate(npcId).getEventQuests(Quest.QuestEventType.ON_TALK);
		if (quests != null)
		{
			for (Quest quest : quests)
			{
				// Copy the current L2Player QuestState in the QuestState table
				if (quest != null)
				{
					// Copy the current L2Player QuestState in the QuestState table
					if (getQuestState(quest.getName()) != null)
					{
						if (states == null)
							states = new QuestState[]
							                        { getQuestState(quest.getName()) };
						else
							states = addToQuestStateArray(states, getQuestState(quest.getName()));
					}
				}
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	public QuestState processQuestEvent(String quest, String event)
	{
		QuestState retval = null;
		if (event == null)
			event = "";
		QuestState qs = getQuestState(quest);
		if (qs == null && event.isEmpty())
			return retval;
		if (qs == null)
		{
			Quest q = QuestService.getInstance().getQuest(quest);
			if (q == null)
				return retval;
			qs = q.newQuestState(this);
		}
		if (qs != null)
		{
			if (getLastQuestNpcObject() > 0)
			{
				L2Object object = getKnownList().getKnownObject(getLastQuestNpcObject());
				if (object instanceof L2Npc && isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					L2Npc npc = (L2Npc) object;
					QuestState[] states = getQuestsForTalk(npc.getNpcId());

					if (states != null)
					{
						for (QuestState state : states)
						{
							if ((state.getQuest().getQuestIntId() == qs.getQuest().getQuestIntId()))// && !qs.isCompleted())
							{
								if (qs.getQuest().notifyEvent(event, npc, this))
									showQuestWindow(quest, State.getStateName(qs.getState()));

								retval = qs;
							}
						}
						sendPacket(new QuestList(this));
					}
				}
			}
		}

		return retval;
	}

	/**
	 * FIXME: move this from L2Player, there is no reason to have this here
	 * @param questId
	 * @param stateId
	 */
	private void showQuestWindow(String questId, String stateId)
	{
		String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
		String content = HtmCache.getInstance().getHtm(path);

		if (content != null)
		{
			if (_log.isDebugEnabled())
				_log.debug("Showing quest window for quest " + questId + " state " + stateId + " html path: " + path);

			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
		}

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	/** List of all QuestState instance that needs to be notified of this L2Player's or its pet's death */
	private final List<QuestState> _NotifyQuestOfDeathList = new SingletonList<QuestState>();

	/**
	 * Add QuestState instance that is to be notified of L2Player's death.<BR>
	 * <BR>
	 *
	 * @param qs The QuestState that subscribe to this event
	 */
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null || _NotifyQuestOfDeathList.contains(qs))
			return;

		_NotifyQuestOfDeathList.add(qs);
	}

	/**
	 * Remove QuestState instance that is to be notified of L2Player's death.<BR>
	 * <BR>
	 *
	 * @param qs The QuestState that subscribe to this event
	 */
	public void removeNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null || !_NotifyQuestOfDeathList.contains(qs))
			return;

		_NotifyQuestOfDeathList.remove(qs);
	}

	/**
	 * Return a list of QuestStates which registered for notify of death of this L2Player.<BR>
	 * <BR>
	 */
	public final List<QuestState> getNotifyQuestOfDeath()
	{
		return _NotifyQuestOfDeathList;
	}

	/**
	 * Set the siege state of the L2Player.<BR><BR>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 */
	public void setSiegeState(byte siegeState)
	{
		_siegeState = siegeState;
		broadcastRelationChanged();
	}

	/**
	 * Get the siege state of the L2Player.<BR><BR>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 */
	public byte getSiegeState()
	{
		return _siegeState;
	}
	
	public void setSiegeSide(int val)
	{
		_siegeSide = val;
	}
	
	public boolean isRegisteredOnThisSiegeField(int val)
	{
		if (_siegeSide != val && (_siegeSide < 81 || _siegeSide > 89))
			return false;
		return true;
	}
	
	public int getSiegeSide()
	{
		return _siegeSide;
	}
	
	public static final byte SIEGE_STATE_NOT_INVOLVED = 0;
	public static final byte SIEGE_STATE_ATTACKER = 1;
	public static final byte SIEGE_STATE_DEFENDER = 2;

	@Override
	public boolean revalidateZone(boolean force)
	{
		if (!super.revalidateZone(force))
			return false;

		if (Config.ALLOW_WATER)
			checkWaterState();

		if (isInsideZone(L2Zone.FLAG_SIEGE))
		{
			setLastCompassZone(ExSetCompassZoneCode.SIEGE_WAR);
		}
		else if (isInsideZone(L2Zone.FLAG_PVP))
		{
			setLastCompassZone(ExSetCompassZoneCode.PVP);
		}
		else if (isIn7sDungeon())
		{
			setLastCompassZone(ExSetCompassZoneCode.SEVEN_SIGNS);
		}
		else if (isInsideZone(L2Zone.FLAG_PEACE))
		{
			setLastCompassZone(ExSetCompassZoneCode.PEACEFUL);
		}
		else
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGE_WAR.getZoneCode())
				updatePvPStatus();

			setLastCompassZone(ExSetCompassZoneCode.GENERAL);
		}

		return true;
	}

	private void setLastCompassZone(ExSetCompassZoneCode packet)
	{
		if (_lastCompassZone == packet.getZoneCode())
			return;

		_lastCompassZone = packet.getZoneCode();
		sendPacket(packet);
	}

	/**
	 * Return True if the L2Player can Craft Dwarven Recipes.<BR><BR>
	 */
	public boolean hasDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1;
	}

	public int getDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN);
	}

	/**
	 * Return True if the L2Player can Craft Dwarven Recipes.<BR><BR>
	 */
	public boolean hasCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}

	public int getCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
	}

	/**
	 * Return the PK counter of the L2Player.<BR><BR>
	 */
	public int getPkKills()
	{
		return _pkKills;
	}

	/**
	 * Set the PK counter of the L2Player.<BR><BR>
	 */
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}

	/**
	 * Return the _deleteTimer of the L2Player.<BR><BR>
	 */
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}

	/**
	 * Set the _deleteTimer of the L2Player.<BR><BR>
	 */
	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	/**
	 * Return the current weight of the L2Player.<BR><BR>
	 */
	@Override
	public int getCurrentLoad()
	{
		return getInventory().getTotalWeight();
	}

	/** @return the number of evaluation points obtained by player. */
	public int getEvalPoints()
	{
		return _evalPoints;
	}

	/**
	 * Set the number of evaluation points obtained by player.
	 * @param value Evaluation point count
	 */
	public void setEvalPoints(int value)
	{
		_evalPoints = value;
	}

	/** @return the number of evaluations this player can give away. */
	public int getEvaluations()
	{
		return _evaluations;
	}

	/**
	 * Set the number of available evaluations.
	 * @param value New available evaluation count
	 */
	public void setEvaluationCount(int value)
	{
		_evaluations = value;
	}

	/**
	 * Add a player that has been evaluated by this player.
	 * @param charId evaluated player's ID
	 */
	public void addEvalRestriction(int charId)
	{
		_evaluated.add(charId);
	}

	/**	Removes all session evaluation restrictions for this player. */
	public void cleanEvalRestrictions()
	{
		_evaluated.clear();
	}

	/**
	 * @param target Player being evaluated
	 * @return whether this player hasn't evaluated the given player
	 */
	public boolean canEvaluate(L2Player target)
	{
		return !_evaluated.contains(target.getObjectId());
	}
	
	public final int getEvalBonusType()
	{
		// Maintain = 1
		return 0;
	}
	
	public final int getEvalBonusTime()
	{
		// TODO: Implement me...
		return 0;
	}

	/**
	 * Set the exp of the L2Player before a death
	 * @param exp
	 */
	public void setExpBeforeDeath(long exp)
	{
		_expBeforeDeath = exp;
	}

	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}

	/**
	 * Return the Karma of the L2Player.<BR><BR>
	 */
	public int getKarma()
	{
		return _karma;
	}

	/**
	 * Set the Karma of the L2Player and send a Server->Client packet StatusUpdate (broadcast).<BR><BR>
	 */
	public void setKarma(int karma)
	{
		if (karma < 0)
			karma = 0;
		if (_karma == 0 && karma > 0)
		{
			for (L2Object object : getKnownList().getKnownObjects().values())
			{
				if (!(object instanceof L2GuardInstance))
					continue;

				if (((L2GuardInstance) object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					((L2GuardInstance) object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		else if (_karma > 0 && karma == 0)
		{
			// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2Player and all L2Player to inform (broadcast)
			setKarmaFlag(0);
		}
		_karma = karma;
		broadcastKarma();
	}

	/**
	 * Return the max weight that the L2Player can load.<BR><BR>
	 */
	@Override
	public int getMaxLoad()
	{
		return (int)(calcStat(Stats.MAX_LOAD, 69000, this, null) * Config.ALT_WEIGHT_LIMIT);
	}

	public int getExpertiseWeaponPenalty()
	{
		return _expertiseWeaponPenalty;
	}

	private void setWeaponPenalty(int level)
	{
		_expertiseWeaponPenalty = level;
	}

	public int getExpertiseArmorPenalty()
	{
		return _expertiseArmorPenalty;
	}

	private void setArmorPenalty(int level)
	{
		_expertiseArmorPenalty = level;
	}

	@Deprecated
	public boolean getExpertisePenalty()
	{
		return getExpertiseWeaponPenalty() > 0 || getExpertiseArmorPenalty() > 0;
	}

	@Override
	public int getWeightPenalty()
	{
		return _curWeightPenalty;
	}

	@Override
	public void setWeightPenalty(int value)
	{
		_curWeightPenalty = value;
	}

	public void refreshExpertisePenalty()
	{
		if (!Config.ALT_GRADE_PENALTY)
			return;

		int weaponPenalty = 0;
		int armorPenalty = 0;
		boolean sendUpdate = false;

		for (L2ItemInstance item : getInventory().getItems())
		{
			if (!item.isEquipped() || item.getItem() == null
					|| item.getItem().getCrystalType() <= getExpertiseIndex())
				continue;

			if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
				weaponPenalty = (item.getItem().getCrystalType() - getExpertiseIndex());
			else
				armorPenalty++;
		}

		L2Skill skill = getKnownSkill(6209);
		int skillLevel = skill == null ? 0 : skill.getLevel();
		if (weaponPenalty > 4)
			weaponPenalty = 4;
		if (getExpertiseWeaponPenalty() != weaponPenalty || skillLevel != weaponPenalty)
		{
			setWeaponPenalty(weaponPenalty);
			if (weaponPenalty > 0)
				super.addSkill(6209, weaponPenalty);
			else
				super.removeSkill(skill);
			sendUpdate = true;
		}

		skill = getKnownSkill(6213);
		skillLevel = skill == null ? 0 : skill.getLevel();
		if (armorPenalty > 4)
			armorPenalty = 4;
		if (getExpertiseArmorPenalty() != armorPenalty || skillLevel != armorPenalty)
		{
			setArmorPenalty(armorPenalty);
			if (armorPenalty > 0)
				super.addSkill(6213, armorPenalty);
			else
				super.removeSkill(skill);
			sendUpdate = true;
		}

		if (sendUpdate)
			sendEtcStatusUpdate();
	}

	/**
	 * Return the the PvP Kills of the L2Player (Number of player killed during a PvP).<BR><BR>
	 */
	public int getPvpKills()
	{
		return _pvpKills;
	}

	/**
	 * Set the the PvP Kills of the L2Player (Number of player killed during a PvP).<BR><BR>
	 */
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	/**
	 * Return the ClassId object of the L2Player contained in L2PcTemplate.<BR><BR>
	 */
	public ClassId getClassId()
	{
		return getTemplate().getClassId();
	}

	public void academyCheck(int Id)
	{
		if ((getSubPledgeType() == -1 || getLvlJoinedAcademy() != 0) && _clan != null && ClassId.values()[Id].getLevel() == ClassLevel.Third)
		{
			if (getLvlJoinedAcademy() <= 16)
				_clan.setReputationScore(_clan.getReputationScore() + Config.JOIN_ACADEMY_MAX_REP_SCORE, true);
			else if (getLvlJoinedAcademy() >= 39)
				_clan.setReputationScore(_clan.getReputationScore() + Config.JOIN_ACADEMY_MIN_REP_SCORE, true);
			else
				_clan.setReputationScore(_clan.getReputationScore() + (Config.JOIN_ACADEMY_MAX_REP_SCORE - (getLvlJoinedAcademy() - 16) * 20), true);
			setLvlJoinedAcademy(0);

			// Oust pledge member from the academy, cuz he has finished his 2nd class transfer
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
			msg.addString(getName());
			_clan.broadcastToOnlineMembers(msg);
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));

			_clan.removeClanMember(getObjectId(), 0);
			sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
			// Receive graduation gift
			getInventory().addItem("Gift", 8181, 1, this, null); // Give academy circlet
		}
	}

	/**
	 * Set the template of the L2Player.<BR><BR>
	 *
	 * @param Id The Identifier of the L2PcTemplate to set to the L2Player
	 *
	 */
	public void setClassId(int Id)
	{
		if (!_subclassLock.tryLock())
			return;

		try
		{
			academyCheck(Id);

			if (isSubClassActive())
			{
				getSubClasses().get(_classIndex).setClassId(Id);
			}
			setClassTemplate(Id);

			setTarget(this);
			// Animation: Production - Clan / Transfer
			MagicSkillUse msu = new MagicSkillUse(this, this, 5103, 1, 1196, 0);
			broadcastPacket(msu);

			// Update class icon in party and clan
			broadcastClassIcon();

			rewardSkills();
		}
		finally
		{
			_subclassLock.unlock();
		}
	}

	public void useEquippableItem(L2ItemInstance item, boolean abortAttack)
	{
		// Equip or unEquip
		L2ItemInstance[] items = null;
		final boolean isEquiped = item.isEquipped();
		final int oldInvLimit = getInventoryLimit();
		SystemMessage sm = null;
		L2ItemInstance old = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		if (old == null)
			old = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);

		int bodyPart = item.getItem().getBodyPart();
		if (isEquiped)
		{
			if (item.getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item);
			}
			sendPacket(sm);

			switch (bodyPart)
			{
			case L2Item.SLOT_L_EAR:
			case L2Item.SLOT_LR_EAR:
			case L2Item.SLOT_L_FINGER:
			case L2Item.SLOT
