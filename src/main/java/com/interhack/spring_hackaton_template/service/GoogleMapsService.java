package com.interhack.spring_hackaton_template.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Slf4j
public class GoogleMapsService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://maps.googleapis.com")
            .build();

    @Data
    @AllArgsConstructor
    public static class DistanceResult {
        private double distanceKm;
        private int durationMinutes;
    }

    public DistanceResult getDistance(double originLat, double originLng,
                                     double destLat, double destLng) {
        if ("YOUR_API_KEY_HERE".equals(apiKey)) {
            return estimateWithHaversine(originLat, originLng, destLat, destLng);
        }

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/maps/api/distancematrix/json")
                            .queryParam("origins", originLat + "," + originLng)
                            .queryParam("destinations", destLat + "," + destLng)
                            .queryParam("key", apiKey)
                            .queryParam("mode", "driving")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Google Maps response: {}", response);
            return parseDistanceResponse(response);
        } catch (Exception e) {
            log.warn("Google Maps API failed, using haversine estimate: {}", e.getMessage());
            return estimateWithHaversine(originLat, originLng, destLat, destLng);
        }
    }

    private DistanceResult estimateWithHaversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double straightLine = R * c;

        double roadDistance = straightLine * 1.3;
        int minutes = (int) (roadDistance / 40.0 * 60);

        return new DistanceResult(Math.round(roadDistance * 100.0) / 100.0, Math.max(minutes, 5));
    }

    private DistanceResult parseDistanceResponse(String json) {
        try {
            if (json.contains("\"distance\"")) {
                int distIdx = json.indexOf("\"value\"", json.indexOf("\"distance\""));
                int distStart = json.indexOf(":", distIdx) + 1;
                int distEnd = json.indexOf("}", distStart);
                double meters = Double.parseDouble(json.substring(distStart, distEnd).trim());

                int durIdx = json.indexOf("\"value\"", json.indexOf("\"duration\""));
                int durStart = json.indexOf(":", durIdx) + 1;
                int durEnd = json.indexOf("}", durStart);
                double seconds = Double.parseDouble(json.substring(durStart, durEnd).trim());

                return new DistanceResult(
                        Math.round(meters / 10.0) / 100.0,
                        (int) Math.ceil(seconds / 60.0));
            }
        } catch (Exception e) {
            log.error("Failed to parse Google Maps response", e);
        }
        return new DistanceResult(0, 0);
    }

    @Data
    @AllArgsConstructor
    public static class GeocodingResult {
        private double lat;
        private double lng;
        private String formattedAddress;
    }

    public GeocodingResult geocode(String address) {
        if ("YOUR_API_KEY_HERE".equals(apiKey)) {
            log.warn("No Google Maps API key configured, geocoding unavailable");
            return null;
        }

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/maps/api/geocode/json")
                            .queryParam("address", address)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseGeocodingResponse(response);
        } catch (Exception e) {
            log.error("Geocoding failed for address: {}", address, e);
            return null;
        }
    }

    private GeocodingResult parseGeocodingResponse(String json) {
        try {
            if (json.contains("\"location\"")) {
                int locIdx = json.indexOf("\"location\"");
                int latIdx = json.indexOf("\"lat\"", locIdx);
                int latStart = json.indexOf(":", latIdx) + 1;
                int latEnd = json.indexOf(",", latStart);
                double lat = Double.parseDouble(json.substring(latStart, latEnd).trim());

                int lngIdx = json.indexOf("\"lng\"", locIdx);
                int lngStart = json.indexOf(":", lngIdx) + 1;
                int lngEnd = json.indexOf("}", lngStart);
                double lng = Double.parseDouble(json.substring(lngStart, lngEnd).trim());

                return new GeocodingResult(lat, lng, "");
            }
        } catch (Exception e) {
            log.error("Failed to parse geocoding response", e);
        }
        return null;
    }
}
