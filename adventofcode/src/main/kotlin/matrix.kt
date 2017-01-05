public interface Matrix<out T> {
    val cols: Int
    val rows: Int
    operator fun get(col: Int, row: Int): T
}

public fun <T> Matrix<T>.traverseRows(f: (col: Int, row: Int, value: T) -> Unit) {
    for (y in 0..rows - 1) {
        for (x in 0..cols - 1) {
            f(x, y, get(x, y))
        }
    }
}

public interface MutableMatrix<T> : Matrix<T> {
    operator fun set(col: Int, row: Int, value: T)
}

public class MutableMatrixImpl<T : Any> (
    override val cols: Int,
    override val rows: Int,
    initialCellValues: (Int, Int) -> T
) : MutableMatrix<T> {

    private val cells = Array<Any>(cols * rows) {
        i ->
        val x = i % cols
        val y = i / cols
        initialCellValues(x, y)
    }

    override fun get(col: Int, row: Int): T {
        return cells[toIndex(col, row)] as T
    }

    override fun set(col: Int, row: Int, value: T) {
        cells[toIndex(col, row)] = value
    }

    private fun toIndex(x: Int, y: Int): Int {
        check(x, y)
        return y * cols + x
    }

    private fun check(x: Int, y: Int) {
        if (x !in 0..cols - 1) {
            throw IndexOutOfBoundsException("x = $x is out of range [0, $cols)")
        }
        if (y !in 0..rows - 1) {
            throw IndexOutOfBoundsException("y = $y is out of range [0, $rows)")
        }
    }
}

public fun <T : Any> MutableMatrix(cols: Int, rows: Int, f: (col: Int, row: Int) -> T): MutableMatrix<T> = MutableMatrixImpl(cols, rows, f)
public fun <T : Any> Matrix(cols: Int, rows: Int, f: (col: Int, row: Int) -> T): Matrix<T> = MutableMatrixImpl(cols, rows, f)


public fun <T> MutableMatrix<T>.fill(f: (col: Int, row: Int, value: T) -> T) {
    for (y in 0..rows - 1) {
        for (x in 0..cols - 1) {
            set(x, y, f(x, y, get(x, y)))
        }
    }
}

public fun <T> MutableMatrix<T>.copyFrom(m: Matrix<T>) {
    for (y in 0..rows - 1) {
        for (x in 0..cols - 1) {
            set(x, y, m[x, y])
        }
    }
}

public fun <T : Any> Matrix<T>.toMutableMatrix(): MutableMatrix<T> = MutableMatrixImpl(cols, rows) { x, y -> get(x, y)}