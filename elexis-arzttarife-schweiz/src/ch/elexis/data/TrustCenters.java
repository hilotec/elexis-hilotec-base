package ch.elexis.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrustCenters {
	public static final int CTESIAS=51;
	public static final int GALLONET=52;
	public static final int HAWATRUST=53;
	public static final int MEDKEY=54;
	public static final int PONTENOVA=55;
	public static final int SYNDATA=55;
	public static final int TC_AARGAU=57;
	public static final int THURCARE=58;
	public static final int TC_TICINO=59;
	public static final int ZUERIDOC=60;
	public static final int TRUSTMED=61;
	public static final int TC_TEST=69;
	
	public static List<String> getTCList(){
		ArrayList<String> list=new ArrayList<String>(tc.size());
		for(String o:tc.keySet()){
			list.add(o);
		}
		return list;
	}
	
	public static final HashMap<String, Integer> tc=new HashMap<String, Integer>();
	static{
		tc.put("Ctésias", CTESIAS); //$NON-NLS-1$
		tc.put("GallOnet", GALLONET); //$NON-NLS-1$
		tc.put("hawatrust", HAWATRUST); //$NON-NLS-1$
		tc.put("+medkey", MEDKEY); //$NON-NLS-1$
		tc.put("PonteNova", PONTENOVA); //$NON-NLS-1$
		tc.put("syndata", SYNDATA); //$NON-NLS-1$
		tc.put("TC Aargau", TC_AARGAU); //$NON-NLS-1$
		tc.put("thurcare", THURCARE); //$NON-NLS-1$
		tc.put("TC Ticino", TC_TICINO); //$NON-NLS-1$
		tc.put("TC züridoc", ZUERIDOC); //$NON-NLS-1$
		tc.put("trustmed", TRUSTMED); //$NON-NLS-1$
		tc.put("TC test", TC_TEST); //$NON-NLS-1$
		
	}
	/*
	Ctésias 51 71
	GallOnet 52 72
	hawatrust 53 73
	+medkey 54 74
	PonteNova 55 75
	syndata 56 76
	TC Aargau 57 77
	thurcare 58 78
	TC Ticiono 59 79
	TC züridoc 60 80
	trustmed 61 81
	TC test 69 89
	*/
}