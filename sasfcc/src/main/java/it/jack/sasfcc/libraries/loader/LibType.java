package it.jack.sasfcc.libraries.loader;

import it.jack.sasfcc.cfg.type.JSClassType;

import java.util.Objects;

public class LibType implements Type {
	private final String name;
	private final boolean pointer;

	public LibType(String name, boolean pointer) {
		this.name = name;
		this.pointer = pointer;
	}

	public String getName() {
		return name;
	}

	public boolean isPointer() {
		return pointer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, pointer);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LibType other = (LibType) obj;
		return Objects.equals(name, other.name) && pointer == other.pointer;
	}

	@Override
	public String toString() {
		return "LibType [name=" + name + ", pointer=" + pointer + "]";
	}

	@Override
	public it.unive.lisa.type.Type toLiSAType() {
		it.unive.lisa.type.Type t = JSClassType.lookup(this.name);
		if (this.pointer)
			t = ((JSClassType) t).getReference();
		return t;
	}
}
