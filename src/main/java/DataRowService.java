import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.ScatterMultiColor;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Setter
public class DataRowService {
    private String[] headerValues;
    private int[] numberOfIterationsRun;
    private HashMap<List<Object>, List<List<Object>>> clusterData;
    private List<List<Object>> convertedData;
    private  Double[] averages;

    public Double[] getAverages() {
        return averages;
    }

    public void setAverages(Double[] averages) {
        this.averages = averages;
    }

    public List<List<Object>> getConvertedData() {
        return convertedData;
    }

    public void setConvertedData(List<List<Object>> convertedData) {
        this.convertedData = convertedData;
    }

    public String[] getHeaderValues() {
        return headerValues;
    }

    public void setHeaderValues(String[] headerValues) {
        this.headerValues = headerValues;
    }

    public HashMap<List<Object>, List<List<Object>>> getClusterData() {
        return clusterData;
    }

    public void setClusterData(HashMap<List<Object>, List<List<Object>>> clusterData) {
        this.clusterData = clusterData;
    }

    public int[] getNumberOfIterationsRun() {
        return numberOfIterationsRun;
    }

    public void setNumberOfIterationsRun(int[] numberOfIterationsRun) {
        this.numberOfIterationsRun = numberOfIterationsRun;
    }

    DataRowService() {
    }

    List<String[]> read(String fileName, String delimiter, boolean isHeaderRow) {
        setConvertedData(null);
        setHeaderValues(null);
        setNumberOfIterationsRun(null);
        setAverages(null);
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            if (isHeaderRow) {
                setHeaderValues(br.readLine().split(delimiter));
                if (getHeaderValues().length < 2) {
                    showAlertWindow("Wrong delimiter!", "Delimiter " + "\"" + delimiter + "\"" + "is wrong for this file!");
                    return null;
                }
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
                    if (values[i].isEmpty()) {
                        showAlertWindow("Empty value!", "Empty value has been found for row: " + Arrays.toString(values));
                        return null;
                    }
                    values[i] = values[i].replace("\"", "");
                }
                data.add(values);
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
        setClusterData(null);
        List<List<Object>> convertedData = convertColumnsByIgnoreAttributes(data, attributesToIgnore);
        BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt"));
        if (convertedData == null) {
            return;
        }
        setConvertedData(convertedData);
        List<HashMap<List<Object>, List<List<Object>>>> clusteringResults = showKMeansAlgorithm(numberOfCentroids, convertedData, attributesToIgnore);
        Double[] averages = calculateAveragesOfNumericAttributes(getHeaderValues(), attributesToIgnore, convertedData);
        setAverages(averages);
        Double[] variances = calcVarianceForEveryAttribute(getHeaderValues(), attributesToIgnore, convertedData, averages);
        List<Double[]> sseResults = calculateSSE(clusteringResults);
        setClusterData(clusteringResults.get(findIndexOfBestCLusterRunBySseResults(sseResults)));
        writer.write("File name: " + loadedFile.getName());
        writer.newLine();
        writer.newLine();
        writer.write("Headers: ");
        for (String k : getHeaderValues()) {
            writer.write(" " + k);
        }

        writer.newLine();
        writer.newLine();
        writer.write("Ignored Attributes: ");
        for (String k : attributesToIgnore.keySet()) {
            writer.write(" " + k);
        }
        writer.newLine();
        writer.newLine();

        writer.write("Number of rows: " + data.size());

        writer.newLine();
        writer.newLine();
        for (int i = 0; i < averages.length; i++) {
            if (averages[i] != null) {
                int finalI = i;
                writer.write("Maximum of numeric attribute: " + headerValues[i] + " is: " + convertedData.stream().mapToDouble(value -> (Double) value.get(finalI)).max().orElse(-1.0));
                writer.newLine();
            }
        }

        writer.newLine();
        writer.newLine();
        for (int i = 0; i < averages.length; i++) {
            if (averages[i] != null) {
                int finalI = i;
                writer.write("Minimum of numeric attribute: " + headerValues[i] + " is: " + convertedData.stream().mapToDouble(value -> (Double) value.get(finalI)).min().orElse(-1.0));
                writer.newLine();
            }
        }


        writer.newLine();
        writer.newLine();
        for (int i = 0; i < averages.length; i++) {
            if (averages[i] != null) {
                writer.write("Median of numeric attribute: " + headerValues[i] + " is: " + averages[i]);
                writer.newLine();
            }
        }


        writer.newLine();
        writer.newLine();
        for (int i = 0; i < averages.length; i++) {
            if (variances[i] != 0.0) {
                writer.write("Variance of numeric attribute: " + headerValues[i] + " is: " + variances[i]);
                writer.newLine();
            }
        }
        writer.newLine();
        writer.newLine();
        for (int i = 0; i < averages.length; i++) {
            if (variances[i] != 0.0) {
                writer.write("Standard deviation of numeric attribute: " + headerValues[i] + " is: " + Math.sqrt(variances[i]));
                writer.newLine();
            }
        }
        writer.newLine();
        writer.newLine();
        writer.write("Best result with number of " + getNumberOfIterationsRun()[findIndexOfBestCLusterRunBySseResults(sseResults)] + " iterations, with SSE: " + Arrays.stream(sseResults.get(findIndexOfBestCLusterRunBySseResults(sseResults))).mapToDouble(Double::doubleValue).sum() + ",  was for clustering run: ");
        int i = 0;
        for (List<Object> d : clusteringResults.get(findIndexOfBestCLusterRunBySseResults(sseResults)).keySet()) {
            writer.newLine();
            writer.write("Cluster " + i + " " + d + " size of cluster  " + clusteringResults.get(findIndexOfBestCLusterRunBySseResults(sseResults)).get(d).size());
            i++;
        }
        writer.newLine();
        writer.newLine();
        writer.close();

        System.out.println("Best result with number of " + getNumberOfIterationsRun()[findIndexOfBestCLusterRunBySseResults(sseResults)] + " iterations, with SSE: " + Arrays.stream(sseResults.get(findIndexOfBestCLusterRunBySseResults(sseResults))).mapToDouble(Double::doubleValue).sum() + ",  was for clustering run: ");
        for (List<Object> d : clusteringResults.get(findIndexOfBestCLusterRunBySseResults(sseResults)).keySet()) {
            System.out.println(d + " size of cluster  " + clusteringResults.get(findIndexOfBestCLusterRunBySseResults(sseResults)).get(d).size());
        }

    }


    private static HashMap<Double, Double> calcFrequencyOfList(List<List<Object>> data, int indexOfAttribute) {
        HashMap freqOfvales = new HashMap<Double, Integer>();

        for (List<Object> row : data) {
            if (!freqOfvales.containsKey(row.get(indexOfAttribute))) {
                freqOfvales.put(row.get(indexOfAttribute), 0.0);
            }
            freqOfvales.put(row.get(indexOfAttribute), calcFrequency(data, (Double) row.get(indexOfAttribute), indexOfAttribute));

        }

        return freqOfvales;
    }

    private static Double calcFrequency(List<List<Object>> data, Double amount, int indexOfAttribute) {
        Double freq = 0.0;
        for (List<Object> row : data) {
            if (row.get(indexOfAttribute).equals(amount)) {
                freq++;
            }
        }

        return freq;
    }

    public void drawNormalDistributionGraph(List<List<Object>> convertedData, HashMap<Integer, String> attributesToDraw) {

        for (Integer k:
        attributesToDraw.keySet()) {


            HashMap<Double, Double> frequenc = calcFrequencyOfList(convertedData, k);

            TreeMap<Double, Double> relativeFrequencyMap = new TreeMap<>();
            for (Map.Entry<Double, Double> m : frequenc.entrySet()) {
                relativeFrequencyMap.put(m.getKey(), (m.getValue() / (double) convertedData.size()));
            }
            double avg = calculateAverageForColumn(convertedData, k);
            TreeMap<Double, Double> nominalDistributionMap = new TreeMap<>();
            for (Map.Entry<Double, Double> m : frequenc.entrySet()) {
                nominalDistributionMap.put(m.getKey(), (countNominalDistribution(m.getKey(), avg, Math.sqrt(calcTotalVarianceForOneAttribute(convertedData, getAverages(), k)))));
            }

            XYChart relativeFrequencyMapChart = new XYChartBuilder().width(600).height(500).title("RelativeFrequency "+ attributesToDraw.get(k)).xAxisTitle("X - width").yAxisTitle("Y - length").build();
            relativeFrequencyMapChart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
            relativeFrequencyMapChart.getStyler().setChartTitleVisible(false);
            relativeFrequencyMapChart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
            relativeFrequencyMapChart.getStyler().setMarkerSize(16);
            relativeFrequencyMapChart.addSeries("relativeFrequencyMap Series", new ArrayList<Double>(relativeFrequencyMap.keySet()), new ArrayList<>(relativeFrequencyMap.values()));
            relativeFrequencyMapChart.addSeries("nominalDistributionMap", new ArrayList<Double>(nominalDistributionMap.keySet()), new ArrayList<>(nominalDistributionMap.values()));
            relativeFrequencyMapChart.addSeries("Mean", Collections.singletonList(avg), Collections.singletonList(avg / convertedData.size()));
            new SwingWrapper<>(relativeFrequencyMapChart).displayChart();

        }
    }
    public static Double countNominalDistribution(double x, double mean, double deviation) {
        return (1 / (Math.sqrt(2 * Math.PI * Math.pow(deviation, 2)))) * Math.exp(-((Math.pow((x - mean), 2)) / (2 * Math.pow(deviation, 2)))) /5;
    }


    public void drawGraphByAvailableDimension(HashMap<List<Object>, List<List<Object>>> clusterData, HashMap<Integer, String> attributesToDraw) {

        if (attributesToDraw.size() == 2) {
            List<List<Double>> xVals = new ArrayList<>();
            List<List<Double>> yVals = new ArrayList<>();
            for (List<Object> key : clusterData.keySet()) {
                xVals.add(clusterData.get(key).stream().map(objects -> (Double) objects.get(new ArrayList<>(attributesToDraw.keySet()).get(0))).collect(Collectors.toList()));
                yVals.add(clusterData.get(key).stream().map(objects -> (Double) objects.get(new ArrayList<>(attributesToDraw.keySet()).get(1))).collect(Collectors.toList()));
            }

            XYChart petalChart = new XYChartBuilder().width(600).height(500).title("Petal width and length").xAxisTitle("X - " + attributesToDraw.get(new ArrayList<>(attributesToDraw.keySet()).get(0))).yAxisTitle("Y - " + attributesToDraw.get(new ArrayList<>(attributesToDraw.keySet()).get(1))).build();
            for (int i = 0; i < xVals.size(); i++) {
                petalChart.addSeries("Cluster " + i, xVals.get(i), yVals.get(i)).setMarker(SeriesMarkers.DIAMOND);
            }
            petalChart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
            petalChart.getStyler().setChartTitleVisible(false);
            petalChart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
            petalChart.getStyler().setMarkerSize(16);
            new SwingWrapper<>(petalChart).displayChart();
        }


        if (attributesToDraw.size() == 3) {
            float x;
            float y;
            float z;
            Coord3d[] points = new Coord3d[clusterData.values().size()];

            for (int i = 0; i < clusterData.values().size(); i++) {
                x = (float) Math.random() - 0.5f;
                y = (float) Math.random() - 0.5f;
                z = (float) Math.random() - 0.5f;
                points[i] = new Coord3d(x, y, z);
            }

            ScatterMultiColor scatter = new ScatterMultiColor(points, new ColorMapper(new ColorMapRainbow(), -0.5f, 0.5f));

            // Create a chart and add scatter
            Chart chart = new Chart();
            chart.getAxeLayout().setMainColor(org.jzy3d.colors.Color.WHITE);
            chart.getView().setBackgroundColor(org.jzy3d.colors.Color.BLACK);
            chart.getScene().add(scatter);
            ChartLauncher.openChart(chart);
        }


    }

    private Double[] calculateAveragesOfNumericAttributes(String[] headerValues, HashMap<String, Integer> ignoredAttributesMap, List<List<Object>> data) {
        Double[][] doubleAmounts = new Double[headerValues.length][data.size()];
        Double[] medians = new Double[headerValues.length];
        for (int i = 0; i < doubleAmounts.length; i++) {
            Arrays.fill(doubleAmounts[i], Double.MAX_VALUE);
        }

        for (int i = 0; i < doubleAmounts.length; i++) {
            for (int j = 0; j < doubleAmounts[i].length; j++) {
                if (!ignoredAttributesMap.containsValue(i)) {
                    doubleAmounts[i][j] = (Double) data.get(j).get(i);
                }
            }
        }

        for (int i = 0; i < doubleAmounts.length; i++) {
            if (!ignoredAttributesMap.containsValue(i)) {
                Arrays.sort(doubleAmounts[i]);
                if (data.size() % 2 != 0) {
                    medians[i] = (double) doubleAmounts[i][data.size() / 2];
                } else {
                    medians[i] = (double) (doubleAmounts[i][(data.size() - 1) / 2] + doubleAmounts[i][data.size() / 2]) / 2.0;
                }
            }
        }

        return medians;
    }

    private List<List<Object>> convertColumnsByIgnoreAttributes(List<String[]> data, HashMap<String, Integer> attributesToIgnore) {
        List<List<Object>> convertedData = new ArrayList<>();
        for (String[] dataArray : data) {
            List<Object> newConvertedRowData = new ArrayList<>();
            for (int i = 0; i < dataArray.length; i++) {
                if (!attributesToIgnore.containsValue(i)) {
                    if (!dataArray[i].isEmpty()) {
                        try {
                            Double val = Double.parseDouble(dataArray[i].replace("\"", "").replace("'", "").replace(",", ".").trim());
                            newConvertedRowData.add(val);
                        } catch (NumberFormatException e) {
                            showAlertWindow("Nominal value found!", e.getMessage() + "at row " + Arrays.toString(dataArray));
                            return null;
                        }

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
            if (totalAmount > bestAmount) {
                bestAmount = totalAmount;
                bestAmountIndex = i;
            }
        }
        return bestAmountIndex;
    }

    private double calculateAverageForColumn(List<List<Object>> data,int columnIndex) {
        Double totalData = 0.0;
        for (List<Object> val :
                data) {
            totalData += (Double) val.get(columnIndex);
        }
        return (double) totalData / data.size();
    }

    private static double calcEuclidanDistanceForObjects(Object obj1, Object obj2) {
        Double sumOfPowers = 0.0;
        if (obj1 instanceof Double && obj2 instanceof Double) {
            sumOfPowers += Math.pow((Double) obj1 - (Double) obj2, 2);

        }
        return sumOfPowers;
    }

    private static Double calcTotalVarianceForOneAttribute( List<List<Object>> data, Double[] averages, int indexOfAttribute){
        Double variance = 0.0;
        for (int j = 0; j  < data.size(); j++) {

            variance += Math.abs(Math.pow(calcEuclidanDistanceForObjects(data.get(j).get(indexOfAttribute), averages[indexOfAttribute]),2));
            }
        return variance/data.size();
    }

    private static Double[] calcVarianceForEveryAttribute(String[] headerValues, HashMap<String, Integer> ignoredAttributesMap, List<List<Object>> data, Double[] averages) {
        Double[] variances = new Double[headerValues.length];
        Arrays.fill(variances, 0.0);
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < headerValues.length; j++) {
                if (!ignoredAttributesMap.containsValue(i)) {
                    variances[j] += calcEuclidanDistanceForObjects(data.get(i).get(j), averages[j]);
                }
            }
        }
        for (int i = 0; i < variances.length; i++) {
            if (!ignoredAttributesMap.containsValue(i)) {
                variances[i] = variances[i] / data.size();
            }
        }
        return variances;
    }


    private double calcTotalVariance(List<List<Object>> points, List<Object> avgPoint) {
        double totalAmount = 0;
        for (List<Object> p : points) {
            totalAmount += Math.abs(Math.pow(calculateEuclidanDistanceForObjectTypes(p, avgPoint), 2));
        }
        return totalAmount / points.size();
    }

    private Double calculateEuclidanDistance(int x1, int x2, int x3, int y1, int y2, int y3) {
        return Math.sqrt(Math.pow(x1 - y1, 2) + Math.pow(x2 - y2, 2) + Math.pow(x3 - y3, 2));
    }

    private static Double calculateEuclidanDistanceForObjectTypes(List<Object> obj1, List<Object> obj2) {
        double sumOfPowers = 0.0;
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        for (int i = 0; i < obj1.size(); i++) {
            if (obj1.get(i) instanceof Double || pattern.matcher(obj1.get(i).toString().replace("\"", "")).matches() && pattern.matcher(obj2.get(i).toString().replace("\"", "")).matches()) {
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
                    newCentroid.set(i, ((Double) newCentroid.get(i) / (double) previousClusterData.get(centroid).size()));
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
        setNumberOfIterationsRun(new int[numberOfCentroids]);
        int[] numbersOfIterations = new int[numberOfCentroids];

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
            System.out.println("Ite: " + it);
            numbersOfIterations[j] = it;
        }
        setNumberOfIterationsRun(numbersOfIterations);
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

    private void showAlertWindow(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
