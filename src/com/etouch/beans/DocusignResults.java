package com.etouch.beans;

import java.sql.Date;

public class DocusignResults {
	
	private int id;
	private String envelopeId;
	private String status;
	private String document;
	private Date date;
	private String fullJson;
	private String header;
	
	
	public String getEnvelopeId() {
		return envelopeId;
	}
	public void setEnvelopeId(String envelopeId) {
		this.envelopeId = envelopeId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDocument() {
		return document;
	}
	public void setDocument(String document) {
		this.document = document;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getFullJson() {
		return fullJson;
	}
	public void setFullJson(String fullJson) {
		this.fullJson = fullJson;
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	
}
