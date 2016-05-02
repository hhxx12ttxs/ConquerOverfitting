package com.pingpong.android.modules.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pingpong.android.R;
import com.pingpong.android.model.Game;
import com.pingpong.android.utils.Utils;

import java.util.List;

/**
 * Created by jiangzhenjie on 15/4/19.
 */
public class GameAdapter extends BaseAdapter {

    private Context mContext;
    private List<Game> mGames;

    public GameAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mGames == null ? 0 : mGames.size();
    }

    @Override
    public Object getItem(int position) {
        return mGames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.hall_game_item, parent, false);
            holder = new ViewHolder();
            holder.gameName = (TextView) convertView.findViewById(R.id.tv_game_name);
            holder.gameExplain = (TextView) convertView.findViewById(R.id.tv_game_explain);
            holder.gameApply = (TextView) convertView.findViewById(R.id.tv_game_apply);
            holder.gameAward = (TextView) convertView.findViewById(R.id.tv_game_award);
            holder.gameTime = (TextView) convertView.findViewById(R.id.tv_game_time);
            holder.gamePlan = (TextView) convertView.findViewById(R.id.tv_game_plan);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Game game = (Game) getItem(position);
        if (game != null) {
            holder.gameName.setText(game.getGameName());
            holder.gameExplain.setText(game.getGameExplain());
            holder.gamePlan.setText(game.getGamePlan());
            holder.gameAward.setText(game.getGameAward());
            holder.gameApply.setText(game.getGameApply());
            holder.gameTime.setText(Utils.formatTime(game.getGameTime()));
        }
        return convertView;
    }

    public void setGames(List<Game> games) {
        this.mGames = games;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView gameName;
        TextView gameExplain;
        TextView gamePlan;
        TextView gameAward;
        TextView gameApply;
        TextView gameTime;
    }
}
