package com.UnitedWeGame.UserClasses;

/**
 * Created by cweeter on 3/11/17.
 */
public class Person {

    private String xboxGamertag;
    private String xboxIdentifier;
    private long userId;


    public Person()
    {

    }

    public Person(long userId, String gamerTag)
    {
        setUserId(userId);
        setXboxGamertag(gamerTag);
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

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId;}

}
