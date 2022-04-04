package pt.unl.fct.di.adc.firstwebapp.util;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;

public class LoginData {

	public String username;
	public String password;
	public String role;

	public LoginData() { }

	public LoginData(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public boolean validLogin(String hashedPwd) {
		String hPassword = DigestUtils.sha512Hex(this.password);
		
		if(!hashedPwd.equals(hPassword)) {
			return false;
		}
		
		return true;
	}
}
