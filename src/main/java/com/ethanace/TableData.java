package com.ethanace;

import java.util.List;

import javafx.collections.ObservableList;

public class TableData {
    private final List<String> columnHeaders;
    private final ObservableList<ObservableList<Object>> rowData;

    public TableData(List<String> columnHeaders, ObservableList<ObservableList<Object>> rowData) {
        this.columnHeaders = columnHeaders;
        this.rowData = rowData;
    }

    public List<String> getColumnHeaders() {
        return columnHeaders;
    }

    public ObservableList<ObservableList<Object>> getRowData() {
        return rowData;
    }
}