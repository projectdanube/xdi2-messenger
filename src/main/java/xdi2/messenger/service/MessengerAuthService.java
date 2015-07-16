package xdi2.messenger.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.Graph;
import xdi2.core.constants.XDILinkContractConstants;
import xdi2.core.features.linkcontracts.LinkContracts;
import xdi2.core.features.linkcontracts.instance.GenericLinkContract;
import xdi2.core.features.linkcontracts.instance.LinkContract;
import xdi2.core.features.policy.PolicyAnd;
import xdi2.core.features.policy.PolicyUtil;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.iterators.IterableIterator;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.MessagingResponse;
import xdi2.messenger.model.CloudUser;
import xdi2.messenger.util.LogUtil;

@Service
public class MessengerAuthService {
	private static final Logger log = LoggerFactory.getLogger(MessengerAuthService.class);

	private static final XDIAddress XDI_LC_TAG = XDIAddress.create("#messages");

	@Autowired
	ReverseNameResolutionService reverseNameResolutionService;

	@Autowired
	DiscoveryService discoveryService;

	public List<String> getAuthorizedClouds() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// deletes the old public LC
		deletePublicLC();

		Iterator<LinkContract> linkContracts = getAllLinkContracts();

		List<String> cloudNames = new ArrayList<String>();
		while (linkContracts.hasNext()) {
			LinkContract linkContract = linkContracts.next();

			IterableIterator<XDIAddress> permissions = linkContract.getPermissionTargetXDIAddresses(XDILinkContractConstants.XDI_ADD_SET);
			boolean foundTarget = false;
			while(permissions.hasNext()) {
				XDIAddress p = permissions.next();
				if (p.toString().startsWith("" + user.getCloudNumber() + MessengerService.XDI_MESSAGES_COL)) {
					foundTarget = true;
				}
			}
			if (foundTarget == false) continue;

			String cloudNumber = ((GenericLinkContract) linkContract).getRequestingAuthority().toString();
			String cloudName = reverseNameResolutionService.getCloudName(user.getEnvironment(), cloudNumber);

			cloudNames.add(cloudName != null ? cloudName : cloudNumber);			
		}

		return cloudNames;
	}

	public void addAuthorizedCloudName(String cloudName) throws Xdi2ClientException {
		Assert.hasLength(cloudName);
		
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		CloudNumber cloudNumber = discoveryService.discover(user.getEnvironment(), cloudName);
		addAuthorizedCloudNumber(cloudNumber.toString());
	}

	public void addAuthorizedCloudNumber(String cloudNumber) throws Xdi2ClientException {
		Assert.hasLength(cloudNumber);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		// set up a link contract
		Graph g = MemoryGraphFactory.getInstance().openGraph();
		LinkContract l = GenericLinkContract.findGenericLinkContract(g, XDIAddress.create(user.getCloudNumber().toString()), XDIAddress.create(cloudNumber), XDI_LC_TAG, true);

		l.setPermissionTargetXDIAddress(XDILinkContractConstants.XDI_ADD_SET, XDIAddress.create("" + user.getCloudNumber() + MessengerService.XDI_MESSAGES_COL));

		PolicyAnd policyAnd = l.getPolicyRoot(true).createAndPolicy(true);
		PolicyUtil.createSenderIsOperator(policyAnd, XDIAddress.create(cloudNumber));
		PolicyUtil.createSignatureValidOperator(policyAnd);

		// create LC in the cloud
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		message.createSetOperation(g);

		user.getXdiClient().send(messageEnvelope);

	}

	public void deleteAuthorizedCloudName(String cloudName) throws Xdi2ClientException {
		Assert.hasLength(cloudName);
		
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		CloudNumber cloudNumber = discoveryService.discover(user.getEnvironment(), cloudName);
		deleteAuthorizedCloudNumber(cloudNumber.toString());
	}

	public void deleteAuthorizedCloudNumber(String cloudNumber) throws Xdi2ClientException {
		Assert.hasLength(cloudNumber);
		
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		message.createDelOperation(getOwnMessengerLCXdiAddress(cloudNumber));

		user.getXdiClient().send(messageEnvelope);		
	}

	// Messenger LC XDI Address used to add new authorized senders
	public XDIAddress getOwnMessengerLCXdiAddress(String senderCloudNumber) {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		return XDIAddress.create("(" + user.getCloudNumber() + "/" + senderCloudNumber + ")" + XDI_LC_TAG + "$do");
	}

	// Messenger LC XDI Address used send a message (it should exist in the receiver's cloud
	public XDIAddress getMessengerLCXdiAddress(String receiverCloudNumber) {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		return XDIAddress.create("(" + receiverCloudNumber + "/" + user.getCloudNumber() + ")" + XDI_LC_TAG + "$do");
	}

	private Iterator<LinkContract> getAllLinkContracts() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		message.createGetOperation(user.getCloudNumber().getXDIAddress());

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		return LinkContracts.getAllLinkContracts(messagingResponse.getResultGraph());
	}


	// Deletes old public LC for [$messages] 
	private void deletePublicLC() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		XDIStatement publicMessengerLC = XDIStatement.create("(" + user.getCloudNumber() + "/$public)$do/$set/" + user.getCloudNumber() + MessengerService.XDI_MESSAGES_COL);

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		xdi2.messaging.Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		message.createDelOperation(publicMessengerLC);

		log.debug("deletePublicLC message:\n" + LogUtil.prepareToLog(messageEnvelope.getGraph().toString("XDI DISPLAY", null)));

		user.getXdiClient().send(messageEnvelope);

	}

}
