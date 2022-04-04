package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.gson.Gson;

import pt.unl.fct.di.adc.firstwebapp.util.ShowSelfData;
import pt.unl.fct.di.adc.firstwebapp.util.UserInfo;

@Path("/showself")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowSelfResource {

	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ShowSelfResource() {
	}

	@POST
	@Path("/op9")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response showSelf(ShowSelfData data) {
		LOG.fine("Attempting to show user " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);

		Key tokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(data.username);
		Entity token = datastore.get(tokenKey);

		if (user == null) {
			LOG.warning("User " + data.username + " does not exist.");

			return Response.status(Status.FORBIDDEN).build();
		}

		if (token == null) {
			LOG.warning("User " + data.username + " is not logged in.");

			return Response.status(Status.FORBIDDEN).build();
		}

		if (!isTokenValid(token)) {
			LOG.warning("User " + data.username + "  session has expired.");

			return Response.status(Status.FORBIDDEN).build();
		}

		UserInfo u = new UserInfo(user.getString("username"), user.getString("email"), user.getString("name"),
				user.getString("profileVisibility"), user.getString("homePhone"), user.getString("mobilePhone"),
				user.getString("address"), user.getString("nif"), user.getString("role"), user.getString("state"));

		return Response.ok(g.toJson(u)).build();

	}

	private boolean isTokenValid(Entity token) {
		long currentTime = System.currentTimeMillis();

		if (token.getLong("validTo") < currentTime) {
			return false;
		}

		return true;
	}
}
