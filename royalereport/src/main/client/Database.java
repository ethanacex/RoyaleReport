package main.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Database implements Serializable {

    private String ip = "";
    private String token = "";
    private ArrayList<String> favourites;

    public String getIp() {
        return ip;
    }

    public String getToken() {
        return token;
    }

    public ArrayList<String> getFavourites() {
        return favourites;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setFavourites(List<String> favourites) {
        this.favourites = new ArrayList<>(favourites);
    }
}
