package pt.unl.fct.di.adc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {

	public String username;
	public String role;
	
	// Validity
	public String tokenID; // magic random number
	public long validFrom;
	public long validTo;
	
	public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2; // 2 hours

	public AuthToken() { }
	
	public AuthToken(String username, String role) {
		this.username = username;
		this.role = role;
		this.tokenID = UUID.randomUUID().toString();
		this.validFrom = System.currentTimeMillis();
		this.validTo = this.validFrom + EXPIRATION_TIME;	
	}
}
