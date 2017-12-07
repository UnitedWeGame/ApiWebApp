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
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cweeter on 3/10/17.
 */
public class XboxAPIRequest extends APIInterface {
    public static final String XBOX_ONE = "XboxOne";
    public static final String XBOX_360 = "Xbox360";

    public XboxAPIRequest() {
        setPlatform(APIInterface.XBOX);
        setBaseApiUrl("https://xboxapi.com/v2/");
    }

    // Get Online Friends
    public List<Friend> getFriendsStatus(Person person) {
        String url = getBaseApiUrl() + person.getXboxIdentifier() + "/friends";
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            // add request header
            request.addHeader("X-AUTH", Property.XBOX_API_TOKEN);
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
            List<Friend> friendsList = new ArrayList<>();
            List<Thread> threads = new ArrayList<>();
            final int threadSize = 1 + (dynaBeans.size() / 18);
            List<UserOnlineFeed> onlineFriends = new ArrayList<>();
            for (int i = 0; i < dynaBeans.size(); i += threadSize) {
                final int j = i;
                try {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<DynaBean> friendsList = dynaBeans.subList(j, (j + threadSize < dynaBeans.size()) ? j + threadSize : dynaBeans.size());
                            //System.out.println("J is: " + j + "listSize is: " + friendsList.size());
                            try {
                                for (DynaBean friend : friendsList) {
                                    try {
                                        Friend friendObj = new Friend();
//
                                        friendObj.setGamertag(friend.get("Gamertag").toString());
                                        DynaBean additionalInfoBean = additionalFriendInformation(friend.get("hostId").toString());
                                        if (additionalInfoBean.get("state").toString().toLowerCase().equals("online")) {
                                            List<DynaBean> devicesList = (List<DynaBean>) additionalInfoBean.get("devices");
                                            DynaBean gameInfo = ((DynaBean) devicesList.get(devicesList.size() - 1));
                                            String type = gameInfo.get("type").toString();
                                            String platform = (StringUtils.equals(type, XBOX_ONE) ? XBOX_ONE : XBOX_360);
                                            List<DynaBean> gameInfoTitles = (List<DynaBean>) gameInfo.get("titles");
                                            if (gameInfoTitles.size() != 1) {
                                                DynaBean gameTitleInfo = ((DynaBean) gameInfoTitles.get(gameInfoTitles.size() - 1));
                                                String gameTitle = gameTitleInfo.get("name").toString();
                                                friendObj.setIsOnline(true);
                                                DatabaseConnectionUtil dbcu = new DatabaseConnectionUtil();
                                                long gameID = dbcu.getGameID(gameTitle, platform);
                                                if (gameID == -1) {
                                                    String imageUrl = new IgdbAPIService().getGameImage(gameTitle);
                                                    List<String> screenshots = new IgdbAPIService().getScreenshots(gameTitle);
                                                    dbcu.insertGameIntoDb(gameTitle, platform, imageUrl, new IgdbAPIService().getGameData(gameTitle));
                                                    gameID = dbcu.getGameID(gameTitle, platform);
                                                    dbcu.insertScreenshotsIntoDB(gameID, new IgdbAPIService().getScreenshots(gameTitle));
                                                }
                                                onlineFriends.add(new UserOnlineFeed(person.getUserId(), gameID, friendObj.getGamertag()));
                                                System.out.println(friendObj.getGamertag() + ":\t" + additionalInfoBean.get("state") + " PLAYING: " + gameTitle);
                                            } else {
                                                System.out.println(friendObj.getGamertag());
                                                if (platform == XBOX_ONE)
                                                    onlineFriends.add(new UserOnlineFeed(person.getUserId(), 999998, friendObj.getGamertag()));
                                                else
                                                    onlineFriends.add(new UserOnlineFeed(person.getUserId(), 999999, friendObj.getGamertag()));
                                            }
                                            DatabaseConnectionUtil dbcu = new DatabaseConnectionUtil();
                                            Connection c = dbcu.deleteOldOnlineFeed(person, XBOX);
                                            for (UserOnlineFeed feed : onlineFriends) {
                                                dbcu.insertIntoOnlineFeed(feed.getUserId(), feed.getGamertag(), feed.getGameId(), XBOX, c);
                                            }
                                            try {
                                                c.commit();
                                                c.setAutoCommit(true);
                                                c.close();
                                                DatabaseConnectionUtil.closedConnections++;
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                        //friendsList.add(friendObj);
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
            Connection c = dbcu.deleteOldOnlineFeed(person, XBOX);
            for (UserOnlineFeed feed : onlineFriends) {
                dbcu.insertIntoOnlineFeed(feed.getUserId(), feed.getGamertag(), feed.getGameId(), XBOX, c);
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

    // Get additional friend info
    public DynaBean additionalFriendInformation(String identifier) throws Exception {
        String url = getBaseApiUrl() + identifier + "/presence";
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            // add request header
            request.addHeader("X-AUTH", Property.XBOX_API_TOKEN);
            HttpResponse response = client.execute(request);
            //System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            List<DynaBean> dynaBeans = new JsonUtil().decodeJsonList(result.toString());
            return dynaBeans.get(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Friend Presence call failed: " + ex);
        }
    }

    // get game library
    public List<Game> getGameLibrary(Person person) {
        System.out.println(person.getXboxGamertag());
        List<Game> games = getXboxOneGames(person.getXboxIdentifier(), person.getUserId());
        games.addAll(getXbox360Games(person.getXboxIdentifier(), person.getUserId()));
        return games;
    }

    // get identifier from gamertag
    public String getIdentifier(Person person) {
        try {
            String url = getBaseApiUrl() + "xuid/" + URLEncoder.encode(person.getXboxGamertag().trim(), "UTF-8");
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            // add request header
            request.addHeader("X-AUTH", Property.XBOX_API_TOKEN);
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception ex) {
            System.err.println("Err: " + ex.getMessage());
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * Helper Methods
     *
     */
    // Get Xbox One games
    private List<Game> getXboxOneGames(String identifier, long userId) {
        List<Game> games = new ArrayList<>();
        try {
            String url = getBaseApiUrl() + identifier + "/xboxonegames";
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            // add request header
            request.addHeader("X-AUTH", Property.XBOX_API_TOKEN);
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
            IgdbAPIService igdb = new IgdbAPIService();
            for (DynaBean game : (List<DynaBean>) dynaBeans.get(0).get("titles")) {
                Game currentGame = new Game();
                currentGame.setPlatform(XBOX_ONE);
                currentGame.setIsOwned(true);
                currentGame.setTitle(game.get("name").toString());
                if (dbcu.getGameID(currentGame.getTitle(), XBOX_ONE) == -1) {
                    String imageUrl = igdb.getGameImage(currentGame.getTitle());
                    List<String> screenshots = igdb.getScreenshots(currentGame.getTitle());
                    dbcu.insertGameIntoDb(currentGame.getTitle(), XBOX_ONE, imageUrl, new IgdbAPIService().getGameData(currentGame.getTitle()));
                    long gameID = dbcu.getGameID(currentGame.getTitle(), XBOX_ONE);
                    dbcu.insertScreenshotsIntoDB(gameID, screenshots);
                }
                long gameID = dbcu.getGameID(currentGame.getTitle(), XBOX_ONE);
                if (!dbcu.userOwnsGame(userId, gameID))
                    dbcu.addGameToUserLibrary(userId, gameID);
                games.add(currentGame);
            }
            return games;
        } catch (Exception ex) {
            ex.printStackTrace();
            return games;
        }
    }

    // Get xbox 360 games
    private List<Game> getXbox360Games(String identifier, long userId) {
        List<Game> games = new ArrayList<>();
        try {
            String url = getBaseApiUrl() + identifier + "/xbox360games";
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            // add request header
            request.addHeader("X-AUTH", Property.XBOX_API_TOKEN);
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
            IgdbAPIService igdb = new IgdbAPIService();
            for (DynaBean game : (List<DynaBean>) dynaBeans.get(0).get("titles")) {
                Game currentGame = new Game();
                currentGame.setPlatform(XBOX_360);
                currentGame.setIsOwned(true);
                currentGame.setTitle(game.get("name").toString());
                if (dbcu.getGameID(currentGame.getTitle(), XBOX_360) == -1) {
                    String imageUrl = igdb.getGameImage(currentGame.getTitle());
                    dbcu.insertGameIntoDb(currentGame.getTitle(), XBOX_360, imageUrl, new IgdbAPIService().getGameData(currentGame.getTitle()));
                    long gameID = dbcu.getGameID(currentGame.getTitle(), XBOX_360);
                    dbcu.insertScreenshotsIntoDB(gameID, igdb.getScreenshots(currentGame.getTitle()));
                }
                long gameID = dbcu.getGameID(currentGame.getTitle(), XBOX_360);
                if (!dbcu.userOwnsGame(userId, gameID))
                    dbcu.addGameToUserLibrary(userId, gameID);
                games.add(currentGame);
            }
            return games;
        } catch (Exception ex) {
            ex.printStackTrace();
            return games;
        }
    }
}
