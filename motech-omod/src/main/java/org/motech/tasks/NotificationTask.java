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
package org.motech.tasks;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.motech.messaging.Message;
import org.motech.messaging.MessageStatus;
import org.motech.model.TroubledPhone;
import org.motech.openmrs.module.MotechService;
import org.motech.util.MotechConstants;
import org.motechproject.ws.ContactNumberType;
import org.motechproject.ws.DeliveryTime;
import org.motechproject.ws.LogType;
import org.motechproject.ws.MediaType;
import org.motechproject.ws.NameValuePair;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.OpenmrsConstants;

/**
 * Defines a task implementation that OpenMRS can execute using the built-in
 * task scheduler. This is how periodic notifications are handled for the
 * OpenMRS motech server implementation. It periodically runs, looks up stored
 * Message objects and constructs and sends messages to patients and nurses if
 * required.
 */
public class NotificationTask extends AbstractTask {

	private static Log log = LogFactory.getLog(NotificationTask.class);

	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {

		String timeOffsetString = this.taskDefinition
				.getProperty(MotechConstants.TASK_PROPERTY_TIME_OFFSET);
		Boolean sendImmediate = Boolean.valueOf(taskDefinition
				.getProperty(MotechConstants.TASK_PROPERTY_SEND_IMMEDIATE));
		Long timeOffset = 0L;
		if (timeOffsetString != null) {
			timeOffset = Long.valueOf(timeOffsetString);
		}

		Date startDate = new Date(System.currentTimeMillis()
				+ (timeOffset * 1000));
		Date endDate = new Date(System.currentTimeMillis()
				+ (this.taskDefinition.getRepeatInterval() * 1000)
				+ (timeOffset * 1000));

		try {
			Context.openSession();
			Context
					.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES);
			Context
					.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES);
			Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
			Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PERSONS);
			Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_CONCEPTS);
			Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_OBS);

			List<Message> shouldAttemptMessages = Context.getService(
					MotechService.class).getMessages(startDate, endDate,
					MessageStatus.SHOULD_ATTEMPT);

			if (log.isDebugEnabled()) {
				log
						.debug("Notification Task executed, Should Attempt Messages found: "
								+ shouldAttemptMessages.size());
			}

			if (shouldAttemptMessages.size() > 0) {
				Date notificationDate = new Date();
				PersonAttributeType phoneNumberType = Context
						.getPersonService().getPersonAttributeTypeByName(
								MotechConstants.PERSON_ATTRIBUTE_PHONE_NUMBER);
				PersonAttributeType phoneType = Context.getPersonService()
						.getPersonAttributeTypeByName(
								MotechConstants.PERSON_ATTRIBUTE_PHONE_TYPE);
				PersonAttributeType languageType = Context.getPersonService()
						.getPersonAttributeTypeByName(
								MotechConstants.PERSON_ATTRIBUTE_LANGUAGE);
				PersonAttributeType mediaAttrType = Context.getPersonService()
						.getPersonAttributeTypeByName(
								MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);

				for (Message shouldAttemptMessage : shouldAttemptMessages) {

					org.motech.model.Log motechLog = new org.motech.model.Log();
					motechLog.setDate(notificationDate);

					String messageId = shouldAttemptMessage.getPublicId();
					Long notificationType = shouldAttemptMessage.getSchedule()
							.getMessage().getPublicId();
					Integer recipientId = shouldAttemptMessage.getSchedule()
							.getRecipientId();
					Patient patient = Context.getPatientService().getPatient(
							recipientId);
					User nurse = Context.getUserService().getUser(recipientId);

					if (patient != null) {
						String patientPhone = patient.getAttribute(
								phoneNumberType).getValue();
						String phoneTypeString = patient
								.getAttribute(phoneType).getValue();
						String languageCode = patient
								.getAttribute(languageType).getValue();
						String mediaTypeString = patient.getAttribute(
								mediaAttrType).getValue();
						ContactNumberType patientNumberType = ContactNumberType
								.valueOf(phoneTypeString);

						NameValuePair[] personalInfo = shouldAttemptMessage
								.getSchedule().getMessage()
								.getNameValueContent(recipientId);
						MediaType mediaType = MediaType
								.valueOf(mediaTypeString);
						Date messageStartDate = null;
						Date messageEndDate = null;

						if (!sendImmediate) {

							messageStartDate = shouldAttemptMessage
									.getAttemptDate();

							Person recipient = Context.getPersonService()
									.getPerson(recipientId);
							PersonAttributeType deliveryTimeType = Context
									.getPersonService()
									.getPersonAttributeTypeByName(
											MotechConstants.PERSON_ATTRIBUTE_DELIVERY_TIME);
							PersonAttribute deliveryTimeAttr = recipient
									.getAttribute(deliveryTimeType);
							String deliveryTimeString = deliveryTimeAttr
									.getValue();
							DeliveryTime deliveryTime = DeliveryTime.ANYTIME;
							if (deliveryTimeString != null) {
								deliveryTime = DeliveryTime
										.valueOf(deliveryTimeString);
							}

							Calendar calendar = Calendar.getInstance();
							calendar.setTime(messageStartDate);
							switch (deliveryTime) {
							case MORNING:
								calendar.set(Calendar.HOUR_OF_DAY, 12);
								break;
							case AFTERNOON:
								calendar.set(Calendar.HOUR_OF_DAY, 17);
								break;
							case EVENING:
								calendar.set(Calendar.HOUR_OF_DAY, 21);
								break;
							default:
								calendar.set(Calendar.HOUR_OF_DAY, 21);
								break;
							}
							calendar.set(Calendar.MINUTE, 0);
							calendar.set(Calendar.SECOND, 0);
							messageEndDate = calendar.getTime();
						}

						// Cancel message if patient phone is considered
						// troubled
						TroubledPhone troubledPhone = Context.getService(
								MotechService.class).getTroubledPhone(
								patientPhone);
						Integer maxFailures = Integer
								.parseInt(Context
										.getAdministrationService()
										.getGlobalProperty(
												MotechConstants.GLOBAL_PROPERTY_TROUBLED_PHONE));
						if (troubledPhone != null
								&& troubledPhone.getSendFailures() >= maxFailures) {
							motechLog
									.setMessage("Attempt to send to Troubled Phone, Patient Phone: "
											+ patientPhone
											+ ", Message cancelled: "
											+ notificationType);
							motechLog.setType(LogType.FAILURE);

							shouldAttemptMessage
									.setAttemptStatus(MessageStatus.CANCELLED);

						} else {
							motechLog
									.setMessage("Scheduled Message Notification, Patient Phone: "
											+ patientPhone
											+ ": "
											+ notificationType);

							try {
								Context.getService(MotechService.class)
										.getMobileService().sendPatientMessage(
												messageId, personalInfo,
												patientPhone,
												patientNumberType,
												languageCode, mediaType,
												notificationType,
												messageStartDate,
												messageEndDate);
								shouldAttemptMessage
										.setAttemptStatus(MessageStatus.ATTEMPT_PENDING);
								motechLog.setType(LogType.SUCCESS);
							} catch (Exception e) {
								log.error("Mobile patient message failure", e);
								shouldAttemptMessage
										.setAttemptStatus(MessageStatus.ATTEMPT_FAIL);
								motechLog.setType(LogType.FAILURE);
							}
						}

					} else if (nurse != null) {
						String nursePhone = nurse.getAttribute(phoneNumberType)
								.getValue();

						NameValuePair[] personalInfo = shouldAttemptMessage
								.getSchedule().getMessage()
								.getNameValueContent(recipientId);
						org.motechproject.ws.Patient[] patients = new org.motechproject.ws.Patient[0];
						String langCode = null;
						MediaType mediaType = null;
						Date messageStartDate = null;
						Date messageEndDate = null;

						// Cancel message if nurse phone is considered troubled
						TroubledPhone troubledPhone = Context.getService(
								MotechService.class).getTroubledPhone(
								nursePhone);
						Integer maxFailures = Integer
								.parseInt(Context
										.getAdministrationService()
										.getGlobalProperty(
												MotechConstants.GLOBAL_PROPERTY_TROUBLED_PHONE));
						if (troubledPhone != null
								&& troubledPhone.getSendFailures() >= maxFailures) {
							motechLog
									.setMessage("Attempt to send to Troubled Phone, Nurse Phone: "
											+ nursePhone
											+ ", Message cancelled: "
											+ notificationType);
							motechLog.setType(LogType.FAILURE);

							shouldAttemptMessage
									.setAttemptStatus(MessageStatus.CANCELLED);

						} else {
							motechLog
									.setMessage("Scheduled Message Notification, Nurse Phone: "
											+ nursePhone
											+ ": "
											+ notificationType);

							try {
								Context.getService(MotechService.class)
										.getMobileService().sendCHPSMessage(
												messageId, personalInfo,
												nursePhone, patients, langCode,
												mediaType, notificationType,
												messageStartDate,
												messageEndDate);

								shouldAttemptMessage
										.setAttemptStatus(MessageStatus.ATTEMPT_PENDING);
								motechLog.setType(LogType.SUCCESS);
							} catch (Exception e) {
								log.error("Mobile nurse message failure", e);
								shouldAttemptMessage
										.setAttemptStatus(MessageStatus.ATTEMPT_FAIL);
								motechLog.setType(LogType.FAILURE);
							}
						}

					}
					Context.getService(MotechService.class).saveLog(motechLog);

					Context.getService(MotechService.class).saveMessage(
							shouldAttemptMessage);
				}
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			Context
					.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES);
			Context
					.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PERSONS);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_CONCEPTS);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_OBS);
			Context.closeSession();
		}
	}

}
