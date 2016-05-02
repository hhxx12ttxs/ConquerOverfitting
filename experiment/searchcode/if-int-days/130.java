package com.wistone.ww2Refactor.game.player.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import com.wistone.common.net.protocol.server.streamProtocol.IStreamRequest;
import com.wistone.common.net.protocol.server.streamProtocol.IStreamResponse;
import com.wistone.common.util.StringUtils;
import com.wistone.ww2Refactor.game.cimelia.processor.ICimeliaProcessor;
import com.wistone.ww2Refactor.game.city.db.pojo.City;
import com.wistone.ww2Refactor.game.city.processor.ICityProcessor;
import com.wistone.ww2Refactor.game.core.AbstractGameHandler;
import com.wistone.ww2Refactor.game.core.message.Message;
import com.wistone.ww2Refactor.game.core.onlinePlayer.OnlinePlayer;
import com.wistone.ww2Refactor.game.mail.IMailConstant;
import com.wistone.ww2Refactor.game.mail.db.MailInfo;
import com.wistone.ww2Refactor.game.mail.processor.IMailProcessor;
import com.wistone.ww2Refactor.game.player.IPlayerConstants;
import com.wistone.ww2Refactor.game.player.db.pojo.Player;
import com.wistone.ww2Refactor.game.player.db.pojo.WarLessEvent;

/**
 * @author zhaofx
 * @command_id 1007
 * @descirption 功能描述:修改玩家免战状态
 */
public class ChangePlayerStatusHandler1007 extends AbstractGameHandler {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Logger logger = Logger.getLogger(ChangePlayerStatusHandler1007.class);
        private ICimeliaProcessor cimeliaProcessor;
        private IMailProcessor mailProcessor;
        private ICityProcessor cityProcessor;

        @Override
        public void doExecute(IStreamRequest request, IStreamResponse response) {
                OnlinePlayer onlinePlayer = this.getOnlinePlayer(request);
                Player player = playerProcessor.getPlayer(onlinePlayer.getPlayerId());
                int days = request.getInt();// 持续小时数
                if (days < 0) {
                        this.sendFailedInfo(response, Message.get("s223"));
                        return;
                }
                // 此时days表示的是小时数
                if (days >= 2 && days <= 120) {
                        if (playerProcessor.isHaveEffectivePlayerStatusByPlayerIdAndTarget(onlinePlayer.getPlayerId(), IPlayerConstants.TARGET_STATUS_WARLESS)) {
                                this.sendFailedInfo(response, Message.get("s94"));
                                return;
                        }
                        int subGem = (int) (9.6 + 1.7 * days);
                        int effetiveTime = days * 60 * 60 * 1000;
                        long warlessEndTime = System.currentTimeMillis() + effetiveTime;
                        int snDay = days / 2;
                        int warlessSnTime = effetiveTime + snDay * 60 * 60 * 1000;
                        if (days >= 12) {
                                warlessSnTime = effetiveTime + 6 * 60 * 60 * 1000;
                        }
                        // 开启事物
                        TransactionStatus transStatus = this.txManager.getTransaction(createDefaultTransactionDefinition());
                        try {
                                int leavCount = playerProcessor.reducePlayerGem(onlinePlayer, subGem, IPlayerConstants.DIAMOND_CONSUME_ADD_LESS_WAR);
                                if (leavCount < 0) {
                                        this.sendFailedInfo(response, Message.get("s1119.3"));
                                        txManager.rollback(transStatus);
                                        return;
                                }
                                long leanCoolTime = playerProcessor.addPlayerStatus(player.getPlayerId(), player.getPlayerId(), effetiveTime, warlessSnTime, IPlayerConstants.PROTOTYPE_STATUS_WARLESS);
                                if (leanCoolTime > 0) {
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(new Date(System.currentTimeMillis() + leanCoolTime));
                                        this.sendFailedInfo(
                                                response,
                                                Message.get(
                                                        "s232",
                                                        cal.get(Calendar.YEAR),
                                                        cal.get(Calendar.MONTH) + 1,
                                                        cal.get(Calendar.DAY_OF_MONTH),
                                                        cal.get(Calendar.HOUR_OF_DAY),
                                                        cal.get(Calendar.MINUTE)));
                                        txManager.rollback(transStatus);
                                        return;
                                }
                                dealEmailAndEvent(player, subGem, warlessEndTime, warlessSnTime, effetiveTime);
                                txManager.commit(transStatus);
                        } catch (Exception ex) {
                                ex.printStackTrace();
                                txManager.rollback(transStatus);
                                logger.error(StringUtils.getExceptionTrace(ex));
                                this.sendFailedInfo(response, Message.get("system.error"));
                                return;
                        }
                        response.putString(formatter.format(new Date(warlessEndTime)));// 免战结束时间
                } else {
                        this.sendFailedInfo(response, Message.get("s223"));
                        return;
                }
        }

        public void changeToRestStatus(OnlinePlayer onlinePlayer, List<City> citys, int days, int needGem, long restTimeStart) {
                playerProcessor.reducePlayerGem(onlinePlayer, needGem, IPlayerConstants.DIAMOND_CONSUME_ADD_LESS_WAR);
                int restTime = days * 24 * 60 * 60 * 1000;
                Date restTimeEndTime = new Date(restTimeStart + restTime);
                playerProcessor.addPlayerStatus(onlinePlayer.getPlayerId(), onlinePlayer.getPlayerId(), restTime, restTime + IPlayerConstants.REST_COOL_TIME, IPlayerConstants.PROTOTYPE_STATUS_REST);
                // 更新最后天灾时间
                playerProcessor.updatePlayerLastCalamityTime(onlinePlayer.getPlayerId(), restTimeEndTime);
                // 更新最后天赐时间
                playerProcessor.updatePlayerLastLuckTime(onlinePlayer.getPlayerId(), restTimeEndTime);
                for (City c : citys) {
                        // 更新最后资源计算时间
                        cityProcessor.asynUpdateCityLastResourceUpdateTime(c.getId(), restTimeEndTime);
                        // 更新最后人口计算时间
                        cityProcessor.asynUpdateCityLastPopulationUpdateTime(c.getId(), restTimeEndTime);
                        // 最后人祸时间
                        cityProcessor.asynUpdateCityLastAnthropogenicDisasterTime(c.getId(), restTimeEndTime);
                }
        }

        public int getRestGem(int days, double base) {
                return (int) (15 + Math.log(days) / Math.log(base));
        }

        /**
         * @param playerInfo
         * @param subGem
         * @param warlessEndTime
         * @param warlessSnEndTime
         * @param effetiveTime
         * @author zhaofx
         * @description 发送邮件和事件
         */
        public void dealEmailAndEvent(Player player, int subGem, long warlessEndTime, int warlessSnTime, int effetiveTime) {
                Calendar startDate = Calendar.getInstance();
                Calendar endDate = Calendar.getInstance();
                endDate.setTime(new Date(warlessEndTime));
                startDate.setTime(new Date(System.currentTimeMillis()));
                MailInfo mailInfo = new MailInfo();
                String startDateStr =
                        Message.get(
                                "s750",
                                "" + startDate.get(Calendar.YEAR),
                                "" + (startDate.get(Calendar.MONTH) + 1),
                                "" + startDate.get(Calendar.DAY_OF_MONTH),
                                "" + startDate.get(Calendar.HOUR_OF_DAY),
                                "" + startDate.get(Calendar.MINUTE));
                String endDateStr =
                        Message.get(
                                "s750",
                                "" + endDate.get(Calendar.YEAR),
                                "" + (endDate.get(Calendar.MONTH) + 1),
                                "" + endDate.get(Calendar.DAY_OF_MONTH),
                                "" + endDate.get(Calendar.HOUR_OF_DAY),
                                "" + endDate.get(Calendar.MINUTE));
                mailInfo.setTitle(Message.get("s233"));
                mailInfo.setContent(Message.get("s235", startDateStr, endDateStr));
                mailInfo.setPlayerName(Message.get(IMailConstant.SYS_NAME));
                mailInfo.setType(IMailConstant.MAIL_TYPE_SYS);
                mailProcessor.sendMail(mailInfo, player);
                // 记录免战事件
                WarLessEvent warLessEvent = new WarLessEvent();
                warLessEvent.setPlayerId(player.getPlayerId());
                warLessEvent.setDiamondCount(subGem);
                Date currentDate = new Date(System.currentTimeMillis());
                warLessEvent.setStartTime(currentDate);
                warLessEvent.setEndTime(new Date(warlessEndTime));
                warLessEvent.setEffectiveTime((long) effetiveTime);
                warLessEvent.setOperateTime(currentDate);
                playerProcessor.ayncRecordWarLessEvent(warLessEvent);
        }

        public void setCimeliaProcessor(ICimeliaProcessor cimeliaProcessor) {
                this.cimeliaProcessor = cimeliaProcessor;
        }

        public void setMailProcessor(IMailProcessor mailProcessor) {
                this.mailProcessor = mailProcessor;
        }

        public void setCityProcessor(ICityProcessor cityProcessor) {
                this.cityProcessor = cityProcessor;
        }
}

