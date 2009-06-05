package org.motech.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "pregnancies")
public class Pregnancy {
	private Long id;
	private Date dueDate;
	private Mother mother;
	private Nurse nurse;
	private Integer parity;
	private Integer hemoglobin;
	private Date deliveryDate;
	private String birthOutcome;
	private Integer modeOfDelivery;
	private String deliveredBy;
	private String causeOfDeath;
	private Integer status;
	private Integer dischargedOrReferred;
	private Boolean adoptionPlanned;

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	@ManyToOne
	@JoinColumn(name = "mother_id")
	public Mother getMother() {
		return mother;
	}

	public void setMother(Mother mother) {
		this.mother = mother;
	}

	@ManyToOne
	@JoinColumn(name = "nurse_id")
	public Nurse getNurse() {
		return nurse;
	}

	public void setNurse(Nurse nurse) {
		this.nurse = nurse;
	}

	public Integer getParity() {
		return parity;
	}

	public void setParity(Integer parity) {
		this.parity = parity;
	}

	public Integer getHemoglobin() {
		return hemoglobin;
	}

	public void setHemoglobin(Integer hemoglobin) {
		this.hemoglobin = hemoglobin;
	}

	public Date getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public String getBirthOutcome() {
		return birthOutcome;
	}

	public void setBirthOutcome(String birthOutcome) {
		this.birthOutcome = birthOutcome;
	}

	public Integer getModeOfDelivery() {
		return modeOfDelivery;
	}

	public void setModeOfDelivery(Integer modeOfDelivery) {
		this.modeOfDelivery = modeOfDelivery;
	}

	public String getDeliveredBy() {
		return deliveredBy;
	}

	public void setDeliveredBy(String deliveredBy) {
		this.deliveredBy = deliveredBy;
	}

	public String getCauseOfDeath() {
		return causeOfDeath;
	}

	public void setCauseOfDeath(String causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getDischargedOrReferred() {
		return dischargedOrReferred;
	}

	public void setDischargedOrReferred(Integer dischargedOrReferred) {
		this.dischargedOrReferred = dischargedOrReferred;
	}

	public Boolean getAdoptionPlanned() {
		return adoptionPlanned;
	}

	public void setAdoptionPlanned(Boolean adoptionPlanned) {
		this.adoptionPlanned = adoptionPlanned;
	}

}