package xdi2.messenger.service;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messenger.model.Environment;

@Service
public class DiscoveryService {
	
	public CloudNumber discover (Environment env, String cloudName) throws Xdi2ClientException {
		Assert.notNull(env);
		Assert.hasLength(cloudName);
		
		XDIDiscoveryResult result = getXdiDiscoveryForEnv(env).discoverFromRegistry(XDIAddress.create(cloudName));

		return result.getCloudNumber();
	}
	
	private XDIDiscoveryClient getXdiDiscoveryForEnv(Environment env) {
		
		if (env == Environment.OTE) 
			return XDIDiscoveryClient.XDI2_NEUSTAR_OTE_DISCOVERY_CLIENT;
		else
			return XDIDiscoveryClient.XDI2_NEUSTAR_PROD_DISCOVERY_CLIENT;
	}
	
}
