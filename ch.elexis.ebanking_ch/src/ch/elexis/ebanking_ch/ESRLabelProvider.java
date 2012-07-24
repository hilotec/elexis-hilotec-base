package ch.elexis.ebanking_ch;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import ch.elexis.Desk;
import ch.elexis.banking.ESRRecord;
import ch.elexis.banking.Messages;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class ESRLabelProvider extends LabelProvider implements ITableLabelProvider,
		ITableColorProvider {
	
	DecimalFormat df = new DecimalFormat("###0.00"); //$NON-NLS-1$
	
	public String getColumnText(Object element, int columnIndex){
		String text = ""; //$NON-NLS-1$
		
		if (element instanceof ESRRecord) {
			ESRRecord rec = (ESRRecord) element;
			
			if (rec.getTyp().equals(ESRRecord.MODE.Summenrecord)) {
				switch (columnIndex) {
				case ESRView.DATUM_INDEX:
					text = rec.get("Datum"); //$NON-NLS-1$
					break;
				case ESRView.RN_NUMMER_INDEX:
					text = "Summe"; //$NON-NLS-1$
					break;
				case ESRView.BETRAG_INDEX:
					text = rec.getBetrag().getAmountAsString();
					break;
				case ESRView.DATEI_INDEX:
					text = rec.getFile();
					break;
				}
			} else {
				switch (columnIndex) {
				case ESRView.DATUM_INDEX:
					text = rec.get("Datum"); //$NON-NLS-1$
					break;
				case ESRView.RN_NUMMER_INDEX:
					Rechnung rn = rec.getRechnung();
					if (rn != null) {
						text = rn.getNr();
					}
					break;
				case ESRView.BETRAG_INDEX:
					text = rec.getBetrag().getAmountAsString();
					break;
				case ESRView.EINGELESEN_INDEX:
					text = rec.getEinlesedatatum();
					break;
				case ESRView.VERRECHNET_INDEX:
					text = rec.getVerarbeitungsdatum();
					break;
				case ESRView.GUTGESCHRIEBEN_INDEX:
					text = rec.getValuta();
					break;
				case ESRView.PATIENT_INDEX:
					text = rec.getPatient().getLabel();
					break;
				case ESRView.BUCHUNG_INDEX:
					String dat = rec.getGebucht();
					if (StringTool.isNothing(dat)) {
						text = Messages.ESRView2_notbooked;
					} else {
						text = new TimeTool(dat).toString(TimeTool.DATE_GER);
					}
					break;
				case ESRView.DATEI_INDEX:
					text = rec.getFile();
					break;
				}
			}
		}
		
		return text;
	}
	
	public Color getForeground(Object element, int columnIndex){
		return Desk.getColor(Desk.COL_BLACK);
	}
	
	public Color getBackground(Object element, int columnIndex){
		if (element instanceof ESRRecord) {
			ESRRecord rec = (ESRRecord) element;
			if (rec.getTyp().equals(ESRRecord.MODE.Summenrecord)) {
				return Desk.getColor(Desk.COL_GREEN);
			}
			String buch = rec.getGebucht();
			if (rec.getRejectCode().equals(ESRRecord.REJECT.OK)) {
				if (StringTool.isNothing(buch)) {
					return Desk.getColor(Desk.COL_GREY);
				}
				return Desk.getColor(Desk.COL_WHITE);
			}
			return Desk.getColor(Desk.COL_RED);
		}
		return Desk.getColor(Desk.COL_SKYBLUE);
	}
	
	@Override
	public Image getColumnImage(Object element, int columnIndex){
		// TODO Auto-generated method stub
		return null;
	}
	
}
