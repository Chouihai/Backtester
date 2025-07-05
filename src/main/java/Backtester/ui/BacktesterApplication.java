package Backtester.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class BacktesterApplication extends Application {

    private Injector injector;

    @Override
    public void init() throws Exception {
        super.init();
        // Initialize dependency injection with simplified module
        injector = Guice.createInjector(new AppModule());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create a simple welcome screen
        primaryStage.setTitle("Backtester - Stock Trading Strategy Backtester");
        
        Label welcomeLabel = new Label("Welcome to Backtester!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-padding: 20px;");
        
        VBox root = new VBox(welcomeLabel);
        root.setAlignment(Pos.CENTER);
        
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("Application shutting down...");
    }

    public static void main(String[] args) {
        launch(args);
    }
} 