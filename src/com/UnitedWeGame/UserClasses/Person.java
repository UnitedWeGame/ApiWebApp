package com.UnitedWeGame.UserClasses;

/**
 * Created by cweeter on 3/11/17.
 */
public class Person {
    private String xboxGamertag;
    private String xboxIdentifier;
    private long userId;
    private String steamIdentifier;
    private String psnIdentifier;

    public Person() {
    }

    public Person(long userId, String gamerTag, String steamIdentifier) {
        setUserId(userId);
        setXboxGamertag(gamerTag);
        setSteamIdentifier(steamIdentifier);
    }

    public String getXboxIdentifier() {
        return xboxIdentifier;
    }

    public void setXboxIdentifier(String xboxIdentifier) {
        this.xboxIdentifier = xboxIdentifier;
    }

    public String getXboxGamertag() {
        return xboxGamertag;
    }

    public void setXboxGamertag(String xboxGamertag) {
        this.xboxGamertag = xboxGamertag;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getSteamIdentifier() {
        return steamIdentifier;
    }

    public void setSteamIdentifier(String steamIdentifier) {
        this.steamIdentifier = steamIdentifier;
    }

    public String getPSNIdentifier() {
        return psnIdentifier;
    }

    public Person setPSNIdentifier(String psnIdentifier) {
        this.psnIdentifier = psnIdentifier;
        return this;
    }
}
