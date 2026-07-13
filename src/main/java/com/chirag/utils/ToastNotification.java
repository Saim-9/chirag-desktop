package com.chirag.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Modern toast notification system replacing intrusive Alert dialogs.
 * Toasts slide in from the top, auto-dismiss after a configurable duration,
 * and support SUCCESS, ERROR, WARNING, and INFO types.
 * Use-case: User Feedback, Non-blocking Notifications.
 */
public class ToastNotification {

    public enum ToastType {
        SUCCESS, ERROR, WARNING, INFO
    }

    private static final double TOAST_DURATION_SECONDS = 3.5;

    /**
     * Shows a toast notification overlaid on the current scene.
     * The toast appears at the top, slides down, then auto-fades after the duration.
     *
     * @param message  the text to display
     * @param type     the toast type (determines color and icon)
     */
    public static void show(String message, ToastType type) {
        Stage stage = SceneManager.getInstance().getPrimaryStage();
        if (stage == null || stage.getScene() == null) return;

        javafx.scene.Parent root = stage.getScene().getRoot();
        if (!(root instanceof javafx.scene.layout.Pane)) return;

        // Create the toast label
        Label toast = new Label(getIcon(type) + "  " + message);
        toast.setWrapText(true);
        toast.setMaxWidth(500);
        toast.setStyle(getStyle(type));
        toast.setAlignment(Pos.CENTER);
        toast.setOpacity(0);
        toast.setTranslateY(-40);

        // Wrap in a StackPane for top-center positioning
        StackPane toastContainer = new StackPane(toast);
        toastContainer.setAlignment(Pos.TOP_CENTER);
        toastContainer.setPickOnBounds(false);
        toastContainer.setMouseTransparent(false);
        toastContainer.setPadding(new javafx.geometry.Insets(15, 0, 0, 0));

        // Allow clicking to dismiss early
        toast.setOnMouseClicked(e -> dismissToast(toast, toastContainer, root));

        // Add to the scene
        if (root instanceof javafx.scene.layout.BorderPane) {
            javafx.scene.layout.BorderPane bp = (javafx.scene.layout.BorderPane) root;
            // Overlay on top of existing content
            javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane();
            overlay.setPickOnBounds(false);
            overlay.setAlignment(Pos.TOP_CENTER);
            if (bp.getCenter() != null) {
                javafx.scene.Node originalCenter = bp.getCenter();
                overlay.getChildren().addAll(originalCenter, toastContainer);
                bp.setCenter(overlay);

                // Store reference for cleanup
                toast.setUserData(new Object[]{bp, originalCenter, overlay});
            }
        } else if (root instanceof javafx.scene.layout.StackPane) {
            ((javafx.scene.layout.StackPane) root).getChildren().add(toastContainer);
        } else if (root instanceof javafx.scene.layout.AnchorPane) {
            javafx.scene.layout.AnchorPane.setTopAnchor(toastContainer, 0.0);
            javafx.scene.layout.AnchorPane.setLeftAnchor(toastContainer, 0.0);
            javafx.scene.layout.AnchorPane.setRightAnchor(toastContainer, 0.0);
            ((javafx.scene.layout.AnchorPane) root).getChildren().add(toastContainer);
        }

        // Slide-in animation
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toast);
        slideIn.setFromY(-40);
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        slideIn.play();
        fadeIn.play();

        // Auto-dismiss after duration
        PauseTransition pause = new PauseTransition(Duration.seconds(TOAST_DURATION_SECONDS));
        pause.setOnFinished(e -> dismissToast(toast, toastContainer, root));
        pause.play();
    }

    /**
     * Convenience methods for each toast type.
     */
    public static void success(String message) {
        show(message, ToastType.SUCCESS);
    }

    public static void error(String message) {
        show(message, ToastType.ERROR);
    }

    public static void warning(String message) {
        show(message, ToastType.WARNING);
    }

    public static void info(String message) {
        show(message, ToastType.INFO);
    }

    /**
     * Dismisses the toast with a fade-out animation and cleans up.
     */
    private static void dismissToast(Label toast, StackPane container, javafx.scene.Parent root) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            // Clean up from parent
            if (toast.getUserData() instanceof Object[]) {
                Object[] data = (Object[]) toast.getUserData();
                javafx.scene.layout.BorderPane bp = (javafx.scene.layout.BorderPane) data[0];
                javafx.scene.Node originalCenter = (javafx.scene.Node) data[1];
                bp.setCenter(originalCenter);
            } else if (root instanceof javafx.scene.layout.StackPane) {
                ((javafx.scene.layout.StackPane) root).getChildren().remove(container);
            } else if (root instanceof javafx.scene.layout.AnchorPane) {
                ((javafx.scene.layout.AnchorPane) root).getChildren().remove(container);
            }
        });
        fadeOut.play();
    }

    /**
     * Returns the text prefix for each toast type.
     */
    private static String getIcon(ToastType type) {
        return switch (type) {
            case SUCCESS -> "[OK]";
            case ERROR -> "[Error]";
            case WARNING -> "[Warning]";
            case INFO -> "[Info]";
        };
    }

    /**
     * Returns the inline CSS style for each toast type.
     */
    private static String getStyle(ToastType type) {
        String baseStyle = "-fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 4);";

        return switch (type) {
            case SUCCESS -> baseStyle + "-fx-background-color: #27714A; -fx-text-fill: white;";
            case ERROR -> baseStyle + "-fx-background-color: #C0392B; -fx-text-fill: white;";
            case WARNING -> baseStyle + "-fx-background-color: #F77F00; -fx-text-fill: white;";
            case INFO -> baseStyle + "-fx-background-color: #1B2A4A; -fx-text-fill: #F5F0EB;";
        };
    }
}
