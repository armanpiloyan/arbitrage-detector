import scala.collection.mutable.ListBuffer

object Driver {
  def main(args: Array[String]): Unit = {


    val V = 5
    val E = 8
    val graph = new Graph(V, E)

    graph.Edge.head.src = 0
    graph.Edge.head.dest = 1
    graph.Edge.head.weight = -1

    graph.Edge(1).src = 0
    graph.Edge(1).dest = 2
    graph.Edge(1).weight = 4

    graph.Edge(2).src = 1
    graph.Edge(2).dest = 2
    graph.Edge(2).weight = 3

    graph.Edge(3).src = 1
    graph.Edge(3).dest = 3
    graph.Edge(3).weight = 2

    graph.Edge(4).src = 1
    graph.Edge(4).dest = 4
    graph.Edge(4).weight = 2

    graph.Edge(5).src = 3
    graph.Edge(5).dest = 2
    graph.Edge(5).weight = 5

    graph.Edge(6).src = 3
    graph.Edge(6).dest = 1
    graph.Edge(6).weight = 1

    graph.Edge(7).src = 4
    graph.Edge(7).dest = 3
    graph.Edge(7).weight = -3

    graph.BellmanFord(graph, 0)

    println("----------------------")

    val V1 = 3
    val E1 = 3
    val graph1 = new Graph(V1, E1)

    graph1.Edge.head.src = 0
    graph1.Edge.head.dest = 1
    graph1.Edge.head.weight = 1

    graph1.Edge(1).src = 1
    graph1.Edge(1).dest = 2
    graph1.Edge(1).weight = 1

    graph1.Edge(2).src = 2
    graph1.Edge(2).dest = 0
    graph1.Edge(2).weight = -3

    graph1.BellmanFord(graph1, 0)


    //Data scrapping
    val dataCollector = new DataCollector()
    val rates = dataCollector.getExchangeRates(
      "https://min-api.cryptocompare.com/data/price",
      "BTC",
      ListBuffer("USD", "JPY", "EUR")
    )

    println()
    print(rates)

  }
}
