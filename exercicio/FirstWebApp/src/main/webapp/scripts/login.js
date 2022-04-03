function login() {

	var lusername = document.getElementById("username").value;
	var lpassword = document.getElementById("password").value;

	var objLogin = {
		username: lusername,
		password: lpassword
	}

	var json = JSON.stringify(objLogin);

	var xmlhttp = new XMLHttpRequest();

	// Processing
	if (xmlhttp) {
		xmlhttp.open("POST", document.location.origin + "/rest/login/op6", true);
		xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
		xmlhttp.send(json);
	}
}