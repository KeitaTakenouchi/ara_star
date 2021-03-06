import java.util.*
import kotlin.math.abs

fun main() {
    val grid = Grid(P(0, 0), P(13, 19)).apply {
        print()
    }
    Search(grid).execute(weight = 2.0)
}

class Search(val grid: Grid) {
    fun execute(weight: Double) {
        val costs = weightedAsterCost(weight)
        if (costs == null) {
            println("unreachable")
            return
        }
        val path = resolvePath(costs)
        grid.print(path)
        grid.printPathAsNumpy(path)
        grid.printCostAsNumpy(costs)
    }

    private fun resolvePath(costs: Map<P, Int>): Path {
        val path = Path()
        var target = grid.goal
        while (target != grid.start) {
            path.addPoint(target)
            target = grid.neighbors(target)
                .minByOrNull { costs.getOrDefault(it, Int.MAX_VALUE) }!!
        }
        return path
    }

    private fun weightedAsterCost(weight: Double): Map<P, Int>? {
        // h: heuristic function
        val h = { n: P -> weight * n.distance(grid.goal) }

        val costs = mutableMapOf<P, Int>().apply { this[grid.start] = 0 }
        val queue = PriorityQueue<PC>().apply { add(PC(grid.start, h(grid.start))) }
        while (queue.isNotEmpty()) {
            val p = queue.poll().point
            if (p == grid.goal) return costs
            grid.neighbors(p).forEach { n ->
                val cost = costs[p]!! + p.distance(n)
                if (costs[n] == null || cost < costs[n]!!) {
                    costs[n] = cost
                    queue.add(PC(n, cost + h(n)))
                }
            }
        }
        return null // the goal is unreachable
    }
}

class PC(val point: P, private val cost: Double) : Comparable<PC> {
    override fun compareTo(other: PC): Int {
        return when {
            cost == other.cost -> 0
            cost < other.cost -> -1
            cost > other.cost -> 1
            else -> throw  IllegalStateException("$cost <-> ${other.cost}")
        }
    }
}

class Path {
    val points = mutableListOf<P>()
    fun addPoint(point: P) {
        points.add(point)
    }
}

class Grid(val start: P, val goal: P) {
    val states = arrayOf(
        arrayOf(0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0),
        arrayOf(0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0),
        arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0),
        arrayOf(0, 1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0),
        arrayOf(0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0),
        arrayOf(1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0),
        arrayOf(0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0),
        arrayOf(0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0),
        arrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0),
        arrayOf(1, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
        arrayOf(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0),
        arrayOf(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0),
        arrayOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0),
        arrayOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    )

    fun isValid(n: P): Boolean {
        return (n.i in states.indices) && (n.j in states[n.i].indices)
    }

    fun state(n: P): Int {
        check(isValid(n)) { "out of range $n" }
        return states[n.i][n.j]
    }

    fun isEmpty(n: P): Boolean {
        return state(n) == 0
    }

    fun neighbors(n: P): List<P> {
        return n.around().filter { isValid(it) && isEmpty(it) }
    }

    fun costMatrix(costs: Map<P, Int>): Array<Array<Int>> {
        return states.mapIndexed { i, row ->
            row.mapIndexed { j, _ ->
                costs.getOrDefault(P(i, j), -30)
            }.toTypedArray()
        }.toTypedArray()
    }

    fun print(path: Path = Path()) {
        val sb = StringBuilder()
        sb.append("\n")

        for (i in states.indices) {
            for (j in states[i].indices) {
                val s = states[i][j]
                val c = when {
                    P(i, j) == start -> " S "
                    P(i, j) == goal -> " G "
                    path.points.contains(P(i, j)) -> " @ "
                    s == 0 -> "   "
                    s == 1 -> " * "
                    else -> throw IllegalStateException("unknown state $s")
                }

                sb.append(c)
            }
            sb.append("\n")
        }
        print(sb.toString())
    }

    fun printPathAsNumpy(path: Path) {
        var i = 0
        val m = states.joinToString(",\n") {
            var j = 0
            val r = states[i].joinToString(",") {
                val s = states[i][j]
                val str = when {
                    P(i, j) == start -> "  0"
                    P(i, j) == goal -> "  0"
                    path.points.contains(P(i, j)) -> " 30"
                    s == 0 -> "-30"
                    s == 1 -> " 60"
                    else -> throw IllegalStateException("unknown state $s")
                }
                j++
                str
            }
            i++
            "\t[$r]"
        }
        println("data = np.array([\n$m]\n)")
    }

    fun printCostAsNumpy(costs: Map<P, Int>) {
        val costMatrix = costMatrix(costs)
        var i = 0
        val m = states.joinToString(",\n") {
            var j = 0
            val r = states[i].joinToString(",") {
                val str = when {
                    states[i][j] == 1 -> "60"
                    else -> costMatrix[i][j].toString().padStart(3, ' ')
                }
                j++
                str
            }
            i++
            "\t[$r]"
        }
        println("data = np.array([\n$m]\n)")
    }
}

// Point class
data class P(val i: Int, val j: Int) {
    fun around(): Array<P> {
        return arrayOf(up(), left(), down(), right())
    }

    // Manhattan distance
    fun distance(o: P): Int {
        return abs(i - o.i) + abs(j - o.j)
    }

    fun up(): P {
        return P(i - 1, j)
    }

    fun down(): P {
        return P(i + 1, j)
    }

    fun left(): P {
        return P(i, j - 1)
    }

    fun right(): P {
        return P(i, j + 1)
    }

    override fun toString(): String {
        return "($i, $j)"
    }
}