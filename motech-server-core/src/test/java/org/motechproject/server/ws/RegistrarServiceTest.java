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

package org.motechproject.server.ws;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.easymock.Capture;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.motechproject.server.model.Community;
import org.motechproject.server.model.ExpectedEncounter;
import org.motechproject.server.model.ExpectedObs;
import org.motechproject.server.model.Facility;
import org.motechproject.server.svc.BirthOutcomeChild;
import org.motechproject.server.svc.ExpectedCareBean;
import org.motechproject.server.svc.IdBean;
import org.motechproject.server.svc.LocationBean;
import org.motechproject.server.svc.MessageBean;
import org.motechproject.server.svc.OpenmrsBean;
import org.motechproject.server.svc.RegistrarBean;
import org.motechproject.ws.BirthOutcome;
import org.motechproject.ws.Care;
import org.motechproject.ws.ContactNumberType;
import org.motechproject.ws.DayOfWeek;
import org.motechproject.ws.Gender;
import org.motechproject.ws.HIVResult;
import org.motechproject.ws.HowLearned;
import org.motechproject.ws.InterestReason;
import org.motechproject.ws.MediaType;
import org.motechproject.ws.Patient;
import org.motechproject.ws.RegistrantType;
import org.motechproject.ws.RegistrationMode;
import org.motechproject.ws.server.RegistrarService;
import org.motechproject.ws.server.ValidationException;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RegistrarServiceTest {

	static ApplicationContext ctx;
	static RegistrarService regWs;
	static RegistrarBean registrarBean;
	static OpenmrsBean openmrsBean;
	static IdBean idBean;
	static MessageBean messageBean;
	static ExpectedCareBean expectedCareBean;
	static LocationBean locationBean;
	static WebServiceModelConverter modelConverter;

	@BeforeClass
	public static void setUpClass() throws Exception {
		registrarBean = createMock(RegistrarBean.class);
		openmrsBean = createMock(OpenmrsBean.class);
		idBean = createMock(IdBean.class);
		messageBean = createMock(MessageBean.class);
		expectedCareBean = createMock(ExpectedCareBean.class);
		locationBean = createMock(LocationBean.class);
		modelConverter = createMock(WebServiceModelConverter.class);
		ctx = new ClassPathXmlApplicationContext("test-context.xml");
		RegistrarWebService regService = (RegistrarWebService) ctx
				.getBean("registrarService");
		regService.setRegistrarBean(registrarBean);
		regService.setOpenmrsBean(openmrsBean);
		regService.setIdBean(idBean);
		regService.setMessageBean(messageBean);
		regService.setExpectedCareBean(expectedCareBean);
		regService.setLocationBean(locationBean);
		regService.setModelConverter(modelConverter);
		regWs = (RegistrarService) ctx.getBean("registrarClient");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		ctx = null;
		regWs = null;
		registrarBean = null;
		openmrsBean = null;
		idBean = null;
		messageBean = null;
		expectedCareBean = null;
		locationBean = null;
		modelConverter = null;
	}

	@Before
	public void setup() {
	}

	@After
	public void tearDown() throws Exception {
		reset(registrarBean, modelConverter, openmrsBean, idBean, messageBean,
				expectedCareBean, locationBean);
	}

	@Test
	public void testRecordPatientHistory() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer lastIPT = 1, lastTT = 1;
		Integer lastOPV = 1, lastPenta = 1, lastIPTI = 1;
		Date date = new Date();

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordPatientHistory(staff, facilityLocation, date,
				patient, lastIPT, date, lastTT, date, date, lastOPV, date,
				lastPenta, date, date, date, lastIPTI, date, date);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordPatientHistory(staffId, facilityId, date, motechId,
				lastIPT, date, lastTT, date, date, lastOPV, date, lastPenta,
				date, date, date, lastIPTI, date, date);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordPatientHistoryInvalidPatientId()
			throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer lastIPT = 1, lastTT = 1;
		Integer lastOPV = 1, lastPenta = 1, lastIPTI = 1;
		Date date = new Date();

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordPatientHistory(staffId, facilityId, date, motechId,
					lastIPT, date, lastTT, date, date, lastOPV, date,
					lastPenta, date, date, date, lastIPTI, date, date);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Patient History request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordMotherANCVisit() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer visitNumber = 1, location = 1, bpSystolic = 130, bpDiastolic = 80;
		Double weight = 63.3, hemoglobin = 11.1, fht = 130.1;
		String house = "House", community = "Community", comments = "Comments";
		Integer ttDose = 1, iptDose = 1, fhr = 130, urineProtein = 0, urineGlucose = 0;
		Boolean iptReactive = false, itnUse = true;
		Boolean vdrlReactive = false, vdrlTreatment = false, dewormer = false, maleInvolved = true;
		Boolean pmtct = false, preTest = false, postTest = false, pmtctTreatment = false, referred = false;
		Date date = new Date();
		HIVResult hivResult = HIVResult.NO_TEST;

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordMotherANCVisit(staff, facilityLocation, date,
				patient, visitNumber, location, house, community, date,
				bpSystolic, bpDiastolic, weight, ttDose, iptDose, iptReactive,
				itnUse, fht, fhr, urineProtein, urineGlucose, hemoglobin,
				vdrlReactive, vdrlTreatment, dewormer, maleInvolved, pmtct,
				preTest, hivResult, postTest, pmtctTreatment, referred, date,
				comments);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordMotherANCVisit(staffId, facilityId, date, motechId,
				visitNumber, location, house, community, date, bpSystolic,
				bpDiastolic, weight, ttDose, iptDose, iptReactive, itnUse, fht,
				fhr, urineProtein, urineGlucose, hemoglobin, vdrlReactive,
				vdrlTreatment, dewormer, maleInvolved, pmtct, preTest,
				hivResult, postTest, pmtctTreatment, referred, date, comments);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordMotherANCVisitInvalidPatientId()
			throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer visitNumber = 1, location = 1, bpSystolic = 130, bpDiastolic = 80;
		Double weight = 63.3, hemoglobin = 11.1, fht = 130.1;
		String house = "House", community = "Community", comments = "Comments";
		Integer ttDose = 1, iptDose = 1, fhr = 130, urineProtein = 0, urineGlucose = 0;
		Boolean iptReactive = false, itnUse = true;
		Boolean vdrlReactive = false, vdrlTreatment = false, dewormer = false, maleInvolved = true;
		Boolean pmtct = false, preTest = false, postTest = false, pmtctTreatment = false, referred = false;
		Date date = new Date();
		HIVResult hivResult = HIVResult.NO_TEST;

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordMotherANCVisit(staffId, facilityId, date, motechId,
					visitNumber, location, house, community, date, bpSystolic,
					bpDiastolic, weight, ttDose, iptDose, iptReactive, itnUse,
					fht, fhr, urineProtein, urineGlucose, hemoglobin,
					vdrlReactive, vdrlTreatment, dewormer, maleInvolved, pmtct,
					preTest, hivResult, postTest, pmtctTreatment, referred,
					date, comments);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Mother ANC Visit request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordPregnancyTermination() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer terminationType = 1, procedure = 2;
		Integer[] complications = new Integer[] { 1, 3, 5, 7 };
		Boolean maternalDeath = false, referred = false, postCounsel = true, postAccept = true;
		String comments = "Comments";
		Date date = new Date();

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordPregnancyTermination(eq(staff),
				eq(facilityLocation), eq(date), eq(patient),
				eq(terminationType), eq(procedure), aryEq(complications),
				eq(maternalDeath), eq(referred), eq(postCounsel),
				eq(postAccept), eq(comments));

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordPregnancyTermination(staffId, facilityId, date, motechId,
				terminationType, procedure, complications, maternalDeath,
				referred, postCounsel, postAccept, comments);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordPregnancyTerminationInvalidPatientId()
			throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer terminationType = 1, procedure = 2;
		Integer[] complications = new Integer[] { 1, 3, 5, 7 };
		Boolean maternalDeath = false, referred = false, postCounsel = true, postAccept = true;
		String comments = "Comments";
		Date date = new Date();

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordPregnancyTermination(staffId, facilityId, date,
					motechId, terminationType, procedure, complications,
					maternalDeath, referred, postCounsel, postAccept, comments);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Pregnancy Termination request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordPregnancyDelivery() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer child1Id = 246, child2Id = 468, child3Id = 579, deliveredBy = 1;
		Integer[] complications = new Integer[] { 1, 3, 5, 7 };
		String child1Name = "Child1First", child2Name = "Child2First", child3Name = "Child3First", comments = "Comments";
		Integer mode = 1, outcome = 2, location = 1, vvf = 2;
		Double child1Weight = 4.2, child2Weight = 4.4, child3Weight = 3.7;
		Boolean maternalDeath = false, maleInvolved = true;
		Date date = new Date();
		BirthOutcome child1birthOutcome = BirthOutcome.A;
		BirthOutcome child2birthOutcome = BirthOutcome.FSB;
		BirthOutcome child3birthOutcome = BirthOutcome.MSB;
		Gender child1Sex = Gender.FEMALE;
		Gender child2Sex = Gender.MALE;
		Gender child3Sex = Gender.MALE;
		RegistrationMode child1RegType = RegistrationMode.USE_PREPRINTED_ID;
		RegistrationMode child2RegType = RegistrationMode.USE_PREPRINTED_ID;
		RegistrationMode child3RegType = RegistrationMode.AUTO_GENERATE_ID;

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		List<org.openmrs.Patient> childPatients = new ArrayList<org.openmrs.Patient>();
		childPatients.add(new org.openmrs.Patient(3));

		Capture<List<BirthOutcomeChild>> outcomesCapture = new Capture<List<BirthOutcomeChild>>();

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		expect(idBean.isValidMotechIdCheckDigit(child1Id)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(child1Id.toString()))
				.andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(child2Id)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(child2Id.toString()))
				.andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(child3Id)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(child3Id.toString()))
				.andReturn(null);
		expect(
				registrarBean.recordPregnancyDelivery(eq(staff),
						eq(facilityLocation), eq(date), eq(patient), eq(mode),
						eq(outcome), eq(location), eq(deliveredBy),
						eq(maleInvolved), aryEq(complications), eq(vvf),
						eq(maternalDeath), eq(comments),
						capture(outcomesCapture))).andReturn(childPatients);
		expect(modelConverter.patientToWebService(childPatients, true))
				.andReturn(new Patient[1]);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordPregnancyDelivery(staffId, facilityId, date, motechId,
				mode, outcome, location, deliveredBy, maleInvolved,
				complications, vvf, maternalDeath, comments,
				child1birthOutcome, child1RegType, child1Id, child1Sex,
				child1Name, child1Weight, child2birthOutcome, child2RegType,
				child2Id, child2Sex, child2Name, child2Weight,
				child3birthOutcome, child3RegType, child3Id, child3Sex,
				child3Name, child3Weight);

		verify(registrarBean, openmrsBean, idBean, locationBean);

		List<BirthOutcomeChild> outcomes = outcomesCapture.getValue();
		assertEquals(3, outcomes.size());

		BirthOutcomeChild child1 = outcomes.get(0);
		assertEquals(child1birthOutcome, child1.getOutcome());
		assertEquals(child1Id, child1.getMotechId());
		assertEquals(child1Name, child1.getFirstName());
		assertEquals(child1Sex, child1.getSex());

		BirthOutcomeChild child2 = outcomes.get(1);
		assertEquals(child2birthOutcome, child2.getOutcome());
		assertEquals(child2Id, child2.getMotechId());
		assertEquals(child2Name, child2.getFirstName());
		assertEquals(child2Sex, child2.getSex());

		BirthOutcomeChild child3 = outcomes.get(2);
		assertEquals(child3birthOutcome, child3.getOutcome());
		assertEquals(child3Id, child3.getMotechId());
		assertEquals(child3Name, child3.getFirstName());
		assertEquals(child3Sex, child3.getSex());
	}

	@Test
	public void testRecordPregnancyDeliveryOneChild()
			throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer child1Id = 246, deliveredBy = 1;
		Integer[] complications = new Integer[] { 1, 3, 5, 7 };
		String child1Name = "Child1First", comments = "Comments";
		Integer mode = 1, outcome = 2, location = 1, vvf = 2;
		Double child1Weight = 4.2;
		Boolean maternalDeath = false, maleInvolved = true;
		Date date = new Date();
		BirthOutcome child1birthOutcome = BirthOutcome.A;
		Gender child1Sex = Gender.FEMALE;
		RegistrationMode child1RegType = RegistrationMode.USE_PREPRINTED_ID;

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		List<org.openmrs.Patient> childPatients = new ArrayList<org.openmrs.Patient>();
		childPatients.add(new org.openmrs.Patient(3));

		Capture<List<BirthOutcomeChild>> outcomesCapture = new Capture<List<BirthOutcomeChild>>();

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		expect(idBean.isValidMotechIdCheckDigit(child1Id)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(child1Id.toString()))
				.andReturn(null);
		expect(
				registrarBean.recordPregnancyDelivery(eq(staff),
						eq(facilityLocation), eq(date), eq(patient), eq(mode),
						eq(outcome), eq(location), eq(deliveredBy),
						eq(maleInvolved), aryEq(complications), eq(vvf),
						eq(maternalDeath), eq(comments),
						capture(outcomesCapture))).andReturn(childPatients);
		expect(modelConverter.patientToWebService(childPatients, true))
				.andReturn(new Patient[1]);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordPregnancyDelivery(staffId, facilityId, date, motechId,
				mode, outcome, location, deliveredBy, maleInvolved,
				complications, vvf, maternalDeath, comments,
				child1birthOutcome, child1RegType, child1Id, child1Sex,
				child1Name, child1Weight, null, null, null, null, null, null,
				null, null, null, null, null, null);

		verify(registrarBean, openmrsBean, idBean, locationBean);

		List<BirthOutcomeChild> outcomes = outcomesCapture.getValue();
		assertEquals(1, outcomes.size());

		BirthOutcomeChild child1 = outcomes.get(0);
		assertEquals(child1birthOutcome, child1.getOutcome());
		assertEquals(child1Id, child1.getMotechId());
		assertEquals(child1Name, child1.getFirstName());
		assertEquals(child1Sex, child1.getSex());
	}

	@Test
	public void testRecordPregnancyDeliveryInvalidIds()
			throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer child1Id = 246, child2Id = 246, child3Id = 246, deliveredBy = 1;
		Integer[] complications = new Integer[] { 1, 3, 5, 7 };
		String child1Name = "Child1First", child2Name = "Child2First", child3Name = "Child3First", comments = "Comments";
		Integer method = 1, outcome = 2, location = 1, vvf = 2;
		Double child1Weight = 4.2, child2Weight = 4.4, child3Weight = 3.7;
		Boolean maternalDeath = false, maleInvolved = true;
		Date date = new Date();
		BirthOutcome child1birthOutcome = BirthOutcome.A;
		BirthOutcome child2birthOutcome = BirthOutcome.FSB;
		BirthOutcome child3birthOutcome = BirthOutcome.MSB;
		Gender child1Sex = Gender.FEMALE;
		Gender child2Sex = Gender.MALE;
		Gender child3Sex = Gender.MALE;
		RegistrationMode child1RegType = RegistrationMode.USE_PREPRINTED_ID;
		RegistrationMode child2RegType = RegistrationMode.USE_PREPRINTED_ID;
		RegistrationMode child3RegType = RegistrationMode.AUTO_GENERATE_ID;

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(child1Id)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(child1Id.toString()))
				.andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(child2Id)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(child2Id.toString()))
				.andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(child3Id)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(child3Id.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordPregnancyDelivery(staffId, facilityId, date, motechId,
					method, outcome, location, deliveredBy, maleInvolved,
					complications, vvf, maternalDeath, comments,
					child1birthOutcome, child1RegType, child1Id, child1Sex,
					child1Name, child1Weight, child2birthOutcome,
					child2RegType, child2Id, child2Sex, child2Name,
					child2Weight, child3birthOutcome, child3RegType, child3Id,
					child3Sex, child3Name, child3Weight);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Pregnancy Delivery request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(3, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error); // Check exists
			error = errors.get(1);
			assertEquals("Child2MotechID=in use", error); // Check conflicts
			error = errors.get(2);
			assertEquals("Child3MotechID=in use", error); // Check conflicts
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordDeliveryNotification() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Date date = new Date();

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordPregnancyDeliveryNotification(staff,
				facilityLocation, date, patient);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordDeliveryNotification(staffId, facilityId, date, motechId);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordRecordDeliveryNotificationInvalidPatientId()
			throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Date date = new Date();

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordDeliveryNotification(staffId, facilityId, date,
					motechId);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Delivery Notification request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordMotherPNCVisit() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer visitNumber = 1, location = 1, ttDose = 1;
		Integer lochiaColour = 1;
		Double fht = 140.2, temperature = 25.3;
		String house = "House", community = "Community", comments = "Comments";
		Boolean referred = false, maleInvolved = true;
		Boolean vitaminA = true, lochiaExcess = false, lochiaFoul = false;
		Date date = new Date();

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordMotherPNCVisit(staff, facilityLocation, date,
				patient, visitNumber, location, house, community, referred,
				maleInvolved, vitaminA, ttDose, lochiaColour, lochiaExcess,
				lochiaFoul, temperature, fht, comments);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordMotherPNCVisit(staffId, facilityId, date, motechId,
				visitNumber, location, house, community, referred,
				maleInvolved, vitaminA, ttDose, lochiaColour, lochiaExcess,
				lochiaFoul, temperature, fht, comments);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordMotherPNCVisitInvalidPatientId()
			throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer visitNumber = 1, location = 1, ttDose = 1;
		Integer lochiaColour = 1;
		Double fht = 140.2, temperature = 25.3;
		String house = "House", community = "Community", comments = "Comments";
		Boolean referred = false, maleInvolved = true;
		Boolean vitaminA = true, lochiaExcess = false, lochiaFoul = false;
		Date date = new Date();

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordMotherPNCVisit(staffId, facilityId, date, motechId,
					visitNumber, location, house, community, referred,
					maleInvolved, vitaminA, ttDose, lochiaColour, lochiaExcess,
					lochiaFoul, temperature, fht, comments);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Mother PNC Visit request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordDeath() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Date date = new Date();

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordDeath(staff, facilityLocation, date, patient);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordDeath(staffId, facilityId, date, motechId);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordDeathInvalidPatientId() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Date date = new Date();

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordDeath(staffId, facilityId, date, motechId);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Death request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordTTVisit() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer ttDose = 1;
		Date date = new Date();

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordTTVisit(staff, facilityLocation, date, patient,
				ttDose);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordTTVisit(staffId, facilityId, date, motechId, ttDose);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordTTVisitInvalidPatientId() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer ttDose = 1;
		Date date = new Date();

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordTTVisit(staffId, facilityId, date, motechId, ttDose);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record TT Visit request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordChildPNCVisit() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer visitNumber = 1, location = 1, respiration = 60;
		String house = "House", community = "Community", comments = "Comments";
		Boolean referred = false, maleInvolved = true;
		Boolean bcg = true, opv0 = true, cordCondition = true, babyCondition = true;
		Double weight = 26.1, temperature = 25.6;
		Date date = new Date();

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordChildPNCVisit(staff, facilityLocation, date,
				patient, visitNumber, location, house, community, referred,
				maleInvolved, weight, temperature, bcg, opv0, respiration,
				cordCondition, babyCondition, comments);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordChildPNCVisit(staffId, facilityId, date, motechId,
				visitNumber, location, house, community, referred,
				maleInvolved, weight, temperature, bcg, opv0, respiration,
				cordCondition, babyCondition, comments);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordChildPNCVisitInvalidPatientId()
			throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		Integer visitNumber = 1, location = 1, respiration = 60;
		String house = "House", community = "Community", comments = "Comments";
		Boolean referred = false, maleInvolved = true;
		Boolean bcg = true, opv0 = true, cordCondition = true, babyCondition = true;
		Double weight = 26.1, temperature = 25.6;
		Date date = new Date();

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordChildPNCVisit(staffId, facilityId, date, motechId,
					visitNumber, location, house, community, referred,
					maleInvolved, weight, temperature, bcg, opv0, respiration,
					cordCondition, babyCondition, comments);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Child PNC Visit request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordChildCWCVisit() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String house = "House", community = "Community", comments = "Comments";
		Integer location = 1, opvDose = 1, pentaDose = 1, iptiDose = 1;
		Boolean bcg = true, yellowFever = true, csm = true, measles = true, vitaminA = true;
		Boolean dewormer = false, maleInvolved = true;
		Double weight = 25.2, muac = 5.1, height = 37.2;
		Date date = new Date();

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordChildCWCVisit(staff, facilityLocation, date,
				patient, location, house, community, bcg, opvDose, pentaDose,
				measles, yellowFever, csm, iptiDose, vitaminA, dewormer,
				weight, muac, height, maleInvolved, comments);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordChildCWCVisit(staffId, facilityId, date, motechId,
				location, house, community, bcg, opvDose, pentaDose, measles,
				yellowFever, csm, iptiDose, vitaminA, dewormer, weight, muac,
				height, maleInvolved, comments);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordChildCWCVisitInvalidPatientId()
			throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String house = "House", community = "Community", comments = "Comments";
		Integer location = 1, opvDose = 1, pentaDose = 1, iptiDose = 1;
		Boolean bcg = true, yellowFever = true, csm = true, measles = true, vitaminA = true;
		Boolean dewormer = false, maleInvolved = true;
		Double weight = 25.2, muac = 5.1, height = 37.2;
		Date date = new Date();

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordChildCWCVisit(staffId, facilityId, date, motechId,
					location, house, community, bcg, opvDose, pentaDose,
					measles, yellowFever, csm, iptiDose, vitaminA, dewormer,
					weight, muac, height, maleInvolved, comments);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Child CWC Visit request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRegisterPatient() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3, motherMotechId = 4;
		String firstName = "First", middleName = "Middle", lastName = "Last", prefName = "Pref";
		String nhis = "NHIS", address = "Address", language = "Language";
		Gender gender = Gender.FEMALE;
		Boolean estBirthDate = false, insured = true, delivDateConf = true, enroll = true, consent = true;
		Integer messageWeek = 5;
		String phone = "15555555";
		Integer communityId = 11111;
		Date date = new Date();
		RegistrationMode mode = RegistrationMode.USE_PREPRINTED_ID;
		RegistrantType type = RegistrantType.CHILD_UNDER_FIVE;
		ContactNumberType phoneType = ContactNumberType.PERSONAL;
		MediaType format = MediaType.VOICE;
		DayOfWeek day = DayOfWeek.MONDAY;
		InterestReason reason = InterestReason.RECENTLY_DELIVERED;
		HowLearned how = HowLearned.GHS_NURSE;

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = null;
		org.openmrs.Patient mother = new org.openmrs.Patient(3);
		Community comm = new Community();

		org.openmrs.Patient createdPatient = new org.openmrs.Patient(4);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(locationBean.getCommunityById(communityId)).andReturn(comm);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		expect(idBean.isValidMotechIdCheckDigit(motherMotechId))
				.andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motherMotechId.toString()))
				.andReturn(mother);
		expect(
				registrarBean.registerPatient(staff, facilityLocation, date,
						mode, motechId, type, firstName, middleName, lastName,
						prefName, date, estBirthDate, gender, insured, nhis,
						date, mother, comm, address, phone, date,
						delivDateConf, enroll, consent, phoneType, format,
						language, day, date, reason, how, messageWeek))
				.andReturn(createdPatient);
		expect(modelConverter.patientToWebService(createdPatient, true))
				.andReturn(new Patient());

		replay(registrarBean, openmrsBean, modelConverter, idBean, locationBean);

		Patient wsPatient = regWs.registerPatient(staffId, facilityId, date,
				mode, motechId, type, firstName, middleName, lastName,
				prefName, date, estBirthDate, gender, insured, nhis, date,
				motherMotechId, communityId, address, phone, date,
				delivDateConf, enroll, consent, phoneType, format, language,
				day, date, reason, how, messageWeek);

		verify(registrarBean, openmrsBean, modelConverter, idBean, locationBean);

		assertNotNull("Patient is null", wsPatient);
	}

	@Test
	public void testRegisterPatientAllErrors() {
		Integer staffId = 1, facilityId = 2, motechId = 3, motherMotechId = 4;
		String firstName = "First", middleName = "Middle", lastName = "Last", prefName = "Pref";
		String nhis = "NHIS", address = "Address", language = "Language";
		Gender gender = Gender.FEMALE;
		Boolean estBirthDate = false, insured = true, delivDateConf = true, enroll = true, consent = true;
		Integer messageWeek = 5;
		String phone = "15555555";
		Integer community = 11111;
		Date date = new Date();
		RegistrationMode mode = RegistrationMode.USE_PREPRINTED_ID;
		RegistrantType type = RegistrantType.CHILD_UNDER_FIVE;
		ContactNumberType phoneType = ContactNumberType.PERSONAL;
		MediaType format = MediaType.VOICE;
		DayOfWeek day = DayOfWeek.MONDAY;
		InterestReason reason = InterestReason.RECENTLY_DELIVERED;
		HowLearned how = HowLearned.GHS_NURSE;

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -6);
		Date childBirthDate = calendar.getTime();

		User staff = null;
		org.openmrs.Patient patient = new org.openmrs.Patient(2);
		org.openmrs.Patient mother = null;

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(null);
		expect(locationBean.getCommunityById(community)).andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		expect(idBean.isValidMotechIdCheckDigit(motherMotechId))
				.andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motherMotechId.toString()))
				.andReturn(mother);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.registerPatient(staffId, facilityId, date, mode, motechId,
					type, firstName, middleName, lastName, prefName,
					childBirthDate, estBirthDate, gender, insured, nhis, date,
					motherMotechId, community, address, phone, date,
					delivDateConf, enroll, consent, phoneType, format,
					language, day, date, reason, how, messageWeek);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Register Patient request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(6, errors.size());
			String staffError = errors.get(0);
			assertEquals("StaffID=not found", staffError);
			String facilityError = errors.get(1);
			assertEquals("FacilityID=not found", facilityError);
			String communityError = errors.get(2);
			assertEquals("Community=not found", communityError);
			String patientError = errors.get(3);
			assertEquals("MotechID=in use", patientError);
			String motherError = errors.get(4);
			assertEquals("MotherMotechID=not found", motherError);
			String dobError = errors.get(5);
			assertEquals("DOB=invalid", dobError);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRegisterPregnancy() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String language = "Language";
		Boolean enroll = true, consent = true;
		String phone = "15555555";
		Date date = new Date();
		ContactNumberType phoneType = ContactNumberType.PERSONAL;
		MediaType format = MediaType.VOICE;
		DayOfWeek day = DayOfWeek.MONDAY;
		HowLearned how = HowLearned.GHS_NURSE;

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.registerPregnancy(staff, facilityLocation, date, patient,
				date, enroll, consent, phoneType, phone, format, language, day,
				date, how);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.registerPregnancy(staffId, facilityId, date, motechId, date,
				enroll, consent, phoneType, phone, format, language, day, date,
				how);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRegisterPregnancyInvalidIds() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String language = "Language";
		Boolean enroll = true, consent = true;
		String phone = "15555555";
		Date date = new Date();
		ContactNumberType phoneType = ContactNumberType.PERSONAL;
		MediaType format = MediaType.VOICE;
		DayOfWeek day = DayOfWeek.MONDAY;
		HowLearned how = HowLearned.GHS_NURSE;

		User staff = null;
		org.openmrs.Patient patient = null;

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.registerPregnancy(staffId, facilityId, date, motechId, date,
					enroll, consent, phoneType, phone, format, language, day,
					date, how);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Register Pregnancy request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(3, errors.size());
			String staffError = errors.get(0);
			assertEquals("StaffID=not found", staffError);
			String facilityError = errors.get(1);
			assertEquals("FacilityID=not found", facilityError);
			String patientError = errors.get(2);
			assertEquals("MotechID=not found", patientError);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRegisterANCMother() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String language = "Language", regNumber = "RegNumber";
		Boolean enroll = true, consent = true;
		Integer gravida = 0, parity = 0;
		Double height = 45.3;
		String phone = "15555555";
		Date date = new Date();
		ContactNumberType phoneType = ContactNumberType.PERSONAL;
		MediaType format = MediaType.VOICE;
		DayOfWeek day = DayOfWeek.MONDAY;
		HowLearned how = HowLearned.GHS_NURSE;

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.registerANCMother(staff, facilityLocation, date, patient,
				regNumber, date, height, gravida, parity, enroll, consent,
				phoneType, phone, format, language, day, date, how);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.registerANCMother(staffId, facilityId, date, motechId, regNumber,
				date, height, gravida, parity, enroll, consent, phoneType,
				phone, format, language, day, date, how);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRegisterANCMotherInvalidIds() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String language = "Language", regNumber = "RegNumber";
		Boolean enroll = true, consent = true;
		Integer gravida = 0, parity = 0;
		Double height = 45.3;
		String phone = "15555555";
		Date date = new Date();
		ContactNumberType phoneType = ContactNumberType.PERSONAL;
		MediaType format = MediaType.VOICE;
		DayOfWeek day = DayOfWeek.MONDAY;
		HowLearned how = HowLearned.GHS_NURSE;

		User staff = null;
		org.openmrs.Patient patient = null;

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {

			regWs.registerANCMother(staffId, facilityId, date, motechId,
					regNumber, date, height, gravida, parity, enroll, consent,
					phoneType, phone, format, language, day, date, how);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Register ANC Mother request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(3, errors.size());
			String staffError = errors.get(0);
			assertEquals("StaffID=not found", staffError);
			String facilityError = errors.get(1);
			assertEquals("FacilityID=not found", facilityError);
			String patientError = errors.get(2);
			assertEquals("MotechID=not found", patientError);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRegisterCWCChild() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String language = "Language", regNumber = "RegNumber";
		Boolean enroll = true, consent = true;
		String phone = "15555555";
		Date date = new Date();
		ContactNumberType phoneType = ContactNumberType.PERSONAL;
		MediaType format = MediaType.VOICE;
		DayOfWeek day = DayOfWeek.MONDAY;
		HowLearned how = HowLearned.GHS_NURSE;

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.registerCWCChild(staff, facilityLocation, date, patient,
				regNumber, enroll, consent, phoneType, phone, format, language,
				day, date, how);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.registerCWCChild(staffId, facilityId, date, motechId, regNumber,
				enroll, consent, phoneType, phone, format, language, day, date,
				how);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRegisterCWCChildInvalidIds() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String language = "Language", regNumber = "RegNumber";
		Boolean enroll = true, consent = true;
		String phone = "15555555";
		Date date = new Date();
		ContactNumberType phoneType = ContactNumberType.PERSONAL;
		MediaType format = MediaType.VOICE;
		DayOfWeek day = DayOfWeek.MONDAY;
		HowLearned how = HowLearned.GHS_NURSE;

		User staff = null;
		org.openmrs.Patient patient = null;

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.registerCWCChild(staffId, facilityId, date, motechId,
					regNumber, enroll, consent, phoneType, phone, format,
					language, day, date, how);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Register CWC Child request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(3, errors.size());
			String staffError = errors.get(0);
			assertEquals("StaffID=not found", staffError);
			String facilityError = errors.get(1);
			assertEquals("FacilityID=not found", facilityError);
			String patientError = errors.get(2);
			assertEquals("MotechID=not found", patientError);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testEditPatient() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String phoneNumber = "2075557894";
		String nhis = "125";
		ContactNumberType phoneType = ContactNumberType.PERSONAL;
		Boolean stopEnrollment = false;
		Date date = new Date();

		User staff = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);

		registrarBean.editPatient(staff, date, patient, phoneNumber, phoneType,
				nhis, date, stopEnrollment);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.editPatient(staffId, facilityId, date, motechId, phoneNumber,
				phoneType, nhis, date, stopEnrollment);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testEditPatientAllErrors() {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String phoneNumber = "2075557894";
		String nhis = "125";
		ContactNumberType phoneType = ContactNumberType.PERSONAL;
		Boolean stopEnrollment = false;
		Date date = new Date();

		User staff = null;
		org.openmrs.Patient patient = null;

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.editPatient(staffId, facilityId, date, motechId, phoneNumber,
					phoneType, nhis, date, stopEnrollment);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Edit Patient request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(3, errors.size());
			String staffError = errors.get(0);
			assertEquals("StaffID=not found", staffError);
			String facilityError = errors.get(1);
			assertEquals("FacilityID=not found", facilityError);
			String patientError = errors.get(2);
			assertEquals("MotechID=not found", patientError);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testGeneralVisit() throws ValidationException {
		Integer staffId = 1, facilityId = 2;
		String serial = "Serial", comments = "Comments";
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean insured = true, newCase = true, referred = false, rdtGiven = true, rdtPositive = false, actTreated = false;
		Date date = new Date();
		Gender gender = Gender.MALE;

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		registrarBean.recordGeneralOutpatientVisit(staffId, facilityId, date,
				serial, gender, date, insured, diagnosis, secondDiagnosis,
				rdtGiven, rdtPositive, actTreated, newCase, referred, comments);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordGeneralVisit(staffId, facilityId, date, serial, gender,
				date, insured, diagnosis, secondDiagnosis, rdtGiven,
				rdtPositive, actTreated, newCase, referred, comments);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testGeneralVisitInvalidIds() {
		Integer staffId = 1, facilityId = 2;
		String serial = "Serial", comments = "Comments";
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean insured = true, newCase = true, referred = false, rdtGiven = true, rdtPositive = false, actTreated = false;
		Date date = new Date();
		Gender gender = Gender.MALE;

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				null);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordGeneralVisit(staffId, facilityId, date, serial, gender,
					date, insured, diagnosis, secondDiagnosis, rdtGiven,
					rdtPositive, actTreated, newCase, referred, comments);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in General Visit request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(2, errors.size());
			String staffError = errors.get(0);
			assertEquals("StaffID=not found", staffError);
			String facilityError = errors.get(1);
			assertEquals("FacilityID=not found", facilityError);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordChildVisit() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String serial = "Serial", comments = "Comments";
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean insured = true, newCase = true, referred = false, rdtGiven = true, rdtPositive = false, actTreated = false;
		Date date = new Date();

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordOutpatientVisit(staff, facilityLocation, date,
				patient, serial, insured, diagnosis, secondDiagnosis, rdtGiven,
				rdtPositive, actTreated, newCase, referred, comments);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordChildVisit(staffId, facilityId, date, serial, motechId,
				insured, diagnosis, secondDiagnosis, rdtGiven, rdtPositive,
				actTreated, newCase, referred, comments);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordChildVisitInvalidIds() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String serial = "Serial", comments = "Comments";
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean insured = true, newCase = true, referred = false, rdtGiven = true, rdtPositive = false, actTreated = false;
		Date date = new Date();

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordChildVisit(staffId, facilityId, date, serial, motechId,
					insured, diagnosis, secondDiagnosis, rdtGiven, rdtPositive,
					actTreated, newCase, referred, comments);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Child Visit request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(2, errors.size());
			String facilityError = errors.get(0);
			assertEquals("FacilityID=not found", facilityError);
			String motechidError = errors.get(1);
			assertEquals("MotechID=not found", motechidError);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordMotherVisit() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String serial = "Serial", comments = "Comments";
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean insured = true, newCase = true, referred = false, rdtGiven = true, rdtPositive = false, actTreated = false;
		Date date = new Date();

		User staff = new User(1);
		Location facilityLocation = new Location(1);
		Facility facility = new Facility();
		facility.setLocation(facilityLocation);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		registrarBean.recordOutpatientVisit(staff, facilityLocation, date,
				patient, serial, insured, diagnosis, secondDiagnosis, rdtGiven,
				rdtPositive, actTreated, newCase, referred, comments);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		regWs.recordMotherVisit(staffId, facilityId, date, serial, motechId,
				insured, diagnosis, secondDiagnosis, rdtGiven, rdtPositive,
				actTreated, newCase, referred, comments);

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testRecordMotherVisitInvalidIds() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;
		String serial = "Serial", comments = "Comments";
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean insured = true, newCase = true, referred = false, rdtGiven = true, rdtPositive = false, actTreated = false;
		Date date = new Date();

		User staff = new User(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				staff);
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, openmrsBean, idBean, locationBean);

		try {
			regWs.recordMotherVisit(staffId, facilityId, date, serial,
					motechId, insured, diagnosis, secondDiagnosis, rdtGiven,
					rdtPositive, actTreated, newCase, referred, comments);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Mother Visit request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(2, errors.size());
			String facilityError = errors.get(0);
			assertEquals("FacilityID=not found", facilityError);
			String motechidError = errors.get(1);
			assertEquals("MotechID=not found", motechidError);
		}

		verify(registrarBean, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testQueryANCDefaulters() throws ValidationException {
		Integer staffId = 1, facilityId = 2;

		Capture<String[]> encounterGroups = new Capture<String[]>();
		Capture<String[]> obsGroups = new Capture<String[]>();

		List<ExpectedEncounter> expectedEncounters = new ArrayList<ExpectedEncounter>();
		List<ExpectedObs> expectedObs = new ArrayList<ExpectedObs>();

		Facility facility = null;

		Care encounterCare = new Care();
		encounterCare.setName("EncounterCare");
		Care obsCare = new Care();
		obsCare.setName("ObsCare");
		Care[] defaultedCares = { encounterCare, obsCare };

		expect(
				expectedCareBean.getDefaultedExpectedEncounters(eq(facility),
						capture(encounterGroups)))
				.andReturn(expectedEncounters);
		expect(
				expectedCareBean.getDefaultedExpectedObs(eq(facility),
						capture(obsGroups))).andReturn(expectedObs);
		expect(
				modelConverter.defaultedToWebServiceCares(expectedEncounters,
						expectedObs)).andReturn(defaultedCares);

		replay(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		Care[] cares = regWs.queryANCDefaulters(staffId, facilityId);

		verify(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		assertEquals(1, encounterGroups.getValue().length);
		assertEquals("ANC", encounterGroups.getValue()[0]);

		assertEquals(2, obsGroups.getValue().length);
		assertEquals("TT", obsGroups.getValue()[0]);
		assertEquals("IPT", obsGroups.getValue()[1]);

		assertNotNull("Care result array is null", cares);
		assertEquals(2, cares.length);
		assertEquals(encounterCare.getName(), cares[0].getName());
		assertEquals(obsCare.getName(), cares[1].getName());
	}

	@Test
	public void testQueryTTDefaulters() throws ValidationException {
		Integer staffId = 1, facilityId = 2;

		Capture<String[]> obsGroups = new Capture<String[]>();

		List<ExpectedObs> expectedObs = new ArrayList<ExpectedObs>();

		Facility facility = new Facility();

		Care obsCare = new Care();
		obsCare.setName("ObsCare");
		Care[] obsCares = { obsCare };

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(
				expectedCareBean.getDefaultedExpectedObs(eq(facility),
						capture(obsGroups))).andReturn(expectedObs);
		expect(modelConverter.defaultedObsToWebServiceCares(expectedObs))
				.andReturn(obsCares);

		replay(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		Care[] cares = regWs.queryTTDefaulters(staffId, facilityId);

		verify(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		assertEquals(1, obsGroups.getValue().length);
		assertEquals("TT", obsGroups.getValue()[0]);

		assertNotNull("Care result array is null", cares);
		assertEquals(1, cares.length);
		assertEquals(obsCare.getName(), cares[0].getName());
	}

	@Test
	public void testQueryMotherPNCDefaulters() throws ValidationException {
		Integer staffId = 1, facilityId = 2;

		Capture<String[]> encounterGroups = new Capture<String[]>();

		List<ExpectedEncounter> expectedEncounters = new ArrayList<ExpectedEncounter>();

		Facility facility = new Facility();

		Care encounterCare = new Care();
		encounterCare.setName("EncounterCare");
		Care[] encounterCares = { encounterCare };

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(
				expectedCareBean.getDefaultedExpectedEncounters(eq(facility),
						capture(encounterGroups)))
				.andReturn(expectedEncounters);
		expect(
				modelConverter
						.defaultedEncountersToWebServiceCares(expectedEncounters))
				.andReturn(encounterCares);

		replay(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		Care[] cares = regWs.queryMotherPNCDefaulters(staffId, facilityId);

		verify(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		assertEquals(1, encounterGroups.getValue().length);
		assertEquals("PNC(mother)", encounterGroups.getValue()[0]);

		assertNotNull("Care result array is null", cares);
		assertEquals(1, cares.length);
		assertEquals(encounterCare.getName(), cares[0].getName());
	}

	@Test
	public void testQueryChildPNCDefaulters() throws ValidationException {
		Integer staffId = 1, facilityId = 2;

		Capture<String[]> encounterGroups = new Capture<String[]>();

		List<ExpectedEncounter> expectedEncounters = new ArrayList<ExpectedEncounter>();

		Facility facility = new Facility();

		Care encounterCare = new Care();
		encounterCare.setName("EncounterCare");
		Care[] encounterCares = { encounterCare };

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(
				expectedCareBean.getDefaultedExpectedEncounters(eq(facility),
						capture(encounterGroups)))
				.andReturn(expectedEncounters);
		expect(
				modelConverter
						.defaultedEncountersToWebServiceCares(expectedEncounters))
				.andReturn(encounterCares);

		replay(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		Care[] cares = regWs.queryChildPNCDefaulters(staffId, facilityId);

		verify(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		assertEquals(1, encounterGroups.getValue().length);
		assertEquals("PNC(baby)", encounterGroups.getValue()[0]);

		assertNotNull("Care result array is null", cares);
		assertEquals(1, cares.length);
		assertEquals(encounterCare.getName(), cares[0].getName());
	}

	@Test
	public void testQueryCWCDefaulters() throws ValidationException {
		Integer staffId = 1, facilityId = 2;

		Capture<String[]> obsGroups = new Capture<String[]>();

		List<ExpectedObs> expectedObs = new ArrayList<ExpectedObs>();

		Facility facility = new Facility();

		Care obsCare = new Care();
		obsCare.setName("ObsCare");
		Care[] obsCares = { obsCare };

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(
				expectedCareBean.getDefaultedExpectedObs(eq(facility),
						capture(obsGroups))).andReturn(expectedObs);
		expect(modelConverter.defaultedObsToWebServiceCares(expectedObs))
				.andReturn(obsCares);

		replay(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		Care[] cares = regWs.queryCWCDefaulters(staffId, facilityId);

		verify(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		assertEquals(7, obsGroups.getValue().length);
		assertEquals("OPV", obsGroups.getValue()[0]);
		assertEquals("BCG", obsGroups.getValue()[1]);
		assertEquals("Penta", obsGroups.getValue()[2]);
		assertEquals("YellowFever", obsGroups.getValue()[3]);
		assertEquals("Measles", obsGroups.getValue()[4]);
		assertEquals("VitaA", obsGroups.getValue()[5]);
		assertEquals("IPTI", obsGroups.getValue()[6]);

		assertNotNull("Care result array is null", cares);
		assertEquals(1, cares.length);
		assertEquals(obsCare.getName(), cares[0].getName());
	}

	@Test
	public void testQueryUpcomingDeliveries() throws ValidationException {
		Integer staffId = 1, facilityId = 2;

		List<Obs> pregnancies = new ArrayList<Obs>();
		pregnancies.add(new Obs());

		Patient patient = new Patient();
		patient.setMotechId("MotechId");
		Patient[] result = { patient };
		Facility facility = new Facility();

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(openmrsBean.getUpcomingPregnanciesDueDate(facility)).andReturn(
				pregnancies);
		expect(modelConverter.dueDatesToWebServicePatients(pregnancies))
				.andReturn(result);

		replay(registrarBean, modelConverter, openmrsBean, idBean, locationBean);

		Patient[] patients = regWs.queryUpcomingDeliveries(staffId, facilityId);

		verify(registrarBean, modelConverter, openmrsBean, idBean, locationBean);

		assertNotNull("Patient result array is null", patients);
		assertEquals(1, patients.length);
	}

	@Test
	public void testQueryRecentDeliveries() throws ValidationException {
		Integer staffId = 1, facilityId = 2;

		List<Encounter> deliveries = new ArrayList<Encounter>();
		deliveries.add(new Encounter());

		Patient patient = new Patient();
		patient.setMotechId("MotechId");
		Patient[] result = { patient };
		Facility facility = new Facility();

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(openmrsBean.getRecentDeliveries(facility)).andReturn(deliveries);
		expect(modelConverter.deliveriesToWebServicePatients(deliveries))
				.andReturn(result);

		replay(registrarBean, modelConverter, openmrsBean, idBean, locationBean);

		Patient[] patients = regWs.queryRecentDeliveries(staffId, facilityId);

		verify(registrarBean, modelConverter, openmrsBean, idBean, locationBean);

		assertNotNull("Patient result array is null", patients);
		assertEquals(1, patients.length);
	}

	@Test
	public void testQueryOverdueDeliveries() throws ValidationException {
		Integer staffId = 1, facilityId = 2;

		List<Obs> pregnancies = new ArrayList<Obs>();
		pregnancies.add(new Obs());

		Patient patient = new Patient();
		patient.setMotechId("MotechId");
		Patient[] result = { patient };
		Facility facility = new Facility();

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(facility);
		expect(openmrsBean.getOverduePregnanciesDueDate(facility)).andReturn(
				pregnancies);
		expect(modelConverter.dueDatesToWebServicePatients(pregnancies))
				.andReturn(result);

		replay(registrarBean, modelConverter, openmrsBean, idBean, locationBean);

		Patient[] patients = regWs.queryOverdueDeliveries(staffId, facilityId);

		verify(registrarBean, modelConverter, openmrsBean, idBean, locationBean);

		assertNotNull("Patient result array is null", patients);
		assertEquals(1, patients.length);
	}

	@Test
	public void testQueryUpcomingCare() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;

		List<ExpectedEncounter> expectedEncounters = new ArrayList<ExpectedEncounter>();
		List<ExpectedObs> expectedObs = new ArrayList<ExpectedObs>();

		Calendar calendar = Calendar.getInstance();

		Care encounterCare1 = new Care();
		encounterCare1.setName("EncounterCare1");
		calendar.set(2010, Calendar.APRIL, 4);
		encounterCare1.setDate(calendar.getTime());
		Care encounterCare2 = new Care();
		encounterCare2.setName("EncounterCare2");
		calendar.set(2010, Calendar.DECEMBER, 12);
		encounterCare2.setDate(calendar.getTime());
		Care obsCare1 = new Care();
		obsCare1.setName("ObsCare1");
		calendar.set(2010, Calendar.OCTOBER, 10);
		obsCare1.setDate(calendar.getTime());
		Care obsCare2 = new Care();
		obsCare2.setName("ObsCare2");
		calendar.set(2010, Calendar.JANUARY, 1);
		obsCare2.setDate(calendar.getTime());
		Care[] upcomingCares = { obsCare2, encounterCare1, obsCare1,
				encounterCare2 };

		org.openmrs.Patient patient = new org.openmrs.Patient(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		expect(modelConverter.patientToWebService(eq(patient), eq(true)))
				.andReturn(new Patient());

		expect(expectedCareBean.getUpcomingExpectedEncounters(patient))
				.andReturn(expectedEncounters);
		expect(expectedCareBean.getUpcomingExpectedObs(patient)).andReturn(
				expectedObs);
		expect(
				modelConverter.upcomingToWebServiceCares(expectedEncounters,
						expectedObs, false)).andReturn(upcomingCares);

		replay(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		Patient wsPatient = regWs.queryUpcomingCare(staffId, facilityId,
				motechId);

		verify(registrarBean, modelConverter, openmrsBean, expectedCareBean,
				idBean, locationBean);

		assertNotNull("Patient result is null", wsPatient);
		Care[] cares = wsPatient.getCares();
		assertNotNull("Patient cares is null", cares);
		assertEquals(4, cares.length);
		assertEquals(obsCare2.getName(), cares[0].getName());
		assertEquals(obsCare2.getDate(), cares[0].getDate());
		assertEquals(encounterCare1.getName(), cares[1].getName());
		assertEquals(encounterCare1.getDate(), cares[1].getDate());
		assertEquals(obsCare1.getName(), cares[2].getName());
		assertEquals(obsCare1.getDate(), cares[2].getDate());
		assertEquals(encounterCare2.getName(), cares[3].getName());
		assertEquals(encounterCare2.getDate(), cares[3].getDate());
	}

	@Test
	public void testQueryMotechId() throws ValidationException {
		Integer staffId = 1, facilityId = 2;
		String firstName = "FirstName", lastName = "LastName", prefName = "PrefName";
		String nhis = "NHIS";
		String phone = "22424324";
		Date birthDate = new Date();

		List<org.openmrs.Patient> patients = new ArrayList<org.openmrs.Patient>();
		patients.add(new org.openmrs.Patient(1));

		Patient patient = new Patient();
		patient.setMotechId("MotechId");
		Patient[] result = { patient };

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(
				openmrsBean.getPatients(firstName, lastName, prefName,
						birthDate, null, phone, nhis, null))
				.andReturn(patients);
		expect(modelConverter.patientToWebService(patients, true)).andReturn(
				result);

		replay(registrarBean, modelConverter, openmrsBean, idBean, locationBean);

		Patient[] wsPatients = regWs.queryMotechId(staffId, facilityId,
				firstName, lastName, prefName, birthDate, nhis, phone);

		verify(registrarBean, modelConverter, openmrsBean, idBean, locationBean);

		assertNotNull("Patient result array is null", wsPatients);
		assertEquals(1, wsPatients.length);
	}

	@Test
	public void testQueryPatient() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;

		org.openmrs.Patient patient = new org.openmrs.Patient(1);

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(
				new Facility());
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		expect(modelConverter.patientToWebService(eq(patient), eq(false)))
				.andReturn(new Patient());

		replay(registrarBean, modelConverter, openmrsBean, idBean, locationBean);

		regWs.queryPatient(staffId, facilityId, motechId);

		verify(registrarBean, modelConverter, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testQueryPatientInvalidIds() throws ValidationException {
		Integer staffId = 1, facilityId = 2, motechId = 3;

		expect(idBean.isValidIdCheckDigit(staffId)).andReturn(true);
		expect(openmrsBean.getStaffBySystemId(staffId.toString())).andReturn(
				new User(1));
		expect(idBean.isValidIdCheckDigit(facilityId)).andReturn(true);
		expect(locationBean.getFacilityById(facilityId)).andReturn(null);
		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, modelConverter, openmrsBean, idBean, locationBean);

		try {
			regWs.queryPatient(staffId, facilityId, motechId);

			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Patient Query request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(2, errors.size());
			String facilityError = errors.get(0);
			assertEquals("FacilityID=not found", facilityError);
			String motechidError = errors.get(1);
			assertEquals("MotechID=not found", motechidError);
		}

		verify(registrarBean, modelConverter, openmrsBean, idBean, locationBean);
	}

	@Test
	public void testGetPatientEnrollments() throws ValidationException {
		Integer motechId = 3;

		org.openmrs.Patient patient = new org.openmrs.Patient(1);
		String[] enrollments = { "Enrollment1", "Enrollment2" };

		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(patient);
		expect(messageBean.getActiveMessageProgramEnrollmentNames(patient))
				.andReturn(enrollments);

		replay(registrarBean, modelConverter, openmrsBean, idBean, messageBean);

		String[] result = regWs.getPatientEnrollments(motechId);

		verify(registrarBean, modelConverter, openmrsBean, idBean, messageBean);

		assertArrayEquals(enrollments, result);
	}

	@Test
	public void testGetPatientEnrollmentsInvalidPatientId()
			throws ValidationException {
		Integer motechId = 3;

		expect(idBean.isValidMotechIdCheckDigit(motechId)).andReturn(true);
		expect(openmrsBean.getPatientByMotechId(motechId.toString()))
				.andReturn(null);

		replay(registrarBean, modelConverter, openmrsBean, idBean, messageBean);

		try {
			regWs.getPatientEnrollments(motechId);

			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Get Patient Enrollments request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<String> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			String error = errors.get(0);
			assertEquals("MotechID=not found", error);
		}

		verify(registrarBean, modelConverter, openmrsBean, idBean, messageBean);
	}

	@Test
	public void testSetMessageStatus() {
		String messageId = "12345678-1234-1234-1234-123456789012";
		Boolean success = true;

		messageBean.setMessageStatus(messageId, success);

		replay(messageBean);

		regWs.setMessageStatus(messageId, success);

		verify(messageBean);
	}

	@Test
	public void testRegistrarBeanProperty() throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		RegistrarWebService regWs = new RegistrarWebService();

		Field regBeanField = regWs.getClass().getDeclaredField("registrarBean");
		regBeanField.setAccessible(true);

		regWs.setRegistrarBean(registrarBean);
		assertEquals(registrarBean, regBeanField.get(regWs));

		regWs.setRegistrarBean(null);
		assertEquals(null, regBeanField.get(regWs));
	}
}
