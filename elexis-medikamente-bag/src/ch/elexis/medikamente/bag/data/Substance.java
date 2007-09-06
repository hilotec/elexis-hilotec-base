package ch.elexis.medikamente.bag.data;

import ch.elexis.data.PersistentObject;

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

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

}
