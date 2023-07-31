package it.jack.sasfcc;

import it.jack.sasfcc.antlr.JavaScriptLexer;
import it.jack.sasfcc.antlr.JavaScriptParser;
import it.jack.sasfcc.antlr.JavaScriptParser.AdditiveExpressionContext;
import it.jack.sasfcc.antlr.JavaScriptParser.AnonymousFunctionContext;
import it.jack.sasfcc.antlr.JavaScriptParser.AnonymousFunctionDeclContext;
import it.jack.sasfcc.antlr.JavaScriptParser.ArgumentContext;
import it.jack.sasfcc.antlr.JavaScriptParser.ArgumentsContext;
import it.jack.sasfcc.antlr.JavaScriptParser.ArgumentsExpressionContext;
import it.jack.sasfcc.antlr.JavaScriptParser.ArrowFunctionContext;
import it.jack.sasfcc.antlr.JavaScriptParser.AssignableContext;
import it.jack.sasfcc.antlr.JavaScriptParser.ExpressionSequenceContext;
import it.jack.sasfcc.antlr.JavaScriptParser.ExpressionStatementContext;
import it.jack.sasfcc.antlr.JavaScriptParser.FunctionBodyContext;
import it.jack.sasfcc.antlr.JavaScriptParser.FunctionExpressionContext;
import it.jack.sasfcc.antlr.JavaScriptParser.IdentifierContext;
import it.jack.sasfcc.antlr.JavaScriptParser.IdentifierExpressionContext;
import it.jack.sasfcc.antlr.JavaScriptParser.LiteralContext;
import it.jack.sasfcc.antlr.JavaScriptParser.LiteralExpressionContext;
import it.jack.sasfcc.antlr.JavaScriptParser.MemberDotExpressionContext;
import it.jack.sasfcc.antlr.JavaScriptParser.NumericLiteralContext;
import it.jack.sasfcc.antlr.JavaScriptParser.SingleExpressionContext;
import it.jack.sasfcc.antlr.JavaScriptParser.SourceElementContext;
import it.jack.sasfcc.antlr.JavaScriptParser.SourceElementsContext;
import it.jack.sasfcc.antlr.JavaScriptParser.VariableDeclarationContext;
import it.jack.sasfcc.antlr.JavaScriptParser.VariableDeclarationListContext;
import it.jack.sasfcc.antlr.JavaScriptParser.VariableStatementContext;
import it.jack.sasfcc.cfg.type.JSClassType;
import it.jack.sasfcc.antlr.JavaScriptParserBaseVisitor;
import it.jack.sasfcc.libraries.LibrarySpecificationProvider;

import it.unive.lisa.util.datastructures.graph.code.NodeList;
import it.unive.lisa.AnalysisSetupException;
import it.unive.lisa.program.CodeUnit;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.Parameter;
import it.unive.lisa.program.cfg.VariableTableEntry;
import it.unive.lisa.program.cfg.edge.Edge;
import it.unive.lisa.program.cfg.edge.SequentialEdge;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Ret;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.program.cfg.statement.literal.Float32Literal;
import it.unive.lisa.program.cfg.statement.literal.Int32Literal;
import it.unive.lisa.program.cfg.statement.literal.StringLiteral;
import it.unive.lisa.program.cfg.statement.numeric.Addition;
import it.unive.lisa.program.cfg.statement.numeric.Subtraction;
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

import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class JSFrontend extends JavaScriptParserBaseVisitor<Object> {
    private Program program;
    private String filePath;
    private CodeUnit currentUnit;
    private CFG currentCFG;

    private static final Logger log = LogManager.getLogger(JSFrontend.class);

    private static final SequentialEdge SEQUENTIAL_SINGLETON = new SequentialEdge();


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


    private CodeMemberDescriptor buildAnonymousCFGDescriptor(SourceCodeLocation loc) {
        Parameter[] cfgArgs = new Parameter[] {};
        String anonF = "$function@" + loc.toString();
        return new CodeMemberDescriptor(loc, currentUnit, false, anonF, cfgArgs);
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
            Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> el = visitSourceElements(ctx.sourceElements());
            currentCFG.getNodeList().mergeWith(el.getMiddle());
            currentCFG.getEntrypoints().add(el.getLeft());
        }

        addRetNodesToCurrentCFG();
        
        return null;
    }
    
    public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitSourceElements(SourceElementsContext ctx) {
        // SourceElements: SourceElement+
        NodeList<CFG, Statement, Edge> block = new NodeList<>(SEQUENTIAL_SINGLETON);
        Statement last = null, first = null;
        if (ctx.sourceElement() != null) {
            for (SourceElementContext sec : ctx.sourceElement()) {
                Object e = visitSourceElement(sec);
                if (e instanceof Triple<?,?,?>) {
                    Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> element = (Triple<Statement, NodeList<CFG, Statement, Edge>, Statement>) e;
                    block.mergeWith(element.getMiddle());
                    if (first == null) {
                        first = element.getLeft();
                    }
                    if (last != null) {
                        block.addEdge(new SequentialEdge(last, element.getLeft()));
                    }
                    last = element.getRight();
                    
                }
            }
        }
        return Triple.of(first, block, last);
    }
    
    public Object visitSourceElement(SourceElementContext ctx) {
        if (ctx.statement() != null) {
            return visitStatement(ctx.statement());
        }
        return null;
    }

    @Override
    public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitVariableDeclarationList(VariableDeclarationListContext ctx) {
        // variableDeclarationList: varModifier variableDeclaration (',' variableDeclaration)*
        NodeList<CFG, Statement, Edge> block = new NodeList<>(SEQUENTIAL_SINGLETON);
        Statement last = null, first = null;

        if (ctx.variableDeclaration() != null) {
            for (VariableDeclarationContext vdc : ctx.variableDeclaration()) {
                Statement s = visitVariableDeclaration(vdc);
                block.addNode(s);
                if (first == null)
                    first = s;
                if (last != null)
                    block.addEdge(new SequentialEdge(last, s));
                last = s;
            }
        }

        return Triple.of(first, block, last);
    }

    @Override
    public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitVariableStatement(VariableStatementContext ctx){
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
            String literal = ctx.StringLiteral().getText();
            literal = literal.substring(1, literal.length()-1);
            
            return new StringLiteral(currentCFG,getLocation(ctx), literal);
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

    public Expression visitArgument(ArgumentContext ctx) {
        /* Ellipsis? (singleExpression | identifier) */
        if (ctx.identifier() != null) {
            return visitIdentifier(ctx.identifier());
        }

        if (ctx.singleExpression() != null) {
            return visitSingleExpression(ctx.singleExpression());
        }
        if (ctx.Ellipsis() != null) {
            //TODO: Ellipsis
            return null;
        }
        return null;
    }
    public List<Expression> visitArguments(ArgumentsContext ctx) {
        List<Expression> arguments = new ArrayList<>();
        for (ArgumentContext ac : ctx.argument()) {
            Expression e = visitArgument(ac);
            if (e != null) {
                arguments.add(e);
            }
        }
        return arguments;
    }

    public Expression visitAdditiveExpression(AdditiveExpressionContext ctx) {
        return null;
    }

    public Expression visitSingleExpression(SingleExpressionContext ctx) {
        if (ctx instanceof LiteralExpressionContext) {
            LiteralExpressionContext lec = (LiteralExpressionContext) ctx;
            if (lec.literal() != null) {
                return visitLiteral(lec.literal());
            }
        }

        if (ctx instanceof IdentifierExpressionContext) {
            IdentifierExpressionContext iec = (IdentifierExpressionContext) ctx;
            return visitIdentifier(iec.identifier());
        }

        if (ctx instanceof AdditiveExpressionContext) {
            AdditiveExpressionContext aec = (AdditiveExpressionContext) ctx;
            /* singleExpression ('+' | '-') singleExpression  */
            Expression first = visitSingleExpression(aec.singleExpression(0));
            Expression second = visitSingleExpression(aec.singleExpression(1));
            if (aec.Plus() != null) {
                return new Addition(currentCFG, getLocation(ctx), first, second);
            } else {
                return new Subtraction(currentCFG, getLocation(ctx), first, second);
            }
        }

        if (ctx instanceof ArgumentsExpressionContext) {
            ArgumentsExpressionContext aec = (ArgumentsExpressionContext) ctx;
            List<Expression> arguments = null;
            arguments = visitArguments(aec.arguments());
            Expression singleExpression = visitSingleExpression(aec.singleExpression());
            // this is a method call.
            String methodName = singleExpression.toString();
            System.out.println(singleExpression.toString());
            return new UnresolvedCall(currentCFG, getLocation(ctx), CallType.STATIC, null, methodName, arguments.toArray(Expression[]::new));
        }

        if (ctx instanceof FunctionExpressionContext) {
            FunctionExpressionContext fec = (FunctionExpressionContext) ctx;
            visitFunctionExpression(fec);
        }

        if (ctx instanceof MemberDotExpressionContext) {
            MemberDotExpressionContext mdec = (MemberDotExpressionContext) ctx;
            visitMemberDotExpression(mdec);
        }

        return null;
    }

    public Object visitMemberDotExpression(MemberDotExpressionContext ctx) {
        return null;
    }

    public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitFunctionBody(FunctionBodyContext ctx) {
        if (ctx.sourceElements() != null) {
            return visitSourceElements(ctx.sourceElements());
        }
        return null;
        
    }
    public Object visitAnonymousFunctionDecl(AnonymousFunctionDeclContext ctx) {
        String anonF = "$function@" + getLocation(ctx).toString();
        CFG parentCFG = this.currentCFG;

        this.currentCFG = new CFG(buildAnonymousCFGDescriptor(getLocation(ctx)));
        currentUnit.addCodeMember(currentCFG);
        visitFunctionBody(ctx.functionBody());
        Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> el = visitFunctionBody(ctx.functionBody());
        if (el != null && el.getLeft() != null) {
            currentCFG.getNodeList().mergeWith(el.getMiddle());
            currentCFG.getEntrypoints().add(el.getLeft());
        }
       
        addRetNodesToCurrentCFG();
        this.currentCFG = parentCFG;
        return null;
    }

    public Object visitArrowFunction(ArrowFunctionContext ctx) {
        return null;
    }
    public Object visitFunctionExpression(FunctionExpressionContext ctx) {
        if (ctx.anonymousFunction() != null) {
            AnonymousFunctionContext afc = ctx.anonymousFunction();
            if (afc instanceof AnonymousFunctionDeclContext) {
                AnonymousFunctionDeclContext afdc = (AnonymousFunctionDeclContext) afc;
                return visitAnonymousFunctionDecl(afdc);
            }
            if (afc instanceof ArrowFunctionContext) {
                ArrowFunctionContext arfc = (ArrowFunctionContext) afc;
                return visitArrowFunction(arfc);
            }
        }
        return null;
    }
    public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitExpressionSequence(ExpressionSequenceContext ctx) {
        NodeList<CFG, Statement, Edge> block = new NodeList<>(SEQUENTIAL_SINGLETON);

        Statement last = null, first = null;
        if (ctx.singleExpression() != null) {
            for (SingleExpressionContext sec : ctx.singleExpression()) {
                Statement st = visitSingleExpression(sec);     
                block.addNode(st);
                if (first == null)
                    first = st;
                if (last != null)
                    block.addEdge(new SequentialEdge(last, st));
                last = st;
            }
        }
        return Triple.of(first, block, last);
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
        //addNodeOnCFG(currentCFG, assignment);
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
