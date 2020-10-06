package util;

import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;

import com.github.javafaker.Faker;

import mNetwork.Company;
import mNetwork.Database;
import mNetwork.Entities;
import mNetwork.Entity;
import mNetwork.Entry;
import mNetwork.GeoTag;
import mNetwork.Link;
import mNetwork.MEMIK;
import mNetwork.MNetworkFactory;
import mNetwork.Network;
import mNetwork.Person;
import mNetwork.Server;
import mNetwork.VServer;

public class ModelGenerator {
	/**
	 * internal id tracker to keep IDs unique
	 */
	private int idAcc = 0; // TODO: maybe move
	/**
	 * width to spread for waxman and server placement
	 */
	private double width;
	/**
	 * height to spread for waxman and server placement
	 */
	private double height;
	/**
	 * maximum distance between two corners
	 */
	private static double maxDistance;
	/**
	 * 'alpha' factor of waxman
	 */
	private static final double alpha = 0.4;
	/**
	 * 'beta' factor of waxman
	 */
	private static final double beta = 0.4;
	/**
	 * max number of VServers on a server for random creation
	 */
	private static final int maxVServerFactor = 8;
	/**
	 * max number of databases on a server for random creation
	 */
	private static final int maxDatabaseFactor = 10;
	/**
	 * max number of entries on a database for random creation
	 */
	private static final int maxEntriesOnDatabaseFactor = 10;
	/**
	 * max number of entities on a entry for random creation
	 */
	private static final int maxEntitiesOnEntryFactor = 10;
	/**
	 * portion of businesses in entities for random creation
	 */
	private static final double randomBusinessEntitiyPortion = 0.45;
	/**
	 * portion of entities with GeoTag "EU" for random creation
	 */
	private static final double randomEUEntityPortion = 0.3;
	/**
	 * portion of servers with GeoTag "EU" for random creation
	 */
	private static final double randomEUServerPortion = 0.4;
	
	/**
	 * instance of MNetworkFactory
	 */
	private MNetworkFactory factory = MNetworkFactory.eINSTANCE;
	

	public ModelGenerator(double width, double height) {
		this.width = width;
		this.height = height;
		maxDistance= Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
	}

	/**
	 * @param a lower bound
	 * @param b upper bound
	 * @return random value between a and b
	 */
	private double uniform(double a, double b) {
		return Math.random() * (b - a) + a;
	}

	/**
	 * @param s1
	 * @param s2
	 * @return calculates distance between server 1 and server 2
	 */
	private double getDistance(Server s1, Server s2) {
		return Math.sqrt(Math.pow((s2.getX() - s1.getX()), 2) + Math.pow((s2.getY() - s1.getY()), 2));
	}

	/**
	 * @param numOfServers defines how many servers should be generated
	 * @return list containing numOfServers new servers (already populated with VServers)
	 */
	private ArrayList<Server> createPopulatedServers(int numOfServers, Entities entities) {
		ArrayList<Server> servers = new ArrayList<Server>();
		for (int i = 0; i < numOfServers; i++) {
			Server server = factory.createServer();
			// set unique id
			server.setId(idAcc);
			idAcc++;
			// calculate position
			server.setX(uniform(0.0, width));
			server.setY(uniform(0.0, height));
			if(Math.random()<randomEUServerPortion) server.setTag(GeoTag.EU);
			else server.setTag(GeoTag.NON_EU);
			// create VServers
			for(int n = 0; n < maxVServerFactor * Math.random();n++) {
				VServer v = factory.createVServer();
				for(int u = 0; u < maxDatabaseFactor * Math.random();u++) {
					Database d = createPopulatedDatabase(entities);
					v.getDatabase().add(d);
				}
				server.getVserver().add(v);
			}
			servers.add(server);
		}
		return servers;
	}
	
	/**
	 * @param entities to populate database
	 * @return	populated database with entries containing the given entities
	 */
	private Database createPopulatedDatabase(Entities entities) {
		int numOfEntities = entities.getEntity().size();
		Database d = factory.createDatabase();
		if(numOfEntities>0) {
			for(int i = 0; i < Math.random() * maxEntriesOnDatabaseFactor; i++) {
				Entry e = factory.createEntry();
				for(int j = 0; j < Math.random() * maxEntitiesOnEntryFactor; j++) {
					e.getEntity().add(
								entities.getEntity().get((int)((numOfEntities-1)*Math.random()))
							);
				}
				d.getEntry().add(e);
			}
		}
		return d;
	}
	
	/**
	 * @param servers list of servers to be connected
	 * @return list of new links between the given servers
	 */
	private ArrayList<Link> createLinksWaxman(ArrayList<Server> servers) {
		ArrayList<Link> links = new ArrayList<Link>();
		for(int i = 0; i < servers.size(); i++) {
			Server s1 = servers.get(i);
			for(int n = i + 1; n < servers.size();n++) {
				Server s2 = servers.get(n);
				double d = getDistance(s1,s2);
				double probaility = beta * Math.exp(-d) / (alpha * maxDistance);
				if(Math.random() < probaility) {
					Link link = factory.createLink();
					link.getServer().add(s1);
					link.getServer().add(s2);
					links.add(link);
				}
			}
		}
		return links;
	}
	
	/**
	 * @param servers new servers to add links between existing servers. New servers dont need to be linked already
	 * @param existingServers already linked
	 * @return new list of links between existing servers and new servers
	 */
	private ArrayList<Link> addLinksWaxman(ArrayList<Server> servers, EList<Server> existingServers) {
		ArrayList<Link> links = createLinksWaxman(servers);
		for(Server s1 : servers) {
			for(Server s2 : existingServers) {
				double d = getDistance(s1,s2);
				double probaility = beta * Math.exp(-d) / (alpha * maxDistance);
				if(Math.random() < probaility) {
					Link link = factory.createLink();
					link.getServer().add(s1);
					link.getServer().add(s2);
					links.add(link);
				}
			}
		}	
		return links;
	}
	
	/**
	 * @param numOfServers number of servers to create in the new network
	 * @param entities to populate network
	 * @return populated Network
	 */
	public Network createPrepopulatedNetwork(int numOfServers, Entities entities) {
		Network n = factory.createNetwork();
		// create servers with VServers, VServers populated with Databases, Databases populated with entities
		ArrayList<Server> servers = createPopulatedServers(numOfServers, entities);
		// create links between servers
		ArrayList<Link> links =  createLinksWaxman(servers);
		// add components server, links and entries to network
		n.getServer().addAll(servers);
		n.getLink().addAll(links);
		return n;
	}
	
	/**
	 * @param numOfServersToAdd number of servers to create and add to a given network
	 * @param network to add servers
	 * @param entities to populate server
	 * @return network with added populated servers
	 */
	public Network addToNetwork(int numOfServersToAdd, Network network, Entities entities) {
		ArrayList<Server> servers = createPopulatedServers(numOfServersToAdd, entities);
		ArrayList<Link> links =  addLinksWaxman(servers, network.getServer());
		network.getServer().addAll(servers);
		network.getLink().addAll(links);
		return network;
	}
	
	/**
	 * @param numOfEntities number of entities to create
	 * @return entities container filled with entities
	 */
	public Entities createEntities(int numOfEntities) {
		Entities entities = factory.createEntities();
		addEntities(numOfEntities, entities);
		return entities;
	}
	
	/**
	 * @param numOfEntities number of entities to add 
	 * @param entities container to add to
	 * @return entities container with added entities
	 */
	private Entities addEntities(int numOfEntities, Entities entities) {
		Faker faker = new Faker();
		Entity e;	
		
		for(int i = 0; i<numOfEntities; i++) {
			// determine if Entity is Person or Company
			if (Math.random()<randomBusinessEntitiyPortion) {
				Company c = factory.createCompany();
				c.setName(faker.company().name());
				e=c;
			} else {
				Person p = factory.createPerson();
				p.setFirstName(faker.name().firstName());
				p.setLastName(faker.name().lastName());
				e=p;
			}
			// random GeoTag
			if(Math.random()<randomEUEntityPortion) e.setTag(GeoTag.EU);
			else e.setTag(GeoTag.NON_EU);
			entities.getEntity().add(e);
		}
		return entities;
	}
	
	/**
	 * @param numOfEntities number of entities to create
	 * @param numOfServers number of servers to create
	 * @return populated memik-network
	 */
	public MEMIK createPrepopulatedMEMIK(int numOfEntities, int numOfServers) {
		MEMIK m = factory.createMEMIK();
		Entities entities = createEntities(numOfEntities);
		Network n = createPrepopulatedNetwork(numOfServers, entities);
		m.setEntities(entities);
		m.setNetwork(n);
		return m;
	}
}
