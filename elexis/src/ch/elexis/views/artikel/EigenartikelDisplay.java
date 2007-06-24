package ch.elexis.views.artikel;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.widgets.*;

import ch.elexis.Desk;
import ch.elexis.data.Eigenartikel;
import ch.elexis.util.LabeledInputField;
import ch.elexis.views.IDetailDisplay;

public class EigenartikelDisplay implements IDetailDisplay {
	
	
	FormToolkit tk=Desk.theToolkit;
	ScrolledForm form;
	LabeledInputField.AutoForm tblArtikel;

	public Composite createDisplay(Composite parent, IViewSite site) {
		parent.setLayout(new FillLayout());
		form=tk.createScrolledForm(parent);
		Composite ret=form.getBody();
		TableWrapLayout twl=new TableWrapLayout();
		ret.setLayout(twl);
		tblArtikel=new LabeledInputField.AutoForm(ret,Artikeldetail.getFieldDefs(parent.getShell()));
        
        TableWrapData twd=new TableWrapData(TableWrapData.FILL_GRAB);
        twd.grabHorizontal=true;
        tblArtikel.setLayoutData(twd);
   		return ret;

	}

	public Class getElementClass() {
		return Eigenartikel.class;
	}

	public void display(Object obj) {
		if(obj instanceof Eigenartikel){
			Eigenartikel m=(Eigenartikel)obj;
			form.setText(m.getLabel());
			tblArtikel.reload(m);
		}

	}

	public String getTitle() {
		return "Eigenartikel";
	}

}
