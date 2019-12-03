import javafx.fxml.FXML;
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
    private CheckBox headerRowId;
    @FXML
    private Button runButtonId;
    DataRowService newDataRowService;
    private File loadedFile;

    private List<String[]> data;

    public void init() {
        newDataRowService = new DataRowService();
        data = new ArrayList<>();
        menuButton = new MenuButton("Ignore attributes");
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
            if (numberOfCentroidsId != null && loadedFile != null && delimiterId != null && !delimiterId.getText().isEmpty()) {
                data = newDataRowService.read(loadedFile.getPath(), delimiterId.getText(), headerRowId.isSelected());
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

    public void runButtonClicked() {
        DataRowService newDataRowService = new DataRowService();
        if (numberOfCentroidsId != null && loadedFile != null && delimiterId != null && !delimiterId.getText().isEmpty()) {
            int numberOfCentroids = Integer.parseInt(numberOfCentroidsId.getText());
            HashMap<String, Integer> ignoredAttributes = new HashMap<>();
            for (int i = 0; i < menuButton.getItems().size(); i++) {
                CheckMenuItem m = (CheckMenuItem) menuButton.getItems().get(i);
                if (m.isSelected()) {
                    ignoredAttributes.put(m.getText(), i);
                }
            }
            newDataRowService.processReadFile(data, numberOfCentroids, ignoredAttributes);
        } else {
            logger.info("Number of centroids is not in valid format!");
        }

    }
}
