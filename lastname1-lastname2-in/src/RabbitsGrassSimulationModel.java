import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;


import java.awt.*;
import java.util.ArrayList;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {

	private static final int GRID_SIZE=100;
	private static final int NUM_INIT_RABBITS = 10;
	private static final int NUM_INIT_GRASS=1000;
	private static final int GRASS_GROWTH_RATE = 15;
	private static final int BIRTH_THRESHOLD = 300;

	private int gridSize = GRID_SIZE;
	private int numInitRabbits = NUM_INIT_RABBITS;
	private int numInitGrass = NUM_INIT_GRASS;
	private int grassGrowthRate = GRASS_GROWTH_RATE;
	private int birthThreshold = BIRTH_THRESHOLD;



	private Schedule schedule;

	private RabbitsGrassSimulationSpace space;
	private ArrayList<RabbitsGrassSimulationAgent> rabbit_list;

	private DisplaySurface displaySurf;
	private OpenSequenceGraph amountOfGrassInSpace;
	private OpenHistogram agentWealthDistribution;

	class GrassInSpace implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double) space.get_total_grass();
		}
	}

	class AgentsInSpace implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double) rabbit_list.size();
		}
	}

	class GrassGrowRate implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double) grassGrowthRate;
		}
	}


	public static void main(String[] args) {

		System.out.println("Rabbit skeleton");

		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		// Do "not" modify the following lines of parsing arguments
		if (args.length == 0) // by default, you don't use parameter file nor batch mode
			init.loadModel(model, "", false);
		else
			init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));

	}

	public void begin(){
		buildModel();
		buildSchedule();
		buildDisplay();

		displaySurf.display();
		amountOfGrassInSpace.display();
	}

	public void buildModel(){

		space = new RabbitsGrassSimulationSpace(gridSize);
		space.add_new_grass(numInitGrass);

			for(int i = 0; i < numInitRabbits; i++){
			if(rabbit_list.size() < gridSize * gridSize) {
				add_rabbit();
			}
		}

	}

	public void buildSchedule(){
		System.out.println("Running BuildSchedule");

		class CarryDropStep extends BasicAction {
			public void execute() {
				space.add_new_grass(grassGrowthRate);

				SimUtilities.shuffle(rabbit_list);
				for(int i = 0; i < rabbit_list.size(); i++){
					RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent) rabbit_list.get(i);
					rabbit.step();
				}

				give_birth_or_die();
				displaySurf.updateDisplay();       }
		}

		schedule.scheduleActionBeginning(0, new CarryDropStep());

		class CarryDropCountLiving extends BasicAction {
			public void execute(){
				count_rabbits();
			}
		}

		schedule.scheduleActionAtInterval(10, new CarryDropCountLiving());


		class CarryDropUpdateGrassInSpace extends BasicAction {
			public void execute(){
				amountOfGrassInSpace.step();
			}
		}

		schedule.scheduleActionAtInterval(1, new CarryDropUpdateGrassInSpace());


		class CarryDropUpdateAgentWealth extends BasicAction {
			public void execute(){
				if (!rabbit_list.isEmpty()) {
					agentWealthDistribution.step();
				}
			}
		}
	}

	public void buildDisplay(){

		ColorMap map = new ColorMap();

		for(int i = 1; i<5; i++){
			map.mapColor(i, new Color((int) 150-50*(i-1), 255, 50));
		}
		for(int i = 5; i<9; i++){
			map.mapColor(i, new Color(0, (int) 255 - (i-4)*50, 0));
		}
		map.mapColor(0, new Color(222,184,135));

		Value2DDisplay displayGrass =
				new Value2DDisplay(space.get_grass_space(), map);

		Object2DDisplay displayAgents = new Object2DDisplay(space.get_rabbit_space());
		displayAgents.setObjectList(rabbit_list);

		displaySurf.addDisplayableProbeable(displayGrass, "Grass");
		displaySurf.addDisplayableProbeable(displayAgents, "Agents");

		amountOfGrassInSpace.addSequence("# squares filed with grass ", new GrassInSpace());
		amountOfGrassInSpace.addSequence("# of rabbits in space", new AgentsInSpace());
		amountOfGrassInSpace.addSequence("Grass grow rate", new GrassGrowRate());

	}

	public String[] getInitParam() {
		// TODO Auto-generated method stub
		// Parameters to be set by users via the Repast UI slider bar
		// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want
		String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold"};
		return params;
	}

	private void add_rabbit(){
		int n_x = (int) (Math.random() * (gridSize));
		int n_y = (int) (Math.random() * (gridSize));
		if(!space.get_rabbit_at(n_x, n_y)) {
			RabbitsGrassSimulationAgent new_rabbit = new RabbitsGrassSimulationAgent(50);
			space.add_new_rabbit(new_rabbit, n_x, n_y);
			rabbit_list.add(new_rabbit);

		}
	}

	public String getName() {
		return "Carry And Drop";
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setup() {
		space = null;
		rabbit_list = new ArrayList<RabbitsGrassSimulationAgent>();
		schedule = new Schedule(1);


		if (displaySurf != null){
			displaySurf.dispose();
		}
		displaySurf = null;

		if (amountOfGrassInSpace != null){
			amountOfGrassInSpace.dispose();
		}
		amountOfGrassInSpace = null;

		if (agentWealthDistribution != null){
			agentWealthDistribution.dispose();
		}
		agentWealthDistribution = null;

		RangePropertyDescriptor b = new RangePropertyDescriptor("GridSize", 0, 200, 100);
		descriptors.put("GridSize", b);
		RangePropertyDescriptor a = new RangePropertyDescriptor("NumInitRabbits", 0, 1000, 200);
		descriptors.put("NumInitRabbits", a);
		RangePropertyDescriptor d = new RangePropertyDescriptor("NumInitGrass", 0, 40000, 20000);
		descriptors.put("NumInitGrass", d);
		RangePropertyDescriptor h = new RangePropertyDescriptor("GrassGrowthRate", 0, 1000, 500);
		descriptors.put("GrassGrowthRate", h);
		RangePropertyDescriptor e = new RangePropertyDescriptor("BirthThreshold", 0, 100, 20);
		descriptors.put("BirthThreshold", e);



		displaySurf = new DisplaySurface(this, "Carry Drop Model Window 1");


		amountOfGrassInSpace = new OpenSequenceGraph("Amount Of Grass In Space",this);

		registerDisplaySurface("Carry Drop Model Window 1", displaySurf);
		this.registerMediaProducer("Plot", amountOfGrassInSpace);

	}

	private int count_rabbits(){
		int rabbit_nbr = 0;
		for(int i = 0; i < rabbit_list.size(); i++){
			RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent) rabbit_list.get(i);
			if(rabbit.get_life() > 0) rabbit_nbr++;
		}
		System.out.println("there are: " + rabbit_nbr+ " rabbits");

		return rabbit_nbr;
	}

	private void give_birth_or_die(){
		for(int i = (rabbit_list.size() - 1); i >= 0 ; i--){
			RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent) rabbit_list.get(i);
			if(rabbit.get_life() > birthThreshold){
				add_rabbit();
			}
		}
		for(int i = (rabbit_list.size() - 1); i >= 0 ; i--){
			RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent) rabbit_list.get(i);
			if(rabbit.get_life() < 0){
				space.remove_rabbit_at(rabbit.getX(), rabbit.getY());
				rabbit_list.remove(i);
			}
		}
	}

	public int getBirthThreshold(){
		return birthThreshold;
	}

	public void setBirthThreshold(int birth_thres){
		birthThreshold = birth_thres;
	}

	public int getNumInitRabbits(){
		return numInitRabbits;
	}

	public void setNumInitRabbits(int rabbit_num){ numInitRabbits = rabbit_num; }

	public int getGridSize(){
		return gridSize;
	}

	public void setGridSize(int size){
		gridSize = size;
	}

	public int getGrassGrowthRate() {
		return grassGrowthRate;
	}

	public void setGrassGrowthRate(int g_rate) {
		grassGrowthRate = g_rate;
	}

	public int getNumInitGrass() {
		return numInitGrass;
	}

	public void setNumInitGrass(int grass_init) {
		numInitGrass = grass_init;
	}





}