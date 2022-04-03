package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import pt.unl.fct.di.adc.firstwebapp.util.RemoveData;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResource {

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

	public RemoveResource() {
	}

	@DELETE
	@Path("/op2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRemove(RemoveData data) {
		LOG.fine("Removal attempt by user: " + data.removerUsername + ". Target user: " + data.removedUsername);

		Key removerKey = datastore.newKeyFactory().setKind("User").newKey(data.removerUsername);
		Key removedKey = datastore.newKeyFactory().setKind("User").newKey(data.removedUsername);

		Key removerTokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(data.removerUsername);
		Key removedTokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(data.removedUsername);

		Transaction txn = datastore.newTransaction();

		try {
			Entity remover = txn.get(removerKey);
			Entity removed = txn.get(removedKey);

			if (remover == null) {
				txn.rollback();

				LOG.warning("Remover " + data.removerUsername + " does not exist.");

				return Response.status(Status.FORBIDDEN).build();
			}

			if (removed == null) {
				txn.rollback();

				LOG.warning("To be removed " + data.removedUsername + " does not exist.");

				return Response.status(Status.FORBIDDEN).build();
			}

			Entity token = txn.get(removerTokenKey);

			// Verify if remover is logged in
			if (token == null) {
				txn.rollback();

				LOG.warning("Remover " + data.removerUsername + " is not logged in.");

				return Response.status(Status.FORBIDDEN).build();
			}

			// Verify if login has expired
			if (!isTokenValid(token)) {
				LogoutResource lr = new LogoutResource();

				lr.processLogout(data.removerUsername);

				txn.rollback();

				LOG.warning("Remover " + data.removerUsername + " session has expired. Please re-login.");

				return Response.status(Status.FORBIDDEN).build();
			}

			// Verify if remover's account is active
			if (remover.getString("state").equals(INACTIVE)) {
				txn.rollback();

				LOG.warning("Remover " + data.removerUsername + " is not active.");

				return Response.status(Status.FORBIDDEN).build();
			}

			// If the removal conditions are correct, then the remover removes the to be
			// removed
			if (verifyRemoval(remover, data.removerUsername, removed, data.removedUsername)) {
				txn.delete(removedKey);
				txn.delete(removedTokenKey);

				LOG.info("User " + data.removedUsername + " has been successfuly removed.");

				txn.commit();

				return Response.ok("{}").build();
			} else {
				txn.rollback();

				LOG.warning("The removal conditions were not met.");

				return Response.status(Status.FORBIDDEN).build();
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	private boolean verifyRemoval(Entity remover, String removerU, Entity removed, String removedU) {
		if (remover.getString("role").equals(USER) && removerU.equals(removedU)) {
			return true;
		}

		if (remover.getString("role").equals(GBO) && removed.getString("role").equals(USER)) {
			return true;
		}

		if (remover.getString("role").equals(GS)
				&& (removed.getString("role").equals(GBO) || removed.getString("role").equals(USER))) {
			return true;
		}

		if (remover.getString("role").equals(SU)) {
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
