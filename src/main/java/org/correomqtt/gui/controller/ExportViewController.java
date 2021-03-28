package org.correomqtt.gui.controller;


import com.google.gson.Gson;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.correomqtt.business.utils.ConnectionHolder;
import org.correomqtt.gui.model.ConnectionPropertiesDTO;
import org.correomqtt.gui.model.WindowProperty;
import org.correomqtt.gui.model.WindowType;
import org.correomqtt.gui.transformer.ConnectionTransformer;
import org.correomqtt.gui.utils.WindowHelper;
import org.hildan.fxgson.FxGson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class ExportViewController extends BaseController {


    private static ResourceBundle resources;

    @FXML
    private TextField textfield;

    @FXML
    private Button selectFileButton;

    @FXML
    private ChoiceBox<ConnectionPropertiesDTO> choiceBox;

    @FXML
    private Button importButton;

    @FXML
    private Button CancelButton;

    private static SimpleObjectProperty<File> lastKnownDirProperty;

    private static FileChooser  instance = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportViewController.class);

    public static LoaderResult<ExportViewController> load() {
        return load(ExportViewController.class, "exportView.fxml");
    }


    public static void showAsDialog() {
        Map<Object, Object> properties = new HashMap<>();
        properties.put(WindowProperty.WINDOW_TYPE, WindowType.ABOUT);

        if (WindowHelper.focusWindowIfAlreadyThere(properties)) {
            return;
        }
        LoaderResult<ExportViewController> result = load();

        resources = result.getResourceBundle();
        showAsDialog(result, "Export", properties, false, false, null, null);
    }
    private static FileChooser getInstance(){
        if(instance == null) {
            instance = new FileChooser();
            instance.initialDirectoryProperty().bindBidirectional(lastKnownDirProperty);
            instance.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.json"));
        }
        return instance;
    }
    public static File showSaveDialog(Window ownerWindow){
        File chosenFile = getInstance().showSaveDialog(ownerWindow);
        if(chosenFile != null){
            //Set the property to the directory of the chosenFile so the fileChooser will open here next
            lastKnownDirProperty.setValue(chosenFile.getParentFile());
            getInstance().initialDirectoryProperty().bindBidirectional(lastKnownDirProperty);
        }
        return chosenFile;
    }

    @FXML
    public void initialize() {

        ObservableList<ConnectionPropertiesDTO> list = FXCollections.observableArrayList(ConnectionPropertiesDTO.extractor());
        ConnectionHolder.getInstance().getSortedConnections()
                .forEach(c -> list.add(ConnectionTransformer.dtoToProps(c)));

        choiceBox.setItems(list);
        choiceBox.getSelectionModel().selectFirst();

        lastKnownDirProperty =new SimpleObjectProperty<>();
        lastKnownDirProperty.set(getInstance().getInitialDirectory());

        if(lastKnownDirProperty.get()!=null){
        Path target = Paths.get(lastKnownDirProperty.get().getAbsolutePath(), choiceBox.getSelectionModel().getSelectedItem().getName());
        textfield.setText(target.toAbsolutePath().toString());
        }else{

        }

        choiceBox.valueProperty().addListener((o, ov, nv) -> {
            if(lastKnownDirProperty.get()!=null) {
                Path newtarget = Paths.get(lastKnownDirProperty.get().getAbsolutePath(), nv.getName());
                textfield.setText(newtarget.toAbsolutePath().toString());
            }
        });

    }

    @FXML
    public void onCancelClicked() {
        LOGGER.debug("Cancel in settings clicked");
        close();
    }

    private void close() {
        Stage stage = (Stage) CancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void OnChooseFileClicked() {
        Window window =  CancelButton.getScene().getWindow();
            File dir = showSaveDialog(window);
            if (dir != null) {
                textfield.setText(dir.toPath().toString());
            } else {
                textfield.setText("");
            }
        }

    @FXML
    void OnExport( ) throws IOException {

        Gson gson = FxGson.coreBuilder().setPrettyPrinting().create();
        //Write JSON file
        try (FileWriter file = new FileWriter(textfield.getText())) {
            file.write(gson.toJson(choiceBox.getSelectionModel().getSelectedItem()));
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText("Content was Exported");
        alert.setContentText("exported to ,\n" + textfield.getText());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            close();
        }
    }


    }
