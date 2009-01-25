package ch.elexis.medikamente.bag.views;

import java.util.HashMap;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.medikamente.bag.data.Substance;
import ch.elexis.util.viewers.FieldDescriptor;
import ch.elexis.util.viewers.SelectorPanelProvider;

public class BagMediControlFieldProvider extends SelectorPanelProvider {
	BAGMediSelector mine;
	
	public BagMediControlFieldProvider(BAGMediSelector bsl, FieldDescriptor<? extends PersistentObject>[] fields,
		boolean exlusive){
		super(fields, exlusive);
		mine=bsl;
	}

	@Override
	public void setQuery(Query<? extends PersistentObject> q){
		/*
		HashMap<String, String> values=getPanel().getValues();
		String name=values.get(mine.FIELD_NAME);
		String subst=values.get(mine.FIELD_SUBSTANCE);
		String notes=values.get(mine.FIELD_NOTES);
		if(name.length()>0){
			q.add(BAGMedi.NAME, "LIKE", name+"%");
		}else if(subst.length()>0){
			StringBuilder sql=new StringBuilder();
			sql.append("SELECT m.product FROM ")
				.append(BAGMedi.JOINTTABLE)
				.append(" m, ")
				.append(Substance.TABLENAME)
				.append(" s WHERE m.Substance=s.ID AND s.name LIKE ")
				.append(subst).append(";");
		
		}
*/
	}
	
	
}
