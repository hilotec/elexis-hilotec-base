package ch.elexis;

public class ElexisException extends Exception {
	private static final long serialVersionUID = -4535064606049686854L;

	public static final int EE_DUPLICATE_DISPATCHER=1;
	public static final int EE_BAD_DISPATCHER = 2;
	
	Class<?> clazz;
	int errcode;
	public ElexisException(Class<?> clazz, String errmsg, int errcode){
		super(errmsg);
		this.clazz=clazz;
		this.errcode=errcode;
	}
	
	public Class<?> getThrowingClass(){
		return clazz;
	}
	
	public int getErrCode(){
		return errcode;
	}
}
