package silverSol.game.area;

import java.util.ArrayList;
import java.util.List;

import silverSol.engine.entity.Entity;
import silverSol.engine.entity.terrain.Terrain;

public class Area {
	private List<Terrain> terrains;
	private List<Entity> entities;
	
	public Area() {
		terrains = new ArrayList<>();
		entities = new ArrayList<>();
	}
	
	public void clearArea() {
		terrains.clear();
		entities.clear();
	}
	
	public List<Terrain> getTerrains() {
		return terrains;
	}
	
	public void addTerrain(Terrain terrain) {
		terrains.add(terrain);
	}
	
	public void addTerrains(Terrain... terrains) {
		for(Terrain terrain : terrains) {
			addTerrain(terrain);
		}
	}
	
	public void addTerrains(List<Terrain> terrains) {
		for(Terrain terrain : terrains) {
			addTerrain(terrain);
		}
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	public void addEntity(Entity entity) {
		entities.add(entity);
	}
	
	public void addEntities(Entity... entities) {
		for(Entity entity : entities) {
			addEntity(entity);
		}
	}
	
	public void addEntities(List<Entity> entities) {
		for(Entity entity : entities) {
			addEntity(entity);
		}
	}
}
