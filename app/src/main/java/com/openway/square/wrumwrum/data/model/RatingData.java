package com.openway.square.wrumwrum.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RatingData {

    @SerializedName("your_place")
    @Expose
    private Integer yourPlace;

    @SerializedName("your_points")
    @Expose
    private Integer yourPoints;

    @SerializedName("competition_end")
    @Expose
    private String CompetitionEnds;

    @SerializedName("tenants_top")
    @Expose
    private List<UserPlace> topTenants;

    public Integer getYourPlace() {
        return yourPlace;
    }

    public void setYourPlace(Integer yourPlace) {
        this.yourPlace = yourPlace;
    }

    public Integer getYourPoints() {
        return yourPoints;
    }

    public void setYourPoints(Integer yourPoints) {
        this.yourPoints = yourPoints;
    }

    public String getCompetitionEnds() {
        return CompetitionEnds;
    }

    public void setCompetitionEnds(String competitionEnds) {
        CompetitionEnds = competitionEnds;
    }

    public List<UserPlace> getTopTenants() {
        return topTenants;
    }

    public void setTopTenants(List<UserPlace> topTenants) {
        this.topTenants = topTenants;
    }
}
