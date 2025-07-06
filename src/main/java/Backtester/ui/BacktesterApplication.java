package Backtester.ui;

import Backtester.AppModule;
import Backtester.services.HistoricalDataService;
import Backtester.strategies.PositionManager;
import Backtester.strategies.StrategyRunner;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class BacktesterApplication extends Application {

    private Injector injector;
    private BacktesterController controller;

    @Override
    public void init() throws Exception {
        super.init();
        // Initialize dependency injection
        injector = Guice.createInjector(new AppModule());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Backtester - Stock Trading Strategy Backtester");
        
        // Get services from dependency injection
        HistoricalDataService historicalDataService = injector.getInstance(HistoricalDataService.class);
        StrategyRunner strategyRunner = injector.getInstance(StrategyRunner.class);
        PositionManager positionManager = injector.getInstance(PositionManager.class);
        
        // Create controller with injected services
        controller = new BacktesterController(historicalDataService, strategyRunner, positionManager);
        
        // Create the UI using the controller
        Parent root = createUI();
        
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    private Parent createUI() {
        // Create the UI layout and inject the controller
        BacktesterUI backtesterUI = new BacktesterUI(controller);
        return backtesterUI.createUI();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 