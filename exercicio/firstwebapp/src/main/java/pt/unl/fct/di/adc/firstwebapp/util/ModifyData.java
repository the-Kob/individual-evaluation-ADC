package pt.unl.fct.di.adc.firstwebapp.util;

import com.google.cloud.datastore.Entity;

public class ModifyData {

	// The user that modifies
	public String modifierUsername;

	// The user being modified
	public String modifiedUsername;
	public String email;
	public String name;
	public String profileVisibility;
	public String homePhone;
	public String mobilePhone;
	public String address;
	public String nif;
	public String role;
	public String state;

	public ModifyData() {
	}

	public ModifyData(String modifierU, String modifiedU, String email, String name, String profileVisibility,
			String homePhone, String mobilePhone, String address, String nif, String role, String state) {
		this.modifierUsername = modifierU;
		this.modifiedUsername = modifiedU;
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

	public void removeNulls(Entity modified) {
		if(this.email == null) {
			this.email = modified.getString("email");
		}
		
		if(this.name == null) {
			this.name = modified.getString("name");
		}
		
		if(this.profileVisibility == null) {
			this.profileVisibility = modified.getString("profileVisibility");
		}
		
		if(this.homePhone == null) {
			this.homePhone = modified.getString("homePhone");
		}
		
		if(this.mobilePhone == null) {
			this.mobilePhone = modified.getString("mobilePhone");
		}
		
		if(this.address == null) {
			this.address = modified.getString("address");
		}
		
		if(this.nif == null) {
			this.nif = modified.getString("nif");
		}
		
		if(this.role == null) {
			this.role = modified.getString("role");
		}
		
		if(this.state == null) {
			this.state = modified.getString("state");
		}
	}
}	
