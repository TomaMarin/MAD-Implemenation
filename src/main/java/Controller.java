import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private ComboBox ignoreAttributesComboBox;
    @FXML
    private CheckBox headerRowId;
    @FXML
    private Button runButtonId;
    DataRowService newDataRowService;
    private File loadedFile;

    private List<DataRow> data;

    public void init() {
        newDataRowService = new DataRowService();
        data = new ArrayList<>();
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
                data = newDataRowService.read(loadedFile.getName(), delimiterId.getText(), headerRowId.isSelected());
                ignoreAttributesComboBox.setItems(FXCollections.observableArrayList( Arrays.asList(this.newDataRowService.getHeaderValues())));
            }
        }
    }

    public void runButtonClicked() {
        DataRowService newDataRowService = new DataRowService();
        if (numberOfCentroidsId != null && loadedFile != null && delimiterId != null && !delimiterId.getText().isEmpty()) {
            int numberOfCentroids = Integer.parseInt(numberOfCentroidsId.getText());
            newDataRowService.processReadFile(data, numberOfCentroids);
        } else {
            logger.info("Number of centroids is not in valid format!");
        }

    }
}
