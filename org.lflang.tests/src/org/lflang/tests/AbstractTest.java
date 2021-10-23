package org.lflang.tests;

import java.util.EnumSet;
import java.util.List;

import org.junit.Assume;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.lflang.ASTUtils;
import org.lflang.Target;
import org.lflang.tests.TestRegistry.TestCategory;

/**
 * A collection of JUnit tests to perform on a given set of targets.
 * 
 * @author Marten Lohstroh <marten@berkeley.edu>
 *
 */
public abstract class AbstractTest extends TestBase {

    /**
     * Construct a test instance that runs tests for a single target.
     *
     * @param target The target to run tests for.
     */
    protected AbstractTest(Target target) {
        super(target);
    }

    /**
     * Construct a test instance that runs tests for a list of targets.
     * @param targets The targets to run tests for.
     */
    protected AbstractTest(List<Target> targets) {
        super(targets);
    }


    /**
     * Whether to enable {@link #runWithFourThreads()}.
     */
    protected boolean supportsThreadsOption() {
        return false;
    }

    /**
     * Whether to enable {@link #runFederatedTests()}.
     */
    protected boolean supportsFederatedExecution() {
        return false;
    }

    /**
     * Whether to enable {@link #runTestsAboutGenerics()}.
     */
    protected boolean supportsGenericTypes() {
        return false;
    }


    @Test
    public void runExampleTests() {
        runTestsForTargets("Description: Run example tests.",
                TestCategory.EXAMPLE_TEST::equals, Configurators::noChanges,
                TestLevel.EXECUTION, false);
    }

    @Test
    public void validateExamples() {
        runTestsForTargets("Description: Validate examples.",
                TestCategory.EXAMPLE::equals, Configurators::noChanges, TestLevel.VALIDATION,
                false);
    }

    @Test
    public void runGenericTests() {
        runTestsForTargets("Description: Run generic tests (threads = 0).",
                           TestCategory.GENERIC::equals, Configurators::useSingleThread,
                           TestLevel.EXECUTION, false);
    }

    @Test
    public void runTargetSpecificTests() {
        runTestsForTargets("Description: Run target-specific tests (threads = 0).",
                           TestCategory.TARGET::equals, Configurators::useSingleThread,
                           TestLevel.EXECUTION, false);
    }

    @Test
    public void runMultiportTests() {
        runTestsForTargets("Description: Run multiport tests (threads = 0).",
                           TestCategory.MULTIPORT::equals, Configurators::useSingleThread,
                           TestLevel.EXECUTION, false);
    }

    @Test
    public void runTestsAboutGenerics() {
        Assumptions.assumeTrue(supportsGenericTypes(), "Target should support generic types");
        runTestsForTargets("Description: Run tests about generics.",
                           TestCategory.GENERICS::equals, Configurators::useSingleThread,
                           TestLevel.EXECUTION, false);
    }


    @Test
    public void runSerializationTests() {
        runTestsForTargets("Description: Run serialization tests (threads = 0).",
                           TestCategory.SERIALIZATION::equals, Configurators::useSingleThread,
                           TestLevel.EXECUTION, false);
    }

    @Test
    public void runAsFederated() {
        Assumptions.assumeTrue(supportsFederatedExecution(), Message.NO_FEDERATION_SUPPORT);

        EnumSet<TestCategory> categories = EnumSet.allOf(TestCategory.class);
        categories.removeAll(EnumSet.of(TestCategory.CONCURRENT,
                                        TestCategory.FEDERATED,
                                        TestCategory.EXAMPLE,
                                        TestCategory.EXAMPLE_TEST,
                                        // FIXME: also run the multiport tests once these are supported.
                                        TestCategory.MULTIPORT));

        runTestsFor(List.of(Target.C),
                    Message.DESC_AS_FEDERATED,
                    categories::contains,
                    it -> ASTUtils.makeFederated(it.fileConfig.resource),
                    TestLevel.EXECUTION,
                    true);
    }


    @Test
    public void runConcurrentTests() {
        runTestsForTargets(Message.DESC_CONCURRENT,
                           TestCategory.CONCURRENT::equals, Configurators::noChanges, TestLevel.EXECUTION,
                           false);

    }

    @Test
    public void runFederatedTests() {
        Assumptions.assumeTrue(supportsFederatedExecution(), Message.NO_FEDERATION_SUPPORT);
        runTestsForTargets(Message.DESC_FEDERATED,
                           TestCategory.FEDERATED::equals, Configurators::noChanges, TestLevel.EXECUTION,
                           false);
    }


    @Test
    public void runWithFourThreads() {
        Assumptions.assumeTrue(supportsThreadsOption(), Message.NO_THREAD_SUPPORT);
        this.runTestsForTargets(
            Message.DESC_FOUR_THREADS,
            Configurators::defaultCategoryExclusion,
            Configurators::useFourThreads,
            TestLevel.EXECUTION,
            true
        );
    }
}
