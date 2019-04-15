package eu.druglogics.drabme.input;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineArgsTest {

    @Test
    void test_all_arguments() {
        CommandLineArgs args = new CommandLineArgs();

        String[] argv = {"-p", "project", "-md", "modelsDir", "-dr", "drugs",
                "-c", "config", "-m", "outputs"};
        JCommander.newBuilder().addObject(args).build().parse(argv);

        assertEquals(args.getProjectName(), "project");
        assertEquals(args.getDirectoryModels(), "modelsDir");
        assertEquals(args.getFilenameDrugs(), "drugs");
        assertEquals(args.getFilenameConfig(), "config");
        assertEquals(args.getFilenameModelOutputs(), "outputs");
        assertNull(args.getFilenamePerturbations());
    }

    @Test
    void test_no_arguments() {
        assertThrows(ParameterException.class, () -> {
            CommandLineArgs args = new CommandLineArgs();
            JCommander.newBuilder().addObject(args).build().parse("");
        });
    }

    @Test
    void test_missing_required_arguments() {
        assertThrows(ParameterException.class, () -> {
            CommandLineArgs args = new CommandLineArgs();
            String[] argv = {"-p", "project", "-c", "config"};
            JCommander.newBuilder().addObject(args).build().parse(argv);
        });
    }

}
