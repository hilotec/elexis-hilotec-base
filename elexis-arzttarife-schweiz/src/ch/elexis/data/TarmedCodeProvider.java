package ch.elexis.data;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.ViewerConfigurer.CommonContentProvider;
import ch.rgw.tools.LazyTree;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.LazyTree.LazyTreeListener;

public class TarmedCodeProvider implements ITreeContentProvider,
		CommonContentProvider {

	boolean bExpanded;
	LazyTree<TarmedLeistung> root;
	CommonViewer cv;
	Query<TarmedLeistung> qbe=new Query<TarmedLeistung>(TarmedLeistung.class);
	TreeListener ltl=new TreeListener();
	static final String parentColumn="Parent";
	static final TimeTool today=new TimeTool();
	final Filter filter=new Filter();
	private String code="";
	private String text="";
	
	public TarmedCodeProvider(CommonViewer mine){
		root=new LazyTree<TarmedLeistung>(null,null,ltl);
		qbe.add(parentColumn, "=", "NIL");
		qbe.orderBy(true, "ID");
		for(TarmedLeistung tl:qbe.execute()){
			new LazyTree<TarmedLeistung>(root,tl,ltl);
		}
		cv=mine;
	}
	public Object[] getChildren(Object parentElement) {
	   if(parentElement instanceof LazyTree){
            LazyTree tr = (LazyTree) parentElement;
            return tr.getChildren().toArray();
        }
        return null;
	}

	public Object getParent(Object element) {
	   if(element instanceof LazyTree){
            LazyTree tr = (LazyTree) element;
            return tr.getParent();
        }
        return null;
	}

	public boolean hasChildren(Object element) {
		if(element instanceof LazyTree){
			LazyTree tr=(LazyTree)element;
			qbe.clear();
			TarmedLeistung tl=(TarmedLeistung)tr.contents;
			return (qbe.findSingle(parentColumn, "=", tl.getId())!=null);
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return root.getChildren().toArray();
	}

	public void dispose() {
		root=null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

	public void startListening() {
		cv.getViewerWidget().addFilter(filter);
		cv.getConfigurer().getControlFieldProvider().addChangeListener(this);

	}

	public void stopListening() {
		cv.getViewerWidget().removeFilter(filter);
		cv.getConfigurer().getControlFieldProvider().removeChangeListener(this);
	}

	public void changed(String[] fields, String[] values) {
		if(!code.equals(values[0])){
			code=values[0];
			if(code.matches("[0-9][0-9]\\.[0-9]{4,4}")){
				filter.setCode(values[0]);
			}else{
				filter.setCode(null);
			}
			cv.notify(CommonViewer.Message.update_keeplabels);
		}
		if(!text.equals(values[1])){
			text=values[1];
			if(text.length()==0){
				filter.setName(null);
				if(bExpanded){
					((TreeViewer)cv.getViewerWidget()).collapseAll();
					bExpanded=false;
				}
			}else{
				filter.setName(values[1]);
				if(!bExpanded){
					((TreeViewer)cv.getViewerWidget()).expandAll();
					bExpanded=true;
				}
			}
			cv.notify(CommonViewer.Message.update_keeplabels);
		}
	}

	public void reorder(String field) {
		// TODO Auto-generated method stub

	}

	public void selected() {
		// TODO Auto-generated method stub

	}

	class TreeListener implements LazyTreeListener{

		public void fetchChildren(LazyTree l) {
			 qbe.clear();
			 TarmedLeistung tl=(TarmedLeistung) l.contents;
			 if(tl!=null){
				 qbe.add(parentColumn,"=",tl.getId()); //$NON-NLS-1$
				 qbe.orderBy(true, "ID");
				 for(TarmedLeistung tlc:qbe.execute()){
					 new LazyTree<TarmedLeistung>(l,tlc,ltl);
				 }
			 }
		}

		public boolean hasChildren(LazyTree l) {
			qbe.clear();
			 TarmedLeistung tl=(TarmedLeistung) l.contents;
			return qbe.findSingle(parentColumn, "=", tl.getId())!=null;
		}
		
	}
	private static class Filter extends ViewerFilter{
		TimeTool from=new TimeTool();
		TimeTool until=new TimeTool();
		String code;
		String name;
		
		void setCode(String code){
			this.code=code;
		}
		void setName(String name){
			this.name = name==null ? null : name.toLowerCase();
		}
		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if(element instanceof LazyTree){
				LazyTree lt=(LazyTree)element;
				TarmedLeistung tl=(TarmedLeistung)lt.contents;
				if(code!=null){
					LazyTree runner=(LazyTree)lt.getFirstChild();
					while(runner!=null){
						if(select(viewer,lt,runner)){
							return true;
						}
						runner=(LazyTree)runner.getNextSibling();
					}
					if(!tl.getCode().equals(code)){
						return false;
					}
				}
				if(name!=null){
					if(lt.hasChildren()){
						LazyTree runner=(LazyTree)lt.getFirstChild();
						while(runner!=null){
							if(select(viewer,lt,runner)){
								return true;
							}
							runner=(LazyTree)runner.getNextSibling();
						}
					}
					if(!tl.getText().toLowerCase().contains(name)){
						return false;
					}
				}
				if(lt.contents instanceof TarmedLeistung){
					if(from.set(tl.get("GueltigVon"))){
						if(from.isAfter(today)){
							return false;
						}
					}
					if(until.set(tl.get("GueltigBis"))){
						if(until.isBefore(today)){
							return false;
						}
					}
					
					return true;
				}
			}
			return false;
		}
	
	}
}
