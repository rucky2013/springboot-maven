package cn.zhangxd.trip.client.mobile.security.exception;

import cn.zhangxd.trip.client.mobile.constant.Message;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.security.oauth2.common.exceptions.*;
import org.springframework.security.oauth2.common.util.OAuth2Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("serial")
public class OAuth2ExceptionJacksonDeserializer extends StdDeserializer<OAuth2Exception> {

	public OAuth2ExceptionJacksonDeserializer() {
		super(OAuth2Exception.class);
	}

	@Override
	public OAuth2Exception deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
			JsonProcessingException {

		JsonToken t = jp.getCurrentToken();
		if (t == JsonToken.START_OBJECT) {
			t = jp.nextToken();
		}
		Map<String, Object> errorParams = new HashMap<String, Object>();
		for (; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
			// Must point to field name
			String fieldName = jp.getCurrentName();
			// And then the value...
			t = jp.nextToken();
			// Note: must handle null explicitly here; value deserializers won't
			Object value;
			if (t == JsonToken.VALUE_NULL) {
				value = null;
			}
			// Some servers might send back complex content
			else if (t == JsonToken.START_ARRAY) {
				value = jp.readValueAs(List.class);
			}
			else if (t == JsonToken.START_OBJECT) {
				value = jp.readValueAs(Map.class);
			}
			else {
				value = jp.getText();
			}
			errorParams.put(fieldName, value);
		}

		Object errorCode = errorParams.get(Message.RETURN_FIELD_ERROR);
		String errorMessage = errorParams.containsKey(Message.RETURN_FIELD_ERROR_DESC) ? errorParams.get(Message.RETURN_FIELD_ERROR_DESC)
				.toString() : null;
		if (errorMessage == null) {
			errorMessage = errorCode == null ? "OAuth Error" : errorCode.toString();
		}

		OAuth2Exception ex;
		if ("invalid_client".equals(errorCode)) {
			ex = new InvalidClientException(errorMessage);
		}
		else if ("unauthorized_client".equals(errorCode)) {
			ex = new UnauthorizedClientException(errorMessage);
		}
		else if ("invalid_grant".equals(errorCode)) {
			if (errorMessage.toLowerCase().contains("redirect") && errorMessage.toLowerCase().contains("match")) {
				ex = new RedirectMismatchException(errorMessage);
			}
			else {
				ex = new InvalidGrantException(errorMessage);
			}
		}
		else if ("invalid_scope".equals(errorCode)) {
			ex = new InvalidScopeException(errorMessage);
		}
		else if ("invalid_token".equals(errorCode)) {
			ex = new InvalidTokenException(errorMessage);
		}
		else if ("invalid_request".equals(errorCode)) {
			ex = new InvalidRequestException(errorMessage);
		}
		else if ("redirect_uri_mismatch".equals(errorCode)) {
			ex = new RedirectMismatchException(errorMessage);
		}
		else if ("unsupported_grant_type".equals(errorCode)) {
			ex = new UnsupportedGrantTypeException(errorMessage);
		}
		else if ("unsupported_response_type".equals(errorCode)) {
			ex = new UnsupportedResponseTypeException(errorMessage);
		}
		else if ("insufficient_scope".equals(errorCode)) {
			ex = new InsufficientScopeException(errorMessage, OAuth2Utils.parseParameterList((String) errorParams
					.get("scope")));
		}
		else if ("access_denied".equals(errorCode)) {
			ex = new UserDeniedAuthorizationException(errorMessage);
		}
		else {
			ex = new OAuth2Exception(errorMessage);
		}

		Set<Map.Entry<String, Object>> entries = errorParams.entrySet();
		for (Map.Entry<String, Object> entry : entries) {
			String key = entry.getKey();
			if (!Message.RETURN_FIELD_ERROR.equals(key) && !Message.RETURN_FIELD_ERROR_DESC.equals(key)) {
				Object value = entry.getValue();
				ex.addAdditionalInformation(key, value == null ? null : value.toString());
			}
		}

		return ex;

	}

}
