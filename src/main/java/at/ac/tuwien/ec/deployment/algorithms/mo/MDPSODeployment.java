package at.ac.tuwien.ec.deployment.algorithms.mo;

import at.ac.tuwien.ec.deployment.problem.DeploymentProblem;
import at.ac.tuwien.ec.deployment.solution.DeploymentSolution;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import org.uma.jmetal.algorithm.impl.AbstractParticleSwarmOptimization;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.archive.impl.AbstractBoundedArchive;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.*;
import java.util.stream.Collectors;

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
public class MDPSODeployment<S extends DeploymentSolution> extends AbstractParticleSwarmOptimization<S, List<S>> {
    private DeploymentProblem<S> problem;

    protected int swarmSize;
    private int maxIterations;
    private int iterations;

    //private GenericSolutionAttribute<S, S> localBest;
    Vector<ComputationalNode>[] speed;

    private JMetalRandom randomGenerator = JMetalRandom.getInstance();

    private Comparator<S> dominanceComparator;

    private MutationOperator<S> mutation;

    protected SolutionListEvaluator<S> evaluator;

    private int[] L;

    //private BoundedArchive<S> leaders;
    public List<AbstractBoundedArchive<S>> leaders;

    protected List<Double> objectiveWeights;

    /**
     * Constructor of the MDPSO Algorithm in the specific area of DAG Computations in Mobile Cloud Edge Offloading
     *
     * @param problem instance of the DeploymentProblem
     * @param leaders
     * @param swarmSize the size of the swarm, which will be initially created
     * @param mutationOperator MutationOperator to execute the perturbation (or turbulence) within PSO
     * @param maxIterations for the stopping criteria
     * @param evaluator Instance of a SolutionListEvaluator to evaluate the objectives within the particle
     * @param L
     */
    public MDPSODeployment(DeploymentProblem<S> problem, List<AbstractBoundedArchive<S>> leaders,
                           int swarmSize, int maxIterations,
                           MutationOperator<S> mutationOperator,
                           SolutionListEvaluator<S> evaluator,
                           int[] L) {
        this.problem = problem;
        this.swarmSize = swarmSize;
        this.mutation = mutationOperator;
        this.maxIterations = maxIterations;

        //randomGenerator = JMetalRandom.getInstance();
        this.evaluator = evaluator;

        dominanceComparator = new DominanceComparator<S>();
        //localBest = new GenericSolutionAttribute<S, S>();
        speed = new Vector[swarmSize];

        this.L = L;

        this.leaders = leaders;


        this.objectiveWeights = Arrays.asList(0.3334,0.3333,0.3333);
    }

    /**
     * Overrides the extended algorithm with the specific steps of the MDPSO
     * NOT IN USE
     */
    /*@Override
    public void run() {

        this.setSwarm(this.createInitialSwarm());
        this.setSwarm(this.evaluateSwarm(this.getSwarm()));
        this.initializeVelocity(this.getSwarm());

        //Currently not in use since the MDPSO doesn't use a local best
        //this.initializeParticlesMemory(this.getSwarm());

        this.initializeLeader(this.getSwarm());
        this.initProgress();

        while(!this.isStoppingConditionReached()) {

            //System.out.println("MDPSO: Start iteration: "+iterations +" ...");
            this.setSwarm(this.evaluateSwarm(this.getSwarm()));
            this.updateLeaders(this.getSwarm());
            this.updateVelocity(this.getSwarm());
            this.updatePosition(this.getSwarm());
            this.perturbation(this.getSwarm());

            //Currently not in use since the MDPSO doesn't use a local best
            //this.updateParticlesMemory(this.getSwarm());

            //===Do we need this?
            //this.knockOutParticles(this.getSwarm());

            this.updateProgress();
        }
        System.out.println("MDPSO: Done!");
    }*/

    protected void updateLeadersDensityEstimator() {
        //leaders.computeDensityEstimator();
        Iterator var1 = this.leaders.iterator();

        while(var1.hasNext()) {
            BoundedArchive<S> leader = (BoundedArchive)var1.next();
            leader.computeDensityEstimator();
        }
    }

    @Override
    protected void initProgress() {
        iterations = 1;
        updateLeadersDensityEstimator();
    }

    @Override
    protected void updateProgress() {
        iterations += 1;
        updateLeadersDensityEstimator();
    }

    @Override
    protected boolean isStoppingConditionReached() {
        if(iterations >= maxIterations){
            System.out.println("MDPSO: Done!");
            return true;
        }
        return false;
    }

    @Override
    protected List<S> createInitialSwarm() {
        List<S> swarm = new ArrayList<>(swarmSize);

        S newSolution;
        for (int i = 0; i < swarmSize; i++) {
            newSolution = (S)problem.createSolution();
            swarm.add(newSolution);
        }

        return swarm;
    }

    /**
     * Evaluates the swarm using the given problem and the SolutionListEvaluator and the
     * @param swarm the current swarm (List of Ss)
     * @return the evaluated swarm
     */
    @Override
    protected List<S> evaluateSwarm(List<S> swarm) {
        swarm = evaluator.evaluate(swarm, problem);

        return swarm;
    }

    @Override
    protected void initializeLeader(List<S> swarm) {
        Iterator var2 = swarm.iterator();

        while(var2.hasNext()) {
            S particle = (S)var2.next();
            Iterator var4 = this.leaders.iterator();

            while(var4.hasNext()) {
                BoundedArchive<S> leader = (BoundedArchive)var4.next();
                leader.add((S)particle.copy());
            }
        }
    }

    /**
     * Initialize the velocities for each particle.
     * Since the velocity within the discrete MDPSO will be calculated during every iteration, this method just initializes the Vector for each particle
     * @param swarm the current swarm (List of Ss)
     */
    @Override
    protected void initializeVelocity(List<S> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            speed[i] = new Vector<>(problem.getNumberOfVariables());
        }
    }

    /**
     * Currently not in use since the MDPSO doesn't use a local best
     * @param swarm
     */
    @Override
    protected void initializeParticlesMemory(List<S> swarm) {
        /*for (S particle : swarm) {
            localBest.setAttribute(particle, (S) particle.copy());
        }*/
    }

    /**
     * Calculates the new velocity for each particle in the swarm in the current iteration and set it
     * @param swarm the current swarm (List of Ss)
     */
    @Override
    protected void updateVelocity(List<S> swarm) {
        S gbest1;
        S gbest2;
        S gbest3;

        int N = problem.getNumberOfVariables();

        for (int i = 0; i < swarm.size(); i++) {
            S particle = (S) swarm.get(i).copy();


            gbest1 = selectGlobalBest(0,L[0]);
            gbest2 = selectGlobalBest(1,L[1]);
            gbest3 = selectGlobalBest(2,L[2]);

            //=================
            // initialize vecotrs to randomly reserve information for each objective
            Vector<Boolean> c1 = new Vector<>(N);
            Vector<Boolean> c2 = new Vector<>(N);
            Vector<Boolean> c3 = new Vector<>(N);
            for(int j = 0; j < N; j++) {
                c1.add(randomGenerator.nextDouble() < 0.5D);
                c2.add(randomGenerator.nextDouble()< 0.5D);
                c3.add(randomGenerator.nextDouble()< 0.5D);
            }

            //=================
            // calculate the velocity for the particle i




            Vector<ComputationalNode> reservedVector1 = RandomlyReserveInformationOfVector(
                    c1, findDifferenceBetweenParticle(this.getTaskVector(gbest1), this.getTaskVector(particle),N), N);
            Vector<ComputationalNode> reservedVector2 = RandomlyReserveInformationOfVector(
                    c2, findDifferenceBetweenParticle(this.getTaskVector(gbest2), this.getTaskVector(particle),N),N);
            Vector<ComputationalNode> reservedVector3 = RandomlyReserveInformationOfVector(
                    c3, findDifferenceBetweenParticle(this.getTaskVector(gbest3), this.getTaskVector(particle),N),N);

            speed[i] = IntegrateInformationOfTwoParticles(
                    reservedVector1,
                    (IntegrateInformationOfTwoParticles(
                            reservedVector2,
                            reservedVector3,
                            (objectiveWeights.get(1)/(objectiveWeights.get(1)+objectiveWeights.get(2))),
                            N, randomGenerator)),
                    (objectiveWeights.get(0)/(objectiveWeights.get(0)+objectiveWeights.get(1)+objectiveWeights.get(2))),
                    N,randomGenerator);
        }
    }

    /**
     * By operating the velocity, a new particle will be created for each particle in the swarm.
     * This new particle will be evaluated and compared to the current one.
     * The current particle in the swarm will be replaced, if every objective of the new particle is better.
     *
     * @param swarm the current swarm (List of Ss)
     */
    @Override
    protected void updatePosition(List<S> swarm) {
        for (int i = 0; i < swarmSize; i++) {
            //Create new particle x by operate the velocity
            S x = this.operateVelocity(swarm.get(i),speed[i],randomGenerator);

            swarm.set(i, x);
        }
    }

    /**
     * Operates the configured mutation operation "perturbation" (or sometime called turbulence) on some random particles of the swarm
     * @param swarm the current swarm (List of Ss)
     */
    @Override
    protected void perturbation(List<S> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            if ((i % 6) == 0) {
                mutation.execute(swarm.get(i));
            }
        }
    }

    /**
     *
     *
     * @param swarm List of Ss which should be added to the archive
     */
    @Override
    protected void updateLeaders(List<S> swarm) {

        /*List<S> sortedSwarm = new ArrayList<>(swarm);

        for(int i = 0; i<3; i++) {
            archives[i] = new ArrayList<>();
            Collections.sort(sortedSwarm, new ObjectiveComparator(i));

            if(!S.shouldObjectiveBeMinimized(i))
                Collections.reverse(sortedSwarm);

            int archiveUpperBound = Math.min(swarmSize, L[i]);
            for (int j = 0; j < archiveUpperBound; j++) {
                // Check the solution for its constraint violations and add only valid solutions to the archive
                //if(problem.numberOfViolatedConstraints.getAttribute(particle) == 0)
                    archives[i].add((S)sortedSwarm.get(j).copyWithObjectiveValue());
            }
        }*/

        Iterator var2 = swarm.iterator();

        while(var2.hasNext()) {
            S particle = (S)var2.next();
            Iterator var4 = this.leaders.iterator();

            while(var4.hasNext()) {
                BoundedArchive<S> leader = (BoundedArchive)var4.next();
                leader.add((S)particle.copy());
            }
        }
    }

    /**
     * Currently not in use since the MDPSO doesn't use a local best
     * @param swarm
     */
    @Override
    protected void updateParticlesMemory(List<S> swarm) {
        /*for (int i = 0; i < swarm.size(); i++) {
            int flag = dominanceComparator.compare(swarm.get(i), localBest.getAttribute(swarm.get(i)));
            if (flag != 1) {
                S particle = (S) swarm.get(i).copy();
                localBest.setAttribute(swarm.get(i), particle);
            }
        }*/
    }

    @Override
    public List<S> getResult() {
        //return this.leaders.getSolutionList(); //this.getSwarm();

        List<S> resultList = new ArrayList();
        Iterator var2 = this.leaders.iterator();

        while(var2.hasNext()) {
            BoundedArchive<S> leader = (BoundedArchive)var2.next();
            Iterator var4 = leader.getSolutionList().iterator();

            while(var4.hasNext()) {
                S solution = (S)var4.next();
                resultList.add(solution);
            }
        }

        return resultList;
    }

    /**
     * Selects a solution from the sorted archive of the objective given by the objectiveNumber.
     * This solution will be randomly selected between the best (index=0) and the Lth solution within the archive
     *
     * @param objectiveNumber started by 0 to numberOfObjectives-1
     * @param L upper bound for the top solutions in the archive selected randomly
     * @return a solution from the sorted archive[objectiveNumber] randomly between the top and the Lth solution
     */
    protected S selectGlobalBest(int objectiveNumber, int L) {

        /*int selectedSwarmIndex = this.randomGenerator.nextInt(0, this.leaders.size() - 1);
        BoundedArchive<S> selectedSwarm = (BoundedArchive)this.leaders.get(selectedSwarmIndex);
        int pos1 = this.randomGenerator.nextInt(0, selectedSwarm.getSolutionList().size() - 1);
        int pos2 = this.randomGenerator.nextInt(0, selectedSwarm.getSolutionList().size() - 1);
        S one = (S)selectedSwarm.getSolutionList().get(pos1);
        S two = (S)selectedSwarm.getSolutionList().get(pos2);
        S bestGlobal;
        if (selectedSwarm.getComparator().compare(one, two) < 1) {
            bestGlobal = (S)one.copy();
        } else {
            bestGlobal = (S)two.copy();
        }*/

        if(this.leaders.size() < 1)
            return null;

        int selectedSwarmIndex = this.randomGenerator.nextInt(0, this.leaders.size() - 1);
        List<S> leaderSolutionList = this.leaders.get(selectedSwarmIndex).getSolutionList();
        if(leaderSolutionList.size() < 1)
            return null;

        Collections.sort(leaderSolutionList, new ObjectiveComparator(objectiveNumber));

        int randomUpperBound = Math.min(L,leaderSolutionList.size()-1);
        int k = randomGenerator.nextInt(0, randomUpperBound);

        return leaderSolutionList.get(k);
    }

    /**
     *
     * @param swarm
     */
    protected void knockOutParticles(List<S> swarm){
        for (int i = swarm.size()-1; i >= 0; i--) {
            //TODO Check whether this method is necessary
        }
    }


    @Override
    public String getName() {
        return "MDPSO";
    }

    @Override
    public String getDescription() {
        return "Multiobjective Discrete PSO for the deployment problem";
    }

    /* Getters */
    public int getSwarmSize() {
        return swarmSize;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getIterations() {
        return iterations;
    }

    /* Setters */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public DeploymentProblem getProblem(){
        return problem;
    }

    /**
     * We use this operator to find the difference between a particle x and gbest and then retain the unique information of gbest.
     *
     * @param gbest Vector of the global best DeploymentSolution (leader) / P[p1, p2,..., pN]
     * @param x Vector of the particle x / Q[q1, q2,..., qN]
     * @param N Number of tasks (vector lengeth)
     * @return Vector of unique ComputationalNode from the gbest, if gbest[i] != x[i], otherwise null at ith position
     */
    private Vector<ComputationalNode> findDifferenceBetweenParticle(Vector<ComputationalNode> gbest, Vector<ComputationalNode> x, int N){
        if(gbest == null)
            return x;

        Vector<ComputationalNode> result = new Vector<>(N);

        for(int i = 0; i < N; i++) {
            if(gbest.get(i).getId() == x.get(i).getId())
                result.add(i,null);
            else
                result.add(i,gbest.get(i));
        }

        return result;
    }

    /**
     * This operator is used to randomly reserve part of information of gbest
     *
     * @param c Vector of size N with random boolean values
     * @param vector Vector to reserve information on
     * @param N Number of tasks (vector lengeth)
     * @return Vector of ComputationalNode with the value at i of vector[i] if c[i] is true, otherwise null
     */
    private Vector<ComputationalNode> RandomlyReserveInformationOfVector(Vector<Boolean> c, Vector<ComputationalNode> vector, int N){
        Vector<ComputationalNode> result = new Vector<>(N);

        for(int i = 0; i < N; i++) {
            if(c.get(i))
                result.add(i,vector.get(i));
            else
                result.add(i,null);
        }

        return result;
    }

    public List<Double> getObjectiveWeights() {
        return objectiveWeights;
    }

    public synchronized void setObjectiveWeights(List<Double> objectiveWeights) {
        this.objectiveWeights = objectiveWeights;
    }

    /**
     *  This operator is designed to integrate the information of two particles.
     *
     * @param P First vector of Ts
     * @param Q Second vector of Ts
     * @param N Number of tasks (vector lengeth)
     * @param randomThreshold threshold to take value from vector P (value between [0.0,1.0])
     * @param randomGenerator for the random factor
     * @return Vector with result[i] = if one of P[i] or Q[i] is null, the non-null one, otherwise randomly pick the value of P[i] or Q[i]
     */
    private Vector<ComputationalNode> IntegrateInformationOfTwoParticles(Vector<ComputationalNode> P, Vector<ComputationalNode> Q, double randomThreshold, int N, JMetalRandom randomGenerator) {
        Vector<ComputationalNode> result = new Vector<>(N);

        for(int i = 0; i < N; i++) {
            if(P.get(i) == null && Q.get(i) == null) {
                result.add(i, null);
                continue;
            }

            //================
            // if one of pi or qi is 0, we pick up the nonzero one as the result,
            if(P.get(i) == null) {
                result.add(i, Q.get(i));
                continue;
            }
            if(Q.get(i) == null) {
                result.add(i, P.get(i));
                continue;
            }
            //================

            //================
            // if pi is exactly equal to qi, the corresponding bit of the result is equal to pi and qi,
            // otherwise, we randomly pick the value of pi or qi as the ith bit of result.

            if(randomGenerator.nextDouble() < randomThreshold)
                result.add(i, P.get(i));
            else
                result.add(i, Q.get(i));
            //================
        }

        return result;
    }

    /**
     * Operates the given vector of ComputationalNodes (velocity) on the current deployment
     * @param v vector of ComputationalNodes (velocity)
     */
    private S operateVelocity(S solution, Vector<ComputationalNode> v, JMetalRandom randomGenerator) {

        Vector<ComputationalNode> xt1Vector = IntegrateInformationOfTwoParticles(this.getTaskVector(solution),v,0.5D,solution.getNumberOfVariables(),randomGenerator);

        S xt1 = (S)solution.copy();
        for(int i = 0; i < solution.getNumberOfVariables(); i++) {
            if(xt1Vector.get(i) != null)
                xt1.setVariableValue(i,xt1Vector.get(i));
        }
        return xt1;
    }

    private Vector<ComputationalNode> getTaskVector(S solution){
        if(solution == null)
            return null;
        return new Vector<ComputationalNode>(solution.getDeployment().values().stream().map(n -> (ComputationalNode)n).collect(Collectors.toList()));
    }
}
