package com.mti.shop.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name = "Account")
public class Account implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "cpt_email", length = 1024)
	@NotEmpty
	@Email
	private String cptEmail;

	@Column(name = "cpt_name", length = 1024)
	private String cptName;

	@Column(name = "cpt_firstname", length = 1024)
	@NotEmpty
	@NotBlank
	private String cptFirstname;

	@Column(name = "cpt_postal_code", length = 12)
	private String cptPostalCode;

	@Column(name = "cpt_phone_num", length = 20)
	private String cptPhoneNum;

	@Column(name = "cpt_address_line1", length = 1024)
	private String cptAddressLine1;

	@Column(name = "cpt_address_line2", length = 1024)
	private String cptAddressLine2;

	@Column(name = "cpt_password")
	private String cptPassword;

	@Transient
	private String cptPasswordConfirm;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cnt_id")
	private Country country;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "type_id")
	private Type type;


	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "Roles", joinColumns = { @JoinColumn(name = "acc_id") }, inverseJoinColumns = { @JoinColumn(name = "type_id") })
	private Set<Type> roles;

	public String getCptPasswordConfirm() {
		return cptPasswordConfirm;
	}

	public void setCptPasswordConfirm(String cptPasswordConfirm) {
		this.cptPasswordConfirm = cptPasswordConfirm;
	}

	public Set<Type> getRoles() {
		return roles;
	}

	public void setRoles(Set<Type> roles) {
		this.roles = roles;
	}

	public String getCptPassword() {
		return cptPassword;
	}

	public void setCptPassword(String cptPassword) {
		this.cptPassword = cptPassword;
	}


	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCptEmail() {
		return cptEmail;
	}

	public void setCptEmail(String cptEmail) {
		this.cptEmail = cptEmail;
	}

	public String getCptName() {
		return cptName;
	}

	public void setCptName(String cptName) {
		this.cptName = cptName;
	}

	public String getCptFirstname() {
		return cptFirstname;
	}

	public void setCptFirstname(String cptFirstname) {
		this.cptFirstname = cptFirstname;
	}

	public String getCptPostalCode() {
		return cptPostalCode;
	}

	public void setCptPostalCode(String cptPostalCode) {
		this.cptPostalCode = cptPostalCode;
	}

	public String getCptPhoneNum() {
		return cptPhoneNum;
	}

	public void setCptPhoneNum(String cptPhoneNum) {
		this.cptPhoneNum = cptPhoneNum;
	}

	public String getCptAddressLine1() {
		return cptAddressLine1;
	}

	public void setCptAddressLine1(String cptAddressLine1) {
		this.cptAddressLine1 = cptAddressLine1;
	}

	public String getCptAddressLine2() {
		return cptAddressLine2;
	}

	public void setCptAddressLine2(String cptAddressLine2) {
		this.cptAddressLine2 = cptAddressLine2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result
				+ ((cptAddressLine1 == null) ? 0 : cptAddressLine1.hashCode());
		result = prime * result
				+ ((cptAddressLine2 == null) ? 0 : cptAddressLine2.hashCode());
		result = prime * result
				+ ((cptEmail == null) ? 0 : cptEmail.hashCode());
		result = prime * result
				+ ((cptFirstname == null) ? 0 : cptFirstname.hashCode());
		result = prime * result + ((cptName == null) ? 0 : cptName.hashCode());
		result = prime * result
				+ ((cptPassword == null) ? 0 : cptPassword.hashCode());
		result = prime
				* result
				+ ((cptPasswordConfirm == null) ? 0 : cptPasswordConfirm
						.hashCode());
		result = prime * result
				+ ((cptPhoneNum == null) ? 0 : cptPhoneNum.hashCode());
		result = prime * result
				+ ((cptPostalCode == null) ? 0 : cptPostalCode.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (cptAddressLine1 == null) {
			if (other.cptAddressLine1 != null)
				return false;
		} else if (!cptAddressLine1.equals(other.cptAddressLine1))
			return false;
		if (cptAddressLine2 == null) {
			if (other.cptAddressLine2 != null)
				return false;
		} else if (!cptAddressLine2.equals(other.cptAddressLine2))
			return false;
		if (cptEmail == null) {
			if (other.cptEmail != null)
				return false;
		} else if (!cptEmail.equals(other.cptEmail))
			return false;
		if (cptFirstname == null) {
			if (other.cptFirstname != null)
				return false;
		} else if (!cptFirstname.equals(other.cptFirstname))
			return false;
		if (cptName == null) {
			if (other.cptName != null)
				return false;
		} else if (!cptName.equals(other.cptName))
			return false;
		if (cptPassword == null) {
			if (other.cptPassword != null)
				return false;
		} else if (!cptPassword.equals(other.cptPassword))
			return false;
		if (cptPasswordConfirm == null) {
			if (other.cptPasswordConfirm != null)
				return false;
		} else if (!cptPasswordConfirm.equals(other.cptPasswordConfirm))
			return false;
		if (cptPhoneNum == null) {
			if (other.cptPhoneNum != null)
				return false;
		} else if (!cptPhoneNum.equals(other.cptPhoneNum))
			return false;
		if (cptPostalCode == null) {
			if (other.cptPostalCode != null)
				return false;
		} else if (!cptPostalCode.equals(other.cptPostalCode))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Account [id=" + id + ", cptEmail=" + cptEmail + ", cptName="
				+ cptName + ", cptFirstname=" + cptFirstname
				+ ", cptPostalCode=" + cptPostalCode + ", cptPhoneNum="
				+ cptPhoneNum + ", cptAddressLine1=" + cptAddressLine1
				+ ", cptAddressLine2=" + cptAddressLine2 + ", cptPassword="
				+ cptPassword + ", cptPasswordConfirm=" + cptPasswordConfirm
				+ ", country=" + country + ", type=" + type +  ", roles=" + roles + "]";
	}
}

