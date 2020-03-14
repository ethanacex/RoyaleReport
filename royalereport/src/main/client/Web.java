package main.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

class Web {

    private String ip;
    private String token;

    public Web(Database db) {
        this.ip = db.getIp();
        this.token = "Bearer " + db.getToken();
    }

    protected HttpResponse<String> requestFromServer(String group, String tag, String subGroup) throws UnirestException {
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
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Host", "api.clashroyale.com")
                .header("accept-encoding", "gzip, deflate")
                .header("Connection", "keep-alive")
                .header("cache-control", "no-cache")
                .asString();
    }

    protected JSONObject getClan(String clanTag) throws UnirestException {
        HttpResponse<String> response = requestFromServer("clans", clanTag, "");
        return new JSONObject(response.getBody());
    }

    protected JSONObject getWarLog(String clanTag) throws UnirestException {
        HttpResponse<String> response = requestFromServer("clans", clanTag, "warlog");
        return new JSONObject(response.getBody());
    }

    protected JSONObject getPlayer(String playerTag) throws UnirestException {
        HttpResponse<String> response = requestFromServer("players", playerTag, "");
        return new JSONObject(response.getBody());
    }

}
