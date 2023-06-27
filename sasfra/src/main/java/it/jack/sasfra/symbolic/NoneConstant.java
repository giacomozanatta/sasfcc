package it.jack.sasfra.symbolic;

import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.type.NullType;

public class NoneConstant extends Constant {

	private static final Object NULL_CONST = new Object();

	public NoneConstant(CodeLocation location) {
		super(NullType.INSTANCE, NULL_CONST, location);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "none";
	}
}
