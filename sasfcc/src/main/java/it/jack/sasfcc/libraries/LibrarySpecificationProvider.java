package it.jack.sasfcc.libraries;

import it.unive.lisa.AnalysisSetupException;
import it.unive.lisa.program.CodeUnit;
import it.unive.lisa.program.CompilationUnit;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.SyntheticLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.statement.Ret;
import it.jack.sasfcc.antlr.LibraryDefinitionLexer;
import it.jack.sasfcc.antlr.LibraryDefinitionParser;
import it.jack.sasfcc.libraries.loader.Library;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.tuple.Pair;

import it.jack.sasfcc.libraries.loader.Runtime;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class LibrarySpecificationProvider {
	
	private static final Map<String, Library> LIBS = new HashMap<>();

	public static CompilationUnit hierarchyRoot;

	private static CFG init;

	public static void load(Program program) throws AnalysisSetupException {
		String file = "/libs.txt";

		LibraryDefinitionLexer lexer = null;
		try (InputStream stream = LibrarySpecificationParser.class.getResourceAsStream(file);) {
			lexer = new LibraryDefinitionLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new AnalysisSetupException("Unable to parse '" + file + "'", e);
		}

		LibraryDefinitionParser parser = new LibraryDefinitionParser(new CommonTokenStream(lexer));
		LibrarySpecificationParser libParser = new LibrarySpecificationParser(file);
		Pair<Runtime, Collection<Library>> parsed = libParser.visitFile(parser.file());

		AtomicReference<CompilationUnit> root = new AtomicReference<CompilationUnit>(null);
		parsed.getLeft().fillProgram(program, root);
		if (root.get() == null)
			throw new AnalysisSetupException("Runtime does not contain a hierarchy root");
		hierarchyRoot = root.get();
		makeInit(program);
		parsed.getLeft().populateProgram(program, init, hierarchyRoot);

		for (Library lib : parsed.getValue())
			LIBS.put(lib.getName(), lib);
	}

	private static CFG makeInit(Program program) {
		init = new CFG(new CodeMemberDescriptor(SyntheticLocation.INSTANCE, program, false, "LiSA$init"));
		init.addNode(new Ret(init, SyntheticLocation.INSTANCE), true);
		program.addCodeMember(init);
		return init;
	}

	public static void importLibrary(Program program, String name) {
		Library library = LIBS.get(name);
		if (library == null)
			// TODO do we log? could also be imports to other files under analysis...
			return;
		CodeUnit lib = library.toLiSAUnit(program, new AtomicReference<>(hierarchyRoot));
		library.populateUnit(init, hierarchyRoot, lib);
	}

	public static Collection<Library> getLibraryUnits() {
		return LIBS.values();
	}

	public static Library getLibraryUnit(String name) {
		return LIBS.get(name);
	}
}
