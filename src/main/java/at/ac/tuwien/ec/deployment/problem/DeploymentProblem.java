package at.ac.tuwien.ec.deployment.problem;


import at.ac.tuwien.ec.deployment.Deployment;
import at.ac.tuwien.ec.deployment.research.RandomDeploymentSearch;
import at.ac.tuwien.ec.deployment.solution.DeploymentSolution;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.algorithms.OffloadScheduler;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import java.util.ArrayList;

/**
 * This class represents the Deployment Problem we are facing
 *
 *
 */
public class DeploymentProblem<S extends DeploymentSolution>
		extends OffloadScheduler
		implements Problem<S>
{
	/**
	 *
	 */
	public OverallConstraintViolation<S> overallConstraintViolationDegree =
			new OverallConstraintViolation<S>();
	public NumberOfViolatedConstraints<S> numberOfViolatedConstraints =
			new NumberOfViolatedConstraints<S>();
	
	private static final long serialVersionUID = 1L;
		
	
	public DeploymentProblem(MobileCloudInfrastructure I, MobileApplication A)
	{
		super();
		currentApp = A;
		currentInfrastructure = I;
	}
	
	@Override
	public S createSolution() {
		Deployment dep = new Deployment();
		RandomDeploymentSearch rs = new RandomDeploymentSearch(currentApp,currentInfrastructure);
		ArrayList<Deployment> deps;
		do deps = rs.findDeployments(dep);
		while(deps.size()==0);
		return (S)new DeploymentSolution(deps.get(0),currentApp,currentInfrastructure);
	}

	@Override
	public void evaluate(S currDep) {
		double cost = 0;
		double batteryBudget = SimulationSetup.batteryCapacity;
		Deployment d = currDep.getDeployment();
		double runTime = 0.0;
		for(int i = 0; i < d.keySet().size(); i++){
			MobileSoftwareComponent sc = (MobileSoftwareComponent) d.keySet().toArray()[i];
			ComputationalNode n = currDep.getVariableValue(i);
			ArrayList<MobileSoftwareComponent> preds = currentApp.getPredecessors(sc);
			double mPreds = 0.0;
			for(MobileSoftwareComponent ps:preds)
			{
				if(ps.getRunTime() > mPreds)
					mPreds = ps.getRunTime();
			}
			sc.setRunTime(mPreds + sc.getRuntimeOnNode(n, currentInfrastructure));
			runTime = sc.getRunTime();
			cost += n.computeCost(sc, currentInfrastructure);
			batteryBudget -= (n instanceof MobileDevice)?
					currentInfrastructure.getMobileDevice(sc.getUserId()).getCPUEnergyModel().computeCPUEnergy(sc, n, currentInfrastructure):
					currentInfrastructure.getMobileDevice(sc.getUserId()).getNetEnergyModel().computeNETEnergy(sc, n, currentInfrastructure);
		}
		currDep.setObjective(0, runTime);
		currDep.setObjective(1, cost);
		currDep.setObjective(2, batteryBudget);

		evaluateConstraints(currDep);
	}

	public void evaluateConstraints(S arg0) {
		int violatedConstraints = 0;
		double overAllConstraintViolation = 0.0;

		Object[] comps = arg0.getDeployment().keySet().toArray();

		boolean rankConstraintViolation = false;
		for(int i = 0; i < comps.length - 1; i++)
		{
			if(((MobileSoftwareComponent)comps[i]).getRank() < ((MobileSoftwareComponent)comps[i+1]).getRank())
			{
				if(!rankConstraintViolation){
					rankConstraintViolation = true;
					violatedConstraints++;
				}
				overAllConstraintViolation -= 10;
				break;
			}
		}
		
		Deployment temp = new Deployment();
		boolean hardwareConstraintViolation = false;
		for(int i = 0; i < arg0.getNumberOfVariables(); i++)
		{
			ComputationalNode cn = arg0.getVariableValue(i);
			MobileSoftwareComponent msc = (MobileSoftwareComponent) comps[i];

			if(!isValid(temp,msc,cn))
			{
				if(!hardwareConstraintViolation)
				{
					hardwareConstraintViolation = true;
					violatedConstraints++;					
				}
				overAllConstraintViolation += cn.getCapabilities().getAvailableCores() - msc.getHardwareRequirements().getCores();
				break;
			}
			//else
				//deploy(temp,msc,cn);
		}
		
		for(int i = 0; i < arg0.getNumberOfVariables(); i++)
		{
			ComputationalNode cn = arg0.getVariableValue(i);
			MobileSoftwareComponent msc = (MobileSoftwareComponent) comps[i];
			undeploy(temp,msc,cn);
		}
		
		boolean offloadabilityConstraintViolation = false;
		for(int i = 0; i < arg0.getNumberOfVariables(); i++)
		{
			MobileSoftwareComponent msc = ((MobileSoftwareComponent)comps[i]);
			ComputationalNode cn = (ComputationalNode) arg0.getDeployment().get(msc);
			if(!msc.isOffloadable() && !(cn.equals(currentInfrastructure.getMobileDevice())))
			{
				if(!offloadabilityConstraintViolation)
				{
				violatedConstraints++;
				offloadabilityConstraintViolation = true;
				}
				overAllConstraintViolation -= 100.0;
			}
		}
		
		if(arg0.getDeployment().getBatteryLifetime() < 0.0)
		{
			violatedConstraints++;
			overAllConstraintViolation = arg0.getDeployment().getBatteryLifetime();
		}
		
		if(!(Double.isFinite(arg0.getObjective(0)))
				|| !Double.isFinite(arg0.getObjective(1))
				|| !Double.isFinite(arg0.getObjective(2)))
		{
			violatedConstraints++;
		}
		numberOfViolatedConstraints.setAttribute(arg0, violatedConstraints);
		overallConstraintViolationDegree.setAttribute(arg0,overAllConstraintViolation);
	}

	@Override
	public String getName() {
		return "FirstHopDagOffloading";
	}

	/**
	 * Objectives in the DeploymentProblem are running time, battery lifetime and the user cost model
	 * @return 3
	 */
	@Override
	public int getNumberOfObjectives() {
		return 3;
	}

	/**
	 * Variables in the DeploymentProblem are the SoftwareComponents S (tasks) from the MobileApplication A
	 * @return Number of SoftwareComponents S (tasks) from the MobileApplication A
	 */
	@Override
	public int getNumberOfVariables() {
		return currentApp.componentList.size();
	}

	@Override
	public int getNumberOfConstraints() {
		return 6;
	}

	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		return null;
	}
}
