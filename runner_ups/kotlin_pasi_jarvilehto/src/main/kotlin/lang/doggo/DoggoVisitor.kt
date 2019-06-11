package lang.doggo

import lang.doggo.generated.DoggoBaseVisitor
import lang.doggo.generated.DoggoParser.*
import org.antlr.v4.runtime.tree.TerminalNode

class DoggoVisitor : DoggoBaseVisitor<Long>() {
    private val variables = mutableMapOf<String, Long>()

    override fun visitProgram(ctx: ProgramContext): Long {
        ctx.statements()?.let(::visit)
        return ctx.result().let(::visit)
    }

    override fun visitAssignment(ctx: AssignmentContext) = void {
        variables[ctx.ID().text] = ctx.expr().let(::visit)
    }

    override fun visitIfelse(ctx: IfelseContext) = void {
        if (ctx.expr().let(::visit).toBoolean)  visitStatements(ctx.ifstmts)
        else if (ctx.elstmts != null)           visitStatements(ctx.elstmts)
    }

    override fun visitLoop(ctx: LoopContext) = void {
        while (ctx.expr().let(::visit).toBoolean) visitStatements(ctx.statements())
    }

    override fun visitEval_expr(ctx: Eval_exprContext): Long = ctx.ID().text.let { id ->
        variables[id] ?: error("No value found for id: $id")
    }

    override fun visitInfix_expr(ctx: Infix_exprContext): Long {
        val left = ctx.left.let(::visit)
        val right = ctx.right.let(::visit)
        return when (ctx.infix_op().symbolType) {
            OP_ADD -> left + right
            OP_SUB -> left - right
            OP_MUL -> left * right
            OP_GT  -> (left > right).toLong
            OP_LT  -> (left < right).toLong
            else   -> error("Unsupported infix operation ${ctx.infix_op().text}")
        }
    }

    override fun visitLiteral_expr(ctx: Literal_exprContext): Long = ctx.literal().NUMBER().text.toLong()
}

private const val VOID_RESULT = 0L
private const val TRUE_VALUE = 1L
private const val FALSE_VALUE = -1L

private inline fun void(block: () -> Unit): Long = block.invoke().let { VOID_RESULT }

private inline val Long.toBoolean get() = this == TRUE_VALUE

private inline val Boolean.toLong get() = if (this) TRUE_VALUE else FALSE_VALUE

private inline val Infix_opContext.symbolType get() = getChild(TerminalNode::class.java, 0).symbol.type
