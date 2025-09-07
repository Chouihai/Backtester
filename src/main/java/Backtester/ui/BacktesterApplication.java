package Backtester.ui;

import Backtester.AppModule;
import Backtester.services.HistoricalDataService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class BacktesterApplication extends Application {

    private Injector injector;
    private BacktesterController controller;

    @Override
    public void init() throws Exception {
        super.init();
        injector = Guice.createInjector(new AppModule());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Backtester - Stock Trading Strategy Backtester");

        HistoricalDataService historicalDataService = injector.getInstance(HistoricalDataService.class);
        controller = new BacktesterController(historicalDataService);

        Parent root = createUI();
        Scene scene = new Scene(root, 1200, 800);        scene.setFill(Color.web("#0e1117"));
        try {
            var cssUrl = getClass().getResource("/Backtester/ui/dark_theme.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

        } catch (Exception ignore) {}
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    private Parent createUI() {
        BacktesterUI backtesterUI = new BacktesterUI(controller);
        return backtesterUI.createUI();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (controller != null) controller.shutdown();
    }

    public static void main(String[] args) { launch(args); }
}
