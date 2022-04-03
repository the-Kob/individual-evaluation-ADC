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

import pt.unl.fct.di.adc.firstwebapp.util.LogoutData;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LogoutResource() {
	}

	@DELETE
	@Path("/op8")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogout(LogoutData data) {
		LOG.fine("User " + data.username + " is attempting to logout.");

		Transaction txn = datastore.newTransaction();

		Key tokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(data.username);

		try {
			Entity token = txn.get(tokenKey);

			// Verify if user is not logged in
			if (token == null) {
				txn.rollback();

				LOG.warning("User " + data.username + " is not logged in.");

				return Response.status(Status.FORBIDDEN).build();
			}

			txn.delete(tokenKey);

			LOG.info("User " + data.username + " has successfuly logged out.");

			txn.commit();

			return Response.ok("{}").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	public void processLogout(String username) {
		LogoutData data = new LogoutData(username);

		doLogout(data);
	}

}
