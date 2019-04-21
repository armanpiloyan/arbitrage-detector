import scala.collection.mutable.ListBuffer

class Graph(v: Int, e: Int) {

  class Edge() {
    var src: Int = 0
    var dest: Int = 0
    var weight: Int = 0
  }

  var V: Int = v
  var E: Int = e
  val Edge = new ListBuffer[Edge]

  for (_ <- 1 to e)
    Edge += new Edge()


  def BellmanFord(graph: Graph, src: Int): Unit = {
    val E: Int = graph.E
    val V: Int = graph.V
    val dist = new ListBuffer[Int]
    val parent = new ListBuffer[Int]

    //Initialize distances
    for (_ <- 0 until V) {
      dist += Int.MaxValue
      parent += -1
    }

    dist(src) = 0

    //Relaxation
    for (_ <- 1 until V) {
      for (j <- 0 until E) {
        val u = graph.Edge(j).src
        val v = graph.Edge(j).dest
        val weight = graph.Edge(j).weight

        if (dist(u) != Int.MaxValue &&
          dist(u) + weight < dist(v)) {
          dist(v) = dist(u) + weight
          parent(v) = u
        }
      }
    }

    var U: Int = -1

    //Negative cycle detection
    for (j <- 0 until E) {
      val u = graph.Edge(j).src
      val v = graph.Edge(j).dest
      val weight = graph.Edge(j).weight
      if (dist(u) != Int.MaxValue &&
        dist(u) + weight < dist(v)) {
        U = u
      }

    }

    if (U != -1) {
      println("Negative cycle detected!")
      printSolution(parent, U)
    }

    printDist(dist, V)


  }


  def printSolution(parent: ListBuffer[Int], start: Int): Unit = {
    val cycle = new ListBuffer[Int]
    var v = parent(start)
    cycle += start
    cycle += v
    while (v != start) {
      v = parent(v)
      cycle += v
    }
    println(cycle.reverse)
    println()
  }

  def printDist(dist: ListBuffer[Int], V: Int): Unit = {
    for (i <- 0 until V)
      println(i + "\t\t" + dist(i))
  }

}







