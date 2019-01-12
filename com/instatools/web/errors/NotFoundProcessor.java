package com.instatools.web.errors;

import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import net.aionstudios.jdc.content.JDCHeadElement;
import net.aionstudios.jdc.content.RequestVariables;
import net.aionstudios.jdc.processor.ComputeSchedule;
import net.aionstudios.jdc.processor.ElementProcessor;
import net.aionstudios.jdc.processor.ProcessorSet;

public class NotFoundProcessor extends ElementProcessor {

	public NotFoundProcessor(ProcessorSet set) {
		super("404", set, ComputeSchedule.LIVE);
	}

	@Override
	public void generateContent(JDCHeadElement element, HttpExchange he, RequestVariables vars,
			Map<String, Object> pageVariables) {
		element.setText("We couldn't find a resource at "+he.getRequestURI().toString()+" on this server.");
	}

	@Override
	public void generateContent(JDCHeadElement element) {
		//not needed
	}

}
