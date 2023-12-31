package it.jack.sasfcc;

import it.unive.lisa.program.language.LanguageFeatures;
import it.unive.lisa.program.language.hierarchytraversal.HierarcyTraversalStrategy;
import it.unive.lisa.program.language.hierarchytraversal.SingleInheritanceTraversalStrategy;
import it.unive.lisa.program.language.parameterassignment.ParameterAssigningStrategy;
import it.unive.lisa.program.language.parameterassignment.PythonLikeAssigningStrategy;
import it.unive.lisa.program.language.resolution.ParameterMatchingStrategy;
import it.unive.lisa.program.language.resolution.PythonLikeMatchingStrategy;
import it.unive.lisa.program.language.resolution.RuntimeTypesMatchingStrategy;
import it.unive.lisa.program.language.validation.BaseValidationLogic;
import it.unive.lisa.program.language.validation.ProgramValidationLogic;

public class JavascriptFeatures extends LanguageFeatures {
    @Override
    public ParameterMatchingStrategy getMatchingStrategy() {
        return new PythonLikeMatchingStrategy(RuntimeTypesMatchingStrategy.INSTANCE);
    }
    
    @Override
    public HierarcyTraversalStrategy getTraversalStrategy() {
        // TODO this is not right, but its fine for now
        return SingleInheritanceTraversalStrategy.INSTANCE;
    }
    
    @Override
    public ParameterAssigningStrategy getAssigningStrategy() {
        return PythonLikeAssigningStrategy.INSTANCE;
    }
    
    @Override
    public ProgramValidationLogic getProgramValidationLogic() {
        return new BaseValidationLogic();
    }
}