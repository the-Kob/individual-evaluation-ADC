package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Entity;
import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.adc.firstwebapp.util.RegisterData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private static final String USER = "USER";
	private static final String INACTIVE = "INATIVO";

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public RegisterResource() {
	}

	@POST
	@Path("/op1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistration(RegisterData data) {
		LOG.fine("Register attempt by user: " + data.username);

		// Checks input data
		if (!data.validRegistration()) {
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong mandatory parameter (also, your password must have at least one number).").build();
		}

		// Checks optional attributes
		data.verifyOptionalAttributes();

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);

		Transaction txn = datastore.newTransaction();

		try {
			Entity user = txn.get(userKey);

			if (user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} else {
				user = Entity.newBuilder(userKey).set("username", data.username)
						.set("password", DigestUtils.sha512Hex(data.password)).set("email", data.email)
						.set("name", data.name).set("profileVisibility", data.profileVisibility)
						.set("homePhone", data.homePhone).set("mobilePhone", data.mobilePhone)
						.set("address", data.address).set("nif", data.nif).set("role", USER).set("state", INACTIVE)
						.build();
			}

			txn.add(user);

			LOG.info("Registration of " + data.username + " successful.");

			txn.commit();

			return Response.ok("{}").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
