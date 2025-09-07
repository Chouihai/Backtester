package Backtester.ui;

import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.JSONObject;

public class CodeMirrorStrategyEditor {
    private final WebView webView;
    private boolean loaded = false;
    private String pendingText = "";

    public CodeMirrorStrategyEditor() {
        webView = new WebView();
        webView.setPrefHeight(160);
        webView.setMinHeight(120);
        webView.setMaxHeight(160);
        WebEngine engine = webView.getEngine();
        String url = getClass().getResource("/Backtester/ui/editor/codemirror_editor.html").toExternalForm();
        engine.getLoadWorker().stateProperty().addListener((obs, o, s) -> {
            if (s == Worker.State.SUCCEEDED) {
                loaded = true;
                if (pendingText != null && !pendingText.isEmpty()) {
                    setText(pendingText);
                }
            }
        });
        engine.load(url);
    }

    public Node getNode() { return webView; }

    public void requestFocus() { webView.requestFocus(); }

    public void setText(String text) {
        pendingText = text == null ? "" : text;
        if (!loaded) return;
        WebEngine engine = webView.getEngine();
        String quoted = JSONObject.quote(pendingText);
        String js = "try{ window.setEditorText && window.setEditorText(" + quoted + "); }catch(e){ console && console.log && console.log(e);}";
        engine.executeScript(js);
    }

    public String getText() {
        if (!loaded) return pendingText;
        Object result = webView.getEngine().executeScript("(window.getEditorText && window.getEditorText()) || ''");
        return result != null ? String.valueOf(result) : "";
    }
}