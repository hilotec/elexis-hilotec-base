package ch.elexis.omnivore.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.elexis.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.services.IDocumentManager;
import ch.elexis.text.IDocument;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.RegexpFilter;
import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;

public class DocumentManagement implements IDocumentManager {

	@Override
	public boolean addCategorie(String categorie) {
		return false;
	}

	public String addDocument(IDocument doc) throws ElexisException{
		DocHandle dh=new DocHandle(doc.getContentsAsBytes(),doc.getPatient(),doc.getTitle(),doc.getMimeType(),doc.getKeywords());
		return dh.getId();
	}
	/*
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
*/
	/*
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
*/
	@Override
	public String[] getCategories() {
		return null;
	}

	@Override
	public InputStream getDocument(String id) {
		DocHandle dh = DocHandle.load(id);
		return dh.getContentsAsStream();
	}

	@Override
	public List<IDocument> listDocuments(final Patient pat, final String categoryMatch,
			final String titleMatch, final String keywordMatch, final TimeSpan dateMatch,
			final String contentsMatch) throws ElexisException {
		Query<DocHandle> qbe=new Query<DocHandle>(DocHandle.class);
		if(pat!=null){
			qbe.add("PatID", Query.EQUALS, pat.getId());
		}
		if(dateMatch!=null){
			String from=dateMatch.from.toString(TimeTool.DATE_COMPACT);
			String until=dateMatch.until.toString(TimeTool.DATE_COMPACT);
			qbe.add("Datum", Query.GREATER_OR_EQUAL, from);
			qbe.add("Datum", Query.LESS_OR_EQUAL, until);
		}
		if(titleMatch!=null){
			if(titleMatch.matches("/.+/")){
				qbe.addPostQueryFilter(new RegexpFilter(titleMatch.substring(1, titleMatch.length()-1)));
			}else{
				qbe.add("Titel", Query.EQUALS, titleMatch);
			}
		}
		if(keywordMatch!=null){
			if(keywordMatch.matches("/.+/")){
				qbe.addPostQueryFilter(new RegexpFilter(keywordMatch.substring(1, keywordMatch.length()-1)));
			}else{
				qbe.add("Keywords", Query.LIKE, "%"+keywordMatch+"%");
			}
		}
		/*
		if(categoryMatch!=null){
			if(categoryMatch.matches("/.+/")){
				qbe.addPostQueryFilter(new RegexpFilter(categoryMatch.substring(1, categoryMatch.length()-1)));
			}else{
				qbe.add("Category", Query.EQUALS, titleMatch);
			}
		}
		*/
		if(contentsMatch!=null){
			throw new ElexisException(getClass(), "ContentsMatch not supported", ElexisException.EE_NOT_SUPPORTED);
		}
		List<DocHandle> dox=qbe.execute();
		ArrayList<IDocument> ret=new ArrayList<IDocument>(dox.size());
		for(DocHandle doc:dox){
			ret.add(doc);
		}
		return ret;
	}
}
