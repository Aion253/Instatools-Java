package com.instatools.web.account;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import net.aionstudios.jdc.content.JDCHeadElement;
import net.aionstudios.jdc.content.RequestVariables;
import net.aionstudios.jdc.processor.ComputeSchedule;
import net.aionstudios.jdc.processor.ElementProcessor;
import net.aionstudios.jdc.processor.ProcessorSet;
import net.aionstudios.jdc.util.DatabaseUtils;

public class FrontPageLoginProcessor extends ElementProcessor {

	public FrontPageLoginProcessor(ProcessorSet set) {
		super("frontlog", set, ComputeSchedule.LIVE);
	}
	
	private String findSessionQuery = "SELECT `user_sessions`.`uid` FROM `aion_front`.`user_sessions` INNER JOIN `aion_front`.`users` ON `user_sessions`.`uid` = `users`.`uid` WHERE `sessionID` = ?;";
	
	@Override
	public void generateContent(JDCHeadElement element, HttpExchange he, RequestVariables vars,
			Map<String, Object> pageVariables) {
		String returnLogin="";
		try {
			returnLogin = "https://aionstudios.net/account/signin.jdc?pageReturn="+URLEncoder.encode("https://instatools.aionstudios.net/", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//UTF-8 is always in java.
			e.printStackTrace();
		}
		List<Map<String, Object>> qr = DatabaseUtils.prepareAndExecute(findSessionQuery, true, vars.getCookieManager().getRequestCookies().get("sessionID")).get(0).getResults();
		if(qr.isEmpty()){
			element.setText("<br><a href=\""+returnLogin+"\" alt='Sign In'>Sign in</a> now to see how your card is doing later.");
		} else {
			//No option to sign out because why?
			element.setText("<br><a href=\"https://instatools.aionstudios.net/history.jdc\" alt='View my cards'>View</a> your cards now.");
		}
		
	}

	@Override
	public void generateContent(JDCHeadElement element) {
		// TODO Auto-generated method stub
		
	}

}
