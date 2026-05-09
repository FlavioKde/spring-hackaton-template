package com.interhack.spring_hackaton_template.service;

import com.interhack.spring_hackaton_template.model.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClusteringService {

    @Data
    @AllArgsConstructor
    public static class Cluster {
        private String name;
        private double centerLat;
        private double centerLng;
        private List<Client> clients;
    }

    public List<Cluster> clusterClients(List<Client> clients, int maxClusters) {
        if (clients.isEmpty()) return List.of();
        if (clients.size() <= maxClusters) {
            List<Cluster> result = new ArrayList<>();
            for (int i = 0; i < clients.size(); i++) {
                Client c = clients.get(i);
                result.add(new Cluster("Zone-" + (i + 1), c.getLatitude(), c.getLongitude(),
                        new ArrayList<>(List.of(c))));
            }
            return result;
        }

        int k = Math.min(maxClusters, clients.size());
        return kMeansClustering(clients, k, 100);
    }

    private List<Cluster> kMeansClustering(List<Client> clients, int k, int maxIterations) {
        Random random = new Random(42);
        List<double[]> centroids = new ArrayList<>();

        List<Client> shuffled = new ArrayList<>(clients);
        Collections.shuffle(shuffled, random);
        for (int i = 0; i < k; i++) {
            centroids.add(new double[]{shuffled.get(i).getLatitude(), shuffled.get(i).getLongitude()});
        }

        List<List<Client>> assignments = null;

        for (int iter = 0; iter < maxIterations; iter++) {
            assignments = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                assignments.add(new ArrayList<>());
            }

            for (Client client : clients) {
                int nearest = 0;
                double minDist = Double.MAX_VALUE;
                for (int i = 0; i < k; i++) {
                    double dist = haversine(client.getLatitude(), client.getLongitude(),
                            centroids.get(i)[0], centroids.get(i)[1]);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = i;
                    }
                }
                assignments.get(nearest).add(client);
            }

            boolean converged = true;
            for (int i = 0; i < k; i++) {
                List<Client> cluster = assignments.get(i);
                if (cluster.isEmpty()) continue;

                double newLat = cluster.stream().mapToDouble(Client::getLatitude).average().orElse(0);
                double newLng = cluster.stream().mapToDouble(Client::getLongitude).average().orElse(0);

                if (Math.abs(newLat - centroids.get(i)[0]) > 0.0001 ||
                    Math.abs(newLng - centroids.get(i)[1]) > 0.0001) {
                    converged = false;
                }
                centroids.set(i, new double[]{newLat, newLng});
            }

            if (converged) break;
        }

        List<Cluster> result = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            if (!assignments.get(i).isEmpty()) {
                result.add(new Cluster(
                        "Zone-" + (i + 1),
                        centroids.get(i)[0],
                        centroids.get(i)[1],
                        assignments.get(i)
                ));
            }
        }
        return result;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
