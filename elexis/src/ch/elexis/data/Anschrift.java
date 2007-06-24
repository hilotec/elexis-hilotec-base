/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Anschrift.java 2618 2007-06-24 10:08:05Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import ch.rgw.tools.StringTool;


public class Anschrift{
	String Strasse,Plz,Ort,Land;
    String[] fields={"Strasse","Plz","Ort","Land"};	
    Kontakt mine;
    
    public Anschrift(Kontakt k)
    {
    	mine=k;
        String[] values=new String[fields.length];
        k.get(fields,values);
        Strasse=values[0];
        Plz=values[1];
        Ort=values[2];
        Land=values[3];
    }
    public Anschrift(){}
    /** 
     * Eine Etikette der Anschrift liefern
     * @param withName TODO
     * @param multiline Wenn true wird die Etikette mehrzeilig, sonst einzeilig
     */
    public String getEtikette(boolean withName, boolean multiline){
        String sep="\n";
        if(multiline==false){
            sep=", ";
        }
        StringBuilder ret=new StringBuilder(100);
        if(withName==true){
        	ret.append(mine.getLabel(false)).append(sep);
        }
        if(Strasse!=null){
            ret.append(Strasse).append(sep);
        }
        if(!StringTool.isNothing(Land)){
        	ret.append(Land).append(" - ");
        }
        if((Plz!=null) && (Ort!=null)){
            ret.append(Plz).append(" ").append(Ort);
        }
        if (multiline) {
        	// append trailing newline
        	ret.append("\n");
        }
        return ret.toString();
    }
    public String getLabel(){
    	return getEtikette(true, false);
    }
	public String getStrasse(){
	    return Strasse==null?"":Strasse;
    }
	public String getPlz(){
	    return Plz==null ? "" : Plz;
    }
    public String getOrt(){
        return Ort==null?"":Ort;
    }
    public String getLand(){
        return Land==null ? "" :  Land;
    }
 	public void setStrasse(String s){
 		Strasse=s;
 	}
 	public void setPlz(String plz){
 		Plz=plz.length()>6 ? plz.substring(0, 6) : plz;
 	}
 	public void setOrt(String ort){
 		Ort=ort;
 	}
 	public void setLand(String land){
 		Land=land.length()>3 ? land.substring(0,3) : land;
 	}
 	public boolean write(Kontakt k){
 		return k.set(fields,Strasse,Plz,Ort,Land);
 	}
}
