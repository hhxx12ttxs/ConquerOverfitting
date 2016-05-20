package com.wistone.ww2Refactor.game.battle.manager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.wistone.common.util.Util4Number;
import com.wistone.common.util.SystemControl.IInitializable;
import com.wistone.common.util.SystemControl.InitException;
import com.wistone.ww2Refactor.game.army.IArmyConstant;
import com.wistone.ww2Refactor.game.army.db.pojo.ArmyPrototype;
import com.wistone.ww2Refactor.game.army.db.pojo.CityArmy;
import com.wistone.ww2Refactor.game.army.processor.IArmyProcessor;
import com.wistone.ww2Refactor.game.battle.BattleExecuteTask;
import com.wistone.ww2Refactor.game.battle.BattleFinishTask;
import com.wistone.ww2Refactor.game.battle.BattleOldRecoverTask;
import com.wistone.ww2Refactor.game.battle.BattleRecoverTask;
import com.wistone.ww2Refactor.game.battle.IBattleConstants;
import com.wistone.ww2Refactor.game.battle.build.IBattleFilter;
import com.wistone.ww2Refactor.game.battle.calculate.IBattleFinishListener;
import com.wistone.ww2Refactor.game.battle.db.pojo.BattleArmy;
import com.wistone.ww2Refactor.game.battle.db.pojo.BattleData;
import com.wistone.ww2Refactor.game.battle.db.pojo.BattleInfo;
import com.wistone.ww2Refactor.game.battle.execute.Battle;
import com.wistone.ww2Refactor.game.battle.execute.BattleArmyDetail;
import com.wistone.ww2Refactor.game.battle.execute.BattleArmyInfo;
import com.wistone.ww2Refactor.game.battle.execute.BattleCommander;
import com.wistone.ww2Refactor.game.battle.execute.BattleField;
import com.wistone.ww2Refactor.game.battle.execute.BattleSide;
import com.wistone.ww2Refactor.game.battle.execute.BattleUnit;
import com.wistone.ww2Refactor.game.battle.execute.WallUnit;
import com.wistone.ww2Refactor.game.battle.processor.IBattleProcessor;
import com.wistone.ww2Refactor.game.battle.result.BattleResult;
import com.wistone.ww2Refactor.game.battle.result.MeetingBattleResult;
import com.wistone.ww2Refactor.game.battle.result.NpcCityBattleResult;
import com.wistone.ww2Refactor.game.battle.result.OwnerFieldBattleResult;
import com.wistone.ww2Refactor.game.battle.result.OwnerNuclearFieldBattleResult;
import com.wistone.ww2Refactor.game.battle.result.OwnerlessFieldBattleResult;
import com.wistone.ww2Refactor.game.battle.result.OwnerlessNuclearFieldBattleResult;
import com.wistone.ww2Refactor.game.battle.result.PlayerCityBattleResult;
import com.wistone.ww2Refactor.game.city.ICityConstant;
import com.wistone.ww2Refactor.game.city.db.pojo.City;
import com.wistone.ww2Refactor.game.city.processor.ICityProcessor;
import com.wistone.ww2Refactor.game.core.GameConstants;
import com.wistone.ww2Refactor.game.core.timerTask.ISecondTaskExecuter;
import com.wistone.ww2Refactor.game.expedition.ArmyInfo;
import com.wistone.ww2Refactor.game.expedition.db.ExpeditionArmy;
import com.wistone.ww2Refactor.game.expedition.db.ExpeditionInfo;
import com.wistone.ww2Refactor.game.expedition.processor.IExpeditionProcessor;
import com.wistone.ww2Refactor.game.map.FieldArmyInfo;
import com.wistone.ww2Refactor.game.map.IMapConstant;
import com.wistone.ww2Refactor.game.map.db.pojo.Field;
import com.wistone.ww2Refactor.game.map.db.pojo.FieldArmy;
import com.wistone.ww2Refactor.game.map.db.pojo.FieldOfficer;
import com.wistone.ww2Refactor.game.map.mapcell.MapCell;
import com.wistone.ww2Refactor.game.map.mapcell.MapField;
import com.wistone.ww2Refactor.game.map.mapcell.MapNpcCity;
import com.wistone.ww2Refactor.game.map.mapcell.MapNuclearField;
import com.wistone.ww2Refactor.game.map.mapcell.MapPlayerCity;
import com.wistone.ww2Refactor.game.map.processor.IMapService;
import com.wistone.ww2Refactor.game.meetbattle.db.pojo.MeetBattleArmy;
import com.wistone.ww2Refactor.game.meetbattle.db.pojo.MeetBattleInfo;
import com.wistone.ww2Refactor.game.meetbattle.processor.IMeetBattleService;
import com.wistone.ww2Refactor.game.npc.db.pojo.FieldNuclear;
import com.wistone.ww2Refactor.game.npc.db.pojo.FieldNuclearArmy;
import com.wistone.ww2Refactor.game.npc.db.pojo.FieldNuclearOfficerPrototype;
import com.wistone.ww2Refactor.game.npc.db.pojo.NpcCity;
import com.wistone.ww2Refactor.game.npc.db.pojo.NpcCityArmy;
import com.wistone.ww2Refactor.game.npc.db.pojo.NpcCityOfficer;
import com.wistone.ww2Refactor.game.npc.processor.INpcCityProcessor;
import com.wistone.ww2Refactor.game.npc.processor.INuclearFieldProcessor;
import com.wistone.ww2Refactor.game.officer.db.pojo.Officer;
import com.wistone.ww2Refactor.game.officer.processor.IOfficerService;
import com.wistone.ww2Refactor.game.officer.processor.OfficerVo;
import com.wistone.ww2Refactor.game.player.db.pojo.NpcPlayer;
import com.wistone.ww2Refactor.game.player.db.pojo.Player;
import com.wistone.ww2Refactor.game.player.processor.IPlayerProcessor;

public class BattleManager implements ISecondTaskExecuter, IInitializable {
        Logger logger = Logger.getLogger(getClass());

        private ExecutorService battleExecutor;
        private DataSourceTransactionManager txManager;
        private IBattleProcessor battleProcessor;
        private IOfficerService officerService;
        private IExpeditionProcessor expeditionProcessor;
        private IMapService mapService;
        private IArmyProcessor armyProcessor;
        private ICityProcessor cityProcessor;
        private INuclearFieldProcessor nuclearFieldProcessor;
        private INpcCityProcessor npcCityProcessor;
        private IPlayerProcessor playerProcessor;
        private IMeetBattleService meetBattleService;
        private IBattleFinishListener battleFinishListener;
        private String battleObjectUrl;
        private List<IBattleFilter> battleFilters;

        public void setMeetBattleService(IMeetBattleService meetBattleService) {
                this.meetBattleService = meetBattleService;
        }

        public void setBattleObjectUrl(String battleObjectUrl) {
                this.battleObjectUrl = battleObjectUrl;
        }

        public void setBattleFinishListener(IBattleFinishListener battleFinishListener) {
                this.battleFinishListener = battleFinishListener;
        }

        public void setPlayerProcessor(IPlayerProcessor playerProcessor) {
                this.playerProcessor = playerProcessor;
        }

        public void setNpcCityProcessor(INpcCityProcessor npcCityProcessor) {
                this.npcCityProcessor = npcCityProcessor;
        }

        public void setTxManager(DataSourceTransactionManager txManager) {
                this.txManager = txManager;
        }

        public void setNuclearFieldProcessor(INuclearFieldProcessor nuclearFieldProcessor) {
                this.nuclearFieldProcessor = nuclearFieldProcessor;
        }

        public void setCityProcessor(ICityProcessor cityProcessor) {
                this.cityProcessor = cityProcessor;
        }

        public void setArmyProcessor(IArmyProcessor armyProcessor) {
                this.armyProcessor = armyProcessor;
        }

        public void setMapService(IMapService mapService) {
                this.mapService = mapService;
        }

        public void setExpeditionProcessor(IExpeditionProcessor expeditionProcessor) {
                this.expeditionProcessor = expeditionProcessor;
        }

        public void setOfficerService(IOfficerService officerService) {
                this.officerService = officerService;
        }

        public void setBattleProcessor(IBattleProcessor battleProcessor) {
                this.battleProcessor = battleProcessor;
        }

        public void setBattleExecutor(ExecutorService battleExecutor) {
                this.battleExecutor = battleExecutor;
        }

        public void setBattleFilters(List<IBattleFilter> battleFilters) {
                this.battleFilters = battleFilters;
        }

        /**
         * 生成默认事务
         * 
         * @return
         * @author Neo
         * @description 方法说明
         */
        protected DefaultTransactionDefinition createDefaultTransactionDefinition() {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                // 设置事务的传播性
                def.setPropagationBehavior(GameConstants.TRANSATION_PROPAGATION_BEHAVIOR);
                // 设置事务的隔离级别
                def.setIsolationLevel(GameConstants.TRANSATION_ISOLATION_LEVEL);
                // 设置事务的超时时间
                def.setTimeout(GameConstants.TRANSATION_TIME_OUT);
                return def;
        }

        @Override
        public void init() throws InitException {

                List<BattleData> battleDatas = battleProcessor.getInitOldBattleData();
                for (BattleData battleData : battleDatas) {
                        battleExecutor.execute(new BattleOldRecoverTask(this, battleData));
                }

                List<BattleInfo> battleInfos = battleProcessor.getInitBattleInfos();
                logger.info("起服需要恢复战场数量为" + battleInfos.size());
                for (BattleInfo battleInfo : battleInfos) {
                        battleExecutor.execute(new BattleRecoverTask(this, battleInfo));
                }
        }

        @Override
        public void secondTaskExecute(int times) {
                //获取完成的战斗
                List<Battle> finishedBattles = battleProcessor.getFinishedBattle();
                //logger.warn("==============>已完成的战斗" + (finishedBattles != null ? finishedBattles.size() : 0) + "<=============");
                for (Battle battle : finishedBattles) {
                        battleExecutor.execute(new BattleFinishTask(this, battle));
                }
                List<Battle> processibleBattles = battleProcessor.getProcessibleBattle();
                //logger.warn("==============>正在进行的战斗" + (processibleBattles != null ? processibleBattles.size() : 0) + "<=============");
                for (Battle battle : processibleBattles) {
                        battleExecutor.execute(new BattleExecuteTask(battle));
                }
        }

        public void recoverBattle(BattleInfo battleInfo) {
                switch (battleInfo.getBattleFieldType()) {
                case IBattleConstants.BATTLE_FIELD_TYPE_METTING:
                        recoverMeetingBattle(battleInfo);
                        break;
                case IBattleConstants.BATTLE_FIELD_TYPE_NPC_CITY:
                        recoverMapBattleOnNpcCity(battleInfo);
                        break;
                case IBattleConstants.BATTLE_FIELD_TYPE_OWNER_FIELD:
                        recoverMapBattleOnOwnerField(battleInfo);
                        break;
                case IBattleConstants.BATTLE_FIELD_TYPE_OWNERLESS_FIELD:
                        recoverMapBattleOnOwnerlessField(battleInfo);
                        break;
                case IBattleConstants.BATTLE_FIELD_TYPE_OWNER_NUCLEARFIELD:
                        recoverMapBattleOnOwnerNuclearField(battleInfo);
                        break;
                case IBattleConstants.BATTLE_FIELD_TYPE_OWNERLESS_NUCLEARFIELD:
                        recoverMapBattleOnOwnerlessNuclearField(battleInfo);
                        break;
                case IBattleConstants.BATTLE_FIELD_TYPE_PLAYER_CITY:
                        recoverMapBattleOnPlayerCity(battleInfo);
                        break;
                }
        }

        public void onBattleFinshed(Battle battle) {
                BattleResult result = battle.buildBattleResultInfo();
                battleFinishListener.onBattleFinish(result);
                battleProcessor.removeBattle(battle);
                battleProcessor.removeBattleArmy(battle.getBattleId());
        }

        private void recoverMapBattleOnOwnerField(BattleInfo battleInfo) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //本场战斗涉及到的兵力信息
                List<BattleArmy> battleArmys = battleProcessor.getBattleArmy(battleInfo.getId());
                long attackExpId = -1;
                long mainDefendExpId = -1;
                List<BattleArmy> attackBattleArmys = new LinkedList<BattleArmy>();
                Map<Long, List<BattleArmy>> defendBattleArmyMap = new HashMap<Long, List<BattleArmy>>();
                //对战场兵力数据进行分类处理
                for (BattleArmy battleArmy : battleArmys) {
                        if (battleArmy.getSide() == IBattleConstants.ATTACK_SIDE) {
                                if (battleArmy.getBelongType() == IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION) {
                                        attackExpId = battleArmy.getBelongId();
                                        attackBattleArmys.add(battleArmy);
                                }
                        }
                        if (battleArmy.getSide() == IBattleConstants.DEFANSE_SIDE) {
                                if (battleArmy.getBelongType() == IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION) {
                                        if (battleArmy.getIsmain() == GameConstants.YES) {
                                                mainDefendExpId = battleArmy.getBelongId();
                                        }
                                        List<BattleArmy> tempArmys = defendBattleArmyMap.get(battleArmy.getBelongId());
                                        if (tempArmys == null) {
                                                tempArmys = new LinkedList<BattleArmy>();
                                                defendBattleArmyMap.put(battleArmy.getBelongId(), tempArmys);
                                        }
                                        tempArmys.add(battleArmy);
                                }
                        }
                }
                Map<Long, ExpeditionInfo> expeditionMap = expeditionProcessor.getExpeditionInfoByExpIds(new LinkedList<Long>(joinExpeditionIds));
                Map<Long, List<ExpeditionArmy>> expeditionArmyMap = expeditionProcessor.getExpeditionArmysByExpeditionId(new LinkedList<Long>(joinExpeditionIds));
                ExpeditionInfo attackExpeditionInfo = expeditionMap.remove(attackExpId);
                List<ExpeditionArmy> attackExpeditionArmys = expeditionArmyMap.remove(attackExpId);

                MapCell mapcell = mapService.getMapCell(battleInfo.getBattleFieldId());
                Battle battle = new Battle(battleInfo.getBattleType());
                BattleField battleField = new BattleField(mapcell, IBattleConstants.BATTLE_FIELD_TYPE_OWNER_FIELD, battle);
                battle.setBattleField(battleField);
                //生成传递到战后的数据对象
                OwnerFieldBattleResult battleResult = new OwnerFieldBattleResult(battleInfo.getBattleType(), mapcell);
                battle.setBattleResultInfo(battleResult);
                //保存进攻方相关信息
                battleResult.setAttackPlayerId(battleInfo.getAttackPlayerId());
                battleResult.setAttackArmyTotalCost(battleInfo.getAttackSideCost());
                battleResult.setAttackExpeditionInfo(attackExpeditionInfo);
                battleResult.setAttackExpeditionArmys(attackExpeditionArmys);
                battleResult.setAttackCityId(attackExpeditionInfo.getCityId());
                OfficerVo attackOfficerVo = officerService.getCityOfficerVo(attackExpeditionInfo.getGeneralId());
                battleResult.setAttackOfficer(attackOfficerVo);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExpeditionInfo.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(attackBattleArmys.size());
                for (BattleArmy battleArmy : attackBattleArmys) {
                        attackBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy, attackExpeditionInfo.getPlayerId(), attackExpeditionInfo.getCityId()));
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                joinPlayerIds.add(attackExpeditionInfo.getPlayerId());
                joinCityIds.add(attackExpeditionInfo.getCityId());
                joinCityIds.add(attackExpeditionInfo.getId());
                battleResult.setDefendPlayerId(battleInfo.getDefendPlayerId());
                battleResult.setDefenseArmyTotalCost(battleInfo.getDefendSideCost());
                battleResult.setDefendExpeditionInfoMap(expeditionMap);
                battleResult.setDefendExpeditionArmyMap(expeditionArmyMap);
                ExpeditionInfo mainDefendExpeditionInfo = expeditionMap.get(mainDefendExpId);
                if (mainDefendExpeditionInfo.getGeneralId() != null) {
                        OfficerVo defendOfficerVo = officerService.getCityOfficerVo(mainDefendExpeditionInfo.getGeneralId());
                        battleResult.setDefendOfficer(defendOfficerVo);
                }
                List<BattleArmyInfo> defendBattleArmyInfos = new LinkedList<BattleArmyInfo>();
                for (long expId : defendBattleArmyMap.keySet()) {
                        BattleArmyInfo battleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, expId);
                        if (expId == mainDefendExpId) {
                                battleArmyInfo.setMain(true);
                        }
                        List<BattleArmy> defendBattleArmys = defendBattleArmyMap.get(expId);
                        Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendBattleArmys.size());
                        ExpeditionInfo defendExpeditionInfo = expeditionMap.get(expId);
                        joinPlayerIds.add(defendExpeditionInfo.getPlayerId());
                        joinCityIds.add(defendExpeditionInfo.getCityId());
                        joinExpeditionIds.add(defendExpeditionInfo.getId());
                        for (BattleArmy battleArmy : defendBattleArmys) {
                                defendBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy, defendExpeditionInfo.getPlayerId(), defendExpeditionInfo.getCityId()));
                        }
                        battleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                        defendBattleArmyInfos.add(battleArmyInfo);
                }
                battleResult.setDefendBattleArmyInfos(defendBattleArmyInfos);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                Player attackPlayer = playerProcessor.getPlayer(battleInfo.getAttackPlayerId());
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守方
                Player defendPlayer = playerProcessor.getPlayer(battleInfo.getDefendPlayerId());
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                //设置战斗开始
                battle.startBattle();
                //恢复战场
                battleProcessor.recoverBattle(battle);

        }

        private void recoverMapBattleOnOwnerNuclearField(BattleInfo battleInfo) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                List<BattleArmy> battleArmys = battleProcessor.getBattleArmy(battleInfo.getId());
                long attackExpId = -1;
                long mainDefendExpId = -1;
                List<BattleArmy> attackBattleArmys = new LinkedList<BattleArmy>();
                Map<Long, List<BattleArmy>> defendBattleArmyMap = new HashMap<Long, List<BattleArmy>>();

                for (BattleArmy battleArmy : battleArmys) {
                        if (battleArmy.getSide() == IBattleConstants.ATTACK_SIDE) {
                                if (battleArmy.getBelongType() == IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION) {
                                        attackExpId = battleArmy.getBelongId();
                                        attackBattleArmys.add(battleArmy);
                                }
                        }
                        if (battleArmy.getSide() == IBattleConstants.DEFANSE_SIDE) {
                                if (battleArmy.getBelongType() == IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION) {
                                        if (battleArmy.getIsmain() == GameConstants.YES) {
                                                mainDefendExpId = battleArmy.getBelongId();
                                        }
                                        List<BattleArmy> tempArmys = defendBattleArmyMap.get(battleArmy.getBelongId());
                                        if (tempArmys == null) {
                                                tempArmys = new LinkedList<BattleArmy>();
                                                defendBattleArmyMap.put(battleArmy.getBelongId(), tempArmys);
                                        }
                                        tempArmys.add(battleArmy);
                                }
                        }
                }
                Map<Long, ExpeditionInfo> expeditionMap = expeditionProcessor.getExpeditionInfoByExpIds(new LinkedList<Long>(joinExpeditionIds));
                Map<Long, List<ExpeditionArmy>> expeditionArmyMap = expeditionProcessor.getExpeditionArmysByExpeditionId(new LinkedList<Long>(joinExpeditionIds));
                ExpeditionInfo attackExpeditionInfo = expeditionMap.remove(attackExpId);
                List<ExpeditionArmy> attackExpeditionArmys = expeditionArmyMap.remove(attackExpId);

                MapCell mapcell = mapService.getMapCell(battleInfo.getBattleFieldId());
                Battle battle = new Battle(battleInfo.getBattleType());
                BattleField battleField = new BattleField(mapcell, IBattleConstants.BATTLE_FIELD_TYPE_OWNER_NUCLEARFIELD, battle);
                battle.setBattleField(battleField);
                //生成传递到战后的数据对象
                OwnerNuclearFieldBattleResult battleResult = new OwnerNuclearFieldBattleResult(battleInfo.getBattleType(), mapcell);
                battle.setBattleResultInfo(battleResult);
                //保存进攻方相关信息
                battleResult.setAttackPlayerId(battleInfo.getAttackPlayerId());
                battleResult.setAttackArmyTotalCost(battleInfo.getAttackSideCost());
                battleResult.setAttackExpeditionInfo(attackExpeditionInfo);
                battleResult.setAttackExpeditionArmys(attackExpeditionArmys);
                battleResult.setAttackCityId(attackExpeditionInfo.getCityId());
                OfficerVo attackOfficerVo = officerService.getCityOfficerVo(attackExpeditionInfo.getGeneralId());
                battleResult.setAttackOfficer(attackOfficerVo);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExpeditionInfo.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(attackBattleArmys.size());
                for (BattleArmy battleArmy : attackBattleArmys) {
                        attackBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy, attackExpeditionInfo.getPlayerId(), attackExpeditionInfo.getCityId()));
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);

                joinPlayerIds.add(attackExpeditionInfo.getPlayerId());
                joinCityIds.add(attackExpeditionInfo.getCityId());
                joinExpeditionIds.add(attackExpeditionInfo.getId());

                battleResult.setDefendPlayerId(battleInfo.getDefendPlayerId());
                battleResult.setDefenseArmyTotalCost(battleInfo.getDefendSideCost());
                battleResult.setDefendExpeditionInfoMap(expeditionMap);
                battleResult.setDefendExpeditionArmyMap(expeditionArmyMap);
                ExpeditionInfo mainDefendExpeditionInfo = expeditionMap.get(mainDefendExpId);
                if (mainDefendExpeditionInfo.getGeneralId() != null) {
                        OfficerVo defendOfficerVo = officerService.getCityOfficerVo(mainDefendExpeditionInfo.getGeneralId());
                        battleResult.setDefendOfficer(defendOfficerVo);
                }
                List<BattleArmyInfo> defendBattleArmyInfos = new LinkedList<BattleArmyInfo>();
                for (long expId : defendBattleArmyMap.keySet()) {
                        BattleArmyInfo battleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, expId);
                        if (expId == mainDefendExpId) {
                                battleArmyInfo.setMain(true);
                        }
                        List<BattleArmy> defendBattleArmys = defendBattleArmyMap.get(expId);
                        Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendBattleArmys.size());
                        ExpeditionInfo defendExpeditionInfo = expeditionMap.get(expId);

                        joinPlayerIds.add(defendExpeditionInfo.getPlayerId());
                        joinCityIds.add(defendExpeditionInfo.getCityId());
                        joinExpeditionIds.add(defendExpeditionInfo.getId());
                        for (BattleArmy battleArmy : defendBattleArmys) {
                                defendBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy, defendExpeditionInfo.getPlayerId(), defendExpeditionInfo.getCityId()));
                        }
                        battleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                        defendBattleArmyInfos.add(battleArmyInfo);
                }
                battleResult.setDefendBattleArmyInfos(defendBattleArmyInfos);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                Player attackPlayer = playerProcessor.getPlayer(battleInfo.getAttackPlayerId());
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守方
                Player defendPlayer = playerProcessor.getPlayer(battleInfo.getDefendPlayerId());
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                //设置战斗开始
                battle.startBattle();
                //恢复战场
                battleProcessor.recoverBattle(battle);
        }

        private void recoverMapBattleOnPlayerCity(BattleInfo battleInfo) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                List<BattleArmy> battleArmys = battleProcessor.getBattleArmy(battleInfo.getId());
                long attackExpId = -1;
                List<BattleArmy> attackBattleArmys = new LinkedList<BattleArmy>();
                Map<Long, List<BattleArmy>> defendBattleArmyMap = new HashMap<Long, List<BattleArmy>>();
                List<BattleArmy> defendCityBattleArmys = new LinkedList<BattleArmy>();
                for (BattleArmy battleArmy : battleArmys) {
                        if (battleArmy.getSide() == IBattleConstants.ATTACK_SIDE) {
                                if (battleArmy.getBelongType() == IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION) {
                                        attackExpId = battleArmy.getBelongId();
                                        attackBattleArmys.add(battleArmy);
                                }
                        }
                        if (battleArmy.getSide() == IBattleConstants.DEFANSE_SIDE) {
                                if (battleArmy.getBelongType() == IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION) {
                                        List<BattleArmy> tempArmys = defendBattleArmyMap.get(battleArmy.getBelongId());
                                        if (tempArmys == null) {
                                                tempArmys = new LinkedList<BattleArmy>();
                                                defendBattleArmyMap.put(battleArmy.getBelongId(), tempArmys);
                                        }
                                        tempArmys.add(battleArmy);
                                } else if (battleArmy.getBelongType() == IBattleConstants.BATTLE_ARMY_TYPE_PLAYER_CITY) {
                                        defendCityBattleArmys.add(battleArmy);
                                }
                        }
                }
                Map<Long, ExpeditionInfo> expeditionMap = expeditionProcessor.getExpeditionInfoByExpIds(new LinkedList<Long>(joinExpeditionIds));
                Map<Long, List<ExpeditionArmy>> expeditionArmyMap = expeditionProcessor.getExpeditionArmysByExpeditionId(new LinkedList<Long>(joinExpeditionIds));
                ExpeditionInfo attackExpeditionInfo = expeditionMap.remove(attackExpId);
                List<ExpeditionArmy> attackExpeditionArmys = expeditionArmyMap.remove(attackExpId);

                MapCell mapcell = mapService.getMapCell(battleInfo.getBattleFieldId());
                Battle battle = new Battle(battleInfo.getBattleType());
                BattleField battleField = new BattleField(mapcell, IBattleConstants.BATTLE_FIELD_TYPE_PLAYER_CITY, battle);
                battle.setBattleField(battleField);
                //生成传递到战后的数据对象
                PlayerCityBattleResult battleResult = new PlayerCityBattleResult(battleInfo.getBattleType(), mapcell);
                battle.setBattleResultInfo(battleResult);
                //保存进攻方相关信息
                battleResult.setAttackPlayerId(battleInfo.getAttackPlayerId());
                battleResult.setAttackArmyTotalCost(battleInfo.getAttackSideCost());
                battleResult.setAttackExpeditionInfo(attackExpeditionInfo);
                battleResult.setAttackExpeditionArmys(attackExpeditionArmys);
                battleResult.setAttackCityId(attackExpeditionInfo.getCityId());
                OfficerVo attackOfficerVo = officerService.getCityOfficerVo(attackExpeditionInfo.getGeneralId());
                battleResult.setAttackOfficer(attackOfficerVo);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExpeditionInfo.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(attackBattleArmys.size());
                for (BattleArmy battleArmy : attackBattleArmys) {
                        attackBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy, attackExpeditionInfo.getPlayerId(), attackExpeditionInfo.getCityId()));
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);

                joinPlayerIds.add(attackExpeditionInfo.getPlayerId());
                joinCityIds.add(attackExpeditionInfo.getCityId());
                joinExpeditionIds.add(attackExpeditionInfo.getId());
                //设置防守方数据
                battleResult.setDefendPlayerId(battleInfo.getDefendPlayerId());
                battleResult.setDefenseArmyTotalCost(battleInfo.getAttackSideCost());
                battleResult.setDefendExpeditionInfoMap(expeditionMap);
                battleResult.setDefendExpeditionArmyMap(expeditionArmyMap);
                MapPlayerCity mapPlayerCity = (MapPlayerCity) mapcell;
                City city = mapPlayerCity.getCity();
                Officer commander = officerService.getCommanderByCityId(city.getId());
                if (commander != null) {
                        OfficerVo defendOfficerVo = officerService.getCityOfficerVo(commander);
                        battleResult.setDefendOfficer(defendOfficerVo);
                }
                List<BattleArmyInfo> defendBattleArmyInfos = new LinkedList<BattleArmyInfo>();
                //整理防守方出征部队兵力
                for (long expId : defendBattleArmyMap.keySet()) {
                        BattleArmyInfo battleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, expId);
                        List<BattleArmy> defendBattleArmys = defendBattleArmyMap.get(expId);
                        Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendBattleArmys.size());
                        ExpeditionInfo defendExpeditionInfo = expeditionMap.get(expId);
                        joinPlayerIds.add(defendExpeditionInfo.getPlayerId());
                        joinCityIds.add(defendExpeditionInfo.getCityId());
                        joinExpeditionIds.add(defendExpeditionInfo.getId());
                        for (BattleArmy battleArmy : defendBattleArmys) {
                                defendBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy, defendExpeditionInfo.getPlayerId(), defendExpeditionInfo.getCityId()));
                        }
                        battleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                        defendBattleArmyInfos.add(battleArmyInfo);
                }
                //整理防守方城市兵力
                BattleArmyInfo battleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_PLAYER_CITY, city.getId());
                battleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendCityBattleArmys.size());
                for (BattleArmy battleArmy : defendCityBattleArmys) {
                        defendBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy, city.getPlayerId(), city.getId()));
                }
                battleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                defendBattleArmyInfos.add(battleArmyInfo);
                battleResult.setDefendBattleArmyInfos(defendBattleArmyInfos);

                joinPlayerIds.add(city.getPlayerId());
                joinCityIds.add(city.getId());

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                Player attackPlayer = playerProcessor.getPlayer(battleInfo.getAttackPlayerId());
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守方
                Player defendPlayer = playerProcessor.getPlayer(battleInfo.getDefendPlayerId());
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                //设置战斗开始
                battle.startBattle();
                //恢复战场
                battleProcessor.recoverBattle(battle);

        }

        private void recoverMapBattleOnNpcCity(BattleInfo battleInfo) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战出征部队id集合
                List<BattleArmy> battleArmys = battleProcessor.getBattleArmy(battleInfo.getId());
                List<BattleArmy> attackBattleArmys = new LinkedList<BattleArmy>();
                //过滤出属于进攻方的兵力
                long attackExpId = -1;
                for (Iterator<BattleArmy> it = battleArmys.iterator(); it.hasNext();) {
                        BattleArmy battleArmy = it.next();
                        if (battleArmy.getSide() == IBattleConstants.ATTACK_SIDE) {
                                if (battleArmy.getBelongType() == IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION) {
                                        attackExpId = battleArmy.getBelongId();
                                        attackBattleArmys.add(battleArmy);
                                        it.remove();
                                }
                        }
                }
                MapCell mapcell = mapService.getMapCell(battleInfo.getBattleFieldId());
                Battle battle = new Battle(battleInfo.getBattleType());
                BattleField battleField = new BattleField(mapcell, IBattleConstants.BATTLE_FIELD_TYPE_NPC_CITY, battle);
                battle.setBattleField(battleField);
                NpcCityBattleResult battleResult = new NpcCityBattleResult(battleInfo.getBattleType(), mapcell);
                battle.setBattleResultInfo(battleResult);

                ExpeditionInfo attackExpeditionInfo = expeditionProcessor.getExpeditionInfoByExpeditionId(attackExpId);
                List<ExpeditionArmy> attackExpeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExpId);
                battleResult.setAttackPlayerId(battleInfo.getAttackPlayerId());
                battleResult.setAttackArmyTotalCost(battleInfo.getAttackSideCost());
                battleResult.setAttackExpeditionInfo(attackExpeditionInfo);
                battleResult.setAttackExpeditionArmys(attackExpeditionArmys);
                battleResult.setAttackCityId(attackExpeditionInfo.getCityId());
                OfficerVo attackOfficerVo = officerService.getCityOfficerVo(attackExpeditionInfo.getGeneralId());
                battleResult.setAttackOfficer(attackOfficerVo);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExpeditionInfo.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(attackBattleArmys.size());
                for (BattleArmy battleArmy : attackBattleArmys) {
                        attackBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy, attackExpeditionInfo.getPlayerId(), attackExpeditionInfo.getCityId()));
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);

                joinPlayerIds.add(attackExpeditionInfo.getPlayerId());
                joinCityIds.add(attackExpeditionInfo.getCityId());
                joinExpeditionIds.add(attackExpeditionInfo.getId());

                NpcPlayer npcPlayer = npcCityProcessor.getNpcPlayerById(battleInfo.getDefendPlayerId());
                battleResult.setNpcPlayer(npcPlayer);
                battleResult.setDefenseArmyTotalCost(battleInfo.getAttackSideCost());
                MapNpcCity mapNpcCity = (MapNpcCity) mapcell;
                NpcCity npcCity = mapNpcCity.getNpcCity();
                battleResult.setDefendCityArmys(npcCityProcessor.getNpcCityArmyList(npcCity.getId()));
                NpcCityOfficer npcCityOfficer = npcCityProcessor.getNpcCityCommanderOfficer(npcCity.getId());
                if (npcCityOfficer != null)
                        battleResult.setDefendOfficer(npcCityOfficer);
                BattleArmyInfo battleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_NPC_CITY, npcCity.getId());
                battleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(battleArmys.size());
                for (BattleArmy battleArmy : battleArmys) {
                        defendBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy));
                }
                battleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                battleResult.setDefendBattleArmyInfo(battleArmyInfo);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                Player attackPlayer = playerProcessor.getPlayer(battleInfo.getAttackPlayerId());
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守方
                buildDefendBattleSide(npcPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                //设置战斗开始
                battle.startBattle();
                //恢复战场
                battleProcessor.recoverBattle(battle);
        }

        private void recoverMapBattleOnOwnerlessField(BattleInfo battleInfo) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战出征部队id集合
                List<BattleArmy> battleArmys = battleProcessor.getBattleArmy(battleInfo.getId());
                List<BattleArmy> attackBattleArmys = new LinkedList<BattleArmy>();
                //过滤出属于进攻方的兵力
                long attackExpId = -1;
                for (Iterator<BattleArmy> it = battleArmys.iterator(); it.hasNext();) {
                        BattleArmy battleArmy = it.next();
                        if (battleArmy.getSide() == IBattleConstants.ATTACK_SIDE) {
                                if (battleArmy.getBelongType() == IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION) {
                                        attackExpId = battleArmy.getBelongId();
                                        attackBattleArmys.add(battleArmy);
                                        it.remove();
                                }
                        }
                }

                MapCell mapcell = mapService.getMapCell(battleInfo.getBattleFieldId());
                Battle battle = new Battle(battleInfo.getBattleType());
                BattleField battleField = new BattleField(mapcell, IBattleConstants.BATTLE_FIELD_TYPE_OWNERLESS_FIELD, battle);
                battle.setBattleField(battleField);
                OwnerlessFieldBattleResult battleResult = new OwnerlessFieldBattleResult(battleInfo.getBattleType(), mapcell);
                battle.setBattleResultInfo(battleResult);

                ExpeditionInfo attackExpeditionInfo = expeditionProcessor.getExpeditionInfoByExpeditionId(attackExpId);
                List<ExpeditionArmy> attackExpeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExpId);

                battleResult.setAttackPlayerId(battleInfo.getAttackPlayerId());
                battleResult.setAttackArmyTotalCost(battleInfo.getAttackSideCost());
                battleResult.setAttackExpeditionInfo(attackExpeditionInfo);
                battleResult.setAttackExpeditionArmys(attackExpeditionArmys);
                battleResult.setAttackCityId(attackExpeditionInfo.getCityId());
                OfficerVo attackOfficerVo = officerService.getCityOfficerVo(attackExpeditionInfo.getGeneralId());
                battleResult.setAttackOfficer(attackOfficerVo);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExpeditionInfo.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(attackBattleArmys.size());
                for (BattleArmy battleArmy : attackBattleArmys) {
                        attackBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy, attackExpeditionInfo.getPlayerId(), attackExpeditionInfo.getCityId()));
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);

                joinPlayerIds.add(attackExpeditionInfo.getPlayerId());
                joinCityIds.add(attackExpeditionInfo.getCityId());
                joinExpeditionIds.add(attackExpeditionInfo.getId());

                Field field = mapcell.getField();
                battleResult.setDefenseArmyTotalCost(battleInfo.getAttackSideCost());
                FieldArmyInfo fieldArmyInfo = mapService.getFieldArmyInfo(field.getId());
                battleResult.setDefendArmys(fieldArmyInfo.getFieldArmys());
                if (fieldArmyInfo.getFieldOfficer() != null)
                        battleResult.setDefendOfficer(fieldArmyInfo.getFieldOfficer());
                BattleArmyInfo battleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_FIELD, field.getId());
                battleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(battleArmys.size());
                for (BattleArmy battleArmy : battleArmys) {
                        defendBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy));
                }
                battleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                battleResult.setDefendBattleArmyInfo(battleArmyInfo);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                Player attackPlayer = playerProcessor.getPlayer(battleInfo.getAttackPlayerId());
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守方
                buildDefendBattleSide(battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                //设置战斗开始
                battle.startBattle();
                //恢复战场
                battleProcessor.recoverBattle(battle);
        }

        private void recoverMapBattleOnOwnerlessNuclearField(BattleInfo battleInfo) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战出征部队id集合
                List<BattleArmy> battleArmys = battleProcessor.getBattleArmy(battleInfo.getId());
                List<BattleArmy> attackBattleArmys = new LinkedList<BattleArmy>();
                //过滤出属于进攻方的兵力
                long attackExpId = -1;
                for (Iterator<BattleArmy> it = battleArmys.iterator(); it.hasNext();) {
                        BattleArmy battleArmy = it.next();
                        if (battleArmy.getSide() == IBattleConstants.ATTACK_SIDE) {
                                if (battleArmy.getBelongType() == IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION) {
                                        attackExpId = battleArmy.getBelongId();
                                        attackBattleArmys.add(battleArmy);
                                        it.remove();
                                }
                        }
                }
                MapCell mapcell = mapService.getMapCell(battleInfo.getBattleFieldId());
                Battle battle = new Battle(battleInfo.getBattleType());
                BattleField battleField = new BattleField(mapcell, IBattleConstants.BATTLE_FIELD_TYPE_OWNERLESS_NUCLEARFIELD, battle);
                battle.setBattleField(battleField);
                OwnerlessNuclearFieldBattleResult battleResult = new OwnerlessNuclearFieldBattleResult(battleInfo.getBattleType(), mapcell);
                battle.setBattleResultInfo(battleResult);

                ExpeditionInfo attackExpeditionInfo = expeditionProcessor.getExpeditionInfoByExpeditionId(attackExpId);
                List<ExpeditionArmy> attackExpeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExpId);
                battleResult.setAttackPlayerId(battleInfo.getAttackPlayerId());
                battleResult.setAttackArmyTotalCost(battleInfo.getAttackSideCost());
                battleResult.setAttackExpeditionInfo(attackExpeditionInfo);
                battleResult.setAttackExpeditionArmys(attackExpeditionArmys);
                battleResult.setAttackCityId(attackExpeditionInfo.getCityId());
                OfficerVo attackOfficerVo = officerService.getCityOfficerVo(attackExpeditionInfo.getGeneralId());
                battleResult.setAttackOfficer(attackOfficerVo);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExpeditionInfo.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(attackBattleArmys.size());
                for (BattleArmy battleArmy : attackBattleArmys) {
                        attackBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy, attackExpeditionInfo.getPlayerId(), attackExpeditionInfo.getCityId()));
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);

                joinPlayerIds.add(attackExpeditionInfo.getPlayerId());
                joinCityIds.add(attackExpeditionInfo.getCityId());
                joinExpeditionIds.add(attackExpeditionInfo.getId());

                MapNuclearField mapNuclearField = (MapNuclearField) mapcell;
                FieldNuclear fieldNuclear = mapNuclearField.getFieldNuclear();

                battleResult.setDefenseArmyTotalCost(battleInfo.getAttackSideCost());
                List<FieldNuclearArmy> fieldNuclearArmys = nuclearFieldProcessor.getFieldNuclearArmies(fieldNuclear.getId());
                battleResult.setFieldNuclearArmys(fieldNuclearArmys);
                FieldNuclearOfficerPrototype fieldNuclearOfficerPrototype = nuclearFieldProcessor.getFieldNuclearOfficerPrototype(fieldNuclear.getOfficerId());
                battleResult.setFieldNuclearOfficerPrototype(fieldNuclearOfficerPrototype);

                BattleArmyInfo battleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPES_NUCLEARFIELD, fieldNuclear.getId());
                battleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(battleArmys.size());
                for (BattleArmy battleArmy : battleArmys) {
                        defendBattleArmyDetailMap.put(battleArmy.getArmyType(), new BattleArmyDetail(battleArmy));
                }
                battleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                battleResult.setDefendBattleArmyInfo(battleArmyInfo);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                Player attackPlayer = playerProcessor.getPlayer(battleInfo.getAttackPlayerId());
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守方
                buildDefendBattleSide(battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                //设置战斗开始
                battle.startBattle();
                //恢复战场
                battleProcessor.recoverBattle(battle);

        }

        private void recoverMeetingBattle(BattleInfo battleInfo) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();

                Battle battle = new Battle(IBattleConstants.BATTLE_TYPE_MEETING);
                BattleField battleField = new BattleField(IBattleConstants.BATTLE_FIELD_TYPE_METTING, battle);
                battle.setBattleField(battleField);

                //生成一个无归属野地战斗结果封装对象
                MeetingBattleResult battleResult = new MeetingBattleResult(battleInfo.getBattleFieldId().longValue());
                battle.setBattleResultInfo(battleResult);

                MeetBattleInfo meetBattleInfo = meetBattleService.getMeetBattleInfoById(battleInfo.getBattleFieldId().longValue());
                joinExpeditionIds.add(meetBattleInfo.getId());

                Player attackPlayer = playerProcessor.getPlayer(meetBattleInfo.getStartPlayerId());
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                joinPlayerIds.add(attackPlayer.getPlayerId());
                battleResult.setAttackCityId(meetBattleInfo.getStartCityId());
                joinCityIds.add(meetBattleInfo.getStartCityId());
                battleResult.setAttackOfficer(officerService.getCityOfficerVo(meetBattleInfo.getStartGeneralId()));
                List<MeetBattleArmy> attackMeetBattleArmys = meetBattleService.getMeetBattleArmysByMeetBattleInfoIdAndCityId(meetBattleInfo.getId(), meetBattleInfo.getStartCityId());
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_METTING, meetBattleInfo.getId());
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(attackMeetBattleArmys.size());
                double attackCost = 0;
                for (MeetBattleArmy meetBattleArmy : attackMeetBattleArmys) {
                        if (meetBattleArmy.getArmyCount() > 0) {
                                attackCost += armyProcessor.calculateNuclearFieldArmyCost(meetBattleArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.ATTACK_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_METTING);
                                battleArmy.setBelongId(meetBattleInfo.getId());
                                battleArmy.setArmyType(meetBattleArmy.getArmyId());
                                battleArmy.setCount(meetBattleArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(meetBattleArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                battleResult.setAttackArmyTotalCost(attackCost);

                Player defendPlayer = playerProcessor.getPlayer(meetBattleInfo.getTargetPlayerId());
                battleResult.setDefendPlayerId(defendPlayer.getPlayerId());
                joinPlayerIds.add(defendPlayer.getPlayerId());
                battleResult.setDefendCityId(meetBattleInfo.getTargetCityId());
                joinCityIds.add(meetBattleInfo.getTargetCityId());
                battleResult.setDefendOfficer(officerService.getCityOfficerVo(meetBattleInfo.getTargetGeneralId()));
                List<MeetBattleArmy> defendMeetBattleArmys = meetBattleService.getMeetBattleArmysByMeetBattleInfoIdAndCityId(meetBattleInfo.getId(), meetBattleInfo.getStartCityId());
                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_METTING, meetBattleInfo.getId());
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendMeetBattleArmys.size());
                double defendCost = 0;
                for (MeetBattleArmy meetBattleArmy : defendMeetBattleArmys) {
                        if (meetBattleArmy.getArmyCount() > 0) {
                                defendCost += armyProcessor.calculateNuclearFieldArmyCost(meetBattleArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_METTING);
                                battleArmy.setBelongId(meetBattleInfo.getId());
                                battleArmy.setArmyType(meetBattleArmy.getArmyId());
                                battleArmy.setCount(meetBattleArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(meetBattleArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                        }
                }
                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                battleResult.setDefendBattleArmyInfo(defendBattleArmyInfo);
                battleResult.setDefenseArmyTotalCost(defendCost);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);

                //恢复战场
                battleProcessor.recoverBattle(battle);
        }

        /**
         * 有归属野地开战
         * 
         * @param attackPlayer
         * @param attackExp
         * @param defendPlayer
         * @param mapField
         * @author Neo
         * @description 方法说明
         */
        public void startMapBattleOnOwnerField(Player attackPlayer, ExpeditionInfo attackExp, Player defendPlayer, MapField mapField) {
                //参战玩家id集合
                Set<Long> joinPlayerIds = new HashSet<Long>();
                //参战城市id集合
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();
                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();
                //生成内存战场对象
                Battle battle = new Battle(attackExp.getType());
                BattleField battleField = new BattleField(mapField, IBattleConstants.BATTLE_FIELD_TYPE_OWNER_FIELD, battle);
                battle.setBattleField(battleField);
                //生成传递到战后的数据对象
                OwnerFieldBattleResult battleResult = new OwnerFieldBattleResult(attackExp.getType(), mapField);
                battle.setBattleResultInfo(battleResult);
                //保存进攻方相关信息
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                //记录参战出征部队
                battleResult.setAttackExpeditionInfo(attackExp);
                battleResult.setAttackCityId(attackExp.getCityId());
                //获取进攻部队的军官
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                //记录进攻部队的兵力
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                double attackCost = 0;
                //过滤进攻方有效兵力
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(expeditionArmy.getArmyId(), attackPlayer.getRacial());
                                if (attackExp.getType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                        //如果是侦查战，非侦察机兵种不能出战
                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                continue;
                                }
                                //累计成本
                                attackCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.ATTACK_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, attackExp.getPlayerId(), attackExp.getCityId()));
                                //数量设置为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                battleResult.setAttackArmyTotalCost(attackCost);
                //记录参战玩家
                joinPlayerIds.add(attackExp.getPlayerId());
                //记录参战城市
                joinCityIds.add(attackExp.getCityId());
                //记录参战部队
                joinExpeditionIds.add(attackExp.getId());

                //保存防守方相关信息
                battleResult.setDefendPlayerId(defendPlayer.getPlayerId());
                //获取防守方驻守部队
                double defendCost = 0;
                List<ExpeditionInfo> defendExps = expeditionProcessor.getTargetStationExpedition(mapField.getMapId());
                if (defendExps != null && !defendExps.isEmpty()) {
                        List<Long> defendExpIds = new LinkedList<Long>();
                        Map<Long, ExpeditionInfo> defendExpMap = new HashMap<Long, ExpeditionInfo>(defendExps.size());
                        ExpeditionInfo maindefendExp = null;
                        //获得所有驻守部队的id
                        for (ExpeditionInfo exp : defendExps) {
                                joinExpeditionIds.add(exp.getId());
                                joinPlayerIds.add(exp.getPlayerId());
                                joinCityIds.add(exp.getCityId());
                                defendExpIds.add(exp.getId());
                                defendExpMap.put(exp.getId(), exp);
                                if (maindefendExp == null) {
                                        maindefendExp = exp;
                                } else {
                                        //如果是玩家自己的出征部队
                                        if (exp.getPlayerId().longValue() == defendPlayer.getPlayerId()) {
                                                //当前选定的住房部队也是玩家自己的部队
                                                if (maindefendExp.getPlayerId().longValue() == defendPlayer.getPlayerId()) {
                                                        if (exp.getArrivalTime().after(maindefendExp.getArrivalTime())) {
                                                                maindefendExp = exp;
                                                        }
                                                } else {
                                                        maindefendExp = exp;
                                                }
                                        } else {//否则判断当前选定的主防部队是否是玩家自己的部队
                                                if (maindefendExp.getPlayerId().longValue() != defendPlayer.getPlayerId()) {
                                                        //双方都不是防守玩家的部队，判断后到达部队为主防部队
                                                        if (exp.getArrivalTime().after(maindefendExp.getArrivalTime())) {
                                                                maindefendExp = exp;
                                                        }
                                                }
                                        }
                                }
                        }
                        battleResult.setDefendExpeditionInfoMap(defendExpMap);
                        if (maindefendExp.getGeneralId() != null) {
                                OfficerVo defendOfficer = officerService.getCityOfficerVo(maindefendExp.getGeneralId());
                                battleResult.setDefendOfficer(defendOfficer);
                        }
                        Map<Long, List<ExpeditionArmy>> defendExpArmyMap = expeditionProcessor.getExpeditionArmysByExpeditionId(defendExpIds);
                        battleResult.setDefendExpeditionArmyMap(defendExpArmyMap);
                        List<BattleArmyInfo> defendBattleArmyInfos = new LinkedList<BattleArmyInfo>();
                        for (ExpeditionInfo exp : defendExpMap.values()) {
                                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, exp.getId());
                                if (exp.getId().longValue() == maindefendExp.getId())
                                        defendBattleArmyInfo.setMain(true);
                                List<ExpeditionArmy> defendExpeditionArmys = defendExpArmyMap.get(exp.getId());
                                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendExpeditionArmys.size());
                                //过滤防守方有效兵力
                                for (Iterator<ExpeditionArmy> it = defendExpeditionArmys.iterator(); it.hasNext();) {
                                        ExpeditionArmy expeditionArmy = it.next();
                                        if (expeditionArmy.getArmyCount() > 0) {
                                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(expeditionArmy.getArmyId(), defendPlayer.getRacial());
                                                if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                                        //如果是侦查战，非侦察机兵种不能出战
                                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                                continue;
                                                }
                                                //累计成本
                                                defendCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, defendPlayer.getRacial());
                                                //生成一个战场兵力对象
                                                BattleArmy battleArmy = new BattleArmy();
                                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                                battleArmy.setIsmain(defendBattleArmyInfo.isMain() ? GameConstants.YES : GameConstants.NO);
                                                battleArmys.add(battleArmy);
                                                defendBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, exp.getPlayerId(), exp.getCityId()));
                                                //数量设置为0
                                                expeditionArmy.setArmyCount(0);
                                                updateExpeditionArmys.add(expeditionArmy);
                                        }
                                }
                                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                                defendBattleArmyInfos.add(defendBattleArmyInfo);
                        }
                        battleResult.setDefendBattleArmyInfos(defendBattleArmyInfos);
                        battleResult.setDefenseArmyTotalCost(defendCost);

                        //如果防守方没有任何兵力出阵,不进行战斗，直接进入战后处理
                        if (defendCost == 0) {
                                battle.setResult(IBattleConstants.BATTLE_RESULT_DEFEND_SIDE_RUNAWAY);
                                BattleResult result = battle.buildBattleResultInfo();
                                battleFinishListener.onBattleFinish(result);
                                return;
                        }
                } else {
                        //如果防守方没有任何兵力出阵,不进行战斗，直接进入战后处理
                        battle.setResult(IBattleConstants.BATTLE_RESULT_DEFEND_SIDE_RUNAWAY);
                        BattleResult result = battle.buildBattleResultInfo();
                        battleFinishListener.onBattleFinish(result);
                        return;
                }

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinExpIds(joinExpeditionIds);
                battle.setJoinCityIds(joinCityIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                //保存战斗
                battle.startBattle();

                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //攻守双方都有有效的出战兵力
                        expeditionProcessor.changeExpeditionInfoToBattle(new LinkedList<Long>(joinExpeditionIds));
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //野地进入战斗状态
                        mapService.battleStart(mapField.getField());

                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        //保存参战兵力
                        battleProcessor.recordBattleArmy(battleArmys);
                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("startMapBattleOnOwnerField生成战场出错");
                        ex.printStackTrace();
                        txManager.rollback(status);
                }
        }

        /**
         * 有归属超能源野地开战
         * 
         * @param attackPlayer
         * @param attackExp
         * @param defendPlayer
         * @param mapNuclearField
         * @author Neo
         * @description 方法说明
         */
        public void startMapBattleOnOwnerNuclearField(Player attackPlayer, ExpeditionInfo attackExp, Player defendPlayer, MapNuclearField mapNuclearField) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();
                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();

                Battle battle = new Battle(attackExp.getType());
                BattleField battleField = new BattleField(mapNuclearField, IBattleConstants.BATTLE_FIELD_TYPE_OWNER_NUCLEARFIELD, battle);
                battle.setBattleField(battleField);
                //
                OwnerNuclearFieldBattleResult battleResult = new OwnerNuclearFieldBattleResult(attackExp.getType(), mapNuclearField);
                battle.setBattleResultInfo(battleResult);
                //保存进攻方相关信息
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                battleResult.setAttackExpeditionInfo(attackExp);
                battleResult.setAttackCityId(attackExp.getCityId());

                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                //过滤进攻方有效兵力
                double attackCost = 0;
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(expeditionArmy.getArmyId(), attackPlayer.getRacial());
                                if (attackExp.getType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                        //如果是侦查战，非侦察机兵种不能出战
                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                continue;
                                }
                                attackCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.ATTACK_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, attackExp.getPlayerId(), attackExp.getCityId()));
                                //设置兵力为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                //获得进攻方兵力成本
                battleResult.setAttackArmyTotalCost(attackCost);
                //记录参战玩家
                joinPlayerIds.add(attackExp.getPlayerId());
                //记录参战城市
                joinCityIds.add(attackExp.getCityId());
                //记录出征部队
                joinExpeditionIds.add(attackExp.getId());

                //保存防守方相关信息
                battleResult.setDefendPlayerId(defendPlayer.getPlayerId());
                List<ExpeditionInfo> defendExps = expeditionProcessor.getTargetStationExpedition(mapNuclearField.getMapId());
                double defendCost = 0;
                if (defendExps != null && !defendExps.isEmpty()) {
                        List<Long> defendExpIds = new LinkedList<Long>();
                        Map<Long, ExpeditionInfo> defendExpMap = new HashMap<Long, ExpeditionInfo>(defendExps.size());
                        ExpeditionInfo maindefendExp = null;
                        //获得所有驻守部队的id
                        for (ExpeditionInfo exp : defendExps) {
                                joinExpeditionIds.add(exp.getId());
                                joinPlayerIds.add(exp.getPlayerId());
                                joinCityIds.add(exp.getCityId());
                                defendExpIds.add(exp.getId());
                                defendExpMap.put(exp.getId(), exp);
                                if (maindefendExp == null) {
                                        maindefendExp = exp;
                                } else {
                                        //如果是玩家自己的出征部队
                                        if (exp.getPlayerId().longValue() == defendPlayer.getPlayerId()) {
                                                //当前选定的住房部队也是玩家自己的部队
                                                if (maindefendExp.getPlayerId().longValue() == defendPlayer.getPlayerId()) {
                                                        if (exp.getArrivalTime().after(maindefendExp.getArrivalTime())) {
                                                                maindefendExp = exp;
                                                        }
                                                } else {
                                                        maindefendExp = exp;
                                                }
                                        } else {//否则判断当前选定的主防部队是否是玩家自己的部队
                                                if (maindefendExp.getPlayerId().longValue() != defendPlayer.getPlayerId()) {
                                                        //双方都不是防守玩家的部队，判断后到达部队为主防部队
                                                        if (exp.getArrivalTime().after(maindefendExp.getArrivalTime())) {
                                                                maindefendExp = exp;
                                                        }
                                                }
                                        }
                                }
                        }
                        battleResult.setDefendExpeditionInfoMap(defendExpMap);
                        if (maindefendExp.getGeneralId() != null) {
                                OfficerVo defendOfficer = officerService.getCityOfficerVo(maindefendExp.getGeneralId());
                                battleResult.setDefendOfficer(defendOfficer);
                        }
                        Map<Long, List<ExpeditionArmy>> defendExpArmyMap = expeditionProcessor.getExpeditionArmysByExpeditionId(defendExpIds);
                        battleResult.setDefendExpeditionArmyMap(defendExpArmyMap);
                        List<BattleArmyInfo> defendBattleArmyInfos = new LinkedList<BattleArmyInfo>();
                        for (ExpeditionInfo exp : defendExpMap.values()) {
                                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, exp.getId());
                                if (exp.getId().longValue() == maindefendExp.getId())
                                        defendBattleArmyInfo.setMain(true);
                                List<ExpeditionArmy> defendExpeditionArmys = defendExpArmyMap.get(exp.getId());
                                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendExpeditionArmys.size());
                                //过滤防守方有效兵力
                                for (Iterator<ExpeditionArmy> it = defendExpeditionArmys.iterator(); it.hasNext();) {
                                        ExpeditionArmy expeditionArmy = it.next();
                                        if (expeditionArmy.getArmyCount() > 0) {
                                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(expeditionArmy.getArmyId(), defendPlayer.getRacial());
                                                if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                                        //如果是侦查战，非侦察机兵种不能出战
                                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                                continue;
                                                }
                                                //累计成本
                                                defendCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, defendPlayer.getRacial());
                                                //生成一个战场兵力对象
                                                BattleArmy battleArmy = new BattleArmy();
                                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                                battleArmy.setIsmain(defendBattleArmyInfo.isMain() ? GameConstants.YES : GameConstants.NO);
                                                battleArmys.add(battleArmy);
                                                defendBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, exp.getPlayerId(), exp.getCityId()));
                                                //数量设置为0
                                                expeditionArmy.setArmyCount(0);
                                                updateExpeditionArmys.add(expeditionArmy);
                                        }
                                }
                                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                                defendBattleArmyInfos.add(defendBattleArmyInfo);
                        }
                        battleResult.setDefendBattleArmyInfos(defendBattleArmyInfos);
                        battleResult.setDefenseArmyTotalCost(defendCost);

                        //如果防守方没有任何兵力出阵,不进行战斗，直接进入战后处理
                        if (defendCost == 0) {
                                battle.setResult(IBattleConstants.BATTLE_RESULT_DEFEND_SIDE_RUNAWAY);
                                BattleResult result = battle.buildBattleResultInfo();
                                battleFinishListener.onBattleFinish(result);
                                return;
                        }
                } else {
                        //如果防守方没有任何兵力出阵,不进行战斗，直接进入战后处理
                        battle.setResult(IBattleConstants.BATTLE_RESULT_DEFEND_SIDE_RUNAWAY);
                        BattleResult result = battle.buildBattleResultInfo();
                        battleFinishListener.onBattleFinish(result);
                        return;
                }
                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);

                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);

                battle.startBattle();

                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //攻守双方都有有效的出战兵力
                        expeditionProcessor.changeExpeditionInfoToBattle(new LinkedList<Long>(joinExpeditionIds));
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //超能源野地进入战斗状态
                        nuclearFieldProcessor.updateFieldNuclearStatus(mapNuclearField.getFieldNuclear().getId(), IMapConstant.STATUS_WAR);

                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        //保存参战兵力
                        battleProcessor.recordBattleArmy(battleArmys);
                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("startMapBattleOnOwnerNuclearField生成战场出错");
                        logger.error("" + ex.getStackTrace());
                        txManager.rollback(status);
                }

        }

        /**
         * 玩家城市开战
         * 
         * @param attackPlayer
         * @param attackExp
         * @param defendPlayer
         * @param mapPlayerCity
         * @author Neo
         * @description 方法说明
         */
        public void startMapBattleOnPlayerCity(Player attackPlayer, ExpeditionInfo attackExp, Player defendPlayer, MapPlayerCity mapPlayerCity) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();
                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();
                //用来保存更新数据库城市兵力的对象
                List<CityArmy> updateCityArmys = new LinkedList<CityArmy>();

                Battle battle = new Battle(attackExp.getType());
                BattleField battleField = new BattleField(mapPlayerCity, IBattleConstants.BATTLE_FIELD_TYPE_PLAYER_CITY, battle);
                battle.setBattleField(battleField);
                //
                PlayerCityBattleResult battleResult = new PlayerCityBattleResult(attackExp.getType(), mapPlayerCity);
                battle.setBattleResultInfo(battleResult);
                //保存进攻方相关信息
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                battleResult.setAttackExpeditionInfo(attackExp);
                battleResult.setAttackCityId(attackExp.getCityId());
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                //过滤进攻方有效兵力
                double attackCost = 0;
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(expeditionArmy.getArmyId(), attackPlayer.getRacial());
                                if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                        //如果是侦查战，非侦察机兵种不能出战
                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                continue;
                                }
                                //获得进攻方兵力成本
                                attackCost = armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.ATTACK_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, attackExp.getPlayerId(), attackExp.getCityId()));
                                //设置数量为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                //获得进攻方兵力成本
                battleResult.setAttackArmyTotalCost(attackCost);
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                battleResult.setAttackExpeditionArmys(expeditionArmys);

                joinPlayerIds.add(attackExp.getPlayerId());
                joinCityIds.add(attackExp.getCityId());
                joinExpeditionIds.add(attackExp.getId());

                //设置防守方数据
                battleResult.setDefendPlayerId(defendPlayer.getPlayerId());
                List<BattleArmyInfo> defendBattleArmyInfos = new LinkedList<BattleArmyInfo>();
                double defendCost = 0;
                List<ExpeditionInfo> defendExps = expeditionProcessor.getTargetStationExpedition(mapPlayerCity.getMapId());
                if (defendExps != null && !defendExps.isEmpty()) {
                        Map<Long, ExpeditionInfo> defendExpMap = new HashMap<Long, ExpeditionInfo>(defendExps.size());
                        List<Long> defendExpIds = new LinkedList<Long>();
                        for (ExpeditionInfo exp : defendExps) {
                                joinPlayerIds.add(exp.getPlayerId());
                                joinCityIds.add(exp.getCityId());
                                joinExpeditionIds.add(exp.getId());
                                defendExpIds.add(exp.getId());
                                defendExpMap.put(exp.getId(), exp);
                        }
                        battleResult.setDefendExpeditionInfoMap(defendExpMap);

                        Map<Long, List<ExpeditionArmy>> defendExpArmyMap = expeditionProcessor.getExpeditionArmysByExpeditionId(defendExpIds);
                        battleResult.setDefendExpeditionArmyMap(defendExpArmyMap);
                        for (ExpeditionInfo exp : defendExpMap.values()) {
                                joinPlayerIds.add(exp.getPlayerId());
                                joinCityIds.add(exp.getCityId());
                                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, exp.getId());
                                List<ExpeditionArmy> defendExpeditionArmys = defendExpArmyMap.get(exp.getId());
                                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendExpeditionArmys.size());
                                //过滤防守方有效兵力
                                for (Iterator<ExpeditionArmy> it = defendExpeditionArmys.iterator(); it.hasNext();) {
                                        ExpeditionArmy expeditionArmy = it.next();
                                        if (expeditionArmy.getArmyCount() > 0) {

                                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(expeditionArmy.getArmyId(), defendPlayer.getRacial());
                                                if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                                        //如果是侦查战，非侦察机兵种不能出战
                                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                                continue;
                                                }
                                                defendCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, defendPlayer.getRacial());
                                                //生成一个战场兵力对象
                                                BattleArmy battleArmy = new BattleArmy();
                                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                                battleArmy.setIsmain(GameConstants.NO);
                                                battleArmys.add(battleArmy);
                                                defendBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, exp.getPlayerId(), exp.getCityId()));
                                                //设置数量为0
                                                expeditionArmy.setArmyCount(0);
                                                updateExpeditionArmys.add(expeditionArmy);
                                        }
                                }
                                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                                defendBattleArmyInfos.add(defendBattleArmyInfo);
                        }
                }

                City city = mapPlayerCity.getCity();
                Officer commander = officerService.getCommanderByCityId(city.getId());
                if (commander != null) {
                        OfficerVo defendOfficerVo = officerService.getCityOfficerVo(commander);
                        battleResult.setDefendOfficer(defendOfficerVo);
                }

                List<CityArmy> cityArmys = armyProcessor.getCityArmyByCityId(city.getId());
                battleResult.setDefendCityArmys(cityArmys);
                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_PLAYER_CITY, city.getId());
                defendBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(cityArmys.size());
                //过滤防守方城市有效兵力
                for (Iterator<CityArmy> it = cityArmys.iterator(); it.hasNext();) {
                        CityArmy cityArmy = it.next();
                        if (cityArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(cityArmy.getArmyId(), defendPlayer.getRacial());
                                if (battle.getBattleType() != IBattleConstants.BATTLE_TYPE_CONQUER) {
                                        //如果不是征服战类型，城防兵种不可出战
                                        if (armyPrototype.getType() == IArmyConstant.CITY_DEFENCE_PROTOTYPE_ID)
                                                continue;
                                } else if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                        //如果是侦查战，非侦察机兵种不能出战
                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                continue;
                                }
                                defendCost += armyProcessor.calculateCityArmyCost(cityArmy, defendPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_PLAYER_CITY);
                                battleArmy.setBelongId(cityArmy.getCityId());
                                battleArmy.setArmyType(cityArmy.getArmyId());
                                battleArmy.setCount(cityArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                defendBattleArmyDetailMap.put(cityArmy.getArmyId(), new BattleArmyDetail(battleArmy, city.getPlayerId(), city.getId()));
                                //设置数量为0
                                cityArmy.setArmyCount(0);
                                updateCityArmys.add(cityArmy);
                        }
                }
                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                defendBattleArmyInfos.add(defendBattleArmyInfo);
                battleResult.setDefendBattleArmyInfos(defendBattleArmyInfos);
                battleResult.setDefenseArmyTotalCost(defendCost);

                joinPlayerIds.add(city.getPlayerId());
                joinCityIds.add(city.getId());

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);

                if (defendCost == 0) {
                        battle.setResult(IBattleConstants.BATTLE_RESULT_DEFEND_SIDE_RUNAWAY);
                        BattleResult result = battle.buildBattleResultInfo();
                        battleFinishListener.onBattleFinish(result);
                        return;
                } else {
                        /*if (defendCost > attackCost) {

                                if (defendCost / attackCost >= IBattleConstants.ATTACK_SIDE_CRASH_RATE) {
                                        logger.error("发生进攻方崩溃，: defendCost : " + defendCost + "  attackCost:" + attackCost);
                                        battle.setResult(IBattleConstants.BATTLE_RESULT_ATTACK_SIDE_CRASH);
                                        BattleResult result = battle.buildBattleResultInfo();
                                        battleFinishListener.onBattleFinish(result);
                                        return;
                                } else if (defendCost / attackCost >= IBattleConstants.ATTACK_SIDE_RUNAWAY_RATE) {
                                        logger.error("发生进攻方望风而逃，: defendCost : " + defendCost + "  attackCost:" + attackCost);
                                        battle.setResult(IBattleConstants.BATTLE_RESULT_ATTACK_SIDE_RUNAWAY);
                                        BattleResult result = battle.buildBattleResultInfo();
                                        battleFinishListener.onBattleFinish(result);
                                        return;
                                }
                        }*/
                } //   FIXME 测试战斗暂时注掉

                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //更新城市兵力
                        armyProcessor.updateCityArmy(updateCityArmys);
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //把出征部队设置为战斗状态
                        expeditionProcessor.changeExpeditionInfoToBattle(new LinkedList<Long>(joinExpeditionIds));
                        //城市进入战斗状态
                        cityProcessor.updateCityStatus(city.getId(), ICityConstant.CITY_STATUS_WAR);

                        battle.startBattle();
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        battleProcessor.recordBattleArmy(battleArmys);
                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("startMapBattleOnPlayerCity生成战场出错");
                        ex.printStackTrace();
                        txManager.rollback(status);
                }
        }

        /**
         * npc城市开战
         * 
         * @param attackPlayer
         * @param attackExp
         * @param defendPlayer
         * @param mapNpcCity
         * @author Neo
         * @description 方法说明
         */
        public void startMapBattleOnNpcCity(Player attackPlayer, ExpeditionInfo attackExp, NpcPlayer defendPlayer, MapNpcCity mapNpcCity) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();

                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();
                //用来保存更新数据库城市兵力的对象
                List<NpcCityArmy> updateNpcCityArmys = new LinkedList<NpcCityArmy>();
                //创建战斗对象
                Battle battle = new Battle(attackExp.getType());
                //创建战场数据对象
                BattleField battleField = new BattleField(mapNpcCity, IBattleConstants.BATTLE_FIELD_TYPE_NPC_CITY, battle);
                //为战斗对象设置战场数据对象
                battle.setBattleField(battleField);
                //创建专属战果数据对象，用来存储一些战前数据传递到战后
                NpcCityBattleResult battleResult = new NpcCityBattleResult(attackExp.getType(), mapNpcCity);
                //为战斗对象设置战果数据对象
                battle.setBattleResultInfo(battleResult);
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                joinPlayerIds.add(attackExp.getPlayerId());
                joinCityIds.add(attackExp.getCityId());
                joinExpeditionIds.add(attackExp.getId());
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                battleResult.setAttackExpeditionInfo(attackExp);
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                battleResult.setAttackCityId(attackExp.getCityId());
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                //过滤进攻方有效兵力
                double attackCost = 0;
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(expeditionArmy.getArmyId(), attackPlayer.getRacial());
                                if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                        //如果是侦查战，非侦察机兵种不能出战
                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                continue;
                                }
                                attackCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                //获得进攻方兵力成本
                battleResult.setAttackArmyTotalCost(attackCost);

                //保存防守方信息
                battleResult.setNpcPlayer(defendPlayer);
                NpcCity npcCity = mapNpcCity.getNpcCity();
                NpcCityOfficer npcCityOfficer = npcCityProcessor.getNpcCityCommanderOfficer(npcCity.getId());
                if (npcCityOfficer != null)
                        battleResult.setDefendOfficer(npcCityOfficer);
                List<NpcCityArmy> npcCityArmys = npcCityProcessor.getNpcCityArmyList(npcCity.getId());
                for (Iterator<NpcCityArmy> iterator = npcCityArmys.iterator(); iterator.hasNext();) {
                        if (iterator.next().getArmyCount() <= 0) {
                                iterator.remove();
                        }
                }
                battleResult.setDefendCityArmys(npcCityArmys);
                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_NPC_CITY, npcCity.getId());
                defendBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(npcCityArmys.size());
                //过滤防守方城市有效兵力
                double defendCost = 0;
                for (Iterator<NpcCityArmy> it = npcCityArmys.iterator(); it.hasNext();) {
                        NpcCityArmy npcCityArmy = it.next();
                        if (npcCityArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(npcCityArmy.getArmyId(), defendPlayer.getRacial());
                                if (battle.getBattleType() != IBattleConstants.BATTLE_TYPE_CONQUER) {
                                        //如果不是征服战类型，城防兵种不可出战
                                        if (armyPrototype.getType() == IArmyConstant.CITY_DEFENCE_PROTOTYPE_ID)
                                                continue;
                                } else if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                        //如果是侦查战，非侦察机兵种不能出战
                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                continue;
                                }
                                defendCost += armyProcessor.calculateNpcCityArmyCost(npcCityArmy);
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_NPC_CITY);
                                battleArmy.setBelongId(npcCityArmy.getCityId());
                                battleArmy.setArmyType(npcCityArmy.getArmyId());
                                battleArmy.setCount(npcCityArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                defendBattleArmyDetailMap.put(npcCityArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                npcCityArmy.setArmyCount(0);
                                updateNpcCityArmys.add(npcCityArmy);
                        }
                }
                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                battleResult.setDefendBattleArmyInfo(defendBattleArmyInfo);
                battleResult.setDefenseArmyTotalCost(defendCost);

                if (defendCost == 0) {
                        battle.setResult(IBattleConstants.BATTLE_RESULT_DEFEND_SIDE_RUNAWAY);
                        BattleResult result = battle.buildBattleResultInfo();
                        battleFinishListener.onBattleFinish(result);
                        return;
                }

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //更新城市兵力
                        npcCityProcessor.updateNpcCityArmy(updateNpcCityArmys);
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //把出征部队设置为战斗状态
                        expeditionProcessor.changeExpeditionInfoToBattle(new LinkedList<Long>(joinExpeditionIds));
                        //城市进入战斗状态
                        npcCityProcessor.updateNpcCityStatusToBattle(npcCity.getId());

                        battle.startBattle();
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        battleProcessor.recordBattleArmy(battleArmys);
                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("startMapBattleOnNpcCity生成战场出错");
                        logger.error("" + ex.getStackTrace());
                        txManager.rollback(status);
                }
        }

        /**
         * 无归属野地开战
         * 
         * @param attackPlayer
         * @param attackExp
         * @param mapField
         * @author Neo
         * @description 方法说明
         */
        public void startMapBattleOnOwnerlessField(Player attackPlayer, ExpeditionInfo attackExp, MapField mapField) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();

                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();
                //用来保存更新数据库城市兵力的对象
                List<FieldArmy> updateFieldArmys = new LinkedList<FieldArmy>();

                //生成战斗对象
                Battle battle = new Battle(attackExp.getType());
                //生成战场数据对象
                BattleField battleField = new BattleField(mapField, IBattleConstants.BATTLE_FIELD_TYPE_OWNERLESS_FIELD, battle);
                battle.setBattleField(battleField);
                //生成一个无归属野地战斗结果封装对象
                OwnerlessFieldBattleResult battleResult = new OwnerlessFieldBattleResult(attackExp.getType(), mapField);
                battle.setBattleResultInfo(battleResult);
                //保存进攻方角色id
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                //保存进攻方出征部队
                battleResult.setAttackExpeditionInfo(attackExp);
                battleResult.setAttackCityId(attackExp.getCityId());
                //保存进攻方军官对象
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                //保存进攻方出征部队兵力信息
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                //过滤进攻方有效兵力
                double attackCost = 0;
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(expeditionArmy.getArmyId(), attackPlayer.getRacial());
                                if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                        //如果是侦查战，非侦察机兵种不能出战
                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                continue;
                                }
                                //获得进攻方兵力成本
                                attackCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                battleResult.setAttackArmyTotalCost(attackCost);

                joinPlayerIds.add(attackExp.getPlayerId());
                joinCityIds.add(attackExp.getCityId());
                joinExpeditionIds.add(attackExp.getId());
                //获取野地兵力
                FieldArmyInfo fieldArmyInfo = mapService.getFieldArmyInfo(mapField.getMapId());
                if (fieldArmyInfo == null) { //野地兵力还未恢复
                        //设置战斗结果，防守方逃走
                        battle.setResult(IBattleConstants.BATTLE_RESULT_DEFEND_SIDE_RUNAWAY);
                        BattleResult result = battle.buildBattleResultInfo();
                        battleFinishListener.onBattleFinish(result);
                        return;
                } else {//野地兵力已经恢复
                        FieldOfficer fieldOfficer = fieldArmyInfo.getFieldOfficer();
                        if (fieldOfficer != null)
                                battleResult.setDefendOfficer(fieldOfficer);
                        List<FieldArmy> fieldArmys = fieldArmyInfo.getFieldArmys();
                        for (Iterator<FieldArmy> iterator = fieldArmys.iterator(); iterator.hasNext();) {
                                if (iterator.next().getFieldArmsCount() <= 0) {
                                        iterator.remove();
                                }
                        }
                        battleResult.setDefendArmys(fieldArmys);
                        BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_FIELD, mapField.getMapId());
                        defendBattleArmyInfo.setMain(true);
                        Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(fieldArmys.size());
                        double defendCost = 0;
                        for (Iterator<FieldArmy> it = fieldArmys.iterator(); it.hasNext();) {
                                FieldArmy fieldArmy = it.next();
                                if (fieldArmy.getFieldArmsCount() > 0) {
                                        ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(fieldArmy.getFieldArmsId(), IArmyConstant.ARMY_RACIAL_2);
                                        if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                                //如果是侦查战，非侦察机兵种不能出战
                                                if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                        continue;
                                        }
                                        defendCost += armyProcessor.calculateFieldArmyCost(fieldArmy);
                                        //生成一个战场兵力对象
                                        BattleArmy battleArmy = new BattleArmy();
                                        battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                        battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                        battleArmy.setBelongId(fieldArmy.getFieldId().longValue());
                                        battleArmy.setArmyType(fieldArmy.getFieldArmsId());
                                        battleArmy.setCount(fieldArmy.getFieldArmsCount());
                                        battleArmy.setIsmain(GameConstants.YES);
                                        battleArmys.add(battleArmy);
                                        defendBattleArmyDetailMap.put(fieldArmy.getFieldArmsId(), new BattleArmyDetail(battleArmy));
                                        //设置数量为0
                                        fieldArmy.setFieldArmsCount(0);
                                        updateFieldArmys.add(fieldArmy);
                                }
                        }
                        defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                        battleResult.setDefendBattleArmyInfo(defendBattleArmyInfo);
                        battleResult.setDefenseArmyTotalCost(defendCost);

                        //比较攻防双方的成本实力，判断防守方是否回逃跑
                        if ((attackCost / defendCost) > IBattleConstants.DEFEND_SIDE_RUNAWAY_RATE) {
                                battle.setResult(IBattleConstants.BATTLE_RESULT_DEFEND_SIDE_RUNAWAY);
                                BattleResult result = battle.buildBattleResultInfo();
                                battleFinishListener.onBattleFinish(result);
                                return;
                        }
                }
                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);

                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //把出征部队设置为战斗状态
                        expeditionProcessor.changeExpeditionInfoToBattle(new LinkedList<Long>(joinExpeditionIds));
                        //更新野地npc部队
                        mapService.updateFieldArmyInfo(updateFieldArmys);
                        //野地进入战斗状态
                        mapService.battleStart(mapField.getField());
                        //保存战斗
                        battle.startBattle();
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        //保存参战兵力
                        battleProcessor.recordBattleArmy(battleArmys);

                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("startMapBattleOnOwnerlessField生成战场出错");
                        logger.error("" + ex.getStackTrace());
                        txManager.rollback(status);
                }
        }

        /**
         * 无归属超能源野地开战
         * 
         * @param attackPlayer
         * @param attackExp
         * @param mapNuclearField
         * @author Neo
         * @description 方法说明
         */
        public void startMapBattleOnOwnerlessNuclearField(Player attackPlayer, ExpeditionInfo attackExp, MapNuclearField mapNuclearField) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();

                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();

                List<FieldNuclearArmy> updateFieldNuclearArmys = new LinkedList<FieldNuclearArmy>();

                Battle battle = new Battle(attackExp.getType());
                BattleField battleField = new BattleField(mapNuclearField, IBattleConstants.BATTLE_FIELD_TYPE_OWNERLESS_NUCLEARFIELD, battle);
                battle.setBattleField(battleField);

                //生成一个无归属野地战斗结果封装对象
                OwnerlessNuclearFieldBattleResult battleResult = new OwnerlessNuclearFieldBattleResult(attackExp.getType(), mapNuclearField);
                battle.setBattleResultInfo(battleResult);

                //保存进攻方军官对象
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                //保存进攻方出征部队
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                battleResult.setAttackExpeditionInfo(attackExp);
                battleResult.setAttackCityId(attackExp.getCityId());
                //保存进攻方出征部队兵力信息
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                //过滤进攻方有效兵力
                double attackCost = 0;
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(expeditionArmy.getArmyId(), attackPlayer.getRacial());
                                if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                        //如果是侦查战，非侦察机兵种不能出战
                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                continue;
                                }
                                //放到循环中重复添加id是为了控制只有有兵力参与战斗的出征部队的id会被记录

                                //获得进攻方兵力成本
                                attackCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                battleResult.setAttackArmyTotalCost(attackCost);

                joinPlayerIds.add(attackExp.getPlayerId());
                joinCityIds.add(attackExp.getCityId());
                joinExpeditionIds.add(attackExp.getId());

                FieldNuclear fieldNuclear = mapNuclearField.getFieldNuclear();
                FieldNuclearOfficerPrototype fieldNuclearOfficerPrototype = nuclearFieldProcessor.getFieldNuclearOfficerPrototype(fieldNuclear.getOfficerId());
                battleResult.setFieldNuclearOfficerPrototype(fieldNuclearOfficerPrototype);
                List<FieldNuclearArmy> fieldNuclearArmys = nuclearFieldProcessor.getFieldNuclearArmies(fieldNuclear.getId());
                for (Iterator<FieldNuclearArmy> iterator = fieldNuclearArmys.iterator(); iterator.hasNext();) {
                        if (iterator.next().getArmyCount() <= 0) {
                                iterator.remove();
                        }
                }
                battleResult.setFieldNuclearArmys(fieldNuclearArmys);
                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPES_NUCLEARFIELD, fieldNuclear.getId());
                defendBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(fieldNuclearArmys.size());
                double defendCost = 0;
                for (Iterator<FieldNuclearArmy> it = fieldNuclearArmys.iterator(); it.hasNext();) {
                        FieldNuclearArmy fieldNuclearArmy = it.next();
                        if (fieldNuclearArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(fieldNuclearArmy.getArmyId(), IArmyConstant.ARMY_RACIAL_2);
                                if (battle.getBattleType() == IBattleConstants.BATTLE_TYPE_SCOUT) {
                                        //如果是侦查战，非侦察机兵种不能出战
                                        if (armyPrototype.getId() != IArmyConstant.ARMY_SCOUT_PLANE_PROTOTYPE_ID)
                                                continue;
                                }
                                defendCost += armyProcessor.calculateNuclearFieldArmyCost(fieldNuclearArmy);
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPES_NUCLEARFIELD);
                                battleArmy.setBelongId(fieldNuclear.getId());
                                battleArmy.setArmyType(fieldNuclearArmy.getArmyId());
                                battleArmy.setCount(fieldNuclearArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                defendBattleArmyDetailMap.put(fieldNuclearArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                fieldNuclearArmy.setArmyCount(0);
                                updateFieldNuclearArmys.add(fieldNuclearArmy);
                        }
                }
                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                battleResult.setDefendBattleArmyInfo(defendBattleArmyInfo);

                battleResult.setDefenseArmyTotalCost(defendCost);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);

                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //把出征部队设置为战斗状态
                        expeditionProcessor.changeExpeditionInfoToBattle(new LinkedList<Long>(joinExpeditionIds));
                        //更新超能源野地npc部队
                        nuclearFieldProcessor.updateNuclearArmies(updateFieldNuclearArmys);
                        //超能源野地进入战斗状态
                        nuclearFieldProcessor.updateNuclearToBattle(fieldNuclear.getId());
                        //保存战斗
                        battle.startBattle();
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        //保存参战兵力
                        battleProcessor.recordBattleArmy(battleArmys);

                        txManager.commit(status);
                } catch (Exception ex) {
                        ex.printStackTrace();
                        logger.error("startMapBattleOnOwnerlessNuclearField生成战场出错");
                        logger.error("" + ex.getStackTrace());
                        txManager.rollback(status);
                }
        }

        /**
         * 约战开战
         * 
         * @author Neo
         * @description 方法说明
         */
        public void startMeetingBattle(MeetBattleInfo meetBattleInfo) {

                if (meetBattleInfo.getStartCityId() == null && meetBattleInfo.getTargetCityId() == null) {
                        meetBattleService.deleteMeetBattleInfo(meetBattleInfo.getId());
                        return;
                }

                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                joinExpeditionIds.add(meetBattleInfo.getId());
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();

                Battle battle = new Battle(IBattleConstants.BATTLE_TYPE_MEETING);
                BattleField battleField = new BattleField(IBattleConstants.BATTLE_FIELD_TYPE_METTING, battle);
                battle.setBattleField(battleField);

                //生成一个无归属野地战斗结果封装对象
                MeetingBattleResult battleResult = new MeetingBattleResult(meetBattleInfo.getId());
                battle.setBattleResultInfo(battleResult);

                Player attackPlayer = playerProcessor.getPlayer(meetBattleInfo.getStartPlayerId());
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                joinPlayerIds.add(meetBattleInfo.getStartPlayerId());
                double attackCost = 0;
                if (meetBattleInfo.getStartCityId() != null) {
                        battleResult.setAttackCityId(meetBattleInfo.getStartCityId());
                        joinCityIds.add(meetBattleInfo.getStartCityId());
                        battleResult.setAttackOfficer(officerService.getCityOfficerVo(meetBattleInfo.getStartGeneralId()));
                        List<MeetBattleArmy> attackMeetBattleArmys =
                                meetBattleInfo.getStartCityId() == null ? new ArrayList<MeetBattleArmy>() : meetBattleService.getMeetBattleArmysByMeetBattleInfoIdAndCityId(
                                        meetBattleInfo.getId(),
                                        meetBattleInfo.getStartCityId());
                        BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_METTING, meetBattleInfo.getId());
                        Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(attackMeetBattleArmys.size());
                        for (MeetBattleArmy meetBattleArmy : attackMeetBattleArmys) {
                                if (meetBattleArmy.getArmyCount() > 0) {
                                        attackCost += armyProcessor.calculateNuclearFieldArmyCost(meetBattleArmy, attackPlayer.getRacial());
                                        //生成一个战场兵力对象
                                        BattleArmy battleArmy = new BattleArmy();
                                        battleArmy.setSide(IBattleConstants.ATTACK_SIDE);
                                        battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_METTING);
                                        battleArmy.setBelongId(meetBattleInfo.getId());
                                        battleArmy.setArmyType(meetBattleArmy.getArmyId());
                                        battleArmy.setCount(meetBattleArmy.getArmyCount());
                                        battleArmy.setIsmain(GameConstants.YES);
                                        battleArmys.add(battleArmy);
                                        attackBattleArmyDetailMap.put(meetBattleArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                }
                        }
                        attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                        battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                        battleResult.setAttackMeetBattleArmys(attackMeetBattleArmys);
                }
                battleResult.setAttackArmyTotalCost(attackCost);
                Player defendPlayer = playerProcessor.getPlayer(meetBattleInfo.getTargetPlayerId());
                battleResult.setDefendPlayerId(defendPlayer.getPlayerId());
                joinPlayerIds.add(defendPlayer.getPlayerId());

                double defendCost = 0;
                if (meetBattleInfo.getTargetCityId() != null) {
                        battleResult.setDefendCityId(meetBattleInfo.getTargetCityId());
                        joinCityIds.add(meetBattleInfo.getTargetCityId());
                        battleResult.setDefendOfficer(officerService.getCityOfficerVo(meetBattleInfo.getTargetGeneralId()));
                        List<MeetBattleArmy> defendMeetBattleArmys =
                                meetBattleInfo.getStartCityId() == null ? new ArrayList<MeetBattleArmy>() : meetBattleService.getMeetBattleArmysByMeetBattleInfoIdAndCityId(
                                        meetBattleInfo.getId(),
                                        meetBattleInfo.getStartCityId());
                        BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_METTING, meetBattleInfo.getId());
                        Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendMeetBattleArmys.size());
                        for (MeetBattleArmy meetBattleArmy : defendMeetBattleArmys) {
                                if (meetBattleArmy.getArmyCount() > 0) {
                                        defendCost += armyProcessor.calculateNuclearFieldArmyCost(meetBattleArmy, attackPlayer.getRacial());
                                        //生成一个战场兵力对象
                                        BattleArmy battleArmy = new BattleArmy();
                                        battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                        battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_METTING);
                                        battleArmy.setBelongId(meetBattleInfo.getId());
                                        battleArmy.setArmyType(meetBattleArmy.getArmyId());
                                        battleArmy.setCount(meetBattleArmy.getArmyCount());
                                        battleArmy.setIsmain(GameConstants.YES);
                                        battleArmys.add(battleArmy);
                                        defendBattleArmyDetailMap.put(meetBattleArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                }
                        }
                        defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                        battleResult.setDefendBattleArmyInfo(defendBattleArmyInfo);
                        battleResult.setDefendMeetBattleArmys(defendMeetBattleArmys);
                }
                battleResult.setDefenseArmyTotalCost(defendCost);

                if (defendCost == 0) {
                        battle.setResult(IBattleConstants.BATTLE_RESULT_DEFEND_SIDE_RUNAWAY);
                        BattleResult result = battle.buildBattleResultInfo();
                        battleFinishListener.onBattleFinish(result);
                        return;
                } else if (attackCost == 0) {
                        battle.setResult(IBattleConstants.BATTLE_RESULT_ATTACK_SIDE_RUNAWAY);
                        BattleResult result = battle.buildBattleResultInfo();
                        battleFinishListener.onBattleFinish(result);
                        return;
                }

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);

                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //保存战斗
                        battle.startBattle();
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        //保存参战兵力
                        battleProcessor.recordBattleArmy(battleArmys);

                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("startMeetingBattle生成战场出错");
                        logger.error("" + ex.getStackTrace());
                        txManager.rollback(status);
                }
        }

        private void filterBuildBattle(Battle battle) {
                if (battle.getResult() == IBattleConstants.BATTLE_RESULT_DEFEND_SIDE_RUNAWAY
                        || battle.getResult() == IBattleConstants.BATTLE_RESULT_ATTACK_SIDE_RUNAWAY
                        || battle.getResult() == IBattleConstants.BATTLE_RESULT_ATTACK_SIDE_CRASH) {
                        return;
                }
                for (IBattleFilter filter : battleFilters) {
                        try {
                                filter.filte(battle);
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                }
        }

        private List<BattleUnit> buildBattleUnit(BattleSide battleSide, BattleArmyInfo battleArmyInfo, int racial) {
                List<BattleUnit> battleUnits = new LinkedList<BattleUnit>();
                for (BattleArmyDetail battleArmyDetail : battleArmyInfo.getArmyDetailMap().values()) {
                        boolean isFound = false;
                        for (BattleUnit battleUnit : battleUnits) {
                                if (battleUnit.getArmyPrototype().getId().intValue() == battleArmyDetail.getArmyType()) {
                                        battleUnit.addBattleArmyDetail(battleArmyDetail);
                                        isFound = true;
                                        break;
                                }
                        }
                        if (!isFound) {
                                BattleUnit battleUnit = new BattleUnit(battleSide);
                                battleUnit.addBattleArmyDetail(battleArmyDetail);
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototype(battleArmyDetail.getArmyType(), racial);
                                battleUnit.setArmyPrototype(armyPrototype);
                                battleUnits.add(battleUnit);
                        }
                }
                return battleUnits;
        }

        private List<BattleUnit> bulidBattleUnit(BattleSide battleSide, List<BattleArmyInfo> battleArmyInfos, int racial) {
                List<BattleUnit> battleUnits = new LinkedList<BattleUnit>();
                for (BattleArmyInfo battleArmyInfo : battleArmyInfos) {
                        for (BattleArmyDetail battleArmyDetail : battleArmyInfo.getArmyDetailMap().values()) {
                                boolean isFound = false;
                                for (BattleUnit battleUnit : battleUnits) {
                                        if (battleUnit.getArmyPrototype().getId().intValue() == battleArmyDetail.getArmyType()) {
                                                battleUnit.addBattleArmyDetail(battleArmyDetail);
                                                isFound = true;
                                                break;
                                        }
                                }
                                if (!isFound) {
                                        BattleUnit battleUnit = new BattleUnit(battleSide);
                                        battleUnit.addBattleArmyDetail(battleArmyDetail);
                                        ArmyPrototype armyPrototype = armyProcessor.getArmyPrototype(battleArmyDetail.getArmyType(), racial);
                                        battleUnit.setArmyPrototype(armyPrototype);
                                        battleUnits.add(battleUnit);
                                }
                        }
                }
                return battleUnits;
        }

        private BattleUnit bulidWallUnit(BattleSide battleSide, int racial) {
                BattleUnit battleUnit = new WallUnit(battleSide);
                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototype(IArmyConstant.ARMY_WALL_PROTOTYPE_ID, racial);
                battleUnit.setArmyPrototype(armyPrototype);
                return battleUnit;
        }

        /**
         * 构建战场进攻方
         * 
         * @param player
         * @param battle
         * @author Neo
         * @description 方法说明
         */
        private void buildAttackBattleSide(Player player, Battle battle) {
                BattleSide battleSide = new BattleSide(player, IBattleConstants.ATTACK_SIDE, battle);
                BattleResult result = battle.buildBattleResultInfo();
                OfficerVo attackOfficer = result.getAttackOfficer();
                if (attackOfficer != null) {
                        BattleCommander battleCommander = new BattleCommander(battleSide, attackOfficer);
                        battleSide.setCommander(battleCommander);
                }
                List<BattleUnit> battleUnits = buildBattleUnit(battleSide, result.getAttackBattleArmyInfo(), player.getRacial());
                battleSide.setBattleUnits(battleUnits);
                battle.setAttackSide(battleSide);
        }

        /**
         * 构建战场防守方（玩家角色）
         * 
         * @param player
         * @param battle
         * @author Neo
         * @description 方法说明
         */
        private void buildDefendBattleSide(Player player, Battle battle) {
                BattleSide battleSide = new BattleSide(player, IBattleConstants.DEFANSE_SIDE, battle);
                BattleResult result = battle.buildBattleResultInfo();
                if (result.getBattleFieldType() == IBattleConstants.BATTLE_FIELD_TYPE_OWNER_FIELD) {
                        OwnerFieldBattleResult ownerFieldBattleResult = (OwnerFieldBattleResult) result;
                        OfficerVo defendOfficer = ownerFieldBattleResult.getDefendOfficer();
                        if (defendOfficer != null) {
                                BattleCommander battleCommander = new BattleCommander(battleSide, defendOfficer);
                                battleSide.setCommander(battleCommander);
                        }
                        List<BattleArmyInfo> battleArmyInfos = ownerFieldBattleResult.getDefendBattleArmyInfos();
                        List<BattleUnit> battleUnits = bulidBattleUnit(battleSide, battleArmyInfos, player.getRacial());
                        battleSide.setBattleUnits(battleUnits);
                } else if (result.getBattleFieldType() == IBattleConstants.BATTLE_FIELD_TYPE_OWNER_NUCLEARFIELD) {
                        OwnerNuclearFieldBattleResult ownerNuclearFieldBattleResult = (OwnerNuclearFieldBattleResult) result;
                        OfficerVo defendOfficer = ownerNuclearFieldBattleResult.getDefendOfficer();
                        if (defendOfficer != null) {
                                BattleCommander battleCommander = new BattleCommander(battleSide, defendOfficer);
                                battleSide.setCommander(battleCommander);
                        }
                        List<BattleArmyInfo> battleArmyInfos = ownerNuclearFieldBattleResult.getDefendBattleArmyInfos();
                        List<BattleUnit> battleUnits = bulidBattleUnit(battleSide, battleArmyInfos, player.getRacial());
                        battleSide.setBattleUnits(battleUnits);
                } else if (result.getBattleFieldType() == IBattleConstants.BATTLE_FIELD_TYPE_PLAYER_CITY) {
                        PlayerCityBattleResult playerCityBattleResult = (PlayerCityBattleResult) result;
                        OfficerVo defendOfficer = playerCityBattleResult.getDefendOfficer();
                        if (defendOfficer != null) {
                                BattleCommander battleCommander = new BattleCommander(battleSide, defendOfficer);
                                battleSide.setCommander(battleCommander);
                        }
                        List<BattleArmyInfo> battleArmyInfos = playerCityBattleResult.getDefendBattleArmyInfos();
                        List<BattleUnit> battleUnits = bulidBattleUnit(battleSide, battleArmyInfos, player.getRacial());
                        if (result.getBattleType() == IBattleConstants.BATTLE_TYPE_CONQUER) {
                                battleUnits.add(bulidWallUnit(battleSide, player.getRacial()));
                        }
                        battleSide.setBattleUnits(battleUnits);
                } else if (result.getBattleFieldType() == IBattleConstants.BATTLE_FIELD_TYPE_METTING) {
                        MeetingBattleResult playerCityBattleResult = (MeetingBattleResult) result;
                        OfficerVo defendOfficer = playerCityBattleResult.getDefendOfficer();
                        if (defendOfficer != null) {
                                BattleCommander battleCommander = new BattleCommander(battleSide, defendOfficer);
                                battleSide.setCommander(battleCommander);
                        }
                        List<BattleArmyInfo> battleArmyInfos = new ArrayList<BattleArmyInfo>();
                        battleArmyInfos.add(playerCityBattleResult.getDefendBattleArmyInfo());
                        List<BattleUnit> battleUnits = bulidBattleUnit(battleSide, battleArmyInfos, player.getRacial());
                        battleSide.setBattleUnits(battleUnits);
                }
                battle.setDefendSide(battleSide);
        }

        /**
         * 构建战场防守方（npc角色）
         * 
         * @param player
         * @param battle
         * @author Neo
         * @description 方法说明
         */
        private void buildDefendBattleSide(NpcPlayer npcPlayer, Battle battle) {
                BattleSide battleSide = new BattleSide(npcPlayer, IBattleConstants.DEFANSE_SIDE, battle);
                BattleResult result = battle.buildBattleResultInfo();
                NpcCityBattleResult npcCityBattleResult = (NpcCityBattleResult) result;
                NpcCityOfficer npcCityOfficer = npcCityBattleResult.getDefendOfficer();
                if (npcCityOfficer != null) {
                        BattleCommander battleCommander = new BattleCommander(battleSide, npcCityOfficer);
                        battleSide.setCommander(battleCommander);
                }
                List<BattleUnit> battleUnits = buildBattleUnit(battleSide, npcCityBattleResult.getDefendBattleArmyInfo(), IArmyConstant.ARMY_RACIAL_2);
                if (result.getBattleType() == IBattleConstants.BATTLE_TYPE_CONQUER) {
                        battleUnits.add(bulidWallUnit(battleSide, IArmyConstant.ARMY_RACIAL_2));
                }
                battleSide.setBattleUnits(battleUnits);
                battle.setDefendSide(battleSide);
        }

        /**
         * 构建战场防守方（非玩家角色）
         * 
         * @param battle
         * @author Neo
         * @description 方法说明
         */
        private void buildDefendBattleSide(Battle battle) {
                BattleSide battleSide = new BattleSide(IBattleConstants.DEFANSE_SIDE, battle);
                BattleResult result = battle.buildBattleResultInfo();
                if (result.getBattleFieldType() == IBattleConstants.BATTLE_FIELD_TYPE_OWNERLESS_FIELD) {
                        OwnerlessFieldBattleResult ownerlessFieldBattleResult = (OwnerlessFieldBattleResult) result;
                        FieldOfficer defendOfficer = ownerlessFieldBattleResult.getDefendOfficer();
                        if (defendOfficer != null) {
                                BattleCommander battleCommander = new BattleCommander(battleSide, defendOfficer);
                                battleSide.setCommander(battleCommander);
                        }
                        List<BattleUnit> battleUnits = buildBattleUnit(battleSide, ownerlessFieldBattleResult.getDefendBattleArmyInfo(), IArmyConstant.ARMY_RACIAL_2);
                        battleSide.setBattleUnits(battleUnits);
                } else if (result.getBattleFieldType() == IBattleConstants.BATTLE_FIELD_TYPE_OWNERLESS_NUCLEARFIELD) {
                        OwnerlessNuclearFieldBattleResult ownerlessNuclearFieldBattleResult = (OwnerlessNuclearFieldBattleResult) result;
                        FieldNuclearOfficerPrototype fieldNuclearOfficerPrototype = ownerlessNuclearFieldBattleResult.getFieldNuclearOfficerPrototype();
                        if (fieldNuclearOfficerPrototype != null) {
                                BattleCommander battleCommander = new BattleCommander(battleSide, fieldNuclearOfficerPrototype);
                                battleSide.setCommander(battleCommander);
                        }
                        List<BattleUnit> battleUnits = buildBattleUnit(battleSide, ownerlessNuclearFieldBattleResult.getDefendBattleArmyInfo(), IArmyConstant.ARMY_RACIAL_2);
                        battleSide.setBattleUnits(battleUnits);
                }
                battle.setDefendSide(battleSide);
        }

        /**
         * 兼容老版本的战场初始化方法，起服使用一次后就不会再使用
         * 
         * @author Neo
         * @description 方法说明
         */
        public void recoverOldBattle(BattleData battleData) {
                int mapIndex = battleData.getMapIndex().intValue();
                MapCell mapCell = mapService.getMapCell(mapIndex);
                // 攻击方部队信息
                String aggressExIdStr = battleData.getAggressExId();
                Integer[] attackIds = Util4Number.parseInteterFromString(aggressExIdStr);
                long attackExpId = attackIds[0].longValue();
                ExpeditionInfo attackExp = expeditionProcessor.getExpeditionInfoByExpeditionId(attackExpId);
                Player attackPlayer = playerProcessor.getPlayer(attackExp.getPlayerId());
                if (mapCell instanceof MapField) {
                        MapField mapField = (MapField) mapCell;
                        Field field = mapField.getField();
                        if (field.getOccupyCityId() == null) {
                                this.recoverOldBattleonOwnerlessField(attackPlayer, attackExp, mapField);
                        } else {
                                // 防守方部队信息
                                String defnedExIdStr = battleData.getDefendExId();
                                Integer[] tempDefendIds = Util4Number.parseInteterFromString(defnedExIdStr);
                                List<Long> defendIds = new ArrayList<Long>(tempDefendIds.length);
                                for (int i = 0; i < tempDefendIds.length; i++) {
                                        defendIds.add(tempDefendIds[i].longValue());
                                }
                                Map<Long, ExpeditionInfo> defendExpMap = expeditionProcessor.getExpeditionInfoByExpIds(defendIds);
                                City belongCity = cityProcessor.getCityById(field.getOccupyCityId());
                                Player defendPlayer = playerProcessor.getPlayer(belongCity.getPlayerId());
                                recoverOldBattleonOwnerField(attackPlayer, attackExp, defendPlayer, defendExpMap, mapField);
                        }
                } else if (mapCell instanceof MapPlayerCity) {
                        MapPlayerCity mapPlayerCity = (MapPlayerCity) mapCell;
                        City city = mapPlayerCity.getCity();
                        Player defendPlayer = playerProcessor.getPlayer(city.getPlayerId());
                        List<ArmyInfo> battleCityArmys = null;
                        try {
                                Object army = toArmyList(battleData);
                                battleCityArmys = (List<ArmyInfo>) army;
                        } catch (Exception ex) {
                                battleCityArmys = new LinkedList<ArmyInfo>();
                                return;
                        }
                        List<CityArmy> cityArmys = new LinkedList<CityArmy>();
                        for (ArmyInfo armyInfo : battleCityArmys) {
                                CityArmy cityArmy = new CityArmy();
                                cityArmy.setCityId(city.getId());
                                cityArmy.setArmyId(armyInfo.getArmyPrototype().getId());
                                cityArmy.setArmyCount(armyInfo.getCount());
                                cityArmys.add(cityArmy);
                        }
                        // 防守方部队信息
                        String defnedExIdStr = battleData.getDefendExId();
                        Map<Long, ExpeditionInfo> defendExpMap = null;
                        if (defnedExIdStr != null && !defnedExIdStr.equals("")) {
                                Integer[] tempDefendIds = Util4Number.parseInteterFromString(defnedExIdStr);
                                List<Long> defendIds = new ArrayList<Long>(tempDefendIds.length);
                                for (int i = 0; i < tempDefendIds.length; i++) {
                                        defendIds.add(tempDefendIds[i].longValue());
                                }
                                defendExpMap = expeditionProcessor.getExpeditionInfoByExpIds(defendIds);
                        }
                        recoverOldBattleonPlayerCity(attackPlayer, attackExp, defendPlayer, cityArmys, defendExpMap, mapPlayerCity);
                } else if (mapCell instanceof MapNpcCity) {
                        MapNpcCity mapNpcCity = (MapNpcCity) mapCell;
                        NpcCity city = mapNpcCity.getNpcCity();
                        NpcPlayer defendPlayer = npcCityProcessor.getNpcPlayerById(city.getPlayerId());
                        List<ArmyInfo> battleCityArmys = null;
                        try {
                                Object army = toArmyList(battleData);
                                battleCityArmys = (List<ArmyInfo>) army;
                        } catch (Exception ex) {
                                battleCityArmys = new LinkedList<ArmyInfo>();
                                return;
                        }
                        List<NpcCityArmy> cityArmys = new LinkedList<NpcCityArmy>();
                        for (ArmyInfo armyInfo : battleCityArmys) {
                                NpcCityArmy cityArmy = new NpcCityArmy();
                                cityArmy.setCityId(city.getId());
                                cityArmy.setArmyId(armyInfo.getArmyPrototype().getId());
                                cityArmy.setArmyCount(armyInfo.getCount());
                                cityArmys.add(cityArmy);
                        }
                        recoverOldBattleonNpcCity(attackPlayer, attackExp, defendPlayer, cityArmys, mapNpcCity);
                } else if (mapCell instanceof MapNuclearField) {
                        MapNuclearField mapNuclearField = (MapNuclearField) mapCell;
                        FieldNuclear fieldNuclear = mapNuclearField.getFieldNuclear();
                        if (fieldNuclear.getPlayerId() == null || fieldNuclear.getPlayerId() == -1) {
                                recoverOldBattleonOwnerlessNuclearField(attackPlayer, attackExp, mapNuclearField);
                        } else {
                                // 防守方部队信息
                                String defnedExIdStr = battleData.getDefendExId();
                                Integer[] tempDefendIds = Util4Number.parseInteterFromString(defnedExIdStr);
                                List<Long> defendIds = new ArrayList<Long>(tempDefendIds.length);
                                for (int i = 0; i < tempDefendIds.length; i++) {
                                        defendIds.add(tempDefendIds[i].longValue());
                                }
                                Map<Long, ExpeditionInfo> defendExpMap = expeditionProcessor.getExpeditionInfoByExpIds(defendIds);
                                Player defendPlayer = playerProcessor.getPlayer(fieldNuclear.getPlayerId());
                                recoverOldBattleonOwnerNuclearField(attackPlayer, attackExp, defendPlayer, defendExpMap, mapNuclearField);
                        }
                }
                battleProcessor.removedBattleData(mapIndex);
        }

        private void recoverOldBattleonNpcCity(Player attackPlayer, ExpeditionInfo attackExp, NpcPlayer defendPlayer, List<NpcCityArmy> npcCityArmys, MapNpcCity mapNpcCity) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();
                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();
                //创建战斗对象
                Battle battle = new Battle(attackExp.getType());
                //创建战场数据对象
                BattleField battleField = new BattleField(mapNpcCity, IBattleConstants.BATTLE_FIELD_TYPE_NPC_CITY, battle);
                //为战斗对象设置战场数据对象
                battle.setBattleField(battleField);
                //创建专属战果数据对象，用来存储一些战前数据传递到战后
                NpcCityBattleResult battleResult = new NpcCityBattleResult(attackExp.getType(), mapNpcCity);
                //为战斗对象设置战果数据对象
                battle.setBattleResultInfo(battleResult);
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                battleResult.setAttackExpeditionInfo(attackExp);
                battleResult.setAttackCityId(attackExp.getCityId());
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                //过滤进攻方有效兵力
                double attackCost = 0;
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                attackCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                //获得进攻方兵力成本
                battleResult.setAttackArmyTotalCost(attackCost);
                joinPlayerIds.add(attackExp.getPlayerId());
                joinCityIds.add(attackExp.getCityId());
                joinExpeditionIds.add(attackExp.getId());

                //保存防守方信息
                battleResult.setNpcPlayer(defendPlayer);
                NpcCity npcCity = mapNpcCity.getNpcCity();

                NpcCityOfficer npcCityOfficer = npcCityProcessor.getNpcCityCommanderOfficer(npcCity.getId());
                if (npcCityOfficer != null)
                        battleResult.setDefendOfficer(npcCityOfficer);
                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_NPC_CITY, npcCity.getId());
                defendBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(npcCityArmys.size());
                //过滤防守方城市有效兵力
                double defendCost = 0;
                for (Iterator<NpcCityArmy> it = npcCityArmys.iterator(); it.hasNext();) {
                        NpcCityArmy npcCityArmy = it.next();
                        if (npcCityArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(npcCityArmy.getArmyId(), defendPlayer.getRacial());
                                if (battle.getBattleType() != IBattleConstants.BATTLE_TYPE_CONQUER) {
                                        //如果不是征服战类型，城防兵种不可出战
                                        if (armyPrototype.getType() == IArmyConstant.CITY_DEFENCE_PROTOTYPE_ID)
                                                continue;
                                }
                                defendCost += armyProcessor.calculateNpcCityArmyCost(npcCityArmy);
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_NPC_CITY);
                                battleArmy.setBelongId(npcCityArmy.getCityId());
                                battleArmy.setArmyType(npcCityArmy.getArmyId());
                                battleArmy.setCount(npcCityArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                defendBattleArmyDetailMap.put(npcCityArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                npcCityArmy.setArmyCount(0);
                        }
                }
                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                battleResult.setDefendBattleArmyInfo(defendBattleArmyInfo);
                battleResult.setDefendCityArmys(npcCityArmys);
                battleResult.setDefenseArmyTotalCost(defendCost);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);

                        battle.startBattle();
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        battleProcessor.recordBattleArmy(battleArmys);
                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("recoverOldBattleonNpcCity生成战场出错");
                        logger.error("" + ex.getStackTrace());
                        txManager.rollback(status);
                }
        }

        private void recoverOldBattleonOwnerlessNuclearField(Player attackPlayer, ExpeditionInfo attackExp, MapNuclearField mapNuclearField) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();
                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();

                List<FieldNuclearArmy> updateFieldNuclearArmys = new LinkedList<FieldNuclearArmy>();

                Battle battle = new Battle(attackExp.getType());
                BattleField battleField = new BattleField(mapNuclearField, IBattleConstants.BATTLE_FIELD_TYPE_OWNERLESS_NUCLEARFIELD, battle);
                battle.setBattleField(battleField);

                //生成一个无归属野地战斗结果封装对象
                OwnerlessNuclearFieldBattleResult battleResult = new OwnerlessNuclearFieldBattleResult(attackExp.getType(), mapNuclearField);
                battle.setBattleResultInfo(battleResult);

                //保存进攻方军官对象
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                //保存进攻方出征部队
                battleResult.setAttackExpeditionInfo(attackExp);
                //保存进攻方出征部队兵力信息
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                //过滤进攻方有效兵力
                double attackCost = 0;
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                //获得进攻方兵力成本
                                attackCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                battleResult.setAttackArmyTotalCost(attackCost);

                joinPlayerIds.add(attackExp.getPlayerId());
                joinCityIds.add(attackExp.getCityId());
                joinExpeditionIds.add(attackExp.getId());

                FieldNuclear fieldNuclear = mapNuclearField.getFieldNuclear();
                FieldNuclearOfficerPrototype fieldNuclearOfficerPrototype = nuclearFieldProcessor.getFieldNuclearOfficerPrototype(fieldNuclear.getOfficerId());
                battleResult.setFieldNuclearOfficerPrototype(fieldNuclearOfficerPrototype);
                List<FieldNuclearArmy> fieldNuclearArmys = nuclearFieldProcessor.getFieldNuclearArmies(fieldNuclear.getId());
                battleResult.setFieldNuclearArmys(fieldNuclearArmys);
                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPES_NUCLEARFIELD, fieldNuclear.getId());
                defendBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(fieldNuclearArmys.size());
                double defendCost = 0;
                for (Iterator<FieldNuclearArmy> it = fieldNuclearArmys.iterator(); it.hasNext();) {
                        FieldNuclearArmy fieldNuclearArmy = it.next();
                        if (fieldNuclearArmy.getArmyCount() > 0) {
                                defendCost += armyProcessor.calculateNuclearFieldArmyCost(fieldNuclearArmy);
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPES_NUCLEARFIELD);
                                battleArmy.setBelongId(fieldNuclear.getId());
                                battleArmy.setArmyType(fieldNuclearArmy.getArmyId());
                                battleArmy.setCount(fieldNuclearArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                defendBattleArmyDetailMap.put(fieldNuclearArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                fieldNuclearArmy.setArmyCount(0);
                                updateFieldNuclearArmys.add(fieldNuclearArmy);
                        }
                }
                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                battleResult.setDefendBattleArmyInfo(defendBattleArmyInfo);

                battleResult.setDefenseArmyTotalCost(defendCost);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);

                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //更新超能源野地npc部队
                        nuclearFieldProcessor.updateNuclearArmies(updateFieldNuclearArmys);
                        //保存战斗
                        battle.startBattle();
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        //保存参战兵力
                        battleProcessor.recordBattleArmy(battleArmys);
                        txManager.commit(status);
                } catch (Exception ex) {
                        ex.printStackTrace();
                        logger.error("startMapBattleOnOwnerlessNuclearField恢复战场出错");
                        logger.error("" + ex.getStackTrace());
                        txManager.rollback(status);
                }
        }

        private void recoverOldBattleonOwnerNuclearField(Player attackPlayer, ExpeditionInfo attackExp, Player defendPlayer, Map<Long, ExpeditionInfo> defendExpMap, MapNuclearField mapNuclearField) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();
                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();

                Battle battle = new Battle(attackExp.getType());
                BattleField battleField = new BattleField(mapNuclearField, IBattleConstants.BATTLE_FIELD_TYPE_OWNER_NUCLEARFIELD, battle);
                battle.setBattleField(battleField);
                //
                OwnerFieldBattleResult battleResult = new OwnerFieldBattleResult(attackExp.getType(), mapNuclearField);
                battle.setBattleResultInfo(battleResult);
                //保存进攻方相关信息
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                battleResult.setAttackExpeditionInfo(attackExp);
                battleResult.setAttackCityId(attackExp.getCityId());
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                //过滤进攻方有效兵力
                double attackCost = 0;
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                attackCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.ATTACK_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, attackExp.getPlayerId(), attackExp.getCityId()));
                                //设置兵力为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                joinPlayerIds.add(attackExp.getPlayerId());
                joinCityIds.add(attackExp.getCityId());
                joinExpeditionIds.add(attackExp.getId());
                //获得进攻方兵力成本
                battleResult.setAttackArmyTotalCost(attackCost);
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                //保存防守方相关信息
                battleResult.setDefendPlayerId(defendPlayer.getPlayerId());
                List<Long> defendExpIds = new LinkedList<Long>();
                //获取出征部队id,同时确定主防部队，确定主防方部队可以确定出征军官
                battleResult.setDefendExpeditionInfoMap(defendExpMap);
                ExpeditionInfo maindefendExp = null;
                for (ExpeditionInfo exp : defendExpMap.values()) {
                        if (maindefendExp == null) {
                                maindefendExp = exp;
                        } else {
                                //如果是玩家自己的出征部队
                                if (exp.getPlayerId().longValue() == defendPlayer.getPlayerId()) {
                                        //当前选定的住房部队也是玩家自己的部队
                                        if (maindefendExp.getPlayerId().longValue() == defendPlayer.getPlayerId()) {
                                                if (exp.getArrivalTime().after(maindefendExp.getArrivalTime())) {
                                                        maindefendExp = exp;
                                                }
                                        } else {
                                                maindefendExp = exp;
                                        }
                                } else {//否则判断当前选定的主防部队是否是玩家自己的部队
                                        if (maindefendExp.getPlayerId().longValue() != defendPlayer.getPlayerId()) {
                                                //双方都不是防守玩家的部队，判断后到达部队为主防部队
                                                if (exp.getArrivalTime().after(maindefendExp.getArrivalTime())) {
                                                        maindefendExp = exp;
                                                }
                                        }
                                }
                        }
                        defendExpIds.add(exp.getId());
                }

                Map<Long, List<ExpeditionArmy>> defendExpArmyMap = expeditionProcessor.getExpeditionArmysByExpeditionId(defendExpIds);
                battleResult.setDefendExpeditionArmyMap(defendExpArmyMap);
                List<BattleArmyInfo> defendBattleArmyInfos = new LinkedList<BattleArmyInfo>();
                double defendCost = 0;
                for (ExpeditionInfo exp : defendExpMap.values()) {
                        joinPlayerIds.add(exp.getPlayerId());
                        joinCityIds.add(exp.getCityId());
                        joinExpeditionIds.add(exp.getId());
                        BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, exp.getId());
                        if (exp.getId().longValue() == maindefendExp.getId())
                                defendBattleArmyInfo.setMain(true);
                        List<ExpeditionArmy> defendExpeditionArmys = defendExpArmyMap.get(exp.getId());
                        Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendExpeditionArmys.size());
                        //过滤防守方有效兵力
                        for (Iterator<ExpeditionArmy> it = defendExpeditionArmys.iterator(); it.hasNext();) {
                                ExpeditionArmy expeditionArmy = it.next();
                                if (expeditionArmy.getArmyCount() > 0) {
                                        defendCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, defendPlayer.getRacial());
                                        //生成一个战场兵力对象
                                        BattleArmy battleArmy = new BattleArmy();
                                        battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                        battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                        battleArmy.setBelongId(expeditionArmy.getExpId());
                                        battleArmy.setArmyType(expeditionArmy.getArmyId());
                                        battleArmy.setCount(expeditionArmy.getArmyCount());
                                        if (defendBattleArmyInfo.isMain())
                                                battleArmy.setIsmain(GameConstants.YES);
                                        battleArmys.add(battleArmy);
                                        defendBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, exp.getPlayerId(), exp.getCityId()));
                                        //设置数量为0
                                        expeditionArmy.setArmyCount(0);
                                        updateExpeditionArmys.add(expeditionArmy);
                                }
                        }
                        defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                        defendBattleArmyInfos.add(defendBattleArmyInfo);
                }
                battleResult.setDefendBattleArmyInfos(defendBattleArmyInfos);
                battleResult.setDefenseArmyTotalCost(defendCost);

                if (maindefendExp.getGeneralId() != null) {
                        OfficerVo defendOfficer = officerService.getCityOfficerVo(maindefendExp.getGeneralId());
                        battleResult.setDefendOfficer(defendOfficer);
                }

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinPlayerIDs(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);

                battle.startBattle();
                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //保存战斗相关数据
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        battleProcessor.recordBattleArmy(battleArmys);
                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("startMapBattleOnOwnerNuclearField生成战场出错");
                        logger.error("" + ex.getStackTrace());
                        txManager.rollback(status);
                }
        }

        private void recoverOldBattleonPlayerCity(
                Player attackPlayer,
                ExpeditionInfo attackExp,
                Player defendPlayer,
                List<CityArmy> cityArmys,
                Map<Long, ExpeditionInfo> defendExpMap,
                MapPlayerCity mapPlayerCity) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();

                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();
                //用来保存更新数据库城市兵力的对象
                List<CityArmy> updateCityArmys = new LinkedList<CityArmy>();

                Battle battle = new Battle(attackExp.getType());
                BattleField battleField = new BattleField(mapPlayerCity, IBattleConstants.BATTLE_FIELD_TYPE_PLAYER_CITY, battle);
                battle.setBattleField(battleField);
                //
                PlayerCityBattleResult battleResult = new PlayerCityBattleResult(attackExp.getType(), mapPlayerCity);
                battle.setBattleResultInfo(battleResult);
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                //保存进攻方相关信息
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                battleResult.setAttackExpeditionInfo(attackExp);
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                battleResult.setAttackCityId(attackExp.getCityId());
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                //过滤进攻方有效兵力
                double attackCost = 0;
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                //获得进攻方兵力成本
                                attackCost = armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.ATTACK_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, attackExp.getPlayerId(), attackExp.getCityId()));
                                //设置数量为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);

                joinPlayerIds.add(attackExp.getPlayerId());
                joinCityIds.add(attackExp.getCityId());
                joinExpeditionIds.add(attackExp.getId());
                //获得进攻方兵力成本
                battleResult.setAttackArmyTotalCost(attackCost);

                //设置防守方数据
                battleResult.setDefendPlayerId(defendPlayer.getPlayerId());
                battleResult.setDefendExpeditionInfoMap(defendExpMap);
                List<Long> defendExpIds = new LinkedList<Long>();
                for (ExpeditionInfo exp : defendExpMap.values()) {
                        defendExpIds.add(exp.getId());
                }
                Map<Long, List<ExpeditionArmy>> defendExpArmyMap = expeditionProcessor.getExpeditionArmysByExpeditionId(defendExpIds);
                battleResult.setDefendExpeditionArmyMap(defendExpArmyMap);
                List<BattleArmyInfo> defendBattleArmyInfos = new LinkedList<BattleArmyInfo>();
                double defendCost = 0;
                for (ExpeditionInfo exp : defendExpMap.values()) {
                        joinPlayerIds.add(exp.getPlayerId());
                        joinCityIds.add(exp.getCityId());
                        joinExpeditionIds.add(exp.getId());
                        BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, exp.getId());
                        List<ExpeditionArmy> defendExpeditionArmys = defendExpArmyMap.get(exp.getId());
                        Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendExpeditionArmys.size());
                        //过滤防守方有效兵力
                        for (Iterator<ExpeditionArmy> it = defendExpeditionArmys.iterator(); it.hasNext();) {
                                ExpeditionArmy expeditionArmy = it.next();
                                if (expeditionArmy.getArmyCount() > 0) {
                                        //放到循环中重复添加id是为了控制只有有兵力参与战斗的出征部队的id会被记录
                                        joinExpeditionIds.add(expeditionArmy.getExpId());
                                        defendCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, defendPlayer.getRacial());
                                        //生成一个战场兵力对象
                                        BattleArmy battleArmy = new BattleArmy();
                                        battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                        battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                        battleArmy.setBelongId(expeditionArmy.getExpId());
                                        battleArmy.setArmyType(expeditionArmy.getArmyId());
                                        battleArmy.setCount(expeditionArmy.getArmyCount());
                                        battleArmys.add(battleArmy);
                                        defendBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, exp.getPlayerId(), exp.getCityId()));
                                        //设置数量为0
                                        expeditionArmy.setArmyCount(0);
                                        updateExpeditionArmys.add(expeditionArmy);
                                }
                        }
                        defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                        defendBattleArmyInfos.add(defendBattleArmyInfo);
                }
                City city = mapPlayerCity.getCity();
                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_PLAYER_CITY, city.getId());
                defendBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(cityArmys.size());
                //过滤防守方城市有效兵力

                for (Iterator<CityArmy> it = cityArmys.iterator(); it.hasNext();) {
                        CityArmy cityArmy = it.next();
                        if (cityArmy.getArmyCount() > 0) {
                                ArmyPrototype armyPrototype = armyProcessor.getArmyPrototypeById(cityArmy.getArmyId(), defendPlayer.getRacial());
                                if (battle.getBattleType() != IBattleConstants.BATTLE_TYPE_CONQUER) {
                                        //如果不是征服战类型，城防兵种不可出战
                                        if (armyPrototype.getType() == IArmyConstant.CITY_DEFENCE_PROTOTYPE_ID)
                                                continue;
                                }
                                defendCost += armyProcessor.calculateCityArmyCost(cityArmy, defendPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_PLAYER_CITY);
                                battleArmy.setBelongId(cityArmy.getCityId());
                                battleArmy.setArmyType(cityArmy.getArmyId());
                                battleArmy.setCount(cityArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                defendBattleArmyDetailMap.put(cityArmy.getArmyId(), new BattleArmyDetail(battleArmy, city.getPlayerId(), city.getId()));
                                //设置数量为0
                                cityArmy.setArmyCount(0);
                                updateCityArmys.add(cityArmy);
                        }
                }
                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                defendBattleArmyInfos.add(defendBattleArmyInfo);
                battleResult.setDefendBattleArmyInfos(defendBattleArmyInfos);

                joinPlayerIds.add(city.getPlayerId());
                joinCityIds.add(city.getId());

                battleResult.setDefendCityArmys(cityArmys);
                battleResult.setDefenseArmyTotalCost(defendCost);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);

                Officer commander = officerService.getCommanderByCityId(city.getId());
                if (commander != null) {
                        OfficerVo defendOfficerVo = officerService.getCityOfficerVo(commander);
                        battleResult.setDefendOfficer(defendOfficerVo);
                }

                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //更新城市兵力
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //城市进入战斗状态
                        battle.startBattle();
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        battleProcessor.recordBattleArmy(battleArmys);
                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("recoverOldBattleonPlayerCity生成战场出错");
                        logger.error("" + ex.getStackTrace());
                        txManager.rollback(status);
                }
        }

        private void recoverOldBattleonOwnerField(Player attackPlayer, ExpeditionInfo attackExp, Player defendPlayer, Map<Long, ExpeditionInfo> defendExpMap, MapField mapField) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();
                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();
                //生成内存战场对象
                Battle battle = new Battle(attackExp.getType());
                BattleField battleField = new BattleField(mapField, IBattleConstants.BATTLE_FIELD_TYPE_OWNER_FIELD, battle);
                battle.setBattleField(battleField);
                //生成传递到战后的数据对象
                OwnerFieldBattleResult battleResult = new OwnerFieldBattleResult(attackExp.getType(), mapField);
                //保存进攻方相关信息
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                battleResult.setAttackExpeditionInfo(attackExp);
                battleResult.setAttackCityId(attackExp.getCityId());
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                double attackCost = 0;
                //过滤进攻方有效兵力
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                //累计成本
                                attackCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.ATTACK_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, attackExp.getPlayerId(), attackExp.getCityId()));
                                //数量设置为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                battleResult.setAttackArmyTotalCost(attackCost);

                joinPlayerIds.add(attackExp.getPlayerId());
                joinCityIds.add(attackExp.getCityId());
                joinExpeditionIds.add(attackExp.getId());

                //保存防守方相关信息
                battleResult.setDefendPlayerId(defendPlayer.getPlayerId());
                List<Long> defendExpIds = new LinkedList<Long>();
                ExpeditionInfo maindefendExp = null;
                //获取出征部队id,同时确定主防部队，确定主防方部队可以确定出征军官
                for (ExpeditionInfo exp : defendExpMap.values()) {
                        if (maindefendExp == null) {
                                maindefendExp = exp;
                        } else {
                                //如果是玩家自己的出征部队
                                if (exp.getPlayerId().longValue() == defendPlayer.getPlayerId()) {
                                        //当前选定的住房部队也是玩家自己的部队
                                        if (maindefendExp.getPlayerId().longValue() == defendPlayer.getPlayerId()) {
                                                if (exp.getArrivalTime().after(maindefendExp.getArrivalTime())) {
                                                        maindefendExp = exp;
                                                }
                                        } else {
                                                maindefendExp = exp;
                                        }
                                } else {//否则判断当前选定的主防部队是否是玩家自己的部队
                                        if (maindefendExp.getPlayerId().longValue() != defendPlayer.getPlayerId()) {
                                                //双方都不是防守玩家的部队，判断后到达部队为主防部队
                                                if (exp.getArrivalTime().after(maindefendExp.getArrivalTime())) {
                                                        maindefendExp = exp;
                                                }
                                        }
                                }
                        }
                        defendExpIds.add(exp.getId());
                }
                battleResult.setDefendExpeditionInfoMap(defendExpMap);
                Map<Long, List<ExpeditionArmy>> defendExpArmyMap = expeditionProcessor.getExpeditionArmysByExpeditionId(defendExpIds);
                battleResult.setDefendExpeditionArmyMap(defendExpArmyMap);
                List<BattleArmyInfo> defendBattleArmyInfos = new LinkedList<BattleArmyInfo>();
                double defendCost = 0;
                for (ExpeditionInfo exp : defendExpMap.values()) {
                        joinPlayerIds.add(exp.getPlayerId());
                        joinCityIds.add(exp.getCityId());
                        joinExpeditionIds.add(exp.getId());
                        BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, exp.getId());
                        if (exp.getId().longValue() == maindefendExp.getId())
                                defendBattleArmyInfo.setMain(true);
                        List<ExpeditionArmy> defendExpeditionArmys = defendExpArmyMap.get(exp.getId());
                        Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(defendExpeditionArmys.size());
                        //过滤防守方有效兵力
                        for (Iterator<ExpeditionArmy> it = defendExpeditionArmys.iterator(); it.hasNext();) {
                                ExpeditionArmy expeditionArmy = it.next();
                                if (expeditionArmy.getArmyCount() > 0) {
                                        //累计成本
                                        defendCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, defendPlayer.getRacial());

                                        //生成一个战场兵力对象
                                        BattleArmy battleArmy = new BattleArmy();
                                        battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                        battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                        battleArmy.setBelongId(expeditionArmy.getExpId());
                                        battleArmy.setArmyType(expeditionArmy.getArmyId());
                                        battleArmy.setCount(expeditionArmy.getArmyCount());
                                        if (defendBattleArmyInfo.isMain())
                                                battleArmy.setIsmain(GameConstants.YES);
                                        battleArmys.add(battleArmy);
                                        defendBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy, exp.getPlayerId(), exp.getCityId()));
                                        //数量设置为0
                                        expeditionArmy.setArmyCount(0);
                                        updateExpeditionArmys.add(expeditionArmy);
                                }
                        }
                        defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                        defendBattleArmyInfos.add(defendBattleArmyInfo);
                }
                battleResult.setDefenseArmyTotalCost(defendCost);
                battleResult.setDefendBattleArmyInfos(defendBattleArmyInfos);

                if (maindefendExp.getGeneralId() != null) {
                        OfficerVo defendOfficer = officerService.getCityOfficerVo(maindefendExp.getGeneralId());
                        battleResult.setDefendOfficer(defendOfficer);
                }

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinPlayerIDs(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(defendPlayer, battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);
                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //保存战斗
                        battle.startBattle();
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        //保存参战兵力
                        battleProcessor.recordBattleArmy(battleArmys);

                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("startMapBattleOnOwnerField生成战场出错");
                        ex.printStackTrace();
                        txManager.rollback(status);
                }
        }

        private void recoverOldBattleonOwnerlessField(Player attackPlayer, ExpeditionInfo attackExp, MapField mapField) {
                Set<Long> joinPlayerIds = new HashSet<Long>();
                Set<Long> joinCityIds = new HashSet<Long>();
                //参战出征部队id集合
                Set<Long> joinExpeditionIds = new HashSet<Long>();
                //参战兵力集合
                List<BattleArmy> battleArmys = new LinkedList<BattleArmy>();
                //用来保存更新数据库出征部队兵力的对象
                List<ExpeditionArmy> updateExpeditionArmys = new LinkedList<ExpeditionArmy>();
                //用来保存更新数据库城市兵力的对象
                List<FieldArmy> updateFieldArmys = new LinkedList<FieldArmy>();

                //生成战斗对象
                Battle battle = new Battle(attackExp.getType());
                //生成战场数据对象
                BattleField battleField = new BattleField(mapField, IBattleConstants.BATTLE_FIELD_TYPE_OWNERLESS_FIELD, battle);
                battle.setBattleField(battleField);
                //生成一个无归属野地战斗结果封装对象
                OwnerlessFieldBattleResult battleResult = new OwnerlessFieldBattleResult(attackExp.getType(), mapField);
                battle.setBattleResultInfo(battleResult);
                //保存进攻方角色id
                battleResult.setAttackPlayerId(attackPlayer.getPlayerId());
                //保存进攻方军官对象
                OfficerVo attactOfficer = officerService.getCityOfficerVo(attackExp.getGeneralId());
                battleResult.setAttackOfficer(attactOfficer);
                //保存进攻方出征部队
                battleResult.setAttackExpeditionInfo(attackExp);
                //保存进攻方出征部队兵力信息
                List<ExpeditionArmy> expeditionArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(attackExp.getId());
                battleResult.setAttackExpeditionArmys(expeditionArmys);
                battleResult.setAttackCityId(attackExp.getCityId());
                BattleArmyInfo attackBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION, attackExp.getId());
                attackBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> attackBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(expeditionArmys.size());
                //过滤进攻方有效兵力
                double attackCost = 0;
                for (Iterator<ExpeditionArmy> it = expeditionArmys.iterator(); it.hasNext();) {
                        ExpeditionArmy expeditionArmy = it.next();
                        if (expeditionArmy.getArmyCount() > 0) {
                                //获得进攻方兵力成本
                                attackCost += armyProcessor.calculateExpeditionArmyCost(expeditionArmy, attackPlayer.getRacial());
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(expeditionArmy.getExpId());
                                battleArmy.setArmyType(expeditionArmy.getArmyId());
                                battleArmy.setCount(expeditionArmy.getArmyCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                attackBattleArmyDetailMap.put(expeditionArmy.getArmyId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                expeditionArmy.setArmyCount(0);
                                updateExpeditionArmys.add(expeditionArmy);
                        }
                }
                attackBattleArmyInfo.setArmyDetailMap(attackBattleArmyDetailMap);
                battleResult.setAttackBattleArmyInfo(attackBattleArmyInfo);
                battleResult.setAttackArmyTotalCost(attackCost);

                joinPlayerIds.add(attackExp.getPlayerId());
                joinCityIds.add(attackExp.getCityId());
                joinExpeditionIds.add(attackExp.getId());

                //获取野地兵力
                FieldArmyInfo fieldArmyInfo = mapService.getFieldArmyInfo(mapField.getMapId());
                FieldOfficer fieldOfficer = fieldArmyInfo.getFieldOfficer();
                battleResult.setDefendOfficer(fieldOfficer);
                List<FieldArmy> fieldArmys = fieldArmyInfo.getFieldArmys();
                BattleArmyInfo defendBattleArmyInfo = new BattleArmyInfo(IBattleConstants.BATTLE_ARMY_TYPE_FIELD, mapField.getMapId());
                defendBattleArmyInfo.setMain(true);
                Map<Integer, BattleArmyDetail> defendBattleArmyDetailMap = new HashMap<Integer, BattleArmyDetail>(fieldArmys.size());
                double defendCost = 0;
                for (Iterator<FieldArmy> it = fieldArmys.iterator(); it.hasNext();) {
                        FieldArmy fieldArmy = it.next();
                        if (fieldArmy.getFieldArmsCount() > 0) {
                                defendCost += armyProcessor.calculateFieldArmyCost(fieldArmy);
                                //生成一个战场兵力对象
                                BattleArmy battleArmy = new BattleArmy();
                                battleArmy.setSide(IBattleConstants.DEFANSE_SIDE);
                                battleArmy.setBelongType(IBattleConstants.BATTLE_ARMY_TYPE_EXPEDITION);
                                battleArmy.setBelongId(fieldArmy.getFieldId().longValue());
                                battleArmy.setArmyType(fieldArmy.getFieldArmsId());
                                battleArmy.setCount(fieldArmy.getFieldArmsCount());
                                battleArmy.setIsmain(GameConstants.YES);
                                battleArmys.add(battleArmy);
                                defendBattleArmyDetailMap.put(fieldArmy.getFieldArmsId(), new BattleArmyDetail(battleArmy));
                                //设置数量为0
                                fieldArmy.setFieldArmsCount(0);
                                updateFieldArmys.add(fieldArmy);
                        }
                }
                defendBattleArmyInfo.setArmyDetailMap(defendBattleArmyDetailMap);
                battleResult.setDefendBattleArmyInfo(defendBattleArmyInfo);
                battleResult.setDefendArmys(fieldArmys);
                battleResult.setDefenseArmyTotalCost(defendCost);

                battle.setJoinPlayerIDs(joinPlayerIds);
                battle.setJoinCityIds(joinCityIds);
                battle.setJoinExpIds(joinExpeditionIds);
                //构建进攻方
                buildAttackBattleSide(attackPlayer, battle);
                //构建防守
                buildDefendBattleSide(battle);
                //进行战场对象属性加成
                filterBuildBattle(battle);

                TransactionStatus status = this.txManager.getTransaction(createDefaultTransactionDefinition());
                try {
                        //更新参战部队兵力
                        expeditionProcessor.updateExpeditionArmy(updateExpeditionArmys);
                        //更新野地npc部队
                        mapService.updateFieldArmyInfo(updateFieldArmys);
                        //保存战斗
                        battle.startBattle();
                        long battleId = battleProcessor.recordBattle(battle);
                        for (BattleArmy battleArmy : battleArmys) {
                                battleArmy.setBattleId(battleId);
                        }
                        //保存参战兵力
                        battleProcessor.recordBattleArmy(battleArmys);
                        txManager.commit(status);
                } catch (Exception ex) {
                        logger.error("recoverOldBattleonOwnerlessField生成战场出错");
                        logger.error("" + ex.getStackTrace());
                        txManager.rollback(status);
                }
        }

        public Object toArmyList(BattleData battleData) throws IOException, ClassNotFoundException {
                StringBuilder file = new StringBuilder();
                file.append(battleObjectUrl);
                String url = battleData.getBattleObjectUrl();
                if (url == null)
                        return null;
                file.append(battleData.getBattleObjectUrl());
                InputStream input = null;
                ObjectInputStream in = null;
                Object battle = null;
                try {
                        input = new FileInputStream(file.toString());
                        in = new ObjectInputStream(input);
                        battle = in.readObject();
                } finally {
                        if (input != null) {
                                input.close();
                        }
                        if (in != null) {
                                in.close();
                        }
                }
                return battle;
        }
}

