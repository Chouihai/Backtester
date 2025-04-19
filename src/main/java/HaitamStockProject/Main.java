package HaitamStockProject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Injector injector = Guice.createInjector(new AppModule());
        StockPriceApp stockPriceApp = injector.getInstance(StockPriceApp.class);

        Scene scene = new Scene(stockPriceApp.getRoot(), 800, 600);

        primaryStage.setTitle("Stock Price Fetcher");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}