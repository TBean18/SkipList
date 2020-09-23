import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

public class SkipListSet<T extends Comparable<T>> implements SortedSet<T>, Set<T>, Iterable<T>, Collection<T> {
	
	public SkipListNode head, tail;
	
	public int size;
	
	public int height;
	
	public Random r; //Used for .nextBoolean
	
	private class SkipListNode {
		
		T data;
		//References
		SkipListNode right, left, up, down;
		
		
		int nodeHeight;
		
		public SkipListNode(T data) {
			this.data = data;
			right = null;
			down = null;
			left = null;
			up = null;
			nodeHeight = 1;
		}
		
		//Constructor used for 3rd dimension
		public SkipListNode(T data, SkipListNode child) {
			// TODO Auto-generated constructor stub
			this.data = data;
			right = null;
			down = child;
			left = null;
			up = null;
			
			nodeHeight = child.nodeHeight + 1;
			child.up = this;
		}


	}

	//Custom Iterator Class
	private class SkipListIterator implements Iterator<T>{

		SkipListNode cur;
		boolean removable = true;
		SkipListIterator(SkipListSet<T> set){
			cur = set.returnBottomMostNode(head);
		}
		
		@Override
		public boolean hasNext() {
			if(cur != null) {
				return true;
			}
			return false;
		}

		@Override
		public T next() {
			cur = cur.right;
			if(cur == returnBottomMostNode(tail)) {
				throw new NoSuchElementException();
			}
			removable = true;
			return cur.data;
		}
		//TODO fix this needs to call methods
		public void remove() {
			if(removable == false) {
				throw new IllegalStateException();
			}
			removeNode(cur);

			//reset removable flag
			removable = false;
			return;
		}
	}

	
	public SkipListSet() { //Constructor 1
		
		this.head = new SkipListNode((T)"-inf");
		this.tail = new SkipListNode((T)"+inf");
		
		head.right = tail;
		tail.left = head;
		
		size = 0;
		height = 1;
		
		r = new Random();
		
	
	}

	
	public SkipListSet(Collection<T> C) { //Constructor 2 with Collection
		
		this.head = new SkipListNode((T)"-inf");;
		this.tail = new SkipListNode((T)"+inf");
		
		head.right = tail;
		tail.left = head;
		
		
		size = 0;
		height = 1;
		
		r = new Random();
		
		this.addAll(C);
		
//		this.reBalance();
		
		
		
	}
	
	//Returns the bottom node on a given node stack
	private SkipListNode returnBottomMostNode(SkipListNode n) {
		while(n.down != null) {
			n = n.down;
			
		}
		return n;
	}
	
	// This should be runtime O(n LogN)
	// Thus, we traverse the List Height amount of times resulting in N*Log(n) complexity
	public void reBalance() {
		
		//Reset head and tail to be the bottom most nodes
		head = this.returnBottomMostNode(head);
		tail = this.returnBottomMostNode(tail);
		
		int trueHeight = height;
		height = 1;
		
		//Get a current pointer and a prevStacked pointer
		SkipListNode cur, prevStacked = null;
				
		
		//Loop through until we've achived height
		for(int i = 1; i < trueHeight; i++) {
			//Make a new empty layer
			addLayer();
			prevStacked = head;
			
			//Assign cur to the next node to the head, before we stack head
			cur = head.down.right;
			
			//While cur exists
			while(cur.data != tail.data) {
				
				//check if it wins the coin flip, if so, stack it
				if(r.nextBoolean() == true) {
					cur.up = new SkipListNode(cur.data, cur);
					insertRight(prevStacked, cur.up);
					
					//prevStacked becomes cur
					prevStacked = cur.up;
				}
				cur = cur.right;
			
			}
		}
		
		//Free reference
		prevStacked = null;

	}
	
	
	private void addLayer() {
		SkipListNode newHead = new SkipListNode(head.data, head);
		SkipListNode newTail = new SkipListNode(tail.data, tail);
		
		newHead.right = newTail;
		newTail.left = newHead;
		
		head = newHead;
		tail = newTail;
		
		height++;
		
	}
	
	private void removeLayer() {
		
		head = head.down;
		tail = tail.down;
		
		height--;
	}
	// Kind of confused how Java Garbage collection works, do I even need to flatten it before reBalanceing?
	private void flatten() {
		
		SkipListNode cur = head;
		
		while(cur.right != null) {
			cur.down = null;
			cur = cur.right;
		}
		cur.up = null;
		
		return;
		
		
	}
	
	protected T giveValue(SkipListNode n) {
		return n.data;
	}
	
	//Inserts dts, to the right of src and reassigns all nessesary pointers
	private void insertRight(SkipListNode src, SkipListNode dst) {
		dst.right = src.right;
		
		//Check for tail case
		//TODO the error was since src was equal to the bottom most tail node
		// Whereas tail is equal to the top most node
		src.right.left = dst;
		src.right = dst;
		dst.left = src;
		
		//TODO Implement node stacking 
	}
	//returns the floor Node of either needle if it exists or, where needle should go
	private SkipListNode locateNodeFloor(T needle) {
		
		SkipListNode cur = head;
		//Begin Search
		boolean search = true;
		
		//Never Check the head as it is '-inf'
		
		while(search) {
			//While the element to the right exists AND is less than 
			//Compare the element to the right to e.
			while(cur.right.data != tail.data && cur.right.data.compareTo(needle) <= 0) {
				cur = cur.right;
			}
			
			if(cur.down != null) {
				cur = cur.down;
			}else {
				search = false;
			}
		}
		
		// Search Completed
		return cur;

	}

	//Stacks and links a one additional node ontop of n
	private SkipListNode stackNode(SkipListNode n) {
		SkipListNode leftPointer = null;
		leftPointer = n.left;
		
		while(r.nextBoolean() && n.nodeHeight < height) {
			while(leftPointer.up == null) {
				leftPointer = leftPointer.left;
			}
			leftPointer = leftPointer.up;
			
			SkipListNode fatherN = new SkipListNode(n.data, n);
			insertRight(leftPointer, fatherN);
			
			n = fatherN;
		}
		
		return n;
	}
	
	//Unstacks and returns the base node
	private SkipListNode unStackNode(SkipListNode n) {
		
		//While its possible to go down, do so and remove all reference on the way
		while(n.down != null) {
			n.right = null;
			n.left = null;
			n = n.down;
			n.up = null;
		}
		
		return n;
	}
	
	private void setHeadHeight() {
//		head = unStackNode(head);
		
		for(int i = 1; i < height; i++) {
			head.up = new SkipListNode(head.data, head);
			head = head.up;
		}
		return;
	}
	
	//N exists in our set and needs to be removed 
	//N is also the node floor
	//All of these assumtions are handled in the prior methods
	private void removeNode(SkipListNode n) { 
		
		//Begin by decrementing size
		size--;
		
		//Potentially reSize the height
		if(size > 4) {
			int calcHeight= (int)(Math.log(size) / Math.log(2));
			//Rebalance should NOT be called in ADD, rather just compute the new height to 'add a new layer'
			while(calcHeight < height) {
				this.removeLayer();
			}
		}
		
		while(n != null) {
			//Relink the Neighbors
			n.left.right = n.right;
			n.right.left = n.left;
			
			//Move n up
			n = n.up;
			
		}
		return;
		
	}
	
	//TODO instead just add another height level 
	// Our Set by definition should not allow duplicates
	@Override
	public boolean add(T e) {
		// 'Return' Variable
		SkipListNode ret = new SkipListNode(e);
		//Increment Size
		size++;
		
		
		//Begin Search
		SkipListNode searchResult = locateNodeFloor(e);
		
		//Check if duplicate
		if(searchResult.data.equals(e)) {
			//insertion failed as it already exists
			size--;
			return false;
		}
		
		
		//insert right of SearchResult
		insertRight(searchResult, ret);
		stackNode(ret);
		
//		while(r.nextBoolean() == true && )
		
		//If the size is less than four then its just a linked list
		if(size > 4) {
			int calcHeight= (int)(Math.log(size) / Math.log(2));
			//Rebalance should NOT be called in ADD, rather just compute the new height to 'add a new layer'
			while(calcHeight > height) {
				this.addLayer();
			}
		}
				
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for(T cur : c) {
			this.add(cur);
		}
		return true;
	}

	@Override
	public void clear() {
		this.head = new SkipListNode((T)"-inf");;
		this.tail = new SkipListNode((T)"+inf");;
		
		
		size = 0;
		height = 1;
		
		r = new Random();
		
	}

	@Override
	public boolean contains(Object o) {
		
		if(!(o instanceof Comparable<?>)) {
			throw new ClassCastException();
		}
		
		T needle = (T) o;
		
		if(locateNodeFloor(needle).data.equals(needle)) {
			return true;
		}
		return false;
		
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		
		for(Object o : c) {
			if(this.contains(o) == false) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {

		if(size == 0)
			return true;
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		return new SkipListIterator(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if(size == 0) {
			return false;
		}
		//Check if given object implements comparable
		if(!(o instanceof Comparable<?>)) {
			throw new ClassCastException();
		}
		
		T needle = (T) o;
		SkipListNode needleNode = locateNodeFloor(needle);
		if(needleNode.data.equals(needle)) {
			//Remove Node
			this.removeNode(needleNode);
			return true;
		}
		//Wasn't found
		return false;
		
		
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		
		for(Object cur : c) {
			this.remove((T) cur);
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		c.removeIf(n -> (this.contains(n) == false));
		this.clear();
		this.addAll((Collection<? extends T>) c);
		return true;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public Object[] toArray() {
		Object [] ret =  new Object[size];
		Iterator<T> iter = this.iterator();
		for(int i = 0; i < size; i++) {
			ret[i] = iter.next();
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		
		if (a.length < size) { 
			  a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
			} else if (a.length > size) {
			  a[size] = null;
			}
		Iterator<T> iter = (Iterator<T>) this.iterator();
		for(int i = 0; i < size; i++) {
			a[i] = iter.next();
		}

		return a;
	}

	@Override
	public Comparator<? super T> comparator() {
		// TODO Auto-generated method stub
		return null;
	}

	//Returns Head.Floors.Right.data 
	@Override
	public T first() {
		return returnBottomMostNode(head).right.data;
	}

	@Override
	public SortedSet<T> headSet(T toElement) {
		throw new UnsupportedOperationException();
	}

	//Returns Tail.Floor.Left.Data
	@Override
	public T last() {
		return returnBottomMostNode(tail).left.data;
	}

	@Override
	public SortedSet<T> subSet(T fromElement, T toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedSet<T> tailSet(T fromElement) {
		throw new UnsupportedOperationException();
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object arg0) {
		if(this.hashCode() != arg0.hashCode()) {
			return false;
		}
		Set cur;
		if(arg0 instanceof Set) {
			cur = (Set) arg0;
		}else {
			return false;
		}
		
		Iterator<T> SLIter = this.iterator();
		for(T e: (Set<T>) cur) {
			if(!(e.equals(SLIter.next())) ) {
				return false;
			}
		}
		return true;
		
	}


	@Override
	public int hashCode() {
		int ret = 0;
		for(T cur : this) {
			ret += cur.hashCode();
		}
		return ret;
	}

}
