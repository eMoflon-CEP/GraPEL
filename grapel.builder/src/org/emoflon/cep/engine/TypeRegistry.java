package org.emoflon.cep.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TypeRegistry {
	private Map<Class<?>, Map<Object, Integer>> object2id = Collections.synchronizedMap(new HashMap<>());
	private Map<Class<?>, Integer> lastId = Collections.synchronizedMap(new HashMap<>());
	private Map<Class<?>, Map<Integer, Object>> id2object = Collections.synchronizedMap(new HashMap<>());
	
	public synchronized int addObject(Object object) {
		if(object2id.containsKey(object.getClass()) && object2id.get(object.getClass()).containsKey(object))
			return object2id.get(object.getClass()).get(object);
		
		int id = -1;
		if(!lastId.containsKey(object.getClass())) {
			id = 0;
		}else {
			id = lastId.get(object.getClass())+1;
		}
		lastId.put(object.getClass(), id);
		
		Map<Object, Integer> objectMap = object2id.get(object.getClass());
		Map<Integer, Object> idMap = id2object.get(object.getClass());
		if(objectMap == null) {
			objectMap = Collections.synchronizedMap(new HashMap<>());
			object2id.put(object.getClass(), objectMap);
			idMap = Collections.synchronizedMap(new HashMap<>());
			id2object.put(object.getClass(), idMap);
		}
		
		objectMap.put(object, id);
		idMap.put(id, object);
		
		return id;
	}
	
	public synchronized Object getObject(Class<?> clazz, int id) {
		if(!object2id.containsKey(clazz))
			return null;
		
		Map<Integer, Object> idMap = id2object.get(clazz);
		return idMap.get(id);
	}
}
