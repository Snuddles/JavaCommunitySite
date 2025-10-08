package com.jcs.javacommunitysite.atproto;

import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import dev.mccue.json.Json;
import dev.mccue.json.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static dev.mccue.json.JsonDecoder.object;

public class HttpUtil {
    public static JsonObject post(URL url, Json payload, Map<String, String> headers) throws IOException, AtprotoUnauthorized {
        String payloadString = Json.write(payload);

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


        Json responseElement = Json.read(response.toString());
        return object(responseElement);
    }
}
