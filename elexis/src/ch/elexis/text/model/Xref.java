package ch.elexis.text.model;


public class Xref extends Range implements IXref{
	String provider;
	String id;
	public Xref(int pos, int len, String provider, String id){
		super(pos,len);
		this.provider=provider;
		this.id=id;
	}
	@Override
	public String getID() {
		return id;
	}
	@Override
	public String getName() {
		return "";
	}
	@Override
	public String getProvider() {
		return provider;
	}
}
