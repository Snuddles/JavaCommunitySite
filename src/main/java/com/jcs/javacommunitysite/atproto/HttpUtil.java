package com.jcs.javacommunitysite.atproto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpUtil {
    public static JsonObject post(URL url, JsonObject payload, Map<String, String> headers) throws IOException, AtprotoUnauthorized {
        String payloadString = payload.toString();

        // Set up connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        // Attach payload
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(payloadString);
        wr.close();

        // Response sent - check status
        int statusCode = connection.getResponseCode();
        if (statusCode == 401) {
            throw new AtprotoUnauthorized();
        }

        // Get response
        InputStream is = connection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
            response.append('\n');
        }
        br.close();

        JsonElement responseElement = JsonParser.parseString(response.toString());
        return responseElement.getAsJsonObject();
    }
}
