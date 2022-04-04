package pt.unl.fct.di.adc.firstwebapp.util;

public class RegisterData {
	
	private static final String UNDEFINED = "INDEFINIDO";
	
	// Mandatory attributes
	public String username;
	public String password;
	public String confirmation;
	public String email;
	public String name;
	
	// Optional attributes ("INDEFINIDO" by default)
	public String profileVisibility;
	public String homePhone;
	public String mobilePhone;
	public String address;
	public String nif;

	public RegisterData() { }
	
	public RegisterData(String username, String password, String confirmation, String email, String name,
						String profileVisibility, String homePhone, String mobilePhone, String address, String nif) {
		this.username = username;
		this.password = password;
		this.confirmation = confirmation;
		this.email = email;
		this.name = name;
		this.profileVisibility = profileVisibility;
		this.homePhone = homePhone;
		this.mobilePhone = mobilePhone;
		this.address = address;
		this.nif = nif;
	}
	
	public boolean validRegistration() {
		// Checks for missing info
		if(this.username == null || this.password == null || this.confirmation == null || this.email == null || this.name == null) {
			return false;
		}
		
		// Checks for empty data
		if(this.username.length() == 0 || this.password.length() == 0 || this.confirmation.length() == 0 || this.email.length() == 0 || this.name.length() == 0) {
			return false;
		}
		
		// Check for confirmation
		if(!this.password.equals(this.confirmation)) {
			return false;
		}
		
		// Check if there is at least 1 number in the password
		char[] pass = this.password.toCharArray();
		StringBuilder checkPass = new StringBuilder();
		
		for(char c: pass) {
			if(Character.isDigit(c)) {
				checkPass.append(c);
			}
		}
		
		if(checkPass.length() == 0) {
			return false;
		}
		
		return true;
	}
	
	// If any of the optional attributes are null, set them to the default value
	public void verifyOptionalAttributes() {
		if(this.profileVisibility == null) {
			this.profileVisibility = UNDEFINED;
		}
		
		if(this.homePhone == null) {
			this.homePhone = UNDEFINED;
		}
		
		if(this.mobilePhone == null) {
			this.mobilePhone = UNDEFINED;
		}
		
		if(this.address == null) {
			this.address = UNDEFINED;
		}
		
		if(this.nif == null) {
			this.nif = UNDEFINED;
		}
	}
}