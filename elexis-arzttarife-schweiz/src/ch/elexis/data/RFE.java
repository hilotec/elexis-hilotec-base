package ch.elexis.data;


public class RFE extends PersistentObject {
	public static final String Version="0.1.0";
	private static final String TABLENAME="ch_elexis_arzttarif_ch_rfe";
	private static final String createDB=
		"CREATE TABLE "+TABLENAME+" ("+
		"ID VARCHAR(25) primary key,"+
		"type CHAR(2),"+
		"konsID VARCHAR(25)"+
		");"+
		"CREATE INDEX "+TABLENAME+"_idx ON "+TABLENAME+" (konsID);"+
		"INSERT INTO "+TABLENAME+" (ID, konsID) VALUES ('VERSION','"+Version+"');";
	
	static final String[][] rfe = {
			{ "01", "Kontakt auf wunsch des Patienten" },
			{ "02", "Notfallkonsultation" },
			{ "03", "Kontakt auf Zuweisung" },
			{ "04", "Folgekontakt auf Verordnung/Empfehlung" },
			{ "05", "Folgekontakt wegen auswärtiger Hämatologie und Chemie" },
			{ "06", "Kontakt in Zusammenhang mit Langzeitpflege" },
			{ "07",
					"Kontakt in kausalem Zusammenhang mit Eingriff / Hospitalisation" },
			{ "99", "Kein Arztkontakt" }

	};

	static{
		addMapping(TABLENAME,"type","konsID");
		RFE version=load("VERSION");
		if(!version.exists()){
			createOrModifyTable(createDB);
		}
		
	}
	
	public static String[] getRFETexts(){
		String[] ret=new String[rfe.length];
		for(int i=0;i<rfe.length;i++){
			ret[i]=rfe[i][1];
		}
		return ret;
	}
	public static String[][] getRFEDescriptions() {
		return rfe;
	}

	public String getText(){
		String code=getCode();
		for(String[] line:rfe){
			if(line[0].equals(code)){
				return line[1];
			}
		}
		return "?";
	}
	
	public static RFE[] getRfeForKons(String konsID){
		Query<RFE> qbe=new Query<RFE>(RFE.class);
		qbe.add("konsID", Query.EQUALS, konsID);
		return qbe.execute().toArray(new RFE[0]);
	}
	public String getCode(){
		return checkNull(get("type"));
	}
	
	public Konsultation getKons(){
		return Konsultation.load(get("konsID"));
	}
	@Override
	public String getLabel() {
		return getKons().getLabel()+" : "+getText();
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	public static RFE load(String id){
		return new RFE(id);
	}
	
	protected RFE(String id){
		super(id);
	}
	
	protected RFE(){
		
	}
}
