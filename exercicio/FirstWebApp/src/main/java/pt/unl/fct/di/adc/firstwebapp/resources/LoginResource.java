package pt.unl.fct.di.adc.firstwebapp.resources;

// import java.util.ArrayList;
// import java.util.Calendar;
// import java.util.Date;
// import java.util.List;
import java.util.logging.Logger;

// import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
// import javax.ws.rs.core.Context;
// import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

// import com.google.cloud.datastore.StringValue;
// import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
// import com.google.cloud.datastore.StructuredQuery.OrderBy;
// import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
// import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
// import com.google.cloud.datastore.PathElement;
// import com.google.cloud.datastore.Query;
// import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LoginResource() {
	}

	@POST
	@Path("/op6")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);

		Transaction txn = datastore.newTransaction();

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Key tokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(data.username);

		try {
			Entity user = txn.get(userKey);

			if (user == null) {
				txn.rollback();

				LOG.warning("User " + data.username + " does not exist.");

				return Response.status(Status.FORBIDDEN).build();
			} else {
				String hashedPwd = user.getString("password");

				if (!hashedPwd.equals(DigestUtils.sha512Hex(data.password))) {
					txn.rollback();

					LOG.warning("Wrong password for user: " + data.username);

					return Response.status(Status.FORBIDDEN).build();
				} else {
					AuthToken at = new AuthToken(data.username, user.getString("role"));

					Entity token = txn.get(tokenKey);

					// Verify if user is already logged in AND the token is still valid.
					// If the token is invalid, then the previous expired token will be replaced
					// with a new one
					if (token != null && isTokenValid(token)) {
						txn.rollback();

						LOG.warning("User " + data.username + "  is already logged in.");

						return Response.status(Status.FORBIDDEN).build();
					}

					token = Entity.newBuilder(tokenKey).set("username", at.username).set("role", at.role)
							.set("tokenId", at.tokenID).set("validFrom", at.validFrom).set("validTo", at.validTo)
							.build();

					// Put the token so if there was another token connected to a previous session,
					// it should be replaced with a new one
					txn.put(token);

					LOG.info("User " + data.username + " has successfuly logged in.");

					txn.commit();

					return Response.ok(g.toJson(at)).build();
				}
			}
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
