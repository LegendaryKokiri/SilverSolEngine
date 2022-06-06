package silverSol.utils.structs;

public class Heap<T extends Object> {
	
	public static enum HeapType {MIN_HEAP, MAX_HEAP};
	private HeapType heapType;
	
	private static enum SwapType {LEFT, RIGHT, NONE};
	
	private static final int ROOT_INDEX = 1;
	
	private int size;
	private int maxSize;
	private float[] costs;
	private T[] heap;
	
	@SuppressWarnings("unchecked")
	public Heap(HeapType heapType, int maxSize) {
		this.heapType = heapType;
		this.size = 0;
		this.maxSize = maxSize;
		this.costs = new float[maxSize + 1];
		this.heap = (T[]) new Object[maxSize + 1];
	}
	
	public void insert(T element, float cost) {
		if(this.size >= this.maxSize) {
			throw new IllegalStateException("Cannot add to this heap, as it is already filled to its maximum size (" + this.maxSize + ").");
		}
		
		this.size += 1;
		this.costs[this.size] = cost;
		this.heap[this.size] = element;
		
		if(this.heapType == HeapType.MIN_HEAP) minHeapifyInsertion(this.size);
		else maxHeapifyInsertion(this.size);
	}
	
	public T pop() {
		if(this.size == 0) throw new IllegalStateException("Cannot pop from an empty heap.");
		
		T toReturn = this.heap[ROOT_INDEX];
		this.heap[ROOT_INDEX] = this.heap[this.size];
		this.costs[ROOT_INDEX] = this.costs[this.size];
		this.heapify(ROOT_INDEX);
		
		this.size--;
		return toReturn;
	}
	
	public void clear() {
		this.size = 0;
	}
	
	public int size() {
		return this.size;
	}
	
	public boolean isEmpty() {
		return this.size == 0;
	}
	
	public int maxSize() {
		return this.maxSize;
	}
	
	private void minHeapifyInsertion(int insertionIndex) {
		int parentIndex = getParent(insertionIndex);
		
		while(this.costs[insertionIndex] < this.costs[parentIndex]) {
			if(insertionIndex == ROOT_INDEX) return;
			this.swap(insertionIndex, parentIndex);
			insertionIndex = parentIndex;
			parentIndex = getParent(insertionIndex);
		}
	}
	
	private void maxHeapifyInsertion(int insertionIndex) {
		int parentIndex = getParent(insertionIndex);
		
		while(this.costs[insertionIndex] > this.costs[parentIndex]) {
			if(insertionIndex == ROOT_INDEX) return;
			this.swap(insertionIndex, parentIndex);
			insertionIndex = parentIndex;
			parentIndex = getParent(insertionIndex);
		}
	}
	
	private void heapify(int index) {
		if(!isLeaf(index)) {
			SwapType swapType = oughtSwap(index);
			if(swapType == SwapType.NONE) return;
			
			int child = swapType == SwapType.LEFT ?
					this.getLeftChild(index) : this.getRightChild(index);
			
			this.swap(index, child);
			this.heapify(child);
		}
	}
	
	private SwapType oughtSwap(int index) {
		int leftIndex = getLeftChild(index);
		int rightIndex = getRightChild(index);
		
		switch(this.heapType) {
			case MIN_HEAP:
				boolean leftLess = leftIndex <= this.size && this.costs[index] > this.costs[leftIndex];
				boolean rightLess = rightIndex <= this.size && this.costs[index] > this.costs[rightIndex];
				if(!(leftLess || rightLess)) return SwapType.NONE;
				
				return this.costs[leftIndex] < this.costs[rightIndex] ?
						SwapType.LEFT : SwapType.RIGHT;
			case MAX_HEAP:
				boolean leftMore = leftIndex <= this.size && this.costs[index] < this.costs[leftIndex];
				boolean rightMore = rightIndex <= this.size && this.costs[index] < this.costs[rightIndex];
				if(!(leftMore || rightMore)) return SwapType.NONE;
				
				return this.costs[leftIndex] > this.costs[rightIndex] ?
						SwapType.LEFT : SwapType.RIGHT;
			default:
				return SwapType.NONE;
		}
	}
	
	private void swap(int index1, int index2) {
		float tempCost = this.costs[index1];
		this.costs[index1] = this.costs[index2];
		this.costs[index2] = tempCost;
		
		T tempElement = this.heap[index1];
		this.heap[index1] = this.heap[index2];
		this.heap[index2] = tempElement;
	}
	
	private boolean isLeaf(int index) {
		return index > size / 2 && index <= size;
	}
	
	private int getParent(int index) {
		return index / 2;
	}
	
	private int getLeftChild(int index) {
		return index * 2;
	}
	
	private int getRightChild(int index) {
		return index * 2 + 1;
	}
	
	public static void main(String[] args) {
		Heap<String> heap = new Heap<>(HeapType.MIN_HEAP, 10);
		heap.insert("Mario", 2);
		heap.insert("Waluigi", 4);
		heap.insert("Luigi", 1);
		heap.insert("Wario", 3);
		
		while(heap.size > 0) {
			System.out.println(heap.pop());
		}
	}
	
}
