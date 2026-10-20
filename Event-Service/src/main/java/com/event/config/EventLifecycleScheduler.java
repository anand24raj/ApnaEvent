package com.event.config;

import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;
import com.event.service.EventService;

@Component
public class EventLifecycleScheduler {
	private final EventService service;

	public EventLifecycleScheduler(EventService service) {
		this.service = service;
	}

	@Scheduled(fixedDelayString = "${event.lifecycle.fixed-delay-ms:60000}")
	public void update() {
		service.updateLifecycle();
	}
}
