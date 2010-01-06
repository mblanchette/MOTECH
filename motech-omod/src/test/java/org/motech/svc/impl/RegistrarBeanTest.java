package org.motech.svc.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.Capture;
import org.motech.messaging.MessageNotFoundException;
import org.motech.model.HIVStatus;
import org.motech.model.Log;
import org.motech.model.Message;
import org.motech.model.MessageProgramEnrollment;
import org.motech.model.MessageStatus;
import org.motech.model.ScheduledMessage;
import org.motech.model.TroubledPhone;
import org.motech.model.WhoRegistered;
import org.motech.openmrs.module.ContextService;
import org.motech.openmrs.module.MotechService;
import org.motech.svc.RegistrarBean;
import org.motech.util.GenderTypeConverter;
import org.motech.util.MotechConstants;
import org.motechproject.ws.ContactNumberType;
import org.motechproject.ws.Gender;
import org.motechproject.ws.LogType;
import org.motechproject.ws.MediaType;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.util.OpenmrsConstants;

public class RegistrarBeanTest extends TestCase {

	RegistrarBean regBean;

	ContextService contextService;
	LocationService locationService;
	PersonService personService;
	UserService userService;
	PatientService patientService;
	EncounterService encounterService;
	ObsService obsService;
	ConceptService conceptService;
	MotechService motechService;

	Location defaultClinic;
	PatientIdentifierType ghanaIdType;
	PersonAttributeType nurseIdAttributeType;
	PersonAttributeType primaryPhoneAttributeType;
	PersonAttributeType secondaryPhoneAttributeType;
	PersonAttributeType clinicAttributeType;
	PersonAttributeType nhisAttributeType;
	PersonAttributeType languageTextAttributeType;
	PersonAttributeType languageVoiceAttributeType;
	PersonAttributeType primaryPhoneTypeAttributeType;
	PersonAttributeType secondaryPhoneTypeAttributeType;
	PersonAttributeType mediaTypeInformationalAttributeType;
	PersonAttributeType mediaTypeReminderAttributeType;
	PersonAttributeType deliveryTimeAttributeType;
	PersonAttributeType nhisExpirationType;
	PersonAttributeType whoRegisteredType;
	PersonAttributeType ghsRegisteredAttributeType;
	PersonAttributeType ghsANCRegNumberAttributeType;
	PersonAttributeType ghsCWCRegNumberAttributeType;
	PersonAttributeType insuredAttributeType;
	PersonAttributeType hivStatusAttributeType;
	PersonAttributeType religionAttributeType;
	PersonAttributeType occupationAttributeType;
	Role providerRole;
	EncounterType matVisitType;
	ConceptName immunizationConceptNameObj;
	Concept immunizationConcept;
	ConceptName tetanusConceptNameObj;
	Concept tetanusConcept;
	ConceptName iptConceptNameObj;
	Concept iptConcept;
	ConceptName itnConceptNameObj;
	Concept itnConcept;
	ConceptName visitNumConceptNameObj;
	Concept visitNumConcept;
	ConceptName arvConceptNameObj;
	Concept arvConcept;
	ConceptName onArvConceptNameObj;
	Concept onArvConcept;
	ConceptName prePMTCTConceptNameObj;
	Concept prePMTCTConcept;
	ConceptName testPMTCTConceptNameObj;
	Concept testPMTCTConcept;
	ConceptName postPMTCTConceptNameObj;
	Concept postPMTCTConcept;
	ConceptName hemo36ConceptNameObj;
	Concept hemo36Concept;
	EncounterType pregVisitType;
	ConceptName pregStatusConceptNameObj;
	Concept pregStatusConcept;
	ConceptName dateConfConceptNameObj;
	Concept dateConfConcept;
	ConceptName dateConfConfirmedConceptNameObj;
	Concept dateConfConfirmedConcept;
	ConceptName gravidaConceptNameObj;
	Concept gravidaConcept;
	ConceptName parityConceptNameObj;
	Concept parityConcept;
	ConceptName hemoConceptNameObj;
	Concept hemoConcept;
	RelationshipType parentChildRelationshipType;

	@Override
	protected void setUp() throws Exception {
		contextService = createMock(ContextService.class);

		locationService = createMock(LocationService.class);
		personService = createMock(PersonService.class);
		userService = createMock(UserService.class);
		patientService = createMock(PatientService.class);
		encounterService = createMock(EncounterService.class);
		obsService = createMock(ObsService.class);
		conceptService = createMock(ConceptService.class);
		motechService = createMock(MotechService.class);

		defaultClinic = new Location(1);
		defaultClinic.setName(MotechConstants.LOCATION_GHANA);

		ghanaIdType = new PatientIdentifierType(1);
		ghanaIdType.setName(MotechConstants.PATIENT_IDENTIFIER_GHANA_CLINIC_ID);

		primaryPhoneAttributeType = new PersonAttributeType(2);
		primaryPhoneAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER);

		clinicAttributeType = new PersonAttributeType(3);
		clinicAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_HEALTH_CENTER);

		nhisAttributeType = new PersonAttributeType(4);
		nhisAttributeType.setName(MotechConstants.PERSON_ATTRIBUTE_NHIS_NUMBER);

		languageTextAttributeType = new PersonAttributeType(5);
		languageTextAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE_TEXT);

		primaryPhoneTypeAttributeType = new PersonAttributeType(6);
		primaryPhoneTypeAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_TYPE);

		mediaTypeInformationalAttributeType = new PersonAttributeType(7);
		mediaTypeInformationalAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE_INFORMATIONAL);

		deliveryTimeAttributeType = new PersonAttributeType(8);
		deliveryTimeAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_DELIVERY_TIME);

		nurseIdAttributeType = new PersonAttributeType(9);
		nurseIdAttributeType.setName(MotechConstants.PERSON_ATTRIBUTE_CHPS_ID);

		secondaryPhoneAttributeType = new PersonAttributeType(10);
		secondaryPhoneAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_NUMBER);

		secondaryPhoneTypeAttributeType = new PersonAttributeType(11);
		secondaryPhoneTypeAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_TYPE);

		nhisExpirationType = new PersonAttributeType(12);
		nhisExpirationType
				.setName(MotechConstants.PERSON_ATTRIBUTE_NHIS_EXP_DATE);

		mediaTypeReminderAttributeType = new PersonAttributeType(13);
		mediaTypeReminderAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE_REMINDER);

		languageVoiceAttributeType = new PersonAttributeType(14);
		languageVoiceAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE_VOICE);

		whoRegisteredType = new PersonAttributeType(15);
		whoRegisteredType
				.setName(MotechConstants.PERSON_ATTRIBUTE_WHO_REGISTERED);

		ghsRegisteredAttributeType = new PersonAttributeType(16);
		ghsRegisteredAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_GHS_REGISTERED);

		ghsANCRegNumberAttributeType = new PersonAttributeType(17);
		ghsANCRegNumberAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_GHS_ANC_REG_NUMBER);

		insuredAttributeType = new PersonAttributeType(18);
		insuredAttributeType.setName(MotechConstants.PERSON_ATTRIBUTE_INSURED);

		hivStatusAttributeType = new PersonAttributeType(19);
		hivStatusAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_HIV_STATUS);

		religionAttributeType = new PersonAttributeType(20);
		religionAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_RELIGION);

		occupationAttributeType = new PersonAttributeType(21);
		occupationAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_OCCUPATION);

		ghsCWCRegNumberAttributeType = new PersonAttributeType(22);
		ghsCWCRegNumberAttributeType
				.setName(MotechConstants.PERSON_ATTRIBUTE_GHS_CWC_REG_NUMBER);

		providerRole = new Role(OpenmrsConstants.PROVIDER_ROLE);

		matVisitType = new EncounterType(5);
		matVisitType.setName(MotechConstants.ENCOUNTER_TYPE_MATERNALVISIT);

		immunizationConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_IMMUNIZATIONS_ORDERED, Locale
						.getDefault());
		immunizationConcept = new Concept(6);

		tetanusConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_TETANUS_BOOSTER, Locale.getDefault());
		tetanusConcept = new Concept(7);

		iptConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_INTERMITTENT_PREVENTATIVE_TREATMENT,
				Locale.getDefault());
		iptConcept = new Concept(8);

		itnConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_INSECTICIDE_TREATED_NET_USAGE, Locale
						.getDefault());
		itnConcept = new Concept(9);

		visitNumConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_PREGNANCY_VISIT_NUMBER, Locale
						.getDefault());
		visitNumConcept = new Concept(10);

		arvConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_ANTIRETROVIRAL_USE_DURING_PREGNANCY,
				Locale.getDefault());
		arvConcept = new Concept(11);

		onArvConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_ON_ANTIRETROVIRAL_THERAPY, Locale
						.getDefault());
		onArvConcept = new Concept(12);

		prePMTCTConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_PRE_PREVENTING_MATERNAL_TO_CHILD_TRANSMISSION,
				Locale.getDefault());
		prePMTCTConcept = new Concept(13);

		testPMTCTConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_TEST_PREVENTING_MATERNAL_TO_CHILD_TRANSMISSION,
				Locale.getDefault());
		testPMTCTConcept = new Concept(14);

		postPMTCTConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_POST_PREVENTING_MATERNAL_TO_CHILD_TRANSMISSION,
				Locale.getDefault());
		postPMTCTConcept = new Concept(15);

		hemo36ConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_HEMOGLOBIN_AT_36_WEEKS, Locale
						.getDefault());
		hemo36Concept = new Concept(16);

		pregVisitType = new EncounterType(17);
		pregVisitType.setName(MotechConstants.ENCOUNTER_TYPE_PREGNANCYVISIT);

		pregStatusConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_PREGNANCY_STATUS, Locale.getDefault());
		pregStatusConcept = new Concept(18);

		dateConfConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_ESTIMATED_DATE_OF_CONFINEMENT, Locale
						.getDefault());
		dateConfConcept = new Concept(19);

		gravidaConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_GRAVIDA, Locale.getDefault());
		gravidaConcept = new Concept(20);

		hemoConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_HEMOGLOBIN, Locale.getDefault());
		hemoConcept = new Concept(21);

		parityConceptNameObj = new ConceptName(MotechConstants.CONCEPT_PARITY,
				Locale.getDefault());
		parityConcept = new Concept(22);

		dateConfConfirmedConceptNameObj = new ConceptName(
				MotechConstants.CONCEPT_DATE_OF_CONFINEMENT_CONFIRMED, Locale
						.getDefault());
		dateConfConfirmedConcept = new Concept(23);

		parentChildRelationshipType = new RelationshipType(1);
		parentChildRelationshipType.setaIsToB("Parent");
		parentChildRelationshipType.setbIsToA("Child");

		RegistrarBeanImpl regBeanImpl = new RegistrarBeanImpl();
		regBeanImpl.setContextService(contextService);

		regBean = regBeanImpl;
	}

	@Override
	protected void tearDown() throws Exception {
		regBean = null;

		contextService = null;
		locationService = null;
		personService = null;
		userService = null;
		patientService = null;
		encounterService = null;
		obsService = null;
		conceptService = null;
		motechService = null;
	}

	public void testRegisterChild() {

		Date regDate = new Date(3489), childDob = new Date(874984), nhisExpires = new Date(
				3784784);
		String motherId = "PATIENT1234";
		String childId = "CHILD3783";
		Gender childGender = Gender.MALE;
		String childFirstName = "Harold";
		String nhis = "NHISNUMBER";
		Integer nurseInternalId = 1;

		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();

		User nurseUser = new User(nurseInternalId);

		expect(contextService.getPatientService()).andReturn(patientService)
				.atLeastOnce();
		expect(
				patientService
						.getPatientIdentifierTypeByName(MotechConstants.PATIENT_IDENTIFIER_GHANA_CLINIC_ID))
				.andReturn(ghanaIdType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_NHIS_NUMBER))
				.andReturn(nhisAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_NHIS_EXP_DATE))
				.andReturn(nhisExpirationType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_WHO_REGISTERED))
				.andReturn(whoRegisteredType);

		Patient mother = new Patient();

		PatientIdentifier motherIdObj = new PatientIdentifier();
		motherIdObj.setIdentifierType(ghanaIdType);
		motherIdObj.setIdentifier(motherId);
		mother.addIdentifier(motherIdObj);

		PersonAddress testAddress = new PersonAddress();
		testAddress.setRegion("AREGION");
		testAddress.setCountyDistrict("ADISTRICT");
		mother.addAddress(testAddress);

		Capture<Patient> childCapture = new Capture<Patient>();
		expect(patientService.savePatient(capture(childCapture))).andReturn(
				new Patient());

		replay(contextService, personService, patientService);

		regBean.registerChild(nurseUser, regDate, mother, childId, childDob,
				childGender, childFirstName, nhis, nhisExpires);

		verify(contextService, personService, patientService);

		Patient child = childCapture.getValue();
		assertEquals(childId, child.getPatientIdentifier(ghanaIdType)
				.getIdentifier());
		assertEquals(testAddress.getRegion(), child.getPersonAddress()
				.getRegion());
		assertEquals(testAddress.getCountyDistrict(), child.getPersonAddress()
				.getCountyDistrict());
		assertEquals(testAddress.getCityVillage(), child.getPersonAddress()
				.getCityVillage());
		assertEquals(testAddress.getAddress1(), child.getPersonAddress()
				.getAddress1());
		assertEquals(childDob, child.getBirthdate());
		assertEquals(childFirstName, child.getGivenName());
		assertEquals(childGender, GenderTypeConverter.valueOfOpenMRS(child
				.getGender()));
		PersonAttribute nhisAttr = child.getAttribute(nhisAttributeType);
		assertEquals(nhis, nhisAttr.getValue());
		assertEquals(nhisExpires.toString(), child.getAttribute(
				nhisExpirationType).getHydratedObject());
		assertEquals(WhoRegistered.CHPS_STAFF, WhoRegistered.valueOf(child
				.getAttribute(whoRegisteredType).getValue()));
	}

	public void testRegisterClinic() {
		String clinicName = "A-Test-Clinic-Name";
		String description = "A Ghana Clinic Location";
		Integer clinicId = 3;
		Integer parentId = 2;

		expect(contextService.getLocationService()).andReturn(locationService);

		Capture<Location> locationCap = new Capture<Location>();
		Capture<Location> parentCap = new Capture<Location>();

		expect(locationService.saveLocation(capture(locationCap))).andReturn(
				new Location(clinicId));
		expect(locationService.getLocation(parentId)).andReturn(
				new Location(parentId));
		expect(locationService.saveLocation(capture(parentCap))).andReturn(
				new Location());

		replay(contextService, locationService);

		regBean.registerClinic(clinicName, parentId);

		verify(contextService, locationService);

		Location location = locationCap.getValue();
		assertEquals(clinicName, location.getName());
		assertEquals(description, location.getDescription());

		Location parent = parentCap.getValue();
		Set<Location> childLocations = parent.getChildLocations();
		assertEquals(1, childLocations.size());

		Location childLocation = childLocations.iterator().next();
		assertEquals(clinicId, childLocation.getLocationId());
		assertEquals(parentId, childLocation.getParentLocation()
				.getLocationId());
	}

	public void testRegisterNurse() {

		String name = "Jenny", id = "123abc", phone = "12078675309", clinic = "Mayo Clinic";

		Location clinicLocation = new Location(1);
		clinicLocation.setName(clinic);

		Capture<User> nurseCap = new Capture<User>();

		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(contextService.getUserService()).andReturn(userService);
		expect(contextService.getLocationService()).andReturn(locationService);

		expect(personService.parsePersonName(name)).andReturn(
				new PersonName(name, null, null));
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_CHPS_ID))
				.andReturn(nurseIdAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(userService.getRole(OpenmrsConstants.PROVIDER_ROLE)).andReturn(
				providerRole);
		expect(locationService.getLocation(clinic)).andReturn(clinicLocation);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_HEALTH_CENTER))
				.andReturn(clinicAttributeType);
		expect(userService.saveUser(capture(nurseCap), (String) anyObject()))
				.andReturn(new User());

		replay(contextService, personService, userService, locationService);

		regBean.registerNurse(name, id, phone, clinic);

		verify(contextService, personService, userService, locationService);

		User nurse = nurseCap.getValue();
		assertEquals(name, nurse.getGivenName());
		assertEquals(id, nurse.getAttribute(nurseIdAttributeType).getValue());
		assertEquals(phone, nurse.getAttribute(primaryPhoneAttributeType)
				.getValue());
		assertEquals(clinicLocation.getLocationId().toString(), nurse
				.getAttribute(clinicAttributeType).getValue());
	}

	public void testRegisterPregnantMother() {
		String firstName = "FirstName", lastName = "LastName", prefName = "PrefName";
		String regNumberGHS = "123ABC", nhis = "456DEF";
		String region = "Region", district = "District", community = "Community", address = "Address";
		String religion = "Religion", occupation = "Occupation";
		String primaryPhone = "12075555555", secondaryPhone = "12075555556";
		String languageVoice = "LanguageVoice", languageText = "LanguageText";
		Date date = new Date();
		Boolean birthDateEst = true, registeredGHS = true, insured = true, dueDateConfirmed = true, registerPregProgram = true;
		Integer clinic = 1, gravida = 0, parity = 1;
		HIVStatus hivStatus = HIVStatus.UNKNOWN;
		ContactNumberType primaryPhoneType = ContactNumberType.PERSONAL, secondaryPhoneType = ContactNumberType.PUBLIC;
		MediaType mediaTypeInfo = MediaType.TEXT, mediaTypeReminder = MediaType.VOICE;
		WhoRegistered whoRegistered = WhoRegistered.CHPS_STAFF;

		String pregnancyProgramName = "Weekly Pregnancy Message Program";

		User nurse = new User(1);
		Patient patient = new Patient(2);
		Location ghanaLocation = new Location(1);

		Capture<Patient> patientCap = new Capture<Patient>();
		Capture<MessageProgramEnrollment> enrollmentCap = new Capture<MessageProgramEnrollment>();
		Capture<Encounter> encounterCap = new Capture<Encounter>();
		Capture<Obs> dueDateObsCap = new Capture<Obs>();
		Capture<Obs> dueDateConfirmedObsCap = new Capture<Obs>();
		Capture<Obs> gravidaObsCap = new Capture<Obs>();
		Capture<Obs> parityObsCap = new Capture<Obs>();

		expect(contextService.getPatientService()).andReturn(patientService)
				.atLeastOnce();
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(contextService.getLocationService()).andReturn(locationService)
				.atLeastOnce();
		expect(contextService.getMotechService()).andReturn(motechService)
				.atLeastOnce();
		expect(contextService.getEncounterService())
				.andReturn(encounterService).atLeastOnce();
		expect(contextService.getObsService()).andReturn(obsService);
		expect(contextService.getConceptService()).andReturn(conceptService)
				.atLeastOnce();

		expect(contextService.getAuthenticatedUser()).andReturn(nurse);
		expect(
				patientService
						.getPatientIdentifierTypeByName(MotechConstants.PATIENT_IDENTIFIER_GHANA_CLINIC_ID))
				.andReturn(ghanaIdType);
		expect(locationService.getLocation(MotechConstants.LOCATION_GHANA))
				.andReturn(ghanaLocation);

		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_GHS_REGISTERED))
				.andReturn(ghsRegisteredAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_INSURED))
				.andReturn(insuredAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_NHIS_NUMBER))
				.andReturn(nhisAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_NHIS_EXP_DATE))
				.andReturn(nhisExpirationType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_HEALTH_CENTER))
				.andReturn(clinicAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_TYPE))
				.andReturn(primaryPhoneTypeAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_NUMBER))
				.andReturn(secondaryPhoneAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_TYPE))
				.andReturn(secondaryPhoneTypeAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE_INFORMATIONAL))
				.andReturn(mediaTypeInformationalAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE_REMINDER))
				.andReturn(mediaTypeReminderAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE_TEXT))
				.andReturn(languageTextAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE_VOICE))
				.andReturn(languageVoiceAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_WHO_REGISTERED))
				.andReturn(whoRegisteredType);

		expect(locationService.getLocation(MotechConstants.LOCATION_GHANA))
				.andReturn(ghanaLocation);

		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_GHS_ANC_REG_NUMBER))
				.andReturn(ghsANCRegNumberAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_HIV_STATUS))
				.andReturn(hivStatusAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_RELIGION))
				.andReturn(religionAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_OCCUPATION))
				.andReturn(occupationAttributeType);

		expect(patientService.savePatient(capture(patientCap))).andReturn(
				patient);

		expect(
				motechService.getActiveMessageProgramEnrollment(patient
						.getPatientId(), pregnancyProgramName)).andReturn(null);
		expect(
				motechService
						.saveMessageProgramEnrollment(capture(enrollmentCap)))
				.andReturn(new MessageProgramEnrollment());

		expect(
				encounterService
						.getEncounterType(MotechConstants.ENCOUNTER_TYPE_PREGNANCYVISIT))
				.andReturn(pregVisitType);
		expect(encounterService.saveEncounter(capture(encounterCap)))
				.andReturn(new Encounter());

		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_ESTIMATED_DATE_OF_CONFINEMENT))
				.andReturn(dateConfConcept);
		expect(obsService.saveObs(capture(dueDateObsCap), (String) anyObject()))
				.andReturn(new Obs());

		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_DATE_OF_CONFINEMENT_CONFIRMED))
				.andReturn(dateConfConfirmedConcept);
		expect(
				obsService.saveObs(capture(dueDateConfirmedObsCap),
						(String) anyObject())).andReturn(new Obs());

		expect(conceptService.getConcept(MotechConstants.CONCEPT_GRAVIDA))
				.andReturn(gravidaConcept);
		expect(obsService.saveObs(capture(gravidaObsCap), (String) anyObject()))
				.andReturn(new Obs());

		expect(conceptService.getConcept(MotechConstants.CONCEPT_PARITY))
				.andReturn(parityConcept);
		expect(obsService.saveObs(capture(parityObsCap), (String) anyObject()))
				.andReturn(new Obs());

		replay(contextService, patientService, motechService, personService,
				locationService, userService, encounterService, obsService,
				conceptService);

		regBean.registerPregnantMother(firstName, lastName, prefName, date,
				birthDateEst, registeredGHS, regNumberGHS, insured, nhis, date,
				region, district, community, address, clinic, date,
				dueDateConfirmed, gravida, parity, hivStatus,
				registerPregProgram, primaryPhone, primaryPhoneType,
				secondaryPhone, secondaryPhoneType, mediaTypeInfo,
				mediaTypeReminder, languageVoice, languageText, whoRegistered,
				religion, occupation);

		verify(contextService, patientService, motechService, personService,
				locationService, userService, encounterService, obsService,
				conceptService);

		Patient capturedPatient = patientCap.getValue();
		assertEquals(regNumberGHS, capturedPatient.getPatientIdentifier(
				ghanaIdType).getIdentifier());
		assertEquals(firstName, capturedPatient.getGivenName());
		assertEquals(lastName, capturedPatient.getFamilyName());
		assertEquals(prefName, capturedPatient.getMiddleName());
		assertEquals(date, capturedPatient.getBirthdate());
		assertEquals(birthDateEst, capturedPatient.getBirthdateEstimated());
		assertEquals(GenderTypeConverter.toOpenMRSString(Gender.FEMALE),
				capturedPatient.getGender());
		assertEquals(region, capturedPatient.getPersonAddress().getRegion());
		assertEquals(district, capturedPatient.getPersonAddress()
				.getCountyDistrict());
		assertEquals(community, capturedPatient.getPersonAddress()
				.getCityVillage());
		assertEquals(address, capturedPatient.getPersonAddress().getAddress1());
		assertEquals(clinic, Integer.valueOf(capturedPatient.getAttribute(
				clinicAttributeType).getValue()));
		assertEquals(registeredGHS, Boolean.valueOf(capturedPatient
				.getAttribute(ghsRegisteredAttributeType).getValue()));
		assertEquals(regNumberGHS, capturedPatient.getAttribute(
				ghsANCRegNumberAttributeType).getValue());
		assertEquals(insured, Boolean.valueOf(capturedPatient.getAttribute(
				insuredAttributeType).getValue()));
		assertEquals(nhis, capturedPatient.getAttribute(nhisAttributeType)
				.getValue());
		assertEquals(date.toString(), capturedPatient.getAttribute(
				nhisExpirationType).getValue());
		assertEquals(hivStatus, HIVStatus.valueOf(capturedPatient.getAttribute(
				hivStatusAttributeType).getValue()));
		assertEquals(primaryPhone, capturedPatient.getAttribute(
				primaryPhoneAttributeType).getValue());
		assertEquals(secondaryPhone, capturedPatient.getAttribute(
				secondaryPhoneAttributeType).getValue());
		assertEquals(primaryPhoneType, ContactNumberType
				.valueOf(capturedPatient.getAttribute(
						primaryPhoneTypeAttributeType).getValue()));
		assertEquals(secondaryPhoneType, ContactNumberType
				.valueOf(capturedPatient.getAttribute(
						secondaryPhoneTypeAttributeType).getValue()));
		assertEquals(mediaTypeInfo, MediaType.valueOf(capturedPatient
				.getAttribute(mediaTypeInformationalAttributeType).getValue()));
		assertEquals(mediaTypeReminder, MediaType.valueOf(capturedPatient
				.getAttribute(mediaTypeReminderAttributeType).getValue()));
		assertEquals(languageText, capturedPatient.getAttribute(
				languageTextAttributeType).getValue());
		assertEquals(languageVoice, capturedPatient.getAttribute(
				languageVoiceAttributeType).getValue());
		assertEquals(whoRegistered, WhoRegistered.valueOf(capturedPatient
				.getAttribute(whoRegisteredType).getValue()));
		assertEquals(religion, capturedPatient.getAttribute(
				religionAttributeType).getValue());
		assertEquals(occupation, capturedPatient.getAttribute(
				occupationAttributeType).getValue());

		MessageProgramEnrollment enrollment = enrollmentCap.getValue();
		assertEquals(patient.getPatientId(), enrollment.getPersonId());
		assertEquals(pregnancyProgramName, enrollment.getProgram());
		assertNotNull("Enrollment start date should not be null", enrollment
				.getStartDate());
		assertNull("Enrollment end date should be null", enrollment
				.getEndDate());

		Encounter encounter = encounterCap.getValue();
		assertEquals(nurse, encounter.getProvider());
		assertEquals(patient, encounter.getPatient());
		assertEquals(ghanaLocation, encounter.getLocation());

		Obs dueDateObs = dueDateObsCap.getValue();
		assertEquals(encounter.getEncounterDatetime(), dueDateObs
				.getObsDatetime());
		assertEquals(encounter.getPatient().getPatientId(), dueDateObs
				.getPerson().getPersonId());
		assertEquals(ghanaLocation, dueDateObs.getLocation());
		assertEquals(dateConfConcept, dueDateObs.getConcept());
		assertEquals(date, dueDateObs.getValueDatetime());

		Obs dueDateConfirmedObs = dueDateConfirmedObsCap.getValue();
		assertEquals(encounter.getEncounterDatetime(), dueDateConfirmedObs
				.getObsDatetime());
		assertEquals(encounter.getPatient().getPatientId(), dueDateConfirmedObs
				.getPerson().getPersonId());
		assertEquals(ghanaLocation, dueDateConfirmedObs.getLocation());
		assertEquals(dateConfConfirmedConcept, dueDateConfirmedObs.getConcept());
		assertEquals(Boolean.TRUE, dueDateConfirmedObs.getValueAsBoolean());

		Obs gravidaObs = gravidaObsCap.getValue();
		assertEquals(encounter.getEncounterDatetime(), gravidaObs
				.getObsDatetime());
		assertEquals(encounter.getPatient().getPatientId(), gravidaObs
				.getPerson().getPersonId());
		assertEquals(ghanaLocation, gravidaObs.getLocation());
		assertEquals(gravidaConcept, gravidaObs.getConcept());
		assertEquals(0.0, gravidaObs.getValueNumeric());

		Obs parityObs = parityObsCap.getValue();
		assertEquals(encounter.getEncounterDatetime(), parityObs
				.getObsDatetime());
		assertEquals(encounter.getPatient().getPatientId(), parityObs
				.getPerson().getPersonId());
		assertEquals(ghanaLocation, parityObs.getLocation());
		assertEquals(parityConcept, parityObs.getConcept());
		assertEquals(1.0, parityObs.getValueNumeric());
	}

	public void testRegisterChildWithProgram() {
		String firstName = "FirstName", lastName = "LastName", prefName = "PrefName";
		String regNumberGHS = "123ABC", nhis = "456DEF";
		String region = "Region", district = "District", community = "Community", address = "Address";
		String primaryPhone = "12075555555", secondaryPhone = "12075555556";
		String languageVoice = "LanguageVoice", languageText = "LanguageText";
		Date date = new Date();
		Boolean birthDateEst = true, registeredGHS = true, insured = true, registerPregProgram = true;
		Integer motherId = 564, clinic = 1;
		ContactNumberType primaryPhoneType = ContactNumberType.PERSONAL, secondaryPhoneType = ContactNumberType.PUBLIC;
		MediaType mediaTypeInfo = MediaType.TEXT, mediaTypeReminder = MediaType.VOICE;
		WhoRegistered whoRegistered = WhoRegistered.CHPS_STAFF;
		Gender sex = Gender.FEMALE;

		String pregnancyProgramName = "Weekly Info Pregnancy Message Program";

		Patient child = new Patient(1);
		Patient mother = new Patient(motherId);
		Location ghanaLocation = new Location(1);

		Capture<Patient> patientCap = new Capture<Patient>();
		Capture<MessageProgramEnrollment> enrollmentCap = new Capture<MessageProgramEnrollment>();
		Capture<Relationship> relationshipCap = new Capture<Relationship>();

		expect(contextService.getPatientService()).andReturn(patientService)
				.atLeastOnce();
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(contextService.getLocationService()).andReturn(locationService);
		expect(contextService.getMotechService()).andReturn(motechService)
				.atLeastOnce();

		expect(
				patientService
						.getPatientIdentifierTypeByName(MotechConstants.PATIENT_IDENTIFIER_GHANA_CLINIC_ID))
				.andReturn(ghanaIdType);
		expect(locationService.getLocation(MotechConstants.LOCATION_GHANA))
				.andReturn(ghanaLocation);

		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_GHS_REGISTERED))
				.andReturn(ghsRegisteredAttributeType);

		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_INSURED))
				.andReturn(insuredAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_NHIS_NUMBER))
				.andReturn(nhisAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_NHIS_EXP_DATE))
				.andReturn(nhisExpirationType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_HEALTH_CENTER))
				.andReturn(clinicAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_TYPE))
				.andReturn(primaryPhoneTypeAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_NUMBER))
				.andReturn(secondaryPhoneAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_TYPE))
				.andReturn(secondaryPhoneTypeAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE_INFORMATIONAL))
				.andReturn(mediaTypeInformationalAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE_REMINDER))
				.andReturn(mediaTypeReminderAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE_TEXT))
				.andReturn(languageTextAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE_VOICE))
				.andReturn(languageVoiceAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_WHO_REGISTERED))
				.andReturn(whoRegisteredType);

		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_GHS_CWC_REG_NUMBER))
				.andReturn(ghsCWCRegNumberAttributeType);

		expect(patientService.savePatient(capture(patientCap)))
				.andReturn(child);

		expect(patientService.getPatient(motherId)).andReturn(mother);
		expect(
				personService
						.getRelationshipTypeByName(MotechConstants.RELATIONSHIP_TYPE_PARENT_CHILD))
				.andReturn(parentChildRelationshipType);
		expect(personService.saveRelationship(capture(relationshipCap)))
				.andReturn(new Relationship());

		expect(
				motechService.getActiveMessageProgramEnrollment(child
						.getPatientId(), pregnancyProgramName)).andReturn(null);
		expect(
				motechService
						.saveMessageProgramEnrollment(capture(enrollmentCap)))
				.andReturn(new MessageProgramEnrollment());

		replay(contextService, patientService, motechService, personService,
				locationService, userService, encounterService, obsService,
				conceptService);

		regBean.registerChild(firstName, lastName, prefName, date,
				birthDateEst, sex, motherId, registeredGHS, regNumberGHS,
				insured, nhis, date, region, district, community, address,
				clinic, registerPregProgram, primaryPhone, primaryPhoneType,
				secondaryPhone, secondaryPhoneType, mediaTypeInfo,
				mediaTypeReminder, languageVoice, languageText, whoRegistered);

		verify(contextService, patientService, motechService, personService,
				locationService, userService, encounterService, obsService,
				conceptService);

		Patient capturedPatient = patientCap.getValue();
		assertEquals(regNumberGHS, capturedPatient.getPatientIdentifier(
				ghanaIdType).getIdentifier());
		assertEquals(firstName, capturedPatient.getGivenName());
		assertEquals(lastName, capturedPatient.getFamilyName());
		assertEquals(prefName, capturedPatient.getMiddleName());
		assertEquals(date, capturedPatient.getBirthdate());
		assertEquals(birthDateEst, capturedPatient.getBirthdateEstimated());
		assertEquals(GenderTypeConverter.toOpenMRSString(sex), capturedPatient
				.getGender());
		assertEquals(region, capturedPatient.getPersonAddress().getRegion());
		assertEquals(district, capturedPatient.getPersonAddress()
				.getCountyDistrict());
		assertEquals(community, capturedPatient.getPersonAddress()
				.getCityVillage());
		assertEquals(address, capturedPatient.getPersonAddress().getAddress1());
		assertEquals(clinic, Integer.valueOf(capturedPatient.getAttribute(
				clinicAttributeType).getValue()));
		assertEquals(registeredGHS, Boolean.valueOf(capturedPatient
				.getAttribute(ghsRegisteredAttributeType).getValue()));
		assertEquals(regNumberGHS, capturedPatient.getAttribute(
				ghsCWCRegNumberAttributeType).getValue());
		assertEquals(insured, Boolean.valueOf(capturedPatient.getAttribute(
				insuredAttributeType).getValue()));
		assertEquals(nhis, capturedPatient.getAttribute(nhisAttributeType)
				.getValue());
		assertEquals(date.toString(), capturedPatient.getAttribute(
				nhisExpirationType).getValue());
		assertEquals(primaryPhone, capturedPatient.getAttribute(
				primaryPhoneAttributeType).getValue());
		assertEquals(secondaryPhone, capturedPatient.getAttribute(
				secondaryPhoneAttributeType).getValue());
		assertEquals(primaryPhoneType, ContactNumberType
				.valueOf(capturedPatient.getAttribute(
						primaryPhoneTypeAttributeType).getValue()));
		assertEquals(secondaryPhoneType, ContactNumberType
				.valueOf(capturedPatient.getAttribute(
						secondaryPhoneTypeAttributeType).getValue()));
		assertEquals(mediaTypeInfo, MediaType.valueOf(capturedPatient
				.getAttribute(mediaTypeInformationalAttributeType).getValue()));
		assertEquals(mediaTypeReminder, MediaType.valueOf(capturedPatient
				.getAttribute(mediaTypeReminderAttributeType).getValue()));
		assertEquals(languageText, capturedPatient.getAttribute(
				languageTextAttributeType).getValue());
		assertEquals(languageVoice, capturedPatient.getAttribute(
				languageVoiceAttributeType).getValue());
		assertEquals(whoRegistered, WhoRegistered.valueOf(capturedPatient
				.getAttribute(whoRegisteredType).getValue()));

		MessageProgramEnrollment enrollment = enrollmentCap.getValue();
		assertEquals(child.getPatientId(), enrollment.getPersonId());
		assertEquals(pregnancyProgramName, enrollment.getProgram());
		assertNotNull("Enrollment start date should not be null", enrollment
				.getStartDate());
		assertNull("Enrollment end date should be null", enrollment
				.getEndDate());

		Relationship relationship = relationshipCap.getValue();
		assertEquals(parentChildRelationshipType, relationship
				.getRelationshipType());
		assertEquals(motherId, relationship.getPersonA().getPersonId());
		assertEquals(child.getPatientId(), relationship.getPersonB()
				.getPersonId());
	}

	public void testEditPatient() {

		Integer patientId = 1;
		String pPhone = "12075551212", sPhone = "120773733373", nhis = "28";
		Date nhisExpires = new Date();
		ContactNumberType pPhoneType = ContactNumberType.PERSONAL;
		ContactNumberType sPhoneType = ContactNumberType.HOUSEHOLD;

		User nurse = new User(2);
		Patient patient = new Patient(patientId);

		Capture<Patient> patientCap = new Capture<Patient>();

		expect(contextService.getPatientService()).andReturn(patientService)
				.atLeastOnce();
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();

		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_TYPE))
				.andReturn(primaryPhoneTypeAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_NUMBER))
				.andReturn(secondaryPhoneAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_TYPE))
				.andReturn(secondaryPhoneTypeAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_NHIS_NUMBER))
				.andReturn(nhisAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_NHIS_EXP_DATE))
				.andReturn(nhisExpirationType);
		expect(patientService.savePatient(capture(patientCap))).andReturn(
				new Patient(patientId));

		replay(contextService, patientService, personService);

		regBean.editPatient(nurse, patient, pPhone, pPhoneType, sPhone,
				sPhoneType, nhis, nhisExpires);

		verify(contextService, patientService, personService);

		Patient capturedPatient = patientCap.getValue();
		assertEquals(pPhone, capturedPatient.getAttribute(
				primaryPhoneAttributeType).getValue());
		assertEquals(pPhoneType.toString(), capturedPatient.getAttribute(
				primaryPhoneTypeAttributeType).getValue());
		assertEquals(sPhone, capturedPatient.getAttribute(
				secondaryPhoneAttributeType).getValue());
		assertEquals(sPhoneType.toString(), capturedPatient.getAttribute(
				secondaryPhoneTypeAttributeType).getValue());
		assertEquals(nhis, capturedPatient.getAttribute(nhisAttributeType)
				.getValue());
		assertEquals(nhisExpires.toString(), capturedPatient.getAttribute(
				nhisExpirationType).getValue());
	}

	public void testEditPatientAll() {
		Integer patientId = 2, clinic = 1;
		String firstName = "FirstName", lastName = "LastName", prefName = "PrefName";
		String regNumberGHS = "123ABC", nhis = "456DEF";
		String region = "Region", district = "District", community = "Community", address = "Address";
		String primaryPhone = "12075555555", secondaryPhone = "12075555556";
		String languageVoice = "LanguageVoice", languageText = "LanguageText";
		Date date = new Date();
		Gender sex = Gender.FEMALE;
		Boolean birthDateEst = true, registeredGHS = true, insured = true;
		ContactNumberType primaryPhoneType = ContactNumberType.PERSONAL, secondaryPhoneType = ContactNumberType.PUBLIC;
		MediaType mediaTypeInfo = MediaType.TEXT, mediaTypeReminder = MediaType.VOICE;
		WhoRegistered whoRegistered = WhoRegistered.CHPS_STAFF;

		Patient patient = new Patient(patientId);
		Location ghanaLocation = new Location(1);

		Capture<Patient> patientCap = new Capture<Patient>();

		expect(contextService.getPatientService()).andReturn(patientService)
				.atLeastOnce();
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(contextService.getLocationService()).andReturn(locationService)
				.atLeastOnce();

		expect(patientService.getPatient(patientId)).andReturn(patient);

		expect(
				patientService
						.getPatientIdentifierTypeByName(MotechConstants.PATIENT_IDENTIFIER_GHANA_CLINIC_ID))
				.andReturn(ghanaIdType);
		expect(locationService.getLocation(MotechConstants.LOCATION_GHANA))
				.andReturn(ghanaLocation);

		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_GHS_REGISTERED))
				.andReturn(ghsRegisteredAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_INSURED))
				.andReturn(insuredAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_NHIS_NUMBER))
				.andReturn(nhisAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_NHIS_EXP_DATE))
				.andReturn(nhisExpirationType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_HEALTH_CENTER))
				.andReturn(clinicAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_TYPE))
				.andReturn(primaryPhoneTypeAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_NUMBER))
				.andReturn(secondaryPhoneAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_TYPE))
				.andReturn(secondaryPhoneTypeAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE_INFORMATIONAL))
				.andReturn(mediaTypeInformationalAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE_REMINDER))
				.andReturn(mediaTypeReminderAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE_VOICE))
				.andReturn(languageVoiceAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE_TEXT))
				.andReturn(languageTextAttributeType);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_WHO_REGISTERED))
				.andReturn(whoRegisteredType);

		expect(patientService.savePatient(capture(patientCap))).andReturn(
				patient);

		replay(contextService, patientService, motechService, personService,
				locationService, userService, encounterService, obsService,
				conceptService);

		regBean.editPatient(patientId, firstName, lastName, prefName, date,
				birthDateEst, sex, registeredGHS, regNumberGHS, insured, nhis,
				date, region, district, community, address, clinic,
				primaryPhone, primaryPhoneType, secondaryPhone,
				secondaryPhoneType, mediaTypeInfo, mediaTypeReminder,
				languageVoice, languageText, whoRegistered);

		verify(contextService, patientService, motechService, personService,
				locationService, userService, encounterService, obsService,
				conceptService);

		Patient capturedPatient = patientCap.getValue();
		assertEquals(date, capturedPatient.getBirthdate());
		assertEquals(birthDateEst, capturedPatient.getBirthdateEstimated());
		assertEquals(sex, GenderTypeConverter.valueOfOpenMRS(capturedPatient
				.getGender()));
		assertEquals(firstName, capturedPatient.getGivenName());
		assertEquals(lastName, capturedPatient.getFamilyName());
		assertEquals(prefName, capturedPatient.getMiddleName());
		assertEquals(region, capturedPatient.getPersonAddress().getRegion());
		assertEquals(district, capturedPatient.getPersonAddress()
				.getCountyDistrict());
		assertEquals(community, capturedPatient.getPersonAddress()
				.getCityVillage());
		assertEquals(address, capturedPatient.getPersonAddress().getAddress1());
		assertEquals(regNumberGHS, capturedPatient.getPatientIdentifier(
				ghanaIdType).getIdentifier());
		assertEquals(registeredGHS, Boolean.valueOf(capturedPatient
				.getAttribute(ghsRegisteredAttributeType).getValue()));
		assertEquals(insured, Boolean.valueOf(capturedPatient.getAttribute(
				insuredAttributeType).getValue()));
		assertEquals(nhis, capturedPatient.getAttribute(nhisAttributeType)
				.getValue());
		assertEquals(date.toString(), capturedPatient.getAttribute(
				nhisExpirationType).getValue());
		assertEquals(clinic, Integer.valueOf(capturedPatient.getAttribute(
				clinicAttributeType).getValue()));
		assertEquals(primaryPhone, capturedPatient.getAttribute(
				primaryPhoneAttributeType).getValue());
		assertEquals(secondaryPhone, capturedPatient.getAttribute(
				secondaryPhoneAttributeType).getValue());
		assertEquals(primaryPhoneType, ContactNumberType
				.valueOf(capturedPatient.getAttribute(
						primaryPhoneTypeAttributeType).getValue()));
		assertEquals(secondaryPhoneType, ContactNumberType
				.valueOf(capturedPatient.getAttribute(
						secondaryPhoneTypeAttributeType).getValue()));
		assertEquals(mediaTypeInfo, MediaType.valueOf(capturedPatient
				.getAttribute(mediaTypeInformationalAttributeType).getValue()));
		assertEquals(mediaTypeReminder, MediaType.valueOf(capturedPatient
				.getAttribute(mediaTypeReminderAttributeType).getValue()));
		assertEquals(languageText, capturedPatient.getAttribute(
				languageTextAttributeType).getValue());
		assertEquals(languageVoice, capturedPatient.getAttribute(
				languageVoiceAttributeType).getValue());
		assertEquals(whoRegistered, WhoRegistered.valueOf(capturedPatient
				.getAttribute(whoRegisteredType).getValue()));
	}

	public void testStopPregnancyProgram() {

		String pregnancyProgram1 = "Weekly Pregnancy Message Program", pregnancyProgram2 = "Weekly Info Pregnancy Message Program";

		User nurse = new User(3);
		Integer patientId = 2;
		Patient patient = new Patient(patientId);

		Capture<MessageProgramEnrollment> enrollmentCapture1 = new Capture<MessageProgramEnrollment>();
		Capture<MessageProgramEnrollment> enrollmentCapture2 = new Capture<MessageProgramEnrollment>();

		expect(contextService.getMotechService()).andReturn(motechService)
				.atLeastOnce();

		expect(
				motechService.getActiveMessageProgramEnrollment(patientId,
						pregnancyProgram1)).andReturn(
				new MessageProgramEnrollment());
		expect(
				motechService
						.saveMessageProgramEnrollment(capture(enrollmentCapture1)))
				.andReturn(new MessageProgramEnrollment());
		expect(
				motechService.getActiveMessageProgramEnrollment(patientId,
						pregnancyProgram2)).andReturn(
				new MessageProgramEnrollment());
		expect(
				motechService
						.saveMessageProgramEnrollment(capture(enrollmentCapture2)))
				.andReturn(new MessageProgramEnrollment());

		replay(contextService, motechService);

		regBean.stopPregnancyProgram(nurse, patient);

		verify(contextService, motechService);

		MessageProgramEnrollment enrollment1 = enrollmentCapture1.getValue();
		assertNotNull("Enrollment end date must be set", enrollment1
				.getEndDate());
		MessageProgramEnrollment enrollment2 = enrollmentCapture2.getValue();
		assertNotNull("Enrollment end date must be set", enrollment2
				.getEndDate());
	}

	public void testRegisterMaternalVisit() {

		String nPhone = "12078888888", serialId = "478df-389489";
		Date date = new Date();
		Boolean tetanus = true, ipt = true, itn = true, onARV = true, prePMTCT = true, testPMTCT = true, postPMTCT = true;
		Double hemo = 12.3;
		Integer visit = 1;

		Location locationObj = new Location(1);
		locationObj.setName("Test Location");

		User nurse = new User(1);
		nurse.addAttribute(new PersonAttribute(primaryPhoneAttributeType,
				nPhone));
		nurse.addAttribute(new PersonAttribute(clinicAttributeType, locationObj
				.getLocationId().toString()));

		Patient patient = new Patient(2);
		patient.addIdentifier(new PatientIdentifier(serialId, ghanaIdType,
				locationObj));
		List<Patient> patients = new ArrayList<Patient>();
		patients.add(patient);

		Capture<List<PatientIdentifierType>> typeList = new Capture<List<PatientIdentifierType>>();
		Capture<Encounter> encounterCap = new Capture<Encounter>();
		Capture<Obs> tetanusObsCap = new Capture<Obs>();
		Capture<Obs> iptObsCap = new Capture<Obs>();
		Capture<Obs> itnObsCap = new Capture<Obs>();
		Capture<Obs> visitNumberObsCap = new Capture<Obs>();
		Capture<Obs> arvObsCap = new Capture<Obs>();
		Capture<Obs> preObsCap = new Capture<Obs>();
		Capture<Obs> testObsCap = new Capture<Obs>();
		Capture<Obs> postObsCap = new Capture<Obs>();
		Capture<Obs> hemoglobinObsCap = new Capture<Obs>();

		expect(contextService.getPatientService()).andReturn(patientService)
				.atLeastOnce();
		expect(contextService.getMotechService()).andReturn(motechService)
				.atLeastOnce();
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(contextService.getLocationService()).andReturn(locationService);
		expect(contextService.getEncounterService())
				.andReturn(encounterService).atLeastOnce();
		expect(contextService.getObsService()).andReturn(obsService);
		expect(contextService.getConceptService()).andReturn(conceptService)
				.atLeastOnce();
		expect(contextService.getUserService()).andReturn(userService);

		expect(
				patientService
						.getPatientIdentifierTypeByName(MotechConstants.PATIENT_IDENTIFIER_GHANA_CLINIC_ID))
				.andReturn(ghanaIdType);
		expect(
				patientService.getPatients(same((String) null), eq(serialId),
						capture(typeList), eq(true))).andReturn(patients);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(
				motechService.getUserIdsByPersonAttribute(
						primaryPhoneAttributeType, nPhone)).andReturn(
				new ArrayList<Integer>(Arrays.asList(nurse.getUserId())));
		expect(userService.getUser(nurse.getUserId())).andReturn(nurse);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_HEALTH_CENTER))
				.andReturn(clinicAttributeType);
		expect(locationService.getLocation(1)).andReturn(locationObj);
		expect(
				encounterService
						.getEncounterType(MotechConstants.ENCOUNTER_TYPE_MATERNALVISIT))
				.andReturn(matVisitType);
		expect(encounterService.saveEncounter(capture(encounterCap)))
				.andReturn(new Encounter());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_IMMUNIZATIONS_ORDERED))
				.andReturn(immunizationConcept);
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_TETANUS_BOOSTER))
				.andReturn(tetanusConcept);
		expect(obsService.saveObs(capture(tetanusObsCap), (String) anyObject()))
				.andReturn(new Obs());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_IMMUNIZATIONS_ORDERED))
				.andReturn(immunizationConcept);
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_INTERMITTENT_PREVENTATIVE_TREATMENT))
				.andReturn(iptConcept);
		expect(obsService.saveObs(capture(iptObsCap), (String) anyObject()))
				.andReturn(new Obs());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_INSECTICIDE_TREATED_NET_USAGE))
				.andReturn(itnConcept);
		expect(obsService.saveObs(capture(itnObsCap), (String) anyObject()))
				.andReturn(new Obs());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_PREGNANCY_VISIT_NUMBER))
				.andReturn(visitNumConcept);
		expect(
				obsService.saveObs(capture(visitNumberObsCap),
						(String) anyObject())).andReturn(new Obs());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_ANTIRETROVIRAL_USE_DURING_PREGNANCY))
				.andReturn(arvConcept);
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_ON_ANTIRETROVIRAL_THERAPY))
				.andReturn(onArvConcept);
		expect(obsService.saveObs(capture(arvObsCap), (String) anyObject()))
				.andReturn(new Obs());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_PRE_PREVENTING_MATERNAL_TO_CHILD_TRANSMISSION))
				.andReturn(prePMTCTConcept);
		expect(obsService.saveObs(capture(preObsCap), (String) anyObject()))
				.andReturn(new Obs());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_TEST_PREVENTING_MATERNAL_TO_CHILD_TRANSMISSION))
				.andReturn(testPMTCTConcept);
		expect(obsService.saveObs(capture(testObsCap), (String) anyObject()))
				.andReturn(new Obs());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_POST_PREVENTING_MATERNAL_TO_CHILD_TRANSMISSION))
				.andReturn(postPMTCTConcept);
		expect(obsService.saveObs(capture(postObsCap), (String) anyObject()))
				.andReturn(new Obs());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_HEMOGLOBIN_AT_36_WEEKS))
				.andReturn(hemo36Concept);
		expect(
				obsService.saveObs(capture(hemoglobinObsCap),
						(String) anyObject())).andReturn(new Obs());

		replay(contextService, patientService, motechService, personService,
				locationService, encounterService, obsService, conceptService,
				userService);

		regBean.recordMaternalVisit(nPhone, date, serialId, tetanus, ipt, itn,
				visit, onARV, prePMTCT, testPMTCT, postPMTCT, hemo);

		verify(contextService, patientService, motechService, personService,
				locationService, encounterService, obsService, conceptService,
				userService);

		Encounter e = encounterCap.getValue();
		assertEquals(1, typeList.getValue().size());
		assertTrue(typeList.getValue().contains(ghanaIdType));
		assertEquals(nPhone, e.getProvider().getAttribute(
				primaryPhoneAttributeType).getValue());
		assertEquals(serialId, e.getPatient().getPatientIdentifier()
				.getIdentifier());
		assertEquals(date, e.getEncounterDatetime());

		Obs tetanusObs = tetanusObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), tetanusObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), tetanusObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), tetanusObs.getLocation());
		assertEquals(immunizationConcept, tetanusObs.getConcept());
		assertEquals(tetanusConcept, tetanusObs.getValueCoded());

		Obs iptObs = iptObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), iptObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), iptObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), iptObs.getLocation());
		assertEquals(immunizationConcept, iptObs.getConcept());
		assertEquals(iptConcept, iptObs.getValueCoded());

		Obs itnObs = itnObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), itnObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), itnObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), itnObs.getLocation());
		assertEquals(itnConcept, itnObs.getConcept());
		assertEquals(Boolean.TRUE, itnObs.getValueAsBoolean());

		Obs visitNumberObs = visitNumberObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), visitNumberObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), visitNumberObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), visitNumberObs.getLocation());
		assertEquals(visitNumConcept, visitNumberObs.getConcept());
		assertEquals(Double.valueOf(visit), visitNumberObs.getValueNumeric());

		Obs arvObs = arvObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), arvObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), arvObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), arvObs.getLocation());
		assertEquals(arvConcept, arvObs.getConcept());
		assertEquals(onArvConcept, arvObs.getValueCoded());

		Obs preObs = preObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), preObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), preObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), preObs.getLocation());
		assertEquals(prePMTCTConcept, preObs.getConcept());
		assertEquals(Boolean.TRUE, preObs.getValueAsBoolean());

		Obs testObs = testObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), testObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), testObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), testObs.getLocation());
		assertEquals(testPMTCTConcept, testObs.getConcept());
		assertEquals(Boolean.TRUE, testObs.getValueAsBoolean());

		Obs postObs = postObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), postObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), postObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), postObs.getLocation());
		assertEquals(postPMTCTConcept, postObs.getConcept());
		assertEquals(Boolean.TRUE, postObs.getValueAsBoolean());

		Obs hemoglobinObs = hemoglobinObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), hemoglobinObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), hemoglobinObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), hemoglobinObs.getLocation());
		assertEquals(hemo36Concept, hemoglobinObs.getConcept());
		assertEquals(hemo, hemoglobinObs.getValueNumeric());
	}

	public void testRegisterMaternalVisitNoObs() {

		String nPhone = "12078888888", serialId = "478df-389489";
		Date date = new Date();
		Boolean tetanus = false, ipt = false, itn = false, onARV = false, prePMTCT = false, testPMTCT = false, postPMTCT = false;
		Double hemo = 12.3;
		Integer visit = 1;

		Location locationObj = new Location(1);
		locationObj.setName("Test Location");

		User nurse = new User(1);
		nurse.addAttribute(new PersonAttribute(primaryPhoneAttributeType,
				nPhone));
		nurse.addAttribute(new PersonAttribute(clinicAttributeType, locationObj
				.getLocationId().toString()));

		Patient patient = new Patient(2);
		patient.addIdentifier(new PatientIdentifier(serialId, ghanaIdType,
				locationObj));
		List<Patient> patients = new ArrayList<Patient>();
		patients.add(patient);

		Capture<List<PatientIdentifierType>> typeList = new Capture<List<PatientIdentifierType>>();
		Capture<Encounter> encounterCap = new Capture<Encounter>();
		Capture<Obs> visitNumberObsCap = new Capture<Obs>();
		Capture<Obs> hemoglobinObsCap = new Capture<Obs>();

		expect(contextService.getPatientService()).andReturn(patientService)
				.atLeastOnce();
		expect(contextService.getMotechService()).andReturn(motechService)
				.atLeastOnce();
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(contextService.getLocationService()).andReturn(locationService);
		expect(contextService.getEncounterService())
				.andReturn(encounterService).atLeastOnce();
		expect(contextService.getObsService()).andReturn(obsService);
		expect(contextService.getConceptService()).andReturn(conceptService)
				.atLeastOnce();
		expect(contextService.getUserService()).andReturn(userService);

		expect(
				patientService
						.getPatientIdentifierTypeByName(MotechConstants.PATIENT_IDENTIFIER_GHANA_CLINIC_ID))
				.andReturn(ghanaIdType);
		expect(
				patientService.getPatients(same((String) null), eq(serialId),
						capture(typeList), eq(true))).andReturn(patients);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(
				motechService.getUserIdsByPersonAttribute(
						primaryPhoneAttributeType, nPhone)).andReturn(
				new ArrayList<Integer>(Arrays.asList(nurse.getUserId())));
		expect(userService.getUser(nurse.getUserId())).andReturn(nurse);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_HEALTH_CENTER))
				.andReturn(clinicAttributeType);
		expect(locationService.getLocation(1)).andReturn(locationObj);
		expect(
				encounterService
						.getEncounterType(MotechConstants.ENCOUNTER_TYPE_MATERNALVISIT))
				.andReturn(matVisitType);
		expect(encounterService.saveEncounter(capture(encounterCap)))
				.andReturn(new Encounter());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_PREGNANCY_VISIT_NUMBER))
				.andReturn(visitNumConcept);
		expect(
				obsService.saveObs(capture(visitNumberObsCap),
						(String) anyObject())).andReturn(new Obs());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_HEMOGLOBIN_AT_36_WEEKS))
				.andReturn(hemo36Concept);
		expect(
				obsService.saveObs(capture(hemoglobinObsCap),
						(String) anyObject())).andReturn(new Obs());

		replay(contextService, patientService, motechService, personService,
				locationService, encounterService, obsService, conceptService,
				userService);

		regBean.recordMaternalVisit(nPhone, date, serialId, tetanus, ipt, itn,
				visit, onARV, prePMTCT, testPMTCT, postPMTCT, hemo);

		verify(contextService, patientService, motechService, personService,
				locationService, encounterService, obsService, conceptService,
				userService);

		Encounter e = encounterCap.getValue();
		assertEquals(1, typeList.getValue().size());
		assertTrue(typeList.getValue().contains(ghanaIdType));
		assertEquals(nPhone, e.getProvider().getAttribute(
				primaryPhoneAttributeType).getValue());
		assertEquals(serialId, e.getPatient().getPatientIdentifier()
				.getIdentifier());
		assertEquals(date, e.getEncounterDatetime());

		Obs visitNumberObs = visitNumberObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), visitNumberObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), visitNumberObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), visitNumberObs.getLocation());
		assertEquals(visitNumConcept, visitNumberObs.getConcept());
		assertEquals(Double.valueOf(visit), visitNumberObs.getValueNumeric());

		Obs hemoglobinObs = hemoglobinObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), hemoglobinObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), hemoglobinObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), hemoglobinObs.getLocation());
		assertEquals(hemo36Concept, hemoglobinObs.getConcept());
		assertEquals(hemo, hemoglobinObs.getValueNumeric());
	}

	public void testRegisterPregnancy() {
		String nPhone = "15555555555", serialId = "AFGHSFG";
		Date date = new Date();
		Integer parity = 1;
		Double hemo = 3893.1;

		Location locationObj = new Location(1);
		locationObj.setName("Test Location");

		User nurse = new User(1);
		nurse.addAttribute(new PersonAttribute(primaryPhoneAttributeType,
				nPhone));
		nurse.addAttribute(new PersonAttribute(clinicAttributeType, locationObj
				.getLocationId().toString()));

		Patient patient = new Patient(2);
		patient.addIdentifier(new PatientIdentifier(serialId, ghanaIdType,
				locationObj));
		List<Patient> patients = new ArrayList<Patient>();
		patients.add(patient);

		Capture<Encounter> encounterCap = new Capture<Encounter>();
		Capture<List<PatientIdentifierType>> typeListCap = new Capture<List<PatientIdentifierType>>();
		Capture<Obs> pregStatusObsCap = new Capture<Obs>();
		Capture<Obs> dueDateObsCap = new Capture<Obs>();
		Capture<Obs> parityObsCap = new Capture<Obs>();
		Capture<Obs> hemoglobinObsCap = new Capture<Obs>();

		expect(contextService.getPatientService()).andReturn(patientService)
				.atLeastOnce();
		expect(contextService.getMotechService()).andReturn(motechService)
				.atLeastOnce();
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(contextService.getLocationService()).andReturn(locationService);
		expect(contextService.getEncounterService())
				.andReturn(encounterService).atLeastOnce();
		expect(contextService.getObsService()).andReturn(obsService);
		expect(contextService.getConceptService()).andReturn(conceptService)
				.atLeastOnce();
		expect(contextService.getUserService()).andReturn(userService);

		expect(
				patientService
						.getPatientIdentifierTypeByName(MotechConstants.PATIENT_IDENTIFIER_GHANA_CLINIC_ID))
				.andReturn(ghanaIdType);
		expect(
				patientService.getPatients(same((String) null), eq(serialId),
						capture(typeListCap), eq(true))).andReturn(patients);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(
				motechService.getUserIdsByPersonAttribute(
						primaryPhoneAttributeType, nPhone)).andReturn(
				new ArrayList<Integer>(Arrays.asList(nurse.getUserId())));
		expect(userService.getUser(nurse.getUserId())).andReturn(nurse);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_HEALTH_CENTER))
				.andReturn(clinicAttributeType);
		expect(locationService.getLocation(1)).andReturn(locationObj);
		expect(
				encounterService
						.getEncounterType(MotechConstants.ENCOUNTER_TYPE_PREGNANCYVISIT))
				.andReturn(pregVisitType);
		expect(encounterService.saveEncounter(capture(encounterCap)))
				.andReturn(new Encounter());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_PREGNANCY_STATUS))
				.andReturn(pregStatusConcept);
		expect(
				obsService.saveObs(capture(pregStatusObsCap),
						(String) anyObject())).andReturn(new Obs());
		expect(
				conceptService
						.getConcept(MotechConstants.CONCEPT_ESTIMATED_DATE_OF_CONFINEMENT))
				.andReturn(dateConfConcept);
		expect(obsService.saveObs(capture(dueDateObsCap), (String) anyObject()))
				.andReturn(new Obs());
		expect(conceptService.getConcept(MotechConstants.CONCEPT_PARITY))
				.andReturn(parityConcept);
		expect(obsService.saveObs(capture(parityObsCap), (String) anyObject()))
				.andReturn(new Obs());
		expect(conceptService.getConcept(MotechConstants.CONCEPT_HEMOGLOBIN))
				.andReturn(hemoConcept);
		expect(
				obsService.saveObs(capture(hemoglobinObsCap),
						(String) anyObject())).andReturn(new Obs());

		replay(contextService, patientService, motechService, personService,
				locationService, encounterService, obsService, conceptService,
				userService);

		regBean.registerPregnancy(nPhone, date, serialId, date, parity, hemo);

		verify(contextService, patientService, motechService, personService,
				locationService, encounterService, obsService, conceptService,
				userService);

		Encounter e = encounterCap.getValue();
		assertEquals(nPhone, e.getProvider().getAttribute(
				primaryPhoneAttributeType).getValue());
		assertEquals(serialId, e.getPatient().getPatientIdentifier()
				.getIdentifier());
		assertEquals(date, e.getEncounterDatetime());

		Obs pregStatusObs = pregStatusObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), pregStatusObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), pregStatusObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), pregStatusObs.getLocation());
		assertEquals(pregStatusConcept, pregStatusObs.getConcept());
		assertEquals(Boolean.TRUE, pregStatusObs.getValueAsBoolean());

		Obs dueDateObs = dueDateObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), dueDateObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), dueDateObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), dueDateObs.getLocation());
		assertEquals(dateConfConcept, dueDateObs.getConcept());
		assertEquals(date, dueDateObs.getValueDatetime());

		Obs parityObs = parityObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), parityObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), parityObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), parityObs.getLocation());
		assertEquals(parityConcept, parityObs.getConcept());
		assertEquals(Double.valueOf(parity), parityObs.getValueNumeric());

		Obs hemoglobinObs = hemoglobinObsCap.getValue();
		assertEquals(e.getEncounterDatetime(), hemoglobinObs.getObsDatetime());
		assertEquals(e.getPatient().getPatientId(), hemoglobinObs.getPerson()
				.getPersonId());
		assertEquals(e.getLocation(), hemoglobinObs.getLocation());
		assertEquals(hemoConcept, hemoglobinObs.getConcept());
		assertEquals(hemo, hemoglobinObs.getValueNumeric());
	}

	public void testLog() {
		LogType type = LogType.FAILURE;
		String message = "A simple message";
		Date beforeCall = new Date();

		Capture<Log> logCap = new Capture<Log>();

		expect(contextService.getMotechService()).andReturn(motechService);

		expect(motechService.saveLog(capture(logCap))).andReturn(new Log());

		replay(contextService, motechService);

		regBean.log(type, message);

		verify(contextService, motechService);

		Log log = logCap.getValue();
		Date logDate = log.getDate();
		Date afterCall = new Date();

		assertEquals(type, log.getType());
		assertEquals(message, log.getMessage());
		assertNotNull("Date is null", logDate);
		assertFalse("Date not between invocation and return", logDate
				.before(beforeCall)
				|| logDate.after(afterCall));
	}

	public void testLogMessageTrim() {
		LogType type = LogType.SUCCESS;
		// String length trimmed from 306 to 255 characters (max length for log
		// message)
		String originalMessage = "This message is just too long and will be trimmed. "
				+ "This message is just too long and will be trimmed. "
				+ "This message is just too long and will be trimmed. "
				+ "This message is just too long and will be trimmed. "
				+ "This message is just too long and will be trimmed. "
				+ "This message is just too long and will be trimmed. ";
		String trimmedMessage = "This message is just too long and will be trimmed. "
				+ "This message is just too long and will be trimmed. "
				+ "This message is just too long and will be trimmed. "
				+ "This message is just too long and will be trimmed. "
				+ "This message is just too long and will be trimmed. ";
		Date beforeCall = new Date();

		Capture<Log> logCap = new Capture<Log>();

		expect(contextService.getMotechService()).andReturn(motechService);

		expect(motechService.saveLog(capture(logCap))).andReturn(new Log());

		replay(contextService, motechService);

		regBean.log(type, originalMessage);

		verify(contextService, motechService);

		Log log = logCap.getValue();
		Date logDate = log.getDate();
		Date afterCall = new Date();

		assertEquals(type, log.getType());
		assertEquals(trimmedMessage, log.getMessage());
		assertNotNull("Date is null", logDate);
		assertFalse("Date not between invocation and return", logDate
				.before(beforeCall)
				|| logDate.after(afterCall));
	}

	public void testSetMessageStatusSuccessMessageFoundNotTroubled() {
		String messageId = "12345678-1234-1234-1234-123456789012";
		Boolean success = true;

		Integer recipientId = 2;
		String phoneNumber = "1234567890";
		Person recipient = new Person();
		recipient.addAttribute(new PersonAttribute(primaryPhoneAttributeType,
				phoneNumber));
		TroubledPhone troubledPhone = null;
		Message message = new Message();
		ScheduledMessage scheduledMessage = new ScheduledMessage();
		scheduledMessage.setRecipientId(recipientId);
		message.setSchedule(scheduledMessage);

		Capture<Message> messageCap = new Capture<Message>();

		expect(contextService.getMotechService()).andReturn(motechService);
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(motechService.getMessage(messageId)).andReturn(message);
		expect(personService.getPerson(recipientId)).andReturn(recipient);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(motechService.getTroubledPhone(phoneNumber)).andReturn(
				troubledPhone);
		expect(motechService.saveMessage(capture(messageCap))).andReturn(
				message);

		replay(contextService, motechService, personService);

		regBean.setMessageStatus(messageId, success);

		verify(contextService, motechService, personService);

		Message capturedMessage = messageCap.getValue();
		assertEquals(MessageStatus.DELIVERED, capturedMessage
				.getAttemptStatus());
	}

	public void testSetMessageStatusSuccessMessageFoundTroubled() {
		String messageId = "12345678-1234-1234-1234-123456789012";
		Boolean success = true;

		Integer recipientId = 2;
		String phoneNumber = "1234567890";
		Person recipient = new Person();
		recipient.addAttribute(new PersonAttribute(primaryPhoneAttributeType,
				phoneNumber));
		TroubledPhone troubledPhone = new TroubledPhone();
		Message message = new Message();
		ScheduledMessage scheduledMessage = new ScheduledMessage();
		scheduledMessage.setRecipientId(recipientId);
		message.setSchedule(scheduledMessage);

		Capture<Message> messageCap = new Capture<Message>();

		expect(contextService.getMotechService()).andReturn(motechService);
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(motechService.getMessage(messageId)).andReturn(message);
		expect(personService.getPerson(recipientId)).andReturn(recipient);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(motechService.getTroubledPhone(phoneNumber)).andReturn(
				troubledPhone);
		motechService.removeTroubledPhone(phoneNumber);
		expect(motechService.saveMessage(capture(messageCap))).andReturn(
				message);

		replay(contextService, motechService, personService);

		regBean.setMessageStatus(messageId, success);

		verify(contextService, motechService, personService);

		Message capturedMessage = messageCap.getValue();
		assertEquals(MessageStatus.DELIVERED, capturedMessage
				.getAttemptStatus());
	}

	public void testSetMessageStatusFailureMessageFoundNotTroubled() {
		String messageId = "12345678-1234-1234-1234-123456789012";
		Boolean success = false;

		Integer recipientId = 2;
		String phoneNumber = "1234567890";
		Person recipient = new Person();
		recipient.addAttribute(new PersonAttribute(primaryPhoneAttributeType,
				phoneNumber));
		TroubledPhone troubledPhone = null;
		Message message = new Message();
		ScheduledMessage scheduledMessage = new ScheduledMessage();
		scheduledMessage.setRecipientId(recipientId);
		message.setSchedule(scheduledMessage);

		Capture<Message> messageCap = new Capture<Message>();

		expect(contextService.getMotechService()).andReturn(motechService);
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(motechService.getMessage(messageId)).andReturn(message);
		expect(personService.getPerson(recipientId)).andReturn(recipient);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(motechService.getTroubledPhone(phoneNumber)).andReturn(
				troubledPhone);
		motechService.addTroubledPhone(phoneNumber);
		expect(motechService.saveMessage(capture(messageCap))).andReturn(
				message);

		replay(contextService, motechService, personService);

		regBean.setMessageStatus(messageId, success);

		verify(contextService, motechService, personService);

		Message capturedMessage = messageCap.getValue();
		assertEquals(MessageStatus.ATTEMPT_FAIL, capturedMessage
				.getAttemptStatus());
	}

	public void testSetMessageStatusFailureMessageFoundTroubled() {
		String messageId = "12345678-1234-1234-1234-123456789012";
		Boolean success = false;

		Integer recipientId = 2;
		String phoneNumber = "1234567890";
		Person recipient = new Person();
		recipient.addAttribute(new PersonAttribute(primaryPhoneAttributeType,
				phoneNumber));
		Integer previousFailures = 1;
		TroubledPhone troubledPhone = new TroubledPhone();
		troubledPhone.setSendFailures(previousFailures);
		Message message = new Message();
		ScheduledMessage scheduledMessage = new ScheduledMessage();
		scheduledMessage.setRecipientId(recipientId);
		message.setSchedule(scheduledMessage);

		Capture<TroubledPhone> troubledPhoneCap = new Capture<TroubledPhone>();
		Capture<Message> messageCap = new Capture<Message>();

		expect(contextService.getMotechService()).andReturn(motechService);
		expect(contextService.getPersonService()).andReturn(personService)
				.atLeastOnce();
		expect(motechService.getMessage(messageId)).andReturn(message);
		expect(personService.getPerson(recipientId)).andReturn(recipient);
		expect(
				personService
						.getPersonAttributeTypeByName(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER))
				.andReturn(primaryPhoneAttributeType);
		expect(motechService.getTroubledPhone(phoneNumber)).andReturn(
				troubledPhone);
		motechService.saveTroubledPhone(capture(troubledPhoneCap));
		expect(motechService.saveMessage(capture(messageCap))).andReturn(
				message);

		replay(contextService, motechService, personService);

		regBean.setMessageStatus(messageId, success);

		verify(contextService, motechService, personService);

		Message capturedMessage = messageCap.getValue();
		assertEquals(MessageStatus.ATTEMPT_FAIL, capturedMessage
				.getAttemptStatus());

		Integer expectedFailures = 2;
		TroubledPhone capturedTroubledPhone = troubledPhoneCap.getValue();
		assertEquals(expectedFailures, capturedTroubledPhone.getSendFailures());
	}

	public void testSetMessageStatusMessageNotFound() {
		String messageId = "12345678-1234-1234-1234-123456789012";
		Boolean success = true;

		expect(contextService.getMotechService()).andReturn(motechService);
		expect(contextService.getPersonService()).andReturn(personService);
		expect(motechService.getMessage(messageId)).andReturn(null);

		replay(contextService, motechService, personService);

		try {
			regBean.setMessageStatus(messageId, success);
			fail("Expected org.motech.messaging.MessageNotFoundException: none thrown");
		} catch (MessageNotFoundException e) {

		} catch (Exception e) {
			fail("Expected org.motech.messaging.MessageNotFoundException: other thrown");
		}

		verify(contextService, motechService, personService);
	}
}
