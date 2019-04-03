package eu.druglogics.drabme.input;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommandLineArgsTest {

    @Test
    public void test_all_arguments() {
        CommandLineArgs args = new CommandLineArgs();

        String[] argv = {"-p", "project", "-md", "modelsDir", "-dr", "drugs",
                "-c", "config", "-m", "outputs"};
        JCommander.newBuilder().addObject(args).build().parse(argv);

        Assert.assertEquals(args.getProjectName(), "project");
        Assert.assertEquals(args.getDirectoryModels(), "modelsDir");
        Assert.assertEquals(args.getFilenameDrugs(), "drugs");
        Assert.assertEquals(args.getFilenameConfig(), "config");
        Assert.assertEquals(args.getFilenameModelOutputs(), "outputs");
        Assert.assertNull(args.getFilenamePerturbations());
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void test_no_arguments() {
        exception.expect(ParameterException.class);

        CommandLineArgs args = new CommandLineArgs();
        JCommander.newBuilder().addObject(args).build().parse("");
    }

    @Test
    public void test_missing_required_arguments() {
        exception.expect(ParameterException.class);

        CommandLineArgs args = new CommandLineArgs();
        String[] argv = {"-p", "project", "-c", "config"};
        JCommander.newBuilder().addObject(args).build().parse(argv);
    }

}
