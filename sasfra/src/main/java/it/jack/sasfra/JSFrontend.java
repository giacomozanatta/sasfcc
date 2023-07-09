package it.jack.sasfra;

import it.jack.sasfra.antlr.JavaScriptLexer;
import it.jack.sasfra.antlr.JavaScriptParser;
import it.jack.sasfra.antlr.JavaScriptParser.ArgumentsContext;
import it.jack.sasfra.antlr.JavaScriptParser.ArgumentsExpressionContext;
import it.jack.sasfra.antlr.JavaScriptParser.AssignableContext;
import it.jack.sasfra.antlr.JavaScriptParser.ExpressionSequenceContext;
import it.jack.sasfra.antlr.JavaScriptParser.ExpressionStatementContext;
import it.jack.sasfra.antlr.JavaScriptParser.IdentifierContext;
import it.jack.sasfra.antlr.JavaScriptParser.LiteralContext;
import it.jack.sasfra.antlr.JavaScriptParser.LiteralExpressionContext;
import it.jack.sasfra.antlr.JavaScriptParser.NumericLiteralContext;
import it.jack.sasfra.antlr.JavaScriptParser.SingleExpressionContext;
import it.jack.sasfra.antlr.JavaScriptParser.SourceElementContext;
import it.jack.sasfra.antlr.JavaScriptParser.SourceElementsContext;
import it.jack.sasfra.antlr.JavaScriptParser.VariableDeclarationContext;
import it.jack.sasfra.antlr.JavaScriptParser.VariableDeclarationListContext;
import it.jack.sasfra.antlr.JavaScriptParser.VariableStatementContext;
import it.jack.sasfra.antlr.JavaScriptParserBaseVisitor;
import it.jack.sasfra.libraries.LibrarySpecificationProvider;
import it.unive.lisa.AnalysisSetupException;
import it.unive.lisa.program.CodeUnit;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.Parameter;
import it.unive.lisa.program.cfg.VariableTableEntry;
import it.unive.lisa.program.cfg.edge.SequentialEdge;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Ret;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.literal.Float32Literal;
import it.unive.lisa.program.cfg.statement.literal.Int32Literal;
import it.unive.lisa.program.cfg.statement.literal.Literal;
import it.unive.lisa.program.cfg.statement.literal.StringLiteral;
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
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.RuleNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;

public class JSFrontend extends JavaScriptParserBaseVisitor<Object> {
    private Program program;
    private String filePath;
    private CodeUnit currentUnit;
    private CFG currentCFG;

    private static final Logger log = LogManager.getLogger(JSFrontend.class);



    public JSFrontend(String filePath) {
        this.program = new Program(new JavascriptFeatures(), new JavascriptTypeSystem());
        this.filePath = filePath;
        currentUnit = new CodeUnit(new SourceCodeLocation(filePath, 0, 0), program, FilenameUtils.getBaseName(filePath));
        program.addUnit(currentUnit);
        
    }

    @Override
    public Object visitChildren(RuleNode node) {
        // TODO Auto-generated method stub
        return super.visitChildren(node);
    }

    public String getFilePath() {
        return filePath;
    }
    

    private void registerJSTypes() {
        TypeSystem types = program.getTypes();
        types.registerType(BoolType.INSTANCE);
        types.registerType(StringType.INSTANCE);
        types.registerType(Int32Type.INSTANCE);
        types.registerType(Float32Type.INSTANCE);
        types.registerType(NullType.INSTANCE);
        types.registerType(VoidType.INSTANCE);
        types.registerType(Untyped.INSTANCE);
    }

    private JavaScriptParser getJavaScriptParser(String filePath) throws IOException {
        log.info("Reading file... " + filePath);
    
        JavaScriptLexer lexer = null;
        try (InputStream stream = new FileInputStream(getFilePath());) {
            lexer = new JavaScriptLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IOException("Unable to parse '" + filePath + "'", e);
        }
    
        return new JavaScriptParser(new CommonTokenStream(lexer));
    }
    public Program toLiSAProgram() throws IOException, AnalysisSetupException {
        // Register Javascript types
        registerJSTypes();
        
        // load Libraries from SARL
        LibrarySpecificationProvider.load(program);
        
        // load the Program
        JavaScriptParser parser = getJavaScriptParser(filePath);
        
        // visit the Program
        visitProgram(parser.program());

        for (CFG cm : program.getAllCFGs())
			if (cm.getDescriptor().getName().equals("$main"))
				program.addEntryPoint(cm);

        return program;
    }

    private CodeMemberDescriptor buildMainCFGDescriptor(SourceCodeLocation loc) {
		Parameter[] cfgArgs = new Parameter[] {};

		return new CodeMemberDescriptor(loc, currentUnit, false, "$main", cfgArgs);
	}
    @Override
    public Object visitProgram(JavaScriptParser.ProgramContext ctx) {
        	
        currentCFG = new CFG(buildMainCFGDescriptor(getLocation(ctx)));
        currentUnit.addCodeMember(currentCFG);
        // Program: HashBangLine? | SourceElements? | EOF
        
        if (ctx.sourceElements() != null) {
            visitSourceElements(ctx.sourceElements());
        }

        addRetNodesToCurrentCFG();
        
        return null;
    }
    
    public Object visitSourceElements(SourceElementsContext ctx) {
        // SourceElements: SourceElement+
        if (ctx.sourceElement() != null) {
            for (SourceElementContext sec : ctx.sourceElement()) {
                Object e = visitSourceElement(sec);
                System.out.println(e);
            }
        }
        return null;
    }
    
    public Object visitSourceElement(SourceElementContext ctx) {
        if (ctx.statement() != null) {
            visitStatement(ctx.statement());
        }
        return null;
    }

    @Override
    public Statement[] visitVariableDeclarationList(VariableDeclarationListContext ctx) {
        // variableDeclarationList: varModifier variableDeclaration (',' variableDeclaration)*

        if (ctx.variableDeclaration() != null) {
            for (VariableDeclarationContext vdc : ctx.variableDeclaration()) {
                Object e = visitVariableDeclaration(vdc);
            }
        }
        return null;
    }

    @Override
    public Statement[] visitVariableStatement(VariableStatementContext ctx){
        if (ctx.variableDeclarationList() != null) {
            return visitVariableDeclarationList(ctx.variableDeclarationList());
        } 
        return null;
    }

    @Override
    public Object visitStatement(JavaScriptParser.StatementContext ctx) {
        if (ctx.expressionStatement() != null) {
            return visitExpressionStatement(ctx.expressionStatement());
        }
        if (ctx.variableStatement() != null) {
            return visitVariableStatement(ctx.variableStatement());
        }
        return null;
    }
    

    public Expression visitLiteral(LiteralContext ctx) {
        if (ctx.StringLiteral() != null) {
            // TERMINAL NODE
            return new StringLiteral(currentCFG,getLocation(ctx), ctx.StringLiteral().getText());
        }
        if (ctx.numericLiteral() != null) {
            return visitNumericLiteral(ctx.numericLiteral());
        }
        return null;

    }

    public Expression visitNumericLiteral(NumericLiteralContext ctx) {
        if (ctx.DecimalLiteral() != null) {
            String number = ctx.DecimalLiteral().toString();
            if (number.contains(".") || number.contains("e")) {
                // float number
                return new Float32Literal(currentCFG, getLocation(ctx), Float.parseFloat(number));
            }
            return new Int32Literal(currentCFG, getLocation(ctx), Integer.parseInt(number));
        }

        //TODO: handle Hex, Octal and Binary
        if (ctx.HexIntegerLiteral() != null) {
            throw new RuntimeException();
        }

        if (ctx.OctalIntegerLiteral() != null) {
            throw new RuntimeException();
        }


        if (ctx.BinaryIntegerLiteral() != null) {
            throw new RuntimeException();
        }
        throw new RuntimeException();
    }
    public Object visitArguments(ArgumentsContext ctx) {
        return null;
    }

    public Expression visitSingleExpression(SingleExpressionContext ctx) {
        if (ctx instanceof LiteralExpressionContext) {
            LiteralExpressionContext lec = (LiteralExpressionContext) ctx;
            if (lec.literal() != null) {
                return visitLiteral(lec.literal());
            }
        }

        if (ctx instanceof ArgumentsExpressionContext) {
            ArgumentsExpressionContext aec = (ArgumentsExpressionContext) ctx;
            if (aec.arguments() != null) {
                visitArguments(aec.arguments());
            } 
            System.out.println(aec);
        }
        return null;
    }

    public Object visitExpressionSequence(ExpressionSequenceContext ctx) {
        if (ctx.singleExpression() != null) {
            for (SingleExpressionContext sec : ctx.singleExpression()) {
                Expression e = visitSingleExpression(sec);            
                System.out.println(e);
            }
        }
        return super.visitExpressionSequence(ctx);
    }


    public Expression visitIdentifier(IdentifierContext ctx) {
        if (ctx.Identifier() != null) {
            String identifierName = ctx.Identifier().getSymbol().getText();
            // VariableRef (?)
            return new VariableRef(currentCFG, getLocation(ctx), identifierName);
        }
        return null;
    }
    public Object visitExpressionStatement(ExpressionStatementContext ctx) {
        if (ctx.expressionSequence() != null) {
            return visitExpressionSequence(ctx.expressionSequence());
        }
        return super.visitExpressionStatement(ctx);
    }
    @Override
    public Statement visitVariableDeclaration(JavaScriptParser.VariableDeclarationContext ctx) {
        // assignable ('=' singleExpression)?
       
        if (ctx.assignable() == null && ctx.singleExpression() == null) {
            throw new RuntimeException();
        }
        Expression variableRef = visitAssignable(ctx.assignable());
        if (variableRef == null) {
            throw new RuntimeException();
        }

        Expression singleExpression = visitSingleExpression(ctx.singleExpression());
        if (singleExpression == null) {
            throw new RuntimeException();
        }
        Assignment assignment = new Assignment(currentCFG, getLocation(ctx), variableRef, singleExpression);
        addNodeOnCFG(currentCFG, assignment);
        return assignment;
    }

    @Override
    public Expression visitAssignable(AssignableContext ctx) {
        // assignable: identifier | arrayLiteral | objectLiteral;
        if (ctx.identifier() != null) {
            return visitIdentifier(ctx.identifier());
        }

        return null;
    }
    
    public SourceCodeLocation getLocation(ParserRuleContext ctx) {
		return new SourceCodeLocation(this.getFilePath(), getLine(ctx), getCol(ctx));
	}

    private int getLine(ParserRuleContext ctx) {
		return ctx.getStart().getLine();
	}

    private int getCol(ParserRuleContext ctx) {
		return ctx.getStop().getCharPositionInLine();
	}

    private void addNodeOnCFG(CFG cfg, Statement statement) {
        boolean entryPoint = cfg.getEntrypoints().isEmpty();
        cfg.addNode(statement, entryPoint);
    }

    private void addRetNodesToCurrentCFG() {
		if (currentCFG.getAllExitpoints().isEmpty()) {
			Ret ret = new Ret(currentCFG, currentCFG.getDescriptor().getLocation());
			if (currentCFG.getNodesCount() == 0) {
				currentCFG.addNode(ret, true);
			} else {
				Collection<Statement> preExits = new LinkedList<>();
				for (Statement st : currentCFG.getNodes())
					if (!st.stopsExecution() && currentCFG.followersOf(st).isEmpty())
						preExits.add(st);
				currentCFG.addNode(ret);
				for (Statement st : preExits)
					currentCFG.addEdge(new SequentialEdge(st, ret));

				for (VariableTableEntry entry : currentCFG.getDescriptor().getVariables())
					if (preExits.contains(entry.getScopeEnd()))
						entry.setScopeEnd(ret);
			}
		}
	}
}
