package at.ac.tuwien.ec.deployment.utils;

import at.ac.tuwien.ec.deployment.Deployment;
import at.ac.tuwien.ec.deployment.solution.DeploymentSolution;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import org.apache.commons.lang.math.RandomUtils;
import org.uma.jmetal.operator.MutationOperator;


public class DeploymentMutationOperator implements MutationOperator<DeploymentSolution>{

	private RandomUtils mutationRandomGenerator = SimulationSetup.rand;
	private double mutationProbability;
	
	public DeploymentMutationOperator(double mutationProbability){
		this.mutationProbability = mutationProbability;
	}
	
	@Override
	public DeploymentSolution execute(DeploymentSolution arg0) {
		Deployment d = arg0.getDeployment();
		MobileSoftwareComponent n1,n2;
		int idx1,idx2;
		boolean ex = false;
		if (mutationRandomGenerator.nextDouble() < mutationProbability) {
			do
			{
				idx1 = SimulationSetup.rand.nextInt(d.size());
				while((idx2 = SimulationSetup.rand.nextInt(d.size())) == idx1)
					;

				n1 = (MobileSoftwareComponent) d.keySet().toArray()[idx1];
				n2 = (MobileSoftwareComponent) d.keySet().toArray()[idx2];
				ex = n1.isOffloadable() && n2.isOffloadable();
			}
			while(!n1.isOffloadable() && !n2.isOffloadable() && !ex);

			ComputationalNode cn1 = (ComputationalNode) d.get(n1);
			ComputationalNode cn2 = (ComputationalNode) d.get(n2);
			
			arg0.setVariableValue(idx1, cn2);
			arg0.setVariableValue(idx2, cn1);
		}
		return new DeploymentSolution(d,arg0.getApplication(),arg0.getInfrastructure());
	}

}
