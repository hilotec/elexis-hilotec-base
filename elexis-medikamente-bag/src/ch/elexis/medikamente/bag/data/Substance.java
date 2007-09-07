package ch.elexis.medikamente.bag.data;

import java.util.List;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

public class Substance extends PersistentObject {
	static final String TABLENAME="CH_ELEXIS_MEDIKAMENTE_BAG_SUBSTANCE";
	static final String VERSION="0.1.0";
	static final String createDB="CREATE TABLE "+TABLENAME+"("
		+"ID		VARCHAR(25) primary key,"
		+"deleted	CHAR(1) default '0',"
		+"group		VARCHAR(7),"				// therap. gruppe
		+"name		VARCHAR(80)"
	+");"
	+"CREATE INDEX CEMBS1 ON "+TABLENAME+" (group);"
	+"CREATE INDEX CEMBS2 ON "+TABLENAME+" (name);"
	+"INSERT INTO "+TABLENAME+" (ID,name) VALUES ('VERSION','"+VERSION+"');";
	
	static{
		addMapping(TABLENAME,"name","group");
	}
	@Override
	public String getLabel() {
		return get("name");
	}

	public Substance(final String name, final String group){
		create(null);
		set(new String[]{"name","group"},name,group);
	}
	
		
	public List<Substance> sameGroup(){
		return allFromGroup(get("group"));
	}
	
	public static Substance find(final String name){
		String id=new Query<Substance>(Substance.class).findSingle("name", "=", name);
		if(id!=null){
			return load(id);
		}
		return null;
	}
	
	public static List<Substance> allFromGroup(final String group){
		return new Query<Substance>(Substance.class,"group",group).execute();
		
	}
	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	public static Substance load(final String id){
		return new Substance(id);
	}
	protected Substance(){}
	protected Substance(final String id){
		super(id);
	}
}
