package xdi2.messenger.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import xdi2.messenger.model.CloudUser;

@Component
public class SuccessLoginListener implements ApplicationListener<AuthenticationSuccessEvent> {
	private static final Logger log = LoggerFactory.getLogger(SuccessLoginListener.class);

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		CloudUser user = (CloudUser) event.getAuthentication().getPrincipal();
		log.info("Login successfully " + user.getCloudName());
	}

}
