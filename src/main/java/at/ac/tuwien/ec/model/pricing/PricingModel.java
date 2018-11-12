package at.ac.tuwien.ec.model.pricing;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public interface PricingModel {
	
	public double computeCost(SoftwareComponent sc, ComputationalNode cn, MobileCloudInfrastructure i);

}