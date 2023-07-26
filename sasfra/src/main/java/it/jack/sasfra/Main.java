package it.jack.sasfra;

import it.jack.sasfra.analysis.ConstantPropagation;
import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.heap.pointbased.FieldSensitivePointBasedHeap;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.interprocedural.ReturnTopPolicy;
import it.unive.lisa.interprocedural.callgraph.RTACallGraph;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.program.Program;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, AnalysisException {
        JSFrontend frontend = new JSFrontend("test/input/controller.js");
        Program p = frontend.toLiSAProgram();
        LiSAConfiguration conf = new LiSAConfiguration();
        conf.workdir = "test-javascript";
        conf.serializeResults = true;
        conf.jsonOutput = true;
        conf.analysisGraphs = LiSAConfiguration.GraphType.HTML_WITH_SUBNODES;
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.callGraph = new RTACallGraph();
        conf.openCallPolicy = ReturnTopPolicy.INSTANCE;
        conf.optimize = false;
        //conf.openCallPolicy
        FieldSensitivePointBasedHeap heap = new FieldSensitivePointBasedHeap();
        TypeEnvironment<InferredTypes> type = new TypeEnvironment<>(new InferredTypes());
        //conf.interproceduralAnalysis = new ContextBasedAnalysis();
        ValueEnvironment<ConstantPropagation> domain = new ValueEnvironment<>(new ConstantPropagation());
         conf.abstractState = new SimpleAbstractState<>(heap, domain, type);
        LiSA lisa = new LiSA(conf);
        lisa.run(p);
        return;
    }
}