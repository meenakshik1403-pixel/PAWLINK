package com.pawlink.vet.base;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic TableModel for Swing JTables in PawLink.
 * Parameterized by item type T to eliminate type casting and duplicate boilerplate across panels.
 *
 * @param <T> Model domain entity type
 */
public abstract class BaseTableModel<T> extends AbstractTableModel {

    protected final String[] columnNames;
    protected List<T> items;

    public BaseTableModel(String[] columnNames) {
        this.columnNames = columnNames;
        this.items = new ArrayList<>();
    }

    public BaseTableModel(String[] columnNames, List<T> items) {
        this.columnNames = columnNames;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return (column >= 0 && column < columnNames.length) ? columnNames[column] : "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= items.size()) {
            return null;
        }
        return getValueAt(items.get(rowIndex), columnIndex);
    }

    /**
     * Subclasses implement this method to extract column values from a domain item T.
     *
     * @param item        the domain object
     * @param columnIndex column index
     * @return cell value object
     */
    public abstract Object getValueAt(T item, int columnIndex);

    public void setItems(List<T> newItems) {
        this.items = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        fireTableDataChanged();
    }

    public void addItem(T item) {
        if (item != null) {
            this.items.add(item);
            int row = this.items.size() - 1;
            fireTableRowsInserted(row, row);
        }
    }

    public void clear() {
        this.items.clear();
        fireTableDataChanged();
    }

    public T getItem(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < items.size()) {
            return items.get(rowIndex);
        }
        return null;
    }

    public List<T> getItems() {
        return new ArrayList<>(items);
    }
}
