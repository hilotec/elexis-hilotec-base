package ch.elexis.dialogs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.jdom.Element;

import ch.elexis.Desk;
import ch.elexis.exchange.IExchangeContributor;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.util.SWTHelper;

public class XChangeSelectDialog extends TitleAreaDialog {
	XChangeContainer container; 
	HashMap<Element, IExchangeContributor.UserChoice> choices;
	
	public XChangeSelectDialog(Shell shell, XChangeContainer container, 
			HashMap<Element, IExchangeContributor.UserChoice> choices){
		super(shell);
		this.container=container;
		this.choices=choices;
	}

	

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=(Composite) super.createDialogArea(parent);
		ContainerCheckedTreeViewer cctv=new ContainerCheckedTreeViewer(ret);
		cctv.setAllChecked(true);
		cctv.setContentProvider(new XcdContentProvider());
		cctv.setLabelProvider(new XcdLabelProvider());
		cctv.setInput(container.getRoot());
		cctv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cctv.setAllChecked(true);
		return ret;
	}



	@Override
	public void create() {
		super.create();
		setTitle("Daten zum Export bereitstellen");
		setMessage("Bitte wählen Sie die Elemente aus, die exportiert werden sollen");
		getShell().setText("Elexis: Sgam.xChange");
		Rectangle max=Desk.getDisplay().getBounds();
		getShell().setBounds(max);
	}

	class XcdContentProvider implements ITreeContentProvider{
		
		public Object[] getChildren(Object parentElement) {
			List<Element> ret=new LinkedList<Element>();
			Element parent=(Element)parentElement;
			for(Object el:parent.getChildren()){
				if(choices.get(el)!=null){
					ret.add((Element)el);
				}
			}
			return ret.toArray();
		}

		public Object getParent(Object element) {
			if(element instanceof Element){
				Element el=(Element)element;
				return el.getParent();
			}else{
				return null;
			}
		}

		public boolean hasChildren(Object element) {
			Object[] ch=getChildren(element);
			return ch.length!=0;
		}

		public Object[] getElements(Object inputElement) {
			Element root=(Element)inputElement;
			List<ContactElement> ret=new LinkedList<ContactElement>();
			for(ContactElement ce:container.getContactElements()){
				if(choices.get(ce)!=null){
					ret.add(ce);
				}
			}
			return ret.toArray();
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
		}
		
	}
	class XcdLabelProvider extends LabelProvider{
	
		public String getText(Object element) {
			if(element instanceof Element){
				IExchangeContributor.UserChoice uc=choices.get(element);
				if(uc!=null){
					return uc.getTitle();
				}
			}
			return null;
		}

				
	}
}
