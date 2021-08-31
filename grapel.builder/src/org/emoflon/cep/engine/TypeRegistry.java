package org.emoflon.cep.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping of objects to ID for each type
 */
public class TypeRegistry {
	private Map<Class<?>, Map<Object, Long>> object2id = Collections.synchronizedMap(new HashMap<>());
	private Map<Class<?>, Long> lastId = Collections.synchronizedMap(new HashMap<>());
	private Map<Class<?>, Map<Long, Object>> id2object = Collections.synchronizedMap(new HashMap<>());
	
	/**
	 * Adds an object to the type registry and assigns a ID
	 * 
	 * @param clazz defining the object type
	 * @param object which should be added to the type registry
	 * @return the assigned ID
	 */
	public synchronized long addObject(Class<?> clazz, Object object) {
		if(object2id.containsKey(clazz) && object2id.get(clazz).containsKey(object))
			return object2id.get(clazz).get(object);
		
		long id = -1;
		if(!lastId.containsKey(clazz)) {
			id = 0;
		}else {
			id = lastId.get(clazz)+1;
		}
		lastId.put(clazz, id);
		
		Map<Object, Long> objectMap = object2id.get(clazz);
		Map<Long, Object> idMap = id2object.get(clazz);
		if(objectMap == null) {
			objectMap = Collections.synchronizedMap(new HashMap<>());
			object2id.put(clazz, objectMap);
			idMap = Collections.synchronizedMap(new HashMap<>());
			id2object.put(clazz, idMap);
		}
		
		objectMap.put(object, id);
		idMap.put(id, object);
		
		return id;
	}
	
	/**
	 * Returns the object for a given ID and type
	 * 
	 * @param clazz defining the object type
	 * @param id corresponding to the object
	 * @return the object corresponding to the ID and the id
	 */
	public synchronized Object getObject(Class<?> clazz, long id) {
		if(!object2id.containsKey(clazz))
			return null;
		
		Map<Long, Object> idMap = id2object.get(clazz);
		return idMap.get(id);
	}
}
