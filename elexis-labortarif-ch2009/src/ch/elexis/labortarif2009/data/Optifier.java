/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and medelexis AG
 * All rights reserved.
 * $Id: Optifier.java 140 2009-06-23 20:00:16Z  $
 *******************************************************************************/

package ch.elexis.labortarif2009.data;

import java.util.List;

import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;
import ch.elexis.scripting.TarmedTaxpunktkorrektur;
import ch.elexis.util.IOptifier;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.Result.SEVERITY;

public class Optifier implements IOptifier {

	/**
	 * Add and recalculate the various possible amendments
	 */
	public Result<IVerrechenbar> add(IVerrechenbar code, Konsultation kons){
		if (code instanceof Labor2009Tarif) {
			new Verrechnet(code, kons, 1);
			Result<Object> res = optify(kons);
			if (res.isOK()) {
				return new Result<IVerrechenbar>(code);
			} else {
				return new Result<IVerrechenbar>(res.getSeverity(), res.getCode(), res.toString(),
						code, true);
			}
		}
		return new Result<IVerrechenbar>(SEVERITY.ERROR, 2, "No Lab2009Tariff", null, true);
	}

	public Result<Object> optify(Konsultation kons){
		try {
			boolean haveKons=false;
			TimeTool date = new TimeTool(kons.getDatum());
			TimeTool deadline = new TimeTool("31.12.2011");
			if (date.isBefore(new TimeTool("01.07.2009"))) {
				return new Result<Object>(SEVERITY.WARNING, 3, "Code not yet valid", null, false);
			}

			List<Verrechnet> list = kons.getLeistungen();
			Verrechnet v470710 = null;
			Verrechnet v470720 = null;
			Verrechnet v4708 = null;
			int z4708 = 0;
			int z4707 = 0;
			int z470710 = 0;
			int z470720 = 0;

			for (Verrechnet v : list) {
				IVerrechenbar iv = v.getVerrechenbar();
				if (iv instanceof Labor2009Tarif) {
					String cc = v.getVerrechenbar().getCode();
					if (cc.equals("4708.00")) { // Übergangszuschlag
						v4708 = v;
					} else if (cc.equals("4707.00")) { // Pauschale
						if (z4707 < 1) {
							z4707 = 1;
						} else {
							return new Result<Object>(SEVERITY.WARNING, 1,
									"4707.00 only once per cons", v, false);
						}
					} else if (cc.equals("4707.10")) { // Fachbereich C
						v470710 = v;
					} else if (cc.equals("4707.20")) { // Fachbereich
						// nicht-C
						v470720 = v;
					} else if (cc.equals("4703.00") || cc.equals("4701.00") || cc.equals("4704.00") || cc.equals("4706.00")) {
						continue;
					} else {
						Labor2009Tarif vlt = (Labor2009Tarif) iv;
						if (vlt.get(Labor2009Tarif.FLD_FACHBEREICH).indexOf("C") > -1) {
							z470710 += v.getZahl();
						} else {
							z470720 += v.getZahl();
						}
						z4708 += v.getZahl();
					}
				}else if(iv.getCode().equals("00.0010") || iv.getCode().equals("00.0060")){ // Kons erste 5 Minuten 
					haveKons=true;
				}
			}
			// reduce amendments to max. 24 TP
			while (((4 + 2 * z470710 + z470720) > 26) && z470710 > 0) {
				z470710--;
			}
			while (((4 + 2 * z470710 + z470720) > 24) && z470720 > 0) {
				z470720--;
			}

			if (z470710 == 0 || haveKons==false) {
				if (v470710 != null) {
					v470710.delete();
				}
			} else {
				if (v470710 == null) {
					v470710 = doCreate(kons, "4707.10");
				}
				v470710.setZahl(z470710);
			}

			if (z470720 == 0 || haveKons==false) {
				if (v470720 != null) {
					v470720.delete();
				}
			} else {
				if (v470720 == null) {
					v470720 = doCreate(kons, "4707.20");
				}
				v470720.setZahl(z470720);
			}

			if (z4707 == 0 && ((z470710 + z470720) > 0) && haveKons==true) {
				doCreate(kons, "4707.00");
			}
			if (z4708 > 0 && haveKons==true) {
				if (v4708 == null) {
					if (date.isBefore(deadline)) {
						v4708 = doCreate(kons, "4708.00");
					}
				} else {
					if (date.isAfterOrEqual(deadline)) {
						v4708.delete();
						return new Result<Object>(SEVERITY.WARNING, 2,
								"4708.00 only until 2011-12-31", null, false);
					}
				}
			}
			if (v4708 != null) {
				v4708.setZahl(z4708);
			}
			return new Result<Object>(kons);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return new Result<Object>(SEVERITY.ERROR, 1, "Tariff not installed correctly", null,
					true);

		}

	}

	public Result<Verrechnet> remove(Verrechnet code, Konsultation kons){
		List<Verrechnet> l = kons.getLeistungen();
		l.remove(code);
		code.delete();
		Result<Object> res = optify(kons);
		if (res.isOK()) {
			return new Result<Verrechnet>(code);
		} else {
			return new Result<Verrechnet>(res.getSeverity(), res.getCode(), res.toString(), code,
					true);
		}
	}

	private Verrechnet doCreate(Konsultation kons, String code) throws Exception{
		String z =
			new Query<Labor2009Tarif>(Labor2009Tarif.class).findSingle(Labor2009Tarif.FLD_CODE,
					Query.EQUALS, code);
		if (z != null) {
			return new Verrechnet(Labor2009Tarif.load(z), kons, 1);
		} else {
			throw new Exception("Tariff not installed correctly");
		}

	}

}
