package com.instatools.web.session;

import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import net.aionstudios.jdc.content.Cookie;
import net.aionstudios.jdc.content.RequestVariables;
import net.aionstudios.jdc.database.DatabaseConnector;
import net.aionstudios.jdc.database.QueryResults;
import net.aionstudios.jdc.processor.Processor;
import net.aionstudios.jdc.processor.ProcessorSet;
import net.aionstudios.jdc.util.DatabaseUtils;
import net.aionstudios.jdc.util.SecurityUtils;

public class ValidateSessionProcessor extends Processor {

	public ValidateSessionProcessor(ProcessorSet set) {
		super("validatesession", set);
	}

	@Override
	public void compute(HttpExchange he, RequestVariables vars, Map<String, Object> pageVariables) {
		if(vars.getCookieManager().getRequestCookies().containsKey("sessionID") && vars.getCookieManager().getRequestCookie("sessionID").isEmpty()) {
			Cookie sessionCookie = new Cookie("sessionID", generateNewSession(vars));
			sessionCookie.setDomain("aionstudios.net");
			sessionCookie.setPath("/");
			vars.getCookieManager().getNewCookies().add(sessionCookie);
		} else {
			checkSession(vars);
		}
	}
	
	private String findSessionQuery = "SELECT * FROM `aion_front`.`web_sessions` WHERE `sessionID` = ?;";
	private String insertSessionQuery = "INSERT INTO `aion_front`.`web_sessions` (`sessionID`) VALUES (?);";
	private String updateSessionAge = "UPDATE `aion_front`.`web_sessions` SET `last` = CURRENT_TIMESTAMP WHERE `sessionID` = ?;";
	
	
	private String generateNewSession(RequestVariables vars) {
		String trySession = "";
		boolean sessionAvailable = false;
		while(!sessionAvailable) {
			trySession = SecurityUtils.genToken(64);
			if(DatabaseUtils.prepareAndExecute(findSessionQuery, true, trySession).get(0).getResults().isEmpty()) {
				sessionAvailable = true;
			}
		}
		DatabaseUtils.prepareAndExecute(insertSessionQuery, true, trySession);
		vars.getRequestCookies().put("sessionID", trySession);
		return trySession;
	}
	
	private void checkSession(RequestVariables vars) {
		if(DatabaseUtils.prepareAndExecute(findSessionQuery, true, vars.getCookieManager().getRequestCookies().get("sessionID")).get(0).getResults().isEmpty()) {
			Cookie sessionCookie = new Cookie("sessionID", generateNewSession(vars));
			sessionCookie.setDomain("aionstudios.net");
			sessionCookie.setPath("/");
			vars.getCookieManager().getNewCookies().add(sessionCookie);
			return;
		}
		DatabaseUtils.prepareAndExecute(updateSessionAge, true, vars.getCookieManager().getRequestCookies().get("sessionID"));
	}

}
