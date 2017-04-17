package com.UnitedWeGame.InformationObjects;

/**
 * Created by cweeter on 3/10/17.
 */
public class Game {

    public final static String XBOX_ONE = "Xbox One";
    public final static String PLAYSTATION = "Playstation";
    public final static String STEAM = "Steam";
    public final static String XBOX_360 = "Xbox 360";

    private String title;
    private String platform;
    private boolean isOwned;

    public boolean isOwned() {
        return isOwned;
    }

    public void setIsOwned(boolean isOwned) {
        this.isOwned = isOwned;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
