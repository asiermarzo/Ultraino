package acousticfield3d.utils;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.AbstractListModel;

/**
 *
 * @author Asier
 */
public class GenericListModel<E extends Object> extends AbstractListModel<E> {
    private final ArrayList<E> elements;

    public GenericListModel() {
        elements = new ArrayList<E>();
    }
    
    public GenericListModel(int size) {
        elements = new ArrayList<E>(size);
    }

    public GenericListModel(ArrayList<E> elements) {
        this.elements = elements;
    }

    public ArrayList<E> getElements() {
        return elements;
    }

    public synchronized void setElements(ArrayList<E> e) {
        clear();
        int size = e.size();
        if (size > 0) {
            elements.addAll(e);
            fireIntervalAdded(this, 0, size - 1);
        }
    }

    public synchronized void clear() {
        int size = elements.size();
        if (size > 0) {
            elements.clear();
            fireIntervalRemoved(this, 0, size - 1);
        }
    }

    public synchronized void add(E element) {
        int size = elements.size();
        elements.add(element);
        fireIntervalAdded(this, size, size);
    }

    public synchronized void add( int index,E element) {
        elements.add(index, element);
        fireIntervalAdded(this, index, index);
    }

    public synchronized void delete(int index) {
        elements.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public synchronized void delete(E element) {
        int index = elements.indexOf(element);
        if (index != -1) {
            elements.remove(index);
            fireIntervalRemoved(this, index, index);
        }
    }

    public synchronized void delete(int[] indices) {
        if (indices == null)
            return;
        
        Arrays.sort(indices);
        int offset = 0;
        for (int i : indices) {
            elements.remove(i - offset);
            fireIntervalRemoved(this, i - offset, i - offset);
            offset++;
        }
    }
    
    public synchronized void updateAll(){
        int size = elements.size();
        if(size > 0){
            fireContentsChanged(this, 0, size-1);
        }
    }

    public synchronized void update(E element) {
        int index = elements.indexOf(element);
        if (index != -1) {
            fireContentsChanged(this, index, index);
        }
    }

    public void update(int index) {
        fireContentsChanged(this, index, index);
    }

    public E getAt(int index) {
        return elements.get(index);
    }

    @Override
    public int getSize() {
        return elements.size();
    }

    @Override
    public E getElementAt(int index) {
        E element = elements.get(index);
        return element;
    }

    public synchronized void copyFrom(GenericListModel<E> other) {
        clear();
        ArrayList<E> otherElements = other.getElements();
        setElements(otherElements);
    }

    public synchronized void upElement(E element) {
        int index = elements.indexOf(element);
        if (index > 0) {
            E aux = elements.get(index - 1);
            elements.set(index - 1, element);
            elements.set(index, aux);
            fireContentsChanged(this, index - 1, index);
        }
    }

    public synchronized void upElement(int index) {
        if (index == -1)
            return;
        
        E element = getAt(index);
        if (index > 0) {
            E aux = elements.get(index - 1);
            elements.set(index - 1, element);
            elements.set(index, aux);
            fireContentsChanged(this, index - 1, index);
        }
    }

    public synchronized void downElement(E element) {

        int index = elements.indexOf(element);
        if (index < elements.size() - 1) {
            E aux = elements.get(index + 1);
            elements.set(index + 1, element);
            elements.set(index, aux);
            fireContentsChanged(this, index, index + 1);
        }

    }

    public synchronized void downElement(int index) {
        E element = getAt(index);
        if (index < elements.size() - 1) {
            E aux = elements.get(index + 1);
            elements.set(index + 1, element);
            elements.set(index, aux);
            fireContentsChanged(this, index, index + 1);
        }
    }
    
}
