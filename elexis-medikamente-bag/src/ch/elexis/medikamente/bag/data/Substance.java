package ch.elexis.medikamente.bag.data;

import java.util.ArrayList;
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
		+"name		VARCHAR(127)"
	+");"
	+"CREATE INDEX CEMBS1 ON "+TABLENAME+" (gruppe);"
	+"CREATE INDEX CEMBS2 ON "+TABLENAME+" (name);"
	+"INSERT INTO "+TABLENAME+" (ID,name) VALUES ('VERSION','"+VERSION+"');";
	
	final static String INTERAKTTABLE="CH_ELEXIS_MEDIKAMENTE_BAG_INTERACTIONS";
	static final String createInteract="CREATE TABLE "+INTERAKTTABLE+" ("
		+"ID			VARCHAR(25)	primary key,"
		+"Subst1		VARCHAR(25),"
		+"Subst2		VARCHAR(25),"
		+"Type			VARCHAR(20),"
		+"Severity			CHAR(1),"
		+"Description		TEXT);"
		+"CREATE INDEX CEMBI1 ON "+INTERAKTTABLE+" (Subst1);"
		+"CREATE INDEX CEMBI2 ON "+INTERAKTTABLE+" (Subst2);";
	
	static{
		addMapping(TABLENAME,"name","gruppe","medis=JOINT:product:substance:"+BAGMedi.JOINTTABLE,
				"interactions=JOINT:Subst1:Subst2:"+INTERAKTTABLE);
		Substance v=load("VERSION");
		if(v.state()<PersistentObject.DELETED){
			createTable("Substance", createDB);
			createTable("Interactions",createInteract);
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
	
	public List<Interaction> getInteractions(){
		List<String[]> in=getList("interactions",new String[]{"Type","Severity","Description"});
		List<Interaction> ret=new ArrayList<Interaction>(in.size());
		for(String[] line:in){
			Substance other=Substance.load(line[0]);
			ret.add(new Interaction(other,line[3],line[1],Integer.parseInt(line[2])));
		}
		return ret;
	}
	
	public void addInteraction(final Interaction iac){
		addToList("interactions", iac.subst.getId(), "Type="+iac.type, "Severity="+Integer.toString(iac.severity),"Description="+iac.description);
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
	
	public static class Interaction{
		Substance subst;
		String type;
		String description;
		int severity;
		Interaction(final Substance s, final String desc, final String t, final int sev){
			subst=s;
			description=desc;
			type=t;
			severity=sev;
		}
	}
}
