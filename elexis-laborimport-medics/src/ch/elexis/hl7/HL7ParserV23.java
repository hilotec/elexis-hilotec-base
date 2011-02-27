package ch.elexis.hl7;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.datatype.ED;
import ca.uhn.hl7v2.model.v23.datatype.ST;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ch.elexis.hl7.model.EncapsulatedData;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.model.StringData;
import ch.elexis.hl7.util.HL7Helper;
import ch.rgw.io.FileTool;

public class HL7ParserV23 extends HL7Parser {
	
	/**
	 * Reads an observation ORU_R01 HL7 file
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws EncodingNotSupportedException
	 * @throws HL7Exception
	 * @throws ParseException
	 */
	public ObservationMessage readORU_R01(final File file) throws IOException,
		EncodingNotSupportedException, HL7Exception, ParseException{
		clearMessages();
		ObservationMessage observation = null;
		String text = FileTool.readTextFile(file);
		Parser p = new PipeParser();
		Message hl7Msg = p.parse(text);
		if (hl7Msg instanceof ORU_R01) {
			ORU_R01 oru = (ORU_R01) hl7Msg;
			
			String msh3_sendingApplication =
				oru.getMSH().getMsh3_SendingApplication().getHd1_NamespaceID().getValue();
			String msh4_sendingFacility =
				oru.getMSH().getMsh4_SendingFacility().getHd1_NamespaceID().getValue();
			String msh7_dateTimeOfMessage =
				oru.getMSH().getMsh7_DateTimeOfMessage().getTs1_TimeOfAnEvent().getValue();
			
			String dateStr = HL7Helper.stringToDate(msh7_dateTimeOfMessage).toString();
			System.out.println(msh7_dateTimeOfMessage + " = " + dateStr); //$NON-NLS-1$
			
			String pid2_patientExternalId =
				oru.getRESPONSE(0).getPATIENT().getPID().getPid2_PatientIDExternalID().getCx1_ID()
					.getValue();
			String pid3_patientInternalId =
				oru.getRESPONSE(0).getPATIENT().getPID().getPid3_PatientIDInternalID(0).getCx1_ID()
					.getValue();
			String or2_placerOrderNumber =
				oru.getRESPONSE().getORDER_OBSERVATION(0).getORC().getOrc2_PlacerOrderNumber(0)
					.getEi1_EntityIdentifier().getValue();
			observation =
				new ObservationMessage(msh3_sendingApplication, msh4_sendingFacility,
					msh7_dateTimeOfMessage, pid2_patientExternalId, pid3_patientInternalId,
					or2_placerOrderNumber);
			
			int obscount = oru.getRESPONSE().getORDER_OBSERVATIONReps();
			for (int j = 0; j < obscount; j++) {
				for (int i = 0; i < oru.getRESPONSE().getORDER_OBSERVATION(j).getOBSERVATIONReps(); i++) {
					OBX obx = oru.getRESPONSE().getORDER_OBSERVATION(j).getOBSERVATION(i).getOBX();
					String valueType = obx.getObx2_ValueType().getValue();
					if (HL7Constants.OBX_VALUE_TYPE_ED.equals(valueType)) {
						String observationId =
							obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
						if (!"DOCUMENT".equals(observationId)) { //$NON-NLS-1$
							addWarning(MessageFormat.format(Messages.HL7Parser_wrongObservationId,
								observationId));
						}
						ED ed = (ED) obx.getObx5_ObservationValue(0).getData();
						String filename = ed.getEd3_DataSubtype().getValue();
						String encoding = ed.getEd4_Encoding().getValue();
						String data = ed.getEd5_Data().getValue();
						String dateOfObservation =
							obx.getObx14_DateTimeOfTheObservation().getTs1_TimeOfAnEvent()
								.getValue();
						observation.add(new EncapsulatedData(filename, encoding, data,
							dateOfObservation));
					} else if (HL7Constants.OBX_VALUE_TYPE_ST.equals(valueType)) {
						String name = obx.getObx4_ObservationSubID().getValue();
						String value = ((ST) obx.getObx5_ObservationValue(0).getData()).getValue();
						String unit = obx.getObx6_Units().getCe1_Identifier().getValue();
						String range = obx.getObx7_ReferencesRange().getValue();
						String dateOfObservation =
							obx.getObx14_DateTimeOfTheObservation().getTs1_TimeOfAnEvent()
								.getValue();
						observation
							.add(new StringData(name, unit, value, range, dateOfObservation));
					} else {
						addError(MessageFormat.format(Messages.HL7Parser_valueTypeNotImplemented,
							valueType));
					}
				}
			}
			System.out.println();
		} else {
			addError(MessageFormat.format(Messages.HL7Parser_wrongMessageType, hl7Msg.getName()));
		}
		
		return observation;
	}
	
	@Override
	public String getVersion(){
		return "2.3"; //$NON-NLS-1$
	}
	
}
