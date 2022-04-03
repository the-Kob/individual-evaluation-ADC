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
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.adc.firstwebapp.util.ModifyData;

@Path("/modifyall")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyAttributesResource {

	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(RemoveResource.class.getName());

	private static final String USER = "USER";
	private static final String GBO = "GBO";
	private static final String GS = "GS";
	private static final String SU = "SU";

	private static final String INACTIVE = "INATIVO";

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ModifyAttributesResource() {
	}

	@POST
	@Path("/op4")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyAll(ModifyData data) {
		LOG.fine("Modify attempt by user: " + data.modifierUsername + ". Target user: " + data.modifiedUsername);

		Key modifierKey = datastore.newKeyFactory().setKind("User").newKey(data.modifierUsername);
		Key modifiedKey = datastore.newKeyFactory().setKind("User").newKey(data.modifiedUsername);

		Key modifierTokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(data.modifierUsername);

		Transaction txn = datastore.newTransaction();

		try {
			Entity modifier = txn.get(modifierKey);
			Entity modified = txn.get(modifiedKey);

			if (modifier == null) {
				txn.rollback();

				LOG.warning("Modifier " + data.modifierUsername + " does not exist.");

				return Response.status(Status.FORBIDDEN).build();
			}

			if (modified == null) {
				txn.rollback();

				LOG.warning("To be modified " + data.modifierUsername + " does not exist.");

				return Response.status(Status.FORBIDDEN).build();
			}

			Entity token = txn.get(modifierTokenKey);

			// Verify if modifier is logged in
			if (token == null) {
				txn.rollback();

				LOG.warning("Modifier " + data.modifierUsername + " is not logged in.");

				return Response.status(Status.FORBIDDEN).build();
			}

			// Verify if login has expired
			if (!isTokenValid(token)) {
				LogoutResource lr = new LogoutResource();

				lr.processLogout(data.modifierUsername);

				txn.rollback();

				LOG.warning("Modifier " + data.modifierUsername + " session has expired. Please re-login.");

				return Response.status(Status.FORBIDDEN).build();
			}

			// Verify if modifier's account is active
			if (modifier.getString("state").equals(INACTIVE)) {
				txn.rollback();

				LOG.warning("Modifier " + data.modifierUsername + " is not active.");

				return Response.status(Status.FORBIDDEN).build();
			}

			if (verifyModify(modifier, data.modifierUsername, modified, data.modifiedUsername)) {
				switch (modifier.getString("role")) {
				case USER:
					if (data.email != null || data.name != null || data.role != null) {
						txn.rollback();

						LOG.warning("Modifier " + data.modifierUsername
								+ " tried to change restricted attributes (email, name or role).");

						return Response.status(Status.FORBIDDEN).build();
					}
					break;
				case GBO:
					if (data.role != null || (data.state != null && !modified.getString("role").equals(USER))) {
						txn.rollback();

						LOG.warning("Modifier " + data.modifierUsername
								+ " tried to change restricted attributes (role or state).");

						return Response.status(Status.FORBIDDEN).build();
					}
					break;
				case GS:
					if ((data.role.equals(GBO) && !modified.getString("role").equals(USER))
							|| (data.state != null && !modified.getString("role").equals(GBO))) {
						txn.rollback();

						LOG.warning("Modifier " + data.modifierUsername + " tried to change restricted attributes.");

						return Response.status(Status.FORBIDDEN).build();
					}
					break;
				default:
					break;
				}

				// Removes possible nulls by the original values of the user to be modified
				data.removeNulls(modified);

				modified = Entity.newBuilder(modifiedKey).set("username", modified)
						.set("password", modified.getString("password")).set("email", data.email).set("name", data.name)
						.set("profileVisibility", data.profileVisibility).set("homePhone", data.homePhone)
						.set("mobilePhone", data.mobilePhone).set("address", data.address).set("nif", data.nif)
						.set("role", data.role).set("state", data.state).build();

				txn.put(modified);

				LOG.info("Modification of " + data.modifiedUsername + " successful.");

				txn.commit();

				return Response.ok("{}").build();
			} else {
				txn.rollback();

				LOG.warning("The modification conditions were not met.");

				return Response.status(Status.FORBIDDEN).build();
			}

		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	private boolean verifyModify(Entity modifier, String modifierU, Entity modified, String modifiedU) {
		if (modifier.getString("role").equals(USER) && modifierU.equals(modifiedU)) {
			return true;
		}

		if (modifier.getString("role").equals(GBO) && modified.getString("role").equals(USER)) {
			return true;
		}

		if (modifier.getString("role").equals(GS)
				&& (modified.getString("role").equals(GBO) || modified.getString("role").equals(USER))) {
			return true;
		}

		if (modifier.getString("role").equals(SU) && !modified.getString("role").equals(SU)) {
			return true;
		}

		return false;
	}

	private boolean isTokenValid(Entity token) {
		long currentTime = System.currentTimeMillis();

		if (token.getLong("validTo") < currentTime) {
			return false;
		}

		return true;
	}
}
