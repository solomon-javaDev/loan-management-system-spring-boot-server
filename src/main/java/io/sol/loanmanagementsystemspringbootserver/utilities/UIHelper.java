package io.sol.loanmanagementsystemspringbootserver.utilities;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

public class UIHelper {

    public static void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void updateStatusLabel(Label label, Result<?> result) {
        if (label == null) return;
        label.setText(result.message());
        if (result.isSuccess()) {
            label.setTextFill(Color.GREEN);
        } else {
            label.setTextFill(Color.RED);
        }
    }

    /**
     *
     * @param deafultPrefix
     * @param title
     * @param dataMap
     * @param ownerWindow
     */
    public static void exportToPdf(String deafultPrefix, String title, Map<String, String> dataMap, Stage ownerWindow){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.setInitialFileName (deafultPrefix + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files","*.pdf"));

        File file = fileChooser.showSaveDialog(ownerWindow);

        if(file != null){
            try(PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf)){
                // 1. Add Heading
                document.add(new Paragraph(title + " - " + LocalDate.now()).setBold().setFontSize(18));
                document.add(new Paragraph(" ")); // Spacing

                // 2. Dynamically build the table
                Table table = new Table(2);
                // Make the table fill out nicely
                table.useAllAvailableWidth();

                for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                    table.addCell(entry.getKey() != null ? entry.getKey() : "");
                    table.addCell(entry.getValue() != null ? entry.getValue() : "");
                }

                document.add(table);

                UIHelper.showInfo("Success", "PDF report generated successfully.");
            } catch (FileNotFoundException e) {
                UIHelper.showError("Failed to generate PDF", e.getMessage());
            } catch (IOException e) {
                UIHelper.showError("Failed to generate PDF", e.getMessage());
                            }
        }

    }
}
