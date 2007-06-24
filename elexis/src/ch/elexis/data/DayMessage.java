// $Id: DayMessage.java 240 2006-05-01 13:49:53Z rgw_ch $
/*
 * Created on 05.08.2005
 */
package ch.elexis.data;

import ch.rgw.tools.TimeTool;

public class DayMessage extends PersistentObject {
    public boolean isNew=false;
    static{
        addMapping("AGNDAYS","message","infos");
    }
    public DayMessage(TimeTool date, String message, String infos){
        create(date.toString(TimeTool.DATE_COMPACT));
        setMessages(message,infos);
    }
    public void setMessages(String message, String info){
        set(new String[]{"message","infos"},new String[]{message,info});
    }
    public String getMessage(){
        return get("message");
    }
    public String getInfos(){
        return get("infos");
    }
    public String getLabel(){
    	return get("Date")+" "+getMessage();
    }
    public static DayMessage load(String day){
        DayMessage ret=new DayMessage(day);
        if(!ret.exists()){
            ret.create(day);
            ret.isNew=true;
        }
        return ret;
    }
    @Override
    protected String getTableName()
    {    return "AGNDAYS";
    }

    DayMessage(){/* leer */}
    DayMessage(String id){
        super(id);
    }
    
}
