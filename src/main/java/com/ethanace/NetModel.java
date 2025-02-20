package com.ethanace;

/**
 * @author ethanace
 */
import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.tinylog.Logger;

public class NetModel {
    
    public String getPublicIP() throws Exception {
        String publicIP = HTTPGet("http://checkip.amazonaws.com/", null);
        Logger.info("Public IP resolved: " + publicIP);
        return publicIP;
    }

    public String HTTPGet(String url, String token) throws Exception {
        try {
            URI uri = new URI(url);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .header("User-Agent", "Mozilla/5.0")
                .GET();
            
            if (token != null) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }
            
            HttpRequest request = requestBuilder.build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                Logger.error("Failed to get data: HTTP response code " + response.statusCode());
                throw new Exception("Failed to get data: HTTP response code " + response.statusCode());
            }
        } catch (Exception e) {
            Logger.error("An error occurred during HTTP GET request");
            throw new Exception("An error occurred during HTTP GET request: ", e);
        }
    }

    public void openLink(String url) throws Exception {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                URI uri = new URI(url);
                desktop.browse(uri);
            } else {
                throw new Exception("Something went wrong, please visit Supercell's Developer page to get a new token");
            }
        } catch (Exception e) {
            throw new Exception("Something went wrong, please visit Supercell's Developer page to get a new token", e);
        }
    }

    public void openSupercellDevSite() throws Exception {
        openLink("https://developer.clashroyale.com");
    }
}
