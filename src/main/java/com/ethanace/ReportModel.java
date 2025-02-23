package com.ethanace;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 * @author ethanace
 */
public class ReportModel {

    private final NetModel NET_MODEL;
    private final String API_ENDPOINT = "https://api.clashroyale.com/";

    public ReportModel(NetModel netModel) throws IOException {
        NET_MODEL = netModel;
        Logger.info("ReportModel initialised successfully");
    }

    public String formatDate(String inputDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime dateTime = LocalDateTime.parse(inputDate, inputFormatter);

        return dateTime.format(outputFormatter);
    }

    public int daysSince(String inputDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime lastSeen = LocalDateTime.parse(inputDate, inputFormatter);
        LocalDateTime today = LocalDateTime.now();
        return (int) java.time.temporal.ChronoUnit.DAYS.between(lastSeen, today);
    }

    public JSONObject getRiverRaceLog(String clanTag, String token) throws Exception {
        String template = API_ENDPOINT + "v1/clans/%s/riverracelog";
        String url = String.format(template, clanTag.replace("#", "%23"));

        JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
        JSONArray items = response.getJSONArray("items");
        return null;
    }

    public Task<TableData> getClanReport(String clanTag, String token) throws Exception {

        return new Task<TableData>() {
            @Override
            protected TableData call() throws Exception {

                Logger.info("Clan Report requested");

                String template = API_ENDPOINT + "v1/clans/%s/riverracelog";
                String url = String.format(template, clanTag.replace("#", "%23"));

                List<String> columnHeaders = List.of("War", "Rank", "Name", "Fame", "Participation");

                JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
                JSONArray items = response.getJSONArray("items");

                ObservableList<Object> row = FXCollections.observableArrayList();
                ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

                // First pass: Calculate total iterations
                int totalIterations = 0;

                for (int war = 0; war < items.length(); war++) {
                    JSONArray standings = items.getJSONObject(war).getJSONArray("standings");
                    totalIterations += standings.length();
                }

                // Second pass: Process data and update progress
                if (totalIterations > 0) {
                    int completedIterations = 0;

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
                                data.add(FXCollections.observableArrayList(row));
                                row = FXCollections.observableArrayList();
                            }

                            completedIterations++;
                            updateProgress(completedIterations, totalIterations);
                            Thread.sleep(3);
                        }
                    }
                } else {
                    updateProgress(1, 1);
                }

                Logger.info("Clan Report completed");
                return new TableData(columnHeaders, data);
            }
        };

    }

    public Task<TableData> getPlayerReport(String clanTag, String token) throws Exception {

        return new Task<TableData>() {
            @Override
            protected TableData call() throws Exception {
    
                Logger.info("Player Report requested");
    
                String template = API_ENDPOINT + "v1/clans/%s/riverracelog";
                String url = String.format(template, clanTag.replace("#", "%23"));
    
                List<String> columnHeaders = List.of("Player Tag", "Name", "Participations", "Participation %");
    
                JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
                JSONArray items = response.getJSONArray("items");
    
                JSONArray clanM = getClanMembers(clanTag, token);
                HashMap<String, String> clanMembers = new HashMap<>();
    
                for (int member = 0; member < clanM.length(); member++) {
                    String tag = clanM.getJSONObject(member).getString("tag");
                    String name = clanM.getJSONObject(member).getString("name");
                    clanMembers.put(tag, name);
                }
    
                ObservableList<Object> row = FXCollections.observableArrayList();
                ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
    
                // First pass: Calculate total iterations
                int totalIterations = 0;
    
                for (int war = 0; war < items.length(); war++) {
                    JSONArray standings = items.getJSONObject(war).getJSONArray("standings");
                    totalIterations += standings.length();
                }
    
                totalIterations += clanM.length();
                HashMap<String, Integer> players = new HashMap<>();
    
                // Second pass: Process data and update progress
                if (totalIterations > 0) {
                    int completedIterations = 0;
    
                    // First phase: Process the standings and count player participations
                    for (int war = 0; war < items.length(); war++) {
                        JSONArray standings = items.getJSONObject(war).getJSONArray("standings");
    
                        for (int i = 0; i < standings.length(); i++) {
                            JSONObject warResult = standings.getJSONObject(i);
                            JSONObject clan = warResult.getJSONObject("clan");
                            JSONArray participants = clan.getJSONArray("participants");
                            String tag = clan.getString("tag");
    
                            if (tag.equalsIgnoreCase(clanTag)) {
    
                                for (int participant = 0; participant < participants.length(); participant++) {
                                    if (participants.getJSONObject(participant).getInt("decksUsed") > 0) {
                                        String playertag = participants.getJSONObject(participant).getString("tag");
                                        players.put(playertag, players.getOrDefault(playertag, 0) + 1);
                                    }
                                }
                            }
    
                            completedIterations++;
                            updateProgress(completedIterations, totalIterations);  // Update progress during collection of data
                            Thread.sleep(5);  // Optional: to simulate processing delay
                        }
                    }
    
                    // Second phase: Fetch missing players and update progress
                    int totalMissingPlayers = 0;
                    for (Map.Entry<String, Integer> entry : players.entrySet()) {
                        if (clanMembers.get(entry.getKey()) == null) {
                            totalMissingPlayers++;
                        }
                    }
    
                    int completedMissingPlayerFetch = 0;
    
                    // Fetch missing players
                    for (Map.Entry<String, Integer> entry : players.entrySet()) {
                        row.add(entry.getKey());
                        if (clanMembers.get(entry.getKey()) == null) {
                            Logger.info("Player Tag: " + entry.getKey() + " not found in clan members list");
                            JSONObject missingPlayer = getClanMember(entry.getKey(), token);
                            row.add(missingPlayer.getString("name"));
                        } else {
                            row.add(clanMembers.get(entry.getKey()));
                        }
                        row.add(entry.getValue());
                        row.add((int) Math.round(entry.getValue() / (float) items.length() * 100));
                        data.add(FXCollections.observableArrayList(row));
                        row = FXCollections.observableArrayList();
    
                        completedMissingPlayerFetch++;
                        updateProgress(completedMissingPlayerFetch + completedIterations, totalIterations + totalMissingPlayers);
                    }
    
                }
                updateProgress(1, 1);
                Logger.info("Player Report completed");
                return new TableData(columnHeaders, data);
            }
        };
    
    }

    public Task<TableData> getPlayerActivityReport(String clanTag, String token) throws Exception {

        return new Task<TableData>() {
            @Override
            protected TableData call() throws Exception {

                Logger.info("Player Activity Report requested");

                String template = API_ENDPOINT + "v1/clans/%s/";
                String url = String.format(template, clanTag.replace("#", "%23"));

                List<String> columnHeaders = List.of("Player Tag", "Name", "Donations","Last Seen", "Days Inactive");

                JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
                JSONArray memberList = response.getJSONArray("memberList");

                ObservableList<Object> row = FXCollections.observableArrayList();
                ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

                // Calculate total iterations
                int totalIterations = memberList.length();
                int completedIterations = 0;

                if (totalIterations > 0) {
                    for (int i = 0; i < memberList.length(); i++) {
                        JSONObject member = memberList.getJSONObject(i);
                        String lastSeen = formatDate(member.getString("lastSeen"));
                        row.add(member.getString("tag"));
                        row.add(member.getString("name"));
                        row.add(member.getInt("donations"));
                        row.add(lastSeen);
                        row.add(daysSince(lastSeen));
                        data.add(FXCollections.observableArrayList(row));
                        row = FXCollections.observableArrayList();
                        completedIterations++;
                        updateProgress(completedIterations, totalIterations);
                        Thread.sleep(3);
                    }
                } else {
                    updateProgress(1, 1);
                }

                Logger.info("Player Activity Report completed");
                return new TableData(columnHeaders, data);
                
            }
        };

    }

    public JSONArray getClanMembers(String clanTag, String token) throws Exception {
        String template = API_ENDPOINT + "v1/clans/%s/";
        String url = String.format(template, clanTag.replace("#", "%23"));
        JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
        JSONArray memberList = response.getJSONArray("memberList");
        return memberList;
    }

    public JSONObject getClanMember(String tag, String token) throws Exception {
        String template = API_ENDPOINT + "v1/players/%s";
        String url = String.format(template, tag.replace("#", "%23"), tag.replace("#", "%23"));
        JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
        return response;
    }
}
