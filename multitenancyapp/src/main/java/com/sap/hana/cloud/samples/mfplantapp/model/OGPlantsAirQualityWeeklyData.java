package com.sap.hana.cloud.samples.mfplantapp.model;

import java.util.List;

public class OGPlantsAirQualityWeeklyData {
	
	private List<OGPlantAirQualityDayData> plant1AirQualityWeeklyData;
	private List<OGPlantAirQualityDayData> plant2AirQualityWeeklyData;
	private List<OGPlantAirQualityDayData> plant3AirQualityWeeklyData;
	
	public List<OGPlantAirQualityDayData> getPlant101AirQualityWeeklyData() {
		return plant1AirQualityWeeklyData;
	}
	public void setPlant101AirQualityWeeklyData(List<OGPlantAirQualityDayData> plant101AirQualityWeeklyData) {
		this.plant1AirQualityWeeklyData = plant101AirQualityWeeklyData;
	}
	public List<OGPlantAirQualityDayData> getPlant102AirQualityWeeklyData() {
		return plant2AirQualityWeeklyData;
	}
	public void setPlant102AirQualityWeeklyData(List<OGPlantAirQualityDayData> plant102AirQualityWeeklyData) {
		this.plant2AirQualityWeeklyData = plant102AirQualityWeeklyData;
	}
	public List<OGPlantAirQualityDayData> getPlant103AirQualityWeeklyData() {
		return plant3AirQualityWeeklyData;
	}
	public void setPlant103AirQualityWeeklyData(List<OGPlantAirQualityDayData> plant103AirQualityWeeklyData) {
		this.plant3AirQualityWeeklyData = plant103AirQualityWeeklyData;
	}
	
}
