// $Id: VersionInfo.java 2976 2007-08-10 13:54:03Z rgw_ch $

package ch.rgw.tools;

/**
 * Einfaches API zum Vergleich von Versionsnummern des Typs maior.minor.rev
 * 
 * @author G. Weirich
 * @version 1.6.0
 */

public class VersionInfo implements Comparable<VersionInfo> {
	
	public static String Version(){
		return "1.6.0";
	}
	
	String orig;
	String[] spl;
	
	public VersionInfo(){
		this(Version());
	}
	
	public VersionInfo(final String v){
		if (StringTool.isNothing(v)) {
			orig = null;
			spl = null;
		} else {
			orig = v;
			spl = orig.split("\\.");
		}
	}
	
	public String maior(){
		if ((spl == null) || (spl.length < 1)) {
			return "0";
		}
		if (StringTool.isNothing(spl[0])) {
			return "0";
		}
		return spl[0];
	}
	
	public String minor(){
		if ((spl == null) || (spl.length < 2)) {
			return "0";
		}
		return spl[1];
	}
	
	public String rev(){
		if ((spl == null) || (spl.length < 3)) {
			return "0";
		}
		return spl[2];
	}
	
	public String version(){
		return orig;
	}
	
	public boolean isNewer(final String other){
		VersionInfo vo = new VersionInfo(other);
		return isNewer(vo);
	}
	
	public boolean isOlder(final String other){
		VersionInfo vn = new VersionInfo(other);
		return isOlder(vn);
	}
	
	/**
	 * Ist diese Version neuer, als die andere?
	 * 
	 * @param vo
	 *            die andere
	 * @return true:ja, false: nein
	 */
	public boolean isNewer(final VersionInfo vo){
		return (compareTo(vo) > 0);
	}
	
	public boolean isOlder(final VersionInfo vo){
		return (compareTo(vo) < 0);
	}
	
	public boolean isNewerMaior(final VersionInfo vo){
		return compareElem(this.maior(), vo.maior()) > 0;
	}
	
	public boolean isNewerMinor(final VersionInfo vo){
		if(isNewerMaior(vo)){
			return true;
		}
		if(isOlder(vo)){
			return false;
		}
		return compareElem(this.minor(), vo.minor()) > 0;
	}
	
	public boolean isNewerRev(final VersionInfo vo){
		return isNewerMaior(vo) ? true : isNewerMinor(vo) ? true
				: compareElem(this.rev(), vo.rev()) > 0;
	}
	
	public boolean isEqual(final VersionInfo vo){
		return (compareTo(vo) == 0);
	}
	
	public int compareTo(final VersionInfo vo){
		int c = compareElem(this.maior(), vo.maior());
		if (c != 0) {
			return c;
		}
		c = compareElem(this.minor(), vo.minor());
		if (c != 0) {
			return c;
		}
		return compareElem(this.rev(), vo.rev());
		
	}
	
	private int compareElem(final String a, final String b){
		int al = a.length();
		int bl = b.length();
		if (al == bl) {
			return a.compareToIgnoreCase(b);
		}
		int diff = Math.abs(al - bl);
		String x = StringTool.pad(StringTool.LEFT, '0', a, al + diff + 1);
		String y = StringTool.pad(StringTool.LEFT, '0', b, al + diff + 1);
		return x.compareToIgnoreCase(y);
	}
}