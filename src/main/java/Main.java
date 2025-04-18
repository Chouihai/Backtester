import Services.StockService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String API_KEY = "dqHpjXO1Lvm1CI56Vpp8DTcmJi6g8PIX";

    @Override
    public void start(Stage primaryStage) {
        StockService stockService = new StockService(API_KEY);
        StockPriceApp stockPriceApp = new StockPriceApp(stockService);

        Scene scene = new Scene(stockPriceApp.getRoot(), 400, 250);

        primaryStage.setTitle("Stock Price Fetcher");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}