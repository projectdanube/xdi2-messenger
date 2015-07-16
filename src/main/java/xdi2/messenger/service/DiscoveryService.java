package xdi2.messenger.service;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

@Service
public class DiscoveryService {
	
	public CloudNumber discover (String cloudName) throws Xdi2ClientException {
		Assert.hasLength(cloudName);
		
		XDIDiscoveryResult result = XDIDiscoveryClient.XDI2_DISCOVERY_CLIENT.discoverFromRegistry(XDIAddress.create(cloudName));

		return result.getCloudNumber();
	}
	
}
