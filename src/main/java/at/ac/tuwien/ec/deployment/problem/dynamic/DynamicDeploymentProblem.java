package at.ac.tuwien.ec.deployment.problem.dynamic;


import at.ac.tuwien.ec.deployment.problem.DeploymentProblem;
import at.ac.tuwien.ec.deployment.solution.DeploymentSolution;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.software.MobileApplication;
import org.uma.jmetalsp.DynamicProblem;
import org.uma.jmetalsp.observeddata.ObservedValue;
import org.uma.jmetalsp.observer.Observable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class DynamicDeploymentProblem<S extends DeploymentSolution>
        extends DeploymentProblem<S>
        implements DynamicProblem<S, ObservedValue<Object>> {

    private boolean theProblemHasBeenModified;
    private final String algorithName = "DynamicDeploymentProblem";
    private Date lastProblemChange;
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
    private MobileApplication newA;
    private MobileCloudInfrastructure newI;

    public DynamicDeploymentProblem(MobileCloudInfrastructure I, MobileApplication A) {
        super(I, A);
        lastProblemChange = new Date();
        theProblemHasBeenModified = true;
    }

    public synchronized boolean hasTheProblemBeenModified() {

        if(theProblemHasBeenModified){
            if(newA != null){
                this.currentApp = newA;
                //this.businessPolicies = new HashMap<String, HashSet<String>>();
                newA = null;
            }
            if(newI != null){
                this.currentInfrastructure = newI;
                newI = null;
            }
        }

        return theProblemHasBeenModified;
    }

    @Override
    public synchronized void reset() {
        theProblemHasBeenModified = false ;
    }


    @Override
    public void update(Observable<ObservedValue<Object>> observable, ObservedValue<Object> data) {
        if(data.getValue() instanceof MobileApplication){
            updateApplication((MobileApplication) data.getValue());
        }
        if(data.getValue() instanceof MobileCloudInfrastructure){
            updateInfrastructure((MobileCloudInfrastructure) data.getValue());
        }
    }

    public synchronized void updateApplication(MobileApplication A) {
        try {
            this.newA = A;
            lastProblemChange = new Date();
            theProblemHasBeenModified = true;
        }catch(Exception ex){

        }
        //JMetalLogger.logger.info("Updated cost: " + row + ", " + col + ": " + newValue) ;
    }

    public synchronized void updateInfrastructure(MobileCloudInfrastructure I) {
        try {
            this.newI = I;
            lastProblemChange = new Date();
            theProblemHasBeenModified = true;
        }catch(Exception ex){

        }
        //JMetalLogger.logger.info("Updated cost: " + row + ", " + col + ": " + newValue) ;
    }

    @Override
    public String getName(){
        return "S="+currentApp.componentList.size()+";("+sdfDate.format(lastProblemChange)+")";
    }
}
