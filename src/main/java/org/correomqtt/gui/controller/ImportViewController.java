package org.correomqtt.gui.controller;

import com.google.gson.Gson;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.correomqtt.business.model.ConnectionConfigDTO;
import org.correomqtt.business.provider.SettingsProvider;
import org.correomqtt.gui.keyring.KeyringHandler;
import org.correomqtt.gui.model.ConnectionPropertiesDTO;
import org.correomqtt.gui.model.WindowProperty;
import org.correomqtt.gui.model.WindowType;
import org.correomqtt.gui.transformer.ConnectionTransformer;
import org.correomqtt.gui.utils.WindowHelper;
import org.hildan.fxgson.FxGson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ImportViewController extends BaseController {


    private static ResourceBundle resources;

    @FXML
    private TextField textfield;

    @FXML
    private Button selectFileButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Button importButton;

    @FXML
    private Button CancelButton;

    private static SimpleObjectProperty<File> lastKnownDirProperty;

    private static FileChooser  instance = null;


    private static final Logger LOGGER = LoggerFactory.getLogger(ImportViewController.class);

    public static LoaderResult<ImportViewController> load() {
        return load(ImportViewController.class, "importView.fxml");
    }

    public static void showAsDialog() {

        Map<Object, Object> properties = new HashMap<>();
        properties.put(WindowProperty.WINDOW_TYPE, WindowType.ABOUT);

        if (WindowHelper.focusWindowIfAlreadyThere(properties)) {
            return;
        }
        LoaderResult<ImportViewController> result = load();
        resources = result.getResourceBundle();
        showAsDialog(result, "Import", properties, false, false, null, null);
    }

    @FXML
    public void initialize() {
        statusLabel.setVisible(false);
        lastKnownDirProperty =new SimpleObjectProperty<>();
        lastKnownDirProperty.set(getInstance().getInitialDirectory());
        if(getInstance().getInitialDirectory()!=null)
        textfield.setText(getInstance().getInitialDirectory().getAbsolutePath());
    }
    private static FileChooser getInstance(){
        if(instance == null) {
            instance = new FileChooser();
            instance.initialDirectoryProperty().bindBidirectional(lastKnownDirProperty);

        }
        return instance;
    }
    public static File showOpenDialog(Window ownerWindow){
        File chosenFile = getInstance().showOpenDialog(ownerWindow);
        if(chosenFile != null){
            //Set the property to the directory of the chosenFile so the fileChooser will open here next
            lastKnownDirProperty.setValue(chosenFile.getParentFile());
            getInstance().initialDirectoryProperty().bindBidirectional(lastKnownDirProperty);
        }
        return chosenFile;
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
        Window window = CancelButton.getScene().getWindow();
        File dir = showOpenDialog(window);
        if (dir != null) {
            textfield.setText(dir.toPath().toString());
        } else {
            textfield.setText("");
        }
    }

    @FXML
    void OnImport( ) throws IOException {
        Gson gson = FxGson.coreBuilder().setPrettyPrinting().create();
        // create a reader
        Reader reader = Files.newBufferedReader(Paths.get(textfield.getText()));
        ConnectionPropertiesDTO dto = gson.fromJson(reader, ConnectionPropertiesDTO.class);
        // close reader
        reader.close();

        if(dto.getIdProperty()!=null) {

            List<ConnectionConfigDTO> connections = SettingsProvider.getInstance().getConnectionConfigs();
            connections.add(ConnectionTransformer.propsToDto(dto));

            KeyringHandler.getInstance().retryWithMasterPassword(
                    masterPassword -> SettingsProvider.getInstance().saveConnections(connections, masterPassword),
                    resources.getString("onPasswordSaveFailedTitle"),
                    resources.getString("onPasswordSaveFailedHeader"),
                    resources.getString("onPasswordSaveFailedContent"),
                    resources.getString("onPasswordSaveFailedGiveUp"),
                    resources.getString("onPasswordSaveFailedTryAgain")
            );

            statusLabel.setVisible(true);
            statusLabel.setText("File loaded.");
            statusLabel.setTextFill(Color.GREEN);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setHeaderText("Content was imported");
            alert.setContentText("File loaded." + "Connection Profile");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) { close(); }
        } else{
            statusLabel.setText("Failed: Not a valid conncetion porfile import file.");
            statusLabel.setTextFill(Color.RED); }
    }

}
