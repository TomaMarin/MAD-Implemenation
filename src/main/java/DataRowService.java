import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class DataRowService {
    private String[] headerValues;

    DataRowService() {
    }

    List<String[]> read(String fileName, String delimiter, boolean isHeaderRow) {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            if (isHeaderRow) {
                setHeaderValues(br.readLine().split(delimiter));
            }
            String line;
            List<String[]> data = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(delimiter);
                if (!isHeaderRow) {
                    String[] attributesHeaderValues = new String[values.length];
                    for (int i = 0; i < attributesHeaderValues.length; i++) {
                        attributesHeaderValues[i] = "Attribute" + String.valueOf(i);
                    }
                    setHeaderValues(attributesHeaderValues);
                }
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].replace("\"", "");
                }
                String[] newDataRow = values;
                data.add(newDataRow);
                records.add(Arrays.asList(values));
            }
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void processReadFile(List<String[]> data, int numberOfCentroids, HashMap<String, Integer> attributesToIgnore, File loadedFile) throws IOException {
//        System.out.println("Average of math score is: " + calculateAverageForColumn(data.stream().map(Double::doubleValue).collect(Collectors.toList())));
//        System.out.println("Average of reading score is: " + calculateAverageForColumn(data.stream().map(DataRow::getReadingScore).collect(Collectors.toList())));
//        System.out.println("Average of writing score is: " + calculateAverageForColumn(data.stream().map(DataRow::getWritingScore).collect(Collectors.toList())));
        List<List<Object>> convertedData = convertColumnsByIgnoreAttributes(data, attributesToIgnore);
        List<HashMap<List<Object>, List<List<Object>>>> clusteringResults = showKMeansAlgorithm(numberOfCentroids, convertedData, attributesToIgnore);
        List<Double[]> sseResults = calculateSSE(clusteringResults);
        BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt"));
        writer.write(loadedFile.getName());
        writer.newLine();
        writer.newLine();
        writer.write("Headers: ");
        for (String k :getHeaderValues()) {
            writer.write(" "+k);
        }

        writer.newLine();
        writer.newLine();
        writer.write("Ignored Attributes: ");
        for (String k :attributesToIgnore.keySet()) {
            writer.write(" "+k);
        }
        writer.newLine();
        writer.newLine();

        writer.write("Number of rows: " + data.size());
        writer.newLine();
        writer.newLine();
        writer.write("Best result was for cluster run: ");
        int i=0;
        for (List<Object> d : clusteringResults.get(findIndexOfBestCLusterRunBySseResults(sseResults)).keySet()) {
            writer.newLine();
            writer.write("Cluster "+i +" "+ d+" size of cluster  " + clusteringResults.get(findIndexOfBestCLusterRunBySseResults(sseResults)).get(d).size());
            i++;
        }
        writer.newLine();
        writer.newLine();
        writer.close();

        System.out.println("Best result was for cluster run: ");
        for (List<Object> d : clusteringResults.get(findIndexOfBestCLusterRunBySseResults(sseResults)).keySet()) {
            System.out.println(d + " size of cluster  " + clusteringResults.get(findIndexOfBestCLusterRunBySseResults(sseResults)).get(d).size());
        }

    }

    private List<List<Object>> convertColumnsByIgnoreAttributes(List<String[]> data, HashMap<String, Integer> attributesToIgnore) {
        List<List<Object>> convertedData = new ArrayList<>();
        for (String[] dataArray : data) {
            List<Object> newConvertedRowData = new ArrayList<>();
            for (int i = 0; i < dataArray.length; i++) {
                if (!attributesToIgnore.containsValue(i)) {
                    if (!dataArray[i].isEmpty()) {
                        newConvertedRowData.add(Double.parseDouble(dataArray[i].replace("\"", "").replace("'", "").replace(",", ".").trim()));
                    } else {
                        newConvertedRowData.add(0.0);
                    }
                } else {
                    newConvertedRowData.add(dataArray[i]);
                }


            }
            convertedData.add(newConvertedRowData);
        }
        return convertedData;
    }

    private int findIndexOfBestCLusterRunBySseResults(List<Double[]> sseResults) {
        double bestAmount = Double.MAX_VALUE;
        int bestAmountIndex = 0;
        for (int i = 0; i < sseResults.size(); i++) {
            double totalAmount = 0.0;
            for (int j = 0; j < sseResults.get(i).length; j++) {
                totalAmount += sseResults.get(i)[j];
            }
            if (totalAmount < bestAmount) {
                bestAmount = totalAmount;
                bestAmountIndex = i;
            }
        }
        return bestAmountIndex;
    }

    private double calculateAverageForColumn(List<Double> columnData) {
        int totalData = 0;
        for (Double val :
                columnData) {
            totalData += val;
        }
        return (double) totalData / columnData.size();
    }

    private double calcEuclidanDistanceForObjects(Object obj1, Object obj2) {
        Double sumOfPowers = 0.0;
            if (obj1 instanceof Double && obj2 instanceof Double) {
                sumOfPowers += Math.pow((Double)obj1 - (Double)obj2, 2);

        }
        return sumOfPowers;
    }

//    private static double calcTotalVariance(List<List<Object>> points, List<Object> avgPoint) {
//        double totalAmount = 0;
//        for (List<Object> p : points) {
//            totalAmount += Math.abs(Math.pow(calcEuclidanDistanceForObjects(p.getxPoint(), avgPoint.getxPoint(), p.getyPoint(), avgPoint.getyPoint()), 2));
//        }
//        return totalAmount / points.size();
//
//    }

    private Double calculateEuclidanDistance(int x1, int x2, int x3, int y1, int y2, int y3) {
        return Math.sqrt(Math.pow(x1 - y1, 2) + Math.pow(x2 - y2, 2) + Math.pow(x3 - y3, 2));
    }

    private Double calculateEuclidanDistanceForObjectTypes(List<Object> obj1, List<Object> obj2) {
        double sumOfPowers = 0.0;
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        for (int i = 0; i < obj1.size(); i++) {
            if (pattern.matcher(obj1.get(i).toString().replace("\"", "")).matches() && pattern.matcher(obj2.get(i).toString().replace("\"", "")).matches()) {
                sumOfPowers += Math.pow(Double.parseDouble(obj1.get(i).toString()) - Double.parseDouble(obj2.get(i).toString()), 2);
            }
        }
        return Math.sqrt(sumOfPowers);
    }

    private HashMap<List<Object>, List<List<Object>>> setKMeans(int numberOfCentroids, List<List<Object>> vals) {
        List<List<Object>> actualCentroids = selectRandomDataRows(vals, numberOfCentroids);
        HashMap<List<Object>, List<List<Object>>> clusterMap = new HashMap<>();

        for (List<Object> fp : actualCentroids) {
            clusterMap.put(fp, new ArrayList<>());
        }

        return findBestCentroidForEveryDataRow(vals, clusterMap);


    }

    private List<List<Object>> selectRandomDataRows(List<List<Object>> vals, int numberOfRows) {
        List<List<Object>> actualCentroids = new ArrayList<>();
        for (int i = 0; i < numberOfRows; i++) {
            actualCentroids.add(vals.get((int) (Math.random() * vals.size())));
        }
        return actualCentroids;
    }


    private List<Object> findNearestCentroidByEuclidDistForItem(List<Object> item, Set<List<Object>> centroids) {
        double minDistance = Double.MAX_VALUE;
        List<Object> bestCentroidForItem = new ArrayList<>();
        for (List<Object> centroid : centroids) {
            Double actualDistance = calculateEuclidanDistanceForObjectTypes(item, centroid);
            if (actualDistance < minDistance) {
                minDistance = actualDistance;
                bestCentroidForItem = centroid;
            }
        }
        return bestCentroidForItem;

    }

    private HashMap<List<Object>, List<List<Object>>> calculateNewCentroids(HashMap<List<Object>, List<List<Object>>> previousClusterData, List<List<Object>> vals, HashMap<String, Integer> attributesToIgnore) {
        HashMap<List<Object>, List<List<Object>>> newCentroids = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#.#");
        for (List<Object> centroid : previousClusterData.keySet()) {
            List<Object> newCentroid = new ArrayList<>();
            for (int i = 0; i < centroid.size(); i++) {
                if (!attributesToIgnore.containsValue(i)) {
                    newCentroid.add(0.0);
                } else {
                    newCentroid.add("");
                }
            }
            for (List<Object> row :
                    previousClusterData.get(centroid)) {
                for (int i = 0; i < row.size(); i++) {
                    if (row.get(i) instanceof Double) {
                        newCentroid.set(i, (Double) newCentroid.get(i) + (Double) row.get(i));
                    }
                }
            }
            for (int i = 0; i < newCentroid.size(); i++) {
                if (newCentroid.get(i) instanceof Double) {
                    newCentroid.set(i, (Double) newCentroid.get(i) / (double) previousClusterData.get(centroid).size());
                }
            }
            newCentroids.put(newCentroid, new ArrayList<List<Object>>());
        }

        return findBestCentroidForEveryDataRow(vals, newCentroids);

    }

    private HashMap<List<Object>, List<List<Object>>> findBestCentroidForEveryDataRow(List<List<Object>> vals, HashMap<List<Object>, List<List<Object>>> centroids) {
        for (List<Object> item : vals) {
            List<Object> bestCentroidForItem = findNearestCentroidByEuclidDistForItem(item, centroids.keySet());

            List<List<Object>> actualClusterForNearestCentroid = new ArrayList<>();
            if (centroids.get(bestCentroidForItem) != null) {
                actualClusterForNearestCentroid = centroids.get(bestCentroidForItem);
            }
            actualClusterForNearestCentroid.add(item);
            centroids.put(bestCentroidForItem, actualClusterForNearestCentroid);
        }
        return centroids;
    }

    private static boolean checkIfCentroidsEqual(List<List<Object>> oldCentroids, List<List<Object>> newCentroids) {
        return oldCentroids.equals(newCentroids);
    }

    private List<HashMap<List<Object>, List<List<Object>>>> showKMeansAlgorithm(int numberOfCentroids, List<List<Object>> vals, HashMap<String, Integer> attributesToIgnore) {
        List<HashMap<List<Object>, List<List<Object>>>> mapOfClusterMaps = new ArrayList<>();

        for (int j = 0; j < numberOfCentroids; j++) {
            HashMap<List<Object>, List<List<Object>>> kk = setKMeans(numberOfCentroids, vals);
            HashMap<List<Object>, List<List<Object>>> newCentroids = calculateNewCentroids(kk, vals, attributesToIgnore);
            int it = 0;
            while (true) {
                List<List<Object>> oldCentroids = new ArrayList<>(newCentroids.keySet());
                List<List<Object>> newerCentroids = new ArrayList<>(calculateNewCentroids(newCentroids, vals, attributesToIgnore).keySet());
                if (checkIfCentroidsEqual(oldCentroids, newerCentroids)) {
                    mapOfClusterMaps.add(newCentroids);
                    break;
                }
                newCentroids = calculateNewCentroids(newCentroids, vals, attributesToIgnore);
                it++;
            }
            System.out.println("ite: " + it);
        }
        return mapOfClusterMaps;
    }

    private List<Double[]> calculateSSE(List<HashMap<List<Object>, List<List<Object>>>> listOfclusterMaps) {
        List<Double[]> listOfSSEValuesOfEachCluster = new ArrayList<>();
        for (HashMap<List<Object>, List<List<Object>>> clusterMap : listOfclusterMaps) {
            Double[] ssesOfClustersOfCurrentMap = calculateSumOfTheSquaresOfTheDistancesOfEachPointFromTheCentroid(clusterMap);
            listOfSSEValuesOfEachCluster.add(ssesOfClustersOfCurrentMap);
        }
        return listOfSSEValuesOfEachCluster;
    }

    private Double[] calculateSumOfTheSquaresOfTheDistancesOfEachPointFromTheCentroid(HashMap<List<Object>, List<List<Object>>> clusterMap) {
        Double[] ssesOfClusters = new Double[clusterMap.keySet().size()];
        for (int i = 0; i < ssesOfClusters.length; i++) {
            ssesOfClusters[i] = 0.0;
        }


        // for each run of k means , calc for each key => vals euclid  distance
        for (int i = 0; i < clusterMap.keySet().size(); i++) {
            List<List<Object>> actualClusters = new ArrayList(clusterMap.keySet());
            for (int j = 0; j < clusterMap.get(actualClusters.get(i)).size(); j++) {
                Double totalVal = 0.0;
                for (int k = 0; k < actualClusters.get(i).size(); k++) {
                    if (actualClusters.get(i).get(k) instanceof Double) {
                        totalVal += Math.pow((Double) actualClusters.get(i).get(k) - (Double) clusterMap.get(actualClusters.get(i)).get(j).get(k), 2);
                    }
                }
                ssesOfClusters[i] += Math.sqrt(totalVal);
            }
        }
        return ssesOfClusters;
    }
}
