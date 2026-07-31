package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final StringProperty searchQuery = new SimpleStringProperty("");

    public StringProperty searchQueryProperty() {
        return searchQuery;
    }

    public Result<String> getSearchQuery() {
        return Result.success("Search query loaded successfully.", searchQuery.get());
    }

    public Result<String> setSearchQuery(String query) {
        this.searchQuery.set(query == null ? "" : query);
        return Result.success("Search query updated successfully.", this.searchQuery.get());
    }
}
