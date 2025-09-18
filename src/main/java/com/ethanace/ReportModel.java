package com.ethanace;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    // Scheduler to enforce rate limits (one call every 120ms ~ 8/sec)
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(4);

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

    public JSONArray getRiverRaceLog(String clanTag, String token, int limit) throws Exception {

        String template;
        String url;

        if (limit > 0) {
            template = API_ENDPOINT + "v1/clans/%s/riverracelog?limit=%d";
            url = String.format(template, clanTag.replace("#", "%23"), limit);
        } else {
            template = API_ENDPOINT + "v1/clans/%s/riverracelog";
            url = String.format(template, clanTag.replace("#", "%23"));
        }

        JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
        JSONArray items = response.getJSONArray("items");
        return items;
    }

    public Task<TableData> getClanReport(String clanTag, String token) throws Exception {

        JSONArray items = getRiverRaceLog(clanTag, token, 0);

        return new Task<TableData>() {
            @Override
            protected TableData call() throws Exception {

                Logger.info("Clan Report requested");

                List<String> columnHeaders = List.of("War", "Rank", "Name", "Fame", "Participation");

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

    public Task<TableData> getWeeklyReport(String clanTag, String token) throws Exception {

        int limit = 1;

        JSONArray items = getRiverRaceLog(clanTag, token, limit);
        JSONArray clanM = getClanMembers(clanTag, token);

        return new Task<TableData>() {
            @Override
            protected TableData call() throws Exception {

                Logger.info("Weekly Report requested");

                List<String> columnHeaders = List.of("Achievement", "Value", "Player Name", "Player Tag");

                ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
                ObservableList<Object> row = FXCollections.observableArrayList();

                // Step 1: Get highest rated players
                int highestFame = 0;
                String highestPlayerTag = "";
                String highestPlayerName = "";
                float highestAvgFame = 0;
                String highestAvgFamePlayerTag = "";
                String highestAvgFameName = "";
                int highestDonations = 0;
                String highestDonationsPlayerTag = "";
                String highestDonationsPlayerName = "";

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
                                    int fame = participants.getJSONObject(participant).getInt("fame");
                                    String name = participants.getJSONObject(participant).getString("name");
                                    int decksUsed = participants.getJSONObject(participant).getInt("decksUsed");
                                    float avgFame = Math.round((fame / decksUsed) * 100f) / 100f;

                                    if (fame > highestFame) {
                                        highestFame = fame;
                                        highestPlayerTag = playertag;
                                        highestPlayerName = name;
                                    }

                                    if (avgFame > highestAvgFame) {
                                        highestAvgFame = avgFame;
                                        highestAvgFamePlayerTag = playertag;
                                        highestAvgFameName = name;
                                    }
                                    
                                }
                            }
                        }
                    }
                }

                String template = API_ENDPOINT + "v1/clans/%s/";
                String url = String.format(template, clanTag.replace("#", "%23"));
                JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
                JSONArray memberList = response.getJSONArray("memberList");

                // Calculate total iterations
                int totalIterations = memberList.length();
                int completedIterations = 0;

                if (totalIterations > 0) {
                    for (int i = 0; i < memberList.length(); i++) {
                        JSONObject member = memberList.getJSONObject(i);
                        int donations = member.getInt("donations");

                        if (donations > highestDonations) {
                            highestDonations = donations;
                            highestDonationsPlayerTag = member.getString("tag");
                            highestDonationsPlayerName = member.getString("name");
                        }

                        completedIterations++;
                        updateProgress(completedIterations, totalIterations);
                        Thread.sleep(3);
                    }
                } else {
                    updateProgress(1, 1);
                }

                row.add("Highest Fame");
                row.add(highestFame);
                row.add(highestPlayerTag);
                row.add(highestPlayerName);
                data.add(row);

                row = FXCollections.observableArrayList();

                row.add("Highest Average Fame");
                row.add(highestAvgFame);
                row.add(highestAvgFameName);
                row.add(highestAvgFamePlayerTag);
                data.add(row);

                row = FXCollections.observableArrayList();

                row.add("Highest Donations");
                row.add(highestDonations);
                row.add(highestDonationsPlayerName);
                row.add(highestDonationsPlayerTag);
                data.add(row);

                Logger.info("Player Report completed");
                updateProgress(1, 1);
                return new TableData(columnHeaders, data);
            }
        };
    }


    public Task<TableData> getPlayerReport(String clanTag, String token) throws Exception {

        JSONArray items = getRiverRaceLog(clanTag, token, 0);
        JSONArray clanM = getClanMembers(clanTag, token);

        return new Task<TableData>() {
            @Override
            protected TableData call() throws Exception {

                Logger.info("Player Report requested");

                List<String> columnHeaders = List.of("Player Tag", "Name", "Participations", "Participation %");

                HashMap<String, String> clanMembers = new HashMap<>();

                for (int member = 0; member < clanM.length(); member++) {
                    String tag = clanM.getJSONObject(member).getString("tag");
                    String name = clanM.getJSONObject(member).getString("name");
                    clanMembers.put(tag, name);
                }

                ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

                // Collect player participations
                HashMap<String, Integer> players = new HashMap<>();
                int totalIterations = 0;

                for (int war = 0; war < items.length(); war++) {
                    JSONArray standings = items.getJSONObject(war).getJSONArray("standings");
                    totalIterations += standings.length();

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
                    }
                }

                // Fetch missing players in parallel with scheduled executor (throttled)
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                AtomicInteger completed = new AtomicInteger(0);
                int delayIncrement = 120; // ms per request ~ 8/sec
                int counter = 0;

                for (Map.Entry<String, Integer> entry : players.entrySet()) {
                    String playerTag = entry.getKey();
                    int participations = entry.getValue();
                    int delay = counter * delayIncrement;
                    counter++;

                    CompletableFuture<Void> future = new CompletableFuture<>();
                    scheduler.schedule(() -> {
                        try {
                            String playerName;
                            if (clanMembers.containsKey(playerTag)) {
                                playerName = clanMembers.get(playerTag);
                            } else {
                                Logger.info("Fetching missing player " + playerTag);
                                JSONObject missingPlayer = getPlayer(playerTag, token);
                                playerName = missingPlayer.getString("name");
                            }

                            ObservableList<Object> row = FXCollections.observableArrayList();
                            row.add(playerTag);
                            row.add(playerName);
                            row.add(participations);
                            row.add((int) Math.round(participations / (float) items.length() * 100));
                            synchronized (data) {
                                data.add(row);
                            }

                            int done = completed.incrementAndGet();
                            updateProgress(done, players.size());

                            future.complete(null);
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                            Logger.error(e);
                        }
                    }, delay, TimeUnit.MILLISECONDS);

                    futures.add(future);
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                Logger.info("Player Report completed");
                updateProgress(1, 1);
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

    public JSONObject getPlayer(String tag, String token) throws Exception {
        String template = API_ENDPOINT + "v1/players/%s";
        String url = String.format(template, tag.replace("#", "%23"));
        JSONObject response = new JSONObject(NET_MODEL.HTTPGet(url, token));
        return response;
    }
}