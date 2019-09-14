package at.ac.tuwien.ec.deployment.algorithms.mo.builder;

import at.ac.tuwien.ec.deployment.algorithms.mo.MDPSODeployment;
import at.ac.tuwien.ec.deployment.problem.DeploymentProblem;
import at.ac.tuwien.ec.deployment.solution.DeploymentSolution;
import at.ac.tuwien.ec.deployment.utils.DeploymentMutationOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.archive.impl.AbstractBoundedArchive;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * This class is a builder for an instance of the MDPSO algorithm ...
 * (described in: An Efficient Algorithm of Discrete Particle Swarm Optimization for Multi-Objective Task Assignment
 * 2016. DOI: 10.1587/transinf.2016PAP0032)
 *
 * ... to solve DAG Computations within the area of Mobile Cloud Edge Offloading
 * (described in: First Hop Mobile Offloading of DAG Computations
 * 2018. DOI: 10.1109/CCGRID.2018.00023)
 *
 * @author Lukas Leitzinger <e1327140@student.tuwien.ac.at>
 */
public class MDPSODeploymentBuilder implements AlgorithmBuilder<MDPSODeployment<DeploymentSolution>> {
    public enum MDPSOVariant { DAGComputation }

    private DeploymentProblem problem;

    private int L1;
    private int L2;
    private int L3;

    private int swarmSize;
    private int maxIterations;

    private double mutationProbability;

    protected int archiveSize;

    protected MutationOperator<DeploymentSolution> mutationOperator;

    protected List<AbstractBoundedArchive<DeploymentSolution>> leaders;

    protected SolutionListEvaluator<DeploymentSolution> evaluator;

    protected MDPSODeploymentBuilder.MDPSOVariant variant ;

    public MDPSODeploymentBuilder(DeploymentProblem problem, List<AbstractBoundedArchive<DeploymentSolution>> leaders) {
        this.problem = problem;
        this.leaders = leaders;

        swarmSize = 100;
        maxIterations = 250;

        this.L1 = 25;
        this.L2 = 25;
        this.L3 = 25;

        this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
        mutationOperator = new DeploymentMutationOperator(mutationProbability);;
        evaluator = new SequentialSolutionListEvaluator<DeploymentSolution>() ;

        this.variant = MDPSODeploymentBuilder.MDPSOVariant.DAGComputation ;
    }

    /* Getters */
    public int getSwarmSize() {
        return swarmSize;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getL1() {
        return L1;
    }

    public int getL2() {
        return L2;
    }

    public int getL3() {
        return L3;
    }

    public MutationOperator<DeploymentSolution> getMutation() {
        return mutationOperator;
    }

    /* Setters */
    public MDPSODeploymentBuilder setSwarmSize(int swarmSize) {
        this.swarmSize = swarmSize;

        return this;
    }

    public MDPSODeploymentBuilder setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;

        return this;
    }

    public MDPSODeploymentBuilder setMutation(MutationOperator<DeploymentSolution> mutation) {
        mutationOperator = mutation;

        return this;
    }

    public MDPSODeploymentBuilder setL1(int l1) {
        L1 = l1;

        return this;
    }

    public MDPSODeploymentBuilder setL2(int l2) {
        L2 = l2;

        return this;
    }

    public MDPSODeploymentBuilder setL3(int l3) {
        L3 = l3;

        return this;
    }

    public MDPSODeploymentBuilder setRandomGenerator(PseudoRandomGenerator randomGenerator) {
        JMetalRandom.getInstance().setRandomGenerator(randomGenerator);

        return this;
    }

    public MDPSODeploymentBuilder setSolutionListEvaluator(SolutionListEvaluator<DeploymentSolution> evaluator) {
        this.evaluator = evaluator ;

        return this ;
    }

    public MDPSODeploymentBuilder setVariant(MDPSODeploymentBuilder.MDPSOVariant variant) {
        this.variant = variant;

        return this;
    }

    public MDPSODeployment build() {
        if (variant.equals(MDPSODeploymentBuilder.MDPSOVariant.DAGComputation)) {
            return new MDPSODeployment(problem, leaders, swarmSize, maxIterations,
                    mutationOperator, evaluator, new int[]{L1, L2, L3});
        } else {
            /*return new SMPSODeploymentAlgorithmMeasures(problem, swarmSize, leaders, mutationOperator, maxIterations, r1Min, r1Max,
                    r2Min, r2Max, c1Min, c1Max, c2Min, c2Max, weightMin, weightMax, changeVelocity1,
                    changeVelocity2, evaluator);*/
            throw new NotImplementedException();
        }
    }

    /*
     * Getters
     */
    public DeploymentProblem getProblem() {
        return problem;
    }

    public int getArchiveSize() {
        return archiveSize;
    }

    public MutationOperator<DeploymentSolution> getMutationOperator() {
        return mutationOperator;
    }

    public List<AbstractBoundedArchive<DeploymentSolution>> getLeaders() {
        return leaders;
    }

    public SolutionListEvaluator<DeploymentSolution> getEvaluator() {
        return evaluator;
    }
}




