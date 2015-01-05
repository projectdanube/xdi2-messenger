package xdi2.messenger.model;

import java.util.Date;

public class Message {
	
	private String from;
	private String to;
	private String content;
	private Date timestamp;
	private String xdiAddress;
	private String xdi;
	
	public Message(String from, String content) {
		this.from = from;
		this.content = content;
		this.timestamp = new Date();
	}

	public Message() {

	}
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getXdiAddress() {
		return xdiAddress;
	}

	public void setXdiAddress(String xdiAddress) {
		this.xdiAddress = xdiAddress;
	}

	public String getXdi() {
		return xdi;
	}

	public void setXdi(String xdi) {
		this.xdi = xdi;
	}
	
	

}
