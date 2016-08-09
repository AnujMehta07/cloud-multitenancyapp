package com.sap.hana.cloud.samples.mfplantapp.api;

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
import com.sap.hana.cloud.samples.mfplantapp.model.MFPlantLocation;

/**
 * {@link MFPlantListService}
 * 
 * @version 0.1
 */
@Path("/locations")
@Produces({ MediaType.APPLICATION_JSON })
public class MFPlantListService 
{
	@SuppressWarnings("unchecked")
	@GET
	@Path("/")
	public List<MFPlantLocation> getMFPlantLists(@Context HttpServletRequest request)
	{
		List<MFPlantLocation> retVal = null;		
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		retVal = em.createNamedQuery("MFPlantLocations").getResultList();
		return retVal;
	}
	
	@GET
	@Path("/{id}")
	public MFPlantLocation getMFPlantById(@PathParam(value = "id") String id, @Context SecurityContext ctx)
	{
		MFPlantLocation retVal = null;
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		try
		{
			Query query = em.createNamedQuery("MFPlantLocationById");
			query.setParameter("id", id);
			retVal = (MFPlantLocation) query.getSingleResult();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			em.close();
		}
		return retVal;
	}
	
	@GET
	@Path("/owner")
	public List<MFPlantLocation> getMFPlantByOwner(@Context HttpServletRequest req)
	{
		List<MFPlantLocation> retVal = null;
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();	
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		try
		{
			Query query = em.createNamedQuery("MFPlantLocationByOwner");
			query.setParameter("owner", req.getUserPrincipal().getName());
			retVal = query.getResultList();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			em.close();
		}	
		
		return retVal;
	}
	@SuppressWarnings("unchecked")
	@POST
	@Path("/{id}/{name}/{countryCode}/{owner}")
	public List<MFPlantLocation> addMFPlant(@Context SecurityContext ctx,
			@PathParam(value = "id") String id,
			@PathParam(value = "name") String name,
			@PathParam(value = "countryCode") String countryCode,
			@PathParam(value = "owner") String owner) {
		List<MFPlantLocation> retVal = null;
		MFPlantLocation city = new MFPlantLocation();
		city.setId(id);
		city.setName(name);
		city.setCountryCode(countryCode);
		city.setOwner(owner);
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(
				props);
		try {
			em.getTransaction().begin();
			em.persist(city);
			em.getTransaction().commit();

			retVal = em.createNamedQuery("MFPlantLocations").getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			em.close();
		}

		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	@DELETE
	@Path("/{id}")
	public List<MFPlantLocation> removeMFPlant(@PathParam(value = "id") String id, @Context SecurityContext ctx)
	{
		List<MFPlantLocation> retVal = null;
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);	
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		try
		{
			Query query = em.createNamedQuery("MFPlantLocationById");
			query.setParameter("id", id);
			MFPlantLocation city = (MFPlantLocation) query.getSingleResult();		
			if (city != null)
			{
				em.getTransaction().begin();
				em.remove(city);
				em.getTransaction().commit();
			}
			retVal = em.createNamedQuery("MFPlantLocations").getResultList();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			em.close();
		}
		
		return retVal;
	}
	
	
	/**
	 * Returns the <code>DefaultDB</code> {@link DataSource} via JNDI.
	 * 
	 * @return <code>DefaultDB</code> {@link DataSource}
	 */
	protected DataSource getDataSource()
	{
		DataSource retVal = null;
		
		try 
	    {
	        InitialContext ctx = new InitialContext();
	        retVal = (DataSource) ctx.lookup("java:comp/env/jdbc/DefaultDB");
	    }
		catch (NamingException ex)
		{
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
	protected EntityManagerFactory getEntityManagerFactory()
	{
		EntityManagerFactory retVal = null;	
		try
		{
			Map properties = new HashMap();
			DataSource ds = this.getDataSource();
	        properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
	        retVal = Persistence.createEntityManagerFactory("mfplantapp", properties);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return retVal;
	}
	
	@GET
	@Path("/{id}/mfplantinfo")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getMFPlantDetail(@PathParam(value = "id") String id, @Context SecurityContext ctx)
	{
		MFPlantDetailService plantInfoService = new MFPlantDetailService();
		return plantInfoService.getMFPlantInformation(id);
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