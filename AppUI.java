public class AppUI extends Application {
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Popup Window");

        Label messageLabel = new Label("Test");
        StackPane layout = new StackPane();
        layout.getChildren().add(messageLabel);

        Scene scene = new Scene(layout, 250, 150);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
