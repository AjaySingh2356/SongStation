package com.songstation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongStationController {
    @FXML
    private TextField stationTitleField;
    @FXML
    private TextField songTitleField;
    @FXML
    private Button addButton;
    @FXML
    private ListView<Song> songListView;
    @FXML
    private TextArea songDetailArea;

    private ObservableList<Song> songs = FXCollections.observableArrayList().sorted();
    private List<Song> stationSongs = new ArrayList<>();
    private final String JSON_FILE_PATH = "songstation.json";

    @FXML
    private void initialize() {
        createJsonFileIfNotExists();
        loadSongsFromJSON();
        songListView.setItems(songs);
        songListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showSongDetails(newValue));
    }

    private void createJsonFileIfNotExists() {
        try (FileReader fileReader = new FileReader(JSON_FILE_PATH)) {
            fileReader.close(); // If the file exists, close the reader
        } catch (IOException e) {
            // If the file doesn't exist, create it and write an empty array
            try (FileWriter fileWriter = new FileWriter(JSON_FILE_PATH)) {
                JsonArray emptyArray = Json.createArrayBuilder().build();
                JsonWriter jsonWriter = Json.createWriter(fileWriter);
                jsonWriter.write(emptyArray);
                jsonWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void onAddButtonClicked() {
        String stationTitle = stationTitleField.getText().trim();
        String songTitle = songTitleField.getText().trim();

        Song newSong = new Song(songTitle);
        stationSongs.add(newSong);
        songListView.getItems().add(newSong);

        // Clear the input fields after adding a new song
        stationTitleField.clear();
        songTitleField.clear();

        saveSongsToJSON(); // Save data to JSON file after adding a new song
    }

    private void showSongDetails(Song selectedSong) {
        if (selectedSong != null) {
            songDetailArea.setText(selectedSong.toString());
        }
    }

    private void loadSongsFromJSON() {
        try (JsonReader jsonReader = Json.createReader(new FileReader(JSON_FILE_PATH))) {
            JsonArray jsonArray = jsonReader.readArray();
            stationSongs.clear();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.getJsonObject(i);
                String title = jsonObject.getString("title");
                Song song = new Song(title);
                stationSongs.add(song);
            }
            songs.setAll(stationSongs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSongsToJSON() {
        JsonArray jsonArray = Json.createArrayBuilder()
                .addAll(stationSongs.stream()
                        .map(song -> Json.createObjectBuilder()
                                .add("title", song.getTitle())
                                .build())
                        .toArray(JsonValue[]::new))
                .build();

        try (FileWriter fileWriter = new FileWriter("songstation.json")) {
            JsonWriter jsonWriter = Json.createWriter(fileWriter);
            jsonWriter.write(jsonArray);
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
