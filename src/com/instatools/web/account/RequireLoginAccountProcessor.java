package com.instatools.web.account;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import net.aionstudios.jdc.content.RequestVariables;
import net.aionstudios.jdc.processor.Processor;
import net.aionstudios.jdc.processor.ProcessorSet;
import net.aionstudios.jdc.util.DatabaseUtils;

public class RequireLoginAccountProcessor extends Processor {
	
	private String findSessionQuery = "SELECT `user_sessions`.`uid` FROM `aion_front`.`user_sessions` INNER JOIN `aion_front`.`users` ON `user_sessions`.`uid` = `users`.`uid` WHERE `sessionID` = ?;";

	public RequireLoginAccountProcessor(ProcessorSet set) {
		super("reqlog", set);
	}

	@Override
	public void compute(HttpExchange he, RequestVariables vars, Map<String, Object> pageVariables) {
		String pageReturn = vars.getPage();
		Map<String, String> gets = vars.getGet();
		if(gets.size()>0) {
			pageReturn = pageReturn + "?" + gets.keySet().toArray()[0] + "=" + gets.get(gets.keySet().toArray()[0]);
		}
		for(int i = 1; i < gets.size(); i++) {
			pageReturn = pageReturn + "&" + gets.keySet().toArray()[0] + "=" + gets.get(gets.keySet().toArray()[0]);
		}
		List<Map<String, Object>> qr = DatabaseUtils.prepareAndExecute(findSessionQuery, true, vars.getCookieManager().getRequestCookies().get("sessionID")).get(0).getResults();
		if(qr.isEmpty()){
			try {
				vars.setRedirect("https://aionstudios.net/account/signin.jdc?pageReturn="+URLEncoder.encode("https://instatools.aionstudios.net"+pageReturn, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				//utf8 always supported
			}
		} else {
			pageVariables.put("uid", qr.get(0).get("uid"));
		}
	}

}
