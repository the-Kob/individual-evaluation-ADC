package pt.unl.fct.gcn.pereira.individualeval.util;

public class LoginData {

	public String username;
	public String password;

	public LoginData() { }

	public LoginData(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public boolean validLogin() {
		
		return true;
	}
}