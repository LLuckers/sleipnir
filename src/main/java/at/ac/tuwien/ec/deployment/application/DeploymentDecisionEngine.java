package at.ac.tuwien.ec.deployment.application;


import at.ac.tuwien.ec.deployment.algorithms.mo.interactive.iMDPSODeployment;
import at.ac.tuwien.ec.deployment.consumer.ChartDeploymentConsumer3D;
import at.ac.tuwien.ec.deployment.problem.dynamic.DynamicDeploymentProblem;
import at.ac.tuwien.ec.deployment.solution.DeploymentSolution;
import at.ac.tuwien.ec.deployment.utils.DeploymentMutationOperator;
import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultIoTPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.RandomEdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.mobile.MobileDevicePlannerWithMobility;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileWorkload;
import at.ac.tuwien.ec.model.software.mobileapps.WorkloadGenerator;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.util.archive.impl.AbstractBoundedArchive;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.point.PointSolution;
import org.uma.jmetalsp.DataConsumer;
import org.uma.jmetalsp.DynamicAlgorithm;
import org.uma.jmetalsp.JMetalSPApplication;
import org.uma.jmetalsp.StreamingDataSource;
import org.uma.jmetalsp.algorithm.indm2.InDM2;
import org.uma.jmetalsp.algorithm.indm2.InDM2Builder;
import org.uma.jmetalsp.consumer.LocalDirectoryOutputConsumer;
import org.uma.jmetalsp.examples.streamingdatasource.ComplexStreamingDataSourceFromKeyboard;
import org.uma.jmetalsp.impl.DefaultRuntime;
import org.uma.jmetalsp.observeddata.AlgorithmObservedData;
import org.uma.jmetalsp.observeddata.ObservedValue;
import org.uma.jmetalsp.observer.impl.DefaultObservable;
import org.uma.jmetalsp.qualityindicator.CoverageFront;
import org.uma.jmetalsp.spark.evaluator.SparkSolutionListEvaluator;
import org.uma.jmetalsp.util.restartstrategy.RestartStrategy;
import org.uma.jmetalsp.util.restartstrategy.impl.CreateNRandomSolutions;
import org.uma.jmetalsp.util.restartstrategy.impl.RemoveNRandomSolutions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.ac.tuwien.ec.sleipnir.SimulationSetup.defaultMobileDeviceCPUModel;

/**
 *
 * Invoking command to run the program in local model with 8 cores:
 * spark-submit --class="at.ac.tuwien.ec.applications.DeploymentDecisionEngine" --master local[8] target\fogtorchpi-extended-1.0-SNAPSHOT.jar
 */
public class DeploymentDecisionEngine {

    public static void main(String[] args) throws IOException, InterruptedException {

        // STEP 1. Create the problem
        MobileWorkload globalWorkload = new MobileWorkload();
        WorkloadGenerator generator = new WorkloadGenerator();
        //for(int j = 0; j< SimulationSetup.mobileNum; j++)
        //    globalWorkload.joinParallel(generator.setupWorkload(SimulationSetup.appNumber, "mobile_"+j));
        globalWorkload = generator.setupWorkload(2, "mobile_0");
        //MobileApplication app = new FacerecognizerApp(0,"mobile_0");
        MobileDataDistributionInfrastructure inf = new MobileDataDistributionInfrastructure();

        /*MobileDevice device = new MobileDevice("mobile_0", SimulationSetup.defaultMobileDeviceHardwareCapabilities.clone(),SimulationSetup.mobileEnergyBudget);
        device.setCPUEnergyModel(SimulationSetup.defaultMobileDeviceCPUModel);
        device.setNetEnergyModel(SimulationSetup.defaultMobileDeviceNETModel);
        Coordinates randomCoordinates = new Coordinates(RandomUtils.nextInt(SimulationSetup.MAP_M),
                RandomUtils.nextInt(SimulationSetup.MAP_N*2));
        device.setCoords(randomCoordinates);
        inf.addMobileDevice(device);*/

        DefaultCloudPlanner.setupCloudNodes(inf, SimulationSetup.cloudNum);
        EdgeAllCellPlanner.setupEdgeNodes(inf);
        DefaultIoTPlanner.setupIoTNodes(inf, SimulationSetup.iotDevicesNum);
        DefaultMobileDevicePlanner.setupMobileDevices(inf,SimulationSetup.mobileNum);
        DefaultNetworkPlanner.setupNetworkConnections(inf);


        /*MobileCloudInfrastructure I = SystemMonitoring.ReadFromFile("infra_01_flex.json");
        MobileApplication A = ApplicationProfiler.ReadFromFile("app_01_flex.json");*/


        DynamicDeploymentProblem<DeploymentSolution> problem = new DynamicDeploymentProblem(inf, globalWorkload);

        // STEP 2. Create and configure the algorithm

        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        //double mutationDistributionIndex = 20.0;
        DeploymentMutationOperator mutation = new DeploymentMutationOperator(mutationProbability);//, mutationDistributionIndex);

        int L1 = 25;
        int L2 = 25;
        int L3 = 25;

        int maxIterations = 2500;
        int swarmSize = 100;

        List<Double> objectiveWeights= Arrays.asList(1.0, 1.0, 1.0);
        List<AbstractBoundedArchive<DeploymentSolution>> archives = new ArrayList<>();

        archives.add(new CrowdingDistanceArchive<>(swarmSize));

       /* List<AbstractBoundedArchive<DeploymentSolution>> archivesWithReferencePoints = new ArrayList<>();
        for (int i = 0; i < referencePoints.size(); i++) {
            archivesWithReferencePoints.add(
                    new CrowdingDistanceArchiveWithReferencePoint<DeploymentSolution>(
                            swarmSize/referencePoints.size(), referencePoints.get(i))) ;
        }*/


        /*SparkConf sparkConf = new SparkConf();
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);*/

        iMDPSODeployment<DeploymentSolution> iMDPSO = new iMDPSODeployment<DeploymentSolution>(
                problem, archives,
                swarmSize, maxIterations,
                mutation,
                new SequentialSolutionListEvaluator<DeploymentSolution>(),
                //new SparkSolutionListEvaluator<>(sparkContext),
                new int[]{L1, L2, L3});
        iMDPSO.setObjectiveWeights(objectiveWeights);

        //InteractiveAlgorithm<DoubleSolution,List<DoubleSolution>> iWasfga = new InteractiveWASFGA<>(problem,100,crossover,mutation,
        //   new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>()), new SequentialSolutionListEvaluator<>(),0.005,referencePoint,weightVectorsFileName );

        InvertedGenerationalDistance<PointSolution> igd =
                new InvertedGenerationalDistance<>();
        CoverageFront<PointSolution> coverageFront = new CoverageFront<>(0.005,igd);
        InDM2<DeploymentSolution> algorithm = new InDM2Builder<>(iMDPSO, new DefaultObservable<>(),coverageFront)
                .setMaxIterations(25000)
                .setPopulationSize(swarmSize)
                .build(problem);

        algorithm.setRestartStrategy(new RestartStrategy<>(
                //new RemoveFirstNSolutions<>(50),
                //new RemoveNSolutionsAccordingToTheHypervolumeContribution<>(50),
                //new RemoveNSolutionsAccordingToTheCrowdingDistance<>(50),
                new RemoveNRandomSolutions(swarmSize),
                new CreateNRandomSolutions<DeploymentSolution>()));

        algorithm.setRestartStrategyForReferencePointChange(new RestartStrategy<>(
                new RemoveNRandomSolutions<>(10),
                new CreateNRandomSolutions<DeploymentSolution>()));

        // STEP 3. Create a streaming data source for the problem and register
        /*StreamingDataSource<ObservedValue<MobileApplication>> applicationStreamingDataSource =
                new SimpleStreamingApplicationProfiler("mobile_23",1*30*1000) ;

        StreamingDataSource<ObservedValue<MobileCloudInfrastructure>> infrastructureStreamingDataSource =
                new SimpleStreamingSystemMonitor(2*60*1000) ;*/

        //streamingDataSource.getObservable().register(problem);

        // STEP 4. Create a streaming data source for the algorithm
        StreamingDataSource<ObservedValue<List<Double>>> keyboardstreamingDataSource =
                new ComplexStreamingDataSourceFromKeyboard() ;

        // STEP 5. Create the data consumers
        DataConsumer<AlgorithmObservedData> localDirectoryOutputConsumer =
                new LocalDirectoryOutputConsumer<DeploymentSolution>("outputdirectory") ;
        DataConsumer<AlgorithmObservedData> chartConsumer =
                new ChartDeploymentConsumer3D<DeploymentSolution>(algorithm.getName(), null);//referencePoint) ;

        // STEP 6. Create the application and run
        JMetalSPApplication<
                DeploymentSolution,
                DynamicDeploymentProblem<DeploymentSolution>,
                DynamicAlgorithm<List<DeploymentSolution>, AlgorithmObservedData>> application;

        application = new JMetalSPApplication<>();

        application.setStreamingRuntime(new DefaultRuntime())
                .setProblem(problem)
                .setAlgorithm(algorithm)
                //.addStreamingDataSource(applicationStreamingDataSource,problem)
                //.addStreamingDataSource(infrastructureStreamingDataSource,problem)
                .addStreamingDataSource(keyboardstreamingDataSource,algorithm)
                .addAlgorithmDataConsumer(localDirectoryOutputConsumer)
                .addAlgorithmDataConsumer(chartConsumer)
                .run();
}

    public static MobileApplication extractWorkload(ArrayList<MobileApplication> workload){
        MobileWorkload mWorkload = new MobileWorkload(workload);
        return mWorkload;
    }
}
