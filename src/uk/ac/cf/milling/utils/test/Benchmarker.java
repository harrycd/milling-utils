package uk.ac.cf.milling.utils.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class Benchmarker {
	
	
	
	
	/**
	 * @param supplier - The method that execution time is calculated
	 * @param iterations - Number of iterations for the test
	 * @return the average execution time in milliseconds 
	 */
	public static double testExecutionTime(Supplier<Object> supplier, int iterations) {
		long start = System.currentTimeMillis();
		double execTime = 0;
		
		for (int it = 0; it < iterations; it++) {
			supplier.get();
		}
		
		execTime = (System.currentTimeMillis() - start) / iterations;
		System.out.println("Execution time: " + execTime);
		
		return execTime;
	}
	
	public static double testExecutionTime(Method method, int iterations) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		long start = System.currentTimeMillis();
		double execTime = 0.0;
		
		try {
			for (int it = 0; it < iterations; it++) {
				
				method.invoke(method.getDeclaringClass().newInstance());
			}
			
		} catch (SecurityException | InstantiationException e) {
			e.printStackTrace();
		}
		
		
		execTime = 1.0*(System.currentTimeMillis() - start) / iterations;
		System.out.println("Execution time: " + execTime);
		
		return execTime;
	}
	
}
