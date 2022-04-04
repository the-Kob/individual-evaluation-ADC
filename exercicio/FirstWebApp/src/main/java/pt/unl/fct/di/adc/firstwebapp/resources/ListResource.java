package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.adc.firstwebapp.util.ListData;

@Path("/list")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListResource {

	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private static final String USER = "USER";
	private static final String GBO = "GBO";
	private static final String GS = "GS";
	private static final String SU = "SU";

	private static final String INACTIVE = "INATIVO";
	private static final String ACTIVE = "ATIVO";

	private static final String PUBLIC = "Público";

	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ListResource() {
	}

	@GET
	@Path("/op3")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doList(ListData data) {
		LOG.fine("List attempt by user: " + data.username);

		Transaction txn = datastore.newTransaction();

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Key tokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(data.username);

		try {
			Entity user = txn.get(userKey);

			if (user == null) {
				txn.rollback();

				LOG.warning("User " + data.username + " does not exist.");

				return Response.status(Status.FORBIDDEN).build();
			}

			Entity token = txn.get(tokenKey);

			// Verify if modifier is logged in
			if (token == null) {
				txn.rollback();

				LOG.warning("User " + data.username + " is not logged in.");

				return Response.status(Status.FORBIDDEN).build();
			}

			// Verify if login has expired
			if (!isTokenValid(token)) {
				LogoutResource lr = new LogoutResource();

				lr.processLogout(data.username);

				txn.rollback();

				LOG.warning("User " + data.username + " session has expired. Please re-login.");

				return Response.status(Status.FORBIDDEN).build();
			}

			// Verify if modifier's account is active
			if (user.getString("state").equals(INACTIVE)) {
				txn.rollback();

				LOG.warning("User " + data.username + " is not active.");

				return Response.status(Status.FORBIDDEN).build();
			}

			List<String> l = new ArrayList<String>();

			switch (user.getString("role")) {
			case USER:
				Query<Entity> queryUSER = Query.newEntityQueryBuilder().setKind("User")
						.setFilter(CompositeFilter.and(PropertyFilter.eq("role", USER),
								PropertyFilter.eq("profileVisibility", PUBLIC), PropertyFilter.eq("state", ACTIVE)))
						.build();

				QueryResults<Entity> usersUSER = datastore.run(queryUSER);

				usersUSER.forEachRemaining(userList -> {
					l.add("{ username:" + userList.getString("username") + ", email:" + userList.getString("email")
							+ ", name:" + userList.getString("name") + " }");
				});
				break;
			case GBO:
				Query<Entity> queryGBO = Query.newEntityQueryBuilder().setKind("User")
						.setFilter(PropertyFilter.eq("role", USER)).build();

				QueryResults<Entity> usersGBO = datastore.run(queryGBO);

				usersGBO.forEachRemaining(userList -> {
					l.add("{ username:" + userList.getString("username") + ", email:" + userList.getString("email")
							+ ", name:" + userList.getString("name") + ", profileVisibility:"
							+ userList.getString("profileVisibility") + ", homePhone:" + userList.getString("homePhone")
							+ ", mobilePhone:" + userList.getString("mobilePhone") + ", address:"
							+ userList.getString("address") + ", nif:" + userList.getString("nif") + ", role:"
							+ userList.getString("role") + ", state:" + userList.getString("state") + " }");
				});
				break;
			case GS:
				Query<Entity> queryGS_USER = Query.newEntityQueryBuilder().setKind("User")
						.setFilter(PropertyFilter.eq("role", USER)).build();

				QueryResults<Entity> usersGS_USER = datastore.run(queryGS_USER);

				usersGS_USER.forEachRemaining(userList -> {
					l.add("{ username:" + userList.getString("username") + ", email:" + userList.getString("email")
							+ ", name:" + userList.getString("name") + ", profileVisibility:"
							+ userList.getString("profileVisibility") + ", homePhone:" + userList.getString("homePhone")
							+ ", mobilePhone:" + userList.getString("mobilePhone") + ", address:"
							+ userList.getString("address") + ", nif:" + userList.getString("nif") + ", role:"
							+ userList.getString("role") + ", state:" + userList.getString("state") + " }");
				});

				Query<Entity> queryGS_GBO = Query.newEntityQueryBuilder().setKind("User")
						.setFilter(PropertyFilter.eq("role", GBO)).build();

				QueryResults<Entity> usersGS_GBO = datastore.run(queryGS_GBO);

				usersGS_GBO.forEachRemaining(userList -> {
					l.add("{ username:" + userList.getString("username") + ", email:" + userList.getString("email")
							+ ", name:" + userList.getString("name") + ", profileVisibility:"
							+ userList.getString("profileVisibility") + ", homePhone:" + userList.getString("homePhone")
							+ ", mobilePhone:" + userList.getString("mobilePhone") + ", address:"
							+ userList.getString("address") + ", nif:" + userList.getString("nif") + ", role:"
							+ userList.getString("role") + ", state:" + userList.getString("state") + " }");
				});
				break;
			case SU:
				Query<Entity> querySU = Query.newEntityQueryBuilder().setKind("User").build();

				QueryResults<Entity> usersSU = datastore.run(querySU);

				usersSU.forEachRemaining(userList -> {
					l.add("{ username:" + userList.getString("username") + ", email:" + userList.getString("email")
							+ ", name:" + userList.getString("name") + ", profileVisibility:"
							+ userList.getString("profileVisibility") + ", homePhone:" + userList.getString("homePhone")
							+ ", mobilePhone:" + userList.getString("mobilePhone") + ", address:"
							+ userList.getString("address") + ", nif:" + userList.getString("nif") + ", role:"
							+ userList.getString("role") + ", state:" + userList.getString("state") + " }");
				});
				break;
			default:
				break;
			}

			LOG.info("Listing by user " + data.username + " was successful.");

			return Response.ok(g.toJson(l)).build();

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
