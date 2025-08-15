package hs.kr.entrydsm.domain.ast.factories

import hs.kr.entrydsm.domain.ast.entities.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * ASTNodeFactory의 실제 구현된 메서드들만 테스트하는 클래스입니다.
 */
class ASTNodeFactoryTest {

    private lateinit var factory: ASTNodeFactory

    @BeforeEach
    fun setUp() {
        factory = ASTNodeFactory()
    }

    @Test
    fun `createNumber가 올바르게 숫자 노드를 생성하는지 테스트`() {
        val node = factory.createNumber(42.0)
        assertEquals(42.0, node.value)
        assertTrue(node is NumberNode)
    }

    @Test
    fun `createBoolean이 올바르게 불리언 노드를 생성하는지 테스트`() {
        val trueNode = factory.createBoolean(true)
        assertTrue(trueNode.value)
        assertTrue(trueNode is BooleanNode)

        val falseNode = factory.createBoolean(false)
        assertEquals(false, falseNode.value)
    }

    @Test
    fun `createVariable이 올바르게 변수 노드를 생성하는지 테스트`() {
        val varNode = factory.createVariable("x")
        assertEquals("x", varNode.name)
        assertTrue(varNode is VariableNode)
    }

    @Test
    fun `createBinaryOp이 올바르게 이항 연산 노드를 생성하는지 테스트`() {
        val left = factory.createNumber(10.0)
        val right = factory.createNumber(5.0)

        val addNode = factory.createBinaryOp(left, "+", right)
        assertEquals("+", addNode.operator)
        assertEquals(left, addNode.left)
        assertEquals(right, addNode.right)
        assertTrue(addNode is BinaryOpNode)
    }

    @Test
    fun `createUnaryOp이 올바르게 단항 연산 노드를 생성하는지 테스트`() {
        val operand = factory.createNumber(42.0)
        
        val negNode = factory.createUnaryOp("-", operand)
        assertEquals("-", negNode.operator)
        assertEquals(operand, negNode.operand)
        assertTrue(negNode is UnaryOpNode)
    }

    @Test
    fun `createFunctionCall이 올바르게 함수 호출 노드를 생성하는지 테스트`() {
        val arg1 = factory.createNumber(1.0)
        val arg2 = factory.createNumber(2.0)
        
        val maxNode = factory.createFunctionCall("max", listOf(arg1, arg2))
        assertEquals("max", maxNode.name)
        assertEquals(2, maxNode.args.size)
        assertEquals(arg1, maxNode.args[0])
        assertEquals(arg2, maxNode.args[1])
        assertTrue(maxNode is FunctionCallNode)
    }

    @Test
    fun `createIf가 올바르게 조건문 노드를 생성하는지 테스트`() {
        val condition = factory.createBinaryOp(
            factory.createVariable("x"),
            ">",
            factory.createNumber(0.0)
        )
        val trueValue = factory.createNumber(1.0)
        val falseValue = factory.createNumber(-1.0)
        
        val ifNode = factory.createIf(condition, trueValue, falseValue)
        
        assertEquals(condition, ifNode.condition)
        assertEquals(trueValue, ifNode.trueValue)
        assertEquals(falseValue, ifNode.falseValue)
        assertTrue(ifNode is IfNode)
    }

    @Test
    fun `createArguments가 올바르게 인수 목록 노드를 생성하는지 테스트`() {
        val arg1 = factory.createNumber(1.0)
        val arg2 = factory.createVariable("x")
        val arg3 = factory.createBoolean(true)
        
        val argsNode = factory.createArguments(listOf(arg1, arg2, arg3))
        
        assertEquals(3, argsNode.arguments.size)
        assertEquals(arg1, argsNode.arguments[0])
        assertEquals(arg2, argsNode.arguments[1])
        assertEquals(arg3, argsNode.arguments[2])
        assertTrue(argsNode is ArgumentsNode)
    }

    @Test
    fun `복잡한 표현식 트리를 생성할 수 있는지 테스트`() {
        // (x + 2) * (y - 1) 표현식 생성
        val x = factory.createVariable("x")
        val two = factory.createNumber(2.0)
        val y = factory.createVariable("y")
        val one = factory.createNumber(1.0)
        
        val leftExpr = factory.createBinaryOp(x, "+", two)
        val rightExpr = factory.createBinaryOp(y, "-", one)
        val result = factory.createBinaryOp(leftExpr, "*", rightExpr)
        
        assertEquals("*", result.operator)
        assertTrue(result.left is BinaryOpNode)
        assertTrue(result.right is BinaryOpNode)
        
        val leftBinary = result.left as BinaryOpNode
        assertEquals("+", leftBinary.operator)
        assertEquals("x", (leftBinary.left as VariableNode).name)
        assertEquals(2.0, (leftBinary.right as NumberNode).value)
        
        val rightBinary = result.right as BinaryOpNode
        assertEquals("-", rightBinary.operator)
        assertEquals("y", (rightBinary.left as VariableNode).name)
        assertEquals(1.0, (rightBinary.right as NumberNode).value)
    }

    @Test
    fun `중첩된 함수 호출을 생성할 수 있는지 테스트`() {
        // max(min(a, b), c) 같은 중첩된 함수 호출
        val a = factory.createVariable("a")
        val b = factory.createVariable("b")
        val c = factory.createVariable("c")
        
        val minCall = factory.createFunctionCall("min", listOf(a, b))
        val maxCall = factory.createFunctionCall("max", listOf(minCall, c))
        
        assertEquals("max", maxCall.name)
        assertEquals(2, maxCall.args.size)
        assertTrue(maxCall.args[0] is FunctionCallNode)
        
        val innerCall = maxCall.args[0] as FunctionCallNode
        assertEquals("min", innerCall.name)
        assertEquals(2, innerCall.args.size)
        assertEquals("a", (innerCall.args[0] as VariableNode).name)
        assertEquals("b", (innerCall.args[1] as VariableNode).name)
        
        assertEquals("c", (maxCall.args[1] as VariableNode).name)
    }

    @Test
    fun `논리 연산자를 사용한 표현식을 생성할 수 있는지 테스트`() {
        // (x > 0) && (y < 10) 표현식 생성
        val x = factory.createVariable("x")
        val zero = factory.createNumber(0.0)
        val y = factory.createVariable("y")
        val ten = factory.createNumber(10.0)
        
        val leftCondition = factory.createBinaryOp(x, ">", zero)
        val rightCondition = factory.createBinaryOp(y, "<", ten)
        val andExpression = factory.createBinaryOp(leftCondition, "&&", rightCondition)
        
        assertEquals("&&", andExpression.operator)
        assertTrue(andExpression.left is BinaryOpNode)
        assertTrue(andExpression.right is BinaryOpNode)
        
        val leftBinary = andExpression.left as BinaryOpNode
        assertEquals(">", leftBinary.operator)
        assertEquals("x", (leftBinary.left as VariableNode).name)
        assertEquals(0.0, (leftBinary.right as NumberNode).value)
        
        val rightBinary = andExpression.right as BinaryOpNode
        assertEquals("<", rightBinary.operator)
        assertEquals("y", (rightBinary.left as VariableNode).name)
        assertEquals(10.0, (rightBinary.right as NumberNode).value)
    }

    @Test
    fun `삼항 연산자(조건문)를 사용한 표현식을 생성할 수 있는지 테스트`() {
        // x > 0 ? x : 0 표현식 생성
        val x = factory.createVariable("x")
        val zero = factory.createNumber(0.0)
        val condition = factory.createBinaryOp(x, ">", zero)
        val trueValue = factory.createVariable("x")
        val falseValue = factory.createNumber(0.0)
        
        val ternary = factory.createIf(condition, trueValue, falseValue)
        
        assertTrue(ternary.condition is BinaryOpNode)
        assertTrue(ternary.trueValue is VariableNode)
        assertTrue(ternary.falseValue is NumberNode)
        
        val conditionBinary = ternary.condition as BinaryOpNode
        assertEquals(">", conditionBinary.operator)
        assertEquals("x", (conditionBinary.left as VariableNode).name)
        assertEquals(0.0, (conditionBinary.right as NumberNode).value)
        
        assertEquals("x", (ternary.trueValue as VariableNode).name)
        assertEquals(0.0, (ternary.falseValue as NumberNode).value)
    }

    @Test
    fun `팩토리가 올바르게 인스턴스화되는지 테스트`() {
        assertNotNull(factory)
    }
}