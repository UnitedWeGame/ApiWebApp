package com.UnitedWeGame.APIServer;

import com.UnitedWeGame.InformationObjects.Game;
import com.UnitedWeGame.InformationObjects.UserOnlineFeed;
import com.UnitedWeGame.UserClasses.Friend;
import com.UnitedWeGame.UserClasses.Person;
import com.UnitedWeGame.Utils.DatabaseConnectionUtil;
import com.UnitedWeGame.Utils.JsonUtil;
import com.UnitedWeGame.Utils.Property;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: cweeter
 * Date: 8/14/17
 */
public class SteamAPIRequest extends APIInterface {

    public static final String STEAM = "Steam";
    public static Map<String, String> gameList = new HashMap<>();

    public SteamAPIRequest() {
        setPlatform(APIInterface.STEAM);
        setBaseApiUrl("http://api.steampowered.com/");
    }


    @Override
    public List<Friend> getFriendsStatus(Person person) {

        String url = getBaseApiUrl() + "ISteamUser/GetFriendList/v0001/?key=" + Property.STEAM_API_TOKEN + "&steamid=" + person.getSteamIdentifier() + "&relationship=friend";

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            // add request header
            HttpResponse response = client.execute(request);

            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            List<DynaBean> dynaBeans = new JsonUtil().decodeJsonList(result.toString());

            List<DynaBean> friendBeans = (List<DynaBean>) ((DynaBean) dynaBeans.get(0).get("friendslist")).get("friends");

            List<Friend> friendsList = new ArrayList<>();

            List<Thread> threads = new ArrayList<>();

            final int threadSize = 100;
            List<UserOnlineFeed> onlineFriends = new ArrayList<>();

            for (int i = 0; i < dynaBeans.size(); i += threadSize) {
                final int j = i;

                try {

                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {

                            List<DynaBean> friendsList = friendBeans.subList(j, (j + threadSize < friendBeans.size()) ? j + threadSize : friendBeans.size());

                            StringBuilder sb = new StringBuilder();


                            try {

                                for (DynaBean friend : friendsList) {

                                    try {

                                        if (sb.toString().equals(""))
                                            sb.append(friend.get("steamid"));
                                        else
                                            sb.append("," + friend.get("steamid"));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                List<DynaBean> friendStatuses = additionalFriendInformation(sb.toString());
                                List<Friend> friends = new ArrayList<>();

                                for (DynaBean friendStatus : friendStatuses) {
                                    try {
                                        Friend friend = new Friend();
                                        friend.setGamertag(friendStatus.get("personaname").toString());
                                        friend.setGamerId(friendStatus.get("steamid").toString());

                                        String onlineStatus = friendStatus.get("personastate").toString();
                                        DatabaseConnectionUtil dbcu;
                                        long gameID = 0l;

                                        switch (onlineStatus) {
                                            case "0":
                                                friend.setIsOnline(false);
                                                break;
                                            case "1":
                                                friend.setIsOnline(true);

                                                try {

                                                    String appId = "", gameName = "";

                                                    try {
                                                        appId = friendStatus.get("gameid").toString();

                                                        gameName = gameList.get(appId);
                                                    }
                                                    catch (Exception ex)
                                                    {
                                                        ex.printStackTrace();

                                                        dbcu = new DatabaseConnectionUtil();

                                                        gameID = dbcu.getGameID("Online", STEAM);

                                                        if (gameID == -1) {

                                                            String imageUrl = "https://vignette4.wikia.nocookie.net/fallout/images/3/3b/Steam-Logo.png/revision/latest?cb=20130607061922";

                                                            dbcu.insertGameIntoDb("Online", STEAM, imageUrl);

                                                            gameID = dbcu.getGameID("Online", STEAM);
                                                        }

                                                        break;
                                                    }

                                                    dbcu = new DatabaseConnectionUtil();

                                                    if (StringUtils.isEmpty(gameName)) {
                                                        String getGameInfoURL = "http://store.steampowered.com/api/appdetails?appids=" + appId;


                                                        HttpClient client = HttpClientBuilder.create().build();
                                                        HttpGet request = new HttpGet(getGameInfoURL);

                                                        // add request header
                                                        HttpResponse response = client.execute(request);

                                                        BufferedReader rd = new BufferedReader(
                                                                new InputStreamReader(response.getEntity().getContent()));

                                                        StringBuffer result = new StringBuffer();
                                                        String line = "";
                                                        while ((line = rd.readLine()) != null) {
                                                            result.append(line);
                                                        }

                                                        List<DynaBean> dynaBeans = new JsonUtil().decodeJsonList(result.toString());
                                                        dbcu = new DatabaseConnectionUtil();

                                                        DynaBean bean = (DynaBean) dynaBeans.get(0).get(appId);
                                                        DynaBean dataBean = (DynaBean) bean.get("data");
                                                        String appName = dataBean.get("name").toString();

                                                        gameID = dbcu.getGameID(appName, STEAM);


                                                        List<String> screenshots = new ArrayList<>();

                                                        if (gameID == -1) {

                                                            String imageUrl = dataBean.get("header_image").toString();
                                                            imageUrl = imageUrl.replace("\\", "");

                                                            screenshots = new IgdbAPIService().getScreenshots(appName);

                                                            dbcu.insertGameIntoDb(appName, STEAM, imageUrl);

                                                            gameID = dbcu.getGameID(appName, STEAM);

                                                            dbcu.insertScreenshotsIntoDB(gameID, screenshots);

                                                            gameList.put(appId, appName);

                                                        }

                                                    } else {
                                                        gameID = dbcu.getGameID(gameName, STEAM);

                                                        if(gameID == -1)
                                                        {
                                                            String getGameInfoURL = "http://store.steampowered.com/api/appdetails?appids=" + appId;


                                                            HttpClient client = HttpClientBuilder.create().build();
                                                            HttpGet request = new HttpGet(getGameInfoURL);

                                                            // add request header
                                                            HttpResponse response = client.execute(request);

                                                            BufferedReader rd = new BufferedReader(
                                                                    new InputStreamReader(response.getEntity().getContent()));

                                                            StringBuffer result = new StringBuffer();
                                                            String line = "";
                                                            while ((line = rd.readLine()) != null) {
                                                                result.append(line);
                                                            }

                                                            List<DynaBean> dynaBeans = new JsonUtil().decodeJsonList(result.toString());
                                                            dbcu = new DatabaseConnectionUtil();

                                                            DynaBean bean = (DynaBean) dynaBeans.get(0).get(appId);
                                                            DynaBean dataBean = (DynaBean) bean.get("data");
                                                            String appName = dataBean.get("name").toString();

                                                            gameID = dbcu.getGameID(appName, STEAM);


                                                            if (gameID == -1) {

                                                                String imageUrl = dataBean.get("header_image").toString();
                                                                imageUrl = imageUrl.replace("\\", "");

                                                                dbcu.insertGameIntoDb(appName, STEAM, imageUrl);

                                                                gameID = dbcu.getGameID(appName, STEAM);

                                                                dbcu.insertScreenshotsIntoDB(gameID, new IgdbAPIService().getScreenshots(appName));

                                                                gameList.put(appId, appName);

                                                            }
                                                        }
                                                    }


                                                } catch (Exception ex) {
                                                    ex.printStackTrace();

                                                    friend.setCurrentGame("Unknown");

                                                    dbcu = new DatabaseConnectionUtil();

                                                    gameID = dbcu.getGameID("Unknown", STEAM);

                                                    if (gameID == -1) {

                                                        String imageUrl = "http://jonvilma.com/images/unknown-1.jpg";

                                                        dbcu.insertGameIntoDb("Unknown", STEAM, imageUrl);

                                                        gameID = dbcu.getGameID("Unknown", STEAM);
                                                    }
                                                }

                                                break;
                                            case "2":
                                                friend.setIsOnline(true);
                                                friend.setCurrentGame("Busy");
                                                friend.setCurrentPlatform(STEAM);

                                                dbcu = new DatabaseConnectionUtil();

                                                gameID = dbcu.getGameID("Busy", STEAM);

                                                if (gameID == -1) {

                                                    String imageUrl = "https://lh4.ggpht.com/58HEelKMZA7jUDxvMhx_71a7-tZ67caWjS1D-JpuCxfjQbjPjZKLtlOdHzG49YPvg3c=w300";

                                                    dbcu.insertGameIntoDb("Busy", STEAM, imageUrl);

                                                    gameID = dbcu.getGameID("Busy", STEAM);
                                                }
                                                break;
                                            case "3":
                                                friend.setIsOnline(true);
                                                friend.setCurrentGame("Away");
                                                friend.setCurrentPlatform(STEAM);

                                                dbcu = new DatabaseConnectionUtil();

                                                gameID = dbcu.getGameID("Away", STEAM);

                                                if (gameID == -1) {

                                                    String imageUrl = "https://pbs.twimg.com/profile_images/656299279900913664/Q0k7qq_E.jpg";

                                                    dbcu.insertGameIntoDb("Away", STEAM, imageUrl);

                                                    gameID = dbcu.getGameID("Away", STEAM);
                                                }
                                                break;
                                            case "4":
                                                friend.setIsOnline(true);
                                                friend.setCurrentGame("Snooze");
                                                friend.setCurrentPlatform(STEAM);

                                                dbcu = new DatabaseConnectionUtil();

                                                gameID = dbcu.getGameID("Snooze", STEAM);

                                                if (gameID == -1) {

                                                    String imageUrl = "https://www.mailbutler.io/images/MB_Icon_Snooze.svg";

                                                    dbcu.insertGameIntoDb("Snooze", STEAM, imageUrl);

                                                    gameID = dbcu.getGameID("Snooze", STEAM);
                                                }
                                                break;
                                            case "5":
                                                friend.setIsOnline(true);
                                                friend.setCurrentGame("Looking To Trade");
                                                friend.setCurrentPlatform(STEAM);

                                                dbcu = new DatabaseConnectionUtil();

                                                gameID = dbcu.getGameID("Looking To Trade", STEAM);

                                                if (gameID == -1) {

                                                    String imageUrl = "http://www.mncgroups.com/images/service/tradelicence-service-mncgroups.png";

                                                    dbcu.insertGameIntoDb("Looking To Trade", STEAM, imageUrl);

                                                    gameID = dbcu.getGameID("Looking To Trade", STEAM);
                                                }
                                                break;

                                            case "6":
                                                friend.setIsOnline(true);
                                                friend.setCurrentGame("Looking To Play");
                                                friend.setCurrentPlatform(STEAM);

                                                dbcu = new DatabaseConnectionUtil();

                                                gameID = dbcu.getGameID("Looking To Play", STEAM);

                                                if (gameID == -1) {

                                                    String imageUrl = "https://lh3.googleusercontent.com/608ZzH6Ky9Rj6xQaT4YpEZfOc58TYE3FYbTyHkiKj3OLOFXbB1IHxyNYK_H70ayh4kY=w300";

                                                    dbcu.insertGameIntoDb("Looking To Play", STEAM, imageUrl);

                                                    gameID = dbcu.getGameID("Looking To Play", STEAM);
                                                }
                                                break;
                                        }

                                        if (friend.isOnline())
                                            onlineFriends.add(new UserOnlineFeed(person.getUserId(), gameID, friend.getGamertag()));


                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }


                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        }
                    });

                    threads.add(thread);

                    thread.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

            DatabaseConnectionUtil dbcu = new DatabaseConnectionUtil();

            for (Thread thread : threads) {
                thread.join();
            }

            Connection c = dbcu.deleteOldOnlineFeed(person, STEAM);

            for (UserOnlineFeed feed : onlineFriends) {
                dbcu.insertIntoOnlineFeed(feed.getUserId(), feed.getGamertag(), feed.getGameId(), STEAM, c);
            }

            try {
                c.commit();
                c.setAutoCommit(true);
                c.close();
                DatabaseConnectionUtil.closedConnections++;
            } catch (Exception ex) {
                ex.printStackTrace();

            }

            return friendsList;
        } catch (Exception ex) {
            ex.printStackTrace();

            return new ArrayList<>();
        }
    }


    public List<DynaBean> additionalFriendInformation(String identifiers) {

        String url = getBaseApiUrl() + "ISteamUser/GetPlayerSummaries/v0002/?key=" + Property.STEAM_API_TOKEN + "&steamids=" + identifiers;

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            // add request header
            HttpResponse response = client.execute(request);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            List<DynaBean> dynaBeans = new JsonUtil().decodeJsonList(result.toString());

            return (List<DynaBean>) ((DynaBean) dynaBeans.get(0).get("response")).get("players");

        } catch (Exception ex) {
            ex.printStackTrace();

        }

        return new ArrayList<>();
    }

    @Override
    public List<Game> getGameLibrary(Person person) {

        long startTime = System.currentTimeMillis();


        String url = getBaseApiUrl() + "IPlayerService/GetOwnedGames/v0001/?key=" + Property.STEAM_API_TOKEN + "&steamid=" + person.getSteamIdentifier() + "&include_played_free_games=1&include_appinfo=1";
        List<Game> games = new ArrayList<>();

        try {

            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            // add request header
            HttpResponse response = client.execute(request);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            List<DynaBean> dynaBeans = new JsonUtil().decodeJsonList(result.toString());
            DatabaseConnectionUtil dbcu = new DatabaseConnectionUtil();

            for (DynaBean game : (List<DynaBean>) ((DynaBean) dynaBeans.get(0).get("response")).get("games")) {
                try {
                    System.out.println(person.getSteamIdentifier());
                    String baseImageURL = "http://media.steampowered.com/steamcommunity/public/images/apps/{appid}/{hash}.jpg";
                    Game currentGame = new Game();

                    currentGame.setPlatform(STEAM);
                    currentGame.setIsOwned(true);
                    currentGame.setTitle(game.get("name").toString());

                    gameList.put(game.get("appid").toString(), currentGame.getTitle());

                    if (dbcu.getGameID(currentGame.getTitle(), STEAM) == -1) {

                        try {
                            if(StringUtils.isEmpty(game.get("appid").toString()) || StringUtils.isEmpty(game.get("img_logo_url").toString()))
                            {
                                baseImageURL = "http://unitedwegame.herokuapp.com/No_Image_Found_Steam.png";
                            }
                            else
                            {
                                baseImageURL = baseImageURL.replace("{appid}", game.get("appid").toString());
                                baseImageURL = baseImageURL.replace("{hash}", game.get("img_logo_url").toString());
                            }

                            dbcu.insertGameIntoDb(currentGame.getTitle(), STEAM, baseImageURL);

                            long gameID = dbcu.getGameID(currentGame.getTitle(), STEAM);

                            dbcu.insertScreenshotsIntoDB(gameID, new IgdbAPIService().getScreenshots(currentGame.getTitle()));


                            gameList.put(game.get("appid").toString(), currentGame.getTitle());
                        } catch (Exception ex) {
                            ex.printStackTrace();

                            String imageURL = "http://unitedwegame.herokuapp.com/No_Image_Found_Steam.png";

                            dbcu.insertGameIntoDb(currentGame.getTitle(), STEAM, imageURL);

                            long gameID = dbcu.getGameID(currentGame.getTitle(), STEAM);

                            dbcu.insertScreenshotsIntoDB(gameID, new IgdbAPIService().getScreenshots(currentGame.getTitle()));

                            gameList.put(game.get("appid").toString(), currentGame.getTitle());

                        }
                    }

                    long gameID = dbcu.getGameID(currentGame.getTitle(), STEAM);

                    if (!dbcu.userOwnsGame(person.getUserId(), gameID))
                        dbcu.addGameToUserLibrary(person.getUserId(), gameID);

                    games.add(currentGame);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();

                }
            }


            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println("TIME OF EXECUTION! " + elapsedTime);

            return games;

        } catch (Exception ex) {
            ex.printStackTrace();

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println("TIME OF EXECUTION! " + elapsedTime);

            return games;
        }
    }

    @Override
    public String getIdentifier(Person person) {

        return person.getSteamIdentifier();
    }
}
