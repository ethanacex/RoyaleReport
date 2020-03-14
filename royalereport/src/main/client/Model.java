package main.client;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class Model {

    private Database storage = new Database();
    private final Web web = new Web(storage);
    private final String USER_PATH = System.getProperty("user.home") + File.separator + "Desktop"
            + File.separator + "RoyaleReport" + File.separator + "save.db";


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
                UserNotify.error("Text field must not be empty");
            }
        } catch (UnirestException e) {
            UserNotify.error("Could not fetch information from server.");
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
            FileWriter csvReport = dirBuilder("warPerformanceReport.csv", columns);

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
            UserNotify.alertOperationSuccess().showAndWait();
            locateFile();
        } catch (IOException e) {
            UserNotify.alertWriteError().showAndWait();
        }
    }

    private void buildPlayerCardReport(String tag) throws UnirestException {

        // Column headers //
        String[] columns = {"Tag", "Name", "Role", "Maxed", "Legendary", "Gold", "Silver", "Bronze", "Low",
                "Total", "% Legendary", "% Gold", "% Silver", "% Bronze", "% Low"};

        try {
            // Build or overwrite existing file //
            FileWriter csvReport = dirBuilder("cardReport.csv", columns);

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
            UserNotify.alertOperationSuccess().showAndWait();
            locateFile();
        } catch (IOException e) {
            UserNotify.alertWriteError().showAndWait();
        }
    }

    private void buildPromotionReport(String tag) throws UnirestException {

        // Column headers //
        String[] columns = {"Tag", "Name", "Role", "King Level", "Trophies", "Personal Best", "Donations", "Received",
                "Ratio", "Total Donations", "War Wins", "War Participations", "Participation Rate"};

        DecimalFormat df = new DecimalFormat("##.00");

        try {
            // Build or overwrite existing file //
            FileWriter csvReport = dirBuilder("pdkReport.csv", columns);

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
            UserNotify.alertOperationSuccess().showAndWait();
            locateFile();
        } catch (IOException e) {
            UserNotify.alertWriteError().showAndWait();
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

    private FileWriter dirBuilder(String fileName, String[] columns) throws IOException {
        String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
        File file = new File(path, fileName);
        try {
            boolean dirCreated = file.getParentFile().mkdirs();
            if (!dirCreated) {
                UserNotify.alertOverwriteWarning().showAndWait();
            }
        } catch (SecurityException e) {
            UserNotify.alertDirectoryBuildError().showAndWait();
        }
        FileWriter csv = new FileWriter(file);
        for (String header : columns) {
            csv.append(header);
            csv.append(",");
        }
        csv.append("\n");
        return csv;
    }

//    public boolean loadUserData() {
//        try {
//            BufferedReader in = new BufferedReader(new FileReader(USER_PATH));
//            ArrayList<String> lines = new ArrayList<>();
//            while (in.readLine() != null) {
//                lines.add(in.readLine());
//            }
//            storage.setIp(lines.get(0));
//            storage.setToken(lines.get(1));
//            for (int i = 2; i < lines.size(); i++) {
//                storage.setFavourite(lines.get(i));
//            }
//        }
//        catch(IOException e) {
//            UserNotify.alertNotFoundError().showAndWait();
//            return false;
//        }
//        UserNotify.alertLoadSuccess().showAndWait();
//        return true;
//    }
//
//    public void saveCredentials() {
//        File file = new File(USER_PATH);
//        try {
//            boolean dirCreated = file.getParentFile().mkdirs();
//            if (!dirCreated) {
//                UserNotify.alertOverwriteWarning().showAndWait();
//            }
//        } catch (SecurityException e) {
//            UserNotify.alertDirectoryBuildError().showAndWait();
//            return;
//        }
//        List<String> lines = Arrays.asList(storage.getIp(), storage.getToken());
//        Path save = Paths.get(USER_PATH);
//        try {
//            Files.write(save, lines, StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            UserNotify.alertWriteError().showAndWait();
//            return;
//        }
//        UserNotify.alertCredentialsSaved().showAndWait();
//    }

    public void saveDatabase() {

        try {
            FileOutputStream outputStream = new FileOutputStream(USER_PATH);
            ObjectOutputStream serializer = new ObjectOutputStream(outputStream);
            serializer.writeObject(storage);
            serializer.close();
        } catch (Exception e) {
            UserNotify.error("An error occurred, file may be in use or you may not have write permissions.");
        }

    }

    public void loadDatabase() {
        try {
            FileInputStream inputStream = new FileInputStream(USER_PATH);
            ObjectInputStream serializer = new ObjectInputStream(inputStream);

            Object object = serializer.readObject();
            serializer.close();
            storage = (Database) object;
        } catch (Exception e) {
            UserNotify.warning("No default credentials found.");
        }
    }

    private void locateFile() {
        if (isWindows()) {
            try {
                String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
                Desktop.getDesktop().open(new File(path));
            } catch (IOException e) {
                UserNotify.alertOperationSuccess().showAndWait();
            }

        } else if (isMac()) {
            try {
                String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
                Runtime.getRuntime().exec("open " + path);
            } catch (IOException e) {
                UserNotify.alertDirectoryBuildError().showAndWait();
            }

        }
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("win"));
    }

    private boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("mac"));
    }

    private boolean isValid(String text) {
        if (text.isEmpty()) {
            UserNotify.alertInputError().showAndWait();
            return false;
        }
        return true;
    }

    public List<String> getFavourites() {
        return storage.getFavourites();
    }

    public String getPublicIP() {
        try {
            URL url = new URL("http://checkip.amazonaws.com");
            BufferedReader ip = new BufferedReader(new InputStreamReader(url.openStream()));
            return ip.readLine().trim();
        } catch (IOException e) {
            UserNotify.error("Public IP address not found. Please check your internet connection");
        }
        return "Unknown";
    }

    public String getSavedIP() {
        return storage.getIp();
    }

    public String getSavedToken() {
        return storage.getToken();
    }

    public void saveUserData(String ip, String token, List<String> favourites) {
        if (isValid(ip) && isValid(token)) {
            storage.setIp(ip);
            storage.setToken("Bearer " + token);
            storage.setFavourites(favourites);
            saveDatabase();
            UserNotify.success("Credentials saved.").showAndWait();
        } else {
            UserNotify.error("Text field must not be empty").showAndWait();
        }
    }

}