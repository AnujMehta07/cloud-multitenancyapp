package com.sap.hana.cloud.samples.mfplantapp;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sap.hana.cloud.samples.mfplantapp.api.MFPlantListService;
import com.sap.hana.cloud.samples.mfplantapp.model.MFPlant;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;

/**
 * Servlet implementation class MFPlantProtectedServlet
 */
public class MFPlantProtectedServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MFPlantProtectedServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		JSONArray plants = new JSONArray();
		MFPlantListService plantListService = new MFPlantListService();
		// Check for a logged in user
		if (request.getUserPrincipal() != null && request.isUserInRole("admin")) {
			// UserProvider provides access to the user storage
			UserProvider users = null;
			try {
				users = UserManagementAccessor.getUserProvider();
				// Read the currently logged in user from the user storage
				User user = users.getUser(request.getUserPrincipal().getName());
				String plant_id = user.getAttribute("PLANT_ID");
				if (plant_id == null) {// this means he is the admin
					List<MFPlant> plantList = plantListService
							.getMFPlantList(request);
					writePlantList(request, response, plants, plantList);

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (request.getUserPrincipal() != null
				&& request.isUserInRole("user")) {

			// UserProvider provides access to the user storage
			UserProvider users = null;
			try {
				users = UserManagementAccessor.getUserProvider();
				// Read the currently logged in user from the user storage
				User user = users.getUser(request.getUserPrincipal().getName());
				String plant_id = user.getAttribute("PLANT_ID");
				List<MFPlant> plantList= plantListService.getMFPlantsById(plant_id);
				writePlantList(request, response, plants, plantList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void writePlantList(HttpServletRequest request,
			HttpServletResponse response, JSONArray plants,
			List<MFPlant> plantList) throws IOException {
		for (Iterator<MFPlant> iterator = plantList.iterator(); iterator
				.hasNext();) {
			MFPlant mfPlant = (MFPlant) iterator.next();
			JSONObject plant = new JSONObject();
			plant.put("id", mfPlant.getId());
			plant.put("o3", mfPlant.getO3());
			plant.put("dateField", mfPlant.getDateField());
			plants.put(plant);
		}
		response.setContentType("application/json");
		response.getWriter().write(plants.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
