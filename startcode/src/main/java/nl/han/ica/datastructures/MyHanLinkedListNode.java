package nl.han.ica.datastructures;

public class MyHanLinkedListNode<T> {
    private T value;
    private MyHanLinkedListNode<T> next;

    public MyHanLinkedListNode(T val) {
        this.value = val;
    }

    public MyHanLinkedListNode<T> getNext() {
        return next;
    }

    public void setNext(MyHanLinkedListNode<T> node) {
        this.next = node;
    }

    public T getValue() {
        return value;
    }
}
