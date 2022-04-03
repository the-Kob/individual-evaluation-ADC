function load() {
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
	console.log("logout")
	
	var xmlhttp = new XMLHttpRequest();

	var iusername = localStorage.getItem("curr_user_logged");

	var objIn = {
		username: iusername
	}

	var json = JSON.stringify(objIn);
	
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				window.location.href = "/index.html";
			}
		}
	}
	
	xmlhttp.open("POST", document.location.origin + "/rest/logout/op8", true);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);
}