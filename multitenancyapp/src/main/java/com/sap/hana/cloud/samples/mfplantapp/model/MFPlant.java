package com.sap.hana.cloud.samples.mfplantapp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model object representing a single {@link MFPlant} instance.
 * 
 * @version 0.1
 */
@Entity
@Table(name = "MFPLANTAPP_CITY")
@NamedQueries({@NamedQuery(name = "MFPlants", query = "SELECT c FROM MFPlant c"), 
	           @NamedQuery(name = "MFPlantById", query = "SELECT c FROM MFPlant c WHERE c.id = :id")})

@XmlRootElement(name ="plantlist")
@XmlAccessorType(XmlAccessType.FIELD)
public class MFPlant extends BaseObject implements Serializable
{   /**
	 * The <code>serialVersionUID</code> of the {@link MFPlant} class.
	 */
	private static final long serialVersionUID = 1L;

	@Column(name="ID", length = 36, nullable=true)
	String id = null;
	
	@Column(name="CO", length = 10, nullable=true)
	String co = null;
	
	@Column(name="O3", length = 10, nullable=true)
	String o3 = null;
	
	@Column(name="PM10", length = 10, nullable=true)
	String pm10 = null;
	
	@Column(name="PM25", length = 10, nullable=true)
	String pm25 = null;
	
	@Column(name="SO2", length = 10, nullable=true)
	String so2 = null;
	
	@Column(name="NO2", length = 10, nullable=true)
	String no2 = null;
	
	public String getCo() {
		return co;
	}

	public void setCo(String co) {
		this.co = co;
	}

	public String getO3() {
		return o3;
	}

	public void setO3(String o3) {
		this.o3 = o3;
	}

	public String getPm10() {
		return pm10;
	}

	public void setPm10(String pm10) {
		this.pm10 = pm10;
	}

	public String getPm25() {
		return pm25;
	}

	public void setPm25(String pm25) {
		this.pm25 = pm25;
	}

	public String getSo2() {
		return so2;
	}

	public void setSo2(String so2) {
		this.so2 = so2;
	}

	public String getNo2() {
		return no2;
	}

	public void setNo2(String no2) {
		this.no2 = no2;
	}
		
	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	

}
