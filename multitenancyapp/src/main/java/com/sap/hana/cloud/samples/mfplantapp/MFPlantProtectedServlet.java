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
import com.sap.hana.cloud.samples.mfplantapp.model.MFPlantLocation;

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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONArray locations = new JSONArray();
		MFPlantListService locationService=new MFPlantListService();	
		if (request.getRemoteUser() != null) {

			if (request.isUserInRole("admin")) {

				List<MFPlantLocation> plantLocations = locationService
						.getMFPlantLists(request);
				for (Iterator iterator = plantLocations.iterator(); iterator
						.hasNext();) {
					MFPlantLocation plantLocation = (MFPlantLocation) iterator.next();
					JSONObject city = new JSONObject();
					city.put("name", plantLocation.getName());
					city.put("id", plantLocation.getId());
					city.put("owner", plantLocation.getOwner());
					locations.put(city);
				}
				response.setContentType("application/json");
				response.getWriter().write(locations.toString());
			}
			else if(request.isUserInRole("user")){			
				List<MFPlantLocation> mfPlantLocations = locationService.getMFPlantByOwner(request);
				for (Iterator iterator = mfPlantLocations.iterator(); iterator
						.hasNext();) {
					MFPlantLocation plantLocation = (MFPlantLocation) iterator.next();
					JSONObject city = new JSONObject();
					city.put("name", plantLocation.getName());
					city.put("id", plantLocation.getId());
					city.put("owner", plantLocation.getOwner());
					locations.put(city);			
				}
				response.setContentType("application/json");
				response.getWriter().write(locations.toString());
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
