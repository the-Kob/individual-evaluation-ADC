package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.adc.firstwebapp.util.ModifyPasswordData;

@Path("/modifypwd")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyPasswordResource {
	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private static final String INACTIVE = "INATIVO";

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ModifyPasswordResource() {
	}

	@POST
	@Path("/op5")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyPassword(ModifyPasswordData data) {
		LOG.fine("Modify password attempt by user: " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);

		Key userTokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(data.username);

		Transaction txn = datastore.newTransaction();

		try {
			Entity user = txn.get(userKey);

			if (user == null) {
				txn.rollback();

				LOG.warning("User " + data.username + " does not exist.");

				return Response.status(Status.FORBIDDEN).build();
			}

			Entity token = txn.get(userTokenKey);

			// Verify if modifier is logged in
			if (token == null) {
				txn.rollback();

				LOG.warning("Modifier " + data.username + " is not logged in.");

				return Response.status(Status.FORBIDDEN).build();
			}

			// Verify if login has expired
			if (!isTokenValid(token)) {
				LogoutResource lr = new LogoutResource();

				lr.processLogout(data.username);

				txn.rollback();

				LOG.warning("Modifier " + data.username + " session has expired. Please re-login.");

				return Response.status(Status.FORBIDDEN).build();
			}

			// Verify if modifier's account is active
			if (user.getString("state").equals(INACTIVE)) {
				txn.rollback();

				LOG.warning("Modifier " + data.username + " is not active.");

				return Response.status(Status.FORBIDDEN).build();
			}

			String hashedOldPwd = user.getString("password");

			if (!hashedOldPwd.equals(DigestUtils.sha512Hex(data.oldPassword))) {
				txn.rollback();

				LOG.warning("Wrong password for user: " + data.username);

				return Response.status(Status.FORBIDDEN).build();
			}

			if (!data.validPassword()) {
				txn.rollback();

				LOG.warning("The new password and it's confirmation don't match.");

				return Response.status(Status.FORBIDDEN).build();
			}

			user = Entity.newBuilder(userKey).set("username", user.getString("username"))
					.set("password", DigestUtils.sha512Hex(data.password)).set("email", user.getString("email"))
					.set("name", user.getString("name")).set("profileVisibility", user.getString("profileVisibility"))
					.set("homePhone", user.getString("homePhone")).set("mobilePhone", user.getString("mobilePhone"))
					.set("address", user.getString("address")).set("nif", user.getString("nif"))
					.set("role", user.getString("role")).set("state", user.getString("state")).build();

			txn.put(user);

			LOG.info("Passowrd modification of " + data.username + " successful.");

			txn.commit();

			return Response.ok("{}").build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	private boolean isTokenValid(Entity token) {
		long currentTime = System.currentTimeMillis();

		if (token.getLong("validTo") < currentTime) {
			return false;
		}

		return true;
	}
}
