package Backtester.ui;

import javafx.scene.control.Label;

public final class UiFormat {
    private UiFormat() {}

    public static String formatCurrency(double value) {
        return String.format("$%,.2f", value);
    }

    public static String formatPercentage(double value) {
        if (Double.isNaN(value)) return "0.00%";
        return String.format("%.2f%%", value * 100);
    }

    public static String formatDecimal(double value) {
        if (Double.isNaN(value)) return "0.00";
        return String.format("%.2f", value);
    }

    public static void setLabelColor(Label label, double value) {
        if (Double.isNaN(value)) {
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        } else if (value > 0) {
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");
        } else if (value < 0) {
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
        } else {
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        }
    }
}

