//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package at.ac.tuwien.ec.deployment.consumer;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.point.PointSolution;

import java.io.IOException;
import java.util.List;

/**
 * Plots a chart with the produce fronts
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class DeploymentSimulationChart3D<S extends Solution<?>>{

  private MyChartContainer3D chart;
  private String simulationName;
  private String objective1Title,objective2Title,objective3Title;
  private Integer numberOfExecutions;

  public DeploymentSimulationChart3D(String simulationName,String objective1Title,String objective2Title,String objective3Title,
                                     Integer numberOfExecutions) {
    this.chart = null;
    this.simulationName = simulationName;
    this.objective1Title = objective1Title;
    this.objective2Title = objective2Title;
    this.objective3Title = objective3Title;
    this.numberOfExecutions = numberOfExecutions;
  }

  public void drawAlgorithmResult(String nameAlgorithm, List<PointSolution> solutionList) {

    double coverageValue = 0;
    if (chart == null) {
      this.chart = new MyChartContainer3D(this.simulationName, 200);
      chart.addFrontChart(0, 1, objective1Title,objective2Title);
      chart.addFrontChart(0, 2,objective1Title,objective3Title);
      chart.addFrontChart(1, 2,objective2Title,objective3Title);
      //chart.setVarChart(0, 1);
      // ??? this.chart.getFrontChart().getStyler().setLegendPosition(Styler.LegendPosition.InsideNE) ;

      this.chart.initChart();
    }

    if (solutionList.size() != 0) {
      List<XYChart> charts = this.chart.getCharts();
      for (XYChart xychart : charts) {
        xychart.setTitle("Iterations: " + numberOfExecutions);
      }

        //if (lastReceivedFront == null) {
         // lastReceivedFront = solutionList;
          this.chart.updateFrontCharts(solutionList, numberOfExecutions, nameAlgorithm);
         // this.chart.refreshCharts();
        //} else {
          //Front referenceFront = new ArrayFront(lastReceivedFront);

          //InvertedGenerationalDistance igd =
           //       new InvertedGenerationalDistance<S>(referenceFront);

          //coverageValue = igd.evaluate(solutionList);
        //}

        //if (coverageValue > 0.005) {
          //this.chart.updateFrontCharts(solutionList, numberOfIterations);
          //lastReceivedFront = solutionList;
          try {
            this.chart.saveChart(simulationName + ".chart", BitmapEncoder.BitmapFormat.PNG);
          } catch (IOException e) {
            e.printStackTrace();
          }
        //}


        this.chart.refreshCharts();
      //}
    }
  }
}

