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

package org.motechproject.server.event.impl;

import java.util.Date;

import org.motechproject.server.model.MessageProgramEnrollment;
import org.motechproject.server.svc.OpenmrsBean;

public class SMSInputDemoStateTransitionImpl extends
		MessageProgramStateTransitionExpectedDateImpl {

	OpenmrsBean openmrsBean;

	boolean terminatingTransition = false;

	public void setOpenmrsBean(OpenmrsBean openmrsBean) {
		this.openmrsBean = openmrsBean;
	}

	public void setTerminating(boolean terminating) {
		this.terminatingTransition = terminating;
	}

	@Override
	public boolean evaluate(MessageProgramEnrollment enrollment,
			Date currentDate) {

		if (!terminatingTransition) {
			boolean trueBasedOnDate = super.evaluate(enrollment, currentDate);

			Date terminatingObsDate = openmrsBean.getLastObsCreationDate(
					enrollment.getPersonId(), prevState.getProgram()
							.getConceptName(), null);

			boolean terminatingObservationExists = terminatingObsDate != null
					&& enrollment.getStartDate().before(terminatingObsDate);

			return trueBasedOnDate && !terminatingObservationExists;
		} else {
			Date terminatingObsDate = openmrsBean.getLastObsCreationDate(
					enrollment.getPersonId(), prevState.getProgram()
							.getConceptName(), null);
			return terminatingObsDate != null
					&& enrollment.getStartDate().before(terminatingObsDate);
		}
	}
}
