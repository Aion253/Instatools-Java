package com.instatools.web.postcard;

import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import net.aionstudios.jdc.content.JDCElement;
import net.aionstudios.jdc.content.JDCHeadElement;
import net.aionstudios.jdc.content.RequestVariables;
import net.aionstudios.jdc.processor.ComputeSchedule;
import net.aionstudios.jdc.processor.ElementProcessor;
import net.aionstudios.jdc.processor.ProcessorSet;
import net.aionstudios.jdc.util.DatabaseUtils;

public class HistoryCardsProcessor extends ElementProcessor {

	public HistoryCardsProcessor(ProcessorSet set) {
		super("history", set, ComputeSchedule.LIVE);
	}

	private String getUserPostcardsQuery = "SELECT `postcards`.`cardID`, `postcards`.`cardCaption`, `postcards`.`cardImage`, `postcards`.`cardRedirect` FROM `instatools`.`postcards` WHERE `postcards`.`cardOwner` = ?;";
	private String getTrackingTCOQuery = "SELECT COUNT(*) FROM `instatools`.`tracking` WHERE `cardID` = ? AND `referer` LIKE '%t.co%';";
	
	@Override
	public void generateContent(JDCHeadElement element, HttpExchange he, RequestVariables vars,
			Map<String, Object> pageVariables) {
		List<Map<String, Object>> qr = DatabaseUtils.prepareAndExecute(getUserPostcardsQuery, true, pageVariables.get("uid")).get(0).getResults();
		if(!qr.isEmpty()) {
			for(Map<String, Object> m : qr) {
				long trc = (long) DatabaseUtils.prepareAndExecute(getTrackingTCOQuery, true, m.get("cardID")).get(0).getResults().get(0).get("COUNT(*)");
				JDCElement historyCard = new JDCElement("div").setAttribute("class", "history-card")
						.addChild(new JDCElement("div").setAttribute("class", "hc-background").setAttribute("style", "background-image: url('"+m.get("cardImage")+"');"))
						.addChild(new JDCElement("p").setAttribute("class", "hc-caption").setText(m.get("cardCaption")+"<span class=\"hc-count\">"+(trc==1?"1 click":(trc + " clicks"))+"</span>"))
						.addChild(new JDCElement("a").setAttribute("class", "hc-link").setAttribute("target", "_blank").setAttribute("href", (String) m.get("cardRedirect")).setText("View the full post on Instagram."));
				element.addChild(historyCard);
			}
		} else {
			element.setText("No posts for this account!");
		}
	}

	@Override
	public void generateContent(JDCHeadElement element) {
		
	}

}
