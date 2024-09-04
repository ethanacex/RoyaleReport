package com.ethanace.royalereport;

/**
 * @author ethanace
 */
import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NetModel {

    public String getPublicIPAddress() throws Exception {
        String publicIP = "Undetermined";
        try {
            URI uri = new URI("http://checkip.amazonaws.com/");
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
            if (response.statusCode() == 200) {
                publicIP = response.body();
            } else {
                throw new Exception("Failed to get IP address: HTTP response code " + response.statusCode());
            }
        } catch (Exception e) {
            throw new Exception("An error occurred while getting the public IP address: ", e);
        }
        return publicIP;
    }
    
    public String HTTPGet(String url, String token) throws Exception {
        try {
            URI uri = new URI(url);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new Exception("Failed to get data: HTTP response code " + response.statusCode());
            }
        } catch (Exception e) {
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
}
