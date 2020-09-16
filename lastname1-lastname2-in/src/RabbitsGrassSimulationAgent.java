import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	private int x;
	private int y;
	private int vx;
	private int vy;
	private int life;
	private RabbitsGrassSimulationSpace space;

	public RabbitsGrassSimulationAgent(int birth_life){
		set_speed(); // set random directions
		life = birth_life;

	}
	private void set_speed(){

		vx=0;
		vy=0;

		if(Math.random() < 0.5) {
			if(Math.random() < 0.5) {
				vx = 1;
			}
			else {
				vx = -1;
			}
		}
		else {
			if(Math.random() < 0.5) {
				vy = 1;
			}
			else {
				vy = -1;
			}
		}
	}

	public void draw(SimGraphics arg0) {
		float value = 1;


		Color color = new Color((float) 1.0, value, (float) 0.0);


		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("rabbit.jpg"));
		} catch (IOException e) {
		}


		arg0.drawFastCircle(color);
		
	}
	public int get_life(){
		return life;
	}

	public void decrease_life(int life_taken){
		life -= life_taken;
	}

	public void try_to_eat() {
		if (space.get_grass_at(x, y)){
			space.grass_eaten(x,y);
			life += 10;
		}
	}

	public int getX() {return x; }

	public int getY() {return y; }

	public void set_x(int new_x) { this.x=new_x; }

	public void set_y(int new_y) { this.y=new_y; }

	public void set_space(RabbitsGrassSimulationSpace rabbit_space){
		space = rabbit_space;
	}

	public void step(){

		set_speed();

		int new_x = this.x + vx;
		int new_y = this.y + vy;

		Object2DGrid grid = space.get_rabbit_space();

		int size= grid.getSizeX();

		if(new_x>=size){
			new_x=0;
		}
		else if(new_x<0){
			new_x=size-1;
		}

		if(new_y>=size){
			new_y=0;
		}
		else if(new_y<0){
			new_y=size-1;
		}

		if(!space.get_rabbit_at(new_x, new_y)){
			space.move_rabbit_to(x, y, new_x, new_y);
			set_x(new_x);
			set_y(new_y);
			try_to_eat();
		}


		life-=1;
	}

}
