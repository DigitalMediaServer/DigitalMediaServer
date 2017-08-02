package testclasses;


import java.util.List;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import net.pms.PMS;


public class CustomSuite extends Suite {

	/**
	 * Called reflectively on classes annotated with
	 * {@code @RunWith(Suite.class)}.
	 *
	 * @param klass the root class.
	 * @param builder builds runners for classes in the suite.
	 */
	public CustomSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
		super(klass, builder);
		PMS.configureJNA();
	}

	/**
	 * Call this when there is no single root class (for example, multiple class
	 * names passed on the command line to {@link org.junit.runner.JUnitCore}.
	 *
	 * @param builder builds runners for classes in the suite.
	 * @param classes the classes in the suite.
	 */
	public CustomSuite(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
		super(builder, classes);
		PMS.configureJNA();
	}

	/**
	 * Call this when the default builder is good enough. Left in for
	 * compatibility with JUnit 4.4.
	 *
	 * @param klass the root of the suite.
	 * @param suiteClasses the classes in the suite.
	 */
	public CustomSuite(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
		super(klass, suiteClasses);
		PMS.configureJNA();
	}

	/**
	 * Called by this class and subclasses once the runners making up the suite
	 * have been determined.
	 *
	 * @param klass root of the suite.
	 * @param runners for each class in the suite, a {@link Runner}.
	 */
	public CustomSuite(Class<?> klass, List<Runner> runners) throws InitializationError {
		super(klass, runners);
		PMS.configureJNA();
	}

	/**
	 * Called by this class and subclasses once the classes making up the suite
	 * have been determined.
	 *
	 * @param builder builds runners for classes in the suite.
	 * @param klass the root of the suite.
	 * @param suiteClasses the classes in the suite.
	 */
	public CustomSuite(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
		super(builder, klass, suiteClasses);
		PMS.configureJNA();
	}

}
