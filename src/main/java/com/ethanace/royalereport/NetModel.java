package com.ethanace.royalereport;

/**
 * @author ethanace
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class NetModel {

    @SuppressWarnings("deprecation")
    public String getPublicIPAddress() {

        String publicIP = "Undetermined";

        try {

            URL url = new URL("http://checkip.amazonaws.com/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            publicIP = in.readLine();
            in.close();

            //TODO: Alert
        } catch (MalformedURLException e) {
            System.err.println("The URL is malformed: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("The host could not be determined: " + e.getMessage());
        } catch (SocketTimeoutException e) {
            System.err.println("Connection timed out: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("An I/O error occurred: " + e.getMessage());
        }
        return publicIP;
    }

    @SuppressWarnings("deprecation")
    public String HTTPGet(String url, String token) {
        try {

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + token);
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                return response.toString();
            } else {
                return "";
            }
        } catch (MalformedURLException e) {
            // TODO: Alert MalformedURLException
        } catch (ProtocolException e) {
            // TODO: AlertProtocolException
        } catch (IOException e) {
            // TODO: AlertIOException
        } catch (SecurityException e) {
            // TODO: AlertSecurityException
        } catch (Exception e) {
            // TODO: Alert general Exception
        }
        return "";
    }

}
