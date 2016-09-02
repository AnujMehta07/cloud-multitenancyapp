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
		JSONArray plants = new JSONArray();
		MFPlantListService plantListService=new MFPlantListService();	
		if (request.getRemoteUser() != null) {
			if (request.isUserInRole("admin")) {
				List<MFPlant> plantList = plantListService
						.getMFPlantList(request);
				for (Iterator iterator = plantList.iterator(); iterator
						.hasNext();) {
					MFPlant mfPlant = (MFPlant) iterator.next();
					JSONObject plant = new JSONObject();
					plant.put("name", mfPlant.getName());
					plant.put("id", mfPlant.getId());
					plant.put("employeeId", mfPlant.getEmployeeId());
					plant.put("co", mfPlant.getCo());
					plant.put("o3", mfPlant.getO3());
					plant.put("pm10", mfPlant.getPm10());
					plant.put("pm25", mfPlant.getPm25());
					plant.put("so2", mfPlant.getSo2());
					plant.put("no2", mfPlant.getNo2());
					plants.put(plant);
				}
				response.setContentType("application/json");
				response.getWriter().write(plants.toString());
			}
			else if(request.isUserInRole("user")){			
				List<MFPlant> plantList = plantListService.getMFPlantByEmployeeId(request);
				for (Iterator iterator = plantList.iterator(); iterator
						.hasNext();) {
					MFPlant mfPlant = (MFPlant) iterator.next();
					JSONObject plant = new JSONObject();
					plant.put("name", mfPlant.getName());
					plant.put("id", mfPlant.getId());
					plant.put("employeeId", mfPlant.getEmployeeId());
					plant.put("co", mfPlant.getCo());
					plant.put("o3", mfPlant.getO3());
					plant.put("pm10", mfPlant.getPm10());
					plant.put("pm25", mfPlant.getPm25());
					plant.put("so2", mfPlant.getSo2());
					plant.put("no2", mfPlant.getNo2());
					plants.put(plant);			
				}
				response.setContentType("application/json");
				response.getWriter().write(plants.toString());
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
