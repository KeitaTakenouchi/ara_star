import java.util.*

fun main() {

    val area = Area(Node(0, 0), Node(0, 13))
    area.print()

    DijkstraSearch(area).execute()
}

class DijkstraSearch(val area: Area) {
    fun execute() {
        val costs = calcCost()
        if (costs == null) {
            println("unreachable")
            return
        }
        val path = resolvePath(costs)
        area.addPath(path)
        area.print()
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

    private fun calcCost(): Map<Node, Int>? {
        val node2cost = mutableMapOf<Node, Int>()
            .apply { this[area.start] = 0 }

        val worklist = PriorityQueue<NodeP>()
            .apply { add(NodeP(0, area.start)) }
        while (worklist.isNotEmpty()) {
            val target = worklist.poll()
            for (neighbor in area.neighbors(target.node)) {
                if (!node2cost.containsKey(neighbor)) {
                    val cost = target.cost + 1
                    node2cost[neighbor] = cost

                    if (neighbor == area.goal)
                        return node2cost // finish search

                    val next = NodeP(cost, neighbor)
                    worklist.add(next)
                }
            }
        }
        // the goal is unreachable
        return null
    }

    class NodeP(val cost: Int, val node: Node) : Comparable<NodeP> {
        override fun compareTo(other: NodeP): Int {
            return cost - other.cost
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
        arrayOf(0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0),
        arrayOf(0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0),
        arrayOf(0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0),
        arrayOf(0, 1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0),
        arrayOf(0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0),
        arrayOf(0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0),
        arrayOf(0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0),
        arrayOf(0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0),
        arrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0),
        arrayOf(1, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0),
        arrayOf(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0),
        arrayOf(0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0),
        arrayOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0),
        arrayOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0),
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

    fun addPath(path: Path) {
        path.nodes.forEach {
            states[it.i][it.j] = 2
        }
    }

    fun print() {
        val sb = StringBuilder()
        sb.append("\n")

        states.forEach { row ->
            row.forEach { s ->
                val c = when (s) {
                    0 -> " ♢ "
                    1 -> " ⚠ "
                    2 -> " ☗ " // path
                    else -> throw IllegalStateException("unknown state $s")
                }
                sb.append(c)
            }
            sb.append("\n")
        }
        print(sb.toString())
    }
}

data class Node(val i: Int, val j: Int) {
    fun around(): Array<Node> {
        return arrayOf(
            up(), left(), down(), right()
        )
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