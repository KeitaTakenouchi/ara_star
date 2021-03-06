import java.util.*
import kotlin.math.abs

fun main() {
    val area = Area(Node(0, 0), Node(13, 19)).apply {
        print()
    }
    Search(area).execute()
}

class Search(val area: Area) {
    fun execute() {
        val weight = 1.0
        val costs = weightedAsterCost(weight)
        if (costs == null) {
            println("unreachable")
            return
        }
        area.print(resolvePath(costs))
        area.printCostAsNumpy(costs)
    }

    private fun resolvePath(costs: Map<Node, Int>): Path {
        val path = Path().apply { addNode(area.goal) }
        var target = area.goal
        while (target != area.start) {
            val next = area.neighbors(target)
                .filter { costs.containsKey(it) }
                .minByOrNull { costs[it]!! }!!
            target = next
            path.addNode(target)
        }
        return path
    }

    private fun weightedAsterCost(weight: Double): Map<Node, Int>? {
        val h = { n: Node -> weight * n.dist(area.goal) }

        val costs = mutableMapOf<Node, Int>().apply { this[area.start] = 0 }
        val queue = PriorityQueue<NodeP>().apply { add(NodeP(area.start, h(area.start))) }
        while (queue.isNotEmpty()) {
            val target = queue.poll().node
            if (target == area.goal) return costs
            for (n in area.neighbors(target)) {
                val cost = costs[target]!! + target.dist(n)
                if (costs[n] == null || cost < costs[n]!!) {
                    costs[n] = cost
                    queue.add(NodeP(n, costs[n]!! + h(n)))
                }
            }
        }
        return null // the goal is unreachable
    }
}

class NodeP(val node: Node, private val cost: Double) : Comparable<NodeP> {
    override fun compareTo(other: NodeP): Int {
        return when {
            cost == other.cost -> 0
            cost < other.cost -> -1
            cost > other.cost -> 1
            else -> throw  IllegalStateException("$cost <-> ${other.cost}")
        }
    }
}

class Path {
    val nodes = mutableListOf<Node>()
    fun addNode(node: Node) {
        nodes.add(node)
    }
}

class Area(val start: Node, val goal: Node) {
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

    fun isValid(n: Node): Boolean {
        return (n.i in states.indices) && (n.j in states[n.i].indices)
    }

    fun state(n: Node): Int {
        check(isValid(n)) { "out of range $n" }
        return states[n.i][n.j]
    }

    fun isEmpty(n: Node): Boolean {
        return state(n) == 0
    }

    fun neighbors(n: Node): List<Node> {
        return n.around().filter { isValid(it) && isEmpty(it) }
    }

    fun print(path: Path = Path()) {
        val sb = StringBuilder()
        sb.append("\n")

        for (i in states.indices) {
            for (j in states[i].indices) {
                val s = states[i][j]
                val c = when {
                    Node(i, j) == start -> " S "
                    Node(i, j) == goal -> " G "
                    path.nodes.contains(Node(i, j)) -> " @ "
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

    fun costMatrix(costs: Map<Node, Int>): Array<Array<Int>> {
        return states.mapIndexed { i, row ->
            row.mapIndexed { j, _ ->
                costs.getOrDefault(Node(i, j), -30)
            }.toTypedArray()
        }.toTypedArray()
    }

    fun printCostAsNumpy(costs: Map<Node, Int>) {
        val sb = StringBuilder()
        sb.append("\n")

        val costMatrix = costMatrix(costs)
        val m = costMatrix.joinToString(",\n") { row ->
            val r = row.joinToString(",") { cost ->
                cost.toString().padStart(3, ' ')
            }
            "\t[$r]"
        }
        print("data = np.array([\n$m]\n)")
    }
}

data class Node(val i: Int, val j: Int) {
    fun around(): Array<Node> {
        return arrayOf(up(), left(), down(), right())
    }

    // Manhattan distance
    fun dist(o: Node): Int {
        return abs(i - o.i) + abs(j - o.j)
    }

    fun up(): Node {
        return Node(i - 1, j)
    }

    fun down(): Node {
        return Node(i + 1, j)
    }

    fun left(): Node {
        return Node(i, j - 1)
    }

    fun right(): Node {
        return Node(i, j + 1)
    }

    override fun toString(): String {
        return "($i, $j)"
    }
}