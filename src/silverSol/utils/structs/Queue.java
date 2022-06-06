package silverSol.utils.structs;

import java.util.Iterator;

public class Queue<T> implements Iterable<T> {

	private QueueItem first;
	private QueueItem last;
	private int size;
	
	private class QueueItem {
		
		public T item;
		public QueueItem next;
		
		public QueueItem(T item) {
			this.item = item;
		}
		
		@Override
		public String toString() {
			return "QueueItem " + item;
		}
	}
	
	public Queue() {
		this.size = 0;
	}
	
	public void enqueue(T item) {
		QueueItem queueItem = new QueueItem(item);
		
		if(isEmpty()) {
			first = last = queueItem;
		} else {
			last.next = queueItem;
			last = queueItem;
		}
		
		size++;
	}
	
	public T peek() {
		if(isEmpty()) return null;
		return first.item;
	}
	
	public T dequeue() {
		if(isEmpty()) return null;
		
		T toReturn = first.item;
		first = first.next;
		size--;
		
		if(isEmpty()) first = last = null;
		
		return toReturn;
	}
	
	public void clear() {
		first = last = null;
		size = 0;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public int size() {
		return size;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new QueueIterator();
	}
	
	private class QueueIterator implements Iterator<T> {
		private QueueItem current;
		
		public QueueIterator() {
			current = first;
		}
		
		@Override
		public boolean hasNext() {
			return current != null;
		}
		
		@Override
		public T next() {
			T item = current.item;
			current = current.next;
			return item;
		}

		@Override
		public void remove() {
			
		}
	}
	
}
