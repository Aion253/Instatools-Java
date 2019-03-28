package com.instatools.web.postcard;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import net.aionstudios.jdc.content.JDCElement;
import net.aionstudios.jdc.content.JDCHeadElement;
import net.aionstudios.jdc.content.RequestVariables;
import net.aionstudios.jdc.processor.ComputeSchedule;
import net.aionstudios.jdc.processor.ElementProcessor;
import net.aionstudios.jdc.processor.ProcessorSet;
import net.aionstudios.jdc.util.DatabaseUtils;
import net.aionstudios.jdc.util.SecurityUtils;

public class PostCardProcessor extends ElementProcessor {

	public PostCardProcessor(ProcessorSet set) {
		super("form", set, ComputeSchedule.LIVE);
	}
	
	private String instagramRegex = "(https?:\\/\\/)(www\\.)?instagram\\.com(\\/p\\/\\w+\\/)";

	@Override
	public void generateContent(JDCHeadElement element, HttpExchange he, RequestVariables vars,
			Map<String, Object> pageVariables) {
		
		boolean complete = false;
		
		String instagramLink = vars.getPost().containsKey("instaLink")?vars.getPost().get("instaLink"):"";
		if(!instagramLink.startsWith("http")) {
			instagramLink = "https://"+instagramLink;
		}
		if(instagramLink.length()>30&&instagramLink.contains("?")) {
			instagramLink = instagramLink.split("\\?", 2)[0];
		}
		String instagramImage = instagramLink+"media/?size=l";
		String error = "";
		int code = 404;
		try {
			if(instagramLink.matches(instagramRegex)) {
				URL url = new URL(instagramLink);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setInstanceFollowRedirects(true);
				connection.setRequestProperty("User-Agent", "Aion InstaTools/1.1");
				connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				connection.addRequestProperty("Referer", "https://instatools.aionstudios.net/");
				connection.setRequestMethod("GET");
				connection.connect();
				code = connection.getResponseCode();
			}
		} catch (IOException e) {
			code = 404;
		}
		instagramImage = getFinalURL(instagramImage);
		if(code == 200) {
			if((vars.getPost().containsKey("instaLink"))) {
				if(vars.getPost().containsKey("instaCap")) {
					String ic = vars.getPost().get("instaCap");
					if(instagramLink.matches(instagramRegex) && instagramLink.length() > 30) {
							if(instagramLink.length() < 44) {
								if(ic.length() < 256) {
									String postTag = insertCard(instagramLink, instagramImage, ic);
									String inLink = he.getRequestHeaders().getFirst("Host")+"/postcard.jdc?p="+postTag;
									if(!inLink.startsWith("http")) {
										inLink = "https://"+inLink;
									}
									element.setAttribute("class", "justify-container")
										.addChild(new JDCElement("h1").setAttribute("class", "cardMessage").setText("Just embed this link: ")
											.addChild(new JDCElement("span").setAttribute("id", "cardLink").setText(inLink)));
									complete = true;
								} else {
									error = "Caption must be less than 256 characters";
								}
							} else {
								error = "Post must be public";
							}
					} else {
						error = "Invalid Instagram link";
					}
				}
			} else {
				error = "Bad Instagram link";
			}
		} else {
			error = "Nonexistent post";
		}
		
		if(error.length() > 0) {
			element.setAttribute("class", "justify-container").addChild(new JDCElement("h1").setAttribute("class", "cardMessage cardError").setText(error));
		} else {
			if(!complete) {
				element.setAttribute("class", "thin-container")
					.addChild(new JDCElement("h1").setAttribute("class", "thin-title").setText("Here's Your Post Card"))
					.addChild(new JDCElement("div").setAttribute("id", "finImage").setAttribute("style", "background-image: url('"+instagramImage+"');"))
					.addChild(new JDCElement("div").setAttribute("id", "finForm")
							.addChild(new JDCElement("form").setAttribute("action", "/finishpostcard.jdc").setAttribute("class", "form-large form-full").setAttribute("method", "post")
									.addChild(new JDCElement("input").setAttribute("type", "text").setAttribute("id", "ic").setAttribute("name", "instaCap").setAttribute("placeholder", "Brief Caption").setAttribute("value", ""))
									.addChild(new JDCElement("input").setAttribute("type", "hidden").setAttribute("name", "instaLink").setAttribute("value", instagramLink))
									.addChild(new JDCElement("input").setAttribute("type", "submit").setAttribute("id", "instaS").setAttribute("name", "btnSubmit").setAttribute("class", "button-right button-hollow-fill").setAttribute("value", "Finish Card"))));
			}
		}
		
	}
	
	private String findTokenQuery = "SELECT * FROM `instatools`.`postcards` WHERE `cardID` = ?;";
	private String insertTokenQuery = "INSERT INTO `instatools`.`postcards` (`cardID`, `cardImage`, `cardCaption`, `cardRedirect`) VALUES (?, ?, ?, ?);";
	
	private String insertCard(String instagramLink, String instagramImage, String cardCaption) {
		String tryToken = "";
		boolean tokenAvailable = false;
		while(!tokenAvailable) {
			tryToken = SecurityUtils.genToken(32);
			if(DatabaseUtils.prepareAndExecute(findTokenQuery, true, tryToken).get(0).getResults().isEmpty()) {
				tokenAvailable = true;
			}
		}
		DatabaseUtils.prepareAndExecute(insertTokenQuery, true, tryToken, instagramImage, cardCaption, instagramLink);
		return tryToken;
	}
	
	
	private String generateNewToken(String page, String sessionID) {
		String tryToken = "";
		boolean tokenAvailable = false;
		while(!tokenAvailable) {
			tryToken = SecurityUtils.genToken(64);
			if(DatabaseUtils.prepareAndExecute(findTokenQuery, true, tryToken).get(0).getResults().isEmpty()) {
				tokenAvailable = true;
			}
		}
		DatabaseUtils.prepareAndExecute(insertTokenQuery, true, tryToken, page, sessionID);
		return tryToken;
	}
	
	public static String getFinalURL(String uri) {
	    try {
	    	URL url = new URL(uri);
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();
	        con.setInstanceFollowRedirects(false);
	        con.setRequestProperty("User-Agent", "Aion InstaTools/1.1");
	        con.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
	        con.addRequestProperty("Referer", "https://instatools.aionstudios.net/");
	        con.connect();
	        int resCode = con.getResponseCode();
	        if (resCode == HttpURLConnection.HTTP_SEE_OTHER
	                || resCode == HttpURLConnection.HTTP_MOVED_PERM
	                || resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
	            String Location = con.getHeaderField("Location");
	            if (Location.startsWith("/")) {
	                Location = url.getProtocol() + "://" + url.getHost() + Location;
	            }
	            return getFinalURL(Location);
	        }
	    } catch (Exception e) {
	        System.out.println(e.getMessage());
	    }
	    return uri;
	}

	@Override
	public void generateContent(JDCHeadElement element) {
		
	}

}
