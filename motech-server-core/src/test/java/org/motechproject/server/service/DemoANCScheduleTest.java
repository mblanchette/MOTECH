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

package org.motechproject.server.service;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.Capture;
import org.motechproject.server.model.ExpectedEncounter;
import org.motechproject.server.service.impl.ExpectedDemoANCEncounterSchedule;
import org.motechproject.server.svc.ExpectedCareBean;
import org.motechproject.server.svc.OpenmrsBean;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DemoANCScheduleTest extends TestCase {
	ApplicationContext ctx;

	OpenmrsBean openmrsBean;
	ExpectedCareBean expectedCareBean;
	ExpectedDemoANCEncounterSchedule ancSchedule;
	Concept visitNumberConcept;

	@Override
	protected void setUp() throws Exception {
		ctx = new ClassPathXmlApplicationContext(new String[] {
				"test-common-program-beans.xml",
				"services/pregnancy-demo-anc-service.xml" });
		ancSchedule = (ExpectedDemoANCEncounterSchedule) ctx
				.getBean("pregnancyDemoANCSchedule");

		// EasyMock setup in Spring config
		openmrsBean = (OpenmrsBean) ctx.getBean("openmrsBean");
		expectedCareBean = (ExpectedCareBean) ctx.getBean("expectedCareBean");

		visitNumberConcept = new Concept(1);
		visitNumberConcept.addName(new ConceptName(ancSchedule
				.getNumberConceptName(), null));
	}

	@Override
	protected void tearDown() throws Exception {
		ctx = null;
		ancSchedule = null;
		openmrsBean = null;
		expectedCareBean = null;
		visitNumberConcept = null;
	}

	public void testAllANCNoExpected() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2010, 0, 1);
		Date ancRegDate = calendar.getTime();
		calendar.set(2010, 6, 1);
		Date estConfDate = calendar.getTime();
		calendar.set(2010, 1, 1);
		Date anc1Date = calendar.getTime();
		calendar.set(2010, 3, 1);
		Date anc2Date = calendar.getTime();
		calendar.set(2010, 5, 7);
		Date anc3Date = calendar.getTime();
		calendar.set(2010, 5, 17);
		Date currentDate = calendar.getTime();

		Patient patient = new Patient(1);

		List<Encounter> ancRegEncounterList = new ArrayList<Encounter>();
		Encounter regEncounter1 = new Encounter();
		regEncounter1.setEncounterDatetime(ancRegDate);
		ancRegEncounterList.add(regEncounter1);

		List<Obs> estDateObsList = new ArrayList<Obs>();
		Obs estDateObs1 = new Obs();
		estDateObs1.setValueDatetime(estConfDate);
		estDateObsList.add(estDateObs1);

		List<Encounter> ancEncounterList = new ArrayList<Encounter>();
		Encounter ancEncounter1 = new Encounter(1); // ANC1
		ancEncounter1.setEncounterDatetime(anc1Date);
		Obs visitNumberObs1 = new Obs();
		visitNumberObs1.setConcept(visitNumberConcept);
		visitNumberObs1.setValueNumeric(1.0);
		ancEncounter1.addObs(visitNumberObs1);
		ancEncounterList.add(ancEncounter1);
		Encounter ancEncounter2 = new Encounter(2); // ANC2
		ancEncounter2.setEncounterDatetime(anc2Date);
		Obs visitNumberObs2 = new Obs();
		visitNumberObs2.setConcept(visitNumberConcept);
		visitNumberObs2.setValueNumeric(2.0);
		ancEncounter2.addObs(visitNumberObs2);
		ancEncounterList.add(ancEncounter2);
		Encounter ancEncounter3 = new Encounter(3); // ANC2
		ancEncounter3.setEncounterDatetime(anc3Date);
		Obs visitNumberObs3 = new Obs();
		visitNumberObs3.setConcept(visitNumberConcept);
		visitNumberObs3.setValueNumeric(3.0);
		ancEncounter3.addObs(visitNumberObs3);
		ancEncounterList.add(ancEncounter3);

		List<ExpectedEncounter> expectedEncounterList = new ArrayList<ExpectedEncounter>();

		expect(
				openmrsBean.getEncounters(eq(patient), eq(ancSchedule
						.getReferenceEncounterTypeName()), (Date) anyObject()))
				.andReturn(ancRegEncounterList);
		expect(
				openmrsBean.getObs(eq(patient), eq(ancSchedule
						.getReferenceConceptName()), (String) anyObject(),
						(Date) anyObject())).andReturn(estDateObsList);
		expect(
				openmrsBean.getEncounters(eq(patient), eq(ancSchedule
						.getEncounterTypeName()), (Date) anyObject()))
				.andReturn(ancEncounterList);
		expect(
				expectedCareBean.getExpectedEncounters(patient, ancSchedule
						.getName())).andReturn(expectedEncounterList);

		replay(openmrsBean, expectedCareBean);

		ancSchedule.updateSchedule(patient, currentDate);

		verify(openmrsBean, expectedCareBean);
	}

	public void testAllANCAllExpected() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2010, 0, 1);
		Date ancRegDate = calendar.getTime();
		calendar.set(2010, 6, 1);
		Date estConfDate = calendar.getTime();
		calendar.set(2010, 1, 1);
		Date anc1Date = calendar.getTime();
		calendar.set(2010, 3, 1);
		Date anc2Date = calendar.getTime();
		calendar.set(2010, 5, 7);
		Date anc3Date = calendar.getTime();
		calendar.set(2010, 5, 17);
		Date currentDate = calendar.getTime();

		Patient patient = new Patient(1);

		Capture<ExpectedEncounter> anc1ExpectedCapture = new Capture<ExpectedEncounter>();
		Capture<ExpectedEncounter> anc2ExpectedCapture = new Capture<ExpectedEncounter>();
		Capture<ExpectedEncounter> anc3ExpectedCapture = new Capture<ExpectedEncounter>();

		List<Encounter> ancRegEncounterList = new ArrayList<Encounter>();
		Encounter regEncounter1 = new Encounter();
		regEncounter1.setEncounterDatetime(ancRegDate);
		ancRegEncounterList.add(regEncounter1);

		List<Obs> estDateObsList = new ArrayList<Obs>();
		Obs estDateObs1 = new Obs();
		estDateObs1.setValueDatetime(estConfDate);
		estDateObsList.add(estDateObs1);

		List<Encounter> ancEncounterList = new ArrayList<Encounter>();
		Encounter ancEncounter1 = new Encounter(1); // ANC1
		ancEncounter1.setEncounterDatetime(anc1Date);
		Obs visitNumberObs1 = new Obs();
		visitNumberObs1.setConcept(visitNumberConcept);
		visitNumberObs1.setValueNumeric(1.0);
		ancEncounter1.addObs(visitNumberObs1);
		ancEncounterList.add(ancEncounter1);
		Encounter ancEncounter2 = new Encounter(2); // ANC2
		ancEncounter2.setEncounterDatetime(anc2Date);
		Obs visitNumberObs2 = new Obs();
		visitNumberObs2.setConcept(visitNumberConcept);
		visitNumberObs2.setValueNumeric(2.0);
		ancEncounter2.addObs(visitNumberObs2);
		ancEncounterList.add(ancEncounter2);
		Encounter ancEncounter3 = new Encounter(3); // ANC2
		ancEncounter3.setEncounterDatetime(anc3Date);
		Obs visitNumberObs3 = new Obs();
		visitNumberObs3.setConcept(visitNumberConcept);
		visitNumberObs3.setValueNumeric(3.0);
		ancEncounter3.addObs(visitNumberObs3);
		ancEncounterList.add(ancEncounter3);

		// IDs required, since expected encounters are only saved voided if
		// updated, ID exists
		List<ExpectedEncounter> expectedEncounterList = new ArrayList<ExpectedEncounter>();
		ExpectedEncounter expectedEncounter1 = new ExpectedEncounter();
		expectedEncounter1.setId(1L);
		expectedEncounter1.setName("ANC1");
		expectedEncounterList.add(expectedEncounter1);
		ExpectedEncounter expectedEncounter2 = new ExpectedEncounter();
		expectedEncounter2.setId(2L);
		expectedEncounter2.setName("ANC2");
		expectedEncounterList.add(expectedEncounter2);
		ExpectedEncounter expectedEncounter3 = new ExpectedEncounter();
		expectedEncounter3.setId(3L);
		expectedEncounter3.setName("ANC3");
		expectedEncounterList.add(expectedEncounter3);

		expect(
				openmrsBean.getEncounters(eq(patient), eq(ancSchedule
						.getReferenceEncounterTypeName()), (Date) anyObject()))
				.andReturn(ancRegEncounterList);
		expect(
				openmrsBean.getObs(eq(patient), eq(ancSchedule
						.getReferenceConceptName()), (String) anyObject(),
						(Date) anyObject())).andReturn(estDateObsList);
		expect(
				openmrsBean.getEncounters(eq(patient), eq(ancSchedule
						.getEncounterTypeName()), (Date) anyObject()))
				.andReturn(ancEncounterList);
		expect(
				expectedCareBean.getExpectedEncounters(patient, ancSchedule
						.getName())).andReturn(expectedEncounterList);

		expect(
				expectedCareBean
						.saveExpectedEncounter(capture(anc1ExpectedCapture)))
				.andReturn(new ExpectedEncounter());
		expect(
				expectedCareBean
						.saveExpectedEncounter(capture(anc2ExpectedCapture)))
				.andReturn(new ExpectedEncounter());
		expect(
				expectedCareBean
						.saveExpectedEncounter(capture(anc3ExpectedCapture)))
				.andReturn(new ExpectedEncounter());

		replay(openmrsBean, expectedCareBean);

		ancSchedule.updateSchedule(patient, currentDate);

		verify(openmrsBean, expectedCareBean);

		ExpectedEncounter capturedANC1Expected = anc1ExpectedCapture.getValue();
		assertEquals(Boolean.TRUE, capturedANC1Expected.getVoided());
		assertEquals(ancEncounter1, capturedANC1Expected.getEncounter());

		ExpectedEncounter capturedANC2Expected = anc2ExpectedCapture.getValue();
		assertEquals(Boolean.TRUE, capturedANC2Expected.getVoided());
		assertEquals(ancEncounter2, capturedANC2Expected.getEncounter());

		ExpectedEncounter capturedANC3Expected = anc3ExpectedCapture.getValue();
		assertEquals(Boolean.TRUE, capturedANC3Expected.getVoided());
		assertEquals(ancEncounter3, capturedANC3Expected.getEncounter());
	}

}
