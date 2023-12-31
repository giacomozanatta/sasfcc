package it.jack.sasfcc.libraries.loader;

import it.unive.lisa.program.annotations.Annotations;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;

import java.util.Objects;

public class Parameter {
	private final String name;
	private final Type type;
	private final Value value;

	public Parameter(String name, Type type) {
		this(name, type, null);
	}

	public Parameter(String name, Type type, Value value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public Value getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		return Objects.equals(name, other.name) && Objects.equals(type, other.type)
				&& Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "Parameter [name=" + name + ", type=" + type + ", value=" + value + "]";
	}

	public it.unive.lisa.program.cfg.Parameter toLiSAParameter(CodeLocation location, CFG init) {
		if (this.value == null)
			return new it.unive.lisa.program.cfg.Parameter(location, this.name, this.type.toLiSAType());
		return new it.unive.lisa.program.cfg.Parameter(
				location,
				this.name,
				this.type.toLiSAType(),
				this.value.toLiSAExpression(init),
				new Annotations());
	}
}
