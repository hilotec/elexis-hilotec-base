/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: TICode.java 2350 2007-05-08 15:14:44Z rgw_ch $
 *******************************************************************************/


package ch.elexis.data;

import java.util.Hashtable;

import org.eclipse.jface.action.IAction;
import ch.elexis.diagnosecodes_schweiz.Messages;

/**
 * Die Tessinercodes werden nicht in der Datenbank vorgehalten, sondern sind
 * aus Effizientgründen hier in dieser Klasse fest verdrahtet. Die Klasse ist trotzdem
 * im data-Package, um die Programmierkonsistenz zu wahren.
 * @author Gerry
 *
 */
public class TICode extends PersistentObject implements IDiagnose{
    private static Hashtable<String,TICode> hash=new Hashtable<String,TICode>();
	private String Text;
	
	
	TICode(String Code, String Text){
        super(Code);
        this.Text=Text;
	}
	public String getText(){
		return Text;
	}
	public String getCode(){
		return getId();
	}
    public String getLabel()
    {
        return getId()+" "+Text; //$NON-NLS-1$
    }
    public String getCodeSystemName()
    {
        return "TI-Code"; //$NON-NLS-1$
    }
    
    public static TICode load(String code){
        return getFromCode(code);
    }
    public static TICode[] getRootNodes(){
        TICode[] ret=new TICode[ticode.length];
        int i;
        for(i=0;i<ticode.length;i++){
            String[] line=ticode[i];
            ret[i]=new TICode(line[0],line[1]);
        }
        return ret;
    }
    public static TICode getFromCode(String code){
        TICode ret=hash.get(code);
        if(ret==null){
            String chapter=code.substring(0,1);
            int subch=0;
            if(code.length()==2){
                subch=Integer.parseInt(code.substring(1));
            }
            for(int i=0;i<ticode.length;i++){
                if(ticode[i][0].startsWith(chapter)){
                    if(subch==9){
                    	subch=ticode[i].length-2;
                    	ret=new TICode(chapter+"9",ticode[i][subch+1]); //$NON-NLS-1$
                    }else{
                    	ret=new TICode(chapter+subch,ticode[i][subch+1]);
                    }
                    hash.put(code,ret);
                    return ret;
                }
            }
        }
        return ret;
    }
    public TICode getParent(){
        if(getId().length()==1){
            return null;
        }
        return getFromCode(getId().substring(0,1));
    }
    public boolean hasChildren(){
        if(getId().length()==1){
            return true;
        }
        return false;
    }
    
    @Override
	public boolean exists() {
    	return true;
	}
	public TICode[] getChildren(){
    	if(getId().length()>1){
    		return null;
    	}
    	String chapter=getId().substring(0,1);
    	for(int i=0;i<ticode.length;i++){
    		if(ticode[i][0].equals(chapter)){
    			TICode[] ret=new TICode[ticode[i].length-2];
    			for(int j=2;j<ticode[i].length;j++){
    				String x;
    				if(j==ticode[i].length-1){
    					x="9"; //$NON-NLS-1$
    				}else{
    					x=Integer.toString(j-1);
    				}
    				ret[j-2]=new TICode(chapter+x,ticode[i][j]);
    			}
    			return ret;
    		}
    	}
    	return null;
    }
    
    //private static String[][] ticode;
    
    /*
    private static final String[][] ticode_fr={
    	{"A", "Système cardio-vasculaire",
    		   "Vitiens cardiaques/ valvules cardiaques",
    		   "Maladies coronariennes, y compris infarctus du myocarde",
    		   "Troubles du rythme cardiaque",
    		   "Hypertonie artèrielles",
    		   "Artères",
    		   "Veines (y compris varices)",
    		   "Vaisseaux lymphatiques y compris oedème lymphatique",
    		   "Autres maladies du système cardio-vasculaire"},
       {"B", "Sang / moelle osseuse/ rate",
    			   "Anémie",
    			   "Coagulopathie",
    			   "Maladies de la moelle osseuse et du sang",
    			   "Rate / ganglion lymphatique / système immunitaire",
    			   "Autres maladies de la moelle osseuse / du sang / de la rate"},
    			   
       {"C", "Poumon / appareil respiratoire",
    				   "Asthme",
    				   "Bronchite chronique",
    				   "Embolie pulmonaire",
    				   "Poumon / plèvre (tuberculose exclue)",
    				   "Autres maladies de l'appareil respiratoire"},
       {"D", "Squelette / appareil locomoteur",
    					"Muscles / tendons",
    					"Articulations / ligaments / bourse (D3 exclu.)",
    					"Arthrite / M. Bechterew",
    					"Arthrose",
    					"Colonne vertébrale (D3 exclu.)",
    					 "Autres maladies de l'appareil locomoteur"},
       {"E", "Tube digestif",
    	     "Oesophage / estomac / duodénum, ulcère compris)",
    	     "Intestin (intestin grêle / colon)",
    	     "Rectum / anus, hémorroïdes comprises)",
    	     "Foie / voies biliaires / vésicule biliaire",
    	     "Pancréas, diabète exclu)",
    	     "Diaphragme",
    	     "Hernies",
    	     "Autres maladies du tube digestif"},
    	{"F", "Métabolisme",
    	    	 "Métabolisme glucidique",
    	    	 "Maladies de la glande thyroïde",
    	    	 "Autres maladies du métabolisme"},
    	{"G", "Maladies infectieuses et parasitaires",
    	    	"Infection banale",
    	    	"Tuberculose",
    	        "Hépatite virale",
    	         "Autres maladies infectieuses ou parasitaires"},
    	 {"H", "Rein et voies urinaires",
    	       "Rein / insuffisance rénale (dialyse / transplantation)",
    	        "Calcul rénal",
    	        "Voies urinaires",
    	        "Autres maladies des reins et des voies urinaires"},
    	 {"I", "Organes génitaux",
    	        "Organes génitaux masculins",
    	        "Vulve, vagin, petit bassin",
    	        "Utérus",
    	        "Annexes",
    	        "Troubles du cycle menstruel",
    	        "Maladies de la glande mammaire",
    	        "Stérilisation",
    	        "Autres maladies des organes génitaux"},
    	 {"K", "Grossesse / stérilité",
    	        "Grossesse risque normal",
    	        "Grossesse avec complications",
    	        "Stérilité et insémination artificielle"},
    	  {"L", "Système nerveux",
    	        "Cerveau / moelle épinière",
    	        "Nerfs périphériques",
    	        "Paralysie / ataxie",
    	        "Migraine et équivalents",
    	        "Epilepsie",
    	        "Autres maladies du système nerveux"},
    	        
    	   {"M", "Maladies psychiques",
    	         "Troubles du sommeil",
    	        "Maladies psychiques",
    	        "Maladies psycho-organiques"},
    	   {"N", "Peau",
    	        "Maladies allergiques de la peau, eczéma exclu.",
    	        "Maladies inflammatoires / infectieuses de la peau",
    	        "Eczéma",
    	        "Maladies vasculaires / dégénératives de la peau",
    	        "Psoriasis / hyperkératoses",
    	        "Cicatrices",
    	        "Autres maladies de la peau"},
    	    {"O", "Cou / nez / oreilles",
    	        "Nez, N6 exclu.",
    	        "sinus nasal",
    	        "Cavité buccale, glandes salivaires",
    	        	"Amygdales / adénoïdes",
    	        	"Larynx / trachée",
    	        	"Malformations du nez et des oreilles",
    	        	"Oreille moyenne / trompe d'Eustache",
    	        	"Oreille interne",
    	        	"Autres maladies ORL"},
    	     {"P", "Oeil",
    	        	"Paupière / conjonctive",
    	        	"Lentille / cornée / corps vitré",
    	        	"Iris / glaucome",
    	        	"Rétine / nerf optique / vaisseaux",
    	        	"Muscle oculomoteur / strabisme",
    	        	"Autres maladies des yeux"},
    	     {"Q", "Dents / mâchoire",
    	        	"Kyste",
    	        	"Abcès dentaire",
    	        	"Fibromes",
    	        	"Autres maladies de la mâchoire ou dentaires"},
    	     {"R", "Accident / conséquences de l'accident",
    	        	"Tête / colonne vertébrale",
    	        	"Thorax",
    	        	"Abdomen",
    	        	"Extrémités supérieures",
    	            "Extrémités inférieures"},
    	     {"S", "Prestations non prises en charge par l'assurance-maladie",
    	            	"Prestations non prises en charge par l'assurance-maladie"},
    	     {"T", "Mesures préventives",
    	            "Examen préventif",
    	            "Vaccins"},
    	     {"U", "Orientation du médecin-conseil (au lieu du diagnostic)",
    	            	"Orientation du médecin-conseil (au lieu du diagnostic)"},
    	            	
    	     {"0", "Codes supplémentaires",
    	            "droite",
    	        	 "gauche",
    	            "aigu",
    	            "chronique / récidive",
    	            "infectueux",
    	            "fonctionnel",
    	            "néoplasie",
    	            "raisons professionnelles"}
    };
    */
	private static final String[][] ticode={
			{"A",Messages.TICode_Heart, //$NON-NLS-1$
			   Messages.TICode_valves,
			   Messages.TICode_coronaria,
			   Messages.TICode_rhythm,
			   Messages.TICode_hypertonia,
			   Messages.TICode_arteries,
			   Messages.TICode_veins,
			   Messages.TICode_lymphVessels,
			   Messages.TICode_heartOther},
			{"B",Messages.TICode_blood, //$NON-NLS-1$
			    Messages.TICode_anemia,
			    Messages.TICode_coagulo,
			    Messages.TICode_boneMarrow,
			    Messages.TICode_spleen,
			    Messages.TICode_bloodOther},
			    
			{"C",Messages.TICode_lung, //$NON-NLS-1$
			 	Messages.TICode_asthma,
				Messages.TICode_cough,
			 	Messages.TICode_embolism,
			 	Messages.TICode_lungPleural,
			 	Messages.TICode_lungOther},
			 	
			{"D",Messages.TICode_locomotion, //$NON-NLS-1$
			    Messages.TICode_muscle,
			    Messages.TICode_joint,
			    Messages.TICode_arthtiris,
			    Messages.TICode_arthrosis,
			    Messages.TICode_vertebral,
			    Messages.TICode_locoOther},
			    
			{"E",Messages.TICode_digestive, //$NON-NLS-1$
				Messages.TICode_esophagus,
				Messages.TICode_bowel,
				Messages.TICode_rectum,
				Messages.TICode_liver,
				Messages.TICode_pancreas,
				Messages.TICode_diaphragm,
				Messages.TICode_hernia,
				Messages.TICode_digestiveOther},
				
			{"F",Messages.TICode_metabolic, //$NON-NLS-1$
				Messages.TICode_diabetes,
				Messages.TICode_thyroid,
				Messages.TICode_metabolicOther},
				
			{"G",Messages.TICode_infections, //$NON-NLS-1$
				Messages.TICode_simpleInfection,
				Messages.TICode_tuberculosis,
				Messages.TICode_hepatitis,
				Messages.TICode_infectionOther},
			
			{"H",Messages.TICode_urinary, //$NON-NLS-1$
				Messages.TICode_kidney,
				Messages.TICode_stones,
				Messages.TICode_ureters,
				Messages.TICode_urinaryOther},
				
			
			{"I",Messages.TICode_sexual, //$NON-NLS-1$
					Messages.TICode_male,
					Messages.TICode_vaginal,
					Messages.TICode_uterus,
					Messages.TICode_adnexes,
					Messages.TICode_cycle,
					Messages.TICode_mammae,
					Messages.TICode_sterilisation,
					Messages.TICode_sexualOther},
					
			{"K",Messages.TICode_reproduction, //$NON-NLS-1$
						Messages.TICode_pregnancyNormal,
						Messages.TICode_pregnancyAbnormal,
						Messages.TICode_sterility,
						Messages.TICode_reproductionOther},
						
			{"L",Messages.TICode_nervous, //$NON-NLS-1$
						Messages.TICode_brain,
						Messages.TICode_nerves,
						Messages.TICode_palsy,
						Messages.TICode_migraine,
						Messages.TICode_epilepsy,
						Messages.TICode_nervousOther},
						
			{"M",Messages.TICode_psyche, //$NON-NLS-1$
						Messages.TICode_sleep,
						Messages.TICode_psychic,
						Messages.TICode_psychoorganic,
						Messages.TICode_psycheOther},
						
			{"N",Messages.TICode_skin, //$NON-NLS-1$
					    Messages.TICode_allergic,
					    Messages.TICode_inflammation,
					    Messages.TICode_ekcema,
					    Messages.TICode_vaskular,
					    Messages.TICode_psoriasis,
					    Messages.TICode_scars,
					    Messages.TICode_skinOther},
					    
			{"O",Messages.TICode_orl, //$NON-NLS-1$
					    Messages.TICode_nose,
					    Messages.TICode_sinuses,
					    Messages.TICode_mouth,
					    Messages.TICode_tonsil,
					    Messages.TICode_larynx,
					    Messages.TICode_earform,
					    Messages.TICode_middleEar,
					    Messages.TICode_innerEar,
					    Messages.TICode_orlOther},
					    
			{"P",Messages.TICode_eye, //$NON-NLS-1$
					    Messages.TICode_lid,
					    Messages.TICode_cornea,
					    Messages.TICode_eyemuscles,
					    Messages.TICode_iris,
					    Messages.TICode_retina,
					    Messages.TICode_eyeOther	},
				    
			{"Q",Messages.TICode_jaw, //$NON-NLS-1$
					    Messages.TICode_cyst,
					    Messages.TICode_toothabscess,
					    Messages.TICode_fibroma,
					    Messages.TICode_jawOther},
					    
			{"R", Messages.TICode_accidents, //$NON-NLS-1$
					    Messages.TICode_accidentHead,
					    Messages.TICode_accisdentThorax,
					    Messages.TICode_accidentAbdomen,
					    Messages.TICode_accidentArm,
					    Messages.TICode_accidentLeg,
					    Messages.TICode_AccidentOther},
					    
			{"S",Messages.TICode_nonmust, //$NON-NLS-1$
					    Messages.TICode_nonmust},
					    
			{"T",Messages.TICode_prevention, //$NON-NLS-1$
					    Messages.TICode_preventionCheck,
					    Messages.TICode_vaccination	},
					    
			{"U",Messages.TICode_docInformed,Messages.TICode_docInformed}, //$NON-NLS-1$
			{"0",Messages.TICode_accessory, //$NON-NLS-1$
						Messages.TICode_right,
						Messages.TICode_left,
						Messages.TICode_acute,
						Messages.TICode_chronic,
						Messages.TICode_infectiuous,
						Messages.TICode_functional,
						Messages.TICode_neoplastic,
						Messages.TICode_professional	}
	};
    @Override
    protected String getTableName()
    {
      return "None"; //$NON-NLS-1$
    }
  
    @Override
    public boolean isDragOK()
    {
        return true;
    }
  
    TICode(){}
	public String getCodeSystemCode() {
		return "999"; //$NON-NLS-1$
	}
	public Iterable<IAction> getActions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	static{
		/*
		 * war doch keine so gute Idee. Lieber externalisieren
		String lang=Hub.localCfg.get(PreferenceConstants.ABL_LANGUAGE,"d");
		if(lang.equals("d")){
			ticode=ticode_de;
		}else if(lang.equals("f")){
			ticode=ticode_fr;
		}else if(lang.equals("i")){
			ticode=ticode_fr;
		}
		*/
	}

   }
