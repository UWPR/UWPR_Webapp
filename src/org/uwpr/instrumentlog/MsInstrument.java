package org.uwpr.instrumentlog;

public class MsInstrument {

	private int id;
	private String name;
	private String description;
	private boolean active;
	private String color;
	private boolean isMassSpec;

	public MsInstrument(){}

	public MsInstrument(int id, String name, String description, boolean active, boolean isMassSpec) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.active = active;
		this.isMassSpec = isMassSpec;
	}

	public MsInstrument(int id, String name, String description, boolean active, boolean isMassSpec, String color) {
		this(id, name, description, active, isMassSpec);
		this.color = color;
	}

	public int getID() {
		return id;
	}

	public String getName() {
		if(active)
			return name;
		else
		{
			return name + " (retired)";
		}
	}
	
	public String getNameOnly() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isActive() {
		return active;
	}

	public boolean isMassSpec()
	{
		return isMassSpec;
	}

	public String getColor()
	{
		return color;
	}

	public String getHexColor()
	{
		if(color != null && !color.startsWith("#"))
		{
			return "#" + color;
		}
		return color;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public void setMassSpec(boolean massSpec)
	{
		this.isMassSpec = massSpec;
	}

	public void setColor(String color)
	{
		this.color = color;
	}
}
