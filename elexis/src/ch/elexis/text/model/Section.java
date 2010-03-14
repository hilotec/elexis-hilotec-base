package ch.elexis.text.model;

public class Section extends Range {
	String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Section(String name, int start, int len){
		super(start,len);
		this.name=name;
	}
}
