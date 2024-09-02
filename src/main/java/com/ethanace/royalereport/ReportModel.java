package com.ethanace.royalereport;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author ethanace
 */

public class ReportModel {
    
    
    private final NetModel NET_MODEL;
    private final IOModel IO_MODEL;
    private final String API_ENDPOINT = "https://api.clashroyale.com/";

    public ReportModel() throws IOException {
        NET_MODEL = new NetModel();
        IO_MODEL = new IOModel(null);
    }

    public void buildPerformanceReport(String tag, String token) {

        String[] columnHeaders = {"Rank", "Name"};
        String template = API_ENDPOINT + "v1/clans/%s/riverracelog";
        String url = String.format(template, tag.replace("#", "%23"));
        JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
        JSONArray items = response.getJSONArray("items");
        StringBuilder row = new StringBuilder();
        
        ArrayList<String> data = new ArrayList<>();

        for (int war = 0; war < items.length(); war++) {
            JSONArray standings = items.getJSONObject(war).getJSONArray("standings");
            
            for (int i = 0; i < standings.length(); i++) {

                JSONObject warResult = standings.getJSONObject(i);
                JSONObject clan = warResult.getJSONObject("clan");
                String clanRank = warResult.get("rank").toString();
                String clanName = clan.get("name").toString();

                row.append(clanRank);
                row.append(",");
                row.append(clanName);

                data.add(row.toString());

                row = new StringBuilder();

                //System.out.println(clanRank + " " + clanName);
            }
    
            IO_MODEL.writeToFile(data, columnHeaders, "performanceReport");

        }

    }
    
}
