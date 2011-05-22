package elexis_db_shaker.actions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import ch.elexis.util.SWTHelper;

public class Namen {
	static final String VORNAMEN_FILE = "vornamen.txt";
	static final String NACHNAMEN_FILE = "nachnamen.txt";
	
	List<String> vornamen;
	List<String> nachnamen;
	
	public Namen(){
		try {
			vornamen = new LinkedList<String>();
			
			InputStream instreamvn = getClass().getResourceAsStream("/vornamen.txt");
			InputStreamReader infilevn = new InputStreamReader(instreamvn);
			BufferedReader vn = new BufferedReader(infilevn);
			String line;
			while ((line = vn.readLine()) != null) {
				vornamen.add(line);
			}
			vn.close();
			
			nachnamen = new LinkedList<String>();
			InputStream instreamnn = getClass().getResourceAsStream("/nachnamen.txt");
			InputStreamReader infilenn = new InputStreamReader(instreamnn);
			BufferedReader nn = new BufferedReader(infilenn);
			while ((line = nn.readLine()) != null) {
				nachnamen.add(line);
			}
			nn.close();
			
		} catch (FileNotFoundException e) {
			SWTHelper.alert("File not found", e.getMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getRandomVorname(){
		return vornamen.get((int) Math.round(Math.random() * (vornamen.size() - 1))).trim();
	}
	
	public String getRandomNachname(){
		return nachnamen.get((int) Math.round(Math.random() * (nachnamen.size() - 1))).trim();
	}
}
