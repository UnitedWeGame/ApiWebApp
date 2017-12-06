package com.UnitedWeGame.InformationObjects;

/**
 * Created by cweeter on 4/24/17.
 */
public class UserOnlineFeed {
    private long userId;
    private String gamertag;
    private long gameId;

    public UserOnlineFeed(long userId, long gameId, String gamertag) {
        setUserId(userId);
        setGameId(gameId);
        setGamertag(gamertag);
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getGamertag() {
        return gamertag;
    }

    public void setGamertag(String gamertag) {
        this.gamertag = gamertag;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }
}
