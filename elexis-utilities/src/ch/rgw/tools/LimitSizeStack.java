package ch.rgw.tools;

import java.util.LinkedList;

@SuppressWarnings("serial")
public class LimitSizeStack<T> extends LinkedList<T> {
	private int max;
	
	public LimitSizeStack(int limit){
		max = limit;
	}
	
	public void push(T elem){
		if (size() >= max) {
			remove(size()-1);
		}
		add(0, elem);
	}
	
	public T pop(){
		if (size() == 0) {
			return null;
		}
		return remove(0);
	}
}
