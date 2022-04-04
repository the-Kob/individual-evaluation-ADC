function loadInfo() {
	var xmlhttp = new XMLHttpRequest();

	var iusername = localStorage.getItem("curr_user_logged");

	var objIn = {
		username: iusername
	}

	var json = JSON.stringify(objIn);

	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				var inObj = JSON.parse(this.responseText);

				document.getElementById("info_role").innerHTML = inObj.role;
				document.getElementById("info_user").innerHTML = inObj.username;
				document.getElementById("info_email").innerHTML = inObj.email;
				document.getElementById("info_name").innerHTML = inObj.name;
				document.getElementById("info_vis").innerHTML = inObj.profileVisibility;
				document.getElementById("info_hp").innerHTML = inObj.homePhone;
				document.getElementById("info_mp").innerHTML = inObj.mobilePhone;
				document.getElementById("info_address").innerHTML = inObj.address;
				document.getElementById("info_nif").innerHTML = inObj.nif;

				document.getElementById("role_color").innerHTML = document.getElementById("info_role").innerHTML;
				
				if(document.getElementById("role_color").innerHTML == "SU") {
					document.getElementById("role_color").style.backgroundColor = "red";
				} else if(document.getElementById("role_color").innerHTML == "GS") {
					document.getElementById("role_color").style.backgroundColor = "orange";
				} else if(document.getElementById("role_color").innerHTML == "GBO") {
					document.getElementById("role_color").style.backgroundColor = "yellow";
				} else {
					document.getElementById("role_color").style.backgroundColor = "green";
				}
			}
		}
	}

	xmlhttp.open("POST", document.location.origin + "/rest/showself/op9", true);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);

	document.getElementById("options").onchange();

	loadToken();
}

function changeDivs() {
	var selectedValue = document.getElementById("options").value;
	
	document.getElementById("logout-area").style.visibility = "hidden";
	document.getElementById("user-area").style.visibility = "hidden";
	document.getElementById("list-area").style.visibility = "hidden";
	document.getElementById("change-user-area").style.visibility = "hidden";
	document.getElementById("change-pass-area").style.visibility = "hidden";
	document.getElementById("token-area").style.visibility = "hidden";

	if (selectedValue == "logout") {
		document.getElementById("logout-area").style.visibility = "visible";
	}
	if (selectedValue == "remove") {
		document.getElementById("user-area").style.visibility = "visible";
	} 
	if (selectedValue == "list") {
		document.getElementById("list-area").style.visibility = "visible";
	} 
	if (selectedValue == "change-info") {
		document.getElementById("change-user-area").style.visibility = "visible";
	} 
	if (selectedValue == "change-pass") {
		document.getElementById("change-pass-area").style.visibility = "visible";
	}
	if (selectedValue == "token") {
		document.getElementById("token-area").style.visibility = "visible";
	}
}

function logout() {
	var xmlhttp = new XMLHttpRequest();

	var iusername = localStorage.getItem("curr_user_logged");

	var objIn = {
		username: iusername
	}

	var json = JSON.stringify(objIn);
	
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				alert("Logged out!");
				window.location.href = "/index.html";
			} else {
				alert(xmlhttp.responseText);
			}
		}
	}
	
	xmlhttp.open("POST", document.location.origin + "/rest/logout/op8", true);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);
}

function loadToken() {
	var xmlhttp = new XMLHttpRequest();

	var tusername = localStorage.getItem("curr_user_logged");

	var objIn = {
		username: tusername
	}

	var json = JSON.stringify(objIn);
	
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				var inObj = JSON.parse(this.responseText);
				
				document.getElementById("token_role").innerHTML = inObj.role;
				document.getElementById("token_user").innerHTML = inObj.username;
				document.getElementById("token_id").innerHTML = inObj.tokenID;
				document.getElementById("token_from").innerHTML = inObj.validFrom;
				document.getElementById("token_to").innerHTML = inObj.validTo;
			}
		}
	}
	
	xmlhttp.open("POST", document.location.origin + "/rest/showtoken/op7", true);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);
}

function changePwd() {
	var xmlhttp = new XMLHttpRequest();

	var pusername = localStorage.getItem("curr_user_logged");
	var ppassword = document.getElementById("change-prev-password").value;
	var pnewpassword = document.getElementById("change-password").value;
	var pconfirmation = document.getElementById("change-password-conf").value;
	
	if (pnewpassword == "") {
		pnewpassword = null;
	}
	
	if (pconfirmation == "") {
		pconfirmation = null;
	}
	
	var objIn = {
		username: pusername,
		oldPassword: ppassword,
		password: pnewpassword,
		confirmation: pconfirmation
	}

	var json = JSON.stringify(objIn);
	
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				alert("Password was changed successfully.");
				
				ppassword = null;
				pnewpassword = null;
				pconfirmation = null;
			} else {
				alert(xmlhttp.responseText);
			}
		}
	}
	
	xmlhttp.open("POST", document.location.origin + "/rest/modifypwd/op5", true);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);
}

function changeUserInfo() {
	var xmlhttp = new XMLHttpRequest();

	var uimodifier = localStorage.getItem("curr_user_logged");
	var uimodified = document.getElementById("change-user-username").value;
	var uiemail = document.getElementById("change-user-email").value;
	var uiname = document.getElementById("change-user-name").value;
	var uivis;
	var uivis1 = document.getElementById("pv1");
	var uivis2 = document.getElementById("pv2");
	var uihp = document.getElementById("change-user-hp").value;
	var uimp = document.getElementById("change-user-mp").value;
	var uiaddress = document.getElementById("change-user-address").value;
	var uinif = document.getElementById("change-user-nif").value;
	var uirole = document.getElementById("change-user-role").value;
	var uistate = document.getElementById("change-user-state").value;
		
	if (uivis1.checked == true) {
		uivis = "Público";
	} else if (uivis2.checked == true) {
		uivis = "Privado";
	} else {
		uivis = null;
	}
	
	if (uiemail == "") {
		uiemail = null;
	}

	if (uiname == "") {
		uiname = null;
	}

	if (uiaddress == "") {
		uiaddress = null;
	}

	if (uinif == "") {
		uinif = null;
	}
		
	if (uihp == "") {
		uihp = null;
	}

	if (uimp == "") {
		uimp = null;
	}

	if (uirole == "") {
		uirole = null;
	}

	if (uistate == "") {
		uistate = null;
	}
	
	var objIn = {
		modifierUsername: uimodifier,
		modifiedUsername: uimodified,
		email: uiemail,
		name: uiname,
		profileVisibility: uivis,
		homePhone: uihp,
		mobilePhone: uimp,
		address: uiaddress,
		nif: uinif,
		role: uirole,
		state: uistate
	}

	var json = JSON.stringify(objIn);
	
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				alert("User was changed successfully.");
				
				// Reset values
				uimodified = null;
				uiemail = null;
				uiname = null;
				uivis1.checked = false;
				uivis2.checked = false;
				uihp = null;
				uimp = null;
				uiaddress = null;
				uinif = null;
				uirole = null;
				uistate = null;
			} else {
				alert(xmlhttp.responseText);
			}
		}
	}
	
	xmlhttp.open("POST", document.location.origin + "/rest/modifyall/op4", true);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);
}

function removeUser() {
	var xmlhttp = new XMLHttpRequest();

	var uremover = localStorage.getItem("curr_user_logged");
	var uremoved = document.getElementById("removed-user").value;
	
	var objIn = {
		removerUsername: uremover,
		removedUsername: uremoved
	}

	var json = JSON.stringify(objIn);
	
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				alert("User was removed successfully.");
				if(uremover == uremoved) {
					window.location.href = "/index.html";
				}
				
				uremoved = null;
			} else {
				alert(xmlhttp.responseText);
			}
		}
	}
	
	xmlhttp.open("DELETE", document.location.origin + "/rest/remove/op2", true);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);
}

function list() {
	var xmlhttp = new XMLHttpRequest();

	var lusername = localStorage.getItem("curr_user_logged");

	var objIn = {
		username: lusername
	}

	var json = JSON.stringify(objIn);
	
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				alert(xmlhttp.responseText);
			} else {
				alert(xmlhttp.responseText);
			}
		}
	}
	
	xmlhttp.open("POST", document.location.origin + "/rest/list/op3", true);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);
}