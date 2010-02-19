package ch.elexis.text.model;

import ch.elexis.exchange.text.IRange;

public class Section implements IRange {
	private int len;
	private int pos;
	private String name;
	
	public Section(String name, int pos, int len){
		this.name=name;
		this.pos=pos;
		this.len=len;
	}
	@Override
	public int getLength(){
		return len;
	}
	
	@Override
	public int getPosition(){
		return pos;
	}
	
	public String getName(){
		return name;
	}
	
	public void setLength(int l){
		len=l;
	}
	
	public void setPosition(int pos){
		this.pos=pos;
	}
	
	public void setName(String name){
		this.name=name;
	}
}
