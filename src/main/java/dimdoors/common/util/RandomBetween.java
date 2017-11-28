package dimdoors.common.util;

import java.util.Random;

public class RandomBetween {

	public static int getRandomIntBetween(Random rand, int min, int max){
		return rand.nextInt(max - min) + min;
	}

	public static float getRandomFloatBetween(Random rand, float min, float max){
		return (rand.nextFloat() * (max - min)) + min;
	}

	public static double getRandomDoubleBetween(Random rand, double min, double max){
		return (rand.nextDouble() * (max - min)) + min;
	}

}
