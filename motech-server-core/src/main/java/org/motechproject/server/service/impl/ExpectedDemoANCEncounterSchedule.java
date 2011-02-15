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

package org.motechproject.server.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.motechproject.server.model.ExpectedEncounter;
import org.motechproject.server.service.ExpectedCareEvent;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;

public class ExpectedDemoANCEncounterSchedule extends ExpectedEncounterSchedule {

	private static Log log = LogFactory
			.getLog(ExpectedDemoANCEncounterSchedule.class);

	private String numberConceptName;
	private String referenceEncounterTypeName;
	private String referenceConceptName;

	/**
	 * Uses two dates for references: Latest Encounter date for reference
	 * encounter type (ANC registration) Latest Obs value as date for reference
	 * concept name (EDD)
	 * 
	 * Uses number concept Obs numeric value in the ANC encounter type to match
	 * Encounters Third event (ANC3) has requirements before the EDD and after
	 * the second event (ANC2)
	 */

	@Override
	protected void performScheduleUpdate(Patient patient, Date date) {

		Date ancRegistrationDate = getReferenceDate(patient);
		Date estimatedDeliveryDate = getSecondReferenceDate(patient);
		if (!validReferenceDate(ancRegistrationDate, date)
				|| !validReferenceDate(estimatedDeliveryDate, date)) {
			// Handle missing reference date as failed requirement
			log.debug("Failed to meet reference date requisites: "
					+ ancRegistrationDate + ", " + estimatedDeliveryDate
					+ ", removing events for schedule");

			removeExpectedCare(patient);
			return;
		}
		log.debug("Performing " + name + " schedule update: patient: "
				+ patient.getPatientId());

		List<Encounter> encounterList = openmrsBean.getEncounters(patient,
				encounterTypeName, ancRegistrationDate);
		List<ExpectedEncounter> expectedEncounterList = expectedCareBean
				.getExpectedEncounters(patient, name);
		List<ExpectedEncounter> expectedEncounterSaveList = new ArrayList<ExpectedEncounter>();

		EncounterPredicate encounterPredicate = new EncounterPredicate();
		encounterPredicate.setVisitNumberConcept(numberConceptName);
		ExpectedEncounterPredicate expectedEncounterPredicate = new ExpectedEncounterPredicate();

		Date previousEventEncounterDate = null;

		for (ExpectedCareEvent event : events) {
			// Calculate dates for event
			Date minDate = null;
			if (event.getNumber() == 3) {
				// ANC3 uses previous event date to schedule event min
				minDate = getMinDate(previousEventEncounterDate, event);
				previousEventEncounterDate = null;
			} else {
				minDate = getMinDate(ancRegistrationDate, event);
			}
			Date dueDate = null;
			if (event.getNumber() == 3) {
				// ANC3 uses estimated delivery date to schedule event due
				dueDate = getDueDate(estimatedDeliveryDate, event);
			} else if (Boolean.TRUE.equals(event.getDueReferencePrevious())) {
				// Use previous event's satisfying obs date as reference if
				// specified, clear previous obs date after
				dueDate = getDueDate(previousEventEncounterDate, event);
				previousEventEncounterDate = null;
			} else {
				dueDate = getDueDate(ancRegistrationDate, event);
			}

			Date lateDate = getLateDate(dueDate, event);
			Date maxDate = getMaxDate(ancRegistrationDate, event);
			// Set due date to min date if calculated due date is before min
			// date
			if (dueDate != null && minDate != null && dueDate.before(minDate)) {
				dueDate = minDate;
			}

			// Find Encounter satisfying event
			encounterPredicate.setMinDate(minDate);
			encounterPredicate.setMaxDate(maxDate);
			encounterPredicate.setVisitNumber(event.getNumber());
			Encounter eventEncounter = getEventEncounter(encounterList,
					encounterPredicate);

			// Store satisfying Encounter date for possible reference in next
			// event
			if (eventEncounter != null) {
				previousEventEncounterDate = eventEncounter
						.getEncounterDatetime();
				// Remove or avoid adding Expected Encounters before current
				// satisfied
				voidExpectedEncounters(expectedEncounterSaveList);
			}

			// Find ExpectedEncounter previously created for event
			expectedEncounterPredicate.setName(event.getName());
			ExpectedEncounter expectedEncounter = getEventExpectedEncounter(
					expectedEncounterList, expectedEncounterPredicate);

			boolean eventExpired = maxDate != null && date.after(maxDate);

			if (expectedEncounter != null) {
				if (eventEncounter != null) {
					// Remove existing satisfied ExpectedEncounter
					expectedEncounter.setEncounter(eventEncounter);
					expectedEncounter.setVoided(true);
				} else {
					// Update existing ExpectedEncounter, removing if expired
					expectedEncounter.setMinEncounterDatetime(minDate);
					if (dueDate != null) {
						expectedEncounter.setDueEncounterDatetime(dueDate);
					}
					if (lateDate != null) {
						expectedEncounter.setLateEncounterDatetime(lateDate);
					}
					expectedEncounter.setMaxEncounterDatetime(maxDate);
					if (eventExpired) {
						expectedEncounter.setVoided(true);
					}
				}
				expectedEncounterSaveList.add(expectedEncounter);

			} else if (!eventExpired && eventEncounter == null
					&& dueDate != null && lateDate != null) {
				// Create new ExpectedEncounter if not expired, not satisfied,
				// and due date and late date are defined
				ExpectedEncounter newExpectedEncounter = expectedCareBean
						.createExpectedEncounter(patient, encounterTypeName,
								minDate, dueDate, lateDate, maxDate, event
										.getName(), name);
				expectedEncounterSaveList.add(newExpectedEncounter);
			}
		}
		// Remove any remaining Expected Encounters that should not exist
		removeExpectedEncounters(expectedEncounterList);
		// Save Expected Encounters that should exist
		saveExpectedEncounters(expectedEncounterSaveList);
	}

	@Override
	protected Date getReferenceDate(Patient patient) {
		List<Encounter> encounterList = openmrsBean.getEncounters(patient,
				referenceEncounterTypeName, null);

		if (!encounterList.isEmpty()) {
			if (encounterList.size() > 1) {
				log.debug("Multiple matches of encounter ("
						+ referenceEncounterTypeName + ") for " + patient
						+ ": " + encounterList.size());
			}
			// List is ascending by date, remove latest match at end of list
			Encounter encounter = encounterList.get(encounterList.size() - 1);
			return encounter.getEncounterDatetime();
		}
		return null;
	}

	protected Date getSecondReferenceDate(Patient patient) {
		List<Obs> obsList = openmrsBean.getObs(patient, referenceConceptName,
				null, null);

		if (!obsList.isEmpty()) {
			if (obsList.size() > 1) {
				log.debug("Multiple matches of obs (" + referenceConceptName
						+ ") for " + patient + ": " + obsList.size());
			}
			// List is descending by date, remove latest match at beginning of
			// list
			Obs obs = obsList.get(0);
			return obs.getValueDatetime();
		}
		return null;
	}

	public String getNumberConceptName() {
		return numberConceptName;
	}

	public void setNumberConceptName(String numberConceptName) {
		this.numberConceptName = numberConceptName;
	}

	public String getReferenceEncounterTypeName() {
		return referenceEncounterTypeName;
	}

	public void setReferenceEncounterTypeName(String referenceEncounterTypeName) {
		this.referenceEncounterTypeName = referenceEncounterTypeName;
	}

	public String getReferenceConceptName() {
		return referenceConceptName;
	}

	public void setReferenceConceptName(String referenceConceptName) {
		this.referenceConceptName = referenceConceptName;
	}

	protected void voidExpectedEncounters(
			List<ExpectedEncounter> expectedEncounterList) {
		for (ExpectedEncounter expectedEncounter : expectedEncounterList) {
			expectedEncounter.setVoided(true);
		}
	}

	protected void saveExpectedEncounters(
			List<ExpectedEncounter> expectedEncounterList) {
		for (ExpectedEncounter expectedEncounter : expectedEncounterList) {
			// Avoid saving newly created Expected Encounters that were voided
			if (!expectedEncounter.getVoided()
					|| expectedEncounter.getId() != null)
				expectedCareBean.saveExpectedEncounter(expectedEncounter);
		}
	}

}
