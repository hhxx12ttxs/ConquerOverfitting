package com.wistone.ww2Refactor.game.city.calculation;

import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wistone.ww2Refactor.game.army.db.pojo.CityArmy;
import com.wistone.ww2Refactor.game.army.processor.IArmyProcessor;
import com.wistone.ww2Refactor.game.building.IBuildingConstant;
import com.wistone.ww2Refactor.game.building.db.pojo.CityBuilding;
import com.wistone.ww2Refactor.game.building.processor.IBuildingProcessor;
import com.wistone.ww2Refactor.game.city.db.pojo.City;
import com.wistone.ww2Refactor.game.city.db.pojo.CityResource;
import com.wistone.ww2Refactor.game.city.processor.ICityProcessor;
import com.wistone.ww2Refactor.game.core.GameFormula;
import com.wistone.ww2Refactor.game.core.processor.IStatisticsTimesProcessor;
import com.wistone.ww2Refactor.game.expedition.db.ExpeditionArmy;
import com.wistone.ww2Refactor.game.expedition.db.ExpeditionInfo;
import com.wistone.ww2Refactor.game.expedition.processor.IExpeditionProcessor;
import com.wistone.ww2Refactor.game.map.IMapConstant;
import com.wistone.ww2Refactor.game.map.db.pojo.Field;
import com.wistone.ww2Refactor.game.map.processor.IMapService;
import com.wistone.ww2Refactor.game.officer.IOfficerConstant;
import com.wistone.ww2Refactor.game.officer.db.pojo.Officer;
import com.wistone.ww2Refactor.game.officer.processor.IOfficerService;
import com.wistone.ww2Refactor.game.player.db.pojo.Player;
import com.wistone.ww2Refactor.game.player.processor.IPlayerProcessor;
import com.wistone.ww2Refactor.game.technique.db.PlayerTechnique;
import com.wistone.ww2Refactor.game.technique.db.TechniqueLevelPrototype;
import com.wistone.ww2Refactor.game.technique.processor.ITechniqueProcessor;

public class ResourceCalculateInfoBuilder {
        private ICityProcessor cityProcessor;
        private ITechniqueProcessor techniqueProcessor;
        private IPlayerProcessor playerProcessor;
        private IOfficerService officerService;
        private IMapService mapService;
        private IArmyProcessor armyProcessor;
        private IExpeditionProcessor expeditionProcessor;
        private IBuildingProcessor buildingProcessor;
        private IStatisticsTimesProcessor statisticsTimesProcessor;

        public void setStatisticsTimesProcessor(IStatisticsTimesProcessor statisticsTimesProcessor) {
                this.statisticsTimesProcessor = statisticsTimesProcessor;
        }

        public void setCityProcessor(ICityProcessor cityProcessor) {
                this.cityProcessor = cityProcessor;
        }

        public void setTechniqueProcessor(ITechniqueProcessor techniqueProcessor) {
                this.techniqueProcessor = techniqueProcessor;
        }

        public void setPlayerProcessor(IPlayerProcessor playerProcessor) {
                this.playerProcessor = playerProcessor;
        }

        public void setOfficerService(IOfficerService officerService) {
                this.officerService = officerService;
        }

        public void setMapService(IMapService mapService) {
                this.mapService = mapService;
        }

        public void setArmyProcessor(IArmyProcessor armyProcessor) {
                this.armyProcessor = armyProcessor;
        }

        public void setExpeditionProcessor(IExpeditionProcessor expeditionProcessor) {
                this.expeditionProcessor = expeditionProcessor;
        }

        public void setBuildingProcessor(IBuildingProcessor buildingProcessor) {
                this.buildingProcessor = buildingProcessor;
        }

        /**
         * 构建一个用来进行城市资源计算的数据对象
         * 
         * @param city
         * @param cache
         * @param endtime
         * @return
         * @author Neo
         * @description 方法说明
         */
        public IResourceCalculteInfo build(City city, long endtime) {
                ResourceCalculateInfo info = new ResourceCalculateInfo();
                info.city = city;
                info.endTime = endtime;
                //重置时间
                info.lastResourceTime = city.getLastResourceUpdateTime().getTime();
                info.lastPopultionTime = city.getLastPopulationUpdateTime().getTime();
                info.lastAnthropogenicEventTime = city.getLastAnthropogenicDisasterTime().getTime();
                //以资源的最后更新时间为起点时间
                info.currentTime = info.lastResourceTime;
                info.endTime = endtime;
                return info;
        }

        /**
         * 构建一个用来进行资源计算的数据对象(没有传入城市，说明对象会被复用，使用前必须调用reset方法)
         * 
         * @param cache
         * @param endtime
         * @return
         * @author Neo
         * @description 方法说明
         */
        public IResourceCalculteInfo build(Player player, long endtime) {
                ResourceCalculateInfo info = new ResourceCalculateInfo();
                info.player = player;
                info.endTime = endtime;
                return info;
        }

        private class ResourceCalculateInfo implements IResourceCalculteInfo {

                //将构造函数设置为私有，只能在构造器内使用
                private ResourceCalculateInfo() {

                }

                /**
                 * 计算开始时间
                 */
                private long endTime;
                /**
                 * 当前计算时间
                 */
                private long currentTime;
                /**
                 * 最后资源计算时间
                 */
                private long lastResourceTime;
                /**
                 * 最后人口计算时间
                 */
                private long lastPopultionTime;
                /**
                 * 最后人祸时间
                 */
                private long lastAnthropogenicEventTime;
                /**
                 * 角色数据缓存
                 */
                private Player player;

                /**
                 * 玩家科技数据
                 */
                private List<PlayerTechnique> playerTechniques;
                /**
                 * 当前城市有效科技数据缓存
                 */
                private List<TechniqueLevelPrototype> cityValidtechniques;
                /**
                 * 城市驻军所属角色
                 */
                private Map<Long, Player> expeditionPlayerMap;
                /**
                 * 城池数据缓存
                 */
                private City city;
                /**
                 * 城市坐标
                 */
                private Point cityPostion;
                /**
                 * 资源计算数据缓存
                 */
                private CityResourceCalculateCache cache;
                /**
                 * 计算用
                 */
                private CityResource cityResource;
                /**
                 * 城市建筑
                 */
                private List<CityBuilding> buildings;
                /**
                 * 所有军官
                 */
                private List<Officer> officers;
                /**
                 * 市长
                 */
                private Officer commander;
                /**
                 * 野地对象缓存
                 */
                private List<Field> fields;
                /**
                 * 城中兵力
                 */
                private List<CityArmy> armys;

                /**
                 * 城中逃兵数据
                 */
                private List<CityArmy> runawayArmys;
                /**
                 * 城市驻军部队(只可能是盟友的部队)
                 */
                private List<ExpeditionInfo> cityExpeditions;
                /**
                 * 城市驻军部队兵力
                 */
                private Map<Long, List<ExpeditionArmy>> cityExpArmys;
                /**
                 * 城市驻军逃兵数据
                 */
                private Map<Long, List<CityArmy>> runawayCityExpArmys;
                /**
                 * 属地驻军部队(只可能是自己本城的部队)
                 */
                private List<ExpeditionInfo> fieldExpedtions;
                /**
                 * 属地驻军部队兵力
                 */
                private Map<Long, List<ExpeditionArmy>> fieldExpArmys;
                /**
                 * 属地驻军逃兵数据
                 */
                private List<CityArmy> runawayFieldExpArmys;

                /**
                 * 逃兵次数
                 */
                private int runawayTimes = -1;
                /**
                 * 逃兵警告次数
                 */
                private int runawayAlertTimes = -1;

                @Override
                public void reset(City city) {
                        this.city = city;

                        //重置时间
                        this.lastResourceTime = city.getLastResourceUpdateTime().getTime();
                        this.lastPopultionTime = city.getLastPopulationUpdateTime().getTime();
                        this.lastAnthropogenicEventTime = city.getLastAnthropogenicDisasterTime().getTime();
                        //以资源的最后更新时间为起点时间
                        this.currentTime = lastResourceTime;

                        //清空所有缓存数据，player对象除外
                        this.expeditionPlayerMap = null;
                        this.commander = null;
                        this.cityPostion = null;
                        this.cityResource = null;
                        this.buildings = null;
                        this.officers = null;
                        this.fields = null;
                        this.armys = null;
                        this.runawayArmys = null;
                        this.cityExpeditions = null;
                        this.cityExpArmys = null;
                        this.runawayCityExpArmys = null;
                        this.fieldExpArmys = null;
                        this.fieldExpedtions = null;
                        this.runawayFieldExpArmys = null;
                        this.cityValidtechniques = null;
                        this.runawayAlertTimes = -1;
                        this.runawayTimes = -1;
                }

                @Override
                public long getRemainCalculateTime() {
                        return this.endTime - this.currentTime;
                }

                @Override
                public void stepCalculateTime(long stepTime) {
                        this.currentTime += stepTime;
                }

                @Override
                public long getLastPopultionTime() {
                        return this.lastPopultionTime;
                }

                @Override
                public long getPopultionCalculateFreeTime() {
                        return this.currentTime - this.lastPopultionTime;
                }

                @Override
                public void stepPopultionCalculateTime(long stepTime) {
                        this.lastPopultionTime += stepTime;
                }

                @Override
                public long getLastResourceTime() {
                        return this.lastResourceTime;
                }

                @Override
                public long getResourceCalculateFreeTime() {
                        return this.currentTime - this.lastResourceTime;
                }

                @Override
                public void stepResourceCalculateTime(long stepTime) {
                        this.lastResourceTime += stepTime;
                }

                @Override
                public long getLastAnthropogenicDisasterTime() {
                        return this.lastAnthropogenicEventTime;
                }

                @Override
                public long getAnthropogenicDisasterCalculateFreeTime() {
                        return this.currentTime - this.lastAnthropogenicEventTime;
                }

                @Override
                public void stepAnthropogenicDisasterCalculateTime(long stepTime) {
                        this.lastAnthropogenicEventTime += stepTime;
                }

                @Override
                public City getCity() {
                        return this.city;
                }

                @Override
                public CityResourceCalculateCache getCityResourceCalculateCache() {
                        return this.cache;
                }

                @Override
                public void putRunawayFieldExpArmys(CityArmy army) {
                        this.runawayFieldExpArmys = this.getRunawayFieldExpArmys();
                        for (CityArmy runawayArmy : this.runawayFieldExpArmys) {
                                if (army.getArmyId().equals(runawayArmy.getArmyId()) && army.getCityId().equals(runawayArmy.getCityId())) {
                                        runawayArmy.setArmyCount(runawayArmy.getArmyCount() + army.getArmyCount());
                                        return;
                                }
                        }
                        runawayFieldExpArmys.add(army);
                }

                @Override
                public void putRunawayCityExpArmys(long playerId, CityArmy army) {
                        runawayCityExpArmys = this.getRunawayCityExpArmys();
                        List<CityArmy> runaways = runawayCityExpArmys.get(playerId);
                        if (runaways == null) {
                                runaways = new LinkedList<CityArmy>();
                                runawayCityExpArmys.put(playerId, runaways);
                        }
                        for (CityArmy runawayArmy : runaways) {
                                if (army.getArmyId().equals(runawayArmy.getArmyId()) && army.getCityId().equals(runawayArmy.getCityId())) {
                                        runawayArmy.setArmyCount(runawayArmy.getArmyCount() + army.getArmyCount());
                                        return;
                                }
                        }
                        runaways.add(army);
                }

                @Override
                public void resetRunawayInfo() {
                        runawayArmys = null;
                        runawayCityExpArmys = null;
                        runawayFieldExpArmys = null;
                }

                @Override
                public Player getPlayer() {
                        if (this.player == null)
                                this.player = playerProcessor.getPlayer(this.city.getPlayerId());
                        return this.player;
                }

                @Override
                public Point getCityPostion() {
                        if (this.cityPostion == null)
                                this.cityPostion = mapService.getPlayerCityPosition(this.city.getId());
                        return this.cityPostion;
                }

                @Override
                public CityResource getCityResource() {
                        if (this.cityResource == null)
                                this.cityResource = cityProcessor.getCityResourceByCityId(this.city.getId());
                        return this.cityResource;
                }

                @Override
                public List<CityBuilding> getBuildings() {
                        if (this.buildings == null) {
                                this.buildings = buildingProcessor.getCityBuildingByCityId(this.city.getId());
                                if (this.buildings == null)
                                        this.buildings = Collections.emptyList();
                        }
                        return this.buildings;
                }

                @Override
                public List<Officer> getOfficers() {
                        if (this.officers == null) {
                                this.officers = officerService.getCityAllOfficersByCityID(this.city.getId());
                                if (this.officers == null)
                                        this.officers = Collections.emptyList();
                        }
                        return this.officers;
                }

                @Override
                public Officer getCommander() {
                        if (this.commander == null) {
                                List<Officer> officerList = this.getOfficers();
                                for (Officer officer : officerList) {
                                        if (officer.getState() == IOfficerConstant.STATE_COMMANDER) {
                                                this.commander = officer;
                                                break;
                                        }
                                }
                        }
                        return this.commander;
                }

                @Override
                public List<Field> getFields() {
                        if (this.fields == null) {
                                this.fields = mapService.getFieldBelongCity(this.city.getId());
                                if (this.fields == null) {
                                        this.fields = Collections.emptyList();
                                }
                        }
                        return this.fields;
                }

                @Override
                public List<CityArmy> getArmys() {
                        if (this.armys == null) {
                                this.armys = armyProcessor.getCityArmyByCityId(this.city.getId());
                                if (this.armys == null) {
                                        this.armys = Collections.emptyList();
                                }
                        }
                        return this.armys;
                }

                @Override
                public List<CityArmy> getRunawayArmys() {
                        if (this.runawayArmys == null)
                                this.runawayArmys = new LinkedList<CityArmy>();
                        return this.runawayArmys;
                }

                @Override
                public List<ExpeditionInfo> getCityExpeditions() {
                        if (this.cityExpeditions == null) {
                                Point pos = this.getCityPostion();
                                this.cityExpeditions = expeditionProcessor.getTargetStationExpedition(pos.x, pos.y);
                                if (this.cityExpeditions == null)
                                        this.cityExpeditions = Collections.emptyList();
                                //查看部队时间，移除掉到达时间大于当前计算时间的部队，保证数据精确
                                for (Iterator<ExpeditionInfo> it = this.cityExpeditions.iterator(); it.hasNext();) {
                                        ExpeditionInfo exp = it.next();
                                        if (exp.getArrivalTime().getTime() > this.currentTime)
                                                it.remove();
                                }
                        }
                        return this.cityExpeditions;
                }

                @Override
                public Map<Long, List<ExpeditionArmy>> getCityExpArmys() {
                        if (cityExpArmys == null) {
                                List<Long> expIds = new LinkedList<Long>();
                                List<ExpeditionInfo> expList = this.getCityExpeditions();
                                for (ExpeditionInfo exp : expList) {
                                        expIds.add(exp.getId());
                                }
                                this.cityExpArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(expIds);
                                if (this.cityExpArmys == null)
                                        this.cityExpArmys = Collections.emptyMap();
                        }
                        return this.cityExpArmys;
                }

                @Override
                public Map<Long, List<CityArmy>> getRunawayCityExpArmys() {
                        if (this.runawayCityExpArmys == null)
                                this.runawayCityExpArmys = new HashMap<Long, List<CityArmy>>();
                        return this.runawayCityExpArmys;
                }

                @Override
                public List<ExpeditionInfo> getFieldExpedtions() {
                        if (this.fieldExpedtions == null) {
                                //获取城市所有在外处于驻守状态的部队
                                this.fieldExpedtions = expeditionProcessor.getCityExpeditionOnStation(this.city.getId());
                                if (this.fieldExpedtions == null)
                                        this.fieldExpedtions = Collections.emptyList();
                                //如果存在在外驻守部队，过滤掉驻守在其他玩家城市中的部队(这些部队消耗所在驻守城市的粮食)
                                if (!this.fieldExpedtions.isEmpty()) {
                                        //使用set保证没有重复的地点
                                        Set<Integer> mapIds = new HashSet<Integer>();
                                        for (ExpeditionInfo exp : this.fieldExpedtions) {
                                                mapIds.add(GameFormula.getMapId(exp.getEndX(), exp.getEndY()));
                                        }
                                        //取得所有部队所驻守地点的地块类型
                                        Map<Integer, Integer> typeMap = mapService.getMapCellType(new LinkedList<Integer>(mapIds));
                                        for (Iterator<ExpeditionInfo> it = this.fieldExpedtions.iterator(); it.hasNext();) {
                                                ExpeditionInfo exp = it.next();
                                                int id = GameFormula.getMapId(exp.getEndX(), exp.getEndY());
                                                int type = typeMap.get(id);
                                                //如果驻守地点是玩家城市，在移除掉，不参与资源计算
                                                //如果部队到达时间大于当前计算时间，移除
                                                if (type == IMapConstant.MAP_CELL_TYPE_PLAYER_CITY || exp.getArrivalTime().getTime() > this.currentTime)
                                                        it.remove();
                                        }
                                }
                        }
                        return this.fieldExpedtions;
                }

                @Override
                public Map<Long, List<ExpeditionArmy>> getFieldExpArmys() {
                        if (this.fieldExpArmys == null) {
                                List<Long> expIds = new LinkedList<Long>();
                                List<ExpeditionInfo> expList = this.getFieldExpedtions();
                                for (ExpeditionInfo exp : expList) {
                                        expIds.add(exp.getId());
                                }
                                this.fieldExpArmys = expeditionProcessor.getExpeditionArmysByExpeditionId(expIds);
                                if (this.fieldExpArmys == null)
                                        this.fieldExpArmys = Collections.emptyMap();
                        }
                        return this.fieldExpArmys;
                }

                @Override
                public List<CityArmy> getRunawayFieldExpArmys() {
                        if (this.runawayFieldExpArmys == null)
                                this.runawayFieldExpArmys = new LinkedList<CityArmy>();
                        return this.runawayFieldExpArmys;
                }

                @Override
                public List<TechniqueLevelPrototype> getCityValidtechniques() {
                        if (this.cityValidtechniques == null) {
                                Player player = this.getPlayer();
                                if (this.playerTechniques == null) {
                                        this.playerTechniques = techniqueProcessor.getPlayerTechniqueByPlayerId(player.getPlayerId());
                                }
                                List<CityBuilding> buildings = this.getBuildings();
                                int level = 0;
                                for (CityBuilding building : buildings) {
                                        if (building.getPrototypeid() == IBuildingConstant.TECHNIQUE_RESEARCH_CENTER_ID) {
                                                level = building.getLevel();
                                                break;
                                        }
                                }
                                if (level > 0)
                                        this.cityValidtechniques = techniqueProcessor.getCityValidTechnique(playerTechniques, player, level);
                                if (this.cityValidtechniques == null)
                                        this.cityValidtechniques = Collections.emptyList();
                        }
                        return this.cityValidtechniques;
                }

                @Override
                public Map<Long, Player> getExpeditionPlayers() {
                        if (this.expeditionPlayerMap == null) {
                                List<ExpeditionInfo> cityExpList = this.getCityExpeditions();
                                List<Long> expIds = new LinkedList<Long>();
                                for (ExpeditionInfo exp : cityExpList) {
                                        expIds.add(exp.getPlayerId());
                                }
                                List<Player> players = playerProcessor.getPlayers(expIds);
                                if (players.isEmpty()) {
                                        this.expeditionPlayerMap = Collections.emptyMap();
                                } else {
                                        this.expeditionPlayerMap = new HashMap<Long, Player>(players.size());
                                        for (Player p : players) {
                                                this.expeditionPlayerMap.put(p.getPlayerId(), p);
                                        }
                                }
                        }
                        return this.expeditionPlayerMap;
                }

                @Override
                public int getRunwayTimes() {
                        if (this.runawayTimes == -1) {
                                this.runawayTimes = statisticsTimesProcessor.getRunawayTimes(this.city.getId());
                        }
                        return runawayTimes;
                }

                @Override
                public int getRunwayAlertTimes() {
                        if (this.runawayAlertTimes == -1) {
                                this.runawayAlertTimes = statisticsTimesProcessor.getRunawayAlertTimes(this.city.getId());
                        }
                        return this.runawayAlertTimes;
                }

                @Override
                public void setRunwayTimes(int times) {
                        this.runawayTimes = times;
                }

                @Override
                public void setRunwayAlertTimes(int times) {
                        this.runawayAlertTimes = times;

                }

                @Override
                public void setCalculateCache(CityResourceCalculateCache cache) {
                        this.cache = cache;
                }
        }
}

