package nl.han.ica.datastructures;

public class MyHanLinkedList<T> implements IHANLinkedList<T>{
    private MyHanLinkedListNode<T> firstNode = null;

    @Override
    public void addFirst(T value) {
        var temp = new MyHanLinkedListNode<>(value);
        if (firstNode != null) temp.setNext(firstNode);
        firstNode = temp;
    }

    @Override
    public void clear() {
        firstNode = null;
    }

    @Override
    public void insert(int index, T value) {
        if (firstNode == null || index == 0) addFirst(value);

        if (index > getSize()) return;

        var temp = new MyHanLinkedListNode<>(value);
        var indexNode = getNoteAtIndex(index);

        temp.setNext(indexNode.getNext());
        indexNode.setNext(temp);
    }

    @Override
    public void delete(int pos) {
        if (pos == 0) removeFirst();

        if (pos > getSize()) return;

        var indexNode = getNoteAtIndex(pos);
        indexNode.setNext(indexNode.getNext().getNext());
    }

    @Override
    public T get(int pos) {
        return getNoteAtIndex(pos).getValue();
    }

    @Override
    public void removeFirst() {
        firstNode = firstNode.getNext();
    }

    @Override
    public T getFirst() {
        return firstNode.getValue();
    }

    @Override
    public int getSize() {
        int size = 0;

        if (firstNode == null) return size;

        var current = firstNode;

        while (current.getNext() != null) {
            current = current.getNext();
            size++;
        }
        return size;
    }

    private MyHanLinkedListNode<T> getNoteAtIndex(int index) {
        int counter = 0;
        var current = firstNode;

        while (counter != index - 1) {
            current = current.getNext();
            counter++;
        }
        return current;
    }
}
