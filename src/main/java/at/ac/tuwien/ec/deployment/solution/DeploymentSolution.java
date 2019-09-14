package at.ac.tuwien.ec.deployment.solution;


import at.ac.tuwien.ec.deployment.Deployment;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DeploymentSolution implements PermutationSolution<ComputationalNode>{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Deployment deployment;
	private HashMap<Object,Object> solutionAttributes;
	private MobileApplication A;
	private MobileCloudInfrastructure I;
	private double runTime = 0.0, cost = 0.0, battery = 0.0;

	public DeploymentSolution(Deployment deployment, MobileApplication A, MobileCloudInfrastructure I)
	{
		this.deployment = (Deployment) deployment.clone();
		this.A = A;//.clone();
		this.I = I;//.clone();
		solutionAttributes = new HashMap<Object,Object>();
		solutionAttributes.put("feasible", true);
	}

	public DeploymentSolution(DeploymentSolution s){
		this(s.deployment,s.A,s.I);

		int i;
		for(i = 0; i < getNumberOfVariables(); ++i) {
			this.setVariableValue(i, s.getVariableValue(i));
		}

		for(i = 0; i < getNumberOfObjectives(); ++i) {
			double objectiveValue = s.getObjective(i);
			if(i == 2){
				//Objective battery must be reverted during copy
				objectiveValue = SimulationSetup.batteryCapacity - objectiveValue;
			}
			this.setObjective(i, objectiveValue);
		}

		this.solutionAttributes = new HashMap(s.solutionAttributes);
	}

	@Override
	public Object getAttribute(Object arg0) {
		return solutionAttributes.get(arg0);
	}

	@Override
	public Map<Object, Object> getAttributes() {
		return solutionAttributes;
	}

	@Override
	public int getNumberOfObjectives() {
		return 3;
	}
	@Override
	public int getNumberOfVariables() {
		return deployment.size();
	}
	@Override
	public double getObjective(int arg0) {
		switch(arg0)
		{
			case 0: return runTime;
			case 1: return cost;
			case 2: return battery;
			default: return runTime;
		}
	}

	@Override
	public double[] getObjectives() {
		double[] objectives= {runTime,cost,battery};
		return objectives;
	}

	@Override
	public String getVariableValueString(int arg0) {
		return deployment.get(deployment.keySet().toArray()[arg0]).toString();
	}
	@Override
	public void setAttribute(Object arg0, Object arg1) {
		solutionAttributes.put(arg0, arg1);
	}

	@Override
	public void setObjective(int arg0, double arg1) {
		switch(arg0)
		{
			case 0:
				deployment.setRunTime(arg1);
				runTime = arg1;
				break;
			case 1:
				deployment.setUserCost(arg1);
				cost = arg1;
				break;
			case 2:
				deployment.setBatteryLifetime(arg1);
				battery = SimulationSetup.batteryCapacity - arg1;
		}
	}

	@Override
	public ComputationalNode getVariableValue(int arg0) {
		return (ComputationalNode) deployment.get(deployment.keySet().toArray()[arg0]);
	}

	//@Override
	public List<ComputationalNode> getVariables() {
		return ((List<ComputationalNode>)(List<?>)deployment.values());
	}

	@Override
	public void setVariableValue(int arg0, ComputationalNode arg1) {
		MobileSoftwareComponent sc = (MobileSoftwareComponent) deployment.keySet().toArray()[arg0];
		ComputationalNode actual = (ComputationalNode) deployment.get(sc);
		if(actual!=null)
			replace(deployment,sc,actual,arg1);
		else
			deploy(deployment,sc,arg1);
		//sc.setRuntime(sc.getRuntimeOnNode(arg1, I));
	}

	@Override
	public DeploymentSolution copy() {
		return new DeploymentSolution(this);
	}

	public Deployment getDeployment()
	{
		return deployment;
	}

	private void deploy(Deployment deployment, MobileSoftwareComponent s, ComputationalNode n) {
		//SimulationSetup.logger.info("Adding " + s.getId() + " = " + deployment.containsKey(s));
		deployment.put(s, n);
		deployment.addCost(s,n, I);
		deployment.addEnergyConsumption(s, n, I);
		//deployment.addRuntime(s, n, I);
		//System.out.println(deployment + " " + deployment.size());
		n.deploy(s);
		//TODO deployLinks(deployment, s, n);
	}

	private void replace(Deployment deployment, MobileSoftwareComponent s, ComputationalNode oldNode, ComputationalNode newNode) {
		if (deployment.containsKey(s)) {
			oldNode.undeploy(s);
			//deployment.removeRuntime(s, n, I);
			//deployment.removeCost(s, n, I);
			//deployment.removeEnergyConsumption(s, n, I);
			//TODO undeployLinks(deployment, s, oldNode);
			deployment.replace(s,newNode);
			//deployment.addCost(s,n, I);
			//deployment.addEnergyConsumption(s, n, I);
			newNode.deploy(s);
			//TODO deployLinks(deployment, s, newNode);
		}
		// System.out.println("UNDEP"+deployment);
	}

	/*private void deployLinks(Deployment deployment, MobileSoftwareComponent s, ComputationalNode n) {
		for (MobileSoftwareComponent c : deployment.keySet()) {
			ComputationalNode m = deployment.get(c);
			Couple couple1 = new Couple(c.getId(), s.getId());
			Couple couple2 = new Couple(s.getId(), c.getId());

			if (A.L.containsKey(couple1) && A.L.containsKey(couple2)) {
				QoSProfile req1 = A.L.get(couple1); //c,s
				QoSProfile req2 = A.L.get(couple2); //s,c
				Couple c1 = new Couple(m.getId(), n.getId()); // m,n
				Couple c2 = new Couple(n.getId(), m.getId()); // n,m
				if (I.L.containsKey(c1)) {
					QoSProfile pl1 = I.L.get(c1);
					QoSProfile pl2 = I.L.get(c2);
					pl1.setBandwidth(pl1.getBandwidth() - req1.getBandwidth());
					pl2.setBandwidth(pl2.getBandwidth() - req2.getBandwidth());
				}
			}
		}

		for (ThingRequirement t : s.Theta) {
			ExactThing e = (ExactThing) t;
			if (n.isReachable(e.getId(), I, e.getQNodeThing(), e.getQThingNode())) {
				Couple c1 = new Couple(n.getId(), e.getId()); //c1 nodeThing

				QoSProfile pl1 = I.L.get(c1);
				QoSProfile pl2 = I.L.get(new Couple(e.getId(), n.getId()));

				pl1.setBandwidth(pl1.getBandwidth() - e.getQNodeThing().getBandwidth());
				pl2.setBandwidth(pl2.getBandwidth() - e.getQThingNode().getBandwidth());

			}
		}
	}

	private void undeployLinks(Deployment deployment, MobileSoftwareComponent s, ComputationalNode n) {
		for (MobileSoftwareComponent c : deployment.keySet()) {
			ComputationalNode m = deployment.get(c);
			Couple couple1 = new Couple(c.getId(), s.getId());
			Couple couple2 = new Couple(s.getId(), c.getId());

			if (A.L.containsKey(couple1) && A.L.containsKey(couple2)) {
				QoSProfile al1 = A.L.get(couple1);
				QoSProfile al2 = A.L.get(couple2);
				Couple c1 = new Couple(m.getId(), n.getId());
				Couple c2 = new Couple(n.getId(), m.getId());
				if (I.L.containsKey(c1)) {
					QoSProfile pl1 = I.L.get(c1);
					QoSProfile pl2 = I.L.get(c2);

					pl1.setBandwidth(pl1.getBandwidth() + al1.getBandwidth());
					pl2.setBandwidth(pl2.getBandwidth() + al2.getBandwidth());
				}
			}

		}

		for (ThingRequirement t : s.Theta) {
			ExactThing e = (ExactThing) t;
			//System.out.println("Request" + e);
			if (n.isReachable(e.getId(), I, e.getQNodeThing(), e.getQThingNode())) {
				Couple c1 = new Couple(n.getId(), e.getId());

				QoSProfile pl1 = I.L.get(c1);
				QoSProfile pl2 = I.L.get(new Couple(e.getId(), n.getId()));

				pl1.setBandwidth(pl1.getBandwidth() + e.getQNodeThing().getBandwidth());
				pl2.setBandwidth(pl2.getBandwidth() + e.getQThingNode().getBandwidth());

			}
		}
	}*/

	public MobileApplication getApplication() {
		return A;
	}

	public MobileCloudInfrastructure getInfrastructure() {
		return I;
	}

	public Number getLowerBound(int index) {
		return 0.0;
	}
}