package pt.unl.fct.unl.gcn.pereira.individualeval.resources;

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
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.gcn.pereira.individualeval.util.RegisterData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
	
	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public RegisterResource() { }
	
	
	/*
	 * This method is currently deprecated.
	 * It's only here for learning sake.
	 */
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistrationV1(RegisterData data) {
        LOG.fine("Register attempt by user: " + data.username);
        
        // Checks input data
        if(!data.validRegistration()) {
        	return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }
        
        // Creates an entity user from the data. The key is the username
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = Entity.newBuilder(userKey).
        		set("password", data.password).
        		set("userCreationTime", System.currentTimeMillis())
        		.build();

        datastore.put(user);
        
        LOG.info("Registration of " + data.username + " successful.");
        
        return Response.ok("{}").build();  
	}
	
	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistrationV2(RegisterData data) {
        LOG.fine("Register attempt by user: " + data.username);
        
        // Checks input data
        if(!data.validRegistration()) {
        	return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
        }
        
        Transaction txn = datastore.newTransaction();
        
        try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);
			
			if(user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} else {
				user = Entity.newBuilder(userKey)
		        		.set("password", DigestUtils.sha512Hex(data.password))
		        		.set("email", data.email)
						.set("name", data.name)
		        		.set("userCreationTime", Timestamp.now())
		        		.build();
			}
	        
	        txn.add(user);
	        
	        LOG.info("Registration of " + data.username + " successful.");
	        
	        txn.commit();
	        
	        return Response.ok("{}").build();
        } finally {
        	if(txn.isActive()) {
        		txn.rollback();
        	}
        }
	}
}