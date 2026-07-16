package io.sol.loanmanagementsystemspringbootserver.services;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final StringProperty searchQuery = new SimpleStringProperty("");

    public StringProperty searchQueryProperty(){
        return searchQuery;
    }

    public String getSearchQuery(){
        return searchQuery.get();
    }

    public void setSearchQuery(String query){
        this.searchQuery.set(query);
    }
}
