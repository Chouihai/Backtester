//package HaitamStockProject;
//
//import com.google.inject.Guice;
//import com.google.inject.Injector;
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class Main extends Application {
//
//    private static final Logger logger = LoggerFactory.getLogger(Main.class);
//
//    @Override
//    public void start(Stage primaryStage) {
//        logger.info("Starting Stock Visualizer Application...");
//
//        Injector injector = Guice.createInjector(new AppModule());
//        StockPriceApp stockPriceApp = injector.getInstance(StockPriceApp.class);
//
//        Scene scene = new Scene(stockPriceApp.getRoot(), 800, 600);
//
//        primaryStage.setTitle("Stock Price Fetcher");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//
//        logger.info("Application UI launched successfully.");
//    }
//
//    public static void main(String[] args) {
//        logger.info("Launching JavaFX Application...");
//        launch(args);
//    }
//}