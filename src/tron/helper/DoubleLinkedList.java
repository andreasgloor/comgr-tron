package tron.helper;

public class DoubleLinkedList<T> {

	private static class Node<T> {
        private T data;
        private Node next;
        private Node prev;

        public Node(T data) {
            this.data = data;
        }
        
        public void displayNode() {
            System.out.print(data + " ");
        }

        @Override
        public String toString() {
            return data.toString();
        }
    }
	
	public int Length() {
		return length;
	}
	
	private int length = 0; 
	public Node first = null;
    public Node last = null;

    public void addFirst(T data) {
        Node newNode = new Node(data);

        if (isEmpty()) {
            newNode.next = null; 
            newNode.prev = null; 
            first = newNode;
            last = newNode;

        } else {
            first.prev = newNode;
            newNode.next = first;
            newNode.prev = null; 
            first = newNode;
        }
        length++; 
    }
    
    public boolean isEmpty() {
        return (first == null); 
    }
    
    public void displayList() {
        Node current = first;
        while (current != null) {
            current.displayNode();
            current = current.next;
        }
        System.out.println();
    }
    
    public void removeFirst() {
        if (!isEmpty()) {
            Node temp = first;

            if (first.next == null) {
                first = null;
                last = null;
            } else {
                first = first.next;
                first.prev = null;
            }
            // System.out.println(temp.toString() + " is popped from the list");
            length--;
        }
    }
    
    public void removeLast() {
        Node temp = last;

        if (!isEmpty()) {

            if (first.next == null) {
                first = null;
                last = null;
            } else {
                last = last.prev;
                last.next = null;
            }
        }
        //System.out.println(temp.toString() + " is popped from the list"); // ...was removed from...
        length--; 
    }

    public Object getLast() {
    	return last.data;
    }
    public Object getFirst() {
    	return first.data;
    }
    
}
