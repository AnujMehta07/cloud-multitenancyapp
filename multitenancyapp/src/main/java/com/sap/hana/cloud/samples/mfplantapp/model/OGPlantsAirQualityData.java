package com.sap.hana.cloud.samples.mfplantapp.model;

import java.util.List;

public class OGPlantsAirQualityData {

	private List<OGPlantsAirQualityWeeklyData> plantAirQualityWeeklyDataList;
	private String role;

	public List<OGPlantsAirQualityWeeklyData> getPlantAirQualityWeeklyDataList() {
		return plantAirQualityWeeklyDataList;
	}

	public void setPlantAirQualityWeeklyDataList(List<OGPlantsAirQualityWeeklyData> plantAirQualityWeeklyDataList) {
		this.plantAirQualityWeeklyDataList = plantAirQualityWeeklyDataList;
	}
	public void setRole(String role){
		this.role=role;
	}
	
	public String getRole(){
		return role;
		
	}

	
}
