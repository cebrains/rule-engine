package com.uama.microservices.provider.ruleengine.support;

import com.uama.framework.core.DynamicDataSourceContextHolder;
import com.uama.framework.core.StreamConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;


public class DBChannelInterceptor implements ChannelInterceptor {

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		if (message.getHeaders().containsKey(StreamConstant.DBKEY)) {
			String dbkey = (String) message.getHeaders().get(StreamConstant.DBKEY);
			if (StringUtils.isNoneEmpty(dbkey)) {
				DynamicDataSourceContextHolder.setDataSourceType(dbkey);
			}
		} else if (StringUtils.isNoneEmpty(DynamicDataSourceContextHolder.getDataSourceType())) {
			MessageBuilder<?> messageBuilder = MessageBuilder.fromMessage(message);
			messageBuilder.setHeader(StreamConstant.DBKEY, DynamicDataSourceContextHolder.getDataSourceType());
			MessageHeaderAccessor headers = MessageHeaderAccessor.getMutableAccessor(message);
			headers.copyHeaders(messageBuilder.build().getHeaders());
			message = new GenericMessage<>(message.getPayload(), headers.getMessageHeaders());
		}

		return message;
	}
}
