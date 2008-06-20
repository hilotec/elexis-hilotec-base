package ch.elexis.scripting;

import java.util.ArrayList;
import java.util.Collections;

public class Counter {
	private ArrayList<Float> list=new ArrayList<Float>();
	
	public void add(float i){
		list.add(new Float(i));
	}
	
	public void clear(){
		list.clear();
	}
	
	public float getAverage(int digits){
		float sum=0;
		for(Float in:list){
			sum+=in;
		}
		float multiplyer=(float)Math.pow(10.0, digits);
		return Math.round(multiplyer*sum/list.size())/multiplyer;
	}
	
	public float getMedian(){
		Collections.sort(list);
		int size=list.size();
		int center=size>>1;
		float f1=list.get(center);
		if((size&1)==0){
			float f2=list.get(center+1);
			return (f1+f2)/2;
		}
		return f1;
	}
}
