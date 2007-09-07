package ch.elexis.medikamente.bag.data;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.VersionInfo;

public class Substance extends PersistentObject {
	static final String TABLENAME="CH_ELEXIS_MEDIKAMENTE_BAG_SUBSTANCE";
	static final String VERSION="0.1.0";
	static final String createDB="CREATE TABLE "+TABLENAME+"("
		+"ID		VARCHAR(25) primary key,"
		+"deleted	CHAR(1) default '0',"
		+"gruppe	VARCHAR(7),"				// therap. gruppe
		+"name		VARCHAR(80)"
	+");"
	+"CREATE INDEX CEMBS1 ON "+TABLENAME+" (gruppe);"
	+"CREATE INDEX CEMBS2 ON "+TABLENAME+" (name);"
	+"INSERT INTO "+TABLENAME+" (ID,name) VALUES ('VERSION','"+VERSION+"');";
	
	static{
		addMapping(TABLENAME,"name","gruppe","medis=JOINT:product:substance:"+BAGMedi.JOINTTABLE);
		Substance v=load("VERSION");
		if(v.state()<PersistentObject.DELETED){
			createTable("Substance", createDB);
		}else{
			VersionInfo vi=new VersionInfo(v.get("name"));
			if(vi.isOlder(VERSION)){
				SWTHelper.showError("Datenbank Fehler", "Tabelle Substance ist zu alt");
			}
		}
	}
	@Override
	public String getLabel() {
		return get("name");
	}

	public Substance(final String name, final String group){
		create(null);
		set(new String[]{"name","gruppe"},name,group);
	}
	
	public SortedSet<BAGMedi> findMedis(SortedSet<BAGMedi> list){
		if(list==null){
			list=new TreeSet<BAGMedi>();
		}	
		List<String[]> lMedis=getList("medis", new String[0]);
		for(String[] r:lMedis){
			BAGMedi bm=BAGMedi.load(r[0]);
			list.add(bm);
		}
		return list;
	}
	public List<Substance> sameGroup(){
		return allFromGroup(get("gruppe"));
	}
	
	public static Substance find(final String name){
		String id=new Query<Substance>(Substance.class).findSingle("name", "=", name);
		if(id!=null){
			return load(id);
		}
		return null;
	}
	
	public static List<Substance> allFromGroup(final String group){
		return new Query<Substance>(Substance.class,"gruppe",group).execute();
		
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
