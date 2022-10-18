package nl.han.ica.datastructures;

public class MyHanStack<T> implements IHANStack<T>{

    IHANLinkedList<T> list;

    public MyHanStack() {
        list = new MyHanLinkedList<>();
    }

    @Override
    public void push(T value) {
        list.addFirst(value);
    }

    @Override
    public T pop() {
        T temp = list.getFirst();
        list.removeFirst();
        return temp;
    }

    @Override
    public T peek() {
        return list.getFirst();
    }
}
