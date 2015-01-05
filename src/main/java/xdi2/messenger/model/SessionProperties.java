package xdi2.messenger.model;

import java.io.Serializable;

public class SessionProperties implements Serializable {
	private static final long serialVersionUID = 8982798629085140357L;
	
	private String cloudName;
	private String cloudNumber;
	private String xdiEndpointUrl;
	private Environment environment;

	public SessionProperties() {
		super();
	}

	public SessionProperties(CloudUser cloudUser) {
		this.cloudName = cloudUser.getCloudName();
		this.cloudNumber = cloudUser.getCloudNumber().toString();
		this.xdiEndpointUrl = cloudUser.getXdiEndpointUrl();
		this.environment = cloudUser.getEnvironment();
	}
	
	
	public String getCloudName() {
		return cloudName;
	}
	public void setCloudName(String cloudName) {
		this.cloudName = cloudName;
	}
	public String getCloudNumber() {
		return cloudNumber;
	}
	public void setCloudNumber(String cloudNumber) {
		this.cloudNumber = cloudNumber;
	}
	public String getXdiEndpointUrl() {
		return xdiEndpointUrl;
	}
	public void setXdiEndpointUrl(String xdiEndpointUrl) {
		this.xdiEndpointUrl = xdiEndpointUrl;
	}
	public Environment getEnvironment() {
		return environment;
	}
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
}
