package it.jack.sasfra.libraries;

import it.jack.sasfra.cfg.type.JSClassType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.StatementStore;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.lattices.ExpressionSet;
import it.unive.lisa.analysis.value.TypeDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.interprocedural.InterproceduralAnalysis;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.PluggableStatement;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Variable;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.TypeSystem;

public class Require extends it.unive.lisa.program.cfg.statement.UnaryExpression implements PluggableStatement {
    protected Statement st;
    @Override
    public void setOriginatingStatement(Statement st) {
        this.st = st;
    }
    protected Require(CFG cfg, CodeLocation location, String constructName,
                  Expression sequence) {
        super(cfg, location, constructName, sequence);
    }

    public static Require build(CFG cfg, CodeLocation location, Expression[] exprs) {
        return new Require(cfg, location, "require", exprs[0]);
    }

    @Override
    public String toString() {
        return "require";
    }

    @Override
    public <A extends AbstractState<A, H, V, T>, H extends HeapDomain<H>, V extends ValueDomain<V>, T extends TypeDomain<T>> AnalysisState<A, H, V, T> unarySemantics(
            InterproceduralAnalysis<A, H, V, T> interprocedural, AnalysisState<A, H, V, T> state,
            SymbolicExpression expr, StatementStore<A, H, V, T> expressions) throws SemanticException {
        // TypeSystem types = getProgram().getTypes();
        if (expr instanceof Constant) {
            System.out.println(((Constant)expr).getValue());
        }
        //JSClassType classType = JSClassType.lookup();
        return state;
    }
    
}
