package com.mti.shop.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Product")

public class Product implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "prod_name", length = 1024)
	private String prodName;

	@Column(name = "prod_desc", length = 1024)
	private String prodDesc;

	@Column(name = "prod_price")
	private Double prodPrice;

	@Column(name = "prod_expire")
	private Date prodExpire;

	@Column(name = "prod_stock")
	private Integer prodStock;

	@Column(name = "prod_to_display")
	private Boolean prodToDisplay;

	@Column(name = "prod_date_publish")
	private Date prodDatePublish;

	@Column(name = "prod_type_of_sell")
	private Integer prodTypeOfSell;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "prod_cat_id")
	private Categorie categorie;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "prod_shop_id")
	private Shop shop;

	public Categorie getCategorie() {
		return categorie;
	}

	public void setCategorie(Categorie categorie) {
		this.categorie = categorie;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProdName() {
		return prodName;
	}

	public void setProdName(String prodName) {
		this.prodName = prodName;
	}

	public String getProdDesc() {
		return prodDesc;
	}

	public void setProdDesc(String prodDesc) {
		this.prodDesc = prodDesc;
	}

	public Double getProdPrice() {
		return prodPrice;
	}

	public void setProdPrice(Double prodPrice) {
		this.prodPrice = prodPrice;
	}

	public Date getProdExpire() {
		return prodExpire;
	}

	public void setProdExpire(Date prodExpire) {
		this.prodExpire = prodExpire;
	}

	public Integer getProdStock() {
		return prodStock;
	}

	public void setProdStock(Integer prodStock) {
		this.prodStock = prodStock;
	}

	public Boolean getProdToDisplay() {
		return prodToDisplay;
	}

	public void setProdToDisplay(Boolean prodToDisplay) {
		this.prodToDisplay = prodToDisplay;
	}

	public Date getProdDatePublish() {
		return prodDatePublish;
	}

	public void setProdDatePublish(Date prodDatePublish) {
		this.prodDatePublish = prodDatePublish;
	}

	public Integer getProdTypeOfSell() {
		return prodTypeOfSell;
	}

	public void setProdTypeOfSell(Integer prodTypeOfSell) {
		this.prodTypeOfSell = prodTypeOfSell;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((categorie == null) ? 0 : categorie.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((prodDatePublish == null) ? 0 : prodDatePublish.hashCode());
		result = prime * result
				+ ((prodDesc == null) ? 0 : prodDesc.hashCode());
		result = prime * result
				+ ((prodExpire == null) ? 0 : prodExpire.hashCode());
		result = prime * result
				+ ((prodName == null) ? 0 : prodName.hashCode());
		result = prime * result
				+ ((prodPrice == null) ? 0 : prodPrice.hashCode());
		result = prime * result
				+ ((prodStock == null) ? 0 : prodStock.hashCode());
		result = prime * result
				+ ((prodToDisplay == null) ? 0 : prodToDisplay.hashCode());
		result = prime * result
				+ ((prodTypeOfSell == null) ? 0 : prodTypeOfSell.hashCode());
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
		Product other = (Product) obj;
		if (categorie == null) {
			if (other.categorie != null)
				return false;
		} else if (!categorie.equals(other.categorie))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (prodDatePublish == null) {
			if (other.prodDatePublish != null)
				return false;
		} else if (!prodDatePublish.equals(other.prodDatePublish))
			return false;
		if (prodDesc == null) {
			if (other.prodDesc != null)
				return false;
		} else if (!prodDesc.equals(other.prodDesc))
			return false;
		if (prodExpire == null) {
			if (other.prodExpire != null)
				return false;
		} else if (!prodExpire.equals(other.prodExpire))
			return false;
		if (prodName == null) {
			if (other.prodName != null)
				return false;
		} else if (!prodName.equals(other.prodName))
			return false;
		if (prodPrice == null) {
			if (other.prodPrice != null)
				return false;
		} else if (!prodPrice.equals(other.prodPrice))
			return false;
		if (prodStock == null) {
			if (other.prodStock != null)
				return false;
		} else if (!prodStock.equals(other.prodStock))
			return false;
		if (prodToDisplay == null) {
			if (other.prodToDisplay != null)
				return false;
		} else if (!prodToDisplay.equals(other.prodToDisplay))
			return false;
		if (prodTypeOfSell == null) {
			if (other.prodTypeOfSell != null)
				return false;
		} else if (!prodTypeOfSell.equals(other.prodTypeOfSell))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Product [id=" + id + ", prodName=" + prodName + ", prodDesc="
				+ prodDesc + ", prodPrice=" + prodPrice + ", prodExpire="
				+ prodExpire + ", prodStock=" + prodStock + ", prodToDisplay="
				+ prodToDisplay + ", prodDatePublish=" + prodDatePublish
				+ ", prodTypeOfSell=" + prodTypeOfSell + ", categorie="
				+ categorie + "]";
	}

}

