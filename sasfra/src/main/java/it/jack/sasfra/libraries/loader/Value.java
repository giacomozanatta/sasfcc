package it.jack.sasfra.libraries.loader;

import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Expression;

public interface Value {

	Expression toLiSAExpression(CFG init);
}