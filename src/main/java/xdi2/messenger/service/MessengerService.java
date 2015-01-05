package xdi2.messenger.service;

import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.XDIHttpClient;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.Relation;
import xdi2.core.constants.XDITimestampsConstants;
import xdi2.core.features.linkcontracts.instance.PublicLinkContract;
import xdi2.core.features.nodetypes.XdiCommonRoot;
import xdi2.core.features.nodetypes.XdiEntity;
import xdi2.core.features.nodetypes.XdiEntityCollection;
import xdi2.core.features.nodetypes.XdiEntityMember;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.XDIAddressUtil;
import xdi2.core.util.iterators.ReadOnlyIterator;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messenger.model.CloudUser;
import xdi2.messenger.model.Environment;
import xdi2.messenger.model.Message;

@Service
public class MessengerService {
	private static final Logger log = LoggerFactory.getLogger(MessengerService.class);

	private final static XDIAddress XDI_MESSAGES_COL = XDIAddress.create("[$messages]");
	private final static XDIAddress XDI_MESSAGE_TIMESTAMP = XDIAddress.create("<$t>");
	private final static XDIAddress XDI_MESSAGE_CONTENT = XDIAddress.create("<#content>");
	private final static XDIAddress XDI_MESSAGE_FROM = XDIAddress.create("$from");

	@Autowired
	ReverseNameResolutionService reverseNameResolutionService;

	public Collection<Message> getAllMessages() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		// Get messages from cloud
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		xdi2.messaging.Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		message.createGetOperation(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDI_MESSAGES_COL));

		log.debug("getAllMessages message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		MessageResult messageResult = user.getXdiClient().send(messageEnvelope, null);

		// Parse messages collection
		List<Message> messages = new ArrayList<Message>();

		ContextNode contextNode = messageResult.getGraph().getDeepContextNode(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDI_MESSAGES_COL));
		if (contextNode == null) {
			return messages;
		}
		XdiEntityCollection xdiMessagesCollection = XdiEntityCollection.fromContextNode(contextNode);
		if (xdiMessagesCollection == null) {
			return messages;
		}

		ReadOnlyIterator<XdiEntityMember> iMessage = xdiMessagesCollection.getXdiMembers();

		while (iMessage.hasNext()) {
			XdiEntityMember messageXdi = iMessage.next();

			Message msg = new Message();

			XDIAddress messageXdiAddress = messageXdi.getXDIAddress();
			msg.setXdiAddress(messageXdiAddress.toString());
			msg.setXdi(messageXdi.getGraph().toString("XDI DISPLAY", null));

			
			ReadOnlyIterator<Relation> relations = messageXdi.getGraph().getDeepRelations(messageXdi.getXDIAddress(), XDI_MESSAGE_FROM);
			while (relations.hasNext()) {
				Relation r = relations.next();
				
				String cloudNumber = r.getTargetContextNodeXDIAddress().toString();
				String cloudName = reverseNameResolutionService.getCloudName(user.getEnvironment(), cloudNumber);

				msg.setFrom(cloudName == null ? cloudNumber : cloudName);
			}

			Literal l = messageXdi.getGraph().getRootContextNode().getDeepLiteral(XDIAddressUtil.concatXDIAddresses(messageXdiAddress, XDI_MESSAGE_CONTENT, XDIAddress.create("&")));
			if (l != null) {
				msg.setContent(l.getLiteralDataString());
			}

			l = messageXdi.getGraph().getRootContextNode().getDeepLiteral(XDIAddressUtil.concatXDIAddresses(messageXdiAddress, XDI_MESSAGE_TIMESTAMP, XDIAddress.create("&")));
			if (l != null) {
				try {
					msg.setTimestamp(XDITimestampsConstants.FORMATS_TIMESTAMP[0].parse(l.getLiteralDataString()));
				} catch (ParseException e) {
					log.error("Error parsing message timestamp " + l.getLiteralDataString());
				}				
			}

			messages.add(msg);
		}

		return messages;
	}

	public void sendMessage(Message message) throws Xdi2ClientException {
		Assert.notNull(message);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		// Discover destination cloud 
		XDIDiscoveryClient xdiDiscoveryClient = user.getEnvironment() == Environment.PROD ? XDIDiscoveryClient.NEUSTAR_PROD_DISCOVERY_CLIENT : XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT;

		XDIDiscoveryResult discoveryResult = xdiDiscoveryClient.discoverFromRegistry(CloudName.create(message.getTo()).getXDIAddress(), null);
		CloudNumber toCloudNumber = discoveryResult.getCloudNumber();
		URL toXdiEndpoint = discoveryResult.getXdiEndpointUrl();

		// Build the message graph
		Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();
		XdiEntity msg = XdiCommonRoot.findCommonRoot(tempGraph)
				.getXdiEntity(toCloudNumber.getXDIAddress(), true)
				.getXdiEntityCollection(XDI_MESSAGES_COL, true)
				.setXdiMemberUnordered(null);

		String timestamp = XDITimestampsConstants.FORMATS_TIMESTAMP[0].format(new Date());
		msg.getXdiAttribute(XDI_MESSAGE_TIMESTAMP, true).getXdiValue(true).setLiteralString(timestamp);
		msg.getXdiAttribute(XDI_MESSAGE_CONTENT, true).getXdiValue(true).setLiteralString(message.getContent());

		tempGraph.setStatement(XDIStatement.fromRelationComponents(
				msg.getXDIAddress(),
				XDI_MESSAGE_FROM,
				user.getCloudNumber().getXDIAddress()));


		// Send message
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		xdi2.messaging.Message m = messageEnvelope.createMessage(user.getCloudNumber().getXDIAddress());
		m.setToPeerRootXDIArc(toCloudNumber.getPeerRootXDIArc());
		m.setLinkContract(PublicLinkContract.class);
		m.createSetOperation(tempGraph);

		log.debug("sendMessage message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		new XDIHttpClient(toXdiEndpoint).send(messageEnvelope, null);

	}

	public void deleteMessage(String messageXdiAddress) throws Xdi2ClientException {
		Assert.hasLength(messageXdiAddress);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		xdi2.messaging.Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		message.createDelOperation(XDIAddress.create(messageXdiAddress));

		log.debug("deleteMessage message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		user.getXdiClient().send(messageEnvelope, null);

	}

	public boolean checkCloudConfiguration() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		XDIStatement publicMessengerLC = XDIStatement.create("(" + user.getCloudNumber() + "/$public)$do/$set/" + user.getCloudNumber() + XDI_MESSAGES_COL);

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		xdi2.messaging.Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		message.createGetOperation(publicMessengerLC);

		log.debug("checkCloudConfiguration message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		MessageResult messageResult = user.getXdiClient().send(messageEnvelope, null);

		return messageResult.getGraph().containsStatement(publicMessengerLC);

	}

	public void setupCloud() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		XDIStatement publicMessengerLC = XDIStatement.create("(" + user.getCloudNumber() + "/$public)$do/$set/" + user.getCloudNumber() + XDI_MESSAGES_COL);

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		xdi2.messaging.Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		message.createSetOperation(publicMessengerLC);

		log.debug("setupCloud message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		user.getXdiClient().send(messageEnvelope, null);

	}


}
