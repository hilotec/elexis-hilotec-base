package ch.rgw.tools;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class LimitSizeStack<T> extends ArrayList<T> {
	private int max;
	
	public LimitSizeStack(int limit){
		super(limit);
		max = limit;
	}
	
	public void push(T elem){
		if (size() >= max) {
			remove(size());
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
