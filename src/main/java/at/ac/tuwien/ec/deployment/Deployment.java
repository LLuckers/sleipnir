/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ec.deployment;



import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.reflect.macros.Infrastructure;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Lukas
 */
public class Deployment extends OffloadScheduling {

    HashMap<String, HashSet<String>> businessPolicies;

    public Deployment() {
        super();
        this.businessPolicies = new HashMap<String, HashSet<String>>();
    }

    public Deployment(OffloadScheduling deployment) {
        super(deployment);
        this.businessPolicies = new HashMap<String, HashSet<String>>();
    }

    @Override
    public Object clone(){
        Deployment d = new Deployment (this);
        d.setProviderCost(getProviderCost());
        d.businessPolicies = this.businessPolicies;
        d.setUserCost(getUserCost());
        d.setBatteryLifetime(this.getBatteryLifetime());
        d.setInfEnergyConsumption(this.getInfEnergyConsumption());
        d.setRunTime(this.getRunTime());
        return d;
    }

    /**
	 * 
	 */
	/*private static final long serialVersionUID = 5978753101322855324L;
	HashMap<String, HashSet<String>> businessPolicies;
    public Couple<Double, Double> consumedResources;
    public Cost deploymentMonthlyCost;
    private double userCost;
    private double energyConsumption;
    private double mobileEnergyBudget;
    private double energyCost;
    private double runTime;
    
    public Deployment(){
        super();
        businessPolicies = new HashMap<>();   
        //deploymentMonthlyCost = new Cost(0);
        energyConsumption = 0.0;
        energyCost = 0.0;
        userCost = 0.0;
        mobileEnergyBudget = SimulationSetup.batteryCapacity;
        runTime = 0.0;
    }

    Deployment(Deployment deployment) {
        super(deployment);
    }
    
    @Override
    public Object clone(){
        Deployment d = new Deployment (this);
        d.deploymentMonthlyCost = new Cost(deploymentMonthlyCost.getCost());
        d.consumedResources = this.consumedResources;
        d.businessPolicies = this.businessPolicies;
        d.energyConsumption = this.energyConsumption;
        d.mobileEnergyBudget = this.mobileEnergyBudget;
        d.energyCost = this.energyCost;
        d.runTime = this.runTime;
        return d;
    }
    
    @Override
    public String toString(){
        String result ="";

        for (SoftwareComponent s : super.keySet()){
            result+="["+s.getId()+"->" +super.get(s).getId()+"]" ;
        }
        
        return result;   
    }
    
    @Override
    public boolean equals(Object o){
        boolean result = true;
        Deployment d = (Deployment) o;
        for (SoftwareComponent s : this.keySet()){
            if (!this.get(s).equals(d.get(s))){
                result = false;
                break;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        String s = this.toString();
        hash = 47 * hash + s.hashCode();
        return hash;
    }

    public void addRuntime(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
    	double tmp = s.getRuntimeOnNode(n, I);
    	if(this.runTime < tmp)
    		this.runTime = tmp;
    }
    
    public void removeRuntime(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
    	this.runTime -= s.getRuntimeOnNode(super.get(s), I);
    }

    public void addCost(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I) {
        this.userCost += n.computeCost(s, I);
    }

    public void removeCost(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
        this.userCost -= n.computeCost(s, I);
    }
    
    public void addCost(SoftwareComponent s, ComputationalNode n, Infrastructure I) {
        this.deploymentMonthlyCost.add(n.computeCost(s, I));
    }
    
    public void removeCost(SoftwareComponent s, ComputationalNode n, Infrastructure I){
        this.deploymentMonthlyCost.remove(n.computeCost(s, I));
    }

	public void addEnergyConsumption(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
        MobileDevice mobileFromI = i.getMobileDevices().get(s.getUserId());

	    if(n instanceof MobileDevice &&
            n.getId() == mobileFromI.getId())
		{
            mobileFromI.removeFromBudget(computeCPUEnergyConsumption(s, n, i));
			this.mobileEnergyBudget = mobileFromI.getEnergyBudget();
		}
		else
		{
            mobileFromI.removeFromBudget(computeOffloadEnergy(s,n,i));
			this.energyConsumption += computeCPUEnergyConsumption(s,n,i);
			this.energyCost += computeEnergyCost(s,n,i);
			this.mobileEnergyBudget = mobileFromI.getEnergyBudget();
		}
	}

	private double computeEnergyCost(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		return computeCPUEnergyConsumption(s, n, i) * n.en.getEnergyCost();
	}

	private double computeOffloadEnergy(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		return i.getMobileDevice().getNetEnergyModel().computeNETEnergy(s, n, i);
	}

	private double computeCPUEnergyConsumption(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		CPUEnergyModel model = n.getCPUEnergyModel();
		return model.computeCPUEnergy(s,n,i);
	}

	public void removeEnergyConsumption(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {


	    if(n.isMobile()){
			i.getMobileDevice().addToBudget(computeCPUEnergyConsumption(s, n, i));
			this.mobileEnergyBudget = i.getMobileDevice().getEnergyBudget();
		}
		else
		{
			i.getMobileDevice().addToBudget(computeOffloadEnergy(s,n,i));
			this.energyConsumption -= computeCPUEnergyConsumption(s,n,i);
			this.mobileEnergyBudget = i.getMobileDevice().getEnergyBudget();
		}
	}


    public double getUserCost() {
        return userCost;
    }

    public void setUserCost(double userCost) {
        this.userCost = userCost;
    }

    public double getEnergyConsumption() {
        return energyConsumption;
    }

    public void setEnergyConsumption(double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public double getMobileEnergyBudget() {
        return mobileEnergyBudget;
    }

    public void setMobileEnergyBudget(double mobileEnergyBudget) {
        this.mobileEnergyBudget = mobileEnergyBudget;
    }

    public double getEnergyCost() {
        return energyCost;
    }

    public void setEnergyCost(double energyCost) {
        this.energyCost = energyCost;
    }

    public double getRunTime() {
        return runTime;
    }

    public void setRunTime(double runTime) {
        this.runTime = runTime;
    }*/
}
