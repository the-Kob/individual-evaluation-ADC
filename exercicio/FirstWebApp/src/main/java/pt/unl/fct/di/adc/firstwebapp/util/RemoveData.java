package pt.unl.fct.di.adc.firstwebapp.util;

public class RemoveData {
	
	// The user that removes
	public String removerUsername;
	
	// The user being removed
	public String removedUsername;
	
	public RemoveData() { }
	
	public RemoveData(String removerU, String removedU) {
		this.removerUsername = removerU;
		this.removedUsername = removedU;
	}
}
