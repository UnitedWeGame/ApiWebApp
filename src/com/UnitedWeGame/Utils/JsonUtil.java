package com.UnitedWeGame.Utils;

import org.apache.commons.beanutils.DynaBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import net.sf.json.JSONObject;


/**
 * Created by cweeter on 3/11/17.
 */
public class JsonUtil {

    public static List<DynaBean> decodeJsonList(String jsonString)
    {
        try
        {
            String json = "{data:"+jsonString+"}";
            JSONObject jsonObject = JSONObject.fromObject(json);
            DynaBean jsonBean = (DynaBean) JSONObject.toBean(jsonObject);
            Object feed = jsonBean.get("data");
            if (feed instanceof List)
                return (List<DynaBean>)feed;
            else if (feed instanceof DynaBean)
            {
                List<DynaBean> result = new Vector<DynaBean>();
                result.add((DynaBean)feed);
                return result;
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            System.err.println(e.getCause().getMessage());

            e.printStackTrace();
        }
        return new ArrayList<DynaBean>();
    }

}
