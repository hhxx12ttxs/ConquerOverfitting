package com.wistone.ww2Refactor.game.army.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import com.wistone.common.util.SystemControl.IInitializable;
import com.wistone.common.util.SystemControl.InitException;
import com.wistone.ww2Refactor.game.army.Army;
import com.wistone.ww2Refactor.game.army.ArmyUpdateLockKey;
import com.wistone.ww2Refactor.game.army.IArmyConstant;
import com.wistone.ww2Refactor.game.army.action.CureArmyAction;
import com.wistone.ww2Refactor.game.army.action.DisciplineArmyAction;
import com.wistone.ww2Refactor.game.army.db.ArmyDisciplineDAO;
import com.wistone.ww2Refactor.game.army.db.ArmyEscapePrototypeDAO;
import com.wistone.ww2Refactor.game.army.db.ArmyPrototypeDAO;
import com.wistone.ww2Refactor.game.army.db.CityArmyDAO;
import com.wistone.ww2Refactor.game.army.db.EscapeArmyCampDAO;
import com.wistone.ww2Refactor.game.army.db.EscapeArmyRecallDAO;
import com.wistone.ww2Refactor.game.army.db.pojo.ArmyDiscipline;
import com.wistone.ww2Refactor.game.army.db.pojo.ArmyDisciplineExample;
import com.wistone.ww2Refactor.game.army.db.pojo.ArmyEscapePrototype;
import com.wistone.ww2Refactor.game.army.db.pojo.ArmyPrototype;
import com.wistone.ww2Refactor.game.army.db.pojo.ArmyPrototypeExample;
import com.wistone.ww2Refactor.game.army.db.pojo.CityArmy;
import com.wistone.ww2Refactor.game.army.db.pojo.CityArmyExample;
import com.wistone.ww2Refactor.game.army.db.pojo.EscapeArmyCamp;
import com.wistone.ww2Refactor.game.army.db.pojo.EscapeArmyCampExample;
import com.wistone.ww2Refactor.game.army.db.pojo.EscapeArmyRecall;
import com.wistone.ww2Refactor.game.army.db.pojo.EscapeArmyRecallExample;
import com.wistone.ww2Refactor.game.city.db.pojo.City;
import com.wistone.ww2Refactor.game.core.GameProperties;
import com.wistone.ww2Refactor.game.core.SwitchVarContants;
import com.wistone.ww2Refactor.game.core.actionManager.ICureArmyActionListener;
import com.wistone.ww2Refactor.game.core.actionManager.IDisciplineArmyActionListener;
import com.wistone.ww2Refactor.game.core.batch.BatchInfo;
import com.wistone.ww2Refactor.game.core.lock.AbstractLockProcessor;
import com.wistone.ww2Refactor.game.core.lock.ILockKey;
import com.wistone.ww2Refactor.game.core.timerTask.IDayTaskExecuter;
import com.wistone.ww2Refactor.game.expedition.db.ExpeditionArmy;
import com.wistone.ww2Refactor.game.map.db.pojo.FieldArmy;
import com.wistone.ww2Refactor.game.meetbattle.db.pojo.MeetBattleArmy;
import com.wistone.ww2Refactor.game.npc.db.pojo.FieldNuclearArmy;
import com.wistone.ww2Refactor.game.npc.db.pojo.NpcCityArmy;

/**
 * @author wxy
 * @descirption 军队逻辑处理器
 */
public class ArmyProcessor extends AbstractLockProcessor implements IArmyProcessor, IDisciplineArmyActionListener, ICureArmyActionListener, IDayTaskExecuter, IInitializable {

        private static final Logger logger = Logger.getLogger(ArmyProcessor.class);

        private ArmyPrototypeDAO armyPrototypeDAO;
        private CityArmyDAO cityArmyDAO;
        private ArmyDisciplineDAO armyDisciplineDAO;
        private ArmyEscapePrototypeDAO armyEscapePrototypeDAO;
        private EscapeArmyCampDAO escapeArmyCampDAO;
        private EscapeArmyRecallDAO escapeArmyRecallDAO;
        // 缓存<disciplineArmyId,value>
        private Map<Long, DisciplineArmyAction> disciplineArmyActionCache;
        // 造兵队列缓存<building_id，练兵id集合>
        private Map<Long, List<DisciplineArmyAction>> disciplineArmyActionCacheIndex;
        //缓存<伤兵id,value>
        private Map<Long, CureArmyAction> cureArmyActionCache;
        //伤兵治愈队列缓存<city_id,伤兵id集合>
        private Map<Long, List<CureArmyAction>> cureArmyActionCacheIndex;

        //注册锁管理
        @Override
        public List<Class<? extends ILockKey>> registerLockManage() {
                List<Class<? extends ILockKey>> keyClassList = new ArrayList<Class<? extends ILockKey>>();
                keyClassList.add(ArmyUpdateLockKey.class);
                return keyClassList;
        }

        // 计算军队耗粮
        @Override
        public double calculateCityArmyFoodConsume(List<CityArmy> cityArmys, int racial) {
                double totalConsume = 0;
                for (CityArmy cityArmy : cityArmys) {
                        if (cityArmy.getArmyCount() == 0) {
                                continue;
                        }
                        ArmyPrototype prototype = getArmyPrototypeById(cityArmy.getArmyId(), racial);
                        totalConsume += (prototype.getCostFood() * cityArmy.getArmyCount());
                }
                return totalConsume;
        }

        // 计算军队耗粮
        @Override
        public double calculateCityArmyFoodConsume(CityArmy cityArmy, int racial) {
                double totalConsume = 0;

                if (cityArmy.getArmyCount() == 0) {
                        return 0;
                } else {
                        ArmyPrototype prototype = getArmyPrototypeById(cityArmy.getArmyId(), racial);
                        totalConsume += (prototype.getCostFood() * cityArmy.getArmyCount());

                        return totalConsume;
                }
        }

        //计算军队出征耗粮
        @Override
        public double calculateExpeditionArmyFoodConsume(List<ExpeditionArmy> expeditionArmys, int racial) {
                double totalConsume = 0;
                for (ExpeditionArmy cityArmy : expeditionArmys) {
                        ArmyPrototype prototype = getArmyPrototypeById(cityArmy.getArmyId(), racial);
                        totalConsume += (prototype.getCostFood() * cityArmy.getArmyCount());
                }
                return totalConsume;
        }

        //计算军队出征耗粮
        @Override
        public double calculateExpeditionArmyFoodConsume(ExpeditionArmy expeditionArmy, int racial) {
                double totalConsume = 0;

                ArmyPrototype prototype = getArmyPrototypeById(expeditionArmy.getArmyId(), racial);
                totalConsume += (prototype.getCostFood() * expeditionArmy.getArmyCount());

                return totalConsume;
        }

        @Override
        public double calculateExpeditionArmyCost(ExpeditionArmy expeditionArmy, int racial) {
                ArmyPrototype prototype = getArmyPrototypeById(expeditionArmy.getArmyId(), racial);
                double requireFood = prototype.getRequireFood();
                double requireSteel = prototype.getRequireSteel();
                double requireOil = prototype.getRequireOil();
                double requireMineral = prototype.getRequireMineral();
                double cost = expeditionArmy.getArmyCount() * (requireFood + requireSteel + requireOil + requireMineral);
                return cost;
        }

        @Override
        public double calculateFieldArmyCost(FieldArmy fieldArmy) {
                ArmyPrototype prototype = getArmyPrototypeById(fieldArmy.getFieldArmsId(), IArmyConstant.ARMY_RACIAL_2);
                double requireFood = prototype.getRequireFood();
                double requireSteel = prototype.getRequireSteel();
                double requireOil = prototype.getRequireOil();
                double requireMineral = prototype.getRequireMineral();
                double cost = fieldArmy.getFieldArmsCount() * (requireFood + requireSteel + requireOil + requireMineral);
                return cost;
        }

        @Override
        public double calculateNuclearFieldArmyCost(FieldNuclearArmy fieldNuclearArmy) {
                ArmyPrototype prototype = getArmyPrototypeById(fieldNuclearArmy.getArmyId(), IArmyConstant.ARMY_RACIAL_2);
                double requireFood = prototype.getRequireFood();
                double requireSteel = prototype.getRequireSteel();
                double requireOil = prototype.getRequireOil();
                double requireMineral = prototype.getRequireMineral();
                double cost = fieldNuclearArmy.getArmyCount() * (requireFood + requireSteel + requireOil + requireMineral);
                return cost;
        }

        @Override
        public double calculateNuclearFieldArmyCost(MeetBattleArmy meetBattleArmy, int racial) {
                ArmyPrototype prototype = getArmyPrototypeById(meetBattleArmy.getArmyId(), racial);
                double requireFood = prototype.getRequireFood();
                double requireSteel = prototype.getRequireSteel();
                double requireOil = prototype.getRequireOil();
                double requireMineral = prototype.getRequireMineral();
                double cost = meetBattleArmy.getArmyCount() * (requireFood + requireSteel + requireOil + requireMineral);
                return cost;
        }

        @Override
        public double calculateNpcCityArmyCost(NpcCityArmy npcCityArmy) {
                ArmyPrototype prototype = getArmyPrototypeById(npcCityArmy.getArmyId(), IArmyConstant.ARMY_RACIAL_2);
                double requireFood = prototype.getRequireFood();
                double requireSteel = prototype.getRequireSteel();
                double requireOil = prototype.getRequireOil();
                double requireMineral = prototype.getRequireMineral();
                double cost = npcCityArmy.getArmyCount() * (requireFood + requireSteel + requireOil + requireMineral);
                return cost;
        }

        @Override
        public double calculateCityArmyCost(CityArmy cityArmy, int racial) {
                ArmyPrototype prototype = getArmyPrototypeById(cityArmy.getArmyId(), racial);
                double requireFood = prototype.getRequireFood();
                double requireSteel = prototype.getRequireSteel();
                double requireOil = prototype.getRequireOil();
                double requireMineral = prototype.getRequireMineral();
                double cost = cityArmy.getArmyCount() * (requireFood + requireSteel + requireOil + requireMineral);
                return cost;
        }

        @Override
        public double calculateExpeditionArmyCost(List<ExpeditionArmy> expeditionArmies, int racial) {
                double cost = 0;
                for (ExpeditionArmy expeditionArmy : expeditionArmies) {
                        ArmyPrototype prototype = getArmyPrototypeById(expeditionArmy.getArmyId(), racial);
                        if (prototype == null)
                                continue;
                        double requireFood = prototype.getRequireFood();
                        double requireSteel = prototype.getRequireSteel();
                        double requireOil = prototype.getRequireOil();
                        double requireMineral = prototype.getRequireMineral();
                        cost += expeditionArmy.getArmyCount() * (requireFood + requireSteel + requireOil + requireMineral);

                }
                return cost;
        }

        @Override
        public double calculateFieldArmyCost(List<FieldArmy> fieldArmies) {
                double cost = 0;
                for (FieldArmy fieldArmy : fieldArmies) {
                        ArmyPrototype prototype = getArmyPrototypeById(fieldArmy.getFieldArmsId(), IArmyConstant.ARMY_RACIAL_2);
                        if (prototype == null)
                                continue;
                        double requireFood = prototype.getRequireFood();
                        double requireSteel = prototype.getRequireSteel();
                        double requireOil = prototype.getRequireOil();
                        double requireMineral = prototype.getRequireMineral();
                        cost += fieldArmy.getFieldArmsCount() * (requireFood + requireSteel + requireOil + requireMineral);

                }
                return cost;
        }

        @Override
        public double calculateCityArmyCost(List<CityArmy> cityArmies, int racial) {
                double cost = 0;
                for (CityArmy cityArmy : cityArmies) {
                        ArmyPrototype prototype = getArmyPrototypeById(cityArmy.getArmyId(), racial);
                        if (prototype == null)
                                continue;
                        double requireFood = prototype.getRequireFood();
                        double requireSteel = prototype.getRequireSteel();
                        double requireOil = prototype.getRequireOil();
                        double requireMineral = prototype.getRequireMineral();
                        cost += cityArmy.getArmyCount() * (requireFood + requireSteel + requireOil + requireMineral);

                }
                return cost;
        }

        //获得已经建造的城防的占用空间
        @Override
        public int getUsedCityDefenceSpace(long cityId, long buildingId, int racial) {

                CityArmyExample example = new CityArmyExample();
                CityArmyExample.Criteria criteria = example.createCriteria();
                criteria.andCityIdEqualTo(cityId);
                List<CityArmy> cityArmy = cityArmyDAO.selectByExample(example);
                int used = 0;
                for (CityArmy army : cityArmy) {
                        ArmyPrototype armyPrototype = armyPrototypeDAO.selectByPrimaryKey(army.getArmyId(), racial);
                        if (armyPrototype.getType() == IArmyConstant.CITY_DEFENCE_PROTOTYPE_ID) {
                                used += armyPrototype.getRequirePopulation() * army.getArmyCount();
                        }
                }
                List<ArmyDiscipline> armyDiscipline = armyDisciplineDAO.getArmyDisciplineByBuildingId(buildingId);
                for (ArmyDiscipline d : armyDiscipline) {
                        ArmyPrototype armyPrototype = armyPrototypeDAO.getArmyPrototypeById(d.getArmyPrototypeId(), racial);
                        used += armyPrototype.getRequirePopulation() * d.getCount();
                }
                return used;
        }

        //获得逃兵
        @Override
        public Map<Integer, ArmyEscapePrototype> getArmyEscapeMap(int racial) {
                return armyEscapePrototypeDAO.getArmyEscapeMap(racial);
        }

        //获得可以训练的军队原型信息
        @Override
        public List<ArmyPrototype> getEnabledArmyPrototype(int racial) {
                ArmyPrototypeExample example = new ArmyPrototypeExample();
                ArmyPrototypeExample.Criteria criteria = example.createCriteria();
                criteria.andRacialEqualTo(racial).andEnabledEqualTo(true);
                return armyPrototypeDAO.selectByExample(example);
        }

        //异步插入城市军队
        @Override
        public void batchIncreaseCityArmy(long cityId, int armyId, int count) {
                if (count != 0) {
                        CityArmy cityArmy = new CityArmy();
                        cityArmy.setCityId(cityId);
                        cityArmy.setArmyId(armyId);
                        cityArmy.setArmyCount(count);
                        cityArmyDAO.batchIncreaseByPrimaryKey(cityArmy);
                }
        }

        //批量插入或更新城市军队
        @Override
        public int addCityArmy(long cityId, List<CityArmy> records) {
                List<CityArmy> nowArmy = this.getCityArmyByCityId(cityId);
                List<CityArmy> needInsertArmies = new LinkedList<CityArmy>();
                List<CityArmy> needUpdateArmies = new LinkedList<CityArmy>();
                for (CityArmy record : records) {
                        boolean isHaveArmyId = false;//兵种是否存在
                        for (CityArmy cityArmy : nowArmy) {
                                if (record.getArmyId().equals(cityArmy.getArmyId())) {
                                        isHaveArmyId = true;
                                        int armyCount = cityArmy.getArmyCount() + record.getArmyCount();
                                        cityArmy.setArmyCount(armyCount);
                                        needUpdateArmies.add(cityArmy);
                                        break;
                                }
                        }
                        if (isHaveArmyId) {
                                continue;
                        } else {
                                needInsertArmies.add(record);
                        }
                }
                int newArmies = 0, updateArmies = 0;
                if (needInsertArmies != null) {
                        newArmies = this.cityArmyDAO.insert(needInsertArmies);
                }
                if (needUpdateArmies != null) {
                        updateArmies = this.cityArmyDAO.update(needUpdateArmies);
                }
                return newArmies + updateArmies;

        }

        //批量减少城市兵力
        @Override
        public boolean reduceCityArmy(long cityId, List<CityArmy> records) {
                CityArmyExample example = new CityArmyExample();
                example.createCriteria().andCityIdEqualTo(cityId);
                List<CityArmy> cityArmyList = this.cityArmyDAO.selectByExample(example);
                if (cityArmyList == null || cityArmyList.size() <= 0)
                        return false;
                List<CityArmy> needUpdateArmies = new ArrayList<CityArmy>();
                for (CityArmy record : records) {
                        boolean isHaveArmyId = false;//兵种是否存在
                        boolean isEnough = false;//兵力是否足够
                        for (CityArmy cityArmy : cityArmyList) {
                                if (record.getArmyId().equals(cityArmy.getArmyId())) {
                                        isHaveArmyId = true;
                                        int armyCount = cityArmy.getArmyCount() - record.getArmyCount();
                                        isEnough = armyCount >= 0;
                                        cityArmy.setArmyCount(armyCount);
                                        needUpdateArmies.add(cityArmy);
                                        break;
                                }
                        }
                        if (isHaveArmyId && isEnough) {
                                continue;
                        } else {
                                return false;
                        }
                }
                updateCityArmy(needUpdateArmies);
                return true;
        }

        //批量更新城市军队
        @Override
        public int updateCityArmy(List<CityArmy> records) {
                return cityArmyDAO.update(records);
        }

        //获得兵种模型
        @Override
        public ArmyPrototype getArmyPrototypeById(int id, int racial) {
                return armyPrototypeDAO.selectByPrimaryKey(id, racial);
        }

        //获得兵种模型集合
        @Override
        public List<ArmyPrototype> getArmyPrototypeById(List<Integer> ids) {
                ArmyPrototypeExample example = new ArmyPrototypeExample();
                example.createCriteria().andIdIn(ids);
                return armyPrototypeDAO.selectByExample(example);
        }

        //获得全部军队原型信息
        @Override
        public List<ArmyPrototype> getArmyPrototype(int racial) {
                return armyPrototypeDAO.getArmyPrototype(racial);
        }

        //将军种按id排序
        @Override
        public List<ArmyPrototype> getArmyPrototypeOrderID(int racial) {
                return armyPrototypeDAO.getArmyPrototypeOrderID(racial);
        }

        //每天定时清理逃兵/伤兵任务
        @Override
        public void dayTaskExecute(int times) {
                this.clearEscapeArmyByUpdate();

        }

        //action的初始化
        @Override
        public void init() throws InitException {

                disciplineArmyActionCache = new ConcurrentHashMap<Long, DisciplineArmyAction>(2048);
                disciplineArmyActionCacheIndex = new ConcurrentHashMap<Long, List<DisciplineArmyAction>>(2048);
                cureArmyActionCache = new ConcurrentHashMap<Long, CureArmyAction>(2048);
                cureArmyActionCacheIndex = new ConcurrentHashMap<Long, List<CureArmyAction>>(2048);

                //加载正在训练中的军队(status区分,0为训练中,1为队列中)
                ArmyDisciplineExample armyDisciplineExample = new ArmyDisciplineExample();
                ArmyDisciplineExample.Criteria criteria1 = armyDisciplineExample.createCriteria();
                criteria1.andStatusEqualTo(IArmyConstant.DISCIPLINING_STATUS);
                List<ArmyDiscipline> armyDisciplineList = armyDisciplineDAO.selectByExample(armyDisciplineExample);
                if (armyDisciplineList != null && armyDisciplineList.size() > 0) {
                        for (ArmyDiscipline armyDiscipline : armyDisciplineList) {
                                DisciplineArmyAction disciplineArmyAction = new DisciplineArmyAction(armyDiscipline);
                                disciplineArmyActionCache.put(armyDiscipline.getId(), disciplineArmyAction);
                                if (disciplineArmyActionCacheIndex.containsKey(armyDiscipline.getBuildingId())) {
                                        disciplineArmyActionCacheIndex.get(armyDiscipline.getBuildingId()).add(disciplineArmyAction);
                                } else {
                                        List<DisciplineArmyAction> disciplineArmyActionList = new ArrayList<DisciplineArmyAction>();
                                        disciplineArmyActionList.add(disciplineArmyAction);
                                        disciplineArmyActionCacheIndex.put(armyDiscipline.getBuildingId(), disciplineArmyActionList);
                                }
                        }
                }

                //加载治愈伤兵队列escape_army_camp的type字段(0为逃兵,1为伤兵)
                EscapeArmyCampExample escapeArmyCampExample = new EscapeArmyCampExample();
                EscapeArmyCampExample.Criteria criteria2 = escapeArmyCampExample.createCriteria();
                criteria2.andTypeEqualTo(IArmyConstant.TYPE_INJURE);
                List<EscapeArmyCamp> escapeArmyCampList = escapeArmyCampDAO.selectByExample(escapeArmyCampExample);
                if (escapeArmyCampList != null && escapeArmyCampList.size() > 0) {
                        for (EscapeArmyCamp escapeArmyCamp : escapeArmyCampList) {
                                CureArmyAction cureArmyAction = new CureArmyAction(escapeArmyCamp);
                                cureArmyActionCache.put(escapeArmyCamp.getId(), cureArmyAction);
                                if (cureArmyActionCacheIndex.containsKey(escapeArmyCamp.getCityId())) {
                                        cureArmyActionCacheIndex.get(escapeArmyCamp.getCityId()).add(cureArmyAction);
                                } else {
                                        List<CureArmyAction> cureArmyActionList = new ArrayList<CureArmyAction>();
                                        cureArmyActionList.add(cureArmyAction);
                                        cureArmyActionCacheIndex.put(escapeArmyCamp.getCityId(), cureArmyActionList);
                                }

                        }
                }
        }

        //练兵任务完成后
        @Override
        public void onDisciplineArmyFinished(DisciplineArmyAction action) {
                logger.debug("onDisciplineArmyFinished  : 执行了");
                //获得城池
                City city = action.getCity();
                ArmyDiscipline armyDiscipline = action.getArmyDiscipline();
                // 当前时间
                long now = System.currentTimeMillis();
                //练兵完成逻辑处理管理
                if (armyDiscipline.getRequireTime() + armyDiscipline.getStartTime() > now) {
                        return;
                }
                //增加城市军队
                CityArmy cityArmy = new CityArmy();
                cityArmy.setCityId(city.getId());
                cityArmy.setArmyId(armyDiscipline.getArmyPrototypeId());
                cityArmy.setArmyCount(armyDiscipline.getCount());
                List<CityArmy> saveOrUpdateArmies = new LinkedList<CityArmy>();
                saveOrUpdateArmies.add(cityArmy);
                this.addCityArmy(city.getId(), saveOrUpdateArmies);

                //上一条训练完成后，将最近的一条训练置为”当前训练中
                if (armyDiscipline != null && armyDiscipline.getId() > 0) {
                        armyDisciplineDAO.removeArmyDiscipline(armyDiscipline.getId());
                        List<ArmyDiscipline> armyDisciplines = this.getArmyDisciplineByBuildingId(armyDiscipline.getBuildingId());
                        // 后训练队列剩余数量
                        if (armyDisciplines.size() == 0) {
                                return;
                        } else {
                                Collections.sort(armyDisciplines);
                                // 计算队列元素需要修正的完成时间
                                if (armyDiscipline.getStatus() == IArmyConstant.DISCIPLINING_STATUS) {
                                        // 移除的训练队列为首个训练队列，设置后续训练队列
                                        // 如果是正在训练的项目
                                        long lastestFinishTime = armyDiscipline.getStartTime() + armyDiscipline.getRequireTime();
                                        long nextStartTime;
                                        if (lastestFinishTime <= now) {
                                                nextStartTime = armyDiscipline.getStartTime() + armyDiscipline.getRequireTime();
                                        } else {
                                                nextStartTime = now;
                                        }
                                        ArmyDiscipline first = armyDisciplines.get(0);
                                        first.setStatus(IArmyConstant.DISCIPLINING_STATUS);
                                        first.setStartTime(nextStartTime);
                                        armyDisciplineDAO.updateArmyDiscipline(first);
                                }
                        }
                }
                //增加军官经验和玩家声望:OfferProcessor和PlayerProcessor中实现
                // 从持久层移除
                armyDisciplineDAO.deleteByPrimaryKey(armyDiscipline.getId());
                // 完成从缓存移除
                if (disciplineArmyActionCache.get(armyDiscipline.getId()) != null) {
                        disciplineArmyActionCache.remove(armyDiscipline.getId());
                }

        }

        //完成治愈伤兵任务
        @Override
        public void onCureWoundArmyFinished(CureArmyAction action) {
                long escapeArmyCampId = action.getEscapeArmyCamp().getId();
                long cityId = action.getEscapeArmyCamp().getCityId();
                //  减少伤兵营兵数，治疗后，扣除伤兵营已经治疗伤兵的数量
                List<EscapeArmyCamp> escapeArmyCampList = this.getEscapeArmyCampList(cityId, IArmyConstant.TYPE_INJURE);
                if (escapeArmyCampList != null && escapeArmyCampList.size() > 0) {
                        // 组合从数据库中查出来的数据
                        Map<Integer, Army> armyInfos = new HashMap<Integer, Army>();
                        for (EscapeArmyCamp escapeArmyCamp : escapeArmyCampList) {
                                Army army = armyInfos.get(escapeArmyCamp.getArmyId());
                                if (army == null) {
                                        army = new Army();
                                        army.setId(escapeArmyCamp.getArmyId());
                                        army.setCount(escapeArmyCamp.getArmyCount());
                                        armyInfos.put(escapeArmyCamp.getArmyId(), army);
                                } else {
                                        army.setCount(army.getCount() + escapeArmyCamp.getArmyCount());
                                }
                        }
                        Map<Integer, Army> leftArmyInfos = new HashMap<Integer, Army>();
                        for (Map.Entry<Integer, Army> entry : armyInfos.entrySet()) {
                                //数据库剩余
                                for (Army army : armyInfos.values()) {
                                        if (entry.getKey().intValue() == army.getId()) {
                                                Army a = new Army();
                                                a.setId(army.getId());
                                                // 扣除数量
                                                a.setCount((entry.getValue().getCount() - army.getCount()) <= 0 ? 0 : (entry.getValue().getCount() - army.getCount()));
                                                leftArmyInfos.put(army.getId(), a);
                                                break;
                                        }
                                }
                        }
                        // 剩余数量
                        for (Map.Entry<Integer, Army> entry : leftArmyInfos.entrySet()) {
                                Army army = armyInfos.get(entry.getKey());
                                if (army != null) {
                                        armyInfos.remove(entry.getKey());
                                        armyInfos.put(entry.getKey(), entry.getValue());
                                }
                        }
                        // 将剩余数量填写到数据库,清理伤兵营
                        this.deleteEscapeArmyCamp(cityId, IArmyConstant.TYPE_INJURE);
                        for (Map.Entry<Integer, Army> entry : armyInfos.entrySet()) {
                                if (entry.getValue().getCount() > 0) {
                                        this.addEscapeArmy(cityId, entry.getKey(), entry.getValue().getCount(), IArmyConstant.TYPE_INJURE);
                                }
                        }
                }

                // 从持久层移除
                escapeArmyCampDAO.deleteByPrimaryKey(escapeArmyCampId);

                // 完成从缓存移除
                if (cureArmyActionCache.get(escapeArmyCampId) != null)
                        cureArmyActionCache.remove(escapeArmyCampId);

        }

        //获得造兵任务
        @Override
        public List<DisciplineArmyAction> getFinishDisciplineArmy() {
                List<DisciplineArmyAction> finishActions = null;
                for (Iterator<DisciplineArmyAction> it = disciplineArmyActionCache.values().iterator(); it.hasNext();) {
                        DisciplineArmyAction disciplineArmyAction = it.next();
                        ArmyDiscipline armyDiscipline = disciplineArmyAction.getArmyDiscipline();
                        if (armyDiscipline.getStartTime() == null) {
                                continue;
                        }
                        if (armyDiscipline.getStartTime() + armyDiscipline.getRequireTime() <= System.currentTimeMillis()) {
                                if (finishActions == null)
                                        finishActions = new LinkedList<DisciplineArmyAction>();
                                finishActions.add(disciplineArmyAction);
                                it.remove();
                        }
                }
                if (finishActions == null) {
                        return Collections.emptyList();
                } else {
                        return finishActions;
                }

        }

        //获得伤兵任务
        @SuppressWarnings("unchecked")
        @Override
        public List<CureArmyAction> getFinishCureArmy() {
                List<CureArmyAction> finishActions = null;
                for (Iterator<CureArmyAction> it = cureArmyActionCache.values().iterator(); it.hasNext();) {
                        CureArmyAction cureArmyAction = it.next();
                        if (cureArmyAction.getFinishTime() <= System.currentTimeMillis()) {
                                if (finishActions == null)
                                        finishActions = new LinkedList<CureArmyAction>();
                                finishActions.add(cureArmyAction);
                                it.remove();
                        }
                }
                return finishActions == null ? Collections.EMPTY_LIST : finishActions;
        }

        //获得城市兵种信息
        @Override
        public List<CityArmy> getCityArmys(long cityId) {
                CityArmyExample cityArmyExample = new CityArmyExample();
                CityArmyExample.Criteria criteria = cityArmyExample.createCriteria();
                criteria.andCityIdEqualTo(cityId);
                return cityArmyDAO.selectByExample(cityArmyExample);
        }

        //获得兵种原型
        @Override
        public List<ArmyPrototype> getArmyPrototypeByRacial(int racial) {
                ArmyPrototypeExample example = new ArmyPrototypeExample();
                example.createCriteria().andRacialEqualTo(racial);
                return armyPrototypeDAO.selectByExample(example);
        }

        //根据建筑获得训练信息
        @Override
        public List<ArmyDiscipline> getArmyDisciplines(long buildingId) {
                ArmyDisciplineExample example = new ArmyDisciplineExample();
                example.createCriteria().andBuildingIdEqualTo(buildingId);
                return armyDisciplineDAO.selectByExample(example);
        }

        // 创建军队训练信息
        @Override
        public ArmyDiscipline addArmyDiscipline(long playerId, ArmyDiscipline armyDiscipline) {
                // 添加到数据库
                armyDiscipline = armyDisciplineDAO.createArmyDiscipline(armyDiscipline);
                return armyDiscipline;
        }

        //根据 ID 获得 ArmyDiscipline
        @Override
        public ArmyDiscipline getArmyDiscipline(long id) {
                return armyDisciplineDAO.selectByPrimaryKey(id);
        }

        //根据玩家ID获得军队训练信息
        @Override
        public List<ArmyDiscipline> getArmyDisciplineByPlayerId(long playerId) {
                return armyDisciplineDAO.getArmyDisciplineByPlayerId(playerId);
        }

        // 根据城池ID获得军队训练信息
        @Override
        public List<ArmyDiscipline> getArmyDisciplineByCityId(long cityId) {
                return armyDisciplineDAO.getArmyDisciplineByCityId(cityId);
        }

        //根据建筑ID获得军队训练信息
        @Override
        public List<ArmyDiscipline> getArmyDisciplineByBuildingId(long buildingId) {
                return armyDisciplineDAO.getArmyDisciplineByBuildingId(buildingId);
        }

        //根据建筑ID集合获得军队训练信息
        @Override
        public List<ArmyDiscipline> getArmyDisciplineByBuildingId(List<Long> buildingIds) {
                ArmyDisciplineExample example = new ArmyDisciplineExample();
                example.createCriteria().andBuildingIdIn(buildingIds);
                return armyDisciplineDAO.selectByExample(example);
        }

        //根据ID获得军队训练信息
        @Override
        public ArmyDiscipline getArmyDisciplineById(long id) {
                return armyDisciplineDAO.getArmyDisciplineById(id);
        }

        // 根据ID集合获得军队训练信息
        @Override
        public List<ArmyDiscipline> getArmyDisciplineById(List<Integer> armyDisciplineIds) {
                ArmyDisciplineExample example = new ArmyDisciplineExample();
                example.createCriteria().andArmyPrototypeIdIn(armyDisciplineIds);
                return armyDisciplineDAO.selectByExample(example);
        }

        //更新军队训练信息
        @Override
        public void updateArmyDiscipline(ArmyDiscipline armyDiscipline) {
                armyDisciplineDAO.updateArmyDiscipline(armyDiscipline);
        }

        //批量更新军队训练信息
        @Override
        public BatchInfo batchUpdateArmyDiscipline(ArmyDiscipline armyDiscipline) {
                return armyDisciplineDAO.batchUpdateArmyDiscipline(armyDiscipline);
        }

        //创建军队训练信息
        @Override
        public ArmyDiscipline createArmyDiscipline(ArmyDiscipline armyDiscipline) {
                return armyDisciplineDAO.createArmyDiscipline(armyDiscipline);
        }

        //移除军队训练信息
        @Override
        public void removeArmyDiscipline(long disciplineId) {
                ArmyDiscipline armyDiscipline = armyDisciplineDAO.getArmyDisciplineById(disciplineId);
                // 从数据库中移除军队训练信息
                armyDisciplineDAO.removeArmyDiscipline(disciplineId);
                List<ArmyDiscipline> armyDisciplines = getArmyDisciplineByBuildingId(armyDiscipline.getBuildingId());
                if (armyDisciplines.size() == 0) {
                        return;
                } else {
                        TreeSet<ArmyDiscipline> sortedDisciplineQueue = new TreeSet<ArmyDiscipline>();
                        sortedDisciplineQueue.addAll(armyDisciplines);
                        // 计算队列元素需要修正的完成时间
                        if (armyDiscipline.getStatus() == IArmyConstant.DISCIPLINING_STATUS) {
                                // 如果是正在训练的项目
                                long lastestFinishTime = armyDiscipline.getStartTime() + armyDiscipline.getRequireTime();
                                long now = System.currentTimeMillis();
                                long nextStartTime;
                                if (lastestFinishTime <= now) {
                                        nextStartTime = armyDiscipline.getStartTime() + armyDiscipline.getRequireTime();
                                } else {
                                        nextStartTime = now;
                                }
                                ArmyDiscipline first = sortedDisciplineQueue.first();
                                first.setStatus(IArmyConstant.DISCIPLINING_STATUS);
                                first.setStartTime(nextStartTime);
                                armyDisciplineDAO.updateArmyDiscipline(first);
                        }
                }

        }

        //批量移除军队训练信息
        public int removeArmyDiscipline(List<Integer> disciplineIds) {
                List<ArmyDiscipline> armyDisciplineList = this.getArmyDisciplineById(disciplineIds);
                // 从数据库中移除军队训练信息
                armyDisciplineDAO.removeArmyDiscipline(disciplineIds);
                List<Long> longIds = new ArrayList<Long>();
                for (ArmyDiscipline ids : armyDisciplineList) {
                        longIds.add(ids.getBuildingId());
                }
                List<ArmyDiscipline> armyDisciplines = getArmyDisciplineByBuildingId(longIds);
                if (armyDisciplines.size() == 0) {
                        return 0;
                } else {
                        TreeSet<ArmyDiscipline> sortedDisciplineQueue = new TreeSet<ArmyDiscipline>();
                        sortedDisciplineQueue.addAll(armyDisciplines);
                        // 计算队列元素需要修正的完成时间
                        List<ArmyDiscipline> armyDisciplinesFirsts = new ArrayList<ArmyDiscipline>();
                        for (ArmyDiscipline armyDiscipline : armyDisciplineList) {
                                if (armyDiscipline.getStatus() == IArmyConstant.DISCIPLINING_STATUS) {
                                        // 如果是正在训练的项目
                                        long lastestFinishTime = armyDiscipline.getStartTime() + armyDiscipline.getRequireTime();
                                        long now = System.currentTimeMillis();
                                        long nextStartTime;
                                        if (lastestFinishTime <= now) {
                                                nextStartTime = armyDiscipline.getStartTime() + armyDiscipline.getRequireTime();
                                        } else {
                                                nextStartTime = now;
                                        }
                                        ArmyDiscipline first = sortedDisciplineQueue.first();
                                        first.setStatus(IArmyConstant.DISCIPLINING_STATUS);
                                        first.setStartTime(nextStartTime);
                                        armyDisciplinesFirsts.add(first);

                                }
                        }
                        return armyDisciplineDAO.updateArmyDiscipline(armyDisciplinesFirsts);

                }
        }

        //批量移除军队训练信息
        @Override
        public BatchInfo batchRemoveArmyDiscipline(long id) {
                return armyDisciplineDAO.batchRemoveArmyDiscipline(id);
        }

        //根据城池ID移除军队训练信息
        @Override
        public void removeArmyDisciplineByCityId(long cityId) {
                armyDisciplineDAO.removeArmyDisciplineByCityId(cityId);
        }

        //根据城池ID批量移除军队训练信息
        @Override
        public BatchInfo batchRemoveArmyDisciplineByCityId(long cityId) {
                return armyDisciplineDAO.batchRemoveArmyDisciplineByCityId(cityId);
        }

        // 获得所有拨逃兵
        @Override
        public Map<Long, List<EscapeArmyCamp>> countArmyInfoInEscapeArmyCampMap(long cityId, int racial, byte type) {
                Map<Long, List<EscapeArmyCamp>> map = new HashMap<Long, List<EscapeArmyCamp>>();
                EscapeArmyCampExample example = new EscapeArmyCampExample();
                EscapeArmyCampExample.Criteria criteria = example.createCriteria();
                criteria.andCityIdEqualTo(cityId);
                criteria.andTypeEqualTo(type);
                // 只有在回收周期内的逃兵才可以被回收
                criteria.andUpdateTimeGreaterThanOrEqualTo(new Date(System.currentTimeMillis() - (long) GameProperties.getGameCoefficient(SwitchVarContants.ESCAPE_ARMY_VALID_CYCLE_ID) * 3600 * 1000));
                List<EscapeArmyCamp> escapeArmyCampList = this.escapeArmyCampDAO.selectByExample(example);
                List<EscapeArmyCamp> newList = null;
                for (EscapeArmyCamp escapeArmyCamp : escapeArmyCampList) {
                        if (!map.containsKey(escapeArmyCamp.getUpdateTime().getTime())) {
                                newList = new ArrayList<EscapeArmyCamp>();
                                newList.add(escapeArmyCamp);
                                map.put(escapeArmyCamp.getUpdateTime().getTime(), newList);
                        } else {
                                map.get(escapeArmyCamp.getUpdateTime().getTime()).add(escapeArmyCamp);
                        }
                }
                return map;
        }

        /***
         * ===================================================
         * CRUD BaseApi，provide to other Module
         * ===================================================
         ***/

        //根据主键获得逃兵
        @Override
        public ArmyEscapePrototype getArmyEscapePrototype(int armyid, int racial) {
                return armyEscapePrototypeDAO.selectByPrimaryKey(armyid, racial);
        }

        //根据主键获得兵种原型
        @Override
        public ArmyPrototype getArmyPrototype(int id, int racial) {
                return armyPrototypeDAO.selectByPrimaryKey(id, racial);
        }

        //通过主键获得城市军队
        @Override
        public CityArmy getCityArmy(int armyId, long cityId) {
                return cityArmyDAO.selectByPrimaryKey(armyId, cityId);
        }

        //通过主键删除
        @Override
        public int deleteCityArmy(int armyId, long cityId) {
                return cityArmyDAO.deleteByPrimaryKey(armyId, cityId);
        }

        @Override
        public void deleteCityArmy(long cityId) {
                CityArmyExample example = new CityArmyExample();
                example.createCriteria().andCityIdEqualTo(cityId);
                cityArmyDAO.deleteByExample(example);
        }

        //加入新的CityArmy
        @Override
        public void addCityArmy(CityArmy record) {
                cityArmyDAO.insert(record);
        }

        //通过主键更新CityArmy
        @Override
        public int updateCityArmy(CityArmy record) {
                return cityArmyDAO.updateByPrimaryKey(record);
        }

        //插入新训练队列
        @Override
        public void addArmyDiscipline(ArmyDiscipline record) {
                armyDisciplineDAO.insert(record);

        }

        //插入新训练队列
        @Override
        public int addArmyDiscipline(List<ArmyDiscipline> records) {
                return armyDisciplineDAO.insert(records);
        }

        //根据主键删除训练队列
        @Override
        public int deleteArmyDiscipline(long id) {
                return armyDisciplineDAO.deleteByPrimaryKey(id);
        }

        //获得所有逃兵营
        @Override
        public List<EscapeArmyCamp> getEscapeArmyCamps() {
                return escapeArmyCampDAO.selectByExample(new EscapeArmyCampExample());
        }

        //根据ID更新逃兵
        @Override
        public int updateArmyEscapePrototype(ArmyEscapePrototype record) {
                return armyEscapePrototypeDAO.updateByPrimaryKey(record);
        }

        //根据逃兵ID删除
        @Override
        public int deleteArmyEscapePrototype(int armyid, int racial) {
                return armyEscapePrototypeDAO.deleteByPrimaryKey(armyid, racial);
        }

        //根据主键更新兵种
        @Override
        public int updateArmyPrototype(ArmyPrototype record) {
                return armyPrototypeDAO.updateByPrimaryKey(record);
        }

        //根据主键删除兵种
        @Override
        public int deleteArmyPrototype(int id, int racial) {
                return armyPrototypeDAO.deleteByPrimaryKey(id, racial);
        }

        //更新逃兵营
        @Override
        public int updateEscapeArmyCamp(EscapeArmyCamp record) {
                return escapeArmyCampDAO.updateByPrimaryKey(record);
        }

        //批量更新逃兵营
        @Override
        public int updateEscapeArmyCamp(List<EscapeArmyCamp> records) {
                return this.escapeArmyCampDAO.update(records);
        }

        //通过主键查询逃兵营
        @Override
        public EscapeArmyCamp getEscapeArmyCamp(long id) {
                return escapeArmyCampDAO.selectByPrimaryKey(id);
        }

        //根据主键删除逃兵营
        @Override
        public int deleteEscapeArmyCamp(long id) {
                return escapeArmyCampDAO.deleteByPrimaryKey(id);
        }

        //创建逃兵召回
        @Override
        public void addEscapeArmyRecall(EscapeArmyRecall record) {
                escapeArmyRecallDAO.insert(record);

        }

        //根据主键更新召回逃兵
        @Override
        public int updateEscapeArmyRecall(EscapeArmyRecall record) {
                return escapeArmyRecallDAO.updateByPrimaryKey(record);
        }

        //获得一个召回逃兵
        @Override
        public EscapeArmyRecall getEscapeArmyRecall(long id) {
                return escapeArmyRecallDAO.selectByPrimaryKey(id);
        }

        //根据主键删除召回逃兵
        @Override
        public int deleteEscapeArmyRecall(long id) {
                return escapeArmyRecallDAO.deleteByPrimaryKey(id);
        }

        // 增加城池军队
        @Override
        public int addCityArmy(long activeCityId, int armyId, int count) {
                ArmyUpdateLockKey keyLock = new ArmyUpdateLockKey(armyId);
                Lock lock = this.lockManager.getLock(keyLock);
                lock.lock();
                try {
                        int remainCount = 0;
                        CityArmy cityArmy = cityArmyDAO.selectByPrimaryKey(armyId, activeCityId);
                        int armyCount = cityArmy.getArmyCount();
                        if (armyCount > count) {
                                remainCount = armyCount - count;
                        }

                        cityArmy.setArmyCount(remainCount);
                        cityArmyDAO.updateByPrimaryKeySelective(cityArmy);
                        return remainCount;
                } finally {
                        lock.unlock();
                }
        }

        //查询城市军队数量
        @Override
        public int countCityArmyByCityId(long cityId) {
                int count = 0;
                CityArmyExample cityArmyExample = new CityArmyExample();
                CityArmyExample.Criteria criteria = cityArmyExample.createCriteria();
                criteria.andCityIdEqualTo(cityId);
                List<CityArmy> list = this.cityArmyDAO.selectByExample(cityArmyExample);
                if (list != null && list.size() > 0) {
                        for (CityArmy cityArmy : list) {
                                if (cityArmy.getArmyId().intValue() != IArmyConstant.ARMY_PILLBOX_PROTOTYPE_ID
                                        && cityArmy.getArmyId().intValue() != IArmyConstant.ARMY_STRAY_BULLET_PROTOTYPE_ID
                                        && cityArmy.getArmyId().intValue() != IArmyConstant.ARMY_ANTI_TANK_PROTOTYPE_ID
                                        && cityArmy.getArmyId().intValue() != IArmyConstant.ARMY_ANTI_AIRCRAFT_PROTOTYPE_ID) {
                                        count += cityArmy.getArmyCount();
                                }
                        }
                }
                return count;
        }

        /**
         * 抓捕逃兵
         * 将人祸或其他原因引起的逃兵放入逃兵营
         * 或战斗受伤进入伤兵营
         */
        @Override
        public void addEscapeArmy(long cityId, int armyId, int armyCount, byte type) {
                EscapeArmyCamp escapeArmyCamp = new EscapeArmyCamp();
                escapeArmyCamp.setCityId(cityId);
                escapeArmyCamp.setArmyId(armyId);
                escapeArmyCamp.setArmyCount(armyCount);
                escapeArmyCamp.setType(type);
                escapeArmyCamp.setUpdateTime(new Date(System.currentTimeMillis()));
                this.escapeArmyCampDAO.insert(escapeArmyCamp);

        }

        @Override
        public int addEscapeArmy(List<EscapeArmyCamp> escapeArmyCamps) {
                return this.escapeArmyCampDAO.insert(escapeArmyCamps);
        }

        @Override
        public void addRunawayArmy(long cityId, int armyId, int armyCount, Date date) {
                EscapeArmyCamp escapeArmyCamp = new EscapeArmyCamp();
                escapeArmyCamp.setCityId(cityId);
                escapeArmyCamp.setArmyId(armyId);
                escapeArmyCamp.setArmyCount(armyCount);
                escapeArmyCamp.setType(IArmyConstant.TYPE_ESCAPE);
                escapeArmyCamp.setUpdateTime(date);

                this.escapeArmyCampDAO.insert(escapeArmyCamp);

        }

        @Override
        public void addWoundedArmy(long cityId, int armyId, int armyCount) {
                EscapeArmyCamp escapeArmyCamp = new EscapeArmyCamp();
                escapeArmyCamp.setCityId(cityId);
                escapeArmyCamp.setArmyId(armyId);
                escapeArmyCamp.setArmyCount(armyCount);
                escapeArmyCamp.setType(IArmyConstant.TYPE_INJURE);
                escapeArmyCamp.setUpdateTime(new Date(System.currentTimeMillis()));

                this.escapeArmyCampDAO.insert(escapeArmyCamp);

        }

        //清理逃兵营/伤兵营
        @Override
        public void deleteEscapeArmyCamp(long cityId, byte type) {
                deleteEscapeArmyCampByCityIdAndType(cityId, false, type);
        }

        //删除逃兵营/伤兵营
        @Override
        public void deleteEscapeArmyCamp(long cityId, byte type, int armyId) {
                EscapeArmyCampExample example = new EscapeArmyCampExample();
                EscapeArmyCampExample.Criteria criteria = example.createCriteria();
                criteria.andCityIdEqualTo(cityId);
                criteria.andTypeEqualTo(type);
                criteria.andArmyIdEqualTo(armyId);
                escapeArmyCampDAO.deleteByExample(example);
        }

        @Override
        public void deleteEscapeArmyCampByCityIdAndType(long cityId, boolean batchUpdate, byte type) {
                if (batchUpdate) {
                        this.escapeArmyCampDAO.batchDeleteByCityId(cityId, type);
                } else {
                        EscapeArmyCampExample example = new EscapeArmyCampExample();
                        EscapeArmyCampExample.Criteria criteria = example.createCriteria();
                        criteria.andCityIdEqualTo(cityId);
                        criteria.andTypeEqualTo(type);
                        this.escapeArmyCampDAO.deleteByExample(example);
                }
        }

        @Override
        public void deleteEscapeArmyCampByCityId(long cityId, boolean batchUpdate) {
                if (batchUpdate) {
                        this.escapeArmyCampDAO.batchDeleteByCityId(cityId);
                } else {
                        EscapeArmyCampExample example = new EscapeArmyCampExample();
                        EscapeArmyCampExample.Criteria criteria = example.createCriteria();
                        criteria.andCityIdEqualTo(cityId);
                        this.escapeArmyCampDAO.deleteByExample(example);
                }
        }

        @Override
        public List<EscapeArmyCamp> getEscapeArmyCampList(long cityId, byte type) {

                EscapeArmyCampExample example = new EscapeArmyCampExample();
                EscapeArmyCampExample.Criteria criteria = example.createCriteria();
                criteria.andCityIdEqualTo(cityId);
                criteria.andTypeEqualTo(type);
                // 只有在回收周期内的逃兵才可以被回收 及 伤兵才能被治疗
                if (type == IArmyConstant.TYPE_INJURE) {
                        criteria.andUpdateTimeGreaterThanOrEqualTo(new Date(System.currentTimeMillis() - IArmyConstant.INJURE_ARMY_VALID_CYCLE * 3600 * 1000));
                } else {
                        criteria.andUpdateTimeGreaterThanOrEqualTo(new Date(
                                (System.currentTimeMillis() - (long) GameProperties.getGameCoefficient(SwitchVarContants.ESCAPE_ARMY_VALID_CYCLE_ID) * 3600 * 1000)));
                }
                List<EscapeArmyCamp> escapeArmyCampList = this.escapeArmyCampDAO.selectByExample(example);
                return escapeArmyCampList;

        }

        /**
         * 按逃兵的拨ID获得逃兵
         */
        @Override
        public List<EscapeArmyCamp> getEscapeArmyCampList(long cityId, long typeId, byte type) {

                EscapeArmyCampExample example = new EscapeArmyCampExample();
                EscapeArmyCampExample.Criteria criteria = example.createCriteria();
                criteria.andCityIdEqualTo(cityId);
                criteria.andIdEqualTo(typeId);
                // 只有在回收周期内的逃兵才可以被回收 及 伤兵才能被治疗
                criteria.andUpdateTimeGreaterThanOrEqualTo(new Date(System.currentTimeMillis() - (long) GameProperties.getGameCoefficient(SwitchVarContants.ESCAPE_ARMY_VALID_CYCLE_ID) * 3600 * 1000));
                List<EscapeArmyCamp> escapeArmyCampList = this.escapeArmyCampDAO.selectByExample(example);
                return escapeArmyCampList;

        }

        /**
         * 通过按拨数获得逃兵
         */
        @Override
        public Map<Long, List<EscapeArmyCamp>> getEscapeArmyCampMap(long cityId, byte type) {

                Map<Long, List<EscapeArmyCamp>> map = new HashMap<Long, List<EscapeArmyCamp>>();
                EscapeArmyCampExample example = new EscapeArmyCampExample();
                EscapeArmyCampExample.Criteria criteria = example.createCriteria();
                criteria.andCityIdEqualTo(cityId);
                criteria.andTypeEqualTo(type);
                // 只有在回收周期内的逃兵才可以被回收
                criteria.andUpdateTimeGreaterThanOrEqualTo(new Date(System.currentTimeMillis() - (long) GameProperties.getGameCoefficient(SwitchVarContants.ESCAPE_ARMY_VALID_CYCLE_ID) * 3600 * 1000));
                List<EscapeArmyCamp> escapeArmyCampList = this.escapeArmyCampDAO.selectByExample(example);
                List<EscapeArmyCamp> newList = null;
                for (EscapeArmyCamp escapeArmyCamp : escapeArmyCampList) {
                        if (!map.containsKey(escapeArmyCamp.getUpdateTime().getTime())) {
                                newList = new ArrayList<EscapeArmyCamp>();
                                newList.add(escapeArmyCamp);
                                map.put(escapeArmyCamp.getUpdateTime().getTime(), newList);
                        } else {
                                map.get(escapeArmyCamp.getUpdateTime().getTime()).add(escapeArmyCamp);
                        }
                }
                return map;
        }

        //通过cityid获得当前城市逃兵召回队列
        @Override
        public List<EscapeArmyRecall> getEscapeArmyRecallList(long cityid) {
                EscapeArmyRecallExample example = new EscapeArmyRecallExample();
                EscapeArmyRecallExample.Criteria criteria = example.createCriteria();
                criteria.andCityIdEqualTo(cityid);
                criteria.andRecallTimeGreaterThan(new Date());
                example.setOrderByClause("recall_time");
                return this.escapeArmyRecallDAO.selectByExample(example);
        }

        //批量增加伤兵/逃兵(0逃兵,1伤兵)
        @Override
        public int addEscapeArmyCamp(List<EscapeArmyCamp> escapeArmyCamps) {
                return escapeArmyCampDAO.insert(escapeArmyCamps);
        }

        //批量删除逃兵/伤兵为零的记录
        @Override
        public int deleteEscapeArmyCamp(List<EscapeArmyCamp> escapeArmyCamps) {
                return escapeArmyCampDAO.delete(escapeArmyCamps);
        }

        //增加逃兵营
        @Override
        public void addEscapeArmyToCamp(List<CityArmy> cityArmys, Date updateTime) {
                List<EscapeArmyCamp> escapeArmyCampList = new ArrayList<EscapeArmyCamp>();
                for (CityArmy cityArmy : cityArmys) {
                        EscapeArmyCamp escapeArmyCamp = new EscapeArmyCamp();
                        escapeArmyCamp.setArmyId(cityArmy.getArmyId());
                        escapeArmyCamp.setCityId(cityArmy.getCityId());
                        escapeArmyCamp.setArmyCount(cityArmy.getArmyCount());
                        escapeArmyCamp.setType(IArmyConstant.TYPE_ESCAPE);
                        escapeArmyCamp.setUpdateTime(updateTime);
                        escapeArmyCampList.add(escapeArmyCamp);
                }
                this.addEscapeArmyCamp(escapeArmyCampList);

        }

        //增加伤兵营
        @Override
        public void addWoundedArmyToCamp(List<CityArmy> cityArmys, Date updateTime) {
                List<EscapeArmyCamp> escapeArmyCampList = new ArrayList<EscapeArmyCamp>();
                for (CityArmy cityArmy : cityArmys) {
                        if (cityArmy.getArmyCount() <= 0) {
                                continue;
                        }
                        EscapeArmyCamp escapeArmyCamp = new EscapeArmyCamp();
                        escapeArmyCamp.setArmyId(cityArmy.getArmyId());
                        escapeArmyCamp.setCityId(cityArmy.getCityId());
                        escapeArmyCamp.setArmyCount(cityArmy.getArmyCount());
                        escapeArmyCamp.setType(IArmyConstant.TYPE_INJURE);
                        escapeArmyCamp.setUpdateTime(updateTime);
                        escapeArmyCampList.add(escapeArmyCamp);
                }
                this.addEscapeArmyCamp(escapeArmyCampList);

        }

        @Override
        public void clearEscapeArmyByUpdate() {
                // 清除逃兵信息
                Date time = new Date(System.currentTimeMillis() - (long) GameProperties.getGameCoefficient(SwitchVarContants.ESCAPE_ARMY_VALID_CYCLE_ID) * 3600 * 1000);
                EscapeArmyCampExample example = new EscapeArmyCampExample();
                EscapeArmyCampExample.Criteria criteria = example.createCriteria();
                criteria.andUpdateTimeLessThan(time);
                criteria.andTypeEqualTo(IArmyConstant.TYPE_ESCAPE);
                this.escapeArmyCampDAO.deleteByExample(example);
                // 清除伤兵信息
                Date time1 = new Date(System.currentTimeMillis() - IArmyConstant.INJURE_ARMY_VALID_CYCLE * 3600 * 1000);
                EscapeArmyCampExample example1 = new EscapeArmyCampExample();
                EscapeArmyCampExample.Criteria criteria1 = example1.createCriteria();
                criteria1.andUpdateTimeLessThan(time1);
                criteria1.andTypeEqualTo(IArmyConstant.TYPE_INJURE);
                this.escapeArmyCampDAO.deleteByExample(example1);
        }

        /**
         * 获得成功召回的逃兵对象
         */
        @Override
        public List<EscapeArmyRecall> getRecallSuccessList() {
                EscapeArmyRecallExample example = new EscapeArmyRecallExample();

                EscapeArmyRecallExample.Criteria criteria = example.createCriteria();
                Date time = new Date();
                time.setTime(System.currentTimeMillis() - (long) GameProperties.getGameCoefficient(SwitchVarContants.RECALL_GEM_TIME_ID) * 3600 * 1000);
                criteria.andSpendTypeEqualTo((int) IArmyConstant.RECALL_TYPE_GEM);
                criteria.andFoundTimeLessThan(time);
                List<EscapeArmyRecall> list = this.escapeArmyRecallDAO.selectByExample(example);

                time.setTime(System.currentTimeMillis() - (long) GameProperties.getGameCoefficient(SwitchVarContants.RECALL_GOLG_TIME_ID) * 3600 * 1000);
                example = new EscapeArmyRecallExample();
                EscapeArmyRecallExample.Criteria criteria1 = example.createCriteria();
                criteria1.andSpendTypeEqualTo((int) IArmyConstant.RECALL_TYPE_GOLD);
                criteria1.andFoundTimeLessThan(time);
                list.addAll(this.escapeArmyRecallDAO.selectByExample(example));
                return list;
        }

        /**
         * 清除逃兵队列信息
         */
        @Override
        public void clearEscapeArmy() {
                // 清除逃兵队列信息
                Date time = new Date((long) (System.currentTimeMillis() - (long) GameProperties.getGameCoefficient(SwitchVarContants.RECALL_GOLG_TIME_ID) * 3600 * 1000));
                EscapeArmyRecallExample example = new EscapeArmyRecallExample();
                EscapeArmyRecallExample.Criteria criteria = example.createCriteria();
                criteria.andFoundTimeLessThan(time);
                criteria.andSpendTypeEqualTo((int) IArmyConstant.RECALL_TYPE_GOLD);
                this.escapeArmyRecallDAO.deleteByExample(example);

                time = new Date((long) (System.currentTimeMillis() - (long) GameProperties.getGameCoefficient(SwitchVarContants.RECALL_GEM_TIME_ID) * 3600 * 1000));
                example = new EscapeArmyRecallExample();
                EscapeArmyRecallExample.Criteria criteria1 = example.createCriteria();
                criteria1.andFoundTimeLessThan(time);
                criteria1.andSpendTypeEqualTo((int) IArmyConstant.RECALL_TYPE_GEM);
                this.escapeArmyRecallDAO.deleteByExample(example);
        }

        //获得城市兵种信息 
        @Override
        public List<CityArmy> getCityArmyByCityId(long cityId) {
                CityArmyExample example = new CityArmyExample();
                CityArmyExample.Criteria criteria = example.createCriteria();
                criteria.andCityIdEqualTo(cityId);
                return cityArmyDAO.selectByExample(example);
        }

        //增加召回队列
        @Override
        public void addRecallArmyQueue(EscapeArmyRecall escapeArmy) {
                this.escapeArmyRecallDAO.insert(escapeArmy);
        }

        @Override
        public int updateAddCityArmys(List<CityArmy> cityArmies) {
                return cityArmyDAO.updateAddByPrimaryKey(cityArmies);
        }

        @Override
        public void addDisciplineArmyAction(ArmyDiscipline armyDiscipline, DisciplineArmyAction disciplineArmyAction) {

                disciplineArmyActionCache.put(armyDiscipline.getId(), disciplineArmyAction);
                if (disciplineArmyActionCacheIndex.containsKey(armyDiscipline.getBuildingId())) {
                        disciplineArmyActionCacheIndex.get(armyDiscipline.getBuildingId()).add(disciplineArmyAction);
                } else {
                        List<DisciplineArmyAction> disciplineArmyActionList = new ArrayList<DisciplineArmyAction>();
                        disciplineArmyActionList.add(disciplineArmyAction);
                        disciplineArmyActionCacheIndex.put(armyDiscipline.getBuildingId(), disciplineArmyActionList);
                }
        }

        /** 注入setter **/

        public void setArmyPrototypeDAO(ArmyPrototypeDAO armyPrototypeDAO) {
                this.armyPrototypeDAO = armyPrototypeDAO;
        }

        public void setCityArmyDAO(CityArmyDAO cityArmyDAO) {
                this.cityArmyDAO = cityArmyDAO;
        }

        public void setArmyDisciplineDAO(ArmyDisciplineDAO armyDisciplineDAO) {
                this.armyDisciplineDAO = armyDisciplineDAO;
        }

        public void setArmyEscapePrototypeDAO(ArmyEscapePrototypeDAO armyEscapePrototypeDAO) {
                this.armyEscapePrototypeDAO = armyEscapePrototypeDAO;
        }

        public void setEscapeArmyCampDAO(EscapeArmyCampDAO escapeArmyCampDAO) {
                this.escapeArmyCampDAO = escapeArmyCampDAO;
        }

        public void setEscapeArmyRecallDAO(EscapeArmyRecallDAO escapeArmyRecallDAO) {
                this.escapeArmyRecallDAO = escapeArmyRecallDAO;
        }

        public void setDisciplineArmyActionCache(Map<Long, DisciplineArmyAction> disciplineArmyActionCache) {
                this.disciplineArmyActionCache = disciplineArmyActionCache;
        }

        public void setDisciplineArmyActionCacheIndex(Map<Long, List<DisciplineArmyAction>> disciplineArmyActionCacheIndex) {
                this.disciplineArmyActionCacheIndex = disciplineArmyActionCacheIndex;
        }

        public void setCureArmyActionCache(Map<Long, CureArmyAction> cureArmyActionCache) {
                this.cureArmyActionCache = cureArmyActionCache;
        }

        public void setCureArmyActionCacheIndex(Map<Long, List<CureArmyAction>> cureArmyActionCacheIndex) {
                this.cureArmyActionCacheIndex = cureArmyActionCacheIndex;
        }

}
