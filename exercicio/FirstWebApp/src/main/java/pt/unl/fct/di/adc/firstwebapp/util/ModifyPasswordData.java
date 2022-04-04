package pt.unl.fct.di.adc.firstwebapp.util;

public class ModifyPasswordData {
	public String username;
	public String oldPassword;
	public String password;
	public String confirmation;
	
	public ModifyPasswordData() { }
	
	public ModifyPasswordData(String username, String oldPassword, String password, String confirmation) {
		this.username = username;
		this.oldPassword = oldPassword;
		this.password = password;
		this.confirmation = confirmation;
	}
	
	public boolean validPassword() {
		if(this.password == null || this.confirmation == null) {
			return true;
		}
		
		if(this.password.equals(this.confirmation)) {
			return true;
		}
		
		return false;
	}
}
