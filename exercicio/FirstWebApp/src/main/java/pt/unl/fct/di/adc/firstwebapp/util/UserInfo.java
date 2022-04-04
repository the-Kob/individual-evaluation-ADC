package pt.unl.fct.di.adc.firstwebapp.util;

public class UserInfo {

	public String username;
	public String email;
	public String name;
	public String profileVisibility;
	public String homePhone;
	public String mobilePhone;
	public String address;
	public String nif;
	public String role;
	public String state;

	public UserInfo() { }
	
	public UserInfo(String username, String email, String name,
						String profileVisibility, String homePhone, String mobilePhone, String address, String nif, String role, String state) {
		this.username = username;
		this.email = email;
		this.name = name;
		this.profileVisibility = profileVisibility;
		this.homePhone = homePhone;
		this.mobilePhone = mobilePhone;
		this.address = address;
		this.nif = nif;
		this.role = role;
		this.state = state;
	}
}
