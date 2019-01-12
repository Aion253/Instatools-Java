package com.instatools.web.postcard;

import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import net.aionstudios.jdc.content.JDCElement;
import net.aionstudios.jdc.content.JDCHeadElement;
import net.aionstudios.jdc.content.RequestVariables;
import net.aionstudios.jdc.content.ResponseCode;
import net.aionstudios.jdc.processor.ComputeSchedule;
import net.aionstudios.jdc.processor.ElementProcessor;
import net.aionstudios.jdc.processor.ProcessorSet;
import net.aionstudios.jdc.util.DatabaseUtils;

public class CardProcessor extends ElementProcessor {

	public CardProcessor(ProcessorSet set) {
		super("card", set, ComputeSchedule.LIVE);
	}

	private String findTokenQuery = "SELECT * FROM `instatools`.`postcards` WHERE `cardID` = ?;";
	
	@Override
	public void generateContent(JDCHeadElement element, HttpExchange he, RequestVariables vars,
			Map<String, Object> pageVariables) {
		String p = vars.getGet().containsKey("p")?vars.getGet().get("p"):"";
		
		if(p.length() == 32) {
			List<Map<String,Object>> r = DatabaseUtils.prepareAndExecute(findTokenQuery, true, p).get(0).getResults();
			if(!r.isEmpty()) {
				String redirectUrl = (String) r.get(0).get("cardRedirect");
				String imageUrl = (String) r.get(0).get("cardImage");
				String caption = (String) r.get(0).get("cardCaption");
				String ua = he.getRequestHeaders().containsKey("User-Agent")?he.getRequestHeaders().getFirst("User-Agent"):"";
				if(ua.startsWith("Twitter")) {
					element.addChild(new JDCElement("meta").setAttribute("name", "twitter:card").setAttribute("content", "summary_large_image"))
						.addChild(new JDCElement("meta").setAttribute("name", "twitter:title").setAttribute("content", caption))
						.addChild(new JDCElement("meta").setAttribute("name", "twitter:description").setAttribute("content", "View the full post on Intstagram."))
						.addChild(new JDCElement("meta").setAttribute("name", "twitter:image").setAttribute("content", imageUrl));
				} else {
					vars.setRedirect(redirectUrl);
					return;
				}
			} else {
				vars.setResponseCode(ResponseCode.NOT_FOUND);
				return;
			}
		} else {
			if(p.length()==0) {
				vars.setResponseCode(ResponseCode.UNPROCESSABLE_ENTITY);
				return;
			} else {
				vars.setResponseCode(ResponseCode.NOT_FOUND);
				return;
			}
		}
	}

	@Override
	public void generateContent(JDCHeadElement element) {
		
	}

}
