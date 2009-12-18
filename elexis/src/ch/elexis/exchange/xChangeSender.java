package ch.elexis.exchange;

import java.util.List;

import org.jdom.Element;

import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.exchange.XChangeContainer.UserChoice;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.exchange.elements.ContactRefElement;
import ch.elexis.exchange.elements.ContactsElement;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.exchange.elements.XChangeElement;
import ch.elexis.exchange.elements.XidElement;

public class xChangeSender implements IDataSender {
	private XChangeContainer container = new XChangeContainer();

	public boolean canHandle(Class<? extends PersistentObject> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	public void finalizeExport() throws XChangeException {
		// TODO Auto-generated method stub

	}

	public XChangeElement store(Object output) throws XChangeException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Add a new Contact to the file. It will only be added, if it does not yet
	 * exist. Rule for the created ID: If a Contact has a really unique ID (EAN,
	 * Unique Patient Identifier) then this shold be used. Otherwise a unique id
	 * should be generated (here we take the existing id from Elexis which is by
	 * definition already a UUID)
	 * 
	 * @param k
	 *            the contact to insert
	 * @return the Element node of the newly inserted (or earlier inserted)
	 *         contact
	 */
	@SuppressWarnings("unchecked")
	public ContactElement addContact(Kontakt k) {
		ContactsElement eContacts=container.getContactsElement();
		List<ContactElement> lContacts = (List<ContactElement>) eContacts
				.getChildren(ContactElement.XMLNAME, ContactElement.class);
		for (ContactElement e : (List<ContactElement>) lContacts) {
			XidElement xid = e.getXid();
			if ((xid != null) && (xid.match(k) == XidElement.XIDMATCH.SURE)) {
				e.setContainer(container);
				return e;
			}
		}
		ContactElement contact = new ContactElement(container, k);
		eContacts.add(contact);
		container.addChoice(contact.getElement(),k.getLabel(),k);
		return contact;
	}

	public ContactElement addPatient(Patient pat) {
		ContactElement ret = addContact(pat);
		List<BezugsKontakt> bzl = pat.getBezugsKontakte();

		for (BezugsKontakt bz : bzl) {
			ret.add(new ContactRefElement(container, bz));
		}

		MedicalElement eMedical = new MedicalElement(container, pat);
		ret.add(eMedical);
		/*
		choices.put(eMedical.getElement(), new UserChoice(true,
				Messages.XChangeContainer_kg, eMedical));
		for (IExchangeContributor iex : lex) {
			iex.exportHook(eMedical);
		}
		*/
		return ret;
	}

}
