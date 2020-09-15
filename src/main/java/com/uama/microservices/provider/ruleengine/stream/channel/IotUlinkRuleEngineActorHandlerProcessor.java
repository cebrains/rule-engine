package com.uama.microservices.provider.ruleengine.stream.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface IotUlinkRuleEngineActorHandlerProcessor {
	@Input(IotUlinkRuleEngineStreamInput.ACTOR_HANDLER_INPUT)
	SubscribableChannel input();
}
