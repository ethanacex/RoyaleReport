package com.ethanace;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;

/**
 * @author ethanace
 */

public class ReportModel {
    
    private final NetModel NET_MODEL;
    private final ProgressBar PROGRESS_BAR;
    private final String API_ENDPOINT = "https://api.clashroyale.com/";

    public ReportModel(NetModel netModel, ProgressBar progressBar) throws IOException {
        NET_MODEL = netModel;
        PROGRESS_BAR = progressBar;
        Logger.info("ReportModel initialised successfully");
    }

    public JSONObject getRiverRaceLog(String clanTag, String token) throws Exception {
        String template = API_ENDPOINT + "v1/clans/%s/riverracelog";
        String url = String.format(template, clanTag.replace("#", "%23"));
        
        JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
        JSONArray items = response.getJSONArray("items");
        return null;
    }


    public TableData getClanReport(String clanTag, String token) throws Exception {

        Logger.info("Clan Report requested");
        List<String> columnHeaders = List.of("War", "Rank", "Name", "Fame", "Participation");
        String template = API_ENDPOINT + "v1/clans/%s/riverracelog";
        String url = String.format(template, clanTag.replace("#", "%23"));

        JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
        JSONArray items = response.getJSONArray("items");

        ObservableList<Object> row = FXCollections.observableArrayList();
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

        for (int war = 0; war < items.length(); war++) {
            PROGRESS_BAR.setProgress((double) war / items.length());
            Logger.info("Progress: " + PROGRESS_BAR.getProgress());
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

                    row.add(war + 1);
                    row.add(rank);
                    row.add(name);
                    row.add(fame);

                    for (int participant = 0; participant < participants.length(); participant++) {
                        if (participants.getJSONObject(participant).getInt("decksUsed") > 0) {
                            participationCount++;
                        }
                    }

                    row.add(participationCount);

                    // Add final completed row of data to the observable list of rows
                    data.add(FXCollections.observableArrayList(row));
    
                    row = FXCollections.observableArrayList();
                }
            }
        }

        return new TableData(columnHeaders, data);
    }

    public void buildPlayerReport() throws Exception {

        String[] columnHeaders = {"Tag", "Name", "Participation"};



    }

    public void buildPDKReport() throws Exception {

        String[] columnHeaders = {"Playertag", "Name", "Last seen", "Inactive days"};

        

    }
}