package org.motechproject.server.messaging.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.motechproject.server.messaging.MessageScheduler;
import org.motechproject.server.model.MessageProgramEnrollment;
import org.motechproject.server.svc.RegistrarBean;

public class MessageSchedulerImpl implements MessageScheduler {

	private static Log log = LogFactory.getLog(MessageSchedulerImpl.class);

	private RegistrarBean registrarBean;
	private Boolean userPreferenceBased = false;

	public RegistrarBean getRegistrarBean() {
		return registrarBean;
	}

	public void setRegistrarBean(RegistrarBean registrarBean) {
		this.registrarBean = registrarBean;
	}

	public Boolean getUserPreferenceBased() {
		return userPreferenceBased;
	}

	public void setUserPreferenceBased(Boolean userPreferenceBased) {
		this.userPreferenceBased = userPreferenceBased;
	}

	public void scheduleMessage(String messageKey,
			MessageProgramEnrollment enrollment, Date messageDate) {

		log.debug("scheduling message");

		registrarBean.scheduleMessage(messageKey, enrollment, messageDate,
				userPreferenceBased);
	}

}