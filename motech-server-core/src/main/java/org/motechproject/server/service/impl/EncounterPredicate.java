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

import java.util.Date;

import org.apache.commons.collections.Predicate;
import org.openmrs.Encounter;
import org.openmrs.Obs;

public class EncounterPredicate implements Predicate {

	private Date minDate;
	private Date maxDate;
	private Integer visitNumber;
	private String visitNumberConcept;

	public boolean evaluate(Object input) {
		if (input instanceof Encounter) {
			Encounter enc = (Encounter) input;
			Date encounterDate = enc.getEncounterDatetime();

			boolean matchVisitNumber = true;
			if (visitNumber != null && visitNumberConcept != null) {
				matchVisitNumber = visitNumber
						.equals(getEncounterVisitNumber(enc));
			}

			boolean afterMinDate = true;
			if (minDate != null) {
				afterMinDate = encounterDate.after(minDate);
			}

			boolean beforeMaxDate = true;
			if (maxDate != null) {
				beforeMaxDate = encounterDate.before(maxDate);
			}

			return matchVisitNumber && afterMinDate && beforeMaxDate;
		}
		return false;
	}

	protected Integer getEncounterVisitNumber(Encounter enc) {
		for (Obs obs : enc.getAllObs()) {
			if (obs.getConcept().isNamed(visitNumberConcept)) {
				return obs.getValueNumeric().intValue();
			}
		}
		return null;
	}

	public Date getMinDate() {
		return minDate;
	}

	public void setMinDate(Date minDate) {
		this.minDate = minDate;
	}

	public Date getMaxDate() {
		return maxDate;
	}

	public void setMaxDate(Date maxDate) {
		this.maxDate = maxDate;
	}

	public Integer getVisitNumber() {
		return visitNumber;
	}

	public void setVisitNumber(Integer visitNumber) {
		this.visitNumber = visitNumber;
	}

	public String getVisitNumberConcept() {
		return visitNumberConcept;
	}

	public void setVisitNumberConcept(String visitNumberConcept) {
		this.visitNumberConcept = visitNumberConcept;
	}

}
