package acousticfield3d.utils;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Asier
 */
public class GenericTableModel<E> extends AbstractTableModel{
    private final ArrayList<E> elements;
    private GenericTableModelElement<E> tableAccessor;

    public GenericTableModel() {
        elements = new ArrayList<>();
    }

    public GenericTableModel(GenericTableModelElement<E> gtm) {
        tableAccessor = gtm;
        elements = new ArrayList<>();
    }

    public ArrayList<E> getElements() {
        return elements;
    }

    public void setElements(ArrayList<E> e) {
        synchronized(elements){
            clear();
            elements.addAll(e);
        }
    }
    
    public int size(){
        return elements.size();
    }

    public GenericTableModelElement<E> getTableAccessor() {
        return tableAccessor;
    }

    public void setTableAccessor(GenericTableModelElement<E> tableAccesor) {
        this.tableAccessor = tableAccesor;
    }

    
    public GenericTableModel(GenericTableModelElement<E> gtm, ArrayList<E> elements) {
        tableAccessor = gtm;
        this.elements = elements;
    }

    @Override
    public Class getColumnClass(int c) {
        return tableAccessor.getColumnClasses()[c];
    }

    @Override
    public String getColumnName(int c) {
        return tableAccessor.getColumnNames()[c];
    }

    @Override
    public int getRowCount() {
        synchronized(elements){
            return elements.size();
        }
    }

    @Override
    public int getColumnCount() {
         return tableAccessor.getColumnNames().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        synchronized(elements){
            E g = elements.get(rowIndex);
            return tableAccessor.getObject(g, columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return tableAccessor.canEdit(columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        synchronized(elements){
            E g = elements.get(rowIndex);
            if ( tableAccessor.setObject(g, columnIndex, aValue) ){
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }

    public E getAt(int index){
        synchronized(elements){
            return elements.get(index);
        }
    }
    
    public void clear(){
        synchronized(elements){
            int size = elements.size();
            if (size > 0){
                elements.clear();
                fireTableRowsDeleted(0, size-1);
            }
        }
    }

    public void add(E g){
        synchronized(elements){
            int size = elements.size();
            elements.add(g);
            fireTableRowsInserted(size, size);
        }
    }

    public void remove(E g){
        synchronized(elements){
            int index = elements.indexOf(g);
            if(index != -1){
                elements.remove(g);
                fireTableRowsDeleted(index, index);
            }
        }
    }

    public void removeAt(int index){
        synchronized(elements){
            elements.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    public void updateAt(int index){
        synchronized(elements){
            fireTableRowsUpdated(index, index);
        }
    }

    public void update(E element){
        synchronized(elements){
            int index = elements.indexOf(element);
            if(index != -1){
                fireTableRowsUpdated(index, index);
            }
        }
    }

    public void remove(int[] indices) {
        synchronized (elements) {
            Arrays.sort(indices);
            int offset = 0;
            for (int i : indices) {
                elements.remove(i - offset);
                fireTableRowsDeleted(i - offset, i - offset);
                offset++;
            }
        }
    }

    public void updateAll() {
        synchronized(elements){
            int size = elements.size();
            if(size > 0){
                fireTableRowsUpdated(0, size-1);
            }
        }
    }
    
    public void updateStructure() {
        synchronized(elements){
            fireTableStructureChanged();
        }
    }
    
    
}
