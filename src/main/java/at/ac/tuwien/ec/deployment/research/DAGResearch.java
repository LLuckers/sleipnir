package at.ac.tuwien.ec.deployment.research;



import at.ac.tuwien.ec.deployment.Deployment;
import at.ac.tuwien.ec.deployment.simulation.Search;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public abstract class DAGResearch extends Search {
	
	public DAGResearch(MobileApplication A, MobileCloudInfrastructure I) {
		super(A, I);
		
		// TODO Auto-generated constructor stub
	}
	
	public DAGResearch(MobileApplication A, MobileCloudInfrastructure I,
			HashMap<String, HashSet<String>> businessPolicies) {
		super(A, I, businessPolicies);
		// TODO Auto-generated constructor stub
	}

	protected ArrayList<ComputationalNode> compatibleNodes(Deployment d, MobileSoftwareComponent msc) {
		ArrayList<ComputationalNode> cmps = new ArrayList<ComputationalNode>();
		
		for(ComputationalNode cn : currentInfrastructure.getCloudNodes().values())
			if(isValid(d,msc,cn))
				cmps.add(cn);
		for(ComputationalNode cn : currentInfrastructure.getEdgeNodes().values())
			if(isValid(d,msc,cn))
				cmps.add(cn);
		if(isValid(d,msc,currentInfrastructure.getMobileDevice(msc.getUserId())))
			cmps.add(currentInfrastructure.getMobileDevice(msc.getUserId()));
			
		return cmps;
	}
	
	@Override
	public ArrayList<Deployment> findDeployments(Deployment d) {
		return searchOnDAG(d);
     }


	public abstract ArrayList<Deployment> searchOnDAG(Deployment deployment);
	/*public ArrayList<Deployment> searchOnDAG(Deployment deployment) {
		ArrayList<MobileSoftwareComponent> scheduledNodes 
			= new ArrayList<MobileSoftwareComponent>();
		ArrayList<SoftwareComponent> eligibleNodes = new ArrayList<SoftwareComponent>();
		ArrayList<Deployment> deployments = new ArrayList<Deployment>();
		double currentRuntime = 0;
		
		while(!isComplete(deployment)){
			if(!scheduledNodes.isEmpty())
			{
				MobileSoftwareComponent firstTaskToTerminate = getFirstToTerminate(scheduledNodes);
				//SimulationSetup.logger.warning(firstTaskToTerminate.getId() + " terminated, freeing resources on " + deployment.get(firstTaskToTerminate).getId());
				currentRuntime = firstTaskToTerminate.getRunTime();
				//SimulationConstants.logger.info("Current deployment runtime: "+ deployment.runTime);
				//SimulationSetup.logger.warning("Resorces on " + deployment.get(firstTaskToTerminate).getId() + " before Undeploy: " + deployment.get(firstTaskToTerminate).getHardware());
				removeEdgesOutgoingFrom(firstTaskToTerminate);
				deployment.get(firstTaskToTerminate).getHardware().undeploy(firstTaskToTerminate.getHardwareRequirements());
				//SimulationConstants.logger.warning(deployment.get(firstTaskToTerminate).getId() + " new resources: "+ deployment.get(firstTaskToTerminate).getHardware() + " mobile battery: " + I.getMobileDevice().getEnergyBudget());
				tmpApp..S.remove(firstTaskToTerminate);
				scheduledNodes.remove(firstTaskToTerminate);
				firstTaskToTerminate = null;
			}
			MobileSoftwareComponent toSchedule;
			ComputationalNode target;
			ArrayList<MobileSoftwareComponent> newNodes = selectElegibleNodes();
			boolean progress = false;
		
			for(SoftwareComponent sc : newNodes)
				if(!eligibleNodes.contains(sc))
				{
					progress = true;
					eligibleNodes.add((MobileSoftwareComponent)sc);
				}				
			for(int i = 0; i < eligibleNodes.size(); i++)
			{
				SimulationConstants.logger.warning("Trying to schedule " + eligibleNodes.get(i).getId() + ". " + scheduledNodes.size() + " to schedule." );
				toSchedule = (MobileSoftwareComponent) eligibleNodes.get(i);
				if(!toSchedule.isOffloadable())
					if(!isValid(deployment, toSchedule, I.getMobileDevice(toSchedule.getUid())))
						if(scheduledNodes.isEmpty())
							return null;
						else continue;
					else
						target = I.getMobileDevice(toSchedule.getUid());
				else  
					target = findTarget(deployment,(MobileSoftwareComponent) toSchedule);
				if(target == null)
					continue;
				
				//eligibleNodes.remove(toSchedule);
				deploy(deployment,toSchedule,target);
				setRunningTime(deployment, (MobileSoftwareComponent) toSchedule, target);
		        scheduledNodes.add((MobileSoftwareComponent) toSchedule);
			}
			for(int i = 0; i < scheduledNodes.size(); i++)
				if(eligibleNodes.contains(scheduledNodes.get(i)))
					eligibleNodes.remove(scheduledNodes.get(i));
			
			String tmp = "";
			for(SoftwareComponent s : deployment.keySet())
				tmp += s.getId() + " ";
			//SimulationConstants.logger.info("Deployment contains: " + tmp );
			if(!isComplete(deployment) && scheduledNodes.isEmpty() && !progress )
			{
				deployment = null;
				break;
			}
		}
		SimulationConstants.logger.info("Deployment complete: " + deployment);
		
		scheduledNodes = null;
		eligibleNodes = null;
		if(!isComplete(deployment))
			deployment = null;
		
		else 
			deployment.runTime += currentRuntime;
		SimulationConstants.logger.info("Deployment data: runtime=" + deployment.runTime 
				+ " battery lifetime: " + deployment.mobileEnergyBudget
				+ " cost: " + deployment.deploymentMonthlyCost.getCost());
		if(deployment != null)
			deployments.add(deployment);
		return deployments;
	}*/
	
	/*protected void removeEdgesOutgoingFrom(MobileSoftwareComponent firstTaskToTerminate) {

		for(Couple<String,String> edge : tmpApp.L.keySet()){
			if(edge.getA().equals(firstTaskToTerminate))
				tmpApp.L.remove(edge);
		}
		SimulationConstants.logger.info("Removing "+firstTaskToTerminate.getId());
		tmpApp.S.remove(firstTaskToTerminate);
    }



	protected void setRunningTime(Deployment deployment, MobileSoftwareComponent msc, ComputationalNode target) {
		double currRuntime = 0;
		for(MobileSoftwareComponent sc : A.getPredecessors(msc))
		{
			String id1 = sc.getId();
			String id2 = msc.getId();
			double tmpRuntime = sc.getRuntime();
			if(tmpRuntime > currRuntime)
				currRuntime = tmpRuntime;
		}
		msc.setRuntime((currRuntime + msc.getRuntimeOnNode(target, I)));
		deployment.runTime = msc.getRuntime();
	}*/

	protected MobileSoftwareComponent getFirstToTerminate(ArrayList<MobileSoftwareComponent> scheduledNodes) {
		if(scheduledNodes.isEmpty())
			return null;
		MobileSoftwareComponent first = scheduledNodes.get(0);
		double runtime = first.getRunTime();
		for(int i = 1; i < scheduledNodes.size(); i++)
		{
			MobileSoftwareComponent msc = scheduledNodes.get(i);
			if(msc.getRunTime() < runtime)
			{
				first = msc;
				runtime = msc.getRunTime();
			}
		}
		scheduledNodes.remove(first);
		return first;
	}

	protected abstract ComputationalNode findTarget(Deployment deployment, MobileSoftwareComponent msc);

	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		return findDeployments(new Deployment());
	}
}
