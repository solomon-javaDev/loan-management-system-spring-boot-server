package io.sol.loanmanagementsystemspringbootserver.utilities;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class UiControlUtilities {

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
