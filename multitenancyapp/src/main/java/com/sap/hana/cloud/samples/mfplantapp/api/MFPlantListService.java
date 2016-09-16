package com.sap.hana.cloud.samples.mfplantapp.api;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sap.cloud.account.Tenant;
import com.sap.cloud.account.TenantContext;
import com.sap.hana.cloud.samples.mfplantapp.model.MFPlant;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;

/**
 * {@link MFPlantListService}
 * 
 * @version 0.1
 */
@Path("/plantlist")
@Produces({ MediaType.APPLICATION_JSON })
public class MFPlantListService {
	@SuppressWarnings("unchecked")
	@GET
	@Path("/")
	public List<MFPlant> getMFPlantList(@Context HttpServletRequest request)
			throws PersistenceException, UnsupportedUserAttributeException {
		List<MFPlant> retVal = null;
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(
				props);
		// UserProvider provides access to the user storage
		UserProvider userProvider = UserManagementAccessor.getUserProvider();
		// Read the currently logged in user from the user storage
		User user = userProvider.getUser(request.getUserPrincipal().getName());
		String plant_id = user.getAttribute("PLANT_ID");

		if (request.getUserPrincipal() != null && request.isUserInRole("admin")) {

			if (plant_id == null) { // this means he is the admin
				retVal = em.createNamedQuery("MFPlants").getResultList();
			}
		} else if (request.getUserPrincipal() != null
				&& request.isUserInRole("user")) {
			if(plant_id!=null){
				retVal=getMFPlantsById(plant_id);
			}
		}

		return retVal;
	}

	@GET
	@Path("/{id}")
	public List<MFPlant> getMFPlantsById(@PathParam(value = "id") String id) {
		List<MFPlant> retVal = null;
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(
				props);
		Query query = em.createNamedQuery("MFPlantById");
		query.setParameter("id", id);
		retVal = query.getResultList();
		return retVal;
	}

	@GET
	@Path("/{id}/{startDate}/{endDate}")
	public List<MFPlant> getMFPlantByIdAndDate(
			@PathParam(value = "id") String id,
			@PathParam(value = "startDate") String startDate,
			@PathParam(value = "endDate") String endDate,
			@Context SecurityContext ctx) {
		List<MFPlant> retVal = null;
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(
				props);
		Query query = em.createNamedQuery("MFPlantByIdAndDate");
		query.setParameter("id", id);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		retVal = query.getResultList();
		return retVal;
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/{id}/{date}/{o3}/")
	public List<MFPlant> addMFPlant(@Context SecurityContext ctx,
			@PathParam(value = "id") String id,
			@PathParam(value = "date") Date date,
			@PathParam(value = "o3") String o3) {
		List<MFPlant> retVal = null;
		MFPlant plant = new MFPlant();
		plant.setId(id);
		plant.setDateField(date);
		plant.setO3(o3);
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(
				props);
		em.getTransaction().begin();
		em.persist(plant);
		em.getTransaction().commit();
		retVal = em.createNamedQuery("MFPlants").getResultList();
		return retVal;
	}

	@SuppressWarnings("unchecked")
	@DELETE
	@Path("/{id}")
	public List<MFPlant> removeMFPlant(@PathParam(value = "id") String id,
			@Context SecurityContext ctx) {
		List<MFPlant> retVal = null;
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(
				props);
		Query query = em.createNamedQuery("MFPlantById");
		query.setParameter("id", id);
		MFPlant city = (MFPlant) query.getSingleResult();
		if (city != null) {
			em.getTransaction().begin();
			em.remove(city);
			em.getTransaction().commit();
		}
		retVal = em.createNamedQuery("MFPlants").getResultList();
		return retVal;
	}

	/**
	 * Returns the <code>DefaultDB</code> {@link DataSource} via JNDI.
	 * 
	 * @return <code>DefaultDB</code> {@link DataSource}
	 */
	protected DataSource getDataSource() {
		DataSource retVal = null;

		try {
			InitialContext ctx = new InitialContext();
			retVal = (DataSource) ctx.lookup("java:comp/env/jdbc/DefaultDB");
		} catch (NamingException ex) {
			ex.printStackTrace();
		}

		return retVal;
	}

	/**
	 * Returns the {@link EntityManagerFactory}.
	 * 
	 * @return The {@link EntityManagerFactory}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected EntityManagerFactory getEntityManagerFactory() {
		EntityManagerFactory retVal = null;
		try {
			Map<String, DataSource> properties = new HashMap<String, DataSource>();
			DataSource ds = this.getDataSource();
			properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
			retVal = Persistence.createEntityManagerFactory("mfplantapp",
					properties);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return retVal;
	}

	@GET
	@Path("/{id}/mfplantinfo")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getMFPlantDetail(@PathParam(value = "id") String id) {
		MFPlantDetailService plantInfoService = new MFPlantDetailService();
		return plantInfoService.getMFPlantInformation(id);
	}

	@GET
	@Path("/mfplantinfo/all")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getMFPlantsDetail() {
		MFPlantDetailService plantInfoService = new MFPlantDetailService();
		return plantInfoService.getMFPlantsInformation();
	}

	protected TenantContext getTenantContext() {
		TenantContext tenantContext = null;
		try {
			InitialContext ctx = new InitialContext();
			tenantContext = (TenantContext) ctx
					.lookup("java:comp/env/TenantContext");
		} catch (NamingException ex) {
			ex.printStackTrace();
		}
		return tenantContext;
	}

}