package org.motech.openmrs.module.sdsched;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.TestCase;

import org.easymock.Capture;
import org.openmrs.PatientIdentifier;
import org.springframework.transaction.support.TransactionSynchronization;

/**
 * Test for {@link ScheduleMaintServiceImpl}.
 * 
 * @author batkinson
 * 
 */
public class SchedMaintServiceImplTest extends TestCase {

	private TxSyncManWrapper mockSyncMan;
	private ScheduleAdjuster mockAdjuster;
	private ScheduleMaintServiceImpl obj;

	@Override
	protected void setUp() throws Exception {
		mockSyncMan = createMock(TxSyncManWrapper.class);
		mockAdjuster = createMock(ScheduleAdjuster.class);
		obj = new ScheduleMaintServiceImpl();
		obj.setSyncManWrapper(mockSyncMan);
		obj.setScheduleAdjuster(mockAdjuster);
	}

	@Override
	protected void tearDown() throws Exception {
		mockSyncMan = null;
		obj = null;
	}

	public void testSetSyncManWrapper() {
		TxSyncManWrapper txSyncMan = new TxSyncManWrapperImpl();
		obj.setSyncManWrapper(txSyncMan);
		assertEquals(txSyncMan, obj.syncManWrapper);
	}

	public void testSetScheduleAdjuster() {
		ScheduleAdjuster adjuster = new DummyScheduleAdjuster();
		obj.setScheduleAdjuster(adjuster);
		assertEquals(adjuster, obj.scheduleAdjuster);
	}

	public void testGetAffectedPatientsUnboundNoCreate() {

		AffectedPatients result = null;

		expect(mockSyncMan.getResource(ScheduleMaintServiceImpl.RESOURCE_NAME))
				.andReturn(null);

		replay(mockSyncMan);

		result = obj.getAffectedPatients(false);

		verify(mockSyncMan);

		assertNull(result);

	}

	public void testGetAffectedPatientsUnboundCreate() {

		AffectedPatients result = null;

		Capture<Object> affectedPatientsCap = new Capture<Object>();
		expect(mockSyncMan.getResource(ScheduleMaintServiceImpl.RESOURCE_NAME))
				.andReturn(null);
		mockSyncMan.bindResource(eq(ScheduleMaintServiceImpl.RESOURCE_NAME),
				capture(affectedPatientsCap));

		replay(mockSyncMan);

		result = obj.getAffectedPatients(true);

		verify(mockSyncMan);

		assertNotNull(result);
		AffectedPatients capturedPatients = (AffectedPatients) affectedPatientsCap
				.getValue();
		assertEquals(capturedPatients, result);
		assertTrue(result.getAffectedIds().isEmpty());
	}

	public void testGetAffectedPatientsBoundCreate() {

		AffectedPatients result = null;

		AffectedPatients boundPatients = new AffectedPatients();
		boundPatients.getAffectedIds().add(new PatientIdentifier());
		boundPatients.getAffectedIds().add(new PatientIdentifier());
		boundPatients.getAffectedIds().add(new PatientIdentifier());

		expect(mockSyncMan.getResource(ScheduleMaintServiceImpl.RESOURCE_NAME))
				.andReturn(boundPatients);

		replay(mockSyncMan);

		result = obj.getAffectedPatients(true);

		verify(mockSyncMan);

		assertEquals(boundPatients, result);
		assertEquals(3, result.getAffectedIds().size());
	}

	public void testAddAffectedPatient() {

		PatientIdentifier patientId = new PatientIdentifier();
		AffectedPatients boundPatients = new AffectedPatients();

		expect(mockSyncMan.getResource(ScheduleMaintServiceImpl.RESOURCE_NAME))
				.andReturn(boundPatients);

		replay(mockSyncMan);

		obj.addAffectedPatient(patientId);

		verify(mockSyncMan);

		assertTrue(boundPatients.getAffectedIds().contains(patientId));
	}

	public void testRemoveAffectedPatient() {

		PatientIdentifier patientId = new PatientIdentifier();
		AffectedPatients boundPatients = new AffectedPatients();
		boundPatients.getAffectedIds().add(patientId);

		expect(mockSyncMan.getResource(ScheduleMaintServiceImpl.RESOURCE_NAME))
				.andReturn(boundPatients);

		replay(mockSyncMan);

		obj.removeAffectedPatient(patientId);

		verify(mockSyncMan);

		assertFalse(boundPatients.getAffectedIds().contains(patientId));
	}

	public void testClearAffectedPatients() {

		mockSyncMan.unbindResource(ScheduleMaintServiceImpl.RESOURCE_NAME);

		replay(mockSyncMan);

		obj.clearAffectedPatients();

		verify(mockSyncMan);
	}

	public void testRequestSynch() {

		Capture<TransactionSynchronization> syncCap = new Capture<TransactionSynchronization>();
		expect(
				mockSyncMan
						.containsSynchronization(ScheduleMaintSynchronization.class))
				.andReturn(false);
		mockSyncMan.registerSynchronization(capture(syncCap));

		replay(mockSyncMan);

		obj.requestSynch();

		verify(mockSyncMan);

		TransactionSynchronization txSync = syncCap.getValue();
		assertNotNull(txSync);
		assertTrue(txSync instanceof ScheduleMaintSynchronization);
		ScheduleMaintSynchronization maintSync = (ScheduleMaintSynchronization) txSync;
		assertEquals(obj, maintSync.schedService);
	}

	public void testRequestSynchAlreadyExists() {

		expect(
				mockSyncMan
						.containsSynchronization(ScheduleMaintSynchronization.class))
				.andReturn(true);

		replay(mockSyncMan);

		obj.requestSynch();

		verify(mockSyncMan);
	}

	public void testUpdateSchedule() {

		PatientIdentifier patientId = new PatientIdentifier();
		Capture<PatientIdentifier> patientIdCap = new Capture<PatientIdentifier>();
		mockAdjuster.adjustSchedule(capture(patientIdCap));

		replay(mockAdjuster);

		obj.updateSchedule(patientId);

		verify(mockAdjuster);

		assertEquals(patientId, patientIdCap.getValue());
	}
}
