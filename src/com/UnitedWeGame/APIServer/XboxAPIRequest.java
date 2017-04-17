package com.UnitedWeGame.APIServer;

import com.UnitedWeGame.InformationObjects.Game;
import com.UnitedWeGame.UserClasses.Friend;
import com.UnitedWeGame.UserClasses.Person;
import com.UnitedWeGame.Utils.JsonUtil;
import net.sf.ezmorph.bean.MorphDynaBean;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by cweeter on 3/10/17.
 */
public class XboxAPIRequest extends APIInterface {

    public static final String XBOX_ONE = "Xbox One";
    public static final String XBOX_360 = "Xbox 360";

    public static final int THREAD_LENGTH = 4;


    public XboxAPIRequest()
    {
        setPlatform(APIInterface.XBOX);
        setBaseApiUrl("https://xboxapi.com/v2/");
    }

    public List<Friend> getFriendsStatus(Person person) {

        String url = getBaseApiUrl() + person.getXboxIdentifier() + "/friends";

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            // add request header
            request.addHeader("X-AUTH", "be3dfb722ca064ff46e1187b1d7894a83c8e922a");
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


            final int threadSize = 1 + (dynaBeans.size()/18);

            for(int i = 0; i < dynaBeans.size() ; i += threadSize)
            {
                final int j = i;

                try {

                    Thread thread = new Thread( new Runnable() {

                        @Override
                        public void run() {

                            List<DynaBean> friendsList = dynaBeans.subList(j, (j + threadSize - 1 < dynaBeans.size()) ? j + threadSize - 1 : dynaBeans.size());

                            try {

                                for(DynaBean friend : friendsList) {

                                    Friend friendObj = new Friend();
                                    friendObj.setGamertag(friend.get("Gamertag").toString());
                                    DynaBean additionalInfoBean = additionalFriendInformation(friend.get("hostId").toString());

                                    friendObj.setIsOnline((additionalInfoBean.get("state").toString().toLowerCase() == "online"));

                                    System.out.println(friendObj.getGamertag() + ":\t" + additionalInfoBean.get("state"));

                                    //friendsList.add(friendObj);
                                }

                            }
                            catch(Exception ex)
                            {

                            }

                        }

                    });

                    thread.start();

                }
                catch (Exception ex)
                {

                }


            }

            return friendsList;
        }
        catch (Exception ex)
        {
            return new ArrayList<>();
        }
    }

    public DynaBean additionalFriendInformation(String identifier) throws Exception {

        String url = getBaseApiUrl() + identifier + "/presence";

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            // add request header
            request.addHeader("X-AUTH", "be3dfb722ca064ff46e1187b1d7894a83c8e922a");
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

        }
        catch (Exception ex)
        {
            throw new Exception("Friend Presence call failed: " + ex);
        }


    }


    public List<Game> getGameLibrary(Person person) {

        List<Game> games = getXboxOneGames(person.getXboxIdentifier());

        games.addAll(getXbox360Games(person.getXboxIdentifier()));

        return games;

    }

    public String getIdentifier() {
        return null;
    }

    /**
     *
     *  Helper Methods
     */

    private List<Game> getXboxOneGames(String identifier)
    {
        List<Game> games = new ArrayList<>();

        try {

            String url = getBaseApiUrl() + identifier + "/xboxonegames";

            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            // add request header
            request.addHeader("X-AUTH", "be3dfb722ca064ff46e1187b1d7894a83c8e922a");
            HttpResponse response = client.execute(request);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            List<DynaBean> dynaBeans = new JsonUtil().decodeJsonList(result.toString());

            for(DynaBean game : (List<DynaBean>)dynaBeans.get(0).get("titles"))
            {
                Game currentGame = new Game();

                currentGame.setPlatform(XBOX_ONE);
                currentGame.setIsOwned(true);
                currentGame.setTitle(game.get("name").toString());

                games.add(currentGame);

            }

            return games;

        }
        catch(Exception ex)
        {
            return games;
        }

    }

    private List<Game> getXbox360Games(String identifier)
    {
        List<Game> games = new ArrayList<>();

        try {

            String url = getBaseApiUrl() + identifier + "/xbox360games";

            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            // add request header
            request.addHeader("X-AUTH", "be3dfb722ca064ff46e1187b1d7894a83c8e922a");
            HttpResponse response = client.execute(request);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            List<DynaBean> dynaBeans = new JsonUtil().decodeJsonList(result.toString());

            for(DynaBean game : (List<DynaBean>)dynaBeans.get(0).get("titles"))
            {
                Game currentGame = new Game();

                currentGame.setPlatform(XBOX_360);
                currentGame.setIsOwned(true);
                currentGame.setTitle(game.get("name").toString());

                games.add(currentGame);
            }

            return games;
        }
        catch(Exception ex)
        {
            return games;
        }

    }
}
