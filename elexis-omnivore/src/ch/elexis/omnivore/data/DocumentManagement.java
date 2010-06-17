package ch.elexis.omnivore.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import ch.elexis.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.services.IDocumentManager;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public class DocumentManagement implements IDocumentManager {

	@Override
	public boolean addCategorie(String categorie) {
		return false;
	}

	@Override
	public String addDocument(Patient pat, InputStream is, String name,
			String category, String keywords, String date)
			throws ElexisException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileTool.copyStreams(is, baos);
			DocHandle dh = new DocHandle(baos.toByteArray(), pat, name, name,
					keywords);
			if (date != null) {
				dh.set("Datum", date);
			}
			return dh.getId();
		} catch (Exception ex) {
			throw new ElexisException(this.getClass(), ex.getMessage(), 1);
		}
	}

	@Override
	public boolean addDocument(Patient pat, String name, String catecory,
			String keywords, File file, String date) {
		try {
			FileInputStream fis = new FileInputStream(file);
			addDocument(pat, fis, name, null, keywords, date);
			return true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}

	@Override
	public String[] getCategories() {
		return null;
	}

	@Override
	public InputStream getDocument(String id) {
		DocHandle dh = DocHandle.load(id);
		byte[] cnt = dh.getContents();
		ByteArrayInputStream bais = new ByteArrayInputStream(cnt);
		return bais;
	}

}
