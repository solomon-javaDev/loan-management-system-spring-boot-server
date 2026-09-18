package io.sol.loanmanagementsystemspringbootserver.utilities;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;


/**
 * Utility class providing helper methods to manage and configure UI controls.
 */
@Component
public class UiControlUtilities {

    public static void autoSizeColumns(TableView<?> tableView){
        if (tableView == null) return;

        Runnable resize = () -> {
            Text text = new Text();
            for (TableColumn<?, ?> column : tableView.getColumns()) {
                text.setText(column.getText() == null ? "" : column.getText());
                double maxWidth = text.getLayoutBounds().getWidth() + 30;

                for (int row = 0; row < tableView.getItems().size(); row++) {
                    Object value = column.getCellData(row);
                    if (value != null) {
                        text.setText(value.toString());
                        maxWidth = Math.max(maxWidth, text.getLayoutBounds().getWidth() + 24);
                    }
                }
                column.setMinWidth(maxWidth);
                column.setPrefWidth(maxWidth);
            }
            tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        };

        Platform.runLater(resize);
    }

    /**
     *
     * @param formatter
     * @return
     * @param <S>
     * @param <T>
     */
      /*
    // Example with dates
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(TableCellFactoryHelper.forFormattedCell(
                d -> d.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        ));

        For more flexibility: predefined factories can work as well, but we already have a MoneyHelper class
        public static <S> Callback<TableColumn<S, BigDecimal>, TableCell<S, BigDecimal>> moneyCellFactory() {
    return forFormattedCell(MoneyHelper::formatMoney);
        }

        public static <S> Callback<TableColumn<S, LocalDate>, TableCell<S, LocalDate>> dateCellFactory(DateTimeFormatter formatter) {
            return forFormattedCell(d -> d.format(formatter));
        }


        and usage becomes super easy: e.g
        fullPayment.setCellFactory(UiControlUtilities.moneyCellFactory());
        dateColumn.setCellFactory(UiControlUtilities.dateCellFactory(DateTimeFormatter.ISO_DATE));


     */

    public static <S, T> Callback<TableColumn<S,T>, TableCell<S, T>> formattedCell(Function<T, String> formatter){
        return column -> new TableCell<S, T>(){
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null){
                    setText("");
                }else{
                    setText(formatter.apply(item));
                }
            }
        };
    }

    public <T> void configureSearchableDropDown(ComboBox<T> comboBox
    , List<T> sourceItems, Function<T, String> nameExtractor
    ){
        ObservableList<T> allItems = FXCollections.observableArrayList(sourceItems);

        FilteredList<T> filteredItems = new FilteredList<>(allItems, item -> true);

        comboBox.setEditable(true);
        comboBox.setItems(filteredItems);

        comboBox.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T item) {
                return item == null ? "" :  nameExtractor.apply(item);
            }

            @Override
            public T fromString(String values) {
                return comboBox.getValue();
            }
        });

        TextField editor = comboBox.getEditor();

        editor.textProperty().addListener((observable, oldText, newText) -> {
            String query = newText == null ? "" : newText.trim().toLowerCase();

            filteredItems.setPredicate(item -> query.isBlank() || nameExtractor.apply(item).toLowerCase().contains(query));

            if(!filteredItems.isEmpty()){
                comboBox.show();
            }
        });

        comboBox.setOnHidden(event -> {
            if(comboBox.getValue() != null){
                editor.setText((nameExtractor.apply(comboBox.getValue())));
            }
        });
    }


    public <T> void configureDropDown(ComboBox<T> comboBox, List<T> items, Function<T, String> nameExtractor){
        if(items == null){
            return;
        }

        comboBox.getItems().clear();
        // 1. Populated the comboBox with items provided
        comboBox.setItems(FXCollections.observableArrayList(items));

        // 2.
        comboBox.setConverter(
                new StringConverter<T>() {
                    @Override
                    public String toString(T t) {
                        if(t == null){
                            return "";
                        }
                        String val = nameExtractor.apply(t);
                        return val == null ? "" : val.trim();
                    }

                    @Override
                    public T fromString(String s) {
                        return null; //dropdown stays
                    }
                }
        );

    }
}
