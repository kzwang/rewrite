/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.cleanup;

import org.openrewrite.Cursor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.UnwrapParentheses;
import org.openrewrite.java.style.UnnecessaryParenthesesStyle;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;

public class UnnecessaryParenthesesVisitor<P> extends JavaVisitor<P> {
    private final UnnecessaryParenthesesStyle style;

    private static final String UNNECESSARY_PARENTHESES_MARKER = "unnecessaryParenthesesUnwrapTarget";

    public UnnecessaryParenthesesVisitor(UnnecessaryParenthesesStyle style) {
        this.style = style;
    }

    @Override
    public <T extends J> J visitParentheses(J.Parentheses<T> parens, P p) {
        J par = super.visitParentheses(parens, p);
        Cursor c = getCursor().pollNearestMessage(UNNECESSARY_PARENTHESES_MARKER);
        if (c != null && (c.getValue() instanceof J.Literal || c.getValue() instanceof J.Identifier)) {
            par = new UnwrapParentheses<>((J.Parentheses<?>) par).visit(par, p, getCursor().getParentOrThrow());
        }

        assert par != null;
        if (par instanceof J.Parentheses) {
            if (getCursor().getParentTreeCursor().getValue() instanceof J.Parentheses) {
                return ((J.Parentheses<?>) par).getTree().withPrefix(Space.EMPTY);
            }
        }

        return par;
    }

    @Override
    public J visitIdentifier(J.Identifier ident, P p) {
        J.Identifier i = visitAndCast(ident, p, super::visitIdentifier);
        if (style.getIdent() && getCursor().getParentTreeCursor().getValue() instanceof J.Parentheses) {
            getCursor().putMessageOnFirstEnclosing(J.Parentheses.class, UNNECESSARY_PARENTHESES_MARKER, getCursor());
        }
        return i;
    }

    @Override
    public J visitLiteral(J.Literal literal, P p) {
        J.Literal l = visitAndCast(literal, p, super::visitLiteral);
        JavaType.Primitive type = l.getType();
        if ((style.getNumInt() && type == JavaType.Primitive.Int) ||
                (style.getNumDouble() && type == JavaType.Primitive.Double) ||
                (style.getNumLong() && type == JavaType.Primitive.Long) ||
                (style.getNumFloat() && type == JavaType.Primitive.Float) ||
                (style.getStringLiteral() && type == JavaType.Primitive.String) ||
                (style.getLiteralNull() && type == JavaType.Primitive.Null) ||
                (style.getLiteralFalse() && type == JavaType.Primitive.Boolean && l.getValue() == Boolean.valueOf(false)) ||
                (style.getLiteralTrue() && type == JavaType.Primitive.Boolean && l.getValue() == Boolean.valueOf(true))) {
            if (getCursor().getParentTreeCursor().getValue() instanceof J.Parentheses) {
                getCursor().putMessageOnFirstEnclosing(J.Parentheses.class, UNNECESSARY_PARENTHESES_MARKER, getCursor());
            }
        }
        return l;
    }

    @Override
    public J visitAssignmentOperation(J.AssignmentOperation assignOp, P p) {
        J.AssignmentOperation a = visitAndCast(assignOp, p, super::visitAssignmentOperation);
        J.AssignmentOperation.Type op = a.getOperator();
        if (a.getAssignment() instanceof J.Parentheses && ((style.getBitAndAssign() && op == J.AssignmentOperation.Type.BitAnd) ||
                (style.getBitOrAssign() && op == J.AssignmentOperation.Type.BitOr) ||
                (style.getBitShiftRightAssign() && op == J.AssignmentOperation.Type.UnsignedRightShift) ||
                (style.getBitXorAssign() && op == J.AssignmentOperation.Type.BitXor) ||
                (style.getShiftRightAssign() && op == J.AssignmentOperation.Type.RightShift) ||
                (style.getShiftLeftAssign() && op == J.AssignmentOperation.Type.LeftShift) ||
                (style.getMinusAssign() && op == J.AssignmentOperation.Type.Subtraction) ||
                (style.getDivAssign() && op == J.AssignmentOperation.Type.Division) ||
                (style.getPlusAssign() && op == J.AssignmentOperation.Type.Addition) ||
                (style.getStarAssign() && op == J.AssignmentOperation.Type.Multiplication) ||
                (style.getModAssign() && op == J.AssignmentOperation.Type.Modulo))) {
            a = (J.AssignmentOperation) new UnwrapParentheses<>((J.Parentheses<?>) a.getAssignment()).visitNonNull(a, p, getCursor().getParentOrThrow());
        }
        return a;
    }

    @Override
    public J visitAssignment(J.Assignment assignment, P p) {
        J.Assignment a = visitAndCast(assignment, p, super::visitAssignment);
        if (style.getAssign() && a.getAssignment() instanceof J.Parentheses) {
            a = (J.Assignment) new UnwrapParentheses<>((J.Parentheses<?>) a.getAssignment()).visitNonNull(a, p, getCursor().getParentOrThrow());
        }
        return a;
    }

    @Override
    public J visitReturn(J.Return retrn, P p) {
        J.Return rtn = visitAndCast(retrn, p, super::visitReturn);
        if (style.getExpr() && rtn.getExpression() instanceof J.Parentheses) {
            rtn = (J.Return) new UnwrapParentheses<>((J.Parentheses<?>) rtn.getExpression()).visitNonNull(rtn, p, getCursor().getParentOrThrow());
        }
        return rtn;
    }

    @Override
    public J visitVariable(J.VariableDeclarations.NamedVariable variable, P p) {
        J.VariableDeclarations.NamedVariable v = visitAndCast(variable, p, super::visitVariable);
        if (style.getAssign() && v.getInitializer() != null && v.getInitializer() instanceof J.Parentheses) {
            v = (J.VariableDeclarations.NamedVariable) new UnwrapParentheses<>((J.Parentheses<?>) v.getInitializer()).visitNonNull(v, p, getCursor().getParentOrThrow());
        }
        return v;
    }

    @Override
    public J visitLambda(J.Lambda lambda, P p) {
        J.Lambda l = visitAndCast(lambda, p, super::visitLambda);
        if (l.getParameters().getParameters().size() == 1 &&
                l.getParameters().isParenthesized() &&
                l.getParameters().getParameters().get(0) instanceof J.VariableDeclarations &&
                ((J.VariableDeclarations) l.getParameters().getParameters().get(0)).getTypeExpression() == null) {
            l = l.withParameters(l.getParameters().withParenthesized(false));
        }
        return l;
    }

    @Override
    public J visitIf(J.If iff, P p) {
        J.If i = visitAndCast(iff, p, super::visitIf);
        // Unwrap when if condition is a single parenthesized expression
        Expression expression = i.getIfCondition().getTree();
        if (expression instanceof J.Parentheses) {
            i = (J.If) new UnwrapParentheses<>((J.Parentheses<?>) expression).visitNonNull(i, p, getCursor().getParentOrThrow());
        }
        return i;
    }

    @Override
    public J visitWhileLoop(J.WhileLoop whileLoop, P p) {
        J.WhileLoop w = visitAndCast(whileLoop, p, super::visitWhileLoop);
        // Unwrap when while condition is a single parenthesized expression
        Expression expression = w.getCondition().getTree();
        if (expression instanceof J.Parentheses) {
            w = (J.WhileLoop) new UnwrapParentheses<>((J.Parentheses<?>) expression).visitNonNull(w, p, getCursor().getParentOrThrow());
        }
        return w;
    }

    @Override
    public J visitDoWhileLoop(J.DoWhileLoop doWhileLoop, P p) {
        J.DoWhileLoop dw = visitAndCast(doWhileLoop, p, super::visitDoWhileLoop);
        // Unwrap when while condition is a single parenthesized expression
        Expression expression = dw.getWhileCondition().getTree();
        if (expression instanceof J.Parentheses) {
            dw = (J.DoWhileLoop) new UnwrapParentheses<>((J.Parentheses<?>) expression).visitNonNull(dw, p, getCursor().getParentOrThrow());
        }
        return dw;
    }

    @Override
    public J visitForControl(J.ForLoop.Control control, P p) {
        J.ForLoop.Control fc = visitAndCast(control, p, super::visitForControl);
        Expression condition = fc.getCondition();
        if (condition instanceof J.Parentheses) {
            fc = (J.ForLoop.Control) new UnwrapParentheses<>((J.Parentheses<?>) condition).visitNonNull(fc, p, getCursor().getParentOrThrow());
        }
        return fc;
    }
}
