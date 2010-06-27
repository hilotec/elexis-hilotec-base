package ch.elexis.text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ch.elexis.ElexisException;
import ch.elexis.data.Patient;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public class FileDocument implements IDocument {
	String title;
	String category;
	File file;
	String date;
	Patient pat;
	String keywords;

	public FileDocument(Patient pat, String title, String category, File file,
			String date, String keywords) {
		this.title = title;
		this.category = category;
		this.file = file;
		this.date = date;
		this.pat = pat;
		this.keywords = keywords;
	}
	
	public void delete(){
		file.delete();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getMimeType() {
		return "binary/octet-stream";
	}

	@Override
	public InputStream getContentsAsStream() throws ElexisException {
		try {
			FileInputStream fis = new FileInputStream(file);
			return fis;
		} catch (FileNotFoundException e) {
			ExHandler.handle(e);
			throw new ElexisException(getClass(), e.getMessage(),
					ElexisException.EE_FILE_ERROR);
		}
	}

	public byte[] getContentsAsBytes() throws ElexisException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			FileTool.copyStreams(getContentsAsStream(), baos);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new ElexisException(getClass(), e.getMessage(),
					ElexisException.EE_FILE_ERROR);
		}

	}

	@Override
	public String getKeywords() {
		return keywords;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getCreationDate() {
		return date;
	}

	@Override
	public Patient getPatient() {
		return pat;
	}

}
