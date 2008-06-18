package ch.elexis.data;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWTError;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.ParseException;
import bsh.TargetError;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.text.TextContainer;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * A script. At this moment only beanshell is supported as interpreter, but others 
 * are possible 
 * @author gerry
 *
 */
public class Script extends NamedBlob2 {
	private static final Pattern varPattern=Pattern.compile(TextContainer.TEMPLATE_REGEXP);
	private static final String PREFIX="Script:";
	private static Interpreter scripter=new Interpreter();
	
	
	public static Script create(String name, String contents){
		String mid=PREFIX+name;
		Script ret=new Script(mid);
		if(ret.state()==INEXISTENT){
			ret.create(mid);
		}else if(ret.state()==DELETED){
			ret.undelete();
		}
		if(StringTool.isNothing(contents)){
			contents="/* empty */";
		}
		ret.putString(contents);
		return ret;
	}
	@Override
	public String getLabel() {
		String[] name=getId().split(":");
		return name[1];
	}

	public void init() throws Exception{
		scripter.set("finished",false);
		scripter.set("init",true);
		scripter.eval(parse(getString(),new PersistentObject[0]));
		scripter.set("init", false);
	}
	public void finished() throws Exception{
		scripter.set("finished", true);
		scripter.eval(parse(getString(),null));
	}
	private String parse(String t, PersistentObject... params){
		if(params==null){
			params=new PersistentObject[0];
		}
		Matcher matcher=varPattern.matcher(t);
		// Suche Variablen der Form [Patient.Alter]
		StringBuffer sb = new StringBuffer();
		while(matcher.find()){
			boolean bMatched=false;
			String var=matcher.group().replaceAll("[\\[\\]]", "");
			String[] fields=var.split("\\.");
			if(fields.length>1){
				String fqname="ch.elexis.data."+fields[0];
				for(PersistentObject o:params){
					if(o.getClass().getName().equals(fqname)){
						String repl=o.get(fields[1]);
						repl=repl.replace('\\', '/');
						repl=repl.replace('\"', ' ');
						repl=repl.replace('\n', ' ');
						repl=repl.replace('\r', ' ');
						matcher.appendReplacement(sb, "\""+repl+"\"");
						bMatched=true;
					}
				}
			}
			if(!bMatched){
				matcher.appendReplacement(sb, "\"\"");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	public Object execute(PersistentObject... params) throws Exception{
		String t=getString();
		if(!StringTool.isNothing(t)){
			String parsed=parse(t,params);
			try {
				scripter.set("actPatient", GlobalEvents.getSelectedPatient());
				scripter.set("actFall", GlobalEvents.getSelectedFall());
				scripter.set("actKons", GlobalEvents.getSelectedKons());
				scripter.set("Elexis", Hub.plugin);
				return scripter.eval(parsed);
			}catch(TargetError e){
				SWTHelper.showError("Script target Error", "Script Fehler", "Target Error: "+e.getTarget());
				throw(new Exception(e.getMessage()));
			}catch(ParseException e){
				SWTHelper.showError("Script syntax Error", "Script syntax fehler: "+e.getErrorText());
				throw(new Exception(e.getMessage()));
			}
			catch (EvalError e) {
				SWTHelper.showError("Script general error","Script Fehler", "Allgemeiner Script Fehler: "+ e.getErrorText());
				throw(new Exception(e.getMessage()));
			}
		}
		return null;
	}
	public static List<Script> getScripts(){
		Query<Script> qbe=new Query<Script>(Script.class);
		qbe.add("ID", "LIKE", PREFIX+"%");
		return qbe.execute();
	}
	@Override
	public boolean isDragOK() {
		return true;
	}
	@Override
	public boolean isValid() {
		if(getId().matches(PREFIX+"[a-zA-Z0-9_-]+")){
			return super.isValid();	
		}
		return false;
	}
	public static Script load(String id){
		Script ret=new Script(id);
		if(ret.isValid()){
			return ret;
		}
		return null;
	}
	
	protected Script(String id){
		super(id);
	}
	protected Script(){}

}
