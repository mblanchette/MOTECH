/**
 * MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT
 *
 * Copyright (c) 2010 The Trustees of Columbia University in the City of
 * New York and Grameen Foundation USA.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Grameen Foundation USA, Columbia University, or
 * their respective contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA, COLUMBIA UNIVERSITY
 * AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION
 * USA, COLUMBIA UNIVERSITY OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.motechproject.server.omod.web.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.motechproject.server.model.Community;
import org.motechproject.server.model.ExpectedEncounter;
import org.motechproject.server.model.ExpectedObs;
import org.motechproject.server.svc.MessageBean;
import org.motechproject.server.svc.OpenmrsBean;
import org.motechproject.server.util.GenderTypeConverter;
import org.motechproject.server.util.MotechConstants;
import org.motechproject.ws.ContactNumberType;
import org.motechproject.ws.DayOfWeek;
import org.motechproject.ws.HowLearned;
import org.motechproject.ws.InterestReason;
import org.motechproject.ws.MediaType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;

public class WebModelConverterImpl implements WebModelConverter {

	private final Log log = LogFactory.getLog(WebModelConverterImpl.class);

	OpenmrsBean openmrsBean;
	MessageBean messageBean;

	public void setOpenmrsBean(OpenmrsBean openmrsBean) {
		this.openmrsBean = openmrsBean;
	}

	public void setMessageBean(MessageBean messageBean) {
		this.messageBean = messageBean;
	}

	public void patientToWeb(Patient patient, WebPatient webPatient) {

		PatientIdentifier patientId = patient
				.getPatientIdentifier(MotechConstants.PATIENT_IDENTIFIER_MOTECH_ID);
		if (patientId != null) {
			Integer motechId = null;
			try {
				motechId = Integer.parseInt(patientId.getIdentifier());
			} catch (Exception e) {
				log.error("Unable to parse Motech ID: "
						+ patientId.getIdentifier() + ", for Patient ID:"
						+ patient.getPatientId(), e);
			}
			webPatient.setMotechId(motechId);
		}

		webPatient.setId(patient.getPatientId());
		for (PersonName name : patient.getNames()) {
			if (name.isPreferred()) {
				webPatient.setPrefName(name.getGivenName());
			} else {
				webPatient.setFirstName(name.getGivenName());
			}
		}
		webPatient.setMiddleName(patient.getMiddleName());
		webPatient.setLastName(patient.getFamilyName());
		webPatient.setBirthDate(patient.getBirthdate());
		webPatient.setBirthDateEst(patient.getBirthdateEstimated());
		webPatient.setSex(GenderTypeConverter.valueOfOpenMRS(patient
				.getGender()));

		PersonAddress patientAddress = patient.getPersonAddress();
		if (patientAddress != null) {
			webPatient.setAddress(patientAddress.getAddress1());
		}

		Integer motherMotechId = openmrsBean.getMotherMotechId(patient);
		webPatient.setMotherMotechId(motherMotechId);

		Community community = openmrsBean.getCommunityByPatient(patient);
		if (community != null) {
			webPatient.setCommunityId(community.getCommunityId());
			webPatient.setCommunityName(community.getName());

			if (community.getFacility() != null
					&& community.getFacility().getLocation() != null) {
				Location facilityLocation = community.getFacility()
						.getLocation();
				webPatient.setRegion(facilityLocation.getRegion());
				webPatient.setDistrict(facilityLocation.getCountyDistrict());
			}
		}

		String[] enrollments = messageBean
				.getActiveMessageProgramEnrollmentNames(patient);
		if (enrollments != null && enrollments.length > 0) {
			webPatient.setEnroll(true);
			webPatient.setConsent(true);
		} else {
			webPatient.setEnroll(false);
			webPatient.setConsent(false);
		}

		PersonAttribute phoneNumberAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_PHONE_NUMBER);
		if (phoneNumberAttr != null
				&& StringUtils.isNotEmpty(phoneNumberAttr.getValue())) {
			webPatient.setPhoneNumber(phoneNumberAttr.getValue());
		}

		PersonAttribute phoneTypeAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_PHONE_TYPE);
		if (phoneTypeAttr != null
				&& StringUtils.isNotEmpty(phoneTypeAttr.getValue())) {
			ContactNumberType phoneType = null;
			try {
				phoneType = ContactNumberType.valueOf(phoneTypeAttr.getValue());
			} catch (Exception e) {
				log.error("Unable to parse phone type: "
						+ phoneTypeAttr.getValue() + ", for Patient ID:"
						+ patient.getPatientId(), e);
			}
			webPatient.setPhoneType(phoneType);
		}

		PersonAttribute mediaTypeAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
		if (mediaTypeAttr != null
				&& StringUtils.isNotEmpty(mediaTypeAttr.getValue())) {
			MediaType mediaType = null;
			try {
				mediaType = MediaType.valueOf(mediaTypeAttr.getValue());
			} catch (Exception e) {
				log.error("Unable to parse media type: "
						+ mediaTypeAttr.getValue() + ", for Patient ID:"
						+ patient.getPatientId(), e);
			}
			webPatient.setMediaType(mediaType);
		}

		PersonAttribute languageAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE);
		if (languageAttr != null
				&& StringUtils.isNotEmpty(languageAttr.getValue())) {
			webPatient.setLanguage(languageAttr.getValue());
		}

		PersonAttribute dayOfWeekAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_DELIVERY_DAY);
		if (dayOfWeekAttr != null
				&& StringUtils.isNotEmpty(dayOfWeekAttr.getValue())) {
			DayOfWeek dayOfWeek = null;
			try {
				dayOfWeek = DayOfWeek.valueOf(dayOfWeekAttr.getValue());
			} catch (Exception e) {
				log.error("Unable to parse day of week: "
						+ dayOfWeekAttr.getValue() + ", for Patient ID:"
						+ patient.getPatientId(), e);
			}
			webPatient.setDayOfWeek(dayOfWeek);
		}

		PersonAttribute timeOfDayAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_DELIVERY_TIME);
		if (timeOfDayAttr != null
				&& StringUtils.isNotEmpty(timeOfDayAttr.getValue())) {
			Date timeOfDay = null;
			String timeOfDayString = timeOfDayAttr.getValue();
			try {
				SimpleDateFormat timeFormat = new SimpleDateFormat(
						MotechConstants.TIME_FORMAT_DELIVERY_TIME);
				timeOfDay = timeFormat.parse(timeOfDayString);
			} catch (ParseException e) {
				log.error("Cannot parse time of day Date: " + timeOfDayString
						+ ", for Patient ID:" + patient.getPatientId(), e);
			}
			webPatient.setTimeOfDay(timeOfDay);
		}

		PersonAttribute interestReasonAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_INTEREST_REASON);
		if (interestReasonAttr != null
				&& StringUtils.isNotEmpty(interestReasonAttr.getValue())) {
			InterestReason interestReason = null;
			try {
				interestReason = InterestReason.valueOf(interestReasonAttr
						.getValue());
			} catch (Exception e) {
				log.error("Unable to parse interest reason: "
						+ interestReasonAttr.getValue() + ", for Patient ID:"
						+ patient.getPatientId(), e);
			}
			webPatient.setInterestReason(interestReason);
		}

		PersonAttribute howLearnedAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_HOW_LEARNED);
		if (howLearnedAttr != null
				&& StringUtils.isNotEmpty(howLearnedAttr.getValue())) {
			HowLearned howLearned = null;
			try {
				howLearned = HowLearned.valueOf(howLearnedAttr.getValue());
			} catch (Exception e) {
				log.error("Unable to parse how learned: "
						+ howLearnedAttr.getValue() + ", for Patient ID:"
						+ patient.getPatientId(), e);
			}
			webPatient.setHowLearned(howLearned);
		}

		PersonAttribute nhisExpDateAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_NHIS_EXP_DATE);
		if (nhisExpDateAttr != null
				&& StringUtils.isNotEmpty(nhisExpDateAttr.getValue())) {
			Date nhisExpDate = null;
			String nhisExpDateString = nhisExpDateAttr.getValue();
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						MotechConstants.DATE_FORMAT);
				nhisExpDate = dateFormat.parse(nhisExpDateString);
			} catch (ParseException e) {
				log.error("Cannot parse NHIS Expiration Date: "
						+ nhisExpDateString + ", for Patient ID:"
						+ patient.getPatientId(), e);
			}
			webPatient.setNhisExpDate(nhisExpDate);
		}

		PersonAttribute insuredAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_INSURED);
		if (insuredAttr != null
				&& StringUtils.isNotEmpty(insuredAttr.getValue())) {
			webPatient.setInsured(Boolean.valueOf(insuredAttr.getValue()));
		}

		PersonAttribute nhisAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_NHIS_NUMBER);
		if (nhisAttr != null && StringUtils.isNotEmpty(nhisAttr.getValue())) {
			webPatient.setNhis(nhisAttr.getValue());
		}

		webPatient.setDueDate(openmrsBean.getActivePregnancyDueDate(patient
				.getPatientId()));
	}

	public WebSimplePatient patientToSimpleWeb(Patient patient) {

		WebSimplePatient webPatient = new WebSimplePatient();
		webPatient.setId(patient.getPatientId());

		PersonName name = patient.getPersonName();
		if (name != null) {
			webPatient.setName(name.toString());
		}
		return webPatient;
	}

	public List<WebSimplePatient> expectedToWebPatients(
			List<ExpectedEncounter> expectedEncounters,
			List<ExpectedObs> expectedObservations) {
		Map<Integer, WebSimplePatient> patients = new HashMap<Integer, WebSimplePatient>();

		for (ExpectedEncounter expectedEncounter : expectedEncounters) {
			WebCare care = new WebCare();
			care.setName(expectedEncounter.getName());
			care.setDueDate(expectedEncounter.getDueEncounterDatetime());
			care.setLateDate(expectedEncounter.getLateEncounterDatetime());

			Patient patient = expectedEncounter.getPatient();
			Integer patientId = patient.getPatientId();
			WebSimplePatient webPatient = patients.get(patientId);
			if (webPatient == null) {
				webPatient = patientToSimpleWeb(patient);
			}
			webPatient.addCare(care);
			patients.put(patientId, webPatient);
		}
		for (ExpectedObs expectedObs : expectedObservations) {
			WebCare care = new WebCare();
			care.setName(expectedObs.getName());
			care.setDueDate(expectedObs.getDueObsDatetime());
			care.setLateDate(expectedObs.getLateObsDatetime());

			Patient patient = expectedObs.getPatient();
			Integer patientId = patient.getPatientId();
			WebSimplePatient webPatient = patients.get(patientId);
			if (webPatient == null) {
				webPatient = patientToSimpleWeb(patient);
			}
			webPatient.addCare(care);
			patients.put(patientId, webPatient);
		}

		return new ArrayList<WebSimplePatient>(patients.values());
	}

}
