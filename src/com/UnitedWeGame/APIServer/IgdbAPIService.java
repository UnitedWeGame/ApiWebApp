package com.UnitedWeGame.APIServer;

import com.UnitedWeGame.Utils.JsonUtil;
import com.UnitedWeGame.Utils.Property;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;
import uk.ac.shef.wit.simmetrics.*;
import uk.ac.shef.wit.simmetrics.similaritymetrics.*;

/**
 * Created by cweeter on 4/20/17.
 */
public class IgdbAPIService {

    public final String BASE_URL = "https://igdbcom-internet-game-database-v1.p.mashape.com/";

    public String getGameImage(String gameTitle)
    {


        if(gameTitle.equalsIgnoreCase("Netflix"))
        {
            return "http://film.medialt.no/wp-content/uploads/2015/04/netflix_logo.jpg";
        }
        else if(gameTitle.equalsIgnoreCase("Amazon Instant Video"))
        {
            return "http://channels.roku.com/images/5fb394e18d1243c38918ffc2dc39d83c-hd.jpg";
        }
        else if(gameTitle.equalsIgnoreCase("YouTube"))
        {
            return "https://www.advancedwebranking.com/blog/wp-content/uploads/2013/06/youtube.jpg";
        }
        else if(gameTitle.equalsIgnoreCase("Store"))
        {
            return "http://compass.xboxlive.com/assets/3a/bd/3abd3348-1504-4355-ba58-1ad52686cbc1.jpg?n=181x141_x360games_x360store.jpg";
        }




        String imageURL = "http://unitedwegame.herokuapp.com/no-image-found.jpg";

        String url = BASE_URL + "games/?fields=name%2Ccover%2Cscreenshots&limit=3&offset=0&search=";

        try {

            url += URLEncoder.encode(gameTitle, "UTF-8");

            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            // add request header
            request.addHeader("X-Mashape-Key", Property.IGDB_API_TOKEN);
            request.addHeader("Accept", "application/json");
            HttpResponse response = client.execute(request);

            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
                result.append(line);
            }

            List<DynaBean> dynaBeans = new JsonUtil().decodeJsonList(result.toString());

            float maxSimilarity = 0.0f;
            int indexOfMostSimilar = 0;
            int count = 0;
            gameTitle = gameTitle.replace("®", "");
            gameTitle = gameTitle.replace("™", "");


            for(DynaBean bean : dynaBeans)
            {
                MongeElkan metric = new MongeElkan();
                CosineSimilarity cs = new CosineSimilarity();
                JaccardSimilarity js = new JaccardSimilarity();
                Jaro jaro = new Jaro();

                float similarity = metric.getSimilarity(bean.get("name").toString().toLowerCase(), gameTitle.toLowerCase());
                float similarity2 = cs.getSimilarity(bean.get("name").toString().toLowerCase(), gameTitle.toLowerCase());
                float similarity3 = js.getSimilarity(bean.get("name").toString().toLowerCase(), gameTitle.toLowerCase());
                float similarity4 = jaro.getSimilarity(bean.get("name").toString().toLowerCase(), gameTitle.toLowerCase());

                float overallSimilarity = (similarity + similarity2 + similarity3 + similarity4)/4.0f;


                if(overallSimilarity > maxSimilarity)
                {
                    indexOfMostSimilar = count;
                    maxSimilarity = overallSimilarity;
                }

                count++;
            }

            DynaBean gameBean = dynaBeans.get(indexOfMostSimilar);

            boolean hasCover = false;
            boolean hasScreenshot = false;

            for(DynaProperty property : gameBean.getDynaClass().getDynaProperties())
            {
                if(property.getName().equalsIgnoreCase("cover"))
                    hasCover = true;

                if(property.getName().equalsIgnoreCase("screenshots"))
                    hasScreenshot = true;
            }
            if(hasCover)
            {
                imageURL = "http:" + ((DynaBean)gameBean.get("cover")).get("url").toString();
                imageURL = imageURL.replaceAll("t_thumb", "t_cover_big");
            }
            else if(hasScreenshot)
            {
                List<DynaBean> screenshots = ((List<DynaBean>) gameBean.get("screenshots"));

                imageURL = "http:" + screenshots.get(0).get("url").toString();
                imageURL = imageURL.replaceAll("t_thumb", "t_cover_big");
            }
        }
        catch (Exception ex)
        {
            System.err.println("Error with the IGDB API call! Exception: ");
            ex.printStackTrace();
        }
            return imageURL;

        }
}
