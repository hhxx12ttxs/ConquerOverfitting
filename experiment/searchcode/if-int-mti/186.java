package com.mti.shop.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "Shop")
public class Shop {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "shop_name", length = 1024)
	private String shopName;

	@Column(name = "shop_desc", length = 1024)
	private String shopDesc;

	@Column(name = "shop_email", length = 1024)
	private String shopEmail;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "shop_cnt_id")
	private Country country;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "shop_cat_id")
	private Categorie categorie;


	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "owners")
	private Set<Account> owners;

	@Transient
	private String catId;

	@Transient
	private String cntId;

	public String getCatId() {
		return catId;
	}

	public void setCatId(String catId) {
		this.catId = catId;
	}

	public String getCntId() {
		return cntId;
	}

	public void setCntId(String cntId) {
		this.cntId = cntId;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Categorie getCategorie() {
		return categorie;
	}

	public void setCategorie(Categorie categorie) {
		this.categorie = categorie;
	}


	public Set<Account> getOwners() {
		return owners;
	}

	public void setOwners(Set<Account> owners) {
		this.owners = owners;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public String getShopDesc() {
		return shopDesc;
	}

	public void setShopDesc(String shopDesc) {
		this.shopDesc = shopDesc;
	}

	public String getShopEmail() {
		return shopEmail;
	}

	public void setShopEmail(String shopEmail) {
		this.shopEmail = shopEmail;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((categorie == null) ? 0 : categorie.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((shopDesc == null) ? 0 : shopDesc.hashCode());
		result = prime * result
				+ ((shopEmail == null) ? 0 : shopEmail.hashCode());
		result = prime * result
				+ ((shopName == null) ? 0 : shopName.hashCode());
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
		Shop other = (Shop) obj;
		if (categorie == null) {
			if (other.categorie != null)
				return false;
		} else if (!categorie.equals(other.categorie))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (shopDesc == null) {
			if (other.shopDesc != null)
				return false;
		} else if (!shopDesc.equals(other.shopDesc))
			return false;
		if (shopEmail == null) {
			if (other.shopEmail != null)
				return false;
		} else if (!shopEmail.equals(other.shopEmail))
			return false;
		if (shopName == null) {
			if (other.shopName != null)
				return false;
		} else if (!shopName.equals(other.shopName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Shop [id=" + id + ", shopName=" + shopName + ", shopDesc="
				+ shopDesc + ", shopEmail=" + shopEmail + ", country="
				+ country + ", categorie=" + categorie +  "]";
	}
}

