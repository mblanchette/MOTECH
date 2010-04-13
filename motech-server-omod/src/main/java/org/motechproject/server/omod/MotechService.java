/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.motechproject.server.omod;

import java.util.Date;
import java.util.List;

import org.motechproject.server.model.Blackout;
import org.motechproject.server.model.ExpectedEncounter;
import org.motechproject.server.model.ExpectedObs;
import org.motechproject.server.model.GeneralPatientEncounter;
import org.motechproject.server.model.Log;
import org.motechproject.server.model.Message;
import org.motechproject.server.model.MessageAttribute;
import org.motechproject.server.model.MessageDefinition;
import org.motechproject.server.model.MessageProgramEnrollment;
import org.motechproject.server.model.MessageStatus;
import org.motechproject.server.model.ScheduledMessage;
import org.motechproject.server.model.TroubledPhone;
import org.motechproject.server.omod.sdsched.ScheduleMaintService;
import org.motechproject.server.svc.OpenmrsBean;
import org.motechproject.server.svc.RegistrarBean;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service interface for much of 'real work' for the motech server OpenMRS
 * module.
 */
public interface MotechService extends OpenmrsService {

	RegistrarBean getRegistrarBean();
	
	OpenmrsBean getOpenmrsBean();

	ScheduleMaintService getScheduleMaintService();

	@Transactional
	ScheduledMessage saveScheduledMessage(ScheduledMessage scheduledMessage);

	@Transactional
	Message saveMessage(Message message);

	@Transactional
	MessageDefinition saveMessageDefinition(MessageDefinition messageDefinition);

	@Transactional
	MessageAttribute saveMessageAttribute(MessageAttribute messageAttribute);

	@Transactional
	Log saveLog(Log log);

	@Transactional(readOnly = true)
	List<ScheduledMessage> getAllScheduledMessages();

	@Transactional(readOnly = true)
	List<ScheduledMessage> getScheduledMessages(Date startDate, Date endDate);

	@Transactional(readOnly = true)
	public List<ScheduledMessage> getScheduledMessages(Integer recipientId,
			MessageDefinition definition, MessageProgramEnrollment enrollment,
			Date messageDate);

	@Transactional(readOnly = true)
	List<Message> getAllMessages();

	@Transactional(readOnly = true)
	List<Message> getMessages(ScheduledMessage scheduledMessage);

	@Transactional(readOnly = true)
	List<Message> getMessages(Date startDate, Date endDate, MessageStatus status);

	@Transactional(readOnly = true)
	List<Message> getMessages(MessageProgramEnrollment enrollment,
			MessageStatus status);

	@Transactional(readOnly = true)
	List<Message> getMessages(Integer recipientId,
			MessageProgramEnrollment enrollment, MessageDefinition definition,
			Date messageDate, MessageStatus status);

	@Transactional(readOnly = true)
	Message getMessage(String publicId);

	@Transactional(readOnly = true)
	List<MessageDefinition> getAllMessageDefinitions();

	@Transactional(readOnly = true)
	MessageDefinition getMessageDefinition(String messageKey);

	@Transactional(readOnly = true)
	List<MessageAttribute> getAllMessageAttributes();

	@Transactional(readOnly = true)
	List<Log> getAllLogs();

	@Transactional(readOnly = true)
	List<Integer> getUserIdsByPersonAttribute(
			PersonAttributeType personAttributeType, String value);

	@Transactional(readOnly = true)
	List<MessageProgramEnrollment> getAllActiveMessageProgramEnrollments();

	@Transactional(readOnly = true)
	List<MessageProgramEnrollment> getActiveMessageProgramEnrollments(
			Integer personId);

	@Transactional(readOnly = true)
	List<MessageProgramEnrollment> getActiveMessageProgramEnrollments(
			Integer personId, String program);

	@Transactional(readOnly = true)
	List<MessageProgramEnrollment> getActiveMessageProgramEnrollments(
			Integer personId, String program, Integer obsId);

	@Transactional
	MessageProgramEnrollment saveMessageProgramEnrollment(
			MessageProgramEnrollment enrollment);

	@Transactional(readOnly = true)
	Blackout getBlackoutSettings();

	@Transactional
	void setBlackoutSettings(Blackout blackout);

	@Transactional(readOnly = true)
	TroubledPhone getTroubledPhone(String phoneNumber);

	@Transactional
	void saveTroubledPhone(TroubledPhone troubledPhone);

	@Transactional
	void addTroubledPhone(String phoneNumber);

	@Transactional
	void removeTroubledPhone(String phoneNumber);

	@Transactional
	GeneralPatientEncounter saveGeneralPatientEncounter(
			GeneralPatientEncounter encounter);

	@Transactional(readOnly = true)
	List<Location> getAllCountries();

	@Transactional(readOnly = true)
	List<Location> getAllRegions();

	@Transactional(readOnly = true)
	List<Location> getRegions(String country);

	@Transactional(readOnly = true)
	List<Location> getAllDistricts();

	@Transactional(readOnly = true)
	List<Location> getDistricts(String country, String region);

	@Transactional(readOnly = true)
	List<Location> getAllCommunities();

	@Transactional(readOnly = true)
	List<Location> getCommunities(String country, String region, String district);

	@Transactional(readOnly = true)
	List<Location> getAllClinics();

	@Transactional(readOnly = true)
	List<Location> getClinics(String country, String region, String district,
			String community);

	@Transactional(readOnly = true)
	List<Integer> getMatchingPeople(String firstName, String lastName,
			Date birthDate, String community, String phoneNumber,
			Integer primaryPhoneNumberAttrTypeId,
			Integer secondaryPhoneNumberAttrTypeId, String patientId,
			String nhisNumber, Integer nhisAttrTypeId);

	@Transactional(readOnly = true)
	List<Obs> getActivePregnancies(Integer patientId, Concept pregnancyConcept,
			Concept pregnancyStatusConcept);

	@Transactional(readOnly = true)
	List<Obs> getActivePregnanciesDueDateObs(Date fromDueDate, Date toDueDate,
			Concept pregnancyDueDateConcept, Concept pregnancyConcept,
			Concept pregnancyStatusConcept);

	@Transactional
	ExpectedObs saveExpectedObs(ExpectedObs expectedObs);

	@Transactional(readOnly = true)
	List<ExpectedObs> getExpectedObs(Patient patient, String[] groups,
			Date minDueDate, Date maxDueDate, Date maxLateDate,
			Date minMaxDate, boolean nameOrdering);

	@Transactional
	ExpectedEncounter saveExpectedEncounter(ExpectedEncounter expectedEncounter);

	@Transactional(readOnly = true)
	List<ExpectedEncounter> getExpectedEncounter(Patient patient,
			String[] groups, Date minDueDate, Date maxDueDate,
			Date maxLateDate, Date minMaxDate, boolean nameOrdering);

	@Transactional(readOnly = true)
	List<Patient> getPatients(String firstName, String lastName,
			String preferredName, Date birthDate, String community,
			String phoneNumber, PersonAttributeType primaryPhoneNumberAttrType,
			PersonAttributeType secondaryPhoneNumberAttrType,
			String nhisNumber, PersonAttributeType nhisAttrType);
}