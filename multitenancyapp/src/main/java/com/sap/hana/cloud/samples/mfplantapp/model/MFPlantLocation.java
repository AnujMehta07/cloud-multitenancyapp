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
 * Model object representing a single {@link MFPlantLocation} instance.
 * 
 * @version 0.1
 */
@Entity
@Table(name = "MFPLANTAPP_CITY")
@NamedQueries({@NamedQuery(name = "MFPlantLocations", query = "SELECT c FROM MFPlantLocation c"), 
	           @NamedQuery(name = "MFPlantLocationById", query = "SELECT c FROM MFPlantLocation c WHERE c.id = :id"),
	           @NamedQuery(name = "MFPlantLocationByOwner", query = "SELECT c FROM MFPlantLocation c WHERE c.owner = :owner")})

@XmlRootElement(name = "location")
@XmlAccessorType(XmlAccessType.FIELD)
public class MFPlantLocation extends BaseObject implements Serializable
{
	/**
	 * The <code>serialVersionUID</code> of the {@link MFPlantLocation} class.
	 */
	private static final long serialVersionUID = 1L;

	@Column(name="ID", length = 36, nullable=true)
	String id = null;
	
	@Column(name="NAME", length = 128, nullable=true)
	String name = null;
	
	@Column(name="COUNTRY", length = 2, nullable=true)
	String countryCode = null;
	
	@Column(name="OWNER", length = 36, nullable=true)
	String owner = null;

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}

	public String getCountryCode() 
	{
		return countryCode;
	}

	public void setCountryCode(String countryCode) 
	{
		this.countryCode = countryCode;
	}
}
