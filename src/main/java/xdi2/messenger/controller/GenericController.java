package xdi2.messenger.controller;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.messenger.model.CloudUser;
import xdi2.messenger.model.SessionProperties;
import xdi2.messenger.service.DiscoveryService;

@RestController
@RequestMapping("/api/1.0/")
public class GenericController extends AbstractController {
	
	@Autowired
	DiscoveryService discoveryService;

	@RequestMapping(value = "/session/properties/", method = RequestMethod.GET)
	public SessionProperties getSessionDetails() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		SessionProperties sessionProperties = new SessionProperties(user);
		return sessionProperties;
	}
	
	@RequestMapping(value = "/discovery/{cloudName}", method = RequestMethod.GET)
	public String discoverCloudName(@PathVariable String cloudName) throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		return ObjectUtils.toString("\"" + discoveryService.discover(cloudName) + "\"", null);
	}
	
}
