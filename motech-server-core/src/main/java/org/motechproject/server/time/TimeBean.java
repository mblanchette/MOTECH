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

package org.motechproject.server.time;

import java.util.Calendar;
import java.util.Date;

import org.motechproject.server.model.MessageProgramEnrollment;
import org.motechproject.server.svc.OpenmrsBean;

public class TimeBean {

	private OpenmrsBean openmrsBean;

	public OpenmrsBean getOpenmrsBean() {
		return openmrsBean;
	}

	public void setOpenmrsBean(OpenmrsBean openmrsBean) {
		this.openmrsBean = openmrsBean;
	}

	public Date determineTime(TimePeriod timePeriod,
			TimeReference timeReference, Integer timeValue, Integer personId,
			MessageProgramEnrollment enrollment, String conceptName,
			String valueConceptName, Integer currentDoseNumber,
			String encounterTypeName) {

		if (timePeriod != null && timeReference != null && timeValue != null) {

			Calendar calendar = Calendar.getInstance();
			Date timeReferenceDate = null;

			if (personId == null && enrollment != null) {
				personId = enrollment.getPersonId();
			}

			switch (timeReference) {
			case patient_birthdate:
				if (personId != null) {
					timeReferenceDate = openmrsBean
							.getPatientBirthDate(personId);
				}
				break;
			case last_obs_date:
				if (personId != null && conceptName != null
						&& valueConceptName != null) {
					timeReferenceDate = openmrsBean.getLastObsDate(personId,
							conceptName, valueConceptName);
				}
				break;
			case last_dose_obs_date:
				if (personId != null && conceptName != null
						&& currentDoseNumber != null) {
					timeReferenceDate = openmrsBean.getLastDoseObsDate(
							personId, conceptName, currentDoseNumber - 1);
				}
				break;
			case last_dose_obs_date_current_pregnancy:
				if (personId != null && conceptName != null
						&& currentDoseNumber != null) {
					timeReferenceDate = openmrsBean
							.getLastDoseObsDateInActivePregnancy(personId,
									conceptName, currentDoseNumber - 1);
				}
				break;
			case last_obs_datevalue:
				if (personId != null && conceptName != null) {
					timeReferenceDate = openmrsBean.getLastObsValue(personId,
							conceptName);
				}
				break;
			case current_pregnancy_duedate:
				if (personId != null) {
					timeReferenceDate = openmrsBean
							.getActivePregnancyDueDate(personId);
				}
				break;
			case last_pregnancy_end_date:
				if (personId != null) {
					timeReferenceDate = openmrsBean
							.getLastPregnancyEndDate(personId);
				}
				break;
			case enrollment_startdate:
				if (enrollment != null) {
					timeReferenceDate = enrollment.getStartDate();
				}
				break;
			case enrollment_obs_datevalue:
				if (enrollment != null) {
					timeReferenceDate = openmrsBean.getObsValue(enrollment
							.getObsId());
				}
				break;
			}

			if (timeReferenceDate == null) {
				return null;
			}
			calendar.setTime(timeReferenceDate);

			switch (timePeriod) {
			case minute:
				calendar.add(Calendar.MINUTE, timeValue);
				break;
			case hour:
				calendar.add(Calendar.HOUR, timeValue);
				break;
			case day:
				calendar.add(Calendar.DATE, timeValue);
				break;
			case week:
				// Add weeks as days
				calendar.add(Calendar.DATE, timeValue * 7);
				break;
			case month:
				calendar.add(Calendar.MONTH, timeValue);
				break;
			case year:
				calendar.add(Calendar.YEAR, timeValue);
				break;
			}

			return calendar.getTime();
		}
		return null;
	}

}
