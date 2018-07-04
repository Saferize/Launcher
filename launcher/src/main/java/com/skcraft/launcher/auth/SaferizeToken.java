package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaferizeToken {
		
	private String parentEmail;
	private String userToken;
	
	

	public SaferizeToken(String parentEmail, String userToken) {
		setParentEmail(parentEmail);
		setUserToken(userToken);
	}
}
