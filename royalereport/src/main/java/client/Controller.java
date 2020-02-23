package client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

class Controller {

    //<editor-fold desc="Credentials">
    private String ip = "";
    private String token = "Bearer ";
    //</editor-fold>

    void setIp(String ip) {
        this.ip = ip;
    }

    void setToken(String token) {
        this.token = token;
    }

    void buttonHandler(String tag, String value) throws UnirestException {
        switch(value) {
            case "War Readiness" : buildPlayerCardReport(tag); break;
            case "War Performance" : buildPerformanceReport(tag); break;
            case "PDK Report" : buildPromotionReport(tag); break;
            default: break;
        }

    }

    private HttpResponse<String> requestFromServer(String group, String tag, String subGroup) throws UnirestException {
        tag = tag.replace("#","").trim().toUpperCase();
        if (!subGroup.isEmpty()) {
            subGroup = "/" + subGroup;
        }
        return Unirest.get("https://api.clashroyale.com/v1/{group}/%23{tag}{subGroup}?ip={address}")
                .routeParam("group", group)
                .routeParam("tag", tag)
                .routeParam("subGroup", subGroup)
                .routeParam("address", ip)
                .header("Authorization", token)
                .header("User-Agent", "PostmanRuntime/7.13.0")
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Postman-Token", "6a907d89-8fe0-475b-a405-704960e4035b,600fb73f-a729-472a-a920-a93a22c4e918")
                .header("Host", "api.clashroyale.com")
                .header("accept-encoding", "gzip, deflate")
                .header("Connection", "keep-alive")
                .header("cache-control", "no-cache")
                .asString();
    }

    private void buildPerformanceReport(String tag) throws UnirestException {

        // Column headers //
        String[] columns = {"Tag", "Name", "Collection Battles", "Cards Collected", "Battle Count",
                "Battles Played", "Wins", "Losses", "Missed Finals", "Win Ratio"};

        // Breaking down warlog into individual components //
        JSONArray items = getWarLog(tag).getJSONArray("items");
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
            FileWriter csvReport = fileBuilder("warPerformanceReport.csv", columns);

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
            AlertStatus.alertOperationSuccess().showAndWait();
            locateFile();
        } catch (IOException e) {
            AlertStatus.alertWriteError().showAndWait();
        }
    }

    private void buildPlayerCardReport(String tag) throws UnirestException {

        // Column headers //
        String[] columns = {"Tag", "Name", "Role", "Maxed", "Legendary", "Gold", "Silver", "Bronze", "Low",
                "Total", "% Legendary", "% Gold", "% Silver", "% Bronze", "% Low"};

        try {
            // Build or overwrite existing file //
            FileWriter csvReport = fileBuilder("cardReport.csv", columns);

            // List of clan members //
            JSONArray members = getClan(tag).getJSONArray("memberList");

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
            AlertStatus.alertOperationSuccess().showAndWait();
            locateFile();
        } catch (IOException e) {
            AlertStatus.alertWriteError().showAndWait();
        }
    }

    private void buildPromotionReport(String tag) throws UnirestException {

        // Column headers //
        String[] columns = {"Tag", "Name", "Role", "King Level", "Trophies", "Personal Best", "Donations", "Received",
                "Ratio", "Total Donations", "War Wins", "War Participations", "Participation Rate"};

        DecimalFormat df = new DecimalFormat("##.00");

        try {
            // Build or overwrite existing file //
            FileWriter csvReport = fileBuilder("pdkReport.csv", columns);

            // List of clan members //

            JSONArray members = getClan(tag).getJSONArray("memberList");

            // Append unique data to CSV file //
            assert members != null;

            for (int i = 0; i < members.length(); i++) {

                JSONObject clanMember = members.getJSONObject(i);
                JSONObject player = getPlayer(clanMember.getString("tag"));

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

                int playerParticipation = getWarParticipation(playerTag, getWarLog(tag).getJSONArray("items"));
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
            AlertStatus.alertOperationSuccess().showAndWait();
            locateFile();
        } catch (IOException e) {
            AlertStatus.alertWriteError().showAndWait();
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
        JSONObject player = getPlayer(playerTag);
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

    private FileWriter fileBuilder(String fileName, String[] columns) throws IOException {
        String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
        File file = new File(path, fileName);
        try {
            boolean dirCreated = file.getParentFile().mkdirs();
            if (!dirCreated) {
                AlertStatus.alertOverwriteWarning().showAndWait();
            }
        } catch (SecurityException e) {
            AlertStatus.alertDirectoryBuildError().showAndWait();
        }
        FileWriter csv = new FileWriter(file);
        for (String header : columns) {
            csv.append(header);
            csv.append(",");
        }
        csv.append("\n");
        return csv;
    }

    public boolean loadCredentials() {
        try {
            String path = System.getProperty("user.home") + File.separator + "Desktop"
                    + File.separator + "RoyaleReport" + File.separator + "credentials.pref";
            BufferedReader in = new BufferedReader(new FileReader(path));
            String[] lines = new String[2];
            int expectedTokens = 2;
            for (int i = 0; i < expectedTokens; i++) {
                lines[i] = in.readLine();
            }
            ip = lines[0];
            token = lines[1];
        }
        catch(IOException e) {
            AlertStatus.alertNotFoundError().showAndWait();
            return false;
        }
        AlertStatus.alertLoadSuccess().showAndWait();
        return true;
    }

    public void saveCredentials() {
        String path = System.getProperty("user.home") + File.separator + "Desktop"
                + File.separator + "RoyaleReport" + File.separator + "credentials.pref";
        File file = new File(path);
        try {
            boolean dirCreated = file.getParentFile().mkdirs();
            if (!dirCreated) {
                AlertStatus.alertOverwriteWarning().showAndWait();
            }
        } catch (SecurityException e) {
            AlertStatus.alertDirectoryBuildError().showAndWait();
            return;
        }
        List<String> lines = Arrays.asList(ip, token);
        Path save = Paths.get(path);
        try {
            Files.write(save, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            AlertStatus.alertWriteError().showAndWait();
            return;
        }
        AlertStatus.alertCredentialsSaved().showAndWait();
    }

    private JSONObject getClan(String clanTag) throws UnirestException {
        HttpResponse<String> response = requestFromServer("clans", clanTag, "");
        return new JSONObject(response.getBody());
    }

    private JSONObject getWarLog(String clanTag) throws UnirestException {
        HttpResponse<String> response = requestFromServer("clans", clanTag, "warlog");
        return new JSONObject(response.getBody());
    }

    private JSONObject getPlayer(String playerTag) throws UnirestException {
        HttpResponse<String> response = requestFromServer("players", playerTag, "");
        return new JSONObject(response.getBody());
    }

    private void locateFile() {
        if (isWindows()) {
            try {
                String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
                Desktop.getDesktop().open(new File(path));
            } catch (IOException e) {
                AlertStatus.alertOperationSuccess().showAndWait();
            }

        } else if (isMac()) {
            try {
                String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
                Runtime.getRuntime().exec("open " + path);
            } catch (IOException e) {
                AlertStatus.alertDirectoryBuildError().showAndWait();
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

    public boolean authenticate(TextField ipTextField, TextField authTextField) {
        if (ipTextField.getText().isEmpty() || authTextField.getText().isEmpty()) {
            AlertStatus.alertInputError().showAndWait();
            return false;
        } else {
            setIp(ipTextField.getText().trim());
            setToken("Bearer " + authTextField.getText().trim());
            return true;
        }
    }

    public boolean unlockAdmin(TextField txtField, ComboBox<String> dropDown) {
        boolean admin = false;
        if (!txtField.getText().isEmpty()) {
            if (txtField.getText().equals("admin")) {
                AlertStatus.alertAdminWarning().showAndWait();
                admin = true;
            } else {
                try {
                    buttonHandler(txtField.getText(), dropDown.getValue());
                } catch (UnirestException ex) {
                    AlertStatus.alertServerError().showAndWait();
                } catch (Exception ex) {
                    AlertStatus.alertFatalError().showAndWait();
                }
            }
        }
        else {
            AlertStatus.alertInputError().showAndWait();
        }
        return admin;
    }
}