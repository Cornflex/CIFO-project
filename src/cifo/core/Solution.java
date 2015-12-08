package cifo.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Random;


public class Solution implements Comparable<Solution> {

	public static final int VALUES_PER_TRIANGLE = 10;
	public static final int GRID_Width = 50;

	protected ProblemInstance instance;
	protected int[] values;
	protected double fitness;
	protected Random r;
	
	public enum MutationOperator {oneValue, orderFlip, locationFlip, oneValueOccasionalFlipLocation, addorSubtractValues, deltaBased, manyValueChange, manyValueAddSubtract};
	protected MutationOperator[] mutationOperators;

	public Solution(ProblemInstance instance) {
		this.instance = instance;
		r = new Random();
		mutationOperators = Main.MUTATION_OPERATORS;
		initialize();
		
	}

	public void initialize() {
		values = new int[instance.getNumberOfTriangles() * VALUES_PER_TRIANGLE];

		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			// initialize HSB and Alpha
			for (int i = 0; i < 4; i++) {
				values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(256);
			}
			// initialize vertices
			for (int i = 4; i <= 8; i += 2) {
				values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(instance.getImageWidth() + 1);
				values[triangleIndex * VALUES_PER_TRIANGLE + i + 1] = r.nextInt(instance.getImageHeight() + 1);
			}
		}
	}

	public void evaluate() {
		BufferedImage generatedImage = createImage();
		int[] generatedPixels = new int[generatedImage.getWidth() * generatedImage.getHeight()];
		PixelGrabber pg = new PixelGrabber(generatedImage, 0, 0, generatedImage.getWidth(), generatedImage.getHeight(),
				generatedPixels, 0, generatedImage.getWidth());
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		int[] targetPixels = instance.getTargetPixels();
		long sum = 0;
		for (int i = 0; i < generatedPixels.length && i < targetPixels.length; i++) {
			int c1 = targetPixels[i];
			int c2 = generatedPixels[i];
			int red = ((c1 >> 16) & 0xff) - ((c2 >> 16) & 0xff);
			int green = ((c1 >> 8) & 0xff) - ((c2 >> 8) & 0xff);
			int blue = (c1 & 0xff) - (c2 & 0xff);
			sum += red * red + green * green + blue * blue;
		}

		fitness = Math.sqrt((double) sum);
	}
	
	public double[] evaluateGrid() {
		BufferedImage generatedImage = createImage();
		int[] generatedPixels = new int[2500];
		double[] gridFitness =  new double[16];
		long sum = 0;
		int gridNum = 0;
		for (int x=0;x<=150;x+=50){
			for (int y=0;y<=150;y+=50){
				//update to start at x and y values
				
				PixelGrabber pg = new PixelGrabber(generatedImage, 0, 0, 50, 50,
						generatedPixels, 0, 50);
				try {
					pg.grabPixels();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				int[] targetPixels = instance.getTargetPixels();
				for (int i = 0; i < generatedPixels.length && i < targetPixels.length; i++) {
					int c1 = targetPixels[i];
					int c2 = generatedPixels[i];
					int red = ((c1 >> 16) & 0xff) - ((c2 >> 16) & 0xff);
					int green = ((c1 >> 8) & 0xff) - ((c2 >> 8) & 0xff);
					int blue = (c1 & 0xff) - (c2 & 0xff);
					sum += red * red + green * green + blue * blue;
				}
				System.out.println(gridNum);
				gridFitness[gridNum] = Math.sqrt((double) sum);
				gridNum++;
			}
		}
		
		return gridFitness;
		
	}

	//method from original code
	public Solution applyMutation() {
		Solution temp = this.copy();
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
		int valueIndex = r.nextInt(VALUES_PER_TRIANGLE);
		if (valueIndex < 4) {
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256);
		} else {
			if (valueIndex % 2 == 0) {
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageWidth() + 1);
			} else {
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageHeight() + 1);
			}
		}
		return temp;
	}
	
	public Solution applyMutationGrid(double mutationProbability,double averageFitness){
		
		double[] gridFitness = evaluateGrid();
		System.out.println("grid length " + gridFitness.length);
		Solution temp = this.copy();
		
		double fitnessSum=0;
		
		for (int i = 0; i <temp.values.length/VALUES_PER_TRIANGLE; i++) {
			int gridNum = 0;
			Polygon triangle = expressPolygon(i);
			for (int x=0;x<=150;x+=50){
				for (int y=0;y<=150;y+=50){
					if(triangle.intersects(x,y,50,50)){
						System.out.println(gridNum);
						fitnessSum += gridFitness[gridNum];
					}
					System.out.println(gridNum);
					gridNum++;
				}
			}			
			
		}
		
		if (fitnessSum/100<averageFitness){
			temp=applyMutation(0.5);
		}
		else{
			//apply mutation at a lower rate
			temp=applyMutation(mutationProbability);
		}
		return temp;	
	}
			
	

	public Solution applyMutation(double mutationProbability) {
		return applyMutation(mutationProbability, mutationOperators);
	}
	
	public Solution applyMutation(double mutationProbability, MutationOperator[] muOps) {
		Solution temp = this.copy();
		for (int i = 0; i <instance.getNumberOfTriangles(); i++) {
			if (r.nextDouble() <= mutationProbability) {
				//if probability condition met, randomly apply one of the mutation methods entered as parameters
				MutationOperator muOp = muOps[r.nextInt(muOps.length)];
				switch(muOp) {
				case oneValue:
					temp=applyMutationOneValueChange(i);
					break;
				case orderFlip:
					temp=applyMutationFlipOrder(i);
					break;
				case locationFlip:
					temp=applyMutationFlipLocation(i);
					break;
				case oneValueOccasionalFlipLocation:
					temp=oneValueOccasionalFlipLocation(i);
					break;	
				case addorSubtractValues:
					temp=applyAddorSubtractValues(i);
					break;
				case deltaBased:
					temp=applyDeltaBased(i);
					break;
				case manyValueChange:
					temp=applyMutationManyValueChange(i);
					break;
				case manyValueAddSubtract:
					temp=applyManyAddorSubtractValues(i);
					break;
				}								
			}
		}			
		return temp;	
		}
			
	
	public Solution applyMutationOneValueChange(int triangleIndex){
		Solution temp = this.copy();		
		int valueIndex = r.nextInt(VALUES_PER_TRIANGLE);
		if (valueIndex < 4) {
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256);
		} else {
			if (valueIndex % 2 == 0) {
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageWidth() + 1);
			} else {
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageHeight() + 1);
			}
		}
		return temp;
	}

	public Solution applyMutationManyValueChange(int triangleIndex){
		Solution temp = this.copy();
				
		for (int valueIndex=0; valueIndex<10; valueIndex++){

			if (valueIndex < 4) {
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256);
			} 
			else {
				if (valueIndex % 2 == 0) {
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageWidth() + 1);
				} else {
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageHeight() + 1);
				}
			}

		}
		
		return temp;
	}
	
	public Solution applyMutationFlipLocation(int triangleIndexOne){
		Solution temp = this.copy();
		int triangleIndexTwo = r.nextInt(instance.getNumberOfTriangles());
		
		for (int i = 4; i <= 8; i += 2) {

			temp.values[triangleIndexOne * VALUES_PER_TRIANGLE + i] = values[triangleIndexTwo * VALUES_PER_TRIANGLE + i];
			temp.values[triangleIndexOne * VALUES_PER_TRIANGLE + i + 1] = values[triangleIndexTwo * VALUES_PER_TRIANGLE + i + 1];
			
			temp.values[triangleIndexTwo * VALUES_PER_TRIANGLE + i] = values[triangleIndexOne * VALUES_PER_TRIANGLE + i];
			temp.values[triangleIndexTwo * VALUES_PER_TRIANGLE + i + 1] = values[triangleIndexOne * VALUES_PER_TRIANGLE + i +1];
		}
		
		return temp;
	}
	
	public Solution applyMutationFlipOrder(int triangleIndexOne){
		Solution temp = this.copy();		
		int triangleIndexTwo = r.nextInt(instance.getNumberOfTriangles());	
		
		for (int i = 0; i < 10; i ++) {

			temp.values[triangleIndexOne * VALUES_PER_TRIANGLE + i] = values[triangleIndexTwo * VALUES_PER_TRIANGLE + i];	
			temp.values[triangleIndexTwo * VALUES_PER_TRIANGLE + i] = values[triangleIndexOne * VALUES_PER_TRIANGLE + i];
		}
		
		return temp;
	}
	
	
	public Solution oneValueOccasionalFlipLocation(int triangleIndex){
		Solution temp = this.copy();
		int num=r.nextInt(4);
		if (num<2){
			temp=applyMutationOneValueChange(triangleIndex);
		}
		else if (num==2){
			temp=applyMutationFlipLocation(triangleIndex);
		}
		else if (num==3){
			temp=applyMutationFlipOrder(triangleIndex);
		}
		return temp;
	}
	
	public Solution applyAddorSubtractValues(int triangleIndex){
		
		double changePercent=0.05;
		int newValue;
		if (r.nextInt(1)<1){
			changePercent=-1*changePercent;
		}
		Solution temp = this.copy();
		int valueIndex = r.nextInt(VALUES_PER_TRIANGLE);
		System.out.println("Old value " + temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex]);
		if (valueIndex < 4) {
			newValue = temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex]+(int) (255*changePercent);
			if (newValue > 255){newValue = 255;}
			if (newValue < 0){newValue = 0;}
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = newValue;

		} else {
			if (valueIndex % 2 == 0) {
				newValue=temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] +(int) (instance.getImageWidth()*changePercent);
				if (newValue > instance.getImageWidth()){newValue = instance.getImageWidth();}
				if (newValue < 0){newValue = 0;}
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = newValue;

			} else {
				newValue=temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] + (int) (instance.getImageHeight()*changePercent);
				if (newValue > instance.getImageHeight()){newValue = instance.getImageHeight();}
				if (newValue < 0){newValue = 0;}
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = newValue;
				

			}
		}
		System.out.println("New value " + newValue);
		return temp;
	}
	
	public Solution applyManyAddorSubtractValues(int triangleIndex){
		
		double changePercent=0.05;
		int newValue;
		//make percent change negative with 50% probability
		if (r.nextInt(1)<1){
			changePercent=-1*changePercent;
		}		
		Solution temp = this.copy();		
		for (int valueIndex=0; valueIndex<10; valueIndex++){
			//System.out.println("Original value " + valueIndex + ": " + temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex]);
			if (valueIndex < 4) {
				newValue = temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex]+(int) (255*changePercent);
				if (newValue > 255){newValue = 255;}
				if (newValue < 0){newValue = 0;}
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = newValue;
	
			} else {
				if (valueIndex % 2 == 0) {
					newValue=temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] +(int) (instance.getImageWidth()*changePercent);
					if (newValue > instance.getImageWidth()){newValue = instance.getImageWidth();}
					if (newValue < 0){newValue = 0;}
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = newValue;
	
				} else {
					newValue=temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] + (int) (instance.getImageHeight()*changePercent);
					if (newValue > instance.getImageHeight()){newValue = instance.getImageHeight();}
					if (newValue < 0){newValue = 0;}
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = newValue;
					
				}
			}
			//System.out.println("New value " + valueIndex + ": " + temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex]);
		}
		return temp;
	}
	
	
	public Solution applyDeltaBased(int triangleIndex){
		Solution temp = this.copy();
		
		if (fitness >12000){
			temp = applyMutationOneValueChange(triangleIndex);
		}
		
		else{
			temp = applyAddorSubtractValues(triangleIndex);
		}
		return temp;
	}
	
	public void draw() {
		BufferedImage generatedImage = createImage();
		Graphics g = ProblemInstance.view.getFittestDrawingView().getMainPanel().getGraphics();
		g.drawImage(generatedImage, 0, 0, ProblemInstance.view.getFittestDrawingView());
	}

	public void print() {
		System.out.printf("Fitness: %.1f\n", fitness);
	}

	public int getValue(int index) {
		return values[index];
	}

	public void setValue(int index, int value) {
		values[index] = value;
	}

	public int getHue(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 0];
	}

	public int getSaturation(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 1];
	}

	public int getBrightness(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 2];
	}

	public int getAlpha(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 3];
	}

	public int getXFromVertex1(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 4];
	}

	public int getYFromVertex1(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 5];
	}

	public int getXFromVertex2(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 6];
	}

	public int getYFromVertex2(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 7];
	}

	public int getXFromVertex3(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 8];
	}

	public int getYFromVertex3(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 9];
	}

	public void setHue(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 0] = value;
	}

	public void setSaturation(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 1] = value;
	}

	public void setBrightness(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 2] = value;
	}

	public void setAlpha(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 3] = value;
	}

	public void setXFromVertex1(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 4] = value;
	}

	public void setYFromVertex1(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 5] = value;
	}

	public void setXFromVertex2(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 6] = value;
	}

	public void setYFromVertex2(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 7] = value;
	}

	public void setXFromVertex3(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 8] = value;
	}

	public void setYFromVertex3(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 9] = value;
	}

	public int[] getVertex1(int triangleIndex) {
		return new int[] { getXFromVertex1(triangleIndex), getYFromVertex1(triangleIndex) };
	}

	public int[] getVertex2(int triangleIndex) {
		return new int[] { getXFromVertex2(triangleIndex), getYFromVertex2(triangleIndex) };
	}

	public int[] getVertex3(int triangleIndex) {
		return new int[] { getXFromVertex3(triangleIndex), getYFromVertex3(triangleIndex) };
	}

	public ProblemInstance getInstance() {
		return instance;
	}

	public int[] getValues() {
		return values;
	}

	public double getFitness() {
		return fitness;
	}

	public Solution copy() {
		Solution temp = new Solution(instance);
		for (int i = 0; i < values.length; i++) {
			temp.values[i] = values[i];
		}
		temp.fitness = fitness;
		return temp;
	}

	private BufferedImage createImage() {
		BufferedImage target = instance.getTargetImage();
		BufferedImage generatedImage = new BufferedImage(target.getWidth(), target.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics generatedGraphics = generatedImage.getGraphics();

		generatedGraphics.setColor(Color.GRAY);
		generatedGraphics.fillRect(0, 0, generatedImage.getWidth(), generatedImage.getHeight());
		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			generatedGraphics.setColor(expressColor(triangleIndex));
			generatedGraphics.fillPolygon(expressPolygon(triangleIndex));
		}
		return generatedImage;
	}

	private Color expressColor(int triangleIndex) {
		int hue = getHue(triangleIndex);
		int saturation = getSaturation(triangleIndex);
		int brightness = getBrightness(triangleIndex);
		int alpha = getAlpha(triangleIndex);
		Color c = Color.getHSBColor(hue / 255.0f, saturation / 255.0f, brightness / 255.0f);
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	private Polygon expressPolygon(int triangleIndex) {
		int[] xs = new int[] { getXFromVertex1(triangleIndex), getXFromVertex2(triangleIndex),
				getXFromVertex3(triangleIndex) };
		int[] ys = new int[] { getYFromVertex1(triangleIndex), getYFromVertex2(triangleIndex),
				getYFromVertex3(triangleIndex) };
		return new Polygon(xs, ys, 3);
	}

	@Override
	public int compareTo(Solution solution) {
		double diff = this.getFitness() - solution.getFitness();
		if(diff < 0) {
			return -1;
		}
		else if(diff > 0) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
