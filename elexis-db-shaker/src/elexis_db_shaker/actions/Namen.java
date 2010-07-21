package elexis_db_shaker.actions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;



public class Namen {
	static final String VORNAMEN_FILE="vornamen.txt";
	static final String NACHNAMEN_FILE="nachnamen.txt";
	
	
	List<String> vornamen;
	List<String> nachnamen;
	
	public Namen() {
		try {
			vornamen = new LinkedList<String>();
			BufferedReader vn = new BufferedReader(new FileReader(VORNAMEN_FILE));
			String line;			
	        while (( line = vn.readLine()) != null){
	        	vornamen.add(line);
	          }
	        vn.close();
			
	        nachnamen = new LinkedList<String>();
	        BufferedReader nn = new BufferedReader(new FileReader(NACHNAMEN_FILE));
	        while (( line = nn.readLine()) != null){
	        	nachnamen.add(line);
	          }
	        nn.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public String getRandomVorname() {
		return vornamen.get((int)Math.round(Math.random()*100)%vornamen.size()).trim();
	}
	
	public String getRandomNachname() {
		return nachnamen.get((int)Math.round(Math.random()*100)%nachnamen.size()).trim();
	}
	
	public static void main(String[] args) {
		Namen n = new Namen();
		for (int i = 0; i < 40; i++) {
			System.out.println(n.getRandomVorname()+" "+n.getRandomNachname());		
		}

	}
}
