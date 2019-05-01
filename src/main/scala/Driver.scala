import scala.collection.mutable.ListBuffer
import ujson.{Obj, Value}
import scala.util.Random

import scala.collection.mutable

case class Node(id: Int, name: String)

case class Edge(from: Int, to: Int, value: Double)


object Driver {
  def main(args: Array[String]): Unit = {

    val currencyList = Array[String](
      "BTC", "LTC", "NMC", "PPC", "XDG", "GRC", "XPM", "XRP", "NXT", "AUR", "DASH", "NEO", "MZC", "XMR", "XEM",
      "POT", "TIT", "XVG", "XLM", "VTC", "ETH", "ETC", "USDT", "ZEC", "BCH", "EOS", "AFN", "EUR", "ALL", "DZD", "USD",
      "AED", "EUR", "GBP", "AMD", "RUB"
    )

    val random = new Random()
    val randList = (for (_ <- 1 to 16) yield random.nextInt(36)).toArray


    val graph = constructGraph(
      randList,
      0.5,
      currencyList
    )

    postGraph(
      graph,
      randList,
      currencyList
    )

    print("Done!")


  }


  def constructGraph(includeCurrencies: Array[Int], treshhold: Double, currencyList: Array[String]): Graph = {
    val dataCollector = new DataCollector()
    val V = includeCurrencies.length
    var E = 0
    val matrix = Array.ofDim[Double](V, V)

    for (row <- 0 until V) {
      for (col <- 0 until V) {
        val currProb = giveProb(includeCurrencies(row), includeCurrencies(col))
        if (currProb > treshhold)
          E += 1
        matrix(row)(col) = currProb
      }
    }

    val graph = new Graph(V, E)
    var edgeCount = 0

    for (row <- 0 until V) {
      for (col <- 1 until V) {
        if (matrix(row)(col) > treshhold) {
          graph.Edge(edgeCount).src = row
          graph.Edge(edgeCount).dest = col

          val rate = dataCollector.getExchangeRates(
            "https://min-api.cryptocompare.com/data/price",
            currencyList(includeCurrencies(row)),
            ListBuffer(currencyList(includeCurrencies(col)))
          )(currencyList(includeCurrencies(col)))

          graph.Edge(edgeCount).weight = -math.log(rate)
          graph.Edge(edgeCount).weightReal = rate
          edgeCount += 1
        }
      }
    }
    return graph
  }

  def giveProb(source: Int, dest: Int): Double = {
    val r = scala.util.Random
    if (source == dest)
      return 0
    else
      return r.nextFloat
  }

  def postGraph(graph: Graph, includeCurrencies: Array[Int], currencyList: Array[String]): Unit = {

    val nodeList = new ListBuffer[Obj]
    val edgeList = new ListBuffer[Obj]

    for (i <- includeCurrencies.indices) {
      val node: mutable.LinkedHashMap[String, Value] = mutable.LinkedHashMap(
        "id" -> i,
        "name" -> currencyList(includeCurrencies(i)),
        "group" -> (if (graph.cycle.contains(i)) 2 else 1)
      )
      nodeList += new ujson.Obj(node)
    }

    for (edge <- graph.Edge) {
      val edgeObj: mutable.LinkedHashMap[String, Value] = mutable.LinkedHashMap(
        "from" -> edge.src,
        "to" -> edge.dest,
        "value" -> edge.weightReal,
        "group" -> (if (graph.cycle.contains(edge.src) && graph.cycle.contains(edge.src)) 2 else 1)
      )
      edgeList += new ujson.Obj(edgeObj)
    }


    val r = requests.post(
      url = "http://localhost:5000/addGraph",
      headers = Map("Content-Type" -> "application/json"),
      data = Obj(
        "nodes" -> nodeList,
        "edges" -> edgeList
      ).render())

    print(r)
  }


}
