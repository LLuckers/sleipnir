package at.ac.tuwien.ec.deployment.research;


import at.ac.tuwien.ec.deployment.Deployment;
import at.ac.tuwien.ec.deployment.research.utils.NodeRankComparator;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class RandomDeploymentSearch extends DAGResearch{
		
	public RandomDeploymentSearch(MobileApplication A, MobileCloudInfrastructure I) {
		super(A, I);
		/*for(SoftwareComponent msc : A.S)
			System.out.println(msc.getId() + ":" + ((MobileSoftwareComponent)msc).getNodeRank() );
			*/
	}
	
	public ArrayList<Deployment> searchOnDAG(Deployment deployment){
		ArrayList<MobileSoftwareComponent> scheduledNodes
		= new ArrayList<MobileSoftwareComponent>();
		PriorityQueue<MobileSoftwareComponent> tasks = 
				new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator(currentApp,currentInfrastructure));
		ArrayList<Deployment> deployments = new ArrayList<Deployment>();
		
		deployment = new Deployment();
		for(SoftwareComponent sc : currentApp.componentList.values())
			tasks.add((MobileSoftwareComponent) sc);
		double currentRuntime = 0;

		while(!isComplete(deployment)){
			if(!scheduledNodes.isEmpty())
			{
				MobileSoftwareComponent firstTaskToTerminate = getFirstToTerminate(scheduledNodes);
				currentRuntime = firstTaskToTerminate.getRunTime();
				//deployment.get(firstTaskToTerminate).getHardware().undeploy(firstTaskToTerminate.getHardwareRequirements());
				scheduledNodes.remove(firstTaskToTerminate);
			}
			MobileSoftwareComponent currTask = tasks.poll();
			
			//System.out.print(currTask.getId() + " ");
			if(currTask.getId().equals("root"))
			{
				currTask = tasks.poll();
				continue;
			}
			ComputationalNode target = (currTask.isOffloadable())? getRandomTarget(currentInfrastructure,currTask) : currentInfrastructure.getMobileDevice(currTask.getUserId());
			deployment.put(currTask, target);
			target.deploy(currTask);
			//TODO deployLinks(deployment,currTask,target);
			//deploy(deployment,currTask,target);
			//setRunningTime(deployment, (MobileSoftwareComponent)currTask, target);
			scheduledNodes.add(currTask);
		}
		
		while(!scheduledNodes.isEmpty())
		{
			MobileSoftwareComponent firstTaskToTerminate = getFirstToTerminate(scheduledNodes);
			currentRuntime = firstTaskToTerminate.getRunTime();
			deployment.get(firstTaskToTerminate).getCapabilities().undeploy(firstTaskToTerminate);
			scheduledNodes.remove(firstTaskToTerminate);
		}
		//deployment.runTime += currentRuntime;
		/*SimulationConstants.logger.info("Deployment data: runtime=" + deployment.runTime
				+ " battery lifetime: " + deployment.mobileEnergyBudget
				+ " cost: " + deployment.deploymentMonthlyCost.getCost());*/
		
		if(deployment != null && isComplete(deployment))
			deployments.add(deployment);
		return deployments;
	}

	private ComputationalNode getRandomTarget(MobileCloudInfrastructure i,SoftwareComponent cmp) {
		final int mobileChoice = 0;
		final int cloudChoice = 1;
		final int edgeChoice = 2;
		int infChoice = SimulationSetup.rand.nextInt((SimulationSetup.cloudOnly)? 2 : 3 );
		switch(infChoice)
		{
		case mobileChoice : return currentInfrastructure.getMobileDevice(cmp.getUserId());
		case cloudChoice : return (ComputationalNode) (i.getCloudNodes().values().toArray())[SimulationSetup.rand.nextInt(currentInfrastructure.getCloudNodes().size())];
		case edgeChoice : return (ComputationalNode) (i.getEdgeNodes().values().toArray())[SimulationSetup.rand.nextInt(currentInfrastructure.getEdgeNodes().size())];
		}
		return null;
	}

	@Override
	protected ComputationalNode findTarget(Deployment deployment, MobileSoftwareComponent msc) {
		// TODO Auto-generated method stub
		return null;
	}
}
