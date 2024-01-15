import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TableDataViewerFX extends Application {

    private ComboBox<String> tableComboBox;
    private TableView<ObservableList<String>> tableView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Oracle Table Data Viewer");

        // UI components
        tableComboBox = new ComboBox<>();
        tableView = new TableView<>();

        Button displayButton = new Button("Display Data");
        displayButton.setOnAction(e -> displayData());

        // Set up layout

        // Set up layout
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        gridPane.add(new Label("Select Table:"), 0, 0);
        gridPane.add(tableComboBox, 1, 0);
        gridPane.add(displayButton, 2, 0);
        gridPane.add(tableView, 0, 1, 3, 1);

        // Set up the database connection and populate table names
        setupDatabaseConnection();

        Scene scene = new Scene(gridPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupDatabaseConnection() {
        // Replace these values with your Oracle database credentials
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String user = "GestionHotel";
        String password = "hotel";

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection connection = DriverManager.getConnection(url, user, password);
            Statement statement = connection.createStatement();

            // Get table names
            ResultSet resultSet = statement.executeQuery("SELECT table_name FROM all_tables WHERE owner = '" + user.toUpperCase() + "'");
            while (resultSet.next()) {
                tableComboBox.getItems().add(resultSet.getString("table_name"));
            }

            resultSet.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error connecting to the database");
        }
    }

    private void displayData() {
        String selectedTable = tableComboBox.getSelectionModel().getSelectedItem();

        if (selectedTable != null) {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "GestionHotel", "hotel");
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM " + selectedTable);

                // Clear previous data
                tableView.getColumns().clear();
                tableView.getItems().clear();

                // Add columns dynamically
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    final int columnIndex = i - 1;
                    TableColumn<ObservableList<String>, String> column = new TableColumn<>(resultSet.getMetaData().getColumnName(i));
                    column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(columnIndex)));
                    tableView.getColumns().add(column);
                }

                // Add data to the table
                while (resultSet.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        row.add(resultSet.getString(i));
                    }
                    tableView.getItems().add(row);
                }

                resultSet.close();
                statement.close();
                connection.close();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error retrieving data from the database");
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
