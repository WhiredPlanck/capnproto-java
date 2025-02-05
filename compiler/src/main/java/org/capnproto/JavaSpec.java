package org.capnproto;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public abstract class JavaSpec<T> {
    final T value;

    protected JavaSpec(final T value) {
        this.value = value;
    }

    public static final class Type extends JavaSpec<TypeSpec> {
        public Type(TypeSpec spec) {
            super(spec);
        }
    }

    public static final class Method extends JavaSpec<MethodSpec> {
        public Method(MethodSpec spec) {
            super(spec);
        }
    }

    public static final class Field extends JavaSpec<FieldSpec> {
        public Field(FieldSpec spec) {
            super(spec);
        }
    }
}
