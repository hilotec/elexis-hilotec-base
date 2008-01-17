package ch.elexis.data;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import ch.elexis.util.Extensions;
import ch.elexis.util.Log;
import ch.rgw.tools.ExHandler;

public class PersistentObjectFactory  implements IExecutableExtension{

	/**
	 * Ein Objekt als Schablone eines beliebigen abgeleiteten Typs erstellen,
	 * ohne es in die Datenbank einzutragen. Wenn der Programmkern kein Objekt dieser Klasse erstellen kann,
	 * werden der Reihe nach alle Plugins abgeklappert, die eine PersistentObjectFactory deklariert haben.
	 * @param typ Der gewünschte Subtyp von PersistentObject
	 * @return ein unabhängiges Objekt des gewünschten Typs oder null
	 */
	@SuppressWarnings("unchecked")
	public  PersistentObject createTemplate(Class typ){
		
		try{
			return (PersistentObject)typ.newInstance();
		}catch(IllegalAccessException ex){
			List<PersistentObjectFactory> exts=Extensions.getClasses("ch.elexis.PersistentReference","Class");
	        for(PersistentObjectFactory po:exts){
	        	PersistentObject ret=po.doCreateTemplate(typ);
	        	if(ret!=null){
	        		return ret;
	        	}
	         }
		}
		catch(Exception ex){
			ExHandler.handle(ex);
			PersistentObject.log.log("create: Konnte Objekt nicht erstellen "+ex.getMessage(),Log.ERRORS);
	
		}
	    return null;
	}


	protected PersistentObject doCreateTemplate(Class<? extends PersistentObject> typ){
		try {
			return  (PersistentObject)typ.newInstance();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	/**
	 * Helper-Funktion, die Objekte eines beliebigen abgeleiteten Typs
	 * mit beliebigen Feldvorgaben erstellen kann.
	 * @param typ   Die Klasse des zu erstellenden Objekts
	 * @param fields Die initial zu belegenden Felder. ID darf nicht angegeben werden.
	 * @param values Die Werte für die Felder
	 * @return Das Objekt bei Erfolg, sonst null
	 */
	public PersistentObject create(Class<? extends PersistentObject> typ,String[] fields, String[] values){
		PersistentObject template=createTemplate(typ);
		template.create(null);
		if((template!=null) && ( template.set(fields,values)==true)){
	            return template;
	    }
	    return null;
	}


	/**
	 * Ein Objekt einer beliebigen abgeleiteten Klasse anhand des Pseudoserialisiercodes
	 * erstellen. Wenn das Objekt vom Programmkern nicht erstellt werden kann, werden der Reihe 
	 * nach alle Plugins abgeklappert, die eine PersistentObjectFactory deklariert haben. 
	 * @param code der String, der das Objekt beschreibt
	 * @return das erstellte Objekt oder null, wenn aus dem übergebenen Code kein Objekt erstellt
	 * werden konnte.
	 */
	@SuppressWarnings("unchecked")
	public PersistentObject createFromString(String code){
	    if(code==null) {
	        return null;
	    }
	    try{
	        String[] ci=code.split("::");
	        Class clazz=Class.forName(ci[0]);
	        Method load=clazz.getMethod("load",new Class[]{String.class});
	        return  (PersistentObject)(load.invoke(null,new Object[]{ci[1]}));
	    }catch(ClassNotFoundException ex){
	    	List<PersistentObjectFactory> exts=Extensions.getClasses("ch.elexis.PersistentReference","Class");
	        for(PersistentObjectFactory po:exts){
	        	PersistentObject ret=po.createFromString(code);
	        	if(ret!=null){
	        		return ret;
	        	}
	         }
	    }catch(Exception ex){
	    	ExHandler.handle(ex);
	    	return null;
	    }
		return null;
	    
	}
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		
		
	}
	
}
