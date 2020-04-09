package com.openway.square.wrumwrum.ui.rating;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.RatingData;
import com.openway.square.wrumwrum.data.model.UserPlace;

import java.util.ArrayList;

public class RatingRecyclerAdapter extends RecyclerView.Adapter<RatingRecyclerAdapter.RatingViewHolder> {

    private Integer yourPlace;
    private Integer yourPoints;
    private String competitionEnds;
    private ArrayList<UserPlace> topUsers = new ArrayList<>();

    public RatingRecyclerAdapter(@NonNull RatingData ratingData) {
        this.yourPlace = ratingData.getYourPlace();
        this.yourPoints = ratingData.getYourPoints();
        this.competitionEnds = ratingData.getCompetitionEnds();
        this.topUsers = new ArrayList<>(ratingData.getTopTenants());
    }

    public static class RatingViewHolder extends RecyclerView.ViewHolder {

        public TextView tvRatingPlace;
        public TextView tvRatingUsername;
        public TextView tvRatingPoints;

        public RatingViewHolder(View itemView) {
            super(itemView);
            this.tvRatingPlace = itemView.findViewById(R.id.tv_rating_place);
            this.tvRatingUsername = itemView.findViewById(R.id.tv_rating_username);
            this.tvRatingPoints = itemView.findViewById(R.id.tv_rating_points);
        }
    }

    @NonNull
    @Override
    public RatingRecyclerAdapter.RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RatingRecyclerAdapter.RatingViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_rating, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RatingRecyclerAdapter.RatingViewHolder holder, int position) {
        Resources resources = holder.itemView.getContext().getResources();
        final UserPlace topUser = topUsers.get(position);
        if (position + 1 == yourPlace) {
            holder.itemView.setBackgroundColor(resources.getColor(R.color.colorAccent));
            holder.tvRatingPlace.setTextColor(resources.getColor(R.color.accent));
            holder.tvRatingUsername.setTextColor(resources.getColor(R.color.accent));
            holder.tvRatingPoints.setTextColor(resources.getColor(R.color.accent));
        }
        holder.tvRatingPlace.setText(Integer.valueOf(position + 1).toString());
        holder.tvRatingUsername.setText(topUser.getUsername());
        holder.tvRatingPoints.setText(topUser.getRatingPoints().toString());
    }

    @Override
    public int getItemCount() {
        return topUsers.size();
    }

    public void clear() {
        topUsers.clear();
        notifyDataSetChanged();
    }

    public void update(final RatingData ratingData) {
        this.yourPlace = ratingData.getYourPlace();
        this.yourPoints = ratingData.getYourPoints();
        this.competitionEnds = ratingData.getCompetitionEnds();
        this.topUsers = new ArrayList<>(ratingData.getTopTenants());
        notifyDataSetChanged();
    }

}
