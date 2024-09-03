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
    public String getPublicIPAddress() throws Exception {

        String publicIP = "Undetermined";

        try {

            URL url = new URL("http://checkip.amazonaws.com/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            publicIP = in.readLine();
            in.close();

        } catch (MalformedURLException e) {
            throw new Exception("The URL is malformed: ", e);
        } catch (UnknownHostException e) {
            throw new Exception("The host could not be determined: ", e);
        } catch (SocketTimeoutException e) {
            throw new Exception("Connection timed out: ", e);
        } catch (IOException e) {
            throw new Exception("An error occurred on connection InputStream: ", e);
        }
        return publicIP;
    }

    @SuppressWarnings("deprecation")
    public String HTTPGet(String url, String token) throws Exception {
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
            throw new Exception("HTTPGet URL is malformed", e);
        } catch (ProtocolException e) {
            throw new Exception("HTTPGet encountered protocol error", e);
        } catch (IOException e) {
            throw new Exception("Error when retrieving data from HTTP input stream", e);
        } catch (SecurityException e) {
            throw new Exception("A security error occurred during HTTPGet", e);
        } catch (Exception e) {
            throw new Exception("HTTPGet unknown error occurred", e);
        }
    }

}
