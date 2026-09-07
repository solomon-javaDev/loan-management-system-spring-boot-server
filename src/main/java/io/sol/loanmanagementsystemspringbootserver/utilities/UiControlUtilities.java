package io.sol.loanmanagementsystemspringbootserver.utilities;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;


/**
 * Utility class providing helper methods to manage and configure UI controls.
 */
@Component
public class UiControlUtilities {

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
