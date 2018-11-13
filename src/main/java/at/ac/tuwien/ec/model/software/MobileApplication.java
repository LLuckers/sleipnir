package at.ac.tuwien.ec.model.software;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public abstract class MobileApplication {

	private int workloadId;
	private String userId;
	private DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> taskDependencies;
	private HashMap<String,MobileSoftwareComponent> componentList = new HashMap<String,MobileSoftwareComponent>();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MobileApplication()
	{
		workloadId = 0;
		taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		componentList = new HashMap<String,MobileSoftwareComponent>();
		setupTasks();
		setupLinks();
	}

	public MobileApplication(int wId)
	{
		workloadId = wId;
		taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		componentList = new HashMap<String,MobileSoftwareComponent>();
		setupTasks();
		setupLinks();
	}
	
	public MobileApplication(int wId,String uId)
	{
		workloadId = wId;
		userId = uId;
		taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		componentList = new HashMap<String,MobileSoftwareComponent>();
		setupTasks();
		setupLinks();
	}
	
	public int getWorkloadId() {
		return workloadId;
	}

	public void setWorkloadId(int workloadId) {
		this.workloadId = workloadId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void addComponent(String id, Hardware reqs, String uid)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,0.0, uid, 0,0);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
	
	public void addComponent(String id, Hardware reqs, String uid, double mips)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,mips, uid, 0,0);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
	
	public void addComponent(String id, Hardware reqs, String uid, double mips, int inData, int outData)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,mips, uid, inData, outData);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
	
	public void addComponent(String id, Hardware reqs, String uid, double mips, boolean offloadable)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,mips, uid, 0,0, offloadable);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
	
	public void addComponent(String id, Hardware reqs, String uid, double mips, double inData, double outData)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,mips, uid, inData, outData, true);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
	
	public void addComponent(String id, Hardware reqs, String uid, double mips, double inData, double outData, boolean offloadable)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,mips, uid, inData, outData, offloadable);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
		
	public ArrayList<MobileSoftwareComponent> getPredecessors(MobileSoftwareComponent msc)
	{
		ArrayList<MobileSoftwareComponent> preds = (ArrayList<MobileSoftwareComponent>) Graphs.predecessorListOf(taskDependencies, msc);
		return preds;
	}
	
	public ArrayList<MobileSoftwareComponent> getNeighbors(MobileSoftwareComponent msc)
	{
		ArrayList<MobileSoftwareComponent> succs = (ArrayList<MobileSoftwareComponent>) Graphs.successorListOf(taskDependencies, msc);
		return succs;
	}
	
	public void addLink(String u, String v, QoSProfile requirements)
	{
		taskDependencies.addEdge(getComponentById(u), getComponentById(v), new ComponentLink(requirements));
	}
	
	public void addLink(MobileSoftwareComponent u, MobileSoftwareComponent v, QoSProfile requirements)
	{
		taskDependencies.addEdge(u, v, new ComponentLink(requirements));
	}
	
	public void addLink(String u, String v, double latency, double bandwidth)
	{
		taskDependencies.addEdge(getComponentById(u), getComponentById(v), new ComponentLink(new QoSProfile(latency, bandwidth)));
	}
	
	public void addLink(MobileSoftwareComponent u, MobileSoftwareComponent v, double latency, double bandwidth)
	{
		taskDependencies.addEdge(u, v, new ComponentLink(new QoSProfile(latency, bandwidth)));
	}
	
	public MobileSoftwareComponent getComponentById(String id){
		return componentList.get(id);
	}
	
	public double sampleLatency(){
		return ((SimulationSetup.lambdaLatency == 0)?
				Double.MAX_VALUE : 
					SimulationSetup.lambdaLatency 
					+ new ExponentialDistribution(SimulationSetup.lambdaLatency).sample()) ;
	}
	
	public abstract void sampleTasks();
	
	public abstract void sampleLinks();
	
	public abstract void setupTasks();
	
	public abstract void setupLinks();

}