package it.jack.sasfra.cfg.expression;

import java.util.Collections;

import it.jack.sasfra.cfg.type.JSClassType;
import it.unive.lisa.program.Unit;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.statement.literal.Literal;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.TypeTokenType;

public class TypeLiteral extends Literal<Unit>{

    public TypeLiteral(CFG cfg, CodeLocation location, Unit value, Type staticType) {
        super(cfg, location, value, new TypeTokenType(Collections.singleton(JSClassType.lookup(value.getName()))));
    }
    
}
