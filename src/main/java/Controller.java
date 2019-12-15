import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {
    private final Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller() {
        init();
    }

    @FXML
    private TextField filePathId;
    @FXML
    private Button loadButtonId;
    @FXML
    private TextField delimiterId;
    @FXML
    private TextField numberOfCentroidsId;
    @FXML
    private MenuButton menuButton;
    @FXML
    private MenuButton graphMenuButton;
    @FXML
    private CheckBox headerRowId;
    @FXML
    private Button runButtonId;
    @FXML
    private Button drawGraphButtonId;
    @FXML
    private Button drawFrequencyButton;


    DataRowService newDataRowService;
    private File loadedFile;
    HashMap<Integer, String> attributesToDraw;
    private List<String[]> data;

    public void init() {
        newDataRowService = new DataRowService();
        data = new ArrayList<>();
        menuButton = new MenuButton("Ignore attributes");
        attributesToDraw = new HashMap<>();
    }

    public void loadButtonIdClicked() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("xlsx/txt");
        fileChooser.setInitialDirectory(new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("csv or txt file", "*.csv"), new FileChooser.ExtensionFilter("csv or txt file", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(Main.primaryStage);
        if (selectedFile != null) {
            loadedFile = selectedFile;
            filePathId.setText(loadedFile.getCanonicalPath());
            attributesToDraw = new HashMap<>();
            if (numberOfCentroidsId != null && loadedFile != null && delimiterId != null && !delimiterId.getText().isEmpty()) {
                data = newDataRowService.read(loadedFile.getPath(), delimiterId.getText(), headerRowId.isSelected());
                menuButton.getItems().clear();
                Arrays.stream(this.newDataRowService.getHeaderValues()).map(CheckMenuItem::new).forEach(menuButton.getItems()::add);
            }
        }
    }

    private List<CheckMenuItem> ignoreAtt(String[] headervals) {
        List<CheckMenuItem> menuItemList = new ArrayList<>();
        for (String s : headervals) {
            menuItemList.add(new CheckMenuItem(s));
        }
        return menuItemList;
    }

    public void runButtonClicked() throws IOException {
        if (numberOfCentroidsId != null && loadedFile != null && delimiterId != null && !delimiterId.getText().isEmpty()) {
            int numberOfCentroids = Integer.parseInt(numberOfCentroidsId.getText());
            HashMap<String, Integer> ignoredAttributes = new HashMap<>();
            for (int i = 0; i < menuButton.getItems().size(); i++) {
                CheckMenuItem m = (CheckMenuItem) menuButton.getItems().get(i);
                if (m.isSelected()) {
                    ignoredAttributes.put(m.getText(), i);
                }
            }
            newDataRowService.processReadFile(data, numberOfCentroids, ignoredAttributes, loadedFile);
            graphMenuButton.getItems().clear();
            Arrays.stream(this.newDataRowService.getHeaderValues()).filter(s -> !ignoredAttributes.containsKey(s)).map(CheckMenuItem::new).forEach(graphMenuButton.getItems()::add);
        } else {
            logger.info("Number of centroids is not in valid format!");
        }

    }

    public void drawGraphButtonClicked() {

        for (int i = 0; i < graphMenuButton.getItems().size(); i++) {
            CheckMenuItem m = (CheckMenuItem) graphMenuButton.getItems().get(i);
            if (m.isSelected()) {
                for (int j = 0; j < newDataRowService.getHeaderValues().length; j++) {
                    if (newDataRowService.getHeaderValues()[j].equals(m.getText())) {
                        attributesToDraw.put(j, m.getText());
                    }
                }

            }
        }
        if (attributesToDraw.size() > 3) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText("Maximal number of attributes exceeded!  ");
            alert.setContentText("Maximal number of attributes is 2.");
            alert.showAndWait();
            return;
        }
        newDataRowService.drawGraphByAvailableDimension(newDataRowService.getClusterData(), attributesToDraw);
    }

    public void drawFrequencyButtonClicked() {
        for (int i = 0; i < graphMenuButton.getItems().size(); i++) {
            CheckMenuItem m = (CheckMenuItem) graphMenuButton.getItems().get(i);
            if (m.isSelected()) {
                for (int j = 0; j < newDataRowService.getHeaderValues().length; j++) {
                    if (newDataRowService.getHeaderValues()[j].equals(m.getText())) {
                        attributesToDraw.put(j, m.getText());
                    }
                }

            }
        }
        if (attributesToDraw.size() <1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText("Minimal number of attributes needed!  ");
            alert.setContentText("Minimal number of attributes is 1.");
            alert.showAndWait();
            return;
        }
        newDataRowService.drawNormalDistributionGraph(newDataRowService.getConvertedData(), attributesToDraw);
    }
}
