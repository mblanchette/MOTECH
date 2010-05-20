package org.motechproject.server.omod.web.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.motechproject.server.util.GenderTypeConverter;
import org.motechproject.server.util.MotechConstants;
import org.motechproject.ws.ContactNumberType;
import org.motechproject.ws.InterestReason;
import org.motechproject.ws.MediaType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;

public class WebModelConverterImpl implements WebModelConverter {

	private final Log log = LogFactory.getLog(WebModelConverterImpl.class);

	public void patientToWeb(Patient patient, WebPatient webPatient) {

		PatientIdentifier patientId = patient
				.getPatientIdentifier(MotechConstants.PATIENT_IDENTIFIER_MOTECH_ID);
		if (patientId != null) {
			Integer motechId = null;
			try {
				motechId = Integer.parseInt(patientId.getIdentifier());
			} catch (Exception e) {
				log.error("Unable to parse Motech ID: "
						+ patientId.getIdentifier(), e);
			}
			webPatient.setMotechId(motechId);
		}

		webPatient.setId(patient.getPersonId());
		for (PersonName name : patient.getNames()) {
			if (!name.isPreferred() && name.getGivenName() != null) {
				webPatient.setFirstName(name.getGivenName());
				break;
			}
		}
		webPatient.setLastName(patient.getFamilyName());
		webPatient.setPrefName(patient.getGivenName());
		webPatient.setBirthDate(patient.getBirthdate());
		webPatient.setBirthDateEst(patient.getBirthdateEstimated());
		webPatient.setSex(GenderTypeConverter.valueOfOpenMRS(patient
				.getGender()));

		PersonAddress patientAddress = patient.getPersonAddress();
		if (patientAddress != null) {
			webPatient.setRegion(patientAddress.getRegion());
			webPatient.setDistrict(patientAddress.getCountyDistrict());
			webPatient.setCommunity(patientAddress.getCityVillage());
			webPatient.setAddress(patientAddress.getAddress1());
		}

		// TODO: populate registerPregProgram

		PersonAttribute primaryPhoneAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_PHONE_NUMBER);
		if (primaryPhoneAttr != null) {
			webPatient.setPrimaryPhone(primaryPhoneAttr.getValue());
		}

		PersonAttribute primaryPhoneTypeAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_PHONE_TYPE);
		if (primaryPhoneTypeAttr != null) {
			webPatient.setPrimaryPhoneType(ContactNumberType
					.valueOf(primaryPhoneTypeAttr.getValue()));
		}

		PersonAttribute secondaryPhoneAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_PHONE_NUMBER);
		if (secondaryPhoneAttr != null) {
			webPatient.setSecondaryPhone(secondaryPhoneAttr.getValue());
		}

		PersonAttribute secondaryPhoneTypeAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_PHONE_TYPE);
		if (secondaryPhoneTypeAttr != null) {
			webPatient.setSecondaryPhoneType(ContactNumberType
					.valueOf(secondaryPhoneTypeAttr.getValue()));
		}

		PersonAttribute mediaTypeInfoAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
		if (mediaTypeInfoAttr != null) {
			webPatient.setMediaTypeInfo(MediaType.valueOf(mediaTypeInfoAttr
					.getValue()));
		}

		PersonAttribute mediaTypeReminderAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
		if (mediaTypeReminderAttr != null) {
			webPatient.setMediaTypeReminder(MediaType
					.valueOf(mediaTypeReminderAttr.getValue()));
		}

		PersonAttribute languageVoiceAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE);
		if (languageVoiceAttr != null) {
			webPatient.setLanguageVoice(languageVoiceAttr.getValue());
		}

		PersonAttribute languageTextAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE);
		if (languageTextAttr != null) {
			webPatient.setLanguageText(languageTextAttr.getValue());
		}

		PersonAttribute interestReasonAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_INTEREST_REASON);
		if (interestReasonAttr != null) {
			webPatient.setInterestReason(InterestReason
					.valueOf(interestReasonAttr.getValue()));
		}

		PersonAttribute howLearnedAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_HOW_LEARNED);
		if (howLearnedAttr != null) {
			webPatient.setHowLearned(howLearnedAttr.getValue());
		}

		PersonAttribute nhisExpDateAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_NHIS_EXP_DATE);
		if (nhisExpDateAttr != null) {
			Date nhisExpDate = null;
			String nhisExpDateString = nhisExpDateAttr.getValue();
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						MotechConstants.DATE_FORMAT);
				nhisExpDate = dateFormat.parse(nhisExpDateString);
			} catch (ParseException e) {
				log.error("Cannot parse NHIS Expiration Date: "
						+ nhisExpDateString, e);
			}
			webPatient.setNhisExpDate(nhisExpDate);
		}

		PersonAttribute insuredAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_INSURED);
		if (insuredAttr != null) {
			webPatient.setInsured(Boolean.valueOf(insuredAttr.getValue()));
		}

		PersonAttribute nhisAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_NHIS_NUMBER);
		if (nhisAttr != null) {
			webPatient.setNhis(nhisAttr.getValue());
		}

		// TODO: populate dueDate
	}

}
