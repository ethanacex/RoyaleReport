package main.client;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class Model {

    private Web web;
    private Database storage = new Database();

    private final String USER_PATH = System.getProperty("user.home") + File.separator + "Desktop"
            + File.separator + "RoyaleReport" + File.separator;

    public void downloadReport(String tag, String reportType) {
        try {
            if (isValid(tag)) {
                switch(reportType) {
                    case "War Readiness" : buildPlayerCardReport(tag); break;
                    case "War Performance" : buildPerformanceReport(tag); break;
                    case "PDK Report" : buildPromotionReport(tag); break;
                    default: break;
                }
            } else {
                Prompt.error("You must specify a clan").showAndWait();
            }
        } catch (UnirestException ex) {
            Prompt.error("Could not fetch information from server.").showAndWait();
        } catch (Exception ex) {
            Prompt.error("Please check input, internet connection and authorization.").showAndWait();
        }
    }

    private void buildPerformanceReport(String tag) throws UnirestException {

        // Column headers //
        String[] columns = {"Tag", "Name", "Collection Battles", "Cards Collected", "Battle Count",
                "Battles Played", "Wins", "Losses", "Missed Finals", "Win Ratio"};

        // Breaking down warlog into individual components //
        JSONArray items = web.getWarLog(tag).getJSONArray("items");
        JSONObject[] wars = new JSONObject[items.length()];
        JSONArray[] participantLists = new JSONArray[items.length()];

        // Local class to store related participant attributes //
        class Participant {

            private String tag;
            private String name;
            private int collectionBattles = 0;
            private int cardsEarned = 0;
            private int warBattles = 0;
            private int warBattlesPlayed = 0;
            private int wins = 0;

            private Participant(String tag, String name) {
                this.tag = tag;
                this.name = name;
            }

        }

        // HashMap will store name as key and Participant as value //
        HashMap<String, Participant> playerData = new HashMap<>();

        for (int i = 0; i < items.length(); i++) {
            wars[i] = items.getJSONObject(i);
            participantLists[i] = wars[i].getJSONArray("participants");
        }

        for (int i = 0; i < wars.length; i++) {
            JSONArray participants = participantLists[i];
            for (int k = 0; k < participants.length(); k++) {
                String playerName = participants.getJSONObject(k).getString("name");
                String playerTag = participants.getJSONObject(k).getString("tag");
                if (!playerData.containsKey(playerName)) {
                    playerData.put(playerName, new Participant(playerTag, playerName));
                }
                Participant participantStats = playerData.get(playerName);
                JSONObject participant = participants.getJSONObject(k);
                participantStats.collectionBattles += participant.getInt("collectionDayBattlesPlayed");
                participantStats.cardsEarned += participant.getInt("cardsEarned");
                participantStats.warBattles += participant.getInt("numberOfBattles");
                participantStats.warBattlesPlayed += participant.getInt("battlesPlayed");
                participantStats.wins += participant.getInt("wins");
            }
        }


        try {
            // Build or overwrite existing file //
            FileWriter csvReport = csvBuilder("warPerformanceReport.csv", columns);

            for (Participant p : playerData.values()) {

                csvReport.append(String.join(",", p.tag, p.name,
                        Integer.toString(p.collectionBattles),
                        Integer.toString(p.cardsEarned),
                        Integer.toString(p.warBattles),
                        Integer.toString(p.warBattlesPlayed),
                        Integer.toString(p.wins),
                        Integer.toString(p.warBattlesPlayed - p.wins),
                        Integer.toString(p.warBattles - p.warBattlesPlayed),
                        Math.round(((float) (p.wins) / (p.warBattlesPlayed)) * 100) + "%"

                ));
                csvReport.append("\n");
            }
            csvReport.flush();
            csvReport.close();
            Prompt.success("Operation completed successfully").showAndWait();
            locateFile();
        } catch (IOException e) {
            Prompt.error("Write failure, file may be in use.").showAndWait();
        }
    }

    private void buildPlayerCardReport(String tag) throws UnirestException {

        // Column headers //
        String[] columns = {"Tag", "Name", "Role", "Maxed", "Legendary", "Gold", "Silver", "Bronze", "Low",
                "Total", "% Legendary", "% Gold", "% Silver", "% Bronze", "% Low"};

        try {
            // Build or overwrite existing file //
            FileWriter csvReport = csvBuilder("cardReport.csv", columns);

            // List of clan members //
            JSONArray members = web.getClan(tag).getJSONArray("memberList");

            // Append unique data to CSV file //
            assert members != null;

            for (int i = 0; i < members.length(); i++) {
                int[] playerCards = getPlayerCardLevels(members.getJSONObject(i).getString("tag"));
                int cardsAtMax = playerCards[0];
                int cardsAtLegendary = playerCards[1];
                int cardsAtGold = playerCards[2];
                int cardsAtSilver = playerCards[3];
                int cardsAtBronze = playerCards[4];
                int cardsAtLow = playerCards[5];
                int totalCards = 0;
                // count starts from 1 to prevent including maxed cards, thus duplicating some
                for (int count = 1; count < playerCards.length; count++) {
                    totalCards += playerCards[count];
                }
                csvReport.append(String.join(",",
                        members.getJSONObject(i).getString("tag"),
                        members.getJSONObject(i).getString("name"),
                        members.getJSONObject(i).getString("role"),
                        Integer.toString(cardsAtMax),
                        Integer.toString(cardsAtLegendary),
                        Integer.toString(cardsAtGold),
                        Integer.toString(cardsAtSilver),
                        Integer.toString(cardsAtBronze),
                        Integer.toString(cardsAtLow),
                        Integer.toString(totalCards),
                        Math.round(((float) (cardsAtLegendary) / (totalCards)) * 100) + "%",
                        Math.round(((float) (cardsAtGold) / (totalCards)) * 100) + "%",
                        Math.round(((float) (cardsAtSilver) / (totalCards)) * 100) + "%",
                        Math.round(((float) (cardsAtBronze) / (totalCards)) * 100) + "%",
                        Math.round(((float) (cardsAtLow) / (totalCards)) * 100) + "%"
                ));
                csvReport.append("\n");
            }
            csvReport.flush();
            csvReport.close();
            Prompt.success("Report generated successfully").showAndWait();
            locateFile();
        } catch (IOException e) {
            Prompt.error("Write failure, file may be in use.").showAndWait();
        }
    }

    private void buildPromotionReport(String tag) throws UnirestException {

        // Column headers //
        String[] columns = {"Tag", "Name", "Role", "King Level", "Trophies", "Personal Best", "Donations", "Received",
                "Ratio", "Total Donations", "War Wins", "War Participations", "Participation Rate"};

        DecimalFormat df = new DecimalFormat("##.00");

        try {
            // Build or overwrite existing file //
            FileWriter csvReport = csvBuilder("pdkReport.csv", columns);

            // List of clan members //

            JSONArray members = web.getClan(tag).getJSONArray("memberList");

            // Append unique data to CSV file //
            assert members != null;

            for (int i = 0; i < members.length(); i++) {

                JSONObject clanMember = members.getJSONObject(i);
                JSONObject player = web.getPlayer(clanMember.getString("tag"));

                String playerTag = player.getString("tag");
                String playerName = player.getString("name");
                String playerRole = player.getString("role");

                int playerLevel = player.getInt("expLevel");
                int playerTrophies = player.getInt("trophies");
                int playerBestTrophies = player.getInt("bestTrophies");
                int playerDonations = player.getInt("donations");
                int playerDonationsReceived = player.getInt("donationsReceived");
                int playerTotalDonations = player.getInt("totalDonations");
                int playerWarDayWins = player.getInt("warDayWins");

                String ratio;

                if (playerDonations > 0 && playerDonationsReceived > 0) {
                    ratio = df.format((float) playerDonations / playerDonationsReceived);
                } else if (playerDonations > 0 && playerDonationsReceived == 0) {
                    ratio = Integer.toString(playerDonations);
                } else {
                    ratio = "0";
                }

                int playerParticipation = getWarParticipation(playerTag, web.getWarLog(tag).getJSONArray("items"));
                String participationRate = Math.round(((float) playerParticipation / 10) * 100) + "%";

                csvReport.append(String.join(",",
                        playerTag,
                        playerName,
                        playerRole,
                        Integer.toString(playerLevel),
                        Integer.toString(playerTrophies),
                        Integer.toString(playerBestTrophies),
                        Integer.toString(playerDonations),
                        Integer.toString(playerDonationsReceived),
                        ratio,
                        Integer.toString(playerTotalDonations),
                        Integer.toString(playerWarDayWins),
                        Integer.toString(playerParticipation),
                        participationRate
                ));
                csvReport.append("\n");
            }
            csvReport.flush();
            csvReport.close();
            Prompt.success("Report generated successfully").showAndWait();
            locateFile();
        } catch (IOException e) {
            Prompt.error("Write failure, file may be in use.").showAndWait();
        }
    }

    private int getWarParticipation(String playerTag, JSONArray items) {

        JSONObject[] wars = new JSONObject[items.length()];
        JSONArray[] participantLists = new JSONArray[items.length()];

        int counter = 0;

        for (int i = 0; i < items.length(); i++) {
            wars[i] = items.getJSONObject(i);
            participantLists[i] = wars[i].getJSONArray("participants");
            for (int k = 0; k < participantLists[i].length(); k++) {
                if (participantLists[i].getJSONObject(k).getString("tag").equalsIgnoreCase(playerTag)) {
                    counter++;
                }
            }
        }
        return counter;
    }

    private int[] getPlayerCardLevels(String playerTag) throws UnirestException {
        int maxed = 0, legendary = 0, gold = 0, silver = 0, bronze = 0, low = 0;
        JSONObject player = web.getPlayer(playerTag);
        JSONArray cards = player.getJSONArray("cards");
        for (int i = 0; i < cards.length(); i++) {
            int cardLevel = cards.getJSONObject(i).getInt("level")
                    + 13 - cards.getJSONObject(i).getInt("maxLevel");
            switch (cardLevel) {
                case 13: legendary++; maxed++; break;
                case 12: legendary++; break;
                case 11: gold++; break;
                case 10: silver++; break;
                case 9: bronze++; break;
                default: low++; break;
            }
        }
        return new int[] {maxed, legendary, gold, silver, bronze, low};
    }

    private FileWriter csvBuilder(String fileName, String[] columns) throws IOException {
        createDirectory();
        File file = new File(USER_PATH, fileName);
        FileWriter csv = new FileWriter(file);
        for (String header : columns) {
            csv.append(header);
            csv.append(",");
        }
        csv.append("\n");
        return csv;
    }

    private void locateFile() {
        try {
            if (isWindows()) {
                Desktop.getDesktop().open(new File(USER_PATH));
            } else if (isMac()) {
                Runtime.getRuntime().exec("open " + USER_PATH);
            }
        } catch (IOException e) {
            Prompt.error("Could not locate output file").showAndWait();
        }
    }

    public List<String> getFavourites() {
        return storage.getFavourites();
    }

    public String getSavedIP() {
        return storage.getIp();
    }

    public String getSavedToken() {
        return storage.getToken();
    }

    public String getPublicIP() {
        try {
            URL url = new URL("http://checkip.amazonaws.com");
            BufferedReader ip = new BufferedReader(new InputStreamReader(url.openStream()));
            return ip.readLine().trim();
        } catch (IOException e) {
            Prompt.error("Public IP address not found. Please check your internet connection").showAndWait();
        }
        return "Unknown";
    }

    private void createDirectory() {
        try {
            new File(USER_PATH).mkdirs();
        } catch (SecurityException e) {
            Prompt.error("Failed to create directory, please check write permissions.").showAndWait();
        }
    }

    public void saveUserData(String ip, String token, List<String> favourites) {
        if (isValid(ip) && isValid(token)) {
            storage.setIp(ip);
            storage.setToken(token);
            storage.setFavourites(favourites);
            saveDatabase();
            web = new Web(storage);
        } else {
            Prompt.error("Authorization fields cannot be empty").showAndWait();
        }
    }

    private void saveDatabase() {
        createDirectory();
        try {
            FileOutputStream outputStream = new FileOutputStream(USER_PATH + "save.cdb");
            ObjectOutputStream serializer = new ObjectOutputStream(outputStream);
            serializer.writeObject(storage);
            serializer.close();
            Prompt.success("Credentials saved.").showAndWait();
        } catch (Exception e) {
            Prompt.error("An error occurred, file may be in use or you may not have write permissions.").showAndWait();
        }
    }

    public void loadDatabase() {
        try {
            FileInputStream inputStream = new FileInputStream(USER_PATH + "save.cdb");
            ObjectInputStream serializer = new ObjectInputStream(inputStream);
            Object object = serializer.readObject();
            serializer.close();
            storage = (Database) object;
            Prompt.success("Save data loaded successfully").showAndWait();
        } catch (Exception e) {
            Prompt.warning("No previous save data found.").showAndWait();
        }
        if (storage.getFavourites() == null) {
            storage.setFavourites(new ArrayList<>());
        }
        web = new Web(storage);
    }

    private boolean isValid(String text) {
        return !text.isEmpty();
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("win"));
    }

    private boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("mac"));
    }

}