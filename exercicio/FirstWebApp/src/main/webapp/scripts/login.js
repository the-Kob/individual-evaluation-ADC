function login() {
	var lusername = document.getElementById("username").value;
	var lpassword = document.getElementById("password").value;

	var objLogin = {
		username: lusername,
		password: lpassword
	}

	var json = JSON.stringify(objLogin);

	var xmlhttp = new XMLHttpRequest();

	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				localStorage.setItem("curr_user_logged", lusername);
				window.location.href = "/html/in.html";
			}
		}
	}
	
	xmlhttp.open("POST", document.location.origin + "/rest/login/op6", true);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);
}