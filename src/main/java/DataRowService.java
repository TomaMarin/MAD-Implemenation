import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataRowService {
    DataRowService() {
    }

    void read(String fileName) {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            List<DataRow> data = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (!records.isEmpty()) {
                    for (int i = 0; i < values.length; i++) {
                        values[i] = values[i].replace("\"", "");
                    }
                    DataRow newDataRow = new DataRow(values[0], values[1], values[2], values[3], !values[4].equals("none"), Integer.parseInt(values[5]), Integer.parseInt(values[6]), Integer.parseInt(values[7]));
                    data.add(newDataRow);
                }
                records.add(Arrays.asList(values));
            }
            System.out.println("Average of math score is: " + calculateAverageForColumn(data.stream().map(DataRow::getMathScore).collect(Collectors.toList())));
            System.out.println("Average of reading score is: " + calculateAverageForColumn(data.stream().map(DataRow::getReadingScore).collect(Collectors.toList())));
            System.out.println("Average of writing score is: " + calculateAverageForColumn(data.stream().map(DataRow::getWritingScore).collect(Collectors.toList())));
            List<HashMap<DataRow, List<DataRow>>> clusteringResults = showKMeansAlgorithm(4,data);
            List<Double[]>  sseResults =calculateSSE(clusteringResults);
            System.out.println("DDD");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double calculateAverageForColumn(List<Integer> columnData) {
        int totalData = 0;
        for (Integer val :
                columnData) {
            totalData += val;
        }
        return (double) totalData / columnData.size();
    }

    private Double calculateEuclidanDistance(int x1, int x2, int x3, int y1, int y2, int y3) {
        return Math.sqrt(Math.pow(x1 - y1, 2) + Math.pow(x2 - y2, 2) + Math.pow(x3 - y3, 2));
    }

    private HashMap<DataRow, List<DataRow>> setKMeans(int numberOfCentroids, List<DataRow> vals) {
        List<DataRow> actualCentroids = selectRandomDataRows(vals, numberOfCentroids);
        HashMap<DataRow, List<DataRow>> clusterMap = new HashMap<>();

        for (DataRow fp : actualCentroids) {
            clusterMap.put(fp, new ArrayList<>());
        }

        return findBestCentroidForEveryDataRow(vals, clusterMap);


    }

    private List<DataRow> selectRandomDataRows(List<DataRow> vals, int numberOfRows) {
        List<DataRow> actualCentroids = new ArrayList<>();
        for (int i = 0; i < numberOfRows; i++) {
            actualCentroids.add(vals.get((int) (Math.random() * vals.size())));
        }
        return actualCentroids;
    }

    private DataRow findNearestCentroidByEuclidDistForItem(DataRow item, Set<DataRow> centroids) {
        Double minDistance = Double.MAX_VALUE;
        DataRow bestCentroidForItem = new DataRow();
        for (DataRow centroid : centroids) {
            Double actualDistance = calculateEuclidanDistance(item.getMathScore(), item.getReadingScore(), item.getWritingScore()
                    , centroid.getMathScore(), centroid.getReadingScore(), centroid.getWritingScore());
            if (actualDistance < minDistance) {
                minDistance = actualDistance;
                bestCentroidForItem = centroid;
            }
        }
        return bestCentroidForItem;

    }

    private HashMap<DataRow, List<DataRow>> calculateNewCentroids(HashMap<DataRow, List<DataRow>> previousClusterData, List<DataRow> vals) {
        HashMap<DataRow, List<DataRow>> newCentroids = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#.#");
        for (DataRow centroid : previousClusterData.keySet()) {
            DataRow newCentroid = new DataRow(0, 0, 0);
            previousClusterData.get(centroid).forEach(dataRow -> {
                newCentroid.setMathScore(newCentroid.getMathScore() + dataRow.getMathScore());
                newCentroid.setReadingScore(newCentroid.getReadingScore() + dataRow.getReadingScore());
                newCentroid.setWritingScore(newCentroid.getWritingScore() + dataRow.getWritingScore());

            });
            newCentroid.setMathScore((int)Double.parseDouble(df.format(newCentroid.getMathScore() / (double) previousClusterData.get(centroid).size())));
            newCentroid.setReadingScore((int)Double.parseDouble(df.format(newCentroid.getReadingScore() / (double) previousClusterData.get(centroid).size())));
            newCentroid.setWritingScore((int)Double.parseDouble(df.format(newCentroid.getWritingScore() / (double) previousClusterData.get(centroid).size())));
            newCentroids.put(newCentroid, new ArrayList<DataRow>());
        }

        return findBestCentroidForEveryDataRow(vals, newCentroids);

    }

    private HashMap<DataRow, List<DataRow>> findBestCentroidForEveryDataRow(List<DataRow> vals, HashMap<DataRow, List<DataRow>> centroids) {
        for (DataRow item : vals) {
            DataRow bestCentroidForItem = findNearestCentroidByEuclidDistForItem(item, centroids.keySet());

            List<DataRow> actualClusterForNearestCentroid = new ArrayList<>();
            if (centroids.get(bestCentroidForItem) != null) {
                actualClusterForNearestCentroid = centroids.get(bestCentroidForItem);
            }
            actualClusterForNearestCentroid.add(item);
            centroids.put(bestCentroidForItem, actualClusterForNearestCentroid);
        }
        return centroids;
    }
    private static boolean checkIfCentroidsEqual(List<DataRow> oldCentroids, List<DataRow> newCentroids) {
        return oldCentroids.equals(newCentroids);
    }

    private List<HashMap<DataRow, List<DataRow>>> showKMeansAlgorithm (int numberOfCentroids,List<DataRow> vals){
        List<HashMap<DataRow, List<DataRow>>> mapOfClusterMaps = new ArrayList<>();

        for (int j = 0; j < 5; j++) {
            HashMap<DataRow, List<DataRow>> kk = setKMeans(numberOfCentroids, vals);
            HashMap<DataRow, List<DataRow>> newCentroids = calculateNewCentroids(kk, vals);
            int it = 0;
            while (true) {
                List<DataRow> oldCentroids = new ArrayList<>(newCentroids.keySet());
                List<DataRow> newerCentroids = new ArrayList<>(calculateNewCentroids(newCentroids, vals).keySet());
                if (checkIfCentroidsEqual(oldCentroids, newerCentroids)) {
                    mapOfClusterMaps.add(newCentroids);
                    break;
                }
                newCentroids = calculateNewCentroids(newCentroids, vals);
                it++;
            }
            System.out.println("ite: " + it);
        }
        return  mapOfClusterMaps;
    }

    private  List<Double[]> calculateSSE(List<HashMap<DataRow, List<DataRow>>> listOfclusterMaps) {
        List<Double[]>  listOfSSEValuesOfEachCluster = new ArrayList<>();
        for (HashMap<DataRow, List<DataRow>> clusterMap : listOfclusterMaps) {
            Double[] ssesOfClustersOfCurrentMap = calculateSumOfTheSquaresOfTheDistancesOfEachPointFromTheCentroid(clusterMap);
            listOfSSEValuesOfEachCluster.add(ssesOfClustersOfCurrentMap);
        }
        return listOfSSEValuesOfEachCluster;
    }

    private  Double[] calculateSumOfTheSquaresOfTheDistancesOfEachPointFromTheCentroid(HashMap<DataRow, List<DataRow>> clusterMap) {
        Double[] ssesOfClusters = new Double[clusterMap.keySet().size()];
        for (int i = 0; i < ssesOfClusters.length; i++) {
            ssesOfClusters[i] = 0.0;
        }

        for (int i = 0; i < clusterMap.keySet().size(); i++) {
            List<DataRow> actualClusters = new ArrayList(clusterMap.keySet());
            for (int j = 0; j < clusterMap.get(actualClusters.get(i)).size(); j++) {
                ssesOfClusters[i] += calculateEuclidanDistance(actualClusters.get(i).getMathScore(), actualClusters.get(i).getReadingScore(), actualClusters.get(i).getWritingScore(), clusterMap.get(actualClusters.get(i)).get(j).getMathScore(), clusterMap.get(actualClusters.get(i)).get(j).getReadingScore(), clusterMap.get(actualClusters.get(i)).get(j).getWritingScore());
            }
        }
        return ssesOfClusters;
    }
}
