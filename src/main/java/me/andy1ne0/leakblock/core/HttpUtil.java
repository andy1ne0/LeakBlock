package me.andy1ne0.leakblock.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utilities about the http protocol, currently featuring get requests
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
public final class HttpUtil {

    private HttpUtil() {
        throw new UnsupportedOperationException();
    }

    public static String doGetRequest(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
