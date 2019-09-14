package at.ac.tuwien.ec.deployment.algorithms.mo.interactive;

import at.ac.tuwien.ec.deployment.algorithms.mo.MDPSODeployment;
import at.ac.tuwien.ec.deployment.problem.dynamic.DynamicDeploymentProblem;
import at.ac.tuwien.ec.deployment.solution.DeploymentSolution;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.archive.impl.AbstractBoundedArchive;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetalsp.InteractiveAlgorithm;
import org.uma.jmetalsp.util.restartstrategy.RestartStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the MDPSO algorithm described in:
 * An Efficient Algorithm of Discrete Particle Swarm Optimization for Multi-Objective Task Assignment
 * 2016. DOI: 10.1587/transinf.2016PAP0032
 *
 * And adapts it to work DAG Computations within the area of Mobile Cloud Edge Offloading described in:
 * First Hop Mobile Offloading of DAG Computations
 * 2018. DOI: 10.1109/CCGRID.2018.00023
 *
 * @author Lukas Leitzinger <e1327140@student.tuwien.ac.at>
 */
@SuppressWarnings("serial")
public class iMDPSODeployment<S extends DeploymentSolution> extends MDPSODeployment<S>
        implements InteractiveAlgorithm<S, List<S>> {
    private DynamicDeploymentProblem<S> dynamicProblem;

    /**
     * Constructor of the MDPSO Algorithm in the specific area of DAG Computations in Mobile Cloud Edge Offloading
     *
     * @param problem          instance of the DeploymentProblem
     * @param leaders
     * @param swarmSize        the size of the swarm, which will be initially created
     * @param maxIterations    for the stopping criteria
     * @param mutationOperator MutationOperator to execute the perturbation (or turbulence) within PSO
     * @param evaluator        Instance of a SolutionListEvaluator to evaluate the objectives within the particle
     * @param L
     */
    public iMDPSODeployment(DynamicDeploymentProblem<S> problem, List<AbstractBoundedArchive<S>> leaders,
                            int swarmSize, int maxIterations, MutationOperator<S> mutationOperator, SolutionListEvaluator<S> evaluator, int[] L) {
        super(problem, leaders,
                swarmSize, maxIterations, mutationOperator, evaluator, L);
        dynamicProblem = problem;
    }

    public DynamicDeploymentProblem<S> getDynamicProblem() {
        return dynamicProblem;
    }


    @Override
    public void restart(RestartStrategy restartStrategy) {
        restartStrategy.restart(getSwarm(), getDynamicProblem());
        SolutionListUtils.removeSolutionsFromList(getResult(), getResult().size());
        evaluator.evaluate(getSwarm(), getDynamicProblem());
        initializeVelocity(getSwarm());
        initializeParticlesMemory(getSwarm());
        cleanLeaders();
        initProgress();
    }

    private void cleanLeaders() {
        super.leaders = new ArrayList<>();
        super.leaders.add(new CrowdingDistanceArchive<>(swarmSize));
        initializeLeader(getSwarm());
    }

    @Override
    public List<S> getPopulation() {
        return super.getSwarm();
    }

    @Override
    public void compute() {
        updateVelocity(super.getSwarm());
        updatePosition(super.getSwarm());
        perturbation(super.getSwarm());
        evaluate(super.getSwarm());
        super.setSwarm(super.getSwarm());
        updateLeaders(super.getSwarm());
        updateParticlesMemory(super.getSwarm());
        updateProgress();
    }

    @Override
    public List<S> initializePopulation() {
        setSwarm(createInitialSwarm());
        setSwarm(evaluateSwarm(getSwarm()));
        initializeVelocity(getSwarm());
        initializeParticlesMemory(getSwarm());
        initializeLeader(getSwarm());
        return super.getSwarm();
    }

    @Override
    public void evaluate(List<S> population) {
        setSwarm(evaluator.evaluate(getSwarm(), getDynamicProblem()));
    }

    @Override
    public void updatePointOfInterest(List<Double> newReferencePoints) {

        if(newReferencePoints != null &&
                newReferencePoints.size() >= getDynamicProblem().getNumberOfObjectives() )
            super.setObjectiveWeights(newReferencePoints);

        /*referencePoints = new ArrayList<>();
        int numberOfPoints = newReferencePoints.size() / getDynamicProblem().getNumberOfObjectives();
        if (numberOfPoints == 1) {
            referencePoints.add(newReferencePoints);
        } else {
            int i = 0;
            while (i < newReferencePoints.size()) {
                int j = 0;
                List<Double> aux = new ArrayList<>();

                while (j < getDynamicProblem().getNumberOfObjectives()) {
                    aux.add(newReferencePoints.get(i));
                    j++;
                    i++;
                }
                referencePoints.add(aux);

            }
        }
        cleanLeaders();
        changeReferencePoints(referencePoints);*/
    }

    @Override
    public String getName() {
        return "iMDPSO Deployment";
    }

}
