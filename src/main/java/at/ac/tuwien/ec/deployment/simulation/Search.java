package at.ac.tuwien.ec.deployment.simulation;


import at.ac.tuwien.ec.deployment.Deployment;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.algorithms.OffloadScheduler;

import java.util.*;

public abstract class Search extends OffloadScheduler {

    protected HashMap<String, HashSet<String>> businessPolicies;
    protected HashMap<String, ArrayList<ComputationalNode>> K;
    public ArrayList<Deployment> D;

    public Search(MobileApplication A, MobileCloudInfrastructure I) { //, Coordinates deploymentLocation
        super();

        this.currentApp = A;
        this.currentInfrastructure = I;
        //tmpApp = (MobileApplication) A.clone();
        //K = new HashMap<>();
        //D = new ArrayList<>();
        businessPolicies = new HashMap<String, HashSet<String>>();
        //keepLight = new ArrayList<>();
        //for (SoftwareComponent s: A.S){
        //     K.put(s.getId(), new ArrayList<>());
        //}

        //this.deploymentLocation = deploymentLocation;
    }

    public Search(MobileApplication A, MobileCloudInfrastructure I, HashMap<String, HashSet<String>> businessPolicies) {
        currentApp = A;
        currentInfrastructure = I;
        //tmpApp = (MobileApplication) A.clone();
        K = new HashMap<>(); //(SOFTWARE, COMPATIBLENODES)
        D = new ArrayList<>();
        this.businessPolicies = businessPolicies;
        //keepLight = new ArrayList<>();
        for (SoftwareComponent s: A.componentList.values()){
            K.put(s.getId(), new ArrayList<>());
        }
    }


    protected synchronized void deploy(Deployment deployment, MobileSoftwareComponent s, ComputationalNode n) {
        super.deploy(deployment,s,n);
        //TODO deployLinks(deployment, s, n);
    }

    protected void undeploy(Deployment deployment, MobileSoftwareComponent s, ComputationalNode n) {
        super.undeploy(deployment,s,n);
        if (deployment.containsKey(s)) {
            //TODO undeployLinks(deployment, s, n);
        }
    }

    public Deployment search(Deployment deployment) {
        if (isComplete(deployment)) {
            D.add((Deployment) deployment.clone());
            return deployment;
        }
        MobileSoftwareComponent s = selectUndeployedComponent(deployment);
        if(!s.isOffloadable())
            deploy(deployment,s,currentInfrastructure.getMobileDevice());
        search(deployment);
        if (K.get(s.getId()) != null) {
            for (ComputationalNode n : K.get(s.getId())) { // for all nodes compatible with s
                //System.out.println(steps + " Checking " + s.getId() + " onto node " + n.getId());
                if (isValid(deployment, s, n)) {
                    //System.out.println("Deploying " + s.getId() + " onto node " + n.getId());
                    deploy(deployment, s, n);
                    Deployment result = search(deployment);
                    if (result != null) {
                        return deployment;
                    }
                    else
                        undeploy(deployment, s, n);
                }
                //System.out.println("Undeploying " + s.getId() + " from node " + n.getId());
            }
        }
        return deployment;
    }

    protected MobileSoftwareComponent selectUndeployedComponent(Deployment deployment) {
        //System.out.println(deployment.size());
        return (MobileSoftwareComponent)currentApp.componentList.values().toArray()[deployment.size()-1];
    }

    protected boolean isComplete(Deployment deployment) {
        if(deployment == null)
            return false;
        for(SoftwareComponent s : currentApp.componentList.values())
            if(!deployment.containsKey(s))
                return false;
        return true;
    }

    public void resetSearch(Deployment deployment) {
        if(deployment!=null)
        {
            Object[] softwareList = deployment.keySet().toArray();
            for(Object s: softwareList)
            {
                MobileSoftwareComponent a = (MobileSoftwareComponent) s;
                ComputationalNode n = (ComputationalNode)deployment.get(a);
                undeploy(deployment,a,n);
            }
        }
    }

    public abstract ArrayList<Deployment> findDeployments(Deployment d);

    /*TODO protected ArrayList<MobileSoftwareComponent> selectElegibleNodes(){
        ArrayList<MobileSoftwareComponent> elegibleNodes = new ArrayList<MobileSoftwareComponent>();
        for (MobileSoftwareComponent msc :currentApp.componentList.values()) {
            if(currentApp.incidentEdgesIn(msc).size() == 0)
                elegibleNodes.add((MobileSoftwareComponent) currentApp..S.remove(i));
        }
        return elegibleNodes;
    }*/
}
