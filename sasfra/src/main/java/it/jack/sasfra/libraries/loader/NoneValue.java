package it.jack.sasfra.libraries.loader;

import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Expression;
import it.jack.sasfra.cfg.expression.NoneLiteral;

public class NoneValue implements Value {

	@Override
	public String toString() {
		return "NoneValue";
	}

	@Override
	public Expression toLiSAExpression(CFG init) {
		return new NoneLiteral(init, init.getDescriptor().getLocation());
	}
}
