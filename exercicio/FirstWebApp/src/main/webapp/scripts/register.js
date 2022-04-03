function register() {

	// Mandatory fields
	var rusername = document.getElementById("username").value;
	var rpassword = document.getElementById("password").value;
	var rconfirmation = document.getElementById("confirmation").value;
	var remail = document.getElementById("email").value;
	var rname = document.getElementById("name").value;

	// Optional fields
	var rprofileVisibility;
	var profileVisibility1 = document.getElementById("pv1");
	var profileVisibility2 = document.getElementById("pv2");
	var rhomePhone = document.getElementById("homePhone").value;
	var rmobilePhone = document.getElementById("mobilePhone").value;
	var raddress = document.getElementById("address").value;
	var rnif = document.getElementById("nif").value;

	// Special case for profileVisibility since it is of input type radio
	if (profileVisibility1.checked == true) {
		rprofileVisibility = "Público";
	} else if (profileVisibility2.checked == true) {
		rprofileVisibility = "Privado";
	} else {
		rprofileVisibility = null;
	}

	// If any of the other optional fields are empty, replace them with null 	
	if (rhomePhone == "") {
		rhomePhone = null;
	}

	if (rmobilePhone == "") {
		rmobilePhone = null;
	}

	if (raddress == "") {
		raddress = null;
	}

	if (rnif == "") {
		rnif = null;
	}

	var objRegister = {
		username: rusername,
		password: rpassword,
		confirmation: rconfirmation,
		email: remail,
		name: rname,
		profileVisibility: rprofileVisibility,
		homePhone: rhomePhone,
		mobilePhone: rmobilePhone,
		address: raddress,
		nif: rnif
	}

	var json = JSON.stringify(objRegister);

	var xmlhttp = new XMLHttpRequest();

	// Processing
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				window.location.href = "/index.html";
			}
		}
	}

	xmlhttp.open("POST", document.location.origin + "/rest/register/op1", true);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);
}

