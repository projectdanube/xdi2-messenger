package xdi2.messenger.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.ssl.XDI2X509TrustManager;
import xdi2.core.features.linkcontracts.instance.RootLinkContract;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messenger.model.CloudUser;
import xdi2.messenger.model.Environment;
import xdi2.messenger.service.security.CMWebAuthenticationDetails;


public class XdiHelper {

	XDIDiscoveryClient discoveryClient;
	MessageEnvelope messageEnvelope;
	MessageCollection messageCollection;
	
	/*
	 * 		message.setToPeerRootXDIArc(getCloudNumber().getPeerRootXDIArc());
		message.setLinkContract(RootLinkContract.class);
		message.setSecretToken(this.secretToken);
	 * 
	 */
	
//	void bla() {
//		
//
//
//		String cloudName = null;
//		String secretToken = null;
//		
//		if (! (cloudName.startsWith("=") || cloudName.startsWith("*") || cloudName.startsWith("+")) ) {
//			throw new UsernameNotFoundException("Cloud Name doesn't seem to be valid. Please check if it starts with =");
//		}
//		
//		// cloud name discovery
//		XDI2X509TrustManager.enable();
//
//		XDIDiscoveryClient discoveryClient = null;
//		if(env == Environment.PROD) {
//			discoveryClient = XDIDiscoveryClient.DEFAULT_DISCOVERY_CLIENT;
//		}
//		else {
//			discoveryClient = XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT;
//		}
//		
//		XDIDiscoveryResult result = null;
//		try {
//			result = discoveryClient.discoverFromRegistry(XDIAddress.create(cloudName), null);
//		} catch (Xdi2ClientException e1) {
//			log.warn("Error while discovering " + cloudName + ": " + e1.getMessage(), e1);
//			throw new UsernameNotFoundException(e1.getMessage());
//		}
//		if (result == null || result.getCloudNumber() == null) {
//			throw new UsernameNotFoundException("Cloud " + cloudName + " not found.");
//		}
//		if (result.getXdiEndpointUrl() == null || StringUtils.isBlank(result.getXdiEndpointUrl().toString())){
//			throw new UsernameNotFoundException("Cloud " + cloudName + " found with Cloud Number " + result.getCloudNumber() + " but without Cloud Endpoint.");	
//		}
//
//		CloudNumber cloudNumber = result.getCloudNumber();
//		String xdiEndpointUri = result.getXdiEndpointUrl().toString();
//
//		// authentication on personal cloud
//		CloudUser cloudUser = new CloudUser(cloudName, cloudNumber, xdiEndpointUri, secretToken, env);
//
//		MessageEnvelope messageEnvelope = new MessageEnvelope();
//		MessageCollection messageCollection = messageEnvelope.getMessageCollection(cloudUser.getCloudNumber().getXDIAddress(), true);
//		Message message = messageCollection.createMessage();
//		message = cloudUser.prepareMessageToCloud(message);
//		message.createGetOperation(RootLinkContract.createRootLinkContractXDIAddress(cloudUser.getCloudNumber().getXDIAddress()));
//	
//		try {
//			cloudUser.getXdiClient().send(messageEnvelope, null);
//		} catch (Xdi2ClientException e) {
//			if (StringUtils.containsIgnoreCase(e.getMessage(), "invalid secret token")) {
//				throw new BadCredentialsException("Invalid Cloud Name or password ");
//			}
//			else {
//				throw new BadCredentialsException(e.getMessage());
//			}
//		}
//		
//		// what can we do here?
//		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
//		//		SimpleGrantedAuthority role = new SimpleGrantedAuthority("USER_ROLE");
//		//		authorities.add(role);
//
//	
//		
//	}
}
