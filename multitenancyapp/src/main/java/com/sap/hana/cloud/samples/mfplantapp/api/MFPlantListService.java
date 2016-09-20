package com.sap.hana.cloud.samples.mfplantapp.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.sap.cloud.account.Tenant;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.DestinationException;
import com.sap.core.connectivity.api.http.HttpDestination;
import com.sap.hana.cloud.samples.mfplantapp.model.MFPlant;
import com.sap.hana.cloud.samples.mfplantapp.model.OGPlantAirQuality;
import com.sap.security.um.service.UserManagementAccessor;
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
	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;

	@SuppressWarnings("unchecked")
	@GET
	@Path("/")
	public List<OGPlantAirQuality> getPlantAirQualityDetails(
			@Context HttpServletRequest request)
			throws ClientProtocolException, NamingException,
			DestinationException, URISyntaxException, IOException, Exception {
		List<OGPlantAirQuality> OGPlantAirQualityList = null;
		List<MFPlant> mfPlantList = null;
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
				mfPlantList = em.createNamedQuery("MFPlants").getResultList();

				prepareOGPlantAirQualityList(OGPlantAirQualityList, mfPlantList);
				return OGPlantAirQualityList;
			} else if (request.getUserPrincipal() != null
					&& request.isUserInRole("user")) {
				if (plant_id != null) {
					mfPlantList = getMFPlantsById(plant_id);
					prepareOGPlantAirQualityList(OGPlantAirQualityList,
							mfPlantList);
					return OGPlantAirQualityList;
				}
			}

		}
		return OGPlantAirQualityList;
	}

	private void prepareOGPlantAirQualityList(
			List<OGPlantAirQuality> OGPlantAirQualityList,
			List<MFPlant> mfPlantList) throws NamingException,
			DestinationException, URISyntaxException, IOException,
			ClientProtocolException, Exception {
		// Get HTTP destination
		javax.naming.Context ctx = new InitialContext();
		HttpDestination destination = (HttpDestination) ctx
				.lookup("java:comp/env/" + "plantopenaq");
		// Create HTTP client
		//Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
	     //Protocol.registerProtocol("https", easyhttps);
		HttpClient httpClient = destination.createHttpClient();
		HttpClient wrapClient = wrapClient(httpClient);
		for (Iterator<MFPlant> iterator = mfPlantList.iterator(); iterator
				.hasNext();) {
			MFPlant ogPlant = (MFPlant) iterator.next();

			final String baseURL = destination.getURI()
					+ "?parameter=o3&location=" + ogPlant.getLocation()
					+ "&date_from=" + ogPlant.getDateField() + "&date_to="
					+ ogPlant.getDateField();
			//String encodeURL = URLEncoder.encode(baseURL,"UTF-8");
			String encodedURL=baseURL.replaceAll(" ", "%20");
			// Execute HTTP request
			HttpGet httpGet = new HttpGet(encodedURL);
			HttpResponse httpResponse = wrapClient.execute(httpGet);
			// Check response status code
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			// copy content from the incoming response to the outgoing response
			HttpEntity entity = null;
			if (httpResponse != null) {
				entity = httpResponse.getEntity();
			}
			JSONArray msgBody = getResponseBodyasString(entity);
			if (statusCode == HttpServletResponse.SC_OK) {
				JSONObject cityO3 = msgBody.getJSONObject(3);
				OGPlantAirQuality airQuality = new OGPlantAirQuality();
				airQuality.setOgPlant(ogPlant);
				airQuality.setCityO3(cityO3.getString("value"));
				OGPlantAirQualityList.add(airQuality);

			}

		}
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
	@Path("/{id}/{date}/{o3}/{location}")
	public List<MFPlant> addMFPlant(@Context SecurityContext ctx,
			@PathParam(value = "id") String id,
			@PathParam(value = "date") Date date,
			@PathParam(value = "o3") String o3,
			@PathParam(value = "location") String location) {
		List<MFPlant> retVal = null;
		MFPlant plant = new MFPlant();
		plant.setId(id);
		plant.setDateField(date);
		plant.setO3(o3);
		plant.setLocation(location);
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

	/**
	 * Extracts the response body from the specified {@link HttpEntity} and
	 * returns it as a UTF-8 encoded String.
	 * 
	 * @param entity
	 *            The {@link HttpEntity} to extract the message body from
	 * @return The UTF-8 encoded String representation of the message body
	 * @throws
	 */
	static JSONArray getResponseBodyasString(HttpEntity entity)
			throws Exception {
		String retVal = null;

		if (entity != null) {
			InputStream instream = entity.getContent();
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();

			try {
				byte[] buffer = new byte[COPY_CONTENT_BUFFER_SIZE];
				int len;
				while ((len = instream.read(buffer)) != -1) {
					outstream.write(buffer, 0, len);
				}
			} catch (IOException e) {
				// In case of an IOException the connection will be released
				// back to the connection manager automatically
				throw e;
			} finally {
				// Closing the input stream will trigger connection release
				try {
					instream.close();
				} catch (Exception e) {
					// Ignore
				}
			}

			retVal = outstream.toString("UTF-8");
			JSONTokener tokener = new JSONTokener(retVal);
			JSONArray finalResult = new JSONArray(tokener);
			return finalResult;

		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static HttpClient wrapClient(HttpClient base) {
		try {
		SSLContext ctx = SSLContext.getInstance("TLS");
		X509TrustManager tm = new X509TrustManager() {
		 
		public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
		}
		 
		public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
		}
		 
		public X509Certificate[] getAcceptedIssuers() {
		return null;
		}
		};
		ctx.init(null, new TrustManager[]{tm}, null);
		SSLSocketFactory ssf = new SSLSocketFactory(ctx);
		ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		ClientConnectionManager ccm = base.getConnectionManager();
		SchemeRegistry sr = ccm.getSchemeRegistry();
		sr.register(new Scheme("https", ssf, 443));
		return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
		ex.printStackTrace();
		return null;
		}
		}	
	
	
}