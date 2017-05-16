package com.UnitedWeGame.APIServer;

import com.UnitedWeGame.InformationObjects.Game;
import com.UnitedWeGame.UserClasses.Friend;
import com.UnitedWeGame.UserClasses.Person;
import com.sun.deploy.util.StringUtils;
import org.apache.commons.beanutils.DynaBean;

import java.util.List;

/**
 * Created by cweeter on 3/10/17.
 */
public abstract class APIInterface {


    public static final String XBOX = "xbox";
    public static final String PLAYSTATION = "psn";
    public static final String STEAM = "steam";

    protected String platform;

    protected String baseApiUrl;

    public abstract List<Friend> getFriendsStatus(Person person);

    public abstract DynaBean additionalFriendInformation(String identifier) throws Exception;

    public abstract List<Game> getGameLibrary(Person person);

    public abstract String getIdentifier(Person person);

    public final String getPlatform()
    {
        return platform;
    }

    protected final void setPlatform(String platform)
    {
        this.platform = platform;
    }

    public final String getBaseApiUrl() {
        return baseApiUrl;
    }

    public final void setBaseApiUrl(String baseApiUrl) {
        this.baseApiUrl = baseApiUrl;
    }

}
