package org.acme.domain;

import java.math.BigDecimal;
import java.sql.Date;

import com.github.mapresultset.api.Column;
import com.github.mapresultset.api.Table;

@Table(name = "notebook")
public class Notebook {
	private int id;
	private String name;
	private BigDecimal value;
	@Column (name = "release_date")
	private Date releaseDate;
	@Column (name = "is_available")
	private char isAvailable;
	@Column (name = "is_ssd")
	private boolean isSSD;
	@Column (name = "has_wifi")
	private boolean hasWifi;
	@Column (name = "cpu_speed")
	private float cpuSpeed;
	@Column (name = "production_cost")
	private double productionCost;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getCpuSpeed() {
		return cpuSpeed;
	}

	public void setCpuSpeed(float cpuSpeed) {
		this.cpuSpeed = cpuSpeed;
	}

	public boolean isHasWifi() {
		return hasWifi;
	}

	public void setHasWifi(boolean hasWifi) {
		this.hasWifi = hasWifi;
	}

	public boolean isSSD() {
		return isSSD;
	}

	public void setSSD(boolean isSSD) {
		this.isSSD = isSSD;
	}

	public char getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(char isAvailable) {
		this.isAvailable = isAvailable;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public double getProductionCost() {
		return productionCost;
	}

	public void setProductionCost(double productionCost) {
		this.productionCost = productionCost;
	}

	@Override
	public String toString() {
		return "Notebook [cpuSpeed=" + cpuSpeed + ", hasWifi=" + hasWifi + ", id=" + id + ", isAvailable=" + isAvailable
				+ ", isSSD=" + isSSD + ", name=" + name + ", productionCost=" + productionCost + ", releaseDate="
				+ releaseDate + ", value=" + value + "]";
	}
	
}

