package me.andy1ne0.leakblock.core;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.ToString;

import java.util.logging.Logger;

/**
 * Class for requesting and handling of IpApi data
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
public final class IpApi {

    private IpApi() {
        throw new UnsupportedOperationException();
    }

    private static final Gson gson = new Gson();

    public static IpApiResponse requestData(String ip) {
        String response = HttpUtil.doGetRequest("http://ip-api.com/json/" + ip);
        if (response == null) {
            return null;
        }
        return gson.fromJson(response, IpApiResponse.class);
    }

    public static boolean isFailAndLog(IpApiResponse res, Settings settings, Logger logger, String ip) {
        if (res.getStatus().equalsIgnoreCase("fail")) {
            logger.warning("The connection to ip-api returned an error while checking ip: " + ip);
            if (settings.isDebug()) {
                logger.warning("Dump: " + res);
            }
            return true;
        }
        return false;
    }

    public static boolean shouldBlock(IpApiResponse res) {
        return res.getIsp().equalsIgnoreCase("OVH SAS")
                && (res.getCountry().equalsIgnoreCase("France") || res.getCountry().equalsIgnoreCase("Italy"));
    }

    @Getter
    @ToString
    public static class IpApiResponse {

        private String status;
        private String message;
        private String query;
        private String country;
        private String countryCode;
        private String region;
        private String regionName;
        private String city;
        private String zip;
        private String lat;
        private String lon;
        private String timezone;
        private String isp;
        private String org;
        private String as;
    }
}
