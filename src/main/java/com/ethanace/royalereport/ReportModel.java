package com.ethanace.royalereport;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author ethanace
 */

public class ReportModel {
    
    private NetModel netModel = new NetModel();
    
    private String buildReport(String tag, String auth) {
        
        return null;
        
    }

    public boolean buildPerformanceReport(String tag, String token) {

        //String[] columnHeaders = {"Tag", "Name", "Participation Rate"};
        String template = "https://api.clashroyale.com/v1/clans/%s/riverracelog";
        String url = String.format(template, tag.replace("#", "%23"));
        JSONObject response = new JSONObject(netModel.HTTPGet(url, token));
        JSONArray items = response.getJSONArray("items");
        JSONArray standings = items.getJSONObject(0).getJSONArray("standings");

        for (int i = 0; i < standings.length(); i++) {
            JSONObject standing = standings.getJSONObject(i);
            JSONObject clan = standing.getJSONObject("clan");
            System.out.println(clan.get("name"));
        }
        

        return false;
    }
    
}
