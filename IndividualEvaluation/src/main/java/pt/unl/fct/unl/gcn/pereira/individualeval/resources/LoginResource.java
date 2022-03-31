package pt.unl.fct.unl.gcn.pereira.individualeval.resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.gcn.pereira.individualeval.util.AuthToken;
import pt.unl.fct.gcn.pereira.individualeval.util.LoginData;

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
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);

		if (data.username.equals("kob") && data.password.equals("password")) {
			AuthToken at = new AuthToken(data.username);
			return Response.ok(g.toJson(at)).build();
		}

		return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
	}

	@GET
	@Path("/{username}")
	public Response checkUsernameAvailability(@PathParam("username") String username) {
		if (username.trim().equals("kob")) {
			return Response.ok().entity(g.toJson(false)).build();
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
	}

	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLoginV1(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);

		Key userKey = datastore.newKeyFactory().newKey(data.username);
		Entity user = datastore.get(userKey);

		if (user != null) {
			String hashedPwd = user.getString("user_password");
			if (hashedPwd.equals(DigestUtils.sha512Hex(data.password))) {
				AuthToken at = new AuthToken(data.username);
				LOG.info("User " + data.username + " logged in successfuly.");

				return Response.ok(g.toJson(at)).build();
			} else {
				LOG.warning("Wrong passowrd for the user " + data.username);

				return Response.status(Status.FORBIDDEN).build();
			}
		} else {
			// Username does not exist
			LOG.warning("Login attemp by user " + data.username + " has failed.");

			return Response.status(Status.FORBIDDEN).build();
		}
	}

	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLoginV2(LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
		LOG.fine("Login attempt byF user: " + data.username);

		Key userKey = datastore.newKeyFactory().newKey(data.username);
		Key ctrsKey = datastore.newKeyFactory().addAncestors(PathElement.of("User", data.username)).setKind("UserStats")
				.newKey("counters");

		Key logKey = datastore.allocateId(datastore.newKeyFactory().addAncestors(PathElement.of("User", data.username))
				.setKind("UserLog").newKey());

		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if (user == null) {
				// Username does not exist
				LOG.warning("Login attemp by user " + data.username + " has failed.");

				return Response.status(Status.FORBIDDEN).build();
			}

			// Get user stats from the storage
			Entity stats = txn.get(ctrsKey);
			if (stats == null) {
				stats = Entity.newBuilder(ctrsKey).set("user_stats_login", 0L).set("user_stats_failed", 0L)
						.set("user_first_login", Timestamp.now()).set("user_last_login", Timestamp.now()).build();
			}

			String hashedPwd = (String) user.getString("user_password");
			if (hashedPwd.equals(DigestUtils.sha512Hex(data.password))) {
				// Correct password

				// Construct the logs
				Entity log = Entity.newBuilder(logKey).set("user_login_ip", request.getRemoteAddr())
						.set("user_login_host", request.getRemoteHost()).set("user_login_ip",
								// Does not index this property value
								StringValue.newBuilder(headers.getHeaderString("X-AppEngine-CityLatLong"))
										.setExcludeFromIndexes(true).build())
						.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
						.set("user_login_country", headers.getHeaderString("X-AppEngine-Country")).build();

				// Get the user statistics and updates it
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 1L + stats.getLong("user_stats_logins")).set("user_stats_failed", 0L)
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", Timestamp.now()).build();

				txn.put(log, ustats);
				txn.commit();

				AuthToken at = new AuthToken(data.username);
				LOG.info("User " + data.username + " logged in successfuly.");

				return Response.ok(g.toJson(at)).build();
			} else {
				// Incorrect password

				Entity ustats = Entity.newBuilder(ctrsKey).set("user_stats_logins", stats.getLong("user_stats_logins"))
						.set("user_stats_failed", 1L + stats.getLong("user_stats_failed"))
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", stats.getTimestamp("user_last_login"))
						.set("user_last_attempt", Timestamp.now()).build();

				txn.put(ustats);
				txn.commit();

				LOG.warning("Wrong passowrd for the user " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());

			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();

				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response checkUsernameAvailable(LoginData data) {
		Key userKey = datastore.newKeyFactory().newKey(data.username);
		Entity user = datastore.get(userKey);
		if (user != null) {
			String hashedPwd = user.getString("password");

			if (hashedPwd.equals(DigestUtils.sha512Hex(data.password))) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, -1);
				Timestamp yesterday = Timestamp.of(cal.getTime());

				Query<Entity> query = Query.newEntityQueryBuilder().setKind("UserLog")
						.setFilter(CompositeFilter.and(
								PropertyFilter
										.hasAncestor(datastore.newKeyFactory().setKind("User").newKey(data.username)),
								PropertyFilter.ge("user_login_time", yesterday)))
						.setOrderBy(OrderBy.desc("user_login_time")).setLimit(3).build();
				// Run query
				QueryResults<Entity> logs = datastore.run(query);

				// List Results
				List<Date> loginDates = new ArrayList();
				logs.forEachRemaining(userlog -> {
					loginDates.add(userlog.getTimestamp("user_login_time").toDate());
				});

				LOG.info("Received last logins for : " + data.username);
				return Response.ok(g.toJson(loginDates)).entity("Last 24h logins for user found").build();
			} else {
				LOG.warning("Wrong password for :" + data.username);
				return Response.status(Status.FORBIDDEN).entity("Wrong password").build();
			}
		} else {// Username doesn't exist
			LOG.warning("User " + data.username + " does not exist");
			return Response.status(Status.FORBIDDEN).entity("User " + data.username + " does not exist").build();
		}
	}
}
