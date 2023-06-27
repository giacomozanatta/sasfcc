package it.jack.sasfra;

import it.jack.sasfra.antlr.JavaScriptLexer;
import it.jack.sasfra.antlr.JavaScriptParser;
import it.jack.sasfra.antlr.JavaScriptParserBaseVisitor;
import it.jack.sasfra.libraries.LibrarySpecificationProvider;
import it.unive.lisa.AnalysisSetupException;
import it.unive.lisa.program.CodeUnit;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.type.BoolType;
import it.unive.lisa.program.type.Float32Type;
import it.unive.lisa.program.type.Int32Type;
import it.unive.lisa.program.type.StringType;
import it.unive.lisa.type.NullType;
import it.unive.lisa.type.TypeSystem;
import it.unive.lisa.type.Untyped;
import it.unive.lisa.type.VoidType;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JSFrontend extends JavaScriptParserBaseVisitor<Object> {
    private Program program;
    private String filePath;
    
    private static final Logger log = LogManager.getLogger(JSFrontend.class);
    public JSFrontend(String filePath) {
        this.program = new Program(new JavascriptFeatures(), new JavascriptTypeSystem());
        this.filePath = filePath;
        program.addUnit(new CodeUnit(new SourceCodeLocation(filePath, 0, 0),
                program, FilenameUtils.getBaseName(filePath)));
        
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public Program toLiSAProgram() throws IOException, AnalysisSetupException {
        TypeSystem types = program.getTypes();
        types.registerType(BoolType.INSTANCE);
        types.registerType(StringType.INSTANCE);
        types.registerType(Int32Type.INSTANCE);
        types.registerType(Float32Type.INSTANCE);
        types.registerType(NullType.INSTANCE);
        types.registerType(VoidType.INSTANCE);
        types.registerType(Untyped.INSTANCE);
    
        LibrarySpecificationProvider.load(program);
    
        log.info("Reading file... " + filePath);
    
        JavaScriptLexer lexer = null;
        try (InputStream stream = new FileInputStream(getFilePath());) {
            lexer = new JavaScriptLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IOException("Unable to parse '" + filePath + "'", e);
        }
    
        JavaScriptParser parser = new JavaScriptParser(new CommonTokenStream(lexer));
        JavaScriptParser.ProgramContext programContext = parser.expressionSequence();
        return program;
    }
}
