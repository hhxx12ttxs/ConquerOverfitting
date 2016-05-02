package trussoptimizater.Truss.ElementModels;

import java.util.ArrayList;
import trussoptimizater.Truss.Elements.Element;
import trussoptimizater.Truss.Events.ModelEvent;
import trussoptimizater.Truss.TrussModel;

/**
 * All truss element models should extend this class. The observer pattern has been implemented so that
 * a truss model observes all elementModels which observes all their elements
 * <p>
 * As java does not allow multiple inherientance
 * this classes uses an inner class called ObservableArrayList so that both Observable and ArrayList can
 * be extended.
 * </p>
 *
 * <p>
 * This class notifys any observers when a change is made to the arraylist or when a change is made
 * to an element in the element arraylsit
 * @author Chris
 */
public abstract class ElementModel<E extends Element> extends ObservableArraylist<E> {

    public ElementModel(TrussModel truss) {
        this(truss, new ArrayList<E>());
    }

    public ElementModel(TrussModel truss, ArrayList<E> elements) {
        super(truss, elements);
    }

    public void add(E e) {
        e.addObserver(this);
        elements.add(e);
        updateNumbersAfterAdd(0);
        setChanged();
        notifyObservers(new ModelEvent(e, ModelEvent.ADD_ELEMENTS));
    }

    public void add(int index, E element) {
        element.addObserver(this);
        elements.add(index, element);
        updateNumbersAfterAdd(index);
        setChanged();
        notifyObservers(new ModelEvent(element, ModelEvent.ADD_ELEMENTS));


    }

    public void addAll(ArrayList<? extends E> els) {

        for (int i = 0; i < els.size(); i++) {
            els.get(i).addObserver(this);
        }
        elements.addAll(els);
        updateNumbersAfterAdd(els.get(0).getIndex());
        setChanged();
        notifyObservers(new ModelEvent(els, ModelEvent.ADD_ELEMENTS));
    }

    public void addAll(int index, ArrayList<E> els) {
        for (int i = 0; i < els.size(); i++) {
            els.get(i).addObserver(this);
        }
        elements.addAll(index, els);
        updateNumbersAfterAdd(index);
        setChanged();
        notifyObservers(new ModelEvent(els, ModelEvent.ADD_ELEMENTS));
    }

    public void addAll(ElementModel<E> model){
        for(int i = 0;i<model.size();i++){
            elements.add(model.get(i));
        }

    }



    public void remove(int index) {
        Element e = elements.get(index);
        elements.remove(index);
        updateNumbers(index);
        setChanged();
        notifyObservers(new ModelEvent(e, ModelEvent.REMOVE_ELEMENTS));
        notifyObservers(new ModelEvent(e, ModelEvent.UNSELECT_ELEMENTS));
    }

    public void remove(E o) {
        boolean b = elements.remove(o);
        if(!b){
            return;
        }

        updateNumbers(o.getIndex());
        setChanged();
        notifyObservers(new ModelEvent(o, ModelEvent.REMOVE_ELEMENTS));
        notifyObservers(new ModelEvent(o, ModelEvent.UNSELECT_ELEMENTS));

    }

    //elements must be sorted by index for this method to work. Maybe Element should implement sortable interface?
    public void removeAll(ArrayList<? extends E> els) {
        if(elements.isEmpty()){
            return;
        }
        setChanged();
        notifyObservers(new ModelEvent(els, ModelEvent.REMOVE_ELEMENTS));
        notifyObservers(new ModelEvent(els, ModelEvent.UNSELECT_ELEMENTS));

        boolean b = elements.removeAll(els);
        if (!b) {
            return;
        }
        updateNumbers(els.get(0).getIndex());
    }

    public void clear() {
        removeAll((ArrayList<E>) elements.clone());
    }

    /**
     * This method is called after elements have been deleted, and is used to make sure all
     * elements numbers are sequential.
     * @param startIndex The array index where elements numbers need to start being updated
     */
    protected void updateNumbers(int startIndex) {
        updateNumbers(startIndex, elements.size());
    }

    /**
     * This method is called after elements have been deleted, and is used to make sure all
     * elements numbers are sequential.
     * @param startIndex The array index where elements numbers need to start being updated
     * @param endIndex The array index where elements numbers need to stop being updating - normally just array.size()
     */
    protected void updateNumbers(int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            //if (elements.get(i) instanceof Element) {
            Element e = elements.get(i); //(Element)
            //e.setNumber(i + 1);
            e.setNumber(i + 1);
            // }

        }
    }

    protected void updateNumbersAfterAdd(int startIndex) {
        updateNumbersAfterAdd(startIndex, elements.size());
    }

    protected void updateNumbersAfterAdd(int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (elements.get(i) instanceof Element) {
                Element e = (Element) elements.get(i);
                e.setNumber(i + 1);
            }
        }
    }

    public void select(ArrayList<? extends E> els, boolean select){
        for(int i = 0;i<els.size();i++){
            els.get(i).setSelected(select);
        }

        setChanged();
        notifyObservers(new ModelEvent(els, ModelEvent.SELECT_ELEMENTS));
    }

    public void selectAll(boolean select){
        if(elements.isEmpty()){
            return;
        }

        for(int i = 0;i<elements.size();i++){
            elements.get(i).setSelected(select);
        }
        setChanged();
        notifyObservers(new ModelEvent(elements, ModelEvent.SELECT_ELEMENTS));
    }

    public ArrayList<E> getSelectedElements() {
        ArrayList<E> selectedElements = new ArrayList<E>();
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).isSelected()) {
                selectedElements.add(elements.get(i));
            }
        }
        return selectedElements;
    }

    /**
     * Clears existing bars and uses the add method so that each bar is observed by this class
     * @param bars Array of Bar objects
     */
    public void setElements(Object[] elements) {
        this.elements.clear();
        for (int i = 0; i < elements.length; i++) {
            add((E) elements[i]);
        }
    }
}

