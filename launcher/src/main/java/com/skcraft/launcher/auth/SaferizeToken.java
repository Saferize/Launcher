package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.persistence.Scrambled;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Scrambled("SAFERIZE_TOKEN")
public class SaferizeToken {
		
	private String parentEmail;
	private String userToken;
	
	

	public SaferizeToken(String parentEmail, String userToken) {
		setParentEmail(parentEmail);
		setUserToken(userToken);
	}



	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}



	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	
}
