package se.ericthelin.junit.singlemethodrunner;

import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

public class SingleMethodRunner extends ParentRunner<Runner> {

    private static final String TEST_METHOD_PROPERTY_NAME = "test.method";

    private final List<Runner> children;

    public SingleMethodRunner(Class<?> klass) throws InitializationError {
	super(klass);

	try {
	    this.children = computeChildren();
	} catch (RuntimeException | ClassNotFoundException
		| NoTestsRemainException e) {
	    throw new InitializationError(e);
	}

    }

    private List<Runner> computeChildren() throws InitializationError,
	    ClassNotFoundException, NoTestsRemainException {

	String testMethodPattern = System
		.getProperty(TEST_METHOD_PROPERTY_NAME);

	if (testMethodPattern == null) {
	    return Collections.emptyList();
	}

	return Collections
		.singletonList(junitRunnerFor(new TestMethodReference(
			testMethodPattern)));
    }

    private static Runner junitRunnerFor(TestMethodReference reference)
	    throws InitializationError, NoTestsRemainException {

	BlockJUnit4ClassRunner result = new BlockJUnit4ClassRunner(
		reference.getTestClass());

	result.filter(Filter.matchMethodDescription(reference.toDescription()));

	return result;
    }

    @Override
    protected List<Runner> getChildren() {
	return children;
    }

    @Override
    protected Description describeChild(Runner child) {
	return child.getDescription();
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
	child.run(notifier);
    }
}