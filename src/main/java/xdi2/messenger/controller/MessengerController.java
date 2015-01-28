package xdi2.messenger.controller;

import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.exceptions.Xdi2DiscoveryException;
import xdi2.messenger.model.Message;
import xdi2.messenger.service.MessengerAuthService;
import xdi2.messenger.service.MessengerService;

@RestController
@RequestMapping("/api/1.0/messenger/")
public class MessengerController extends AbstractController {

	@Autowired
	private MessengerService messengerService;
	
	@Autowired
	private MessengerAuthService messengerAuthService;
	
	@RequestMapping(value = "/messages/", method = RequestMethod.GET)
	public Collection<Message> getAllMessages() throws Xdi2ClientException {
		return messengerService.getAllMessages();
	}
	
	@RequestMapping(value = "/messages/", method = RequestMethod.POST)
	public void sendMessage(@RequestBody Message message) throws Xdi2DiscoveryException, Xdi2ClientException, MalformedURLException, GeneralSecurityException  {
		messengerService.sendMessage(message);
	}	
	
	@RequestMapping(value = "/messages/{id}", method = RequestMethod.DELETE)
	public void deleteMessage(@PathVariable String id) throws Xdi2ClientException {
		messengerService.deleteMessage(id);
	}
	
	@RequestMapping(value = "/authorizedsenders/", method = RequestMethod.GET)
	public List<String> getAuthorizedClouds() throws Xdi2ClientException {
		return messengerAuthService.getAuthorizedClouds();
	}
	
	@RequestMapping(value = "/authorizedsenders/", method = RequestMethod.POST)
	public void addAuthorizedCloud(@RequestBody String cloudName) throws Xdi2ClientException {
		messengerAuthService.addAuthorizedCloudName(cloudName);;
	}
	
	@RequestMapping(value = "/authorizedsenders/{cloudName}", method = RequestMethod.DELETE)
	public void deleteAuthorizedCloud(@PathVariable String cloudName) throws Xdi2ClientException {
		messengerAuthService.deleteAuthorizedCloudName(cloudName);;
	}
	

}
