package ch.elexis.text;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

public abstract class MimePart {
	private String mimetype;
	protected Object data;
	public MimePart(String type, Object data){
		mimetype=type;
		this.data=data;
	}
	public String getMimeType(){
		return mimetype;
	}
	
	public abstract byte[] getData();
	
	public static class Binary extends MimePart{
		public Binary(String type,byte[] data){
			super(type,data);
		}

		@Override
		public byte[] getData() {
			return (byte[])data;
		}
		
	}
	public static class Img extends MimePart{
		public Img(String type, Image image){
			super(type,image.getImageData());
		}
		public byte[] getData(){
			ImageData imd=(ImageData)data;
			return imd.data;
		}
	}
	
}
