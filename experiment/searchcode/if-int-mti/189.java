package com.mti.shop.model;

import java.io.Serializable;

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
@Table(name = "Evaluation")
public class Evaluation implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "eval_note")
	private Integer evalNote;

	@Column(name = "eval_comment", length = 1024)
	private String evalComment;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "eval_cpt_id")
	private Account evaluator;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "eval_evaluated_account")
	private Account evaluated;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "eval_evaluated_shop")
	private Shop shopEvaluated;
	
	
	public Account getEvaluated() {
		return evaluated;
	}

	public void setEvaluated(Account evaluated) {
		this.evaluated = evaluated;
	}

	public Shop getShopEvaluated() {
		return shopEvaluated;
	}

	public void setShopEvaluated(Shop shopEvaluated) {
		this.shopEvaluated = shopEvaluated;
	}

	public Account getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(Account evaluator) {
		this.evaluator = evaluator;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getEvalNote() {
		return evalNote;
	}

	public void setEvalNote(Integer evalNote) {
		this.evalNote = evalNote;
	}

	public String getEvalComment() {
		return evalComment;
	}

	public void setEvalComment(String evalComment) {
		this.evalComment = evalComment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((evalComment == null) ? 0 : evalComment.hashCode());
		result = prime * result
				+ ((evalNote == null) ? 0 : evalNote.hashCode());
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
		Evaluation other = (Evaluation) obj;
		if (evalComment == null) {
			if (other.evalComment != null)
				return false;
		} else if (!evalComment.equals(other.evalComment))
			return false;
		if (evalNote == null) {
			if (other.evalNote != null)
				return false;
		} else if (!evalNote.equals(other.evalNote))
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
		return "Evaluation [id=" + id + ", evalNote=" + evalNote
				+ ", evalComment=" + evalComment + "]";
	}

}

