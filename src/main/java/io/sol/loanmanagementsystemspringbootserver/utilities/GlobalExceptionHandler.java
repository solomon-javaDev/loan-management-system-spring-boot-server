package io.sol.loanmanagementsystemspringbootserver.utilities;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

@Component
public class GlobalExceptionHandler {

    public static void handleException(Throwable throwable){
        Logger.logError("Unhandled exception: " + throwable.getMessage());
        if(Platform.isFxApplicationThread()){
            showErrorDialog(throwable);

        }else{

            Platform.runLater(()-> showErrorDialog(throwable));
        }
    }

    private static void showErrorDialog(Throwable throwable){
        throwable.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("System Error");
        alert.setHeaderText("A fatal error occurred in the system.");
        alert.setContentText(throwable.getMessage() != null ? throwable.getMessage() : "An unexpected error caused the system to stall.");

        // Create expandable Exception text for the developer/user to copy
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Exception details hidden");


        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();

    }
}
