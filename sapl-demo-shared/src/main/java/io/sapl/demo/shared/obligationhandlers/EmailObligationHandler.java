package io.sapl.demo.shared.obligationhandlers;

import com.fasterxml.jackson.databind.JsonNode;
import io.sapl.spring.marshall.obligation.Obligation;
import io.sapl.spring.marshall.obligation.ObligationFailed;
import io.sapl.spring.marshall.obligation.ObligationHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailObligationHandler implements ObligationHandler {


	@Override
	public void handleObligation(Obligation obligation) throws ObligationFailed {
		JsonNode obNode = obligation.getJsonObligation();
		if (obNode.has("recipient") && obNode.has("subject") && obNode.has("message")) {
			sendEmail(obNode.findValue("recipient").asText(), obNode.findValue("subject").asText(),
					obNode.findValue("message").asText());
		} else {
			throw new ObligationFailed();
		}

	}

	@Override
	public boolean canHandle(Obligation obligation) {
		JsonNode obNode = obligation.getJsonObligation();
		if (obNode.has("type")) {
			String type = obNode.findValue("type").asText();
			if ("sendEmail".equals(type)) {
				return true;
			}
		}
		return false;
	}

	private static void sendEmail(String recipient, String subject, String message) {
		LOGGER.info("An E-Mail has been sent to {} with the subject '{}' and the message '{}'.", recipient, subject,
				message);
	}

}
