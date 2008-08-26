package ch.elexis.scripting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Verrechnet;
import ch.elexis.util.Money;
import ch.rgw.tools.ExHandler;

public class RnKorr {
	FileWriter writer;
	Rechnung actRn;
	
	public void run(){
		try{
			PersistentObject.getConnection().exec("DELETE FROM VK_PREISE WHERE Typ='Covercard' AND DATUM_BIS<'20100801'");
			PersistentObject.getConnection().exec("UPDATE VK_PREISE SET DATUM_VON='20060101' WHERE Typ='Covercard'");
			File file=new File(System.getProperty("user.home")+File.separator+"elexis"+File.separator+"rechnungslauf.log");
			writer=new FileWriter(file);
			Query<Rechnung> qbe=new Query<Rechnung>(Rechnung.class);
			List<Rechnung> rnn=qbe.execute();
			for(Rechnung rn:rnn){
				actRn=rn;
				switch(rn.getStatus()){
				case RnStatus.ZUVIEL_BEZAHLT:
				case RnStatus.BEZAHLT:
				case RnStatus.TEILZAHLUNG:
				case RnStatus.TEILVERLUST:
				case RnStatus.TOTALVERLUST:
				case RnStatus.FEHLERHAFT:
					continue;
				case RnStatus.OFFEN:
				case RnStatus.OFFEN_UND_GEDRUCKT:
				case RnStatus.MAHNUNG_1:
				case RnStatus.MAHNUNG_1_GEDRUCKT:
				case RnStatus.MAHNUNG_2:
				case RnStatus.MAHNUNG_2_GEDRUCKT:
				case RnStatus.MAHNUNG_3:
				case RnStatus.MAHNUNG_3_GEDRUCKT:
					korr(rn);
				}
			}
			Query<Konsultation> q2=new Query<Konsultation>(Konsultation.class);
			List<Konsultation> lk=q2.execute();
			for(Konsultation k:lk){
				Rechnung r=k.getRechnung();
				if( (r==null) || (r.getStatus()==RnStatus.STORNIERT)){
					writer.write("Prüfe Konsultation "+k.getLabel());
					Fall fall=k.getFall();
					if(fall!=null){
						List<Verrechnet> vv=k.getLeistungen();
						for(Verrechnet v:vv){
							v.setStandardPreis();
						}
					}
					writer.write(" - ok.\n");
				}
			}
			
		}catch(Exception ex){
			ExHandler.handle(ex);
			if(writer!=null){
				try {
					writer.write("Fehler "+ex.getMessage());
					if(actRn!=null){
						writer.write("Zuletzt bearbeitete Rechnung: "+actRn.getLabel());
					}
				} catch (IOException e) {
					ExHandler.handle(e);
				}
			}
		}finally{
			if(writer!=null){
				try {
					writer.close();
				} catch (IOException e) {
					ExHandler.handle(e);
				}
			}
		}
		
	}
	
	private void korr(Rechnung rn) throws Exception{
		writer.write("Bearbeite Rechnung "+rn.getLabel());
		List<Konsultation> konss=rn.getKonsultationen();
		rn.storno(true);
		for(Konsultation kons:konss){
			Fall fall=kons.getFall();
			if(fall!=null){
				List<Verrechnet> vv=kons.getLeistungen();
				for(Verrechnet v:vv){
					v.setStandardPreis();
				}
			}
		}
		Rechnung.build(konss);
		writer.write(" - ok.\n");
	}
}
