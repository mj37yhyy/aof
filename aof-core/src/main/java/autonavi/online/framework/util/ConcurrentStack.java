package autonavi.online.framework.util;

import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentStack<E> {
	AtomicReference<Node<E>> top = new AtomicReference<Node<E>>();

	private int size;
	private AtomicReference<Node<E>> head = new AtomicReference<Node<E>>();

	public ConcurrentStack() {
	}

	public void push(E item) {
		Node<E> node = new Node<E>(item);
		Node<E> prev;
		do {
			prev = head.get();
			node.next = prev;
		} while (!head.compareAndSet(prev, node));
		++size;
	}

	public E pop() {
		if (size == 0) {
			throw new IllegalStateException("Empty Stack");
		}

		Node<E> newHead;
		Node<E> oldHead;
		do {
			oldHead = head.get();
			if (oldHead == null) {
				return null;
			}
			newHead = oldHead.next;
		} while (!head.compareAndSet(oldHead, newHead));
		--size;
		return oldHead.item;
	}

	public int size() {
		return size;
	}

	private static class Node<E> {
		public final E item;
		public Node<E> next;

		public Node(E item) {
			this.item = item;
		}
	}

	/**
	 * Test routine (used for CalFuzzer
	 * http://srl.cs.berkeley.edu/~ksen/calfuzzer/)
	 * 
	 * @author Gidon Ernst <ernst@informatik.uni-augsburg.de>
	 * 
	 *         Instructions:
	 * 
	 *         $ cd calfuzzer
	 * 
	 *         save this file in test/benchmarks/testcases/ConcurrentStack.java
	 * 
	 *         add to run.xml
	 * 
	 *         <target name="concurrentstack"> <echo message="unknown?"/>
	 *         <property name="javato.work.dir" value="${benchdir}"/> <property
	 *         name="javato.app.main.class"
	 *         value="benchmarks.testcases.ConcurrentStack"/> <antcall
	 *         target="deadlock-analysis"/> <antcall target="race-analysis"/>
	 *         </target>
	 * 
	 *         $ javac test/benchmarks/testcases/ConcurrentStack.java -d classes
	 * 
	 *         $ ant -f run.xml concurrentstack
	 * 
	 *         it should report two data races (which are in fact benign,
	 *         Treiber's stack has been proved linearizable)
	 */
	public static void main(String[] args) {
		// final ConcurrentStack<Integer> stack = new
		// ConcurrentStack<Integer>();

		/* producer thread */
		/*
		 * new Thread() { public void run() { Random random = new Random();
		 * bounded loops, since the analyzer actually runs this code for(int
		 * i=0; i<10; i++) { stack.push(random.nextInt()); } } }.start();
		 * 
		 * consumer thread new Thread() { public void run() { for(int i=0; i<10;
		 * i++) { stack.pop(); } } }.start();
		 */

		ConcurrentStack<Integer> stack = new ConcurrentStack<Integer>();
		stack.push(10);
		System.out.println("size = " + stack.size());
		stack.push(20);
		System.out.println("size = " + stack.size());
		System.out.println(stack.pop());
		System.out.println(stack.pop());
		System.out.println("size = " + stack.size());
	}
}
