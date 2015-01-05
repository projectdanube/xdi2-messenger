package xdi2.messenger.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.messenger.model.Message;
import xdi2.messenger.service.MessengerService;

@RestController
@RequestMapping("/api/1.0/messenger/")
public class MessengerController extends AbstractController {

	@Autowired
	private MessengerService messengerService;
	
	@RequestMapping(value = "/messages/", method = RequestMethod.GET)
	public Collection<Message> getAllMessages() throws Xdi2ClientException {
		return messengerService.getAllMessages();
	}
	
	@RequestMapping(value = "/messages/", method = RequestMethod.POST)
	public void sendMessage(@RequestBody Message message) throws Xdi2ClientException {
		messengerService.sendMessage(message);
	}	
	
	@RequestMapping(value = "/messages/{id}", method = RequestMethod.DELETE)
	public void deleteMessage(@PathVariable String id) throws Xdi2ClientException {
		messengerService.deleteMessage(id);
	}
	
	@RequestMapping(value = "/setup/", method = RequestMethod.GET)
	public boolean checkCloudConfiguration() throws Xdi2ClientException {
		return messengerService.checkCloudConfiguration();
	}
	
	@RequestMapping(value = "/setup/", method = RequestMethod.POST)
	public void setupCloud() throws Xdi2ClientException {
		messengerService.setupCloud();
	}
	

}
