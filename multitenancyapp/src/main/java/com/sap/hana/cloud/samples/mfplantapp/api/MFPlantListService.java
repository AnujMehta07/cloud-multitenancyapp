package com.sap.hana.cloud.samples.mfplantapp.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
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
import javax.ws.rs.core.SecurityContext;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sap.cloud.account.Tenant;
import com.sap.cloud.account.TenantContext;
import com.sap.cloud.crypto.keystore.api.KeyStoreService;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sap.hana.cloud.samples.mfplantapp.model.MFPlant;
import com.sap.hana.cloud.samples.mfplantapp.model.OGPlantAirQualityDayData;
import com.sap.hana.cloud.samples.mfplantapp.model.OGPlantsAirQualityData;
import com.sap.hana.cloud.samples.mfplantapp.model.OGPlantsAirQualityWeeklyData;
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
@SuppressWarnings("unchecked")
@Path("/plantlist")
@Produces({ MediaType.APPLICATION_JSON })
public class MFPlantListService {
	private static final int COPY_CONTENT_BUFFER_SIZE = 1024;

	@GET
	@Path("/")
	public OGPlantsAirQualityData getPlantAirQualityDetails(@Context HttpServletRequest request,
			@Context HttpServletResponse response) throws Exception {
		List<OGPlantAirQualityDayData> OGPlantAirQualityList = new ArrayList<OGPlantAirQualityDayData>();
		List<MFPlant> mfPlantList = null;
		String tenantId = getTenantId();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		boolean isAdmin = isUserAdmin(request);
		if (isAdmin) {
			mfPlantList = em.createNamedQuery("MFPlants").getResultList();
			OGPlantsAirQualityData ogPlantsAirQualityData = prepareOGPlantAirQualityList(mfPlantList, response);
			ogPlantsAirQualityData.setRole("Area Manager" );
			return ogPlantsAirQualityData;
		} else {
			String plant_id = getPlantId(request);
			mfPlantList = getMFPlantsById(plant_id);
			OGPlantsAirQualityData ogPlantsAirQualityData = prepareOGPlantAirQualityList(mfPlantList, response);
			ogPlantsAirQualityData.setRole("Supervisor");
			return ogPlantsAirQualityData;
		}
	}

	@GET
	@Path("/{id}")
	public List<MFPlant> getMFPlantsById(@PathParam(value = "id") String id) {
		List<MFPlant> retVal = null;
		String tenantId = getTenantId();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		Query query = em.createNamedQuery("MFPlantById");
		query.setParameter("id", id);
		retVal = query.getResultList();
		return retVal;
	}

	@GET
	@Path("/{id}/{startDate}/{endDate}")
	public List<MFPlant> getMFPlantByIdAndDate(@PathParam(value = "id") String id,
			@PathParam(value = "startDate") String startDate, @PathParam(value = "endDate") String endDate,
			@Context SecurityContext ctx) {
		List<MFPlant> retVal = null;
		String tenantId = getTenantId();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		Query query = em.createNamedQuery("MFPlantByIdAndDate");
		query.setParameter("id", id);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		retVal = query.getResultList();
		return retVal;
	}

	@POST
	@Path("/{id}/{date}/{o3}/{location}")
	public List<MFPlant> addMFPlant(@Context SecurityContext ctx, @PathParam(value = "id") String id,
			@PathParam(value = "date") Date date, @PathParam(value = "o3") String o3,
			@PathParam(value = "location") String location) {
		List<MFPlant> retVal = null;
		MFPlant plant = new MFPlant();
		plant.setId(id);
		plant.setDateField(date);
		plant.setO3(o3);
		plant.setLocation(location);
		String tenantId = getTenantId();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		em.getTransaction().begin();
		em.persist(plant);
		em.getTransaction().commit();
		retVal = em.createNamedQuery("MFPlants").getResultList();
		return retVal;
	}

	@DELETE
	@Path("/{id}")
	public List<MFPlant> removeMFPlant(@PathParam(value = "id") String id, @Context SecurityContext ctx) {
		List<MFPlant> retVal = null;
		String tenantId = getTenantId();
		Map<String, String> props = new HashMap<String, String>();
		props.put("elipselink.tenant.id", tenantId);
		EntityManager em = this.getEntityManagerFactory().createEntityManager(props);
		Query query = em.createNamedQuery("MFPlantById");
		query.setParameter("id", id);
		retVal = query.getResultList();
		for (Iterator iterator = retVal.iterator(); iterator.hasNext();) {
			MFPlant mfPlant = (MFPlant) iterator.next();
			if (mfPlant != null) {
				em.getTransaction().begin();
				em.remove(mfPlant);
				em.getTransaction().commit();
			}

		}

		retVal = em.createNamedQuery("MFPlants").getResultList();
		return retVal;
	}

	/**
	 * Returns the <code>DefaultDB</code> {@link DataSource} via JNDI.
	 * 
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
	 * Returns the {@link EntityManagerFactory}
	 * 
	 * @return The {@link EntityManagerFactory}
	 */
	protected EntityManagerFactory getEntityManagerFactory() {
		EntityManagerFactory retVal = null;
		try {
			Map<String, DataSource> properties = new HashMap<String, DataSource>();
			DataSource ds = this.getDataSource();
			properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
			retVal = Persistence.createEntityManagerFactory("mfplantapp", properties);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retVal;
	}

	@GET
	@Path("/mfplantinfo/all")
	@Produces({ MediaType.APPLICATION_JSON })
	public String getMFPlantsDetail(@Context HttpServletRequest request) throws Exception {
		MFPlantDetailService plantInfoService = new MFPlantDetailService();
		boolean isUserAdmin = isUserAdmin(request);
		if (isUserAdmin) {
			return plantInfoService.getMFPlantsInformation();
		} else {
			String plant_id = getPlantId(request);
			return plantInfoService.getMFPlantInformation(plant_id);
		}

	}

	protected TenantContext getTenantContext() {
		TenantContext tenantContext = null;
		try {
			InitialContext ctx = new InitialContext();
			tenantContext = (TenantContext) ctx.lookup("java:comp/env/TenantContext");
		} catch (NamingException ex) {
			ex.printStackTrace();
		}
		return tenantContext;
	}

	/**
	 * Extracts the response body from the specified {@link HttpEntity} and
	 * returns it as a UTF-8 encoded JSON Array.
	 * 
	 * @param entity
	 *            The {@link HttpEntity} to extract the message body
	 *            from @return The UTF-8 encoded JSON Array representation of
	 *            the message body
	 */
	static String getResponseBodyasJSONArray(InputStream instream) throws Exception {
		String retVal = null;
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
		return retVal;

	}

	private SSLSocketFactory establishedSSLConnection(HttpServletResponse response, String host, int port)
			throws Exception {
		KeyStoreService keystoreService = null;
		try {
			javax.naming.Context context = new InitialContext();
			keystoreService = (KeyStoreService) context.lookup("java:comp/env/KeyStoreService");
		} catch (NamingException e) {
			response.getWriter().println("Error:<br><pre>");
			e.printStackTrace(response.getWriter());
			response.getWriter().println("</pre>");
		}
		KeyStore clientKeystore = keystoreService.getKeyStore("jssecacerts", null);
		KeyStore trustedCAKeystore = keystoreService.getKeyStore("cacerts", null);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(clientKeystore, null);
		KeyManager[] keyManagers = kmf.getKeyManagers();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustedCAKeystore);
		TrustManager[] trustManagers = tmf.getTrustManagers();
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagers, trustManagers, null);
		SSLSocketFactory factory = sslContext.getSocketFactory();
		SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		socket.startHandshake();
		return factory;
	}
//TO Do : Add boolean for isAdmin and prepare data accordingly.
	private OGPlantsAirQualityData prepareOGPlantAirQualityList(List<MFPlant> mfPlantList, HttpServletResponse response)
			throws Exception {
		HttpsURLConnection urlConnection = null;
		OGPlantsAirQualityData ogPlantsAirQualityData = new OGPlantsAirQualityData();
		OGPlantsAirQualityWeeklyData plantAirQualityWeeklyData = new OGPlantsAirQualityWeeklyData();
		List<OGPlantAirQualityDayData> plantAirQualityDataList = new ArrayList<>();
		List<OGPlantsAirQualityWeeklyData> plantsAirQualityWeeklyDataList = new ArrayList<>();
		// Look up the connectivity configuration API
		javax.naming.Context ctx = new InitialContext();
		ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx
				.lookup("java:comp/env/connectivityConfiguration");
		// Get destination configuration for "destinationName"
		DestinationConfiguration destConfiguration = configuration.getConfiguration("openaq-api-dest");
		SSLSocketFactory socketFactory = null;
		// Get the destination URL
		String value = destConfiguration.getProperty("URL");
		URL baseUrl = new URL(value);
		socketFactory = establishedSSLConnection(response, baseUrl.getHost(), 443);
		for (Iterator<MFPlant> iterator = mfPlantList.iterator(); iterator.hasNext();) {
			MFPlant ogPlant = (MFPlant) iterator.next();
			final String destinationUrl = value + "?parameter=o3&location=" + ogPlant.getLocation() + "&date_from="
					+ ogPlant.getDateField() + "&date_to=" + ogPlant.getDateField();
			URL url = new URL(destinationUrl.replaceAll(" ", "%20"));
			urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setSSLSocketFactory(socketFactory);
			// Copy content from the incoming response to the outgoing response
			InputStream instream = urlConnection.getInputStream();
			String msgBody = getResponseBodyasJSONArray(instream);
			JSONObject cityO3 = new JSONObject(msgBody);
			OGPlantAirQualityDayData plantAirQualityData = new OGPlantAirQualityDayData();
			plantAirQualityData.setOgPlant(ogPlant);
			JSONArray jsonArray = cityO3.getJSONArray("results");
			if (!jsonArray.isNull(0) && jsonArray.get(0) != null) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(0);
				if (jsonObject != null) {
					Double o3value = (Double) jsonObject.get("value");
					plantAirQualityData.setCityO3(o3value.toString());
				}
			} else {
				// Value is not available
				plantAirQualityData.setCityO3(" ");
			}
			plantAirQualityDataList.add(plantAirQualityData);
		}
		plantAirQualityWeeklyData.setPlant101AirQualityWeeklyData(plantAirQualityDataList.subList(0, 5));
		if (plantAirQualityDataList.size() > 5) {
			plantAirQualityWeeklyData.setPlant102AirQualityWeeklyData(plantAirQualityDataList.subList(5, 10));
			plantAirQualityWeeklyData.setPlant103AirQualityWeeklyData(plantAirQualityDataList.subList(10, 15));
		}
		plantsAirQualityWeeklyDataList.add(plantAirQualityWeeklyData);

		ogPlantsAirQualityData.setPlantAirQualityWeeklyDataList(plantsAirQualityWeeklyDataList);
		return ogPlantsAirQualityData;
	}

	private boolean isUserAdmin(HttpServletRequest request)
			throws PersistenceException, UnsupportedUserAttributeException {
		String plant_id = getPlantId(request);
		if (request.getUserPrincipal() != null && request.isUserInRole("admin")) {
			if (plant_id == null) { // this means he is the admin
				return true;
			} else if (request.getUserPrincipal() != null && request.isUserInRole("user"))
				if (plant_id != null) {
					return false;
				}

		}
		return false;
	}

	private String getPlantId(HttpServletRequest request)
			throws PersistenceException, UnsupportedUserAttributeException {
		// UserProvider provides access to the user storage
		UserProvider userProvider = UserManagementAccessor.getUserProvider();
		// Read the currently logged in user from the user storage
		User user = userProvider.getUser(request.getUserPrincipal().getName());
		String plant_id = user.getAttribute("PLANT_ID");
		return plant_id;
	}

	private String getTenantId() {
		TenantContext tenantContext = getTenantContext();
		Tenant tenant = tenantContext.getTenant();
		String tenantId = tenant.getId().trim();
		return tenantId;
	}

}