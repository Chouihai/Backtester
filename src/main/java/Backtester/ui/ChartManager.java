package Backtester.ui;

import Backtester.strategies.MonteCarloResult;
import javafx.concurrent.Worker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.List;

public class ChartManager {
    private final WebView equityWebView;
    private final VBox chartContainer;
    private final Label statusLabel;

    public ChartManager(WebView equityWebView, VBox chartContainer, Label statusLabel) { this.equityWebView = equityWebView; this.chartContainer = chartContainer; this.statusLabel = statusLabel; try { this.equityWebView.setContextMenuEnabled(false); this.equityWebView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); if (this.chartContainer != null) { this.equityWebView.prefWidthProperty().bind(this.chartContainer.widthProperty()); this.equityWebView.prefHeightProperty().bind(this.chartContainer.heightProperty()); } } catch (Exception ignore) { } }

    public void updateEquity(List<LocalDate> dates, double[] strategy, List<Double> buyHold) {
        if (equityWebView == null || chartContainer == null) return;
        WebEngine engine = equityWebView.getEngine();
        String url = resourceUrl("/charts/equity_chart.html");

        JSONObject payload = new JSONObject();
        JSONArray jDates = new JSONArray();
        JSONArray jStrat = new JSONArray();
        JSONArray jBh = new JSONArray();
        for (int i = 0; i < dates.size(); i++) { jDates.put(dates.get(i).toString()); }
        if (strategy != null) {
            for (int i = 0; i < strategy.length; i++) { jStrat.put(strategy[i]); }
        }
        if (buyHold != null) {
            for (int i = 0; i < buyHold.size(); i++) { jBh.put(buyHold.get(i)); }
        }
        payload.put("dates", jDates);
        if (jStrat.length() > 0) payload.put("strategy", jStrat);
        if (jBh.length() > 0) payload.put("buyhold", jBh);

        String js = buildSafeUpdateCall(payload.toString());
        Runnable runJs = () -> engine.executeScript(js);
        if (!url.equals(engine.getLocation())) {
            engine.getLoadWorker().stateProperty().addListener((obs, o, s) -> { if (s == Worker.State.SUCCEEDED) runJs.run(); });
            engine.load(url);
        } else { runJs.run(); }
        chartContainer.getChildren().setAll(equityWebView);
    }

    public void overlayMonteCarlo(MonteCarloResult mc, List<LocalDate> dates, List<Double> strategy, List<Double> buyHold) {
        if (equityWebView == null || chartContainer == null) return;
        if (mc.eqMean == null || mc.eqMean.length == 0) return;
        WebEngine engine = equityWebView.getEngine();
        String url = resourceUrl("/charts/equity_chart.html");

        int len = Math.min(dates.size(), mc.eqMean.length);
        JSONObject payload = new JSONObject();
        JSONArray jDates = new JSONArray();
        JSONArray jStrat = new JSONArray();
        JSONArray jBh = new JSONArray();
        JSONArray jMcMean = new JSONArray();
        for (int i = 0; i < len; i++) {
            jDates.put(dates.get(i).toString());
            if (strategy != null && i < strategy.size()) jStrat.put(strategy.get(i));
            if (buyHold != null && i < buyHold.size()) jBh.put(buyHold.get(i));
            jMcMean.put(mc.eqMean[i]);
        }
        payload.put("dates", jDates);
        if (jStrat.length() > 0) if (jStrat.length() > 0) if (jStrat.length() > 0) payload.put("strategy", jStrat);
        if (jBh.length() > 0) if (jBh.length() > 0) if (jBh.length() > 0) payload.put("buyhold", jBh);
        payload.put("mcMean", jMcMean);

        String js = buildSafeUpdateCall(payload.toString());
        Runnable runJs = () -> engine.executeScript(js);
        if (!url.equals(engine.getLocation())) {
            engine.getLoadWorker().stateProperty().addListener((obs, o, s) -> { if (s == Worker.State.SUCCEEDED) runJs.run(); });
            engine.load(url);
        } else { runJs.run(); }
        chartContainer.getChildren().setAll(equityWebView);
    }

    private String resourceUrl(String path) {
        return getClass().getResource(path).toExternalForm();
    }

    private String buildSafeUpdateCall(String json) {
        return "try{ if(window.updateChart){ window.updateChart(" + json + "); } else { window.__pendingPayload=" + json + "; } }catch(e){ console && console.log && console.log(e); }";
    }
}
