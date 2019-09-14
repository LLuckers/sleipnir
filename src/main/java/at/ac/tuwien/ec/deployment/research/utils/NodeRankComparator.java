package at.ac.tuwien.ec.deployment.research.utils;



import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;

import java.util.Comparator;

public class NodeRankComparator implements Comparator<MobileSoftwareComponent>
{

	private MobileApplication A;
	private MobileCloudInfrastructure I;
	
	public NodeRankComparator(MobileApplication A, MobileCloudInfrastructure I)
	{
		this.A = A;
		this.I = I;
	}
	@Override
	public int compare(MobileSoftwareComponent o1, MobileSoftwareComponent o2) {
		if( o1.getRank() > o2.getRank() )
			return -1;
		else if( o1.getRank() < o2.getRank() )
			return 1;
		else
			return 0;
	}
		
}
