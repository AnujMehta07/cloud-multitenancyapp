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
import com.sap.hana.cloud.samples.mfplantapp.model.MFPlant;

/**
 * {@link MFPlantListService}
 * 
 * @version 0.1
 */
@Path("/plantlist")
@Produces({ MediaType.APPLICATION_JSON })
public class MFPlantListService 
{
	@SuppressWarnings("unchecked")
	@GET
	@Path("/")
	public List<MFPlant> getMFPlantList(@Context HttpServletRequest request)
	{
		List<MFPlant> retVal = null;		
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		retVal = em.createNamedQuery("MFPlants").getResultList();
		return retVal;
	}
	
	@GET
	@Path("/{id}")
	public MFPlant getMFPlantById(@PathParam(value = "id") String id, @Context SecurityContext ctx)
	{
		MFPlant retVal = null;
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		try
		{
			Query query = em.createNamedQuery("MFPlantById");
			query.setParameter("id", id);
			retVal = (MFPlant) query.getSingleResult();
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
	@Path("/{id}/{co}/{o3}/{pm10}/{pm25}/{so2}/{no2}/")
	public List<MFPlant> addMFPlant(@Context SecurityContext ctx,
			@PathParam(value = "id") String id,
			@PathParam(value = "name") String name,
			@PathParam(value = "country") String country,
			@PathParam(value = "city") String city,
			@PathParam(value = "location") String location,
			@PathParam(value = "employeeId") String employeeId,
			@PathParam(value = "co") String co,
			@PathParam(value = "o3") String o3,
			@PathParam(value = "pm10") String pm10,
			@PathParam(value = "pm25") String pm25,
			@PathParam(value = "so2") String so2,
			@PathParam(value = "no2") String no2
			) {
		List<MFPlant> retVal = null;
		MFPlant plant = new MFPlant();
		plant.setId(id);
		plant.setCo(co);
		plant.setO3(o3);
		plant.setPm10(pm10);
		plant.setPm25(pm25);
		plant.setSo2(so2);
		plant.setNo2(no2);
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		try {
			em.getTransaction().begin();
			em.persist(plant);
			em.getTransaction().commit();
			retVal = em.createNamedQuery("MFPlants").getResultList();
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
	public List<MFPlant> removeMFPlant(@PathParam(value = "id") String id, @Context SecurityContext ctx)
	{
		List<MFPlant> retVal = null;
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);	
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		try
		{
			Query query = em.createNamedQuery("MFPlantById");
			query.setParameter("id", id);
			MFPlant city = (MFPlant) query.getSingleResult();		
			if (city != null)
			{
				em.getTransaction().begin();
				em.remove(city);
				em.getTransaction().commit();
			}
			retVal = em.createNamedQuery("MFPlants").getResultList();
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
	public Response getMFPlantDetail(@PathParam(value = "id") String id)
	{
		MFPlantDetailService plantInfoService = new MFPlantDetailService();
		return plantInfoService.getMFPlantInformation(id);
	}

	@GET
	@Path("/mfplantinfo/all")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getMFPlantsDetail()
	{
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