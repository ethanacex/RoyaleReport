package com.ethanace;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

/**
 * @author ethanace
 */

public class ReportModel {
    
    private final NetModel NET_MODEL;
    private final IOModel IO_MODEL;
    private final String API_ENDPOINT = "https://api.clashroyale.com/";

    public ReportModel(NetModel netModel, IOModel ioModel) throws IOException {
        NET_MODEL = netModel;
        IO_MODEL = ioModel;
        Logger.info("ReportModel initialised successfully");
    }

    public JSONObject getRiverRaceLog(String clanTag, String token) throws Exception {
        String template = API_ENDPOINT + "v1/clans/%s/riverracelog";
        String url = String.format(template, clanTag.replace("#", "%23"));
        
        JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
        JSONArray items = response.getJSONArray("items");
        return null;
    }

    public void buildClanReport(String clanTag, String token) throws Exception {

        Logger.info("Clan Report requested");
        String[] columnHeaders = {"War, Rank", "Name", "Fame", "Participation"};
        String template = API_ENDPOINT + "v1/clans/%s/riverracelog";
        String url = String.format(template, clanTag.replace("#", "%23"));

        JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
        JSONArray items = response.getJSONArray("items");

        StringBuilder row = new StringBuilder();
        
        ArrayList<String> data = new ArrayList<>();

        for (int war = 0; war < items.length(); war++) {
            JSONArray standings = items.getJSONObject(war).getJSONArray("standings");
            
            for (int i = 0; i < standings.length(); i++) {

                JSONObject warResult = standings.getJSONObject(i);
                JSONObject clan = warResult.getJSONObject("clan");
                JSONArray participants = clan.getJSONArray("participants");
                String name = clan.getString("name");
                String tag = clan.getString("tag");
                int fame = clan.getInt("fame");
                int rank = warResult.getInt("rank");
                int participationCount = 0;

                if (tag.equalsIgnoreCase(clanTag)) {

                    row.append(war + 1);
                    row.append(",");
                    row.append(rank);
                    row.append(",");
                    row.append(name);
                    row.append(",");
                    row.append(fame);
                    row.append(",");
                    for (int participant = 0; participant < participants.length(); participant++) {
                        if (participants.getJSONObject(participant).getInt("decksUsed") > 0) {
                            participationCount++;
                        }
                    }
                    row.append(participationCount);
    
                    data.add(row.toString());
    
                    row = new StringBuilder();
                }
            }
        }
        
        IO_MODEL.writeCsv(data, columnHeaders, "Clan Report");

    }

    public void buildPlayerReport() throws Exception {

        String[] columnHeaders = {"Tag", "Name", "Participation"};



    }

    public void buildPDKReport() throws Exception {

        String[] columnHeaders = {"Playertag", "Name", "Last seen", "Inactive days"};

        

    }
}
