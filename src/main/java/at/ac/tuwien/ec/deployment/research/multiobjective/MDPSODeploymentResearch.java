/*package at.ac.tuwien.ec.deployment.research.multiobjective;

import at.ac.tuwien.ec.algorithms.DAGResearch;
import at.ac.tuwien.ec.algorithms.mo.builder.MDPSODeploymentBuilder;
import at.ac.tuwien.ec.appmodel.MobileApplication;
import at.ac.tuwien.ec.appmodel.MobileSoftwareComponent;
import at.ac.tuwien.ec.deployment.DeploymentMutationOperator;
import at.ac.tuwien.ec.infrastructuremodel.MobileCloudInfrastructure;
import at.ac.tuwien.ec.problems.DeploymentProblem;
import at.ac.tuwien.ec.solutions.DeploymentSolution;
import di.unipi.socc.fogtorchpi.deployment.Deployment;
import di.unipi.socc.fogtorchpi.infrastructure.ComputationalNode;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.archive.impl.AbstractBoundedArchive;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MDPSODeploymentResearch extends DAGResearch {

    DeploymentProblem problem;
    Algorithm<List<DeploymentSolution>> algorithm;

    MutationOperator<DeploymentSolution> mutation;
    private double mutationProbability;

    public MDPSODeploymentResearch(MobileApplication A, MobileCloudInfrastructure I) {
        super(A, I);
        A.computeNodeRanks(I);
        this.problem = new DeploymentProblem(I, A);

        this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
        this.mutation = new DeploymentMutationOperator(mutationProbability);
    }

    @Override
    public ArrayList<Deployment> findDeployments(Deployment d) {
        ArrayList<Deployment> deployments = new ArrayList<Deployment>();
        List<DeploymentSolution> population = new ArrayList<DeploymentSolution>();
        try{
            int swarmSize = 100;
            List<AbstractBoundedArchive<DeploymentSolution>> archives = new ArrayList<>();
            archives.add(new CrowdingDistanceArchive<>(swarmSize));

            algorithm = new MDPSODeploymentBuilder(problem,
                    archives)
                    .setMutation(this.mutation)
                    .setMaxIterations(250)
                    .setSwarmSize(swarmSize)
                    .build();
            //AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
            //		.execute() ;
            algorithm.run();
            population = algorithm.getResult() ;
            Collections.sort(population, new RankingAndCrowdingDistanceComparator<>());
            int j = 0;
            for(int i = 0; i < population.size() && j < 20; i++)
            {
                if(!deployments.contains(population.get(i).getDeployment())
                        && (Integer)problem.numberOfViolatedConstraints.getAttribute(population.get(i)) == 0
                        && isComplete(population.get(i).getDeployment()))
                {
                    deployments.add(population.get(i).getDeployment());
                    j++;
                }
            }
        }
        catch(Throwable T) {
            System.err.println("Selection Error");
            population = algorithm.getResult();
            for (int i = 0; i < population.size(); i++) {
                if (!deployments.contains(population.get(i).getDeployment())
                        && (Integer) problem.numberOfViolatedConstraints.getAttribute(population.get(i)) == 0)
                    deployments.add(population.get(i).getDeployment());
            }
        }
		finally {
            System.out.println(algorithm.getName()+" found " + deployments.size() + " results!");
            return deployments;
        }
    }

    @Override
    protected ComputationalNode findTarget(Deployment deployment, MobileSoftwareComponent msc) {
        return null;
    }
}
*/