package com.wistone.ww2Refactor.game.cimelia.handler;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.wistone.common.net.protocol.server.streamProtocol.IStreamRequest;
import com.wistone.common.net.protocol.server.streamProtocol.IStreamResponse;
import com.wistone.ww2Refactor.game.cimelia.db.pojo.PlayerInvitationInfo;
import com.wistone.ww2Refactor.game.cimelia.processor.ICimeliaProcessor;
import com.wistone.ww2Refactor.game.core.AbstractGameHandler;
import com.wistone.ww2Refactor.game.core.message.Message;
import com.wistone.ww2Refactor.game.core.onlinePlayer.OnlinePlayer;
import com.wistone.ww2Refactor.game.player.db.pojo.Player;
import com.wistone.ww2Refactor.game.player.db.pojo.PlayerBase;

/**
 * @author zhaofx
 * @command_id 8011
 * @descirption 功能描述:获取邀请奖励列表
 */
public class GetInviteRewardsListHandler8011 extends AbstractGameHandler {

        Logger logger = Logger.getLogger(GetInviteRewardsListHandler8011.class);
        private ICimeliaProcessor cimeliaProcessor;

        public void setCimeliaProcessor(ICimeliaProcessor cimeliaProcessor) {
                this.cimeliaProcessor = cimeliaProcessor;
        }

        @Override
        public void doExecute(IStreamRequest request, IStreamResponse response) {
                OnlinePlayer onlinePlayer = this.getOnlinePlayer(request);
                int pageNumber = request.getInt();// 当前页码，0无分页
                int itemsPerPage = request.getInt();// 每页显示条数，0无限制
                long playerId = onlinePlayer.getPlayerId();
                List<PlayerInvitationInfo> list = cimeliaProcessor.getPlayerInvitationInfoByPlayerId(playerId);
                if (list != null && list.size() > 0) {
                        int sizeReturn = 0;// 本次请求返回数据条数
                        int totalSize = getTotalPageSize(itemsPerPage, list.size());// 总页数
                        response.putInt(totalSize);
                        int currentPageNumber = getCurrentPageNumber(itemsPerPage, pageNumber, list.size());// 当前页码
                        response.putInt(currentPageNumber);
                        if (currentPageNumber == totalSize) {// 请求数据为最后一页
                                if (itemsPerPage == 0) {
                                        itemsPerPage = 10;
                                }
                                if ((list.size()) % itemsPerPage == 0) {
                                        sizeReturn = itemsPerPage;
                                } else
                                        sizeReturn = (list.size()) % itemsPerPage;
                        } else {
                                sizeReturn = itemsPerPage;
                        }
                        int start = (currentPageNumber - 1) * itemsPerPage;
                        int len = itemsPerPage > sizeReturn ? sizeReturn : itemsPerPage;
                        List<PlayerInvitationInfo> lists = list.subList(start, (start + len));
                        response.putString("");
                        response.putInt(lists.size());
                        for (PlayerInvitationInfo info : lists) {
                                Player player = this.playerProcessor.getPlayer(playerId);
                                response.putLong(info.getTargetId());
                                response.putString(player.getNickname());
                                // 活跃部分取当前日期和该好友上次登录日期。
                                // 两日期差值为0时显示当日活跃
                                // 差值为1时显示1日未活跃
                                // 差值为2时显示2日未活跃
                                // 差值为3时显示3日未活跃
                                // 差值为4及以上时显示3日以上未活跃
                                long currentDayTime = System.currentTimeMillis();
                                Calendar currentDayCal = Calendar.getInstance();
                                currentDayCal.setTimeInMillis(currentDayTime);
                                int currentDayOfYear = currentDayCal.get(Calendar.DAY_OF_YEAR);
                                PlayerBase playerBase = this.playerProcessor.getPlayerBaseByPlayerId(playerId);
                                long lastLoginTime = playerBase.getLastLoginTime().getTime();
                                Calendar lastLoginCal = Calendar.getInstance();
                                lastLoginCal.setTimeInMillis(lastLoginTime);
                                int lastLoginDayOfYear = lastLoginCal.get(Calendar.DAY_OF_YEAR);
                                int tempTime = currentDayOfYear - lastLoginDayOfYear;
                                if (tempTime == 0) {
                                        response.putString(Message.get("s724.0"));
                                } else if (tempTime == 1) {
                                        response.putString(Message.get("s724.1"));
                                } else if (tempTime == 2) {
                                        response.putString(Message.get("s724.2"));
                                } else if (tempTime == 3) {
                                        response.putString(Message.get("s724.3"));
                                } else {
                                        response.putString(Message.get("s724.4"));
                                }
                        }
                } else {
                        response.putInt(0);
                        response.putInt(0);
                        response.putString(Message.get("s724.5"));
                        response.putInt(0);// 条数
                }
        }

        /**
         * 获取当前页码
         * 
         * @param itemsPerPage
         *                每页条数
         * @param pageNumber
         *                客户端传入 当前页码
         * @param size
         *                总条数
         * @return
         */
        private int getCurrentPageNumber(int itemsPerPage, int pageNumber, int size) {
                int currentPageNumber = 1;
                if (itemsPerPage == 0 || pageNumber == 0) {// 无分页
                        return currentPageNumber;
                }
                int itemsPerPageTemp = itemsPerPage;// 每页条数
                int totalSize = getTotalPageSize(itemsPerPageTemp, size);// 总页数
                if (pageNumber <= totalSize) {// 客户端当前页码正常
                        return pageNumber;
                }
                currentPageNumber = pageNumber - 1;// 客户端因为删除等操作导致其当前页已经无数据，故退回上一页

                return currentPageNumber;
        }

        /**
         * 获取总页数
         * 
         * @param itemsPerPage
         *                每页条数
         * @param size
         *                总条数
         * @return
         */
        public int getTotalPageSize(int itemsPerPage, int size) {
                int totalSize = 0;
                if (itemsPerPage == 0)
                        return 1;// 无分页
                if (size > 0 && size % itemsPerPage == 0) {
                        totalSize = size / itemsPerPage;
                        return totalSize;
                }
                totalSize = size / itemsPerPage + 1;

                return totalSize;
        }
}

