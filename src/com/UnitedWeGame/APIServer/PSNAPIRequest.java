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

public class PSNAPIRequest extends APIInterface {
    public static final String PSN = "PSN";
    public static Map<String, String> gameList = new HashMap<>();

    public PSNAPIRequest() {
        setPlatform(APIInterface.PSN);
        setBaseApiUrl("http://api.gumerAPI.com/");
    }

    // Get Friend Status
    @Override
    public List<Friend> getFriendsStatus(Person person) {
        String url = getBaseApiUrl() + "IPSNUser/GetFriendList/v0001/?key=" + Property.PSN_API_TOKEN + "&PSNid=" + person.getPSNIdentifier() + "&relationship=friend";
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
                                            sb.append(friend.get("PSNid"));
                                        else
                                            sb.append("," + friend.get("PSNid"));
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
                                        friend.setGamerId(friendStatus.get("PSNid").toString());
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
                                                    String gameId = "", gameName = "";
                                                    try {
                                                        gameId = friendStatus.get("gameid").toString();
                                                        gameName = gameList.get(gameId);
                                                    } catch (Exception ex) {
                                                        ex.printStackTrace();
                                                        dbcu = new DatabaseConnectionUtil();
                                                        gameID = dbcu.getGameID("Online", PSN);
                                                        if (gameID == -1) {
                                                            String imageUrl = "https://vignette4.wikia.nocookie.net/fallout/images/3/3b/PSN-Logo.png/revision/latest?cb=20130607061922";
                                                            dbcu.insertGameIntoDb("Online", PSN, imageUrl, new HashMap<>());
                                                            gameID = dbcu.getGameID("Online", PSN);
                                                        }
                                                        break;
                                                    }
                                                    dbcu = new DatabaseConnectionUtil();
                                                    if (StringUtils.isEmpty(gameName)) {
                                                        String getGameInfoURL = "http://store.PSNpowered.com/api/gamedetails?gameids=" + gameId;
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
                                                        DynaBean bean = (DynaBean) dynaBeans.get(0).get(gameId);
                                                        DynaBean dataBean = (DynaBean) bean.get("data");
                                                        String appName = dataBean.get("name").toString();
                                                        gameID = dbcu.getGameID(appName, PSN);
                                                        List<String> screenshots = new ArrayList<>();
                                                        if (gameID == -1) {
                                                            String imageUrl = dataBean.get("header_image").toString();
                                                            screenshots = new IgdbAPIService().getScreenshots(gameName);
                                                            dbcu.insertGameIntoDb(appName, PSN, imageUrl, new IgdbAPIService().getGameData(appName));
                                                            gameID = dbcu.getGameID(appName, PSN);
                                                            dbcu.insertScreenshotsIntoDB(gameID, screenshots);
                                                            gameList.put(gameId, appName);
                                                        }
                                                    } else {
                                                        gameID = dbcu.getGameID(gameName, PSN);
                                                        if (gameID == -1) {
                                                            String getGameInfoURL = "http://gumerAPI.com/api/gamedetails?gameids=" + gameId;
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
                                                            DynaBean bean = (DynaBean) dynaBeans.get(0).get(gameId);
                                                            DynaBean dataBean = (DynaBean) bean.get("data");
                                                            String appName = dataBean.get("name").toString();
                                                            gameID = dbcu.getGameID(gameName, PSN);
                                                            if (gameID == -1) {
                                                                String imageUrl = dataBean.get("header_image").toString();
                                                                dbcu.insertGameIntoDb(appName, PSN, imageUrl, new IgdbAPIService().getGameData(appName));
                                                                gameID = dbcu.getGameID(appName, PSN);
                                                                dbcu.insertScreenshotsIntoDB(gameID, new IgdbAPIService().getScreenshots(gameName));
                                                                gameList.put(gameId, appName);
                                                            }
                                                        }
                                                    }
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                    friend.setCurrentGame("Unknown Game");
                                                    dbcu = new DatabaseConnectionUtil();
                                                    gameID = dbcu.getGameID("Unknown", PSN);
                                                    if (gameID == -1) {
                                                        String imageUrl = "http://jonvilma.com/images/unknown-1.jpg";
                                                        dbcu.insertGameIntoDb("Unknown", PSN, imageUrl, new HashMap<>());
                                                        gameID = dbcu.getGameID("Unknown", PSN);
                                                    }
                                                }
                                                break;
                                            case "2":
                                                friend.setIsOnline(true);
                                                friend.setCurrentGame("Busy");
                                                friend.setCurrentPlatform(PSN);
                                                dbcu = new DatabaseConnectionUtil();
                                                gameID = dbcu.getGameID("Store", PSN);
                                                if (gameID == -1) {
                                                    String imageUrl = "https://lh4.ggpht.com/58HEelKMZA7jUDxvMhx_71a7-tZ67caWjS1D-JpuCxfjQbjPjZKLtlOdHzG49YPvg3c=w300";
                                                    dbcu.insertGameIntoDb("Busy", PSN, imageUrl, new HashMap<>());
                                                    gameID = dbcu.getGameID("Busy", PSN);
                                                }

                                                break;
                                        }
                                        if (friend.isOnline())
                                            onlineFriends.add(new UserOnlineFeed(person.getUserId(), gameID, friend.getGamertag()));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                DatabaseConnectionUtil dbcu = new DatabaseConnectionUtil();
                                Connection c = dbcu.deleteOldOnlineFeed(person, PSN);
                                for (UserOnlineFeed feed : onlineFriends) {
                                    dbcu.insertIntoOnlineFeed(feed.getUserId(), feed.getGamertag(), feed.getGameId(), PSN, c);
                                }
                                try {
                                    c.commit();
                                    c.setAutoCommit(true);
                                    c.close();
                                    DatabaseConnectionUtil.closedConnections++;
                                } catch (Exception ex) {
                                    ex.printStackTrace();
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
            Connection c = dbcu.deleteOldOnlineFeed(person, PSN);
            for (UserOnlineFeed feed : onlineFriends) {
                dbcu.insertIntoOnlineFeed(feed.getUserId(), feed.getGamertag(), feed.getGameId(), PSN, c);
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

    // Get Additional Friend info
    public List<DynaBean> additionalFriendInformation(String identifiers) {
        String url = getBaseApiUrl() + "IPSNUser/GetPlayerSummaries/v0002/?key=" + Property.PSN_API_TOKEN + "&PSNids=" + identifiers;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            // add request header
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";

            List<DynaBean> dynaBeans = new JsonUtil().decodeJsonList(result.toString());
            return (List<DynaBean>) ((DynaBean) dynaBeans.get(0).get("response")).get("players");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }

    // Get Game Library
    @Override
    public List<Game> getGameLibrary(Person person) {
        long startTime = System.currentTimeMillis();
        String url = getBaseApiUrl() + "IPlayerService/GetOwnedGames/v0001/?key=" + Property.PSN_API_TOKEN + "&PSNid=" + person.getPSNIdentifier() + "&include_played_free_games=1&include_gameinfo=1";
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
                    System.out.println(person.getPSNIdentifier());
                    String baseImageURL = "http://media.gumerAPI/games/{gameid}/{hash}.jpg";
                    Game currentGame = new Game();
                    currentGame.setPlatform(PSN);
                    currentGame.setIsOwned(true);
                    currentGame.setTitle(game.get("name").toString());
                    gameList.put(game.get("gameid").toString(), currentGame.getTitle());
                    if (dbcu.getGameID(currentGame.getTitle(), PSN) == -1) {
                        try {
                            if (StringUtils.isEmpty(game.get("gameid").toString()) || StringUtils.isEmpty(game.get("img_logo_url").toString())) {
                                baseImageURL = "http://unitedwegame.herokugame.com/No_Image_Found_PSN.png";
                            } else {
                                baseImageURL = baseImageURL.replace("{gameid}", game.get("gameid").toString());
                                baseImageURL = baseImageURL.replace("{hash}", game.get("img_logo_url").toString());
                            }
                            dbcu.insertGameIntoDb(currentGame.getTitle(), PSN, baseImageURL, new IgdbAPIService().getGameData(currentGame.getTitle()));
                            long gameID = dbcu.getGameID(currentGame.getTitle(), PSN);
                            dbcu.insertScreenshotsIntoDB(gameID, new IgdbAPIService().getScreenshots(currentGame.getTitle()));
                            gameList.put(game.get("gameid").toString(), currentGame.getTitle());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            String imageURL = "http://unitedwegame.herokugame.com/No_Image_Found_PSN.png";
                            dbcu.insertGameIntoDb(currentGame.getTitle(), PSN, imageURL, new IgdbAPIService().getGameData(currentGame.getTitle()));
                            long gameID = dbcu.getGameID(currentGame.getTitle(), PSN);
                            dbcu.insertScreenshotsIntoDB(gameID, new IgdbAPIService().getScreenshots(currentGame.getTitle()));
                            gameList.put(game.get("gameid").toString(), currentGame.getTitle());
                        }
                    }
                    long gameID = dbcu.getGameID(currentGame.getTitle(), PSN);
                    if (!dbcu.userOwnsGame(person.getUserId(), gameID))
                        dbcu.addGameToUserLibrary(person.getUserId(), gameID);
                    games.add(currentGame);
                } catch (Exception ex) {
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

    // Get Identifier
    @Override
    public String getIdentifier(Person person) {
        return person.getPSNIdentifier();
    }
}
